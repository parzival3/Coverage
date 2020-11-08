package chisel

import chisel3.{fromIntToLiteral, fromIntToWidth}
import chiseltest.{ChiselScalatestTester, testableClock, testableData}
import chiseltest.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chiseltest.internal.TestOptionObject
import org.scalatest.FlatSpec
import testutils.Alu

trait AluBehavior {
  this: FlatSpec with ChiselScalatestTester =>

  def mask(s: Int): Int = (1 << s) - 1

  def testAddition(a: Int, b: Int, s: Int, ann: Seq[TestOptionObject]): Unit = {
    val result = (a + b) & mask(s)
    it should s"+ $a, $b and the result == $result" in {
      test(new Alu(s)).withAnnotations(ann) { c =>
        c.io.fn.poke(0.U)
        c.io.a.poke(a.U(s.W))
        c.io.b.poke(b.U(s.W))
        c.clock.step()
        c.io.result.expect(result.U(s.W))
      }
    }
  }

  def testOr(a: Int, b: Int, s: Int, ann: Seq[TestOptionObject]): Unit = {
    val result = (a | b) & mask(s)
    it should s"| $a, $b and the result == $result" in {
      test(new Alu(s)).withAnnotations(ann) { c =>
        c.io.fn.poke(2.U)
        c.io.a.poke(a.U(s.W))
        c.io.b.poke(b.U(s.W))
        c.clock.step()
        c.io.result.expect(result.U(s.W))
      }
    }
  }

  def testAnd(a: Int, b: Int, s: Int, ann: Seq[TestOptionObject]): Unit = {
    val result = (a & b) & mask(s)
    it should s"& $a, $b and the result == $result" in {
      test(new Alu(s)).withAnnotations(ann) { c =>
        c.io.fn.poke(3.U)
        c.io.a.poke(a.U(s.W))
        c.io.b.poke(b.U(s.W))
        c.clock.step()
        c.io.result.expect(result.U(s.W))
      }
    }
  }

  def testSubtraction(a: Int, b: Int, s: Int, ann: Seq[TestOptionObject]): Unit = {
    val result = (a - b) & mask(s)
    it should s"- $a, $b and the result == $result" in {
      test(new Alu(s)).withAnnotations(ann) { c =>
        c.io.fn.poke(1.U)
        c.io.a.poke(a.U(s.W))
        c.io.b.poke(b.U(s.W))
        c.clock.step()
        c.io.result.expect(result.U(s.W))
      }
    }
  }
}
