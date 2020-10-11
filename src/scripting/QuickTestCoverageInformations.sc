
import scala.collection.mutable
import scala.io.Source

import scala.sys.process._

var mnameMap = mutable.HashMap[String, Int]()

def readCoverage(fileName: String): Unit = {
  val lines: List[String] = Source.fromFile(fileName).getLines.toList
  lines foreach { line =>
    line(0) match {
      case 'C' =>  {
        val lSplitted = line.split(' ')
        val point = lSplitted(2)
        val hit = lSplitted(3)
        mnameMap(point) = hit.toInt
      }
      case _ =>
    }
  }
  print(mnameMap)
}

readCoverage("/home/enrico/Git/Coverage/src/scripting/output.dat")