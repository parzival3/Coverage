package chisel
import chisel3.fromBigIntToLiteral
import chisel3.tester.testableClock
import chiseltest.testableData
import chiseluvm.classes.{uvm_analysis_port, uvm_driver, uvm_monitor, uvm_sequence, uvm_sequence_item, uvm_sequencer}
import coverage.Coverage.{Bins, CoverPoint}
import testutils.Alu

import scala.collection.mutable.ArrayBuffer
import scala.math.BigInt
import scala.util.Random

class TestUVMAlu extends chiseluvm.classes.uvm_test {

  case class simpleTransaction(inputA: BigInt, inputB: BigInt, inputOp: BigInt) extends uvm_sequence_item

  class simpleAluSequence extends uvm_sequence[simpleTransaction] {
    // TODO: replace with constraint random class
    val rand = Random
    def body(): List[simpleTransaction] = {
      for (i <- (0 to 15).toList) yield simpleTransaction(rand.nextInt(20), rand.nextInt(20), rand.nextInt(5))
    }
  }

  class simpleAluSequencer extends uvm_sequencer[simpleTransaction]

  object portAfter extends uvm_analysis_port[simpleTransaction]
  object portBefor extends uvm_analysis_port[simpleTransaction]

  class simpleAdluDriver(alu: Alu) extends uvm_driver[simpleTransaction] {
    override def runPhase(): Unit = {
      val transaction = seqItemPort.getNextItem(simpleTransaction(10, 10, 1))
      alu.io.a.poke(transaction.inputA.U)
      alu.io.b.poke(transaction.inputB.U)
      alu.io.fn.poke(transaction.inputOp.U)
      alu.clock.step()
    }
  }

  class monitorBefore(alu: Alu) extends uvm_monitor {
    override def buildPhase(): Unit = {

    }

    override def runPhase(): Unit = {

    }
  }

  class monitorAfter(alu: Alu) extends uvm_monitor {
    coverageReporter.register(
      CoverPoint(alu.io.a , "ioa", Bins("lo10", 0 to 10)::Nil)::CoverPoint(alu.io.b, "iob", Bins("lo10", 0 to 10)::Nil)::Nil
    )
    override def buildPhase(): Unit = {

    }

    override def runPhase(): Unit = {

      val a = alu.io.a.peek().litValue()
      val b = alu.io.b.peek().litValue()
      val fun = alu.io.fn.peek().litValue()
      val sum = BigInt(0)
      val sub = BigInt(1)
      val or = BigInt(2)
      val and = BigInt(3)

      val out = fun match {
        case `sum` => a + b
        case `sub` => a - b
        case `or` => a | b
        case `and` => a & b
      }

      val currentTransaction = simpleTransaction(a, b, out)
      portAfter.write(currentTransaction)

    }

  }

}
