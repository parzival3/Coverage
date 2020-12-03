package chiseluvm.tools

import java.io.{BufferedWriter, File, FileWriter}

import scala.collection.{immutable, mutable}
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Class for parsing and generating coverage information
  * TODO: This class can be easily transform into a pure functional class maybe splitted between a companion object
  * and a class to store the information
  */
class CoverageInformation {
  var mnameMap:    mutable.HashMap[String, Int] = mutable.HashMap[String, Int]()
  var mpoints:     ArrayBuffer[Point] = new ArrayBuffer[Point]()
  var sources:     mutable.HashMap[String, VlSource] = new mutable.HashMap[String, VlSource]()
  val annotateMin: Int = 10

  class SourceCount(val lineNum: Int, val coluNum: Int) {
    private var count: Int = 0
    private var ok:    Boolean = false // Coverage is above threshold

    def getCount: Int = count
    def getOk:    Boolean = ok

    def incCount(nCount: Int, isOk: Boolean): Unit = {
      count += nCount
      if (isOk) ok = true
    }
  }

  class VlSource(val name: String) {
    type ColumMap = mutable.Map[Int, SourceCount]
    type LinenoMap = mutable.Map[Int, ColumMap]

    var needed: Boolean = false
    val lines:  LinenoMap = mutable.Map[Int, ColumMap]()

    def incCount(lineNo: Int, colNo: Int, count: Int, ok: Boolean): Unit = {
      // TODO: can I do it in a better way?
      val mColMap: ColumMap = lines.getOrElseUpdate(lineNo, mutable.Map[Int, SourceCount]())
      mColMap.getOrElseUpdate(colNo, new SourceCount(lineNo, colNo)).incCount(count, ok)
    }
  }

  class Point(val name: String, val pointNum: Int, var count: Int = 0) {
    /*
     * Defined inside VlcPoint.h in Verilator source file
     */
    val VL_CIK_FILENAME = "f"
    val VL_CIK_COMMENT = "o"
    val VL_CIK_COLUMN = "n"
    val VL_CIK_LIMIT = "L"
    val VL_CIK_LINENO = "l"
    val VL_CIK_LINESCOV = "S"
    val VL_CIK_TABLE = "T"
    val VL_CIK_THRESH = "s"
    val VL_CIK_TYPE = "t"
    val VL_CIK_WEIGHT = "w"

    // TODO: improve parsing of this string
    val groups: immutable.Seq[List[String]] =
      name.replace("\'", "").split("\u0001").toList.map(_.split("\u0002").toList)
    val groupMap: Map[String, String] = {
      for {
        group <- groups
        if group.length >= 2
      } yield group(0) -> group(1)
    }.toMap

    val filename: Option[String] = groupMap.get(VL_CIK_FILENAME)
    var comment:  Option[String] = groupMap.get(VL_CIK_COMMENT)
    var pType:    Option[String] = groupMap.get(VL_CIK_TYPE)
    var linescov: Option[String] = groupMap.get(VL_CIK_LINESCOV)
    // TODO: change linenum and linecol thresh
    var thresh: Int = groupMap.get(VL_CIK_THRESH) match {
      case Some(x) => x.toInt
      case None    => 0
    }

    var linenum: Int = groupMap.get(VL_CIK_LINENO) match {
      case Some(x) => x.toInt
      case None    => 0
    }
    var linecol: Int = groupMap.get(VL_CIK_COLUMN) match {
      case Some(x) => x.toInt
      case None    => 0
    }

    def countInc(num: Int): Unit = {
      count += num
    }
  }
  // TODO: add bins coverage
  /**
    * This function creates the list of [[VlSource]]  by parsing the [[Point]] processed in the [[readCoverage]]
    * function
    */
  def annotatedCalc(runDir: String): Unit = {
    mpoints.foreach { point =>
      val filename = point.filename.getOrElse("")
      if (filename != "" && point.linenum != 0) {
        // TODO: the combined dat file uses relative path for some files and full paths for others
        //  the relativeFilename variable here is just a way of overcome this limitation. I should rewrite the
        //  write option of verilator_coverage in scala
        var relativeFilename = filename
        if (filename.lastIndexOf(runDir + File.pathSeparator) != -1) {
          relativeFilename = filename.substring(filename.lastIndexOf(runDir + File.pathSeparator))
        }
        val curSource = sources.getOrElseUpdate(relativeFilename, new VlSource(relativeFilename))
        val thresh = if (point.thresh == 0) annotateMin else point.thresh
        val ok = point.count > thresh
        curSource.incCount(point.linenum, point.linecol, point.count, ok)
      }
    }
  }

  /**
    * Transform Dat file into info file
    * @param fileName filename of info output file as String
    * @param path path in which all the source file are stored. This path is then read by the genhtml tool to generate
    *             a report with a link to the source code.
    */
  def writeInfo(fileName: String, path: String, runDir: String): Unit = {
    val file = new File(fileName)
    val bufferString = new BufferedWriter(new FileWriter(file))

    annotatedCalc(runDir)

    bufferString.write("TN:verilator_coverage\n")
    sources.foreach { si =>
      val source = si._2
      bufferString.write(s"SF:$path/" + source.name + "\n")
      source.lines.foreach { line =>
        val lineno = line._1
        val cmap = line._2
        var first = true
        var minCount = 0
        cmap.foreach { ci =>
          val col = ci._2
          if (first) {
            minCount = col.getCount
            first = false
          } else {
            minCount = math.min(minCount, col.getCount)
          }
        }
        bufferString.write("DA:" ++ lineno.toString + "," + minCount.toString + "\n")
      }
      bufferString.write("end_of_record" + "\n")
    }
    bufferString.close()
  }

  /**
    * Read coverage from dat file
    * @param fileName dat file to be parsed
    */
  def readCoverage(fileName: String): Unit = {
    val lines: List[String] = Source.fromFile(fileName).getLines.toList
    lines.foreach { line =>
      val lSplitted = line.split(" ").toList
      lSplitted.head match {
        case "C" => {
          val point = lSplitted(1)
          val hit = lSplitted(2).toInt
          if (mnameMap.contains(point)) {
            mpoints(mnameMap(point)).countInc(hit)
          } else {
            mnameMap(point) = mnameMap.size
            mpoints += new Point(point, mnameMap(point), hit)
          }
        }
        case _ =>
      }
    }
  }
}
