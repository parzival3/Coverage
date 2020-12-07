package vivadoex1

import backends.jacop._
import chisel3.fromIntToLiteral
import chisel3.tester.testableData
import chiseluvm.classes._
import coverage.CoverageReporter
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder

import scala.language.postfixOps

class AdderUvmTest2 extends uvm_test with VerificationContext {

  class ReferenceModel {
    def run(): Unit = {
      val ctrans = drv2rmPort.transactions.last
      val otrans = new Transaction
      otrans.x = ctrans.x
      otrans.y = ctrans.y
      otrans.cin = ctrans.cin

      if (otrans.x.value() + otrans.y.value() + otrans.cin.value() > 15) {
        otrans.cout = 1
        otrans.sum = (ctrans.x.value() + ctrans.y.value() + ctrans.cin.value()) - 16
      } else {
        otrans.cout = 0
        otrans.sum = ctrans.x.value() + ctrans.y.value() + ctrans.cin.value()
      }
      rm2scPort.write(otrans)
    }
  }

  class Transaction extends RandObj(new Model) with uvm_sequence_item {
    var x = new Rand("x", 0, 15)
    var y = new Rand("y", 0, 15)
    var cin = new Rand("cin", 0x0, 1)
    var sum:  BigInt = 0
    var cout: BigInt = 0
    override def toString(): String = {
      super.toString() + "sum = " + sum.toString() + ", cout = " + cout.toString
    }
  }

  class Sequence extends uvm_sequence[Transaction] {
    val pendingT = new Transaction()
    override def iterator: Iterator[Transaction] = new Iterator[Transaction] {
      var i:       Int = -1
      def hasNext: Boolean = pendingT.randomize
      def next: Transaction = {
        i += 1
        pendingT
      }
    }
  }

  class Driver(adder: Adder4Bit, report: CoverageReporter) extends uvm_driver[Transaction] {
    override def run(): Unit = {
      if (sequence.iterator.hasNext) {
        val transaction = sequence.iterator.next()
        adder.io.x.poke(transaction.x.value().U)
        adder.io.y.poke(transaction.y.value().U)
        adder.io.cin.poke(transaction.cin.value().U)
        drv2rmPort.write(transaction)
      }

    }
  }

  class Scoreborad() extends uvm_scoreboard {
    def run(): Unit = {
      println(s"Expected Transaction:= ${mon2scPort.transactions.last.toString}")
      println(s"Calculated Transaction:= ${rm2scPort.transactions.last.toString}")
      assert(mon2scPort.transactions.last.sum == rm2scPort.transactions.last.sum)
      assert(mon2scPort.transactions.last.cout == rm2scPort.transactions.last.cout)
    }
  }

  class Monitor(adder: Adder4Bit, reporter: CoverageReporter) extends uvm_monitor {
    override def run(): Unit = {
      val trans = new Transaction
      trans.x.setVar(adder.io.x.peek().litValue())
      trans.y.setVar(adder.io.y.peek().litValue())
      trans.cin.setVar(adder.io.cin.peek().litValue())
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

  "Adder" should "add" in {

    test(new Adder4Bit).withAnnotations(VerilatorCoverage) { adder =>
      val AdderEnv = new Environment(adder, coverageReporter)
      AdderEnv.step()
      AdderEnv.step()
      AdderEnv.step()
    }

  }
}
