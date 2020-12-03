package chiseluvm.classes

class uvm_sequence[T] extends uvm_object {
  // It needs to be a type of umv_sequence_item
  // It needs to provide some api to have a instantiate a number of transactions
  // It needs to block and wait for the driver to consume the transaction and create a new one
  def iterator(): Iterator[T] = Iterator.empty

  override def run(): Unit = {}
}
