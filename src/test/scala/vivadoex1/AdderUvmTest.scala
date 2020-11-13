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
    val otrans = new transaction
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


class transaction extends Random(30) with uvm_sequence_item {
  var x: RandInt = rand(x, 0 to 15 toList)
  var y: RandInt = rand(y, 0 to 15 toList)
  var cin: RandInt = rand(cin, 0x0 to 1 toList)
  var sum: BigInt = 0
  var cout: BigInt = 0

}

object sequence extends uvm_sequence[transaction] {
  val pendingT = new transaction()
  override def iterator: Iterator[transaction] = new Iterator[transaction] {
    var i: Int = -1
    def hasNext: Boolean = pendingT.randomize
    def next: transaction = {
      i += 1
      pendingT
    }
  }
}

class driver(adder: Adder4Bit, report: CoverageReporter) extends uvm_driver[transaction] {
  override def runPhase(): Unit = {
    if(sequence.iterator.hasNext) {
      val transaction = sequence.iterator.next()
      println(transaction.debug())
      adder.io.x.poke(transaction.x.U)
      adder.io.y.poke(transaction.y.U)
      adder.io.cin.poke(transaction.cin.U)
      adder.clock.step()
      drv2rmPort.write(transaction)
    }

  }
}

class scoreborad() extends uvm_scoreboard {
  def run(): Unit = {
    println(s"Expected Transaction:= ${mon2scPort.transactions.last.debug}")
    println(s"Calculated Transaction:= ${rm2scPort.transactions.last.debug}")
    assert(mon2scPort.transactions.last.sum == rm2scPort.transactions.last.sum)
    assert(mon2scPort.transactions.last.cout == rm2scPort.transactions.last.cout)
  }
}

class monitor(adder: Adder4Bit, reporter: CoverageReporter) extends uvm_monitor {
  override def runPhase(): Unit = {
    val trans = new transaction
    trans.x = adder.io.x.peek().litValue()
    trans.y = adder.io.y.peek().litValue()
    trans.cin = adder.io.cin.peek().litValue()
    trans.sum = adder.io.sum.peek().litValue()
    trans.cout = adder.io.cout.peek().litValue()
    mon2scPort.write(trans)
  }
}

object rm2scPort extends uvm_analysis_port[transaction]
object drv2rmPort extends uvm_analysis_port[transaction]
object mon2scPort extends uvm_analysis_port[transaction]

class agent(adder: Adder4Bit, report: CoverageReporter) extends uvm_agent {
  val dr = new driver(adder, report)
  val mn = new monitor(adder, report)
  def run(): Unit = {
    dr.runPhase()
    mn.runPhase()
  }
}

class env(adder: Adder4Bit, reporter: CoverageReporter) extends uvm_environment {
  val ag = new agent(adder, reporter)
  val rf = new ReferenceModel
  val sc = new scoreborad
  def run(): Unit = {
    ag.run()
    rf.run()
    sc.run()
  }
}


class AdderUvmTest extends uvm_test {

  "Adder" should "add" in {

    test(new Adder4Bit).withAnnotations(VerilatorCoverage) { adder =>
      val AdderEnv = new env(adder, coverageReporter)
      AdderEnv.run()
      AdderEnv.run()
      AdderEnv.run()
    }

  }
}
