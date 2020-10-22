package tools

import coverage.tools.CoverageInformation
import org.scalatest.funsuite.AnyFunSuite

import scala.sys.process._

class TestCoverageInformations extends AnyFunSuite {
  test("CoverageInfomations should be able to parse info file") {
    println("pwd".!!)
    val filename = "src/test/resources/output.dat"
    val cover = new CoverageInformation
    cover.readCoverage(filename)
  }

  test("CoverageInformations should be able to read dat file and output info file") {
    var filename = "src/test/resources/output.dat"
    val cover = new CoverageInformation
    cover.readCoverage(filename)
    cover.writeInfo("src/test/resources/out_info.info", "scr/test/resources/")
  }
}
