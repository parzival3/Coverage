package chisel

import chisel3.{fromIntToLiteral, fromIntToWidth}
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chiseltest.{testableClock, testableData, ChiselScalatestTester}
import chiseltest.internal.{LineCoverageAnnotation, ToggleCoverageAnnotation, VerilatorBackendAnnotation}
import chiseluvm.CoverageTrait
import chiseluvm.test.tags.VerilatorTest
import org.scalatest.{FlatSpec, Matchers}
import testutils.Alu

@VerilatorTest
class AluTestVerilator extends FlatSpec with CoverageTrait with ChiselScalatestTester with Matchers {
  behavior.of("ALU")

  def mask(s: Int): Int = (1 << 4) - 1

  val annotationsSeq = Seq(VerilatorBackendAnnotation, ToggleCoverageAnnotation, LineCoverageAnnotation)

  val result = (2 + 1) & mask(4)
  it should "test static circuits" in {
    test(new Alu(4)).withAnnotations(annotationsSeq) { c =>
      c.io.fn.poke(2.U)
      c.io.a.poke(2.U(4.W))
      c.io.b.poke(1.U(4.W))
      c.clock.step()
      c.io.result.expect(result.U(4.W))
    }
  }
}
