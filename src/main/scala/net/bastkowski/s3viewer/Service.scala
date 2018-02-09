package net.bastkowski.s3viewer

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.{complete, get, pathPrefix, _}
import akka.http.scaladsl.server.{PathMatchers, Route}
import akka.stream.ActorMaterializer
import net.bastkowski.s3viewer.aws.{DisplayEntry, S3BucketAdapter}
import net.bastkowski.s3viewer.html.{Assets, HtmlBuilder}

import scala.concurrent.ExecutionContextExecutor

trait Service {
  import ContentTypes.{`application/octet-stream`, `text/html(UTF-8)`}
  import StatusCodes._

  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def bucket: S3BucketAdapter
  def htmlBuilder: HtmlBuilder

  val logger: LoggingAdapter

  val routes: Route = get {
    pathPrefix("assets" / RemainingPath) { path =>
      complete {
        Assets(path)
      }
    } ~
    path(RemainingPath)  {path =>
      complete {
        if (path.isEmpty || path.endsWithSlash) listDirectory(path)
        else                                    downloadFile(path)
      }
    }
  }

  private[this] def downloadFile(path: Uri.Path): ToResponseMarshallable = bucket.get(path) match {
    case Some(obj) => HttpEntity(`application/octet-stream`, obj.contentLength, obj.source)
    case None      => NotFound
  }

  private[this] def listDirectory(path: Uri.Path): ToResponseMarshallable = {
    println(path)
    HttpEntity(
    `text/html(UTF-8)`,
    htmlBuilder.build(bucket.name, DisplayEntry(path.toString + "/"), bucket.ls(path).toList))}
}
