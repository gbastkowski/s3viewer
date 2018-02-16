package net.bastkowski.s3viewer

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.{complete, get, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor


trait Service {

  import StatusCodes._

  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def backend: Backend

  val routes: Route =
    get {
      pathPrefix("ui" / RemainingPath) { directory =>
        complete {
          backend.listDirectory(directory)
        }
      } ~
      pathPrefix("assets" / RemainingPath) { asset =>
        complete {
          backend.getAsset(asset)
        }
      } ~
      path(RemainingPath) { path =>
        complete {
          if (isDirectory(path)) backend.listDirectory(path)
          else backend.downloadFile(path).getOrElse(NotFound)
        }
      } ~
      complete {
        NotFound
      }
    } ~
    (put | post | delete) {
      complete {
        MethodNotAllowed
      }
    }

  private[this] def isDirectory(path: Uri.Path) = {
    backend.isDirectory(path)
  }
}

