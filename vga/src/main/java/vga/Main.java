package vga;

import jdk.incubator.code.CodeReflection;
import jdk.incubator.code.Op;
import jdk.incubator.code.op.CoreOp;

import java.lang.reflect.Method;

import static vga.Verilog.*;

public class Main {
    static class Compute {
        @CodeReflection
        public static void top(byte[] vga, int x, int y, int width, int height, long frame) {
            //   if ((x > 24 && x < 50) && y == 24) {
            vga[y * width + x] = (byte) (x & 0xff);
            // } else {
            // vga[y * width + x] = (byte) 0x00;
            //}
        }

        @CodeReflection
        public static void vga(
                Input<Clk> clk,
                Input<Btn> btnU,
                Input<Btn> btnD,
                Input<Btn> btnC,
                Output<Wire> vga_hs,
                Output<Wire> vga_vs,
                Output<Reg_4> vga_r,
                Output<Reg_4> vga_g,
                Output<Reg_4> vga_b
           ) {
            var x = wire_9();   /* 0..(640)..1024 */    // wire[9:0] x;
            var y = wire_9();    /* 0..(480).. 512 */    // wire[8:0] y;
            var xcur= wire_6(); /* 0..(80) .. 128 */ //wire[6:0] curX;
            assign(xcur, x.range(9, 3));// assign curX= x[9:3];
            var ycur = wire_5(); /* 0..(60) .. 64  */
            assign(ycur, y.range(8, 3));
            var vga_x = wire_8();
            var vga_y = wire_8();
            onPosEdge(clk,_ -> {
                if (vga_x.v == xcur.v && vga_y.v == ycur.v) {
                    vga_r.wire().v = 0b1000;
                    vga_g.wire().v = 0b1000;
                    vga_b.wire().v = 0b1000;
                } else {
                    vga_r.wire().v = 0b0000;
                    vga_g.wire().v = 0b0000;
                    vga_b.wire().v = 0b0000;
                }
            });

        }
    }

    public static void main(String[] _args) {
        //   VGAFrame vf = new VGAFrame("VGA", 680, 480);
        //  vf.setShader(Compute::top);
        Method method = null;
        try {
            method = Compute.class.getDeclaredMethod("vga",
                    Input.class,
                    Input.class,
                    Input.class,
                    Input.class,
                    Output.class,
                    Output.class,
                    Output.class,
                    Output.class,
                    Output.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        CoreOp.FuncOp javaFunc = Op.ofMethod(method).get();
      //  javaFunc.writeTo(System.out);

        VerilogBuilder verilogBuilder = new VerilogBuilder();
        verilogBuilder.module(javaFunc);
        var code = verilogBuilder.toString();
        System.out.println(code);

    }

}


