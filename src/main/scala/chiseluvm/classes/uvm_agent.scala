package chiseluvm.classes

import scala.reflect.runtime.universe._
import scala.reflect.runtime._

trait uvm_agent extends uvm_object {
  private val rm = runtimeMirror(getClass.getClassLoader)
  private val im = rm.reflect(this)
  private val members = im.symbol.typeSignature.members
  private def drivers:  Iterable[universe.Symbol] = members.filter(_.typeSignature <:< typeOf[uvm_driver[_]])
  private def monitors: Iterable[universe.Symbol] = members.filter(_.typeSignature <:< typeOf[uvm_monitor])

  def run(): Unit = {
    drivers.foreach(c => im.reflectField(c.asTerm).get.asInstanceOf[uvm_driver[_]].run())
    monitors.foreach(c => im.reflectField(c.asTerm).get.asInstanceOf[uvm_monitor].run())
  }
}
