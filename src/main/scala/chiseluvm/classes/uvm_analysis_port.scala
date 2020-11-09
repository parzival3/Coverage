package chiseluvm.classes

import scala.collection.mutable.ArrayBuffer

trait uvm_analysis_port[T] {
  val transactions: ArrayBuffer[T] = new ArrayBuffer[T]()
  def write(transaction: T): Unit = {
    transactions += transaction
  }
}
