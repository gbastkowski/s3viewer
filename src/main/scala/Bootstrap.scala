import java.io.InputStream

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.ContentType.WithCharset
import akka.http.scaladsl.model.HttpCharsets.`UTF-8`
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.{`application/javascript`, `text/css`}
import akka.stream.scaladsl.StreamConverters

trait InputStreamSource {
  protected[this] def asSource(in: InputStream) = StreamConverters.fromInputStream(() => in)
}

object Assets extends InputStreamSource {
  def `bootstrap.css`: ToResponseMarshallable = HttpEntity(
    WithCharset(`text/css`, `UTF-8`),
    asSource(getClass.getResourceAsStream("/META-INF/resources/webjars/bootstrap/4.0.0/css/bootstrap.css")))


  def `bootstrap.js`: ToResponseMarshallable = HttpEntity(
    WithCharset(`application/javascript`, `UTF-8`),
    asSource(getClass.getClassLoader.getResourceAsStream("META-INF/resources/webjars/bootstrap/4.0.0/js/bootstrap.js")))

  def `jquery.js`: ToResponseMarshallable = HttpEntity(
    WithCharset(`text/css`, `UTF-8`),
    asSource(getClass.getResourceAsStream("/META-INF/resources/webjars/jquery/3.3.1/jquery.js")))

  def `popper.js`: ToResponseMarshallable = HttpEntity(
    WithCharset(`text/css`, `UTF-8`),
    asSource(getClass.getResourceAsStream("/META-INF/resources/webjars/popper.js/1.13.0/dist/popper.js")))
}