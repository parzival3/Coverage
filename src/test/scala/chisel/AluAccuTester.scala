package chisel

import chisel3.{fromIntToLiteral, fromIntToWidth}
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chiseltest.{testableClock, testableData, ChiselScalatestTester}
import chiseltest.internal.{LineCoverageAnnotation, ToggleCoverageAnnotation, VerilatorBackendAnnotation}
import chiseltest._
import chisel3._
import chiseluvm.CoverageTrait
import testutils.Types._
import coverage.Coverage.{Bins, CoverPoint}
import coverage.CoverageReporter
import org.scalatest.{FlatSpec, Matchers}
import testutils.{Alu, AluAccu, AluAccuChisel, AluAccuGenerated}

class AluAccuTester extends FlatSpec with CoverageTrait with ChiselScalatestTester with Matchers {

  def testFun[T <: AluAccu](dut: T): Unit = {

    coverageReporter.register(
      //Declare CoverPoints
      CoverPoint(
        dut.io.accu,
        "accu", //CoverPoint 1
        Bins("lo10", 0 to 10) :: Bins("First100", 0 to 100) :: Nil
      ) ::
        // CoverPoint(dut.io.test, "test", //CoverPoint 2
        //     Bins("testLo10", 0 to 10)::Nil)::
        Nil
    )
    //Declare cross points
    /*Cross("accuAndTest", "accu", "test",
            CrossBin("both1", 1 to 1, 1 to 1)::Nil)::
        Nil)*/

    def alu(a: Int, b: Int, op: Int): Int = {

      op match {
        case 0 => a
        case 1 => a + b
        case 2 => a - b
        case 3 => a & b
        case 4 => a | b
        case 5 => a ^ b
        case 6 => b
        case 7 => a >>> 1
        case _ => -123 // This shall not happen
      }
    }

    def toUInt(i: Int) = (BigInt(i) & 0x00ffffffffL).asUInt(32.W)

    def testOne(a: Int, b: Int, fun: Int): Unit = {
      dut.io.op.poke(ld)
      dut.io.ena.poke(true.B)
      dut.io.din.poke(toUInt(a))
      dut.clock.step()
      dut.io.op.poke(fun.asUInt())
      dut.io.din.poke(toUInt(b))
      dut.clock.step()
      dut.io.accu.expect(toUInt(alu(a, b, fun)))

      coverageReporter.sample()
    }

    def test(values: Seq[Int]): Unit = {
      // for (fun <- add to shr) {
      for (fun <- 0 until 8) {
        for (a <- values) {
          for (b <- values) {
            testOne(a, b, fun)
          }
        }
      }
    }

    // Some interesting corner cases
    val interesting = Array(1, 2, 4, -123, 123, 0, -1, -2, 0x80000000, 0x7fffffff)
    test(interesting)

    val randArgs = Seq.fill(20)(scala.util.Random.nextInt)
    test(randArgs)

    //Print coverage report
    coverageReporter.printReport()
  }

  "AluAccuChisel" should "pass" in {
    test(new AluAccuChisel(32))
      .withAnnotations(Seq(VerilatorBackendAnnotation, ToggleCoverageAnnotation, LineCoverageAnnotation)) { dut =>
        testFun(dut)
      }
  }

}
