import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import akka.http.scaladsl.model.Uri
import awscala.s3.{Bucket, S3, S3Object, S3ObjectSummary}

class S3Storage(bucket: Bucket)(implicit s3: S3) extends S3BucketAdapter {
  def get(path: Uri.Path): Option[S3Object] = bucket.get(path.toString)

  def ls(path: Uri.Path): Seq[Either[String, S3ObjectSummary]] = bucket ls path.toString
}
