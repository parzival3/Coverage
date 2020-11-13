package vivadoex1

import Chisel.{Cat, fromIntToWidth, fromtIntToLiteral}
import chisel3.tester.{testableClock, testableData}
import chisel3.util.{is, switch}
import chisel3.{Bundle, Input, Module, Output, UInt, Wire}
import chiseluvm.classes.uvm_test

class HalfAdder extends Module {
  val io = IO(new Bundle{
    val x: UInt = Input(UInt(1.W))
    val y: UInt = Input(UInt(1.W))
    val s: UInt = Output(UInt(1.W))
    val c: UInt = Output(UInt(1.W))
  })

  val sResult: UInt = Wire(UInt(1.W))
  val cResult: UInt = Wire(UInt(1.W))
  sResult := io.x ^ io.y
  cResult := io.x & io.y
  io.s := sResult
  io.c := cResult
}

class FullAdder extends Module {
  val io = IO(new Bundle{
    val x = Input(UInt(1.W))
    val y = Input(UInt(1.W))
    val cin = Input(UInt(1.W))
    val s = Output(UInt(1.W))
    val cout = Output(UInt(1.W))
  })

  val halfAdder1 = Module(new HalfAdder)
  halfAdder1.io.x := io.x
  halfAdder1.io.y := io.y
  val c1 = halfAdder1.io.c

  val halfAdder2 = Module(new HalfAdder)
  halfAdder2.io.x := io.cin
  halfAdder2.io.y := halfAdder1.io.s
  io.s := halfAdder2.io.s
  io.cout := halfAdder2.io.c | c1
}

class Adder4Bit extends Module {
  val io = IO(new Bundle{
    val x = Input(UInt(4.W))
    val y = Input(UInt(4.W))
    val cin = Input(UInt(1.W))
    val sum = Output(UInt(4.W))
    val cout = Output(UInt(1.W))
  })

  val fulladder1 = Module(new FullAdder)
  fulladder1.io.x := io.x(0)
  fulladder1.io.y := io.y(0)
  fulladder1.io.cin := io.cin
  val sum1 = fulladder1.io.s
  val c1 = fulladder1.io.cout

  val fulladder2 = Module(new FullAdder)
  fulladder2.io.x := io.x(1)
  fulladder2.io.y := io.y(1)
  fulladder2.io.cin := c1
  val sum2 = Cat(fulladder2.io.s, sum1)
  val c2 = fulladder2.io.cout

  val fulladder3 = Module(new FullAdder)
  fulladder3.io.x := io.x(2)
  fulladder3.io.y := io.y(2)
  fulladder3.io.cin := c2
  val sum3 = Cat(fulladder3.io.s, sum2)
  val c3 = fulladder3.io.cout

  val fulladder4 = Module(new FullAdder)
  fulladder4.io.x := io.x(3)
  fulladder4.io.y := io.y(3)
  fulladder4.io.cin := c3
  io.sum := Cat(fulladder4.io.s, sum3).asUInt
  io.cout := fulladder4.io.cout
}

class Adder4BitTest extends uvm_test {
 "HalfAdderTest" should "sum" in {
   test(new HalfAdder) { adder =>
     adder.io.x.poke(1.U)
     adder.io.y.poke(0.U)
     adder.clock.step()
     adder.io.s.expect(1.U)
     adder.io.c.equals(0.U)
     //----------------------
     adder.io.x.poke(1.U)
     adder.io.y.poke(1.U)
     adder.clock.step()
     adder.io.s.expect(0.U)
     adder.io.c.equals(1.U)
   }
 }

  "Full Adder" should "sum" in {
    test(new FullAdder) { adder =>
      adder.io.x.poke(1.U)
      adder.io.y.poke(0.U)
      adder.io.cin.poke(0.U)
      adder.clock.step()
      adder.io.s.expect(1.U)
      adder.io.cout.expect(0.U)

      adder.io.x.poke(1.U)
      adder.io.y.poke(0.U)
      adder.io.cin.poke(1.U)
      adder.clock.step()
      adder.io.s.expect(0.U)
      adder.io.cout.expect(1.U)

      adder.io.x.poke(1.U)
      adder.io.y.poke(1.U)
      adder.io.cin.poke(1.U)
      adder.clock.step()
      adder.io.s.expect(1.U)
      adder.io.cout.expect(1.U)
    }
  }

  "4BitAdder" should "sum" in {
    test(new Adder4Bit){adder =>
      adder.io.x.poke(4.U)
      adder.io.y.poke(4.U)
      adder.io.cin.poke(0.U)
      adder.clock.step()
      adder.io.sum.expect(8.U)
      adder.io.cout.expect(0.U)
      adder.io.x.poke(8.U)
      adder.io.y.poke(8.U)
      adder.io.cin.poke(0.U)
      adder.io.sum.expect(0.U)
      adder.io.cout.expect(1.U)
    }

  }
}