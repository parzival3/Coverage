package chisel
import chisel3.fromBigIntToLiteral
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chisel3.tester.testableClock
import chiseltest.testableData
import chiseluvm.classes._
import coverage.Coverage.{Bins, CoverPoint}
import sv.Random.RandInt
import testutils.Alu

import scala.language.postfixOps
import scala.math.BigInt
import scala.util.Random

object GoldenModel {

  def prediction(a: BigInt, b: BigInt, c: BigInt): BigInt = {
    val sum = BigInt(0)
    val sub = BigInt(1)
    val or = BigInt(2)
    val and = BigInt(3)

    c match {
      case `sum` => a + b
      case `sub` => a - b
      case `or` => a | b
      case `and` => a & b
    }
  }
}

class TestUVMAlu extends uvm_test {

  class simpleTransaction() extends sv.Random(20) with uvm_sequence_item {
    var inputA: RandInt = 0
    var inputB: RandInt = 0
    var inputOp: RandInt = 0

    var output: BigInt = 0

    rand(inputA, 0 to 255 toList)
    rand(inputB, 0 to 255 toList)
    rand(inputOp, 0 to 3 toList)

    override def equals(that: Any): Boolean = {
      that match {
        case transaction: simpleTransaction => {
          inputA == transaction.inputA && inputB == transaction.inputB && inputOp == transaction.inputOp && output == transaction.output
        }
        case _ => false
      }
    }
  }

  object simpleAluSequence extends uvm_sequence[simpleTransaction] {
    val iterator: Iterator[simpleTransaction] = new Iterator[simpleTransaction] {
      var i: Int = -1
      val pendingT = new simpleTransaction()
      def hasNext(): Boolean = pendingT.randomize
      def next(): simpleTransaction = {
        i += 1
        pendingT
      }
    }
  }

  object portAfter extends uvm_analysis_port[simpleTransaction]
  object portBefor extends uvm_analysis_port[simpleTransaction]

  class simpleAdluDriver(alu: Alu) extends uvm_driver[simpleTransaction] {
    override def runPhase(): Unit = {
      if (simpleAluSequence.iterator.hasNext) {
        val transaction = simpleAluSequence.iterator.next()
        println(transaction.debug())
        alu.io.a.poke(transaction.inputA.U)
        alu.io.b.poke(transaction.inputB.U)
        alu.io.fn.poke(transaction.inputOp.U)
        alu.clock.step()
      }
    }
  }

  class monitorBefore(alu: Alu) extends uvm_monitor {

    override def runPhase(): Unit = {
      val a = alu.io.a.peek().litValue()
      val b = alu.io.b.peek().litValue()
      val fun = alu.io.fn.peek().litValue()
      val currentTransaction = new simpleTransaction
      currentTransaction.inputA = a
      currentTransaction.inputB = b
      currentTransaction.inputOp = fun
      currentTransaction.output = alu.io.result.peek().litValue()
      portBefor.write(currentTransaction)
    }

  }

  class monitorAfter(alu: Alu) extends uvm_monitor {
    coverageReporter.register(
      CoverPoint(alu.io.a , "ioa", Bins("lo10", 0 to 10)::Nil)::CoverPoint(alu.io.b, "iob", Bins("lo10", 0 to 10)::Nil)::Nil
    )

    override def runPhase(): Unit = {

      val a = alu.io.a.peek().litValue()
      val b = alu.io.b.peek().litValue()
      val fun = alu.io.fn.peek().litValue()

      val currentTransaction = new simpleTransaction
      currentTransaction.inputA = a
      currentTransaction.inputB = b
      currentTransaction.inputOp = fun
      currentTransaction.output = GoldenModel.prediction(a, b, fun)
      coverageReporter.sample()
      portAfter.write(currentTransaction)

    }
  }

  class simpleAluAgent(alu: Alu) extends uvm_agent {
    val driver = new simpleAdluDriver(alu)
    val monitor_b = new monitorBefore(alu)
    val monitor_a = new monitorAfter(alu)

    def run(): Unit = {
      driver.runPhase()
      monitor_a.runPhase()
      monitor_b.runPhase()
    }
  }

  class simpleAluScoreboard extends uvm_scoreboard {
    def run(): Unit = {
      println("Port Before ==========================")
      println(portBefor.transactions.last.debug())
      println("Port After ==========================")
      println(portAfter.transactions.last.debug())
      assert(portBefor.transactions.last == portAfter.transactions.last)
    }
  }

  class simpleEnv(alu: Alu) extends uvm_environment {
    val agent = new simpleAluAgent(alu)
    val scoreboard = new simpleAluScoreboard
    def run(): Unit = {
      agent.run()
      scoreboard.run()
    }
  }

  "UVM test" should "work" in {

    test(new Alu(8)).withAnnotations(VerilatorCoverage) {alu =>
      val simpleAdderEnv = new simpleEnv(alu)
      simpleAdderEnv.run()
      simpleAdderEnv.run()
      simpleAdderEnv.run()
    }
  }
}
