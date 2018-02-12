package net.bastkowski.s3viewer.html

import net.bastkowski.s3viewer.aws.{Directory, DisplayEntry, Path, Root}
import org.fusesource.scalate.{Binding, TemplateEngine}

class HtmlBuilder {
  def build(name: String, path: Path, entries: List[DisplayEntry]): String =
    engine.layout("/index.jade", Map(
      "name"        -> name,
      "breadcrumbs" -> toBreadcrumbs(path),
      "entries"     -> entries))


  private[this] val engine = new TemplateEngine
  engine.importStatements :+= "import net.bastkowski.s3viewer.aws._"
  engine.bindings = List(
    Binding("name",        "String"),
    Binding("breadcrumbs", "List[Path]"),
    Binding("entries",     "List[DisplayEntry]")
  )

  private[this] def toBreadcrumbs(dir: Path): List[Path] = dir match {
    case Root => Nil
    case Directory(parent, _) => toBreadcrumbs(parent) :+ dir
  }
}
