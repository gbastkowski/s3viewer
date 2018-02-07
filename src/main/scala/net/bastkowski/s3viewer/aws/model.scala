package net.bastkowski.s3viewer.aws

import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import akka.stream.IOResult
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters.{fromInputStream => toSource}
import akka.util.ByteString
import awscala.s3.{S3Object, S3ObjectSummary}

import scala.concurrent.Future

object StreamableObject {
  def toStreamableObject(o: S3Object) = StreamableObject(
    o.metadata.getContentLength,
    toSource(() => o.content))
}

case class StreamableObject(contentLength: Long,
                            source: Source[ByteString, Future[IOResult]])

object DisplayEntry {
  def apply(key: String) = DisplayDirectory(simpleName(key), "/" + key)

  def apply(s3ObjectSummary: S3ObjectSummary) =
    DisplayFile(
      simpleName(s3ObjectSummary.getKey),
      s3ObjectSummary.getSize,
      toLocalDateTime(s3ObjectSummary.getLastModified),
      "/" + s3ObjectSummary.getKey)

  def simpleName(path: String): String = segments(path).lastOption.getOrElse("/")

  def segments(path: String): List[String] = path.split('/').filter(_.nonEmpty).toList

  private[this] def toLocalDateTime(date: Date) =
    LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault)
}

sealed trait Path {
  val parent: Path
  val name: String
}
case class Directory(parent: Directory, name: String)
case class File(parent: Directory, name: String)
case object Root extends Path {
  val parent = this
  val name = ""
}

sealed trait DisplayEntry {
  val name, path: String
  def segments = DisplayEntry.segments(path)
}

case class DisplayFile(name: String,
                        size: Long,
                        lastModified: LocalDateTime,
                        path: String
                      ) extends DisplayEntry

case class DisplayDirectory(name: String, path: String) extends DisplayEntry