// See README.md for license details.

package functionalCoverage

import chisel3._
import chisel3.tester.testableData
import functionalCoverage.Coverage._

import scala.collection.mutable.ArrayBuffer

/**
  * Handles everything related to functional coverage
  * @param dut the DUT currently being tested
  * @tparam T the type of the DUT
  */
class CoverageReporter {
  private val coverGroups: ArrayBuffer[CoverGroup] = new ArrayBuffer[CoverGroup]()
  private val coverageDB:  CoverageDB = new CoverageDB

//  def reportHtml: String = {
//    coverGroups.flatMap{group =>
//      val header = s"<table class=\"tg \"><thead><tr><th class=\"tg-baqh\" colspan=\"5\"><span style=\"font-weight:bold\">Group ID ${group.id}</span></th></tr></thead>"
//        val portName = group.points.flatMap{point =>
//          val tab = s"<tbody><tr><td class=\"tg\" colspan=\"3\"><span style=\"font-weight:bold\">Port Name</span></td><td class=\"tg-lax\" colspan=\"2\">${point.portName}</td></tr>"
//          val col = "<tr> <td class=\"tg-0lax\"><span style=\"font-weight:bold\">Type</span></td><td class=\"tg-0lax\"><span style=\"font-weight:bold\">Name </span></td> <td class=\"tg-0lax\"><span style=\"font-weight:bold\">Covering</span></td> <td class=\"tg-0lax\"><span style=\"font-weight:bold\">Range </span></td> <td class=\"tg-0lax\"><span style=\"font-weight:bold\">Hits</span></td> </tr>"
//          val ll = point.bins.flatMap{ bin => s"<tr> <td class=\"tg-0lax\">${bin.name}</td> <td class=\"tg-0lax\">${bin.range.toString}</td> <td class=\"tg-0lax\">${coverageDB.getNHits(bin)}</td> <td class=\"tg-0lax\"></td> <td class=\"tg-0lax\"></td> </tr>"}
//          tab + col + ll + "</tbody>"
//        }
//     header + portName + "</table>"
//  }.toString()
//  }
  /**
    * Makes a readable fucntional coverage report
    * @return the report in string form
    */
  def report: String = {
    val rep: StringBuilder = new StringBuilder(s"\n============ COVERAGE REPORT ============\n")
    coverGroups.foreach(group => {
      rep.append(s"============== GROUP ID: ${group.id} ==============\n")
      group.points.foreach(point => {
        rep.append(s"COVER_POINT PORT NAME: ${point.portName}\n")
        point.bins.foreach(bin => {
          val nHits = coverageDB.getNHits(bin)
          rep.append(s"BIN ${bin.name} COVERING ${bin.range.toString} HAS $nHits HIT(S)\n")
        })
        rep.append(s"=========================================\n")
      })
    })
    rep.mkString
  }

  /**
    * Prints out a human readable coverage report
    */
  def printReport(): Unit = println(report)

  /**
    * Samples all of the coverpoints defined in the various covergroups
    * and updates the values stored in the coverageDB
    */
  def sample(): Unit = {
    //Sample each coverPoint in each group and update the value stored in the DB
    coverGroups.foreach(group => {
      group.points.foreach(point => {
        //Check for the ports
        val pointVal = point.port.peek().litValue()
        point.bins.foreach(bin => {
          if (bin.range contains pointVal) {
            coverageDB.addBinHit(bin, pointVal)
          }
        })
      })
    })
  }

  /**
    * Creates a new coverGroup given a list of coverPoints
    * @param points the list of all coverPoints that will be sampled by the group.
    *               These are defined by (portName: String, bins: List[BinSpec])
    * @return the unique ID attributed to the group
    */
  def register(points: List[CoverPoint]): CoverGroup = {
    //Generate the group's identifier
    val gid: BigInt = coverageDB.createCoverGroup()

    //Create final coverGroup
    val group = CoverGroup(points, gid)
    coverGroups.append(group)
    group
  }
}
