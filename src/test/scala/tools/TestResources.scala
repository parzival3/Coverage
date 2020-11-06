package tools

import chiseluvm.CoverageTrait
import org.scalatest.Suite
import org.scalatest.flatspec.AnyFlatSpec

class TestResources extends AnyFlatSpec {

  "Resources" should "be available" in {

    println(classOf[Suite].getClassLoader.getResource("org/scalatest/HtmlReporter.css"))
    println(classOf[CoverageTrait].getClassLoader.getResource("css/styles.css"))
    println(classOf[CoverageTrait].getResource("/css/styles.css"))
  }
}
