package chiseluvm.classes

trait uvm_driver[T] extends uvm_object {
  var sequence: uvm_sequence[T] = new uvm_sequence[T] {}

  def connectSequence(seq: uvm_sequence[T]): Unit = {
    sequence = seq
  }
}
