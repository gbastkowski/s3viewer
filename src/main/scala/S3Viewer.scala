import java.io.InputStream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import awscala._
import awscala.s3.{Bucket, S3, S3Object, S3ObjectSummary}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

trait S3BucketAdapter {
  def get(path: Uri.Path): Option[S3Object]
  def ls(path: Uri.Path): Seq[Either[String, S3ObjectSummary]]
}

trait Service {
  import ContentTypes.{`application/octet-stream`, `text/html(UTF-8)`}
  import StatusCodes._
  import Assets._

  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def bucket: S3BucketAdapter
  def htmlBuilder: HtmlBuilder
  def config: Config

  val logger: LoggingAdapter

  val routes: Route = {
    get {
      pathPrefix("assets") {
        path("bootstrap.css") {
          complete {
            `bootstrap.css`
          }
        } ~
        path("bootstrap.js") {
          complete {
            `bootstrap.js`
          }
        } ~
          path("jquery.js") {
            complete {
              `jquery.js`
            }
          } ~
          path("popper.js") {
            complete {
              `popper.js`
            }
          }
      } ~
      pathPrefix(RemainingPath) { path =>
        complete {
          if (path.isEmpty || path.endsWithSlash) listDirectory(path)
          else                                    downloadFile(path)
        }
      }
    }
  }

  private[this] def downloadFile(path: Uri.Path): ToResponseMarshallable = bucket.get(path) match {
    case Some(obj) => HttpEntity(`application/octet-stream`,
                                  obj.metadata.getContentLength,
                                  asSource(obj.content))
    case None      => NotFound
  }

  private def asSource(in: InputStream) = StreamConverters.fromInputStream(() => in)

  private def listDirectory(path: Uri.Path): ToResponseMarshallable = HttpEntity(
    `text/html(UTF-8)`,
    htmlBuilder.build(path.toString, bucket.ls(path).toList))
}

object S3Viewer extends App with Service {
  override implicit val system:       ActorSystem              = ActorSystem()
  override implicit val executor:     ExecutionContextExecutor = system.dispatcher
  override implicit val materializer: ActorMaterializer        = ActorMaterializer()
  implicit val          s3:           S3                       = S3.at(Region.EU_WEST_1)

  override val          bucket: S3BucketAdapter                = new S3Storage(Bucket("n4-cd-n4os-storage"))
  override val          htmlBuilder: HtmlBuilder               = new HtmlBuilder()
  override val          config                                 = ConfigFactory.load()
  override val          logger                                 = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}

