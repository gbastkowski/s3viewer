package net.bastkowski.s3viewer.aws

import akka.http.scaladsl.model.Uri

/**
  * Encapsulates operations on S3.
  */
trait S3BucketAdapter {
  def name: String
  def get(path: Uri.Path): Option[StreamableObject]
  def ls(path: Uri.Path): List[DisplayEntry]
}
