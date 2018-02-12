package net.bastkowski.s3viewer

import java.time.LocalDateTime
import java.util.Date

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import net.bastkowski.s3viewer.aws._
import net.bastkowski.s3viewer.html._

import scala.concurrent.ExecutionContextExecutor

class WebServer(interface: String, port: Int, override val bucket: S3BucketAdapter) extends Service {
   implicit val system: ActorSystem = ActorSystem()
   implicit val executor: ExecutionContextExecutor = system.dispatcher
   implicit val materializer: ActorMaterializer = ActorMaterializer()

  override val htmlBuilder = new HtmlBuilder()
  override def assets: Assets = Assets

  Http().bindAndHandle(routes, interface, port)
}

object S3Viewer extends App {
  import ConfigFactory.{systemEnvironment => env}
  val config = ConfigFactory.load()

  new WebServer(
    config.getString("http.interface"),
    config.getInt("http.port"),
    new S3Storage(env.getString("BUCKET"), env.getString("REGION")))
}

object MockViewer {
  val config = ConfigFactory.load()
  new WebServer(
    config.getString("http.interface"),
    config.getInt("http.port"),
    MockBucket)

  private object MockBucket extends S3BucketAdapter {
    def name = "Mock"

    def ls(path: Uri.Path): List[DisplayEntry] = path.toString match {
      case "/" | ""     => root
      case "/directory" => directory
      case _            => entries()
    }

    private[this] def directory = {
      entries(
        DisplayEntry.apply2("/b/"),
        File(Root, "asdf.tgz", 2l, LocalDateTime.now))
    }

    private[this] def root = {
      entries(
        DisplayEntry.apply2("/directory/"),
        File(Root, "a", 1l, LocalDateTime.now))
    }

    private[this] def entries(e: DisplayEntry*) ={
      e.toList
    }

    def get(path: Uri.Path): Option[StreamableObject] = ???
  }

}
