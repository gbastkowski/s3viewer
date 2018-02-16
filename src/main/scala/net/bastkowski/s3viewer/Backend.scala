package net.bastkowski.s3viewer

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import net.bastkowski.s3viewer.aws.{DisplayEntry, S3BucketAdapter}
import net.bastkowski.s3viewer.html.{Assets, HtmlBuilder}

trait Backend {
  def isDirectory(path: Uri.Path):    Boolean
  def getAsset(path: Uri.Path):       ToResponseMarshallable
  def downloadFile(path: Uri.Path):   Option[ToResponseMarshallable]
  def listDirectory(path: Uri.Path):  ToResponseMarshallable
}

class BackendImpl(bucket: S3BucketAdapter, assets: Assets, htmlBuilder: HtmlBuilder) extends Backend {
  import ContentTypes._

  def isDirectory(path: Uri.Path): Boolean = path.isEmpty || path.endsWithSlash

  def getAsset(path: Uri.Path): ToResponseMarshallable = assets.getAsset(path)

  def downloadFile(path: Uri.Path): Option[ToResponseMarshallable] =
    bucket.get(path).map { o =>
      HttpEntity(`application/octet-stream`, o.contentLength, o.source)
    }

  def listDirectory(path: Uri.Path): ToResponseMarshallable = HttpEntity(
    `text/html(UTF-8)`,
    htmlBuilder.build(bucket.name, DisplayEntry(path.toString + "/"), bucket.ls(path))
  )
}
