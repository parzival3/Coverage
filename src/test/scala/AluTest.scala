import chisel3.{fromIntToLiteral, fromIntToWidth}
import chiseltest.{testableClock, testableData, ChiselScalatestTester}
import chiseltest.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chiseltest.internal.{
  LineCoverageAnnotation,
  TestOptionObject,
  ToggleCoverageAnnotation,
  VerilatorBackendAnnotation
}
import coverage.Coverage
import coverage.test.tags.VerilatorTest
import coverage.test.utils.Alu
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

trait AluBehavior {
  this: AnyFlatSpec with ChiselScalatestTester =>

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

class AluTest extends AnyFlatSpec with ChiselScalatestTester with Matchers {
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

@VerilatorTest
class AluTestVerilator extends AnyFlatSpec with Coverage with ChiselScalatestTester with Matchers {
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
