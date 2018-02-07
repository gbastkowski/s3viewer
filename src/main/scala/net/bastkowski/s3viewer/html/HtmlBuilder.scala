package net.bastkowski.s3viewer.html

import akka.http.scaladsl.model.Uri
import net.bastkowski.s3viewer.aws.{DisplayDirectory, DisplayEntry}
import org.fusesource.scalate.{Binding, TemplateEngine}

class HtmlBuilder {
  type Breadcrumbs = List[DisplayDirectory]
  type Segments    = List[String]

  private[this] val engine = new TemplateEngine
  engine.importStatements :+= "import net.bastkowski.s3viewer.aws._"
  engine.bindings = List(
    Binding("name",        "String"),
    Binding("breadcrumbs", "List[DisplayDirectory]"),
    Binding("entries",     "List[DisplayEntry]")
  )

  def build(name: String, path: DisplayDirectory, entries: List[DisplayEntry]): String =
    engine.layout("/index.jade", Map(
        "name"        -> name,
        "breadcrumbs" -> toBreadcrumbs(path),
        "entries"     -> entries))

  private[this] def toBreadcrumbs(dir: DisplayDirectory): Breadcrumbs = {
    def toPath(segments: Segments)  = segments.fold("")(_ + '/' + _)
    def toBreadcrumb(p: Segments)   = DisplayEntry(toPath(p))

    def nestedListOfSegments(n: Segments): List[Segments] =
      if (n.isEmpty)  Nil
      else            nestedListOfSegments(n.dropRight(1)) :+ n

    nestedListOfSegments(dir.segments) map toBreadcrumb
  }

}
