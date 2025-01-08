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
                input<clk> clk,
                input<btn> btnU,
                input<btn> btnD,
                input<btn> btnC,
                output<wire> vga_hs,
                output<wire> vga_vs,
                output<reg_4> vga_r,
                output<reg_4> vga_g,
                output<reg_4> vga_b
           ) {
            wire_9 x = wire_9();   /* 0..(640)..1024 */    // wire[9:0] x;
            wire_9 y = wire_9();    /* 0..(480).. 512 */    // wire[8:0] y;
            wire_8 xcur = wire_8(); /* 0..(80) .. 128 */

            xcur.v = x.range(9, 3);  // wire[6:0] curX; assign curX= x[9:3];
            wire_5 ycur = wire_5(); /* 0..(60) .. 64  */
            ycur.v = y.range(8, 3);
            wire_8 vga_x = wire_8();
            wire_8 vga_y = wire_8();
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
                    input.class,
                    input.class,
                    input.class,
                    input.class,
                    output.class,
                    output.class,
                    output.class,
                    output.class,
                    output.class);
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


