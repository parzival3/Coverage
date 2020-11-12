package chiseluvm.classes

trait uvm_driver[T] extends uvm_object {
  def runPhase(): Unit

  object seqItemPort {

    def getNextItem(seq: T): T = {
     seq
    }
  }

}
