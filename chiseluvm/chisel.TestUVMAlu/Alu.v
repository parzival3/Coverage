module Alu(
  input        clock,
  input        reset,
  input  [1:0] io_fn,
  input  [7:0] io_a,
  input  [7:0] io_b,
  output [7:0] io_result
);
  wire  _T = 2'h0 == io_fn; // @[Conditional.scala 37:30]
  wire [7:0] _result_T_1 = io_a + io_b; // @[Alu.scala 18:30]
  wire  _T_1 = 2'h1 == io_fn; // @[Conditional.scala 37:30]
  wire [7:0] _result_T_3 = io_a - io_b; // @[Alu.scala 19:30]
  wire  _T_2 = 2'h2 == io_fn; // @[Conditional.scala 37:30]
  wire [7:0] _result_T_4 = io_a | io_b; // @[Alu.scala 20:30]
  wire  _T_3 = 2'h3 == io_fn; // @[Conditional.scala 37:30]
  wire [7:0] _result_T_5 = io_a & io_b; // @[Alu.scala 21:30]
  wire [7:0] _GEN_0 = _T_3 ? _result_T_5 : 8'h0; // @[Conditional.scala 39:67 Alu.scala 21:22 Alu.scala 15:10]
  wire [7:0] _GEN_1 = _T_2 ? _result_T_4 : _GEN_0; // @[Conditional.scala 39:67 Alu.scala 20:22]
  wire [7:0] _GEN_2 = _T_1 ? _result_T_3 : _GEN_1; // @[Conditional.scala 39:67 Alu.scala 19:22]
  assign io_result = _T ? _result_T_1 : _GEN_2; // @[Conditional.scala 40:58 Alu.scala 18:22]
endmodule
