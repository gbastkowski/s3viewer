package net.bastkowski.s3viewer.aws.model

import net.bastkowski.s3viewer.aws.{Directory, DisplayEntry, Root}
import org.scalatest.FreeSpec

class PathSpec extends FreeSpec {

  "A path " - {
    "with no segments" - {
      "should be a root" in {
        assert(DisplayEntry.apply2(Nil) == Root)
      }
    }
    "with one segment" - {
      "should have a parent" in {
        assert(DisplayEntry.apply2("a" :: Nil) == Directory(Root, "a"))
      }
    }
    "with an empty string" - {
      "should be root" in {
        assert(DisplayEntry.apply2("") == Root)
      }
    }
    "with only a single slash" - {
      "should be root" in {
        assert(DisplayEntry.apply2("/") == Root)
      }
    }
    "without slashes" - {
      "should be a child of root" in {
        assert(DisplayEntry.apply2("a") == Directory(Root, "a"))
      }
    }
    "without one slash" - {
      "should be a grandchild of root" in {
        assert(DisplayEntry.apply2("a/b") == Directory(Directory(Root, "a"), "b"))
      }
    }
    "does not differentiate between relative and absolute" in {
      assert(DisplayEntry.apply2("a/b") == DisplayEntry.apply2("/a/b"))
    }
    "with two parents" - {
      "should reveal them in the correct order" in {
        val grandchild = Directory(Directory(Root, "a"), "b")
        assert(grandchild.parent == Directory(Root, "a"))
      }
    }
  }

  "A DisplayEntry" - {
    "For Root" - {
      "should show a slash" in {
        assert(Root.href == "/")
      }
    }
    "For any other directory" - {
      "should href correctly with a trailing slash" in {
        assert(Directory(Directory(Root, "a"), "b").href == "/a/b/")
      }
    }
  }

}
