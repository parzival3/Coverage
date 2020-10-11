package coverage
import java.io.{BufferedInputStream, File, FileInputStream, IOException}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.{DirectoryNotEmptyException, FileAlreadyExistsException, Files, NoSuchFileException, Paths}
import java.security.{DigestInputStream, MessageDigest}

import chiseltest.ChiselScalatestTester
import chiseltest.experimental.sanitizeFileName
import com.googlecode.jgenhtml.{Config, CoverageReport}
import coverage.CoverageUtils.getListOfFiles
import coverage.tools.CoverageInformations
import org.scalatest.{Outcome, TestSuite}

import scala.sys.process._

object CoverageUtils {

  def getListOfFiles(dir: String, ext: String): List[String] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(x => x.isFile && x.getName.endsWith(ext)).map(dir + "/" + _.getName).toList
    } else {
      List("")
    }
  }

  def getFileName(path: String): String = {
    path.substring(path.lastIndexOf("/") + 1)
  }

  def generateReport(coveragePath: String, infoFile: String) = {
    val result = Seq("bash", "-c", "cd", s"$coveragePath/", "&&", "genhtml", s"$infoFile").!
    if (result != 0) {
      throw new Exception(s"Couldn't create coverage report: $result")
    }
    val output = Seq("bash", "-c", s"cd $coveragePath/ && genhtml $infoFile").!!
    print(output)
    output
  }

  def extractPercentage(output: String): Option[Float] = {
    val percentage = raw"(\d?\d\d\.?\d?\d)%".r
    percentage.findFirstIn(output) match {
      case Some(x) => Some(x.replaceAll("%", "").toFloat)
      case None    => None
    }
  }

  def makeFolder(dir: String): Unit = {
    val file = new File(dir + "/")
    file.mkdir()
  }

  def unifyCoverageDat(destFile: String, datFiles: List[String]): Unit = {
    val datFilesAsString = datFiles.mkString(" ")
    val result = s"verilator_coverage --write $destFile $datFilesAsString".!
    if (result != 0) {
      throw new Exception(s"Couldn't unify coverage data files: $result")
    }
  }

  def createInfoFile(infoFile: String, datFile: String, sourcePath: String): Unit = {
    val cover = new CoverageInformations
    cover.readCoverage(datFile)
    cover.writeInfo(infoFile, sourcePath)
//    val result = s"verilator_coverage --write-info $infoFile $datFile".!
//    if (result != 0) {
//      throw new Exception(s"Couldn't create info file: $result")
//    }
  }

  private def fileChecksum(filepath: String, mdIn: MessageDigest): String = {
    var mdOut: Option[MessageDigest] = None
    var dis:   Option[DigestInputStream] = None
    // file hashing with DigestInputStream
    try {
      dis = Some(new DigestInputStream(new BufferedInputStream(new FileInputStream(filepath)), mdIn))
      dis match {
        case Some(stream) => {
          while (stream.read != -1) {
            //TODO this seems to be necessary; see dis.read() javadoc
          }
          mdOut = Some(stream.getMessageDigest)
        }
        case None => mdOut = None
      }
    } catch {
      case ioe: IOException => {
        mdOut = null
        System.err.println(ioe.getMessage)
      }
    } finally {
      dis match {
        case Some(x) => x.close()
      }
    }
    mdOut match {
      case Some(out) => convertBytesToHex(out.digest)
      case None      => ""
    }
  }

  def convertBytesToHex(bytes: Seq[Byte]): String = {
    val sb = new StringBuilder
    for (b <- bytes) {
      sb.append(String.format("%02x", Byte.box(b)))
    }
    sb.toString
  }

  object CopyFiles {
    def apply(simDir: String, destinationDirPath: String, fileExstension: String): Unit = {
      new File(destinationDirPath).mkdirs()
      val filesToCopy = getListOfFiles(simDir, fileExstension)
      filesToCopy.foreach { f =>
        try {
          val file = getFileName(f)
          Files.createFile(Paths.get(destinationDirPath + "/" + file))
          Files.copy(
            Paths.get(simDir + "/" + file),
            Paths.get(destinationDirPath + "/" + file),
            REPLACE_EXISTING
          )
        } catch {
          case _: FileAlreadyExistsException =>
            System.out.format("")
          case x: IOException =>
            System.err.format("createFile error: %s%n", x)
        }
      }
    }
  }

  def getCoverageReport(path: String): Float = {
    val file = new Array[String](1)
    file(0) = path + "/total/output.info"
    val config = new Config()
    config.initializeUserPrefs(List("--output-directory", path).toArray)
    CoverageReport.setConfig(config)
    val coverageReport = new CoverageReport(file)
    coverageReport.generateReports()
    100
//    val reportOutput = CoverageUtils.generateReport(path, "total/output.info")
//    CoverageUtils.extractPercentage(reportOutput).getOrElse(0)
  }

  object CopyFilesRename {
    def apply(simDir: String, destinationDirPath: String, fileExstension: String): Unit = {
      new File(destinationDirPath).mkdirs()
      val filesToCopy = getListOfFiles(simDir, fileExstension)
      filesToCopy.foreach { f =>
        try {
          val file = getFileName(f)
          val fileDir = simDir.replace("/logs", "")
          val newName = fileDir.replace("/logs", "").substring(fileDir.lastIndexOf("/") + 1) + ".dat"
          Files.createFile(Paths.get(destinationDirPath + "/" + newName))
          Files.copy(
            Paths.get(simDir + "/" + file),
            Paths.get(destinationDirPath + "/" + newName),
            REPLACE_EXISTING
          )
        } catch {
          case _: FileAlreadyExistsException =>
            System.out.format("")
          case x: IOException =>
            System.err.format("createFile error: %s%n", x)
        }
      }
    }
  }
  def deleteDirectory(path: String): Unit = {
    if (new File(path).exists) {
      Files.walk(Paths.get(path)).toArray.reverse.filter(_.toString != ".").foreach { f =>
        try {
          Files.delete(Paths.get(f.toString))
        } catch {
          case ex @ (_: IOException | _: NoSuchFileException) => {
            println(s"Something wrong happened while deleting $f in $path")
            throw ex
          }
          case ex: DirectoryNotEmptyException => {
            println(s"Cant' delete non empty directory $f in $path")
            throw ex
          }
        }
      }
    }
  }
}

trait Coverage extends ChiselScalatestTester {
  this: TestSuite =>
  private val simulationRoot = "test_run_dir"
  private val coveragePath = "coverage"
  private val coverageFolder = coveragePath + File.separator + suiteName
  private var count = 0
  private var firstRun = true
  private var testRunDir = ""

  def cleanSuiteCoverageDirectory(dir: String = coverageFolder): Unit = {
    CoverageUtils.deleteDirectory(dir)
  }

  def getTestName(name: String): String = {
    sanitizeFileName(name)
  }

  def createSuiteCoverageFolder(): Unit = {
    CoverageUtils.makeFolder(coveragePath)
    CoverageUtils.makeFolder(coverageFolder)
    CoverageUtils.makeFolder(s"$coverageFolder/total")
  }

  /*
    This function copies the testDirectory/logs/coverage.dat file generated by verilator to the
    coverage/suite_name/ directory and it renames it as testDirectory.dat
   */
  def copyCoverageOutput(testDirectory: String = testRunDir) = {
    CoverageUtils.CopyFilesRename(testDirectory + "/logs", coverageFolder, ".dat")
  }

  /*
    This function copies the verilog files generated in the testDirectory folder into coverage/suite_name/
   */
  def copyVerilogFile(testDirectory: String = testRunDir) = {
    CoverageUtils.CopyFiles(testDirectory, coverageFolder, ".v")
  }

  /*
    This function combines all the dat files generated in the previous test and stores them into an output.dat file
   */
  def unifyCoverageData(path: String = coverageFolder) = {
    val datFiles = getListOfFiles(path, ".dat")
    CoverageUtils.unifyCoverageDat(s"$path/total/output.dat", datFiles)
  }
  /*
    This function transforms the output.dat into an output.info this info file will then be processed
    by genhtml program to generate the actual report
   */
  def createInfoFile(path: String = coverageFolder) = {
    CoverageUtils.createInfoFile(s"$path/total/output.info", s"$path/total/output.dat", path)
  }

  /*
    This function cals genhtml and generates the html reports and returns percentage output of the suite
    coverage
   */
  def generateHtmlReport(path: String = coverageFolder) = {
    val reportOutput = CoverageUtils.generateReport(path, "total/output.info")
    CoverageUtils.extractPercentage(reportOutput).getOrElse(0)
  }

  abstract override def withFixture(test: NoArgTest): Outcome = {
    // Get the current test name
    testRunDir = simulationRoot + File.separator + getTestName(testNames.toList(count))

    // Clean and create the needed directories
    if (firstRun) {
      cleanSuiteCoverageDirectory()
      createSuiteCoverageFolder()
      firstRun = false
    }

    // Run the actual test
    val outcome = super.withFixture(test)

    // Copy the data generated in the test directory to the current test suite coverage directory
    copyVerilogFile()
    copyCoverageOutput()

    // Merge all the coverage dat files in a general dat file
    // TODO: should we keep the partial results?
    unifyCoverageData()

    // Crate the info file need by genhtml to generate the html report
    createInfoFile()

    // Generate the actual html report
    val percentage = CoverageUtils.getCoverageReport(coverageFolder)
    println(s"Total coverage was: $percentage% $coverageFolder/total/index.html")
    count += 1
    outcome
  }

}
