package net.bastkowski.s3viewer.html

import net.bastkowski.s3viewer.aws._

class HtmlBuilderImpl extends HtmlBuilder {

  import scalatags.Text.all._

  override def build(name: String, path: Path, entries: List[DisplayEntry]): String =
    html(lang := "en",
      head(
        meta(charset := "utf-8"),
        meta(viewport, content := "width=device-width, initial-scale=1, shrink-to-fit=no"),

        tag("title")(name),
        link(rel:="stylesheet", href:="/assets/bootstrap.css")),

      body(`class` := "container-fluid",
        nav(aria.label := "breadcrumb", breadcrumbs(name, path)),

        if (entries.isEmpty) emptyDiv
        else                 entryList(entries))
    ).render

  private lazy val viewport = attr("name") := "viewport"
  private lazy val nav      = tag("nav")

  private def breadcrumbs(name: String, path: Path) =
    ol(`class` := "breadcrumb",
      li(`class` := "breadcrumb-item font-weight-bold", a(href := "/", name)),
      for (b <- toBreadcrumbs(path)) yield
        li(`class` := "breadcrumb-item", a(href := b.href, b.text)))

  private def entryList(entries: List[DisplayEntry]) =
    table(`class` := "table",
      thead(
        tr(
          tableHeader("Name"),
          tableHeader("Size"),
          tableHeader("Last Modified"))),
      tbody(entries.map {
        case d@Directory(_, n) =>
          tr(
            td(a(href := d.href, n)),
            td(),
            td())
        case f@File(_, n, s, lastModified) =>
          tr(
            td(a(href := f.href, n)),
            td(s),
            td(lastModified.toString)) }))

  private[this] def tableHeader(s: String) = th(attr("scope") := "col", s)

  private[this] def emptyDiv = div(`class` := "jumbotron", "This folder is empty")

  private[this] def toBreadcrumbs(dir: Path): List[Path] = dir match {
    case Root => Nil
    case Directory(parent, _) => toBreadcrumbs(parent) :+ dir
  }
}
