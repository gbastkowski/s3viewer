package net.bastkowski.s3viewer.aws

import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import akka.http.javadsl.model.headers.LastModified
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
  def apply2(path: String): Path = apply2(segments(path).reverse)
  def apply2(segments: List[String]): Path = segments match {
    case head :: tail => Directory(apply2(tail), head)
    case Nil          => Root
  }

  def apply(key: String) = apply2(key)

  def apply(s3ObjectSummary: S3ObjectSummary) = segments(s3ObjectSummary.getKey).reverse match {
    case head :: tail => File(apply2(tail), head,s3ObjectSummary.getSize,
      toLocalDateTime(s3ObjectSummary.getLastModified))
  }

  def simpleName(path: String): String = segments(path).lastOption.getOrElse("/")

  def segments(path: String): List[String] = path.split('/').filter(_.nonEmpty).toList

  private[this] def toLocalDateTime(date: Date) =
    LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault)
}

sealed trait DisplayEntry {
  def href: String
  def text: String
}

sealed trait Path extends DisplayEntry {
  val parent: Path
  val name: String
  def text: String = name
  def href: String = segments.reverse.fold("")(_ + _)

  private def segments: List[String] = this match {
    case Root => "/" :: Nil
    case path => (path.name + "/") :: parent.segments
  }
}

case class Directory(parent: Path, name: String) extends Path
case class File(path: Path, name: String, size: Long, lastModified: LocalDateTime) extends DisplayEntry {
  def text: String = name
  def href: String = path.href + name
}

case object Root extends Path {
  val parent: Path = this
  val name = ""
}
