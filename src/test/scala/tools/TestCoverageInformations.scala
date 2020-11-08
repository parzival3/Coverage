package tools

import chiseluvm.tools.CoverageInformation
import org.scalatest.FunSuite

import scala.sys.process._

class TestCoverageInformations extends FunSuite {
  test("CoverageInfomations should be able to parse info file") {
    val filename = "src/test/resources/output.dat"
    val cover = new CoverageInformation
    cover.readCoverage(filename)
  }

  test("CoverageInformations should be able to read dat file and output info file") {
    var filename = "src/test/resources/output.dat"
    val cover = new CoverageInformation
    cover.readCoverage(filename)
    cover.writeInfo("src/test/resources/out_info.info", "scr/test/resources/", "./")
  }
}
