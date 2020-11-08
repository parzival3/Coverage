package chisel

import chisel3.core.fromIntToWidth
import chisel3.fromIntToLiteral
import chiseltest.{ChiselScalatestTester, testableClock, testableData}
import org.scalatest.{FlatSpec, Matchers}
import testutils.Alu

class AluTest extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior.of("ALU")

  def mask(s: Int): Int = (1 << 4) - 1

  val result = (2 + 1) & mask(4)
  it should "test static circuits" in {
    test(new Alu(4)) { c =>
      c.io.fn.poke(2.U)
      c.io.a.poke(2.U(4.W))
      c.io.b.poke(1.U(4.W))
      c.clock.step()
      c.io.result.expect(result.U(4.W))
    }
  }
}
