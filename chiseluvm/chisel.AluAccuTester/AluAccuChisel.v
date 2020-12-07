module AluAccuChisel(
  input         clock,
  input         reset,
  input  [2:0]  io_op,
  input  [31:0] io_din,
  input         io_ena,
  output [31:0] io_accu
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_REG_INIT
  reg [31:0] a; // @[AluAccuChisel.scala 18:24]
  wire  _T = 3'h0 == io_op; // @[Conditional.scala 37:30]
  wire  _T_1 = 3'h1 == io_op; // @[Conditional.scala 37:30]
  wire [31:0] _res_T_1 = a + io_din; // @[AluAccuChisel.scala 30:16]
  wire  _T_2 = 3'h2 == io_op; // @[Conditional.scala 37:30]
  wire [31:0] _res_T_3 = a - io_din; // @[AluAccuChisel.scala 33:16]
  wire  _T_3 = 3'h3 == io_op; // @[Conditional.scala 37:30]
  wire [31:0] _res_T_4 = a & io_din; // @[AluAccuChisel.scala 36:16]
  wire  _T_4 = 3'h4 == io_op; // @[Conditional.scala 37:30]
  wire [31:0] _res_T_5 = a | io_din; // @[AluAccuChisel.scala 39:16]
  wire  _T_5 = 3'h5 == io_op; // @[Conditional.scala 37:30]
  wire [31:0] _res_T_6 = a ^ io_din; // @[AluAccuChisel.scala 42:16]
  wire  _T_6 = 3'h7 == io_op; // @[Conditional.scala 37:30]
  wire  _T_7 = 3'h6 == io_op; // @[Conditional.scala 37:30]
  wire [31:0] _GEN_0 = _T_7 ? io_din : a; // @[Conditional.scala 39:67 AluAccuChisel.scala 48:11]
  wire [31:0] _GEN_1 = _T_6 ? {{1'd0}, a[31:1]} : _GEN_0; // @[Conditional.scala 39:67 AluAccuChisel.scala 45:11]
  wire [31:0] _GEN_2 = _T_5 ? _res_T_6 : _GEN_1; // @[Conditional.scala 39:67 AluAccuChisel.scala 42:11]
  wire [31:0] _GEN_3 = _T_4 ? _res_T_5 : _GEN_2; // @[Conditional.scala 39:67 AluAccuChisel.scala 39:11]
  wire [31:0] _GEN_4 = _T_3 ? _res_T_4 : _GEN_3; // @[Conditional.scala 39:67 AluAccuChisel.scala 36:11]
  wire [31:0] _GEN_5 = _T_2 ? _res_T_3 : _GEN_4; // @[Conditional.scala 39:67 AluAccuChisel.scala 33:11]
  assign io_accu = a; // @[AluAccuChisel.scala 56:11]
  always @(posedge clock) begin
    if (reset) begin // @[AluAccuChisel.scala 18:24]
      a <= 32'h0; // @[AluAccuChisel.scala 18:24]
    end else if (io_ena) begin // @[AluAccuChisel.scala 52:16]
      if (!(_T)) begin // @[Conditional.scala 40:58]
        if (_T_1) begin // @[Conditional.scala 39:67]
          a <= _res_T_1; // @[AluAccuChisel.scala 30:11]
        end else begin
          a <= _GEN_5;
        end
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  a = _RAND_0[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
