import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import awscala.s3.S3ObjectSummary
import org.fusesource.scalate.{Binding, TemplateEngine}

sealed trait DisplayEntry {
  val name, path: String
}
case class DisplayDirectory(
                       name: String,
                       path: String
                     ) extends DisplayEntry

case class DisplayFile(
                       name: String,
                       size: Long,
                       lastModified: LocalDateTime,
                       path: String
                     ) extends DisplayEntry

class HtmlBuilder {
  type S3SomeEntry = Either[String, S3ObjectSummary]
  type S3Entries   = List[S3SomeEntry]
  type Segment     = String
  type Segments    = List[Segment]

  private[this] val engine = new TemplateEngine
  engine.bindings = List(
    Binding("breadcrumbs", "List[DisplayDirectory]"),
    Binding("entries",     "List[DisplayEntry]")
  )

  def build(path: String, entries: S3Entries): String = render(path, entries.map {
    case Left(dire)           => DisplayDirectory(dire.split('/').last, dire)
    case Right(objectSummary) => DisplayFile(
      simpleName(objectSummary.getKey),
      objectSummary.getSize,
      toLocalDateTime(objectSummary.getLastModified),
      objectSummary.getKey)
  })

  private[this] def render(path: String, entries: List[DisplayEntry]) = engine.layout(
    "/index.jade",
    Map(
      "breadcrumbs" -> segments(path).map{ segment => DisplayDirectory(segment, segment) },
      "entries"     -> entries)
  )

  private[this] def simpleName(path: String): Segment = segments(path).last
  private[this] def segments(path: String): Segments = path.split('/').filter(_.nonEmpty).toList

  private[this] def toLocalDateTime(date: Date) = LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault)
}
