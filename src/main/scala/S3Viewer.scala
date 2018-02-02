import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source, StreamConverters}
import akka.util.ByteString
import org.apache.http.HttpHeaders

trait Service {

  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def config: Config

  val logger: LoggingAdapter

  val routes = {
    pathPrefix(RemainingPath) { path =>
      get {
        complete {
          import awscala._, s3._
          implicit val _ = S3.at(Region.EU_WEST_1)
          val bucket = Bucket("n4-cd-n4os-storage")

          if (path.isEmpty || path.endsWithSlash) {
            val strings = bucket ls path.toString map {
              case Left(s) => {
                val dir = s
                "<a href=\"" + dir + "\">" + dir + "</a>"
              }
              case Right(o) => "<a href=\"" + o.getKey + "\">" +
                "<span class=\"name\">" + o.getKey + "</span>" +
                "<span class=\"date\">" + o.getLastModified + "</span>" +
                "<span class=\"size\">" + o.getSize + "</span>" +
                "</a>"
            }
            val y = strings.
              map("<li>" + _ + "</li>").
              fold("")(_ + '\n' + _)
            HttpEntity(ContentTypes.`text/html(UTF-8)`,
              s"""<!DOCTYPE html>
                 |<html>
                 |  <head><title>Test</head></title>
                 |  <body>
                 |    <h1>$path</h1>
                 |    <ul>
                 |      $y
                 |    </ul>
                 |  </body>
                 |</html>
          """.stripMargin
            )
          } else {
            bucket.get(path.toString) match {
              case Some(obj) =>
                HttpEntity(
                  ContentTypes.`application/octet-stream`,
                  StreamConverters.fromInputStream(() => bucket.get(path.toString).get.content)
                )
              case None => StatusCodes.NotFound
            }
          }
        }
      }
    }
  }
}

object S3Viewer extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}

