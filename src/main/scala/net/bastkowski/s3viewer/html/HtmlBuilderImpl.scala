package net.bastkowski.s3viewer.html

import java.text._
import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}

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
      li(`class` := "breadcrumb-item font-weight-bold", a(href := "/ui/", name)),
      for (b <- toBreadcrumbs(path).dropRight(1)) yield
        li(`class` := "breadcrumb-item", a(href := s"/ui${b.href}", b.text)),
      toBreadcrumbs(path).lastOption.map { x =>
        li(`class` := "breadcrumb-item active", attr("aria-current") := "page", x.text)
      }
    )

  private def entryList(entries: List[DisplayEntry]) =
    table(`class` := "table table-hover",
      thead(
        tr(
          tableHeader("Name"),
          tableHeader("Size"),
          tableHeader("Last Modified"))),
      tbody(entries.map {
        case d@Directory(_, n) => tr(
          td(width := "50%", a(href := s"/ui${d.href}", n)),
          td,
          td)
        case f@File(_, n, s, lastModified) =>
          tr(
            td(width := "50%", a(href := f.href, n)),
            td(width := "25%", format(s)),
            td(width := "25%", format(lastModified))) }))

  private[this] def tableHeader(s: String) = th(attr("scope") := "col", s)

  private[this] def emptyDiv = div(`class` := "jumbotron", "This folder is empty")

  private[this] def format(number: Long) = NumberFormat.getInstance().format(number)
  private[this] def format(date: LocalDateTime) = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(date)

  private[this] def toBreadcrumbs(dir: Path): List[Path] = dir match {
    case Root => Nil
    case Directory(parent, _) => toBreadcrumbs(parent) :+ dir
  }
}
