package net.bastkowski.s3viewer.aws

import akka.http.scaladsl.model.Uri
import awscala.Region0
import awscala.s3.{Bucket, S3}
import StreamableObject.toStreamableObject

class S3Storage(bucketName: String, region: String) extends S3BucketAdapter {
  private[this] implicit val s3: S3 = S3.at(Region0(region))
  private[this] val bucket: Bucket = Bucket(bucketName)

  def name = bucketName

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
  def ls(path: Uri.Path): Stream[DisplayEntry] = bucket.ls(path.toString).map {
    case Left(string)         => DisplayEntry(string)
    case Right(objectSummary) => DisplayEntry(objectSummary)
  }

}
