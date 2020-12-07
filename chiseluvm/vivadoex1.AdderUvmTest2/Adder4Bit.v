module HalfAdder(
  input   io_x,
  input   io_y,
  output  io_s,
  output  io_c
);
  assign io_s = io_x ^ io_y; // @[Adder.scala 19:19]
  assign io_c = io_x & io_y; // @[Adder.scala 20:19]
endmodule
module FullAdder(
  input   io_x,
  input   io_y,
  input   io_cin,
  output  io_s,
  output  io_cout
);
  wire  halfAdder1_io_x; // @[Adder.scala 34:26]
  wire  halfAdder1_io_y; // @[Adder.scala 34:26]
  wire  halfAdder1_io_s; // @[Adder.scala 34:26]
  wire  halfAdder1_io_c; // @[Adder.scala 34:26]
  wire  halfAdder2_io_x; // @[Adder.scala 39:26]
  wire  halfAdder2_io_y; // @[Adder.scala 39:26]
  wire  halfAdder2_io_s; // @[Adder.scala 39:26]
  wire  halfAdder2_io_c; // @[Adder.scala 39:26]
  HalfAdder halfAdder1 ( // @[Adder.scala 34:26]
    .io_x(halfAdder1_io_x),
    .io_y(halfAdder1_io_y),
    .io_s(halfAdder1_io_s),
    .io_c(halfAdder1_io_c)
  );
  HalfAdder halfAdder2 ( // @[Adder.scala 39:26]
    .io_x(halfAdder2_io_x),
    .io_y(halfAdder2_io_y),
    .io_s(halfAdder2_io_s),
    .io_c(halfAdder2_io_c)
  );
  assign io_s = halfAdder2_io_s; // @[Adder.scala 42:8]
  assign io_cout = halfAdder2_io_c | halfAdder1_io_c; // @[Adder.scala 43:30]
  assign halfAdder1_io_x = io_x; // @[Adder.scala 35:19]
  assign halfAdder1_io_y = io_y; // @[Adder.scala 36:19]
  assign halfAdder2_io_x = io_cin; // @[Adder.scala 40:19]
  assign halfAdder2_io_y = halfAdder1_io_s; // @[Adder.scala 41:19]
endmodule
module Adder4Bit(
  input        clock,
  input        reset,
  input  [3:0] io_x,
  input  [3:0] io_y,
  input        io_cin,
  output [3:0] io_sum,
  output       io_cout
);
  wire  fulladder1_io_x; // @[Adder.scala 55:26]
  wire  fulladder1_io_y; // @[Adder.scala 55:26]
  wire  fulladder1_io_cin; // @[Adder.scala 55:26]
  wire  fulladder1_io_s; // @[Adder.scala 55:26]
  wire  fulladder1_io_cout; // @[Adder.scala 55:26]
  wire  fulladder2_io_x; // @[Adder.scala 62:26]
  wire  fulladder2_io_y; // @[Adder.scala 62:26]
  wire  fulladder2_io_cin; // @[Adder.scala 62:26]
  wire  fulladder2_io_s; // @[Adder.scala 62:26]
  wire  fulladder2_io_cout; // @[Adder.scala 62:26]
  wire  fulladder3_io_x; // @[Adder.scala 69:26]
  wire  fulladder3_io_y; // @[Adder.scala 69:26]
  wire  fulladder3_io_cin; // @[Adder.scala 69:26]
  wire  fulladder3_io_s; // @[Adder.scala 69:26]
  wire  fulladder3_io_cout; // @[Adder.scala 69:26]
  wire  fulladder4_io_x; // @[Adder.scala 76:26]
  wire  fulladder4_io_y; // @[Adder.scala 76:26]
  wire  fulladder4_io_cin; // @[Adder.scala 76:26]
  wire  fulladder4_io_s; // @[Adder.scala 76:26]
  wire  fulladder4_io_cout; // @[Adder.scala 76:26]
  wire [2:0] sum3 = {fulladder3_io_s,fulladder2_io_s,fulladder1_io_s}; // @[Cat.scala 29:58]
  FullAdder fulladder1 ( // @[Adder.scala 55:26]
    .io_x(fulladder1_io_x),
    .io_y(fulladder1_io_y),
    .io_cin(fulladder1_io_cin),
    .io_s(fulladder1_io_s),
    .io_cout(fulladder1_io_cout)
  );
  FullAdder fulladder2 ( // @[Adder.scala 62:26]
    .io_x(fulladder2_io_x),
    .io_y(fulladder2_io_y),
    .io_cin(fulladder2_io_cin),
    .io_s(fulladder2_io_s),
    .io_cout(fulladder2_io_cout)
  );
  FullAdder fulladder3 ( // @[Adder.scala 69:26]
    .io_x(fulladder3_io_x),
    .io_y(fulladder3_io_y),
    .io_cin(fulladder3_io_cin),
    .io_s(fulladder3_io_s),
    .io_cout(fulladder3_io_cout)
  );
  FullAdder fulladder4 ( // @[Adder.scala 76:26]
    .io_x(fulladder4_io_x),
    .io_y(fulladder4_io_y),
    .io_cin(fulladder4_io_cin),
    .io_s(fulladder4_io_s),
    .io_cout(fulladder4_io_cout)
  );
  assign io_sum = {fulladder4_io_s,sum3}; // @[Cat.scala 29:58]
  assign io_cout = fulladder4_io_cout; // @[Adder.scala 81:11]
  assign fulladder1_io_x = io_x[0]; // @[Adder.scala 56:26]
  assign fulladder1_io_y = io_y[0]; // @[Adder.scala 57:26]
  assign fulladder1_io_cin = io_cin; // @[Adder.scala 58:21]
  assign fulladder2_io_x = io_x[1]; // @[Adder.scala 63:26]
  assign fulladder2_io_y = io_y[1]; // @[Adder.scala 64:26]
  assign fulladder2_io_cin = fulladder1_io_cout; // @[Adder.scala 65:21]
  assign fulladder3_io_x = io_x[2]; // @[Adder.scala 70:26]
  assign fulladder3_io_y = io_y[2]; // @[Adder.scala 71:26]
  assign fulladder3_io_cin = fulladder2_io_cout; // @[Adder.scala 72:21]
  assign fulladder4_io_x = io_x[3]; // @[Adder.scala 77:26]
  assign fulladder4_io_y = io_y[3]; // @[Adder.scala 78:26]
  assign fulladder4_io_cin = fulladder3_io_cout; // @[Adder.scala 79:21]
endmodule
