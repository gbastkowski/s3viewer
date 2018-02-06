import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import akka.http.javadsl.model.headers.LastModified
import awscala.s3.S3ObjectSummary
import org.fusesource.scalate.TemplateEngine

sealed trait DisplayEntry {
  val name, path: String
}
case class DisplayDirectory(name: String, path: String) extends DisplayEntry
case class DisplayFile(name: String, size: Long, lastModified: LocalDateTime, path: String) extends DisplayEntry

class HtmlBuilder {
  type S3SomeEntry = Either[String, S3ObjectSummary]
  type S3Entries   = Seq[S3SomeEntry]

  private[this] val engine = new TemplateEngine

  def output = engine.layout("/index.jade")

  def build(path: String, entries: S3Entries): String = render(path, entries.map {
    case Left(dire)           => DisplayDirectory(dire.split('/').last, dire)
    case Right(objectSummary) => DisplayFile(
      objectSummary.getKey.split('/').last,
      objectSummary.getSize,
      toLocalDateTime(objectSummary.getLastModified),
      objectSummary.getKey)
  })

  def render(path: String, entries: Seq[DisplayEntry]): String =
    s"""<!DOCTYPE html>
       |<html>
       |  <head>
       |    <meta charset="utf-8">
       |    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
       |
       |    <title>Test</title>
       |    <link rel="stylesheet" href="/assets/bootstrap.css">
       |  </head>
       |  <body>
       |    <div class="container-fluid">
       |
       |      <nav aria-label="breadcrumb">
       |        <ol class="breadcrumb">
       |          <li class="breadcrumb-item font-weight-bold"><a href="/">N4OS Storage</a></li>
       |          ${toBreadcrumbs(path)}
       |        </ol>
       |      </nav>
       |
       |      <table class="table">
       |        <thead>
       |          <tr>
       |            <th scope="col">name</th>
       |            <th scope="col">size</th>
       |            <th scope="col">last modified</th>
       |          </tr>
       |        </thead>
       |        <tbody>
       |          ${toList(entries)}
       |        </tbody>
       |      </table>
       |    </div>
       |
       |    <script src="/assets/jquery.js"></script>
       |    <script src="/assets/popper.js"></script>
       |    <script src="/assets/bootstrap.js"></script>
       |  </body>
       |</html>
       |""".stripMargin

  private[this] def toBreadcrumbs(path: String) = path.split('/').map { segment =>
    s"""<li class="breadcrumb-item">
       |  <a href="$segment">$segment</a>
       |</li>
       |""".stripMargin
  }.fold("")(_ + _)

  private[this] def toList(entries: Seq[DisplayEntry]) = entries.map(toLink).fold("")(_ + _)

  private[this] def toLink(e: DisplayEntry) = e match {
    case DisplayDirectory(dir, path)  =>
      s"""<tr>
         |  <td><a href="$path">$dir/</a></td><td></td><td></td>
         |</tr>
         |""".stripMargin
    case DisplayFile(name, size, lastModified, path) =>
      s"""<tr>
         |  <td><a href="$path">$name</a></td>
         |  <td>$size</td>
         |  <td>$lastModified</td>
         |</tr>
         |""".stripMargin
  }

  private[this] def toLocalDateTime(date: Date) = LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault)
}
