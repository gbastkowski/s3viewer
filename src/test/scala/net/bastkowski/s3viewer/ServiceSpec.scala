package net.bastkowski.s3viewer

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString
import net.bastkowski.s3viewer.aws.{DisplayEntry, Path, S3BucketAdapter, StreamableObject}
import net.bastkowski.s3viewer.html.{Assets, HtmlBuilder}
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class ServiceSpec extends FreeSpec with Matchers with ScalatestRouteTest with Service {
    override implicit val executor: ExecutionContextExecutor = system.dispatcher

    override def backend: Backend = new Backend {
      override def getAsset(path: Uri.Path): ToResponseMarshallable = path match {
        case Path("bootstrap.css") => OK
        case Path("Idontexist.css") => NotFound
        case x => HttpResponse(
          status = StatusCodes.NotImplemented,
          entity = HttpEntity(s"$x not expected"))
      }

      override def downloadFile(path: Uri.Path): Option[ToResponseMarshallable] = path match {
        case Path("b") => Some("download")
        case Path("non-existing") => None
        case x => Some(s"$x not expected")
      }

      override def listDirectory(path: Uri.Path): ToResponseMarshallable = path match {
        case Path("") => "index"
        case Path("a/") => "/listing/"
        case x => s"$x not expected"
      }

      override def isDirectory(path: Uri.Path): Boolean = path.endsWithSlash || path.isEmpty
    }

  "A path prefixed with /assets" - {
    "with an existing asset" in {
      Get("/assets/bootstrap.css") ~> routes ~> check {
        response.status shouldEqual OK
      }
    }
    "with an unknown asset" in {
      Get("/assets/Idontexist.css") ~> routes ~> check {
        response.status shouldEqual NotFound
      }
    }
  }

  "An empty path" - {
    "without leading slash" in {
      Get() ~> routes ~> check {
        responseAs[String] shouldEqual "index"
      }
    }
    "with a leading slash" in {
      Get("/") ~> routes ~> check {
        responseAs[String] shouldEqual "index"
      }
    }
  }

  "A path" - {
    "with a leading and a trailing slash" in {
      Get("/a/") ~> routes ~> check {
        responseAs[String] shouldEqual "/listing/"
      }
    }
    "without a leading slash" in {
      Get("a/") ~> routes ~> check {
        response.status shouldEqual NotFound
      }
    }
    "without a trailing slash" in {
      Get("/b") ~> routes ~> check {
        responseAs[String] shouldEqual "download"
      }
    }
    "to a non-existing file" in {
      Get("/non-existing") ~> routes ~> check {
        response.status shouldEqual NotFound
      }
    }
  }

}
