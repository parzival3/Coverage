package vivadoex1

import Chisel.fromtIntToLiteral
import chisel3.tester.{testableClock, testableData}
import chiseluvm.classes._
import coverage.CoverageReporter
import sv.Random.RandInt
import sv._
import chisel3.fromBigIntToLiteral
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder

import scala.language.postfixOps

class ReferenceModel {
  def run(): Unit = {
    val ctrans = drv2rmPort.transactions.last
    val otrans = new Transaction
    otrans.x = ctrans.x
    otrans.y = ctrans.y
    otrans.cin = ctrans.cin

    if (otrans.x + otrans.y + otrans.cin > 15) {
      otrans.cout = 1
      otrans.sum =  (ctrans.x + ctrans.y + ctrans.cin) - 16
    } else {
      otrans.cout = 0
      otrans.sum = ctrans.x + ctrans.y + ctrans.cin
    }
    rm2scPort.write(otrans)
  }
}


class Transaction extends Random(30) with uvm_sequence_item {
  var x: RandInt = rand(x, 0 to 15 toList)
  var y: RandInt = rand(y, 0 to 15 toList)
  var cin: RandInt = rand(cin, 0x0 to 1 toList)
  var sum: BigInt = 0
  var cout: BigInt = 0

}

class Sequence extends uvm_sequence[Transaction] {
  val pendingT = new Transaction()
  override def iterator: Iterator[Transaction] = new Iterator[Transaction] {
    var i: Int = -1
    def hasNext: Boolean = pendingT.randomize
    def next: Transaction = {
      i += 1
      pendingT
    }
  }
}

class Driver(adder: Adder4Bit, report: CoverageReporter) extends uvm_driver[Transaction] {
  override def run(): Unit = {
    if(sequence.iterator.hasNext) {
      val transaction = sequence.iterator.next()
      println(transaction.debug())
      adder.io.x.poke(transaction.x.U)
      adder.io.y.poke(transaction.y.U)
      adder.io.cin.poke(transaction.cin.U)
      drv2rmPort.write(transaction)
    }

  }
}

class Scoreborad() extends uvm_scoreboard {
  def run(): Unit = {
    println(s"Expected Transaction:= ${mon2scPort.transactions.last.debug}")
    println(s"Calculated Transaction:= ${rm2scPort.transactions.last.debug}")
    assert(mon2scPort.transactions.last.sum == rm2scPort.transactions.last.sum)
    assert(mon2scPort.transactions.last.cout == rm2scPort.transactions.last.cout)
  }
}

class Monitor(adder: Adder4Bit, reporter: CoverageReporter) extends uvm_monitor {
  override def run(): Unit = {
    val trans = new Transaction
    trans.x = adder.io.x.peek().litValue()
    trans.y = adder.io.y.peek().litValue()
    trans.cin = adder.io.cin.peek().litValue()
    trans.sum = adder.io.sum.peek().litValue()
    trans.cout = adder.io.cout.peek().litValue()
    mon2scPort.write(trans)
  }
}

object rm2scPort extends uvm_analysis_port[Transaction]
object drv2rmPort extends uvm_analysis_port[Transaction]
object mon2scPort extends uvm_analysis_port[Transaction]

class Agent(adder: Adder4Bit, report: CoverageReporter) extends uvm_agent {
  val dr = new Driver(adder, report)
  val mn = new Monitor(adder, report)
  val sequ = new Sequence
  dr.connectSequence(sequ)
}

class Environment(val adder: Adder4Bit, reporter: CoverageReporter) extends uvm_environment {
  val ag = new Agent(adder, reporter)
  val rf = new ReferenceModel
  val sc = new Scoreborad

  override def run(): Unit = {
    ag.run()
    rf.run()
    sc.run()
  }
}


class AdderUvmTest extends uvm_test {

  "Adder" should "add" in {

    test(new Adder4Bit).withAnnotations(VerilatorCoverage) { adder =>
      val AdderEnv = new Environment(adder, coverageReporter)
      AdderEnv.step()
      AdderEnv.step()
      AdderEnv.step()
    }

  }
}
