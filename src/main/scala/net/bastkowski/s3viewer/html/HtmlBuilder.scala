package net.bastkowski.s3viewer.html

import net.bastkowski.s3viewer.aws.{DisplayEntry, Path}

trait HtmlBuilder {
  def build(name: String, path: Path, entries: List[DisplayEntry]): String
}
