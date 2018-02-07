import net.bastkowski.s3viewer.html.DisplayDirectory
import org.scalatest.FreeSpec

class DisplayDirectorySpec extends FreeSpec {

  object Breadcrumbs {
    def apply(path: String): List[DisplayDirectory] = {
      val segments = path.split('/').filter(_.nonEmpty).toList
      val paths    = toListOfPaths(segments)
      paths map { p =>
        DisplayDirectory(p.last, p.fold("")(_ + '/' + _))
      }
    }


  }

  import Breadcrumbs.toListOfPaths
  "An empty list" - {
    "should answer an empty list" in {
      assert { toListOfPaths(Nil) == Nil }
    }
  }
  "A single segment" - {
    "should answer a single breadcrumb" in {
      assert {
        toListOfPaths("a" :: Nil) == List(List("a"))
      }
    }
  }
  "Two segments" - {
    "should answer two breadcrumbs" in {
      assert { toListOfPaths("a" :: "b" :: Nil) == List(
        "a" :: Nil,
        "a" :: "b" :: Nil)}
    }
  }

  "A path" - {
    "with just a single slash" - {
      "should answer no breadcrumbs" in {
        assert { Breadcrumbs("/").isEmpty }
      }
    }
    "with 1 segment" - {
      "should answer one breadcrumb" in {
        assert { Breadcrumbs("/a") == Seq(DisplayDirectory("a", "/a"))}
      }
    }
    "with a trailing slash" - {
      "should remove it in name" in {
        assert { Breadcrumbs("/a/") == Seq(DisplayDirectory("a", "/a"))}
      }
    }
    "with 3 segments" - {
      "should answer 3 breadcrumbs" in {
        assert {
          Breadcrumbs("/a/b/c") == Seq(
            DisplayDirectory("a", "/a"),
            DisplayDirectory("b", "/a/b"),
            DisplayDirectory("c", "/a/b/c")
          )
        }
      }
    }
  }

}
