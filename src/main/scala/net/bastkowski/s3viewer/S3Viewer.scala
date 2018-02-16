package net.bastkowski.s3viewer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import net.bastkowski.s3viewer.aws._
import net.bastkowski.s3viewer.html._
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.ExecutionContextExecutor

class WebServer(interface: String, port: Int, override val backend: Backend) extends Service with Logging {
   implicit val system: ActorSystem = ActorSystem()
   implicit val executor: ExecutionContextExecutor = system.dispatcher
   implicit val materializer: ActorMaterializer = ActorMaterializer()

  for (binding <- Http().bindAndHandle(routes, interface, port)) {
    logger.info(s"Webserver listening on ${binding.localAddress}")
  }
}

case class Config(bucket: String, region: String, interface: String, port: Int)

object S3Viewer extends App {
  val config = {
    import ConfigFactory.{systemEnvironment => env}
    Config(env.getString("BUCKET"), env.getString("REGION"), env.getString("HTTP_INTERFACE"), env.getInt("HTTP_PORT")
  )}

  lazy val storage = new S3Storage(config.bucket, config.region)
  lazy val view    = new HtmlBuilderImpl()
  lazy val backend = new BackendImpl(storage, Assets, view)

  new WebServer(config.interface, config.port, backend)
}

object MockViewer {
  val config = ConfigFactory.load()
  new WebServer(
    config.getString("http.interface"),
    config.getInt("http.port"),
    MockBackend)

  private object MockBackend extends Backend {

    def get(path: Uri.Path): Option[StreamableObject] = ???

    override def isDirectory(path: Uri.Path): Boolean = ???

    override def getAsset(path: Uri.Path): ToResponseMarshallable = ???

    override def downloadFile(path: Uri.Path): Option[ToResponseMarshallable] = ???

    override def listDirectory(path: Uri.Path): ToResponseMarshallable = ???
  }

}
