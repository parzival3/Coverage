package chisel
import chisel3.fromBigIntToLiteral
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chisel3.tester.testableClock
import chiseltest.testableData
import chiseluvm.classes._
import coverage.Coverage.{Bins, CoverPoint}
import coverage.CoverageReporter
import sv.Random.RandInt
import sv.Random
import testutils.Alu

import scala.language.postfixOps
import scala.math.BigInt


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

class Transaction extends Random(20) with uvm_sequence_item {
  var inputA: RandInt = rand(inputA, 0 to 255 toList)
  var inputB: RandInt = rand(inputB, 0 to 255 toList)
  var inputOp: RandInt = rand(inputOp, 0 to 10 toList)
  var output: BigInt = 0

  val constraintOP: ConstraintBlock = constraintBlock(
    unary (inputOp => inputOp <= 3)
  )

  override def equals(that: Any): Boolean = {
    that match {
      case transaction: Transaction => {
        inputA == transaction.inputA && inputB == transaction.inputB && inputOp == transaction.inputOp && output == transaction.output
      }
      case _ => false
    }
  }
}

class AluSequence extends uvm_sequence[Transaction] {
  val pendingT = new Transaction()
  override val iterator: Iterator[Transaction] = new Iterator[Transaction] {
    var i: Int = -1
    def hasNext: Boolean = pendingT.randomize
    def next: Transaction = {
      i += 1
      pendingT
    }
  }
}

object portAfter extends uvm_analysis_port[Transaction]
object portBefor extends uvm_analysis_port[Transaction]

class AdluDriver(alu: Alu) extends uvm_driver[Transaction] {

  override def run(): Unit = {
    if (sequence.iterator.hasNext) {
      val transaction = sequence.iterator.next()
      alu.io.a.poke(transaction.inputA.U)
      alu.io.b.poke(transaction.inputB.U)
      alu.io.fn.poke(transaction.inputOp.U)
      //alu.clock.step()
    }
  }
}

class MonitorBefore(alu: Alu, reporter: CoverageReporter) extends uvm_monitor {

  override def run(): Unit = {
    val a = alu.io.a.peek().litValue()
    val b = alu.io.b.peek().litValue()
    val fun = alu.io.fn.peek().litValue()
    val currentTransaction = new Transaction
    currentTransaction.inputA = a
    currentTransaction.inputB = b
    currentTransaction.inputOp = fun
    currentTransaction.output = alu.io.result.peek().litValue()
    portBefor.write(currentTransaction)
  }

}

class MonitorAfter(alu: Alu, reporter: CoverageReporter) extends uvm_monitor {
  reporter.register(
    CoverPoint(alu.io.a , "ioa", Bins("lo10", 0 to 10)::Nil)::CoverPoint(alu.io.b, "iob", Bins("lo10", 0 to 10)::Nil)::Nil
  )

  override def run(): Unit = {

    val a = alu.io.a.peek().litValue()
    val b = alu.io.b.peek().litValue()
    val fun = alu.io.fn.peek().litValue()

    val currentTransaction = new Transaction
    currentTransaction.inputA = a
    currentTransaction.inputB = b
    currentTransaction.inputOp = fun
    currentTransaction.output = GoldenModel.prediction(a, b, fun)

    reporter.sample()
    portAfter.write(currentTransaction)

  }
}

class AluAgent(alu: Alu, reporter: CoverageReporter) extends uvm_agent {
  val driver = new AdluDriver(alu)
  val monitor_b = new MonitorBefore(alu, reporter)
  val monitor_a = new MonitorAfter(alu, reporter)
  val sequ = new AluSequence

  driver.connectSequence(sequ)
}

class AluScoreboard extends uvm_scoreboard {
  def run(): Unit = {
    println("Transaction in Port Before ==========================")
    println(portBefor.transactions.last.debug())
    println("Transaction in Port After  ==========================")
    println(portAfter.transactions.last.debug())
    assert(portBefor.transactions.last == portAfter.transactions.last)
  }
}

class AluEnv(val alu: Alu, reporter: CoverageReporter) extends uvm_environment {
  val agent = new AluAgent(alu, reporter)
  val scoreboard = new AluScoreboard
}

class TestUVMAlu extends uvm_test {
  "UVM test" should "work" in {

    test(new Alu(8)).withAnnotations(VerilatorCoverage) {alu =>
      val simpleAdderEnv = new AluEnv(alu, coverageReporter)
      simpleAdderEnv.step()
      simpleAdderEnv.step()
      simpleAdderEnv.step()
      simpleAdderEnv.step()
      simpleAdderEnv.step()
    }
  }
}
