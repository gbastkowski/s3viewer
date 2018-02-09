package net.bastkowski.s3viewer.aws

import akka.http.scaladsl.model.Uri
import awscala.Region0
import awscala.s3.{Bucket, S3, S3ObjectSummary}
import StreamableObject.toStreamableObject

class S3Storage(bucketName: String, region: String) extends S3BucketAdapter {
  private[this] implicit val s3: S3 = S3.at(Region0(region))
  private[this] val bucket: Bucket = Bucket(bucketName)

  def name = bucketName
  println(s"Connected with bucket $name")

  /**
    * @param path location of an object
    * @return some bucket or None
    */
  def get(path: Uri.Path): Option[StreamableObject] =
    bucket.get(path.toString).map(toStreamableObject)

  /**
    * @param path path to list
    * @return a list of subpaths (left) or S3 objects (right)
    */
  def ls(path: Uri.Path): List[DisplayEntry] =
    listAndFilter(path).toList.filter(isNotMe(path.toString())).map(toDisplayEntry)

  private[this] val toDisplayEntry: Either[String, S3ObjectSummary] => DisplayEntry = {
    case Left(string) => DisplayEntry(string)
    case Right(objectSummary) => DisplayEntry(objectSummary)
  }

  private def listAndFilter(path: Uri.Path) =
    bucket.ls(path.toString).filter(isNotMe(path.toString)).map(log)

  private def isNotMe(path: String)(e: Either[String, S3ObjectSummary]) = e match {
    case Right(o) => o.getKey != path
    case _ => true
  }

  private[this] def log(x: Either[String, S3ObjectSummary]) = {
    x match {
      case Left(s) => println("  - dir " + s)
      case Right(o) => println("  - obj " + o.getKey)
    }
    x
  }

}
