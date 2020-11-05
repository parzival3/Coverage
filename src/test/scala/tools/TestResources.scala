package tools

import chiseluvm.Coverage
import org.scalatest.Suite
import org.scalatest.flatspec.AnyFlatSpec

class TestResources extends AnyFlatSpec {

  "Resources" should "be available" in {

    println(classOf[Suite].getClassLoader.getResource("org/scalatest/HtmlReporter.css"))
    println(classOf[Coverage].getClassLoader.getResource("css/styles.css"))
    println(classOf[Coverage].getResource("/css/styles.css"))
  }
}
