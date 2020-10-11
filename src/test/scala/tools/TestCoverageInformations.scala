package tools

import org.scalatest.funsuite.AnyFunSuite
import coverage.tools.CoverageInformations

import scala.sys.process._

class TestCoverageInformations extends AnyFunSuite {
  test("CoverageInfomations should be able to parse info file") {
    println("pwd".!!)
    val filename = "src/test/resources/output.dat"
    CoverageInformations.readCoverage(filename)
  }
}
