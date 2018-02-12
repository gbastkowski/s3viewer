package net.bastkowski.s3viewer.html

import java.io.InputStream

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.ContentType.WithCharset
import akka.http.scaladsl.model.HttpCharsets.`UTF-8`
import akka.http.scaladsl.model.MediaTypes.{`application/javascript`, `text/css`}
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.{HttpEntity, Uri}
import akka.stream.scaladsl.StreamConverters

trait Assets {
  def getAsset(path: Uri.Path): ToResponseMarshallable
}

object Assets extends Assets {
  val prefix: String = "/assets"

  private[this] val entries = Map(
    "bootstrap.css" -> (`text/css`, "/META-INF/resources/webjars/bootstrap/4.0.0/css/bootstrap.css"),
    "bootstrap.js" -> (`application/javascript`, "/META-INF/resources/webjars/bootstrap/4.0.0/js/bootstrap.js"),
    "jquery.js" -> (`application/javascript`, "/META-INF/resources/webjars/jquery/3.3.1/jquery.js"),
    "popper.js" -> (`application/javascript`, "/META-INF/resources/webjars/popper.js/1.13.0/dist/popper.js")
  )

  def getAsset(path: Uri.Path) = apply(path)

  def apply(path: Uri.Path): ToResponseMarshallable = entries.get(path.toString) match {
    case Some((mediaType, name)) => HttpEntity(
      WithCharset(mediaType, `UTF-8`),
      asSource(getClass.getResourceAsStream(name)))
    case None => NotFound
  }

  private[this] def asSource(in: InputStream) = StreamConverters.fromInputStream(() => in)
}