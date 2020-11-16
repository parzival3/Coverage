package chiseluvm.classes

import chisel3.Clock
import chiseltest.ChiselScalatestTester
import chiseluvm.CoverageTrait
import org.scalatest.FlatSpec

import scala.collection.mutable.ArrayBuffer

trait uvm_test extends FlatSpec with CoverageTrait with ChiselScalatestTester {
//  private val drivers: ArrayBuffer[uvm_driver] = new ArrayBuffer[uvm_driver]()
//  private val monitors: ArrayBuffer[uvm_monitor] = new ArrayBuffer[uvm_monitor]()
//  private val agents: ArrayBuffer[uvm_agent] = new ArrayBuffer[uvm_agent]()
//
//  /** keeps track of the clock cycles */
//  private var time: Int = 0
//
//  private def stepSingle(): Unit = {
//    for (bfm <- bfms.toList) {
//      bfm.update(t, poke_onto_queue)
//    }
//    for (el <- update_queue) {
//      el._1 match {
//        case b: Bool => el._1.poke(el._2.B)
//        case u: UInt => el._1.poke(el._2.U)
//        case _       => throw new UnsupportedOperationException("")
//      }
//    }
//    update_queue.clear()
//  }
//
//  implicit class testableClock(x: Clock) {
//    def step(cycles: Int = 1): Unit = {
//      for(_ <- 0 until cycles) {
//        stepSingle()
//        chiseltest.testableClock(x).step(cycles)
//        time += 1
//      }
//    }
//  }
}
