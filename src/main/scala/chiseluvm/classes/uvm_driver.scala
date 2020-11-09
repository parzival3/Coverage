package chiseluvm.classes

trait uvm_driver[T] {
  def runPhase(): Unit

  object seqItemPort {

    def getNextItem(seq: T): T = {
     seq
    }
  }

}
