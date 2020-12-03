package chiseluvm.classes
import chisel3.MultiIOModule
import chisel3.tester.testableClock

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

trait uvm_environment extends uvm_object {
  private val rm = runtimeMirror(getClass.getClassLoader)
  private val im = rm.reflect(this)
  private val members = im.symbol.typeSignature.members
  private def agents:      Iterable[universe.Symbol] = members.filter(_.typeSignature <:< typeOf[uvm_agent])
  private def scoreboards: Iterable[universe.Symbol] = members.filter(_.typeSignature <:< typeOf[uvm_scoreboard])
  private def duts:        Iterable[universe.Symbol] = members.filter(_.typeSignature <:< typeOf[MultiIOModule])

  def run(): Unit = {
    agents.foreach(c => im.reflectField(c.asTerm).get.asInstanceOf[uvm_agent].run())
    scoreboards.foreach(c => im.reflectField(c.asTerm).get.asInstanceOf[uvm_scoreboard].run())
  }

  def step(): Unit = {
    run()
    duts.foreach(dut => im.reflectField(dut.asTerm).get.asInstanceOf[MultiIOModule].clock.step())
  }
}
