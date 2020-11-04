import chisel3._
import chiseltest._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.{LineCoverageAnnotation, ToggleCoverageAnnotation, VerilatorBackendAnnotation}
import functionalCoverage.Coverage.{Bins, CoverPoint}
import functionalCoverage.{CoverageReporter, CoverageTrait}
import leros.Types._
import leros.{AluAccu, AluAccuChisel}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class AluAccuTester extends AnyFlatSpec with CoverageTrait with ChiselScalatestTester with Matchers {

  val annotationsSeq = Seq(VerilatorBackendAnnotation, ToggleCoverageAnnotation, LineCoverageAnnotation)

  def testFun[T <: AluAccu](dut: T): Unit = {

    val cr = new CoverageReporter
    cr.register(
      CoverPoint(
        dut.io.accu,
        "accu", //CoverPoint 1
        Bins("lo10", 0 to 10) :: Bins("First100", 0 to 100) :: Nil
      ) ::
        Nil
    )

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

      cr.sample()
    }

    def test(values: Seq[Int]) = {
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
    cr.printReport()
  }

  "AluAccuChisel" should "pass" in {
    test(new AluAccuChisel(32)).withAnnotations(annotationsSeq) { dut => testFun(dut) }
  }

}
