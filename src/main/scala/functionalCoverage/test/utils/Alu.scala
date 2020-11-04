package functionalCoverage.test.utils

import chisel3._
import chisel3.util._

class Alu(size: Int) extends Module {
  val io = IO(new Bundle {
    val fn:     UInt = Input(UInt(2.W))
    val a:      UInt = Input(UInt(size.W))
    val b:      UInt = Input(UInt(size.W))
    val result: UInt = Output(UInt(size.W))
  })

  val result: UInt = Wire(UInt(size.W))
  result := 0.U

  switch(io.fn) {
    is(0.U) { result := io.a + io.b }
    is(1.U) { result := io.a - io.b }
    is(2.U) { result := io.a | io.b }
    is(3.U) { result := io.a & io.b }
  }
  io.result := result
}
