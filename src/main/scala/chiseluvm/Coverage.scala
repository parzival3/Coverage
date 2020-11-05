package chiseluvm
import java.io.File
import chiseltest.ChiselScalatestTester
import chiseltest.experimental.sanitizeFileName
import com.googlecode.jgenhtml.{Config, CoverageReport}
import chiseluvm.tools.FileUtils._
import chiseluvm.tools.CoverageInformation
import org.scalatest.{Outcome, TestSuite}

import scala.sys.process._

trait Coverage extends ChiselScalatestTester {
  this: TestSuite =>
  private val simulationRoot = "test_run_dir"
  private val coverageBase = "chiseluvm"
  private val verilogCovOutputPath = "logs"
  private val unifiedInfoFile = "output.info"
  private val unifiedDatFile = "output.dat"
  private val coverageFolder = coverageBase + File.separator + suiteId
  private var firstRun = true
  private var testRunDir = ""
  private def unifiedCovPath:   String = coverageFolder + File.separator + "total"
  private def verilogSources:   String = coverageFolder
  private def datFileFolder:    String = coverageFolder
  private def testDirDatFolder: String = testRunDir + File.separator + verilogCovOutputPath

  /**
    * This function copies the testDirectory/logs/coverage.dat file generated by verilator to the
    * coverage/suite_name/ directory and it renames it as testDirectory.dat
    * @param testDirDatFolder the directory in which the dat files are stored usually in test_run_dir/Test_name/logs/\*.dat
    * @param outputFolder destination of the files
    */
  def copyCoverageOutput(testDirDatFolder: String, outputFolder: String): Unit = {
    copyFiles(testDirDatFolder, outputFolder, ".dat", reanemBasedOnDir = true)
  }

  /**
    * This function copies the verilog files generated in the testDirectory folder into coverage/suite_name/
    * @param testDirectory the directory in which the verilog files are stored usually in test_run_dir
    * @param outputFolder destination of the files
    */
  def copyVerilogFile(testDirectory: String, outputFolder: String): Unit = {
    copyFiles(testDirectory, outputFolder, ".v", reanemBasedOnDir = false)
  }

  /**
    * This function combines all the dat files generated in the previous test and stores them into an [[outputFile]] file
    * @param datFilesFolder Folder in which the dat files are stored
    * @param outputFolder Folder to store the unified dat file
    * @param outputFile  Name of the unified file
    */
  def unifyCoverageData(datFilesFolder: String, outputFolder: String, outputFile: String): Unit = {
    val datFiles = getListOfFiles(datFilesFolder, ".dat")
    val datFilesAsString = datFiles.mkString(" ")
    val result = Seq("verilator_coverage", "--write", outputFolder + File.separator + outputFile, datFilesAsString).!
    if (result != 0) {
      throw new Exception(s"Couldn't unify coverage data files: $result")
    }
  }

  /**
    * TODO: The CoverageReport of jgenhtml is not very flexible, we should consider to extend it with another class
    * TODO: Get the actual coverage percentage
    * Coverage report generated by jgenhtml
    * @param path path of the complete report
    * @return the coverage percentage
    */
  def getCoverageReport(path: String): Float = {
    val file = new Array[String](1)
    file(0) = path + "/total/output.info"
    // Create a new Config for the CoverageReport and set as command line the output directory
    val config = new Config()
    config.initializeUserPrefs(List("--output-directory", path).toArray)
    // Set the configuration to the CoverageReport singleton
    CoverageReport.setConfig(config)
    val coverageReport = new CoverageReport(file)
    coverageReport.generateReports()
    100
  }

  /**
    * This function transforms the output.dat into an output.info this info file will then be processed by jgenhtml
    * program to generate the actual report
    * @param outputFolder Folder of the dat file
    * @param sourceFolder Folder in which the Verilog files are stored
    * @param datFile Name of the dat file
    * @param infoFile Name of the info file
    */
  def createInfoFile(outputFolder: String, sourceFolder: String, datFile: String, infoFile: String): Unit = {
    val cover = new CoverageInformation
    cover.readCoverage(outputFolder + File.separator + datFile)
    cover.writeInfo(outputFolder + File.separator + infoFile, sourceFolder)
  }

  def initializeDirectory(test: NoArgTest): Unit = {
    // Get the current test name
    testRunDir = simulationRoot + File.separator + sanitizeFileName(test.name)

    // Clean and create the needed directories
    if (firstRun) {
      deleteDirectory(coverageFolder)
      makeFolder(coverageBase)
      makeFolder(coverageFolder)
      makeFolder(unifiedCovPath)
      firstRun = false
    }
  }

  def createReport(): Unit = {

    // Copy the data generated in the test directory to the current test suite coverage directory
    copyVerilogFile(testRunDir, coverageFolder)
    copyCoverageOutput(testDirDatFolder, coverageFolder)

    // Merge all the coverage dat files in a general dat file
    // TODO: should we keep the partial results?
    unifyCoverageData(datFileFolder, unifiedCovPath, unifiedDatFile)

    // Crate the info file need by jgenhtml to generate the html report
    createInfoFile(unifiedCovPath, verilogSources, unifiedDatFile, unifiedInfoFile)

    // Generate the actual html report
    val percentage = getCoverageReport(coverageFolder)
    println(s"Total coverage was: $percentage% $coverageFolder/total/index.html")
    // Return the outcome of the test
  }

  /**
    * Method that calls the super class and run the tests
    * @param test the current test
    * @return the outcome of the test
    */
  abstract override def withFixture(test: NoArgTest): Outcome = {

    initializeDirectory(test)

    // Run the actual test
    val outcome = super.withFixture(test)

    createReport()

    // return the test results
    outcome
  }

}
