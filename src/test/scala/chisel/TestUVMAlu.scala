package chisel
import chisel3.fromBigIntToLiteral
import chisel3.tester.testableClock
import chiseltest.testableData
import chiseluvm.classes._
import coverage.Coverage.{Bins, CoverPoint}
import testutils.Alu

import scala.math.BigInt
import scala.util.Random



class TestUVMAlu extends uvm_test {

  case class simpleTransaction(inputA: BigInt, inputB: BigInt, inputOp: BigInt) extends uvm_sequence_item

  object simpleAluSequence extends uvm_sequence[simpleTransaction] {
    // TODO: replace with constraint random class
    val rand = Random
    def body(): Stream[simpleTransaction] = {
      (for (i <- (0 to 15).toList) yield simpleTransaction(rand.nextInt(20), rand.nextInt(20), rand.nextInt(5))).toStream
    }
  }

  class simpleAluSequencer extends uvm_sequencer[simpleTransaction]

  object portAfter extends uvm_analysis_port[simpleTransaction]
  object portBefor extends uvm_analysis_port[simpleTransaction]

  class simpleAdluDriver(alu: Alu) extends uvm_driver[simpleTransaction] {
    override def runPhase(): Unit = {
      if (simpleAluSequence.body().nonEmpty) {
        val transaction = simpleAluSequence.body().iterator.next()
        alu.io.a.poke(transaction.inputA.U)
        alu.io.b.poke(transaction.inputB.U)
        alu.io.fn.poke(transaction.inputOp.U)
        alu.clock.step()
      }
    }
  }

  class monitorBefore(alu: Alu) extends uvm_monitor {
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
      portBefor.write(currentTransaction)
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

  class simpleAluAgent(alu: Alu) extends uvm_agent {
    val sequencer = new simpleAluSequencer
    val driver = new simpleAdluDriver(alu)
    val monitor_b = new monitorBefore(alu)
    val monitor_a = new monitorAfter(alu)

    def run(): Unit = {

    }
  }

  class simpleAluScoreboard extends uvm_scoreboard {
    def run(): Unit = {
      assert(portBefor.transactions.last == portAfter.transactions.last)
    }
  }

  class simpleEnv(alu: Alu) extends uvm_environment {
    val agent = new simpleAluAgent(alu)
    val scoreboard = new simpleAluScoreboard
    def run(): Unit = {
      agent.run()
    }
  }

  "UVM test" should "work" in {
    val dut = new Alu(16)
    val simpleAdderEnv = new simpleEnv(dut)

    test(dut) {alu =>

    }
  }
}
