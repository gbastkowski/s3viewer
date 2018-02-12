package net.bastkowski.s3viewer

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.StreamConverters
import net.bastkowski.s3viewer.aws.{DisplayEntry, Path, S3BucketAdapter, StreamableObject}
import net.bastkowski.s3viewer.html.{Assets, HtmlBuilder}
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class ServiceSpec extends FreeSpec with Matchers with ScalatestRouteTest with Service {
    override implicit val executor: ExecutionContextExecutor = system.dispatcher

    private[this] def testCases(p: String) = p match {
      case "" | "/" => "root"
      case "a"      => "/listing/"
      case "b"      => "b"
      case x        => s"Path $x is not a valid test case"
    }

    override val bucket = new S3BucketAdapter {
      override def ls(path: Uri.Path) = Nil
      override def get(path: Uri.Path) = path match {
        case Uri.Path("b")  => Some(StreamableObject(8l, testSource))
        case _              => None
      }

      private val testSource =
        StreamConverters.fromInputStream(() => new ByteArrayInputStream("download".getBytes(StandardCharsets.UTF_8)))

      override val name = "Test"
    }

    override def assets: Assets = Assets

    override def htmlBuilder: HtmlBuilder = (_: String, path: Path, _: List[DisplayEntry]) => testCases(path.name)

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
        responseAs[String] shouldEqual "root"
      }
    }
    "with a leading slash" in {
      Get("/") ~> routes ~> check {
        responseAs[String] shouldEqual "root"
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
