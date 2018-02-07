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
import net.bastkowski.s3viewer.html.HtmlBuilder

import scala.concurrent.ExecutionContextExecutor

class ServiceImpl(interface: String, port: Int, override val bucket: S3BucketAdapter) extends Service {
  override implicit val system: ActorSystem = ActorSystem()
  override implicit val executor: ExecutionContextExecutor = system.dispatcher
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  override val logger = Logging(system, getClass)
  override val htmlBuilder = new HtmlBuilder()

  Http().bindAndHandle(routes, interface, port)
}

object S3Viewer extends App {
  val config = ConfigFactory.load()
  new ServiceImpl(
    config.getString("http.interface"),
    config.getInt("http.port"),
    new S3Storage(config.getString("aws.bucket"), config.getString("aws.region")))
}

object MockViewer  {
  val config = ConfigFactory.load()
  new ServiceImpl(
    config.getString("http.interface"),
    config.getInt("http.port"),
    new S3BucketAdapter {
      def name = "Mock"
      def ls(path: Uri.Path): Stream[DisplayEntry] = path.toString match {
        case "/" | "" => Seq(
          DisplayDirectory("directory/", "/directory/"),
          DisplayFile("a", 1l, LocalDateTime.now, "/a")).toStream
        case "/directory" => Seq(
          DisplayDirectory("b/", "/b/"),
          DisplayFile("asdf.tgz", 2l, LocalDateTime.now, "/asdf.tgz")).toStream
        case _ => Nil.toStream
      }

      def get(path: Uri.Path): Option[StreamableObject] = ???
    })
}