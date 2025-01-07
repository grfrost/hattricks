package vga;

import jdk.incubator.code.CodeReflection;
import jdk.incubator.code.Op;
import jdk.incubator.code.op.CoreOp;

import java.lang.reflect.Method;

import static vga.Verilog.Btn;
import static vga.Verilog.Clk;
import static vga.Verilog.Vga;
import static vga.Verilog.onPosEdge;
import static vga.Verilog.wire;

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
        public static void vga(Clk clk, Btn btnU, Btn btnD, Btn btnC, Vga vga) {
            var x = wire(10);   /* 0..(640)..1024 */    // wire[9:0] x;
            var y = wire(9);    /* 0..(480).. 512 */    // wire[8:0] y;
            var xcur = wire(8); /* 0..(80) .. 128 */  xcur.assign(x.range(9, 3));  // wire[6:0] curX; assign curX= x[9:3];
            var ycur = wire(5); /* 0..(60) .. 64  */  ycur.assign(y.range(8, 3));
            onPosEdge(clk,_ -> {
                if (vga.pos().x() == xcur && vga.pos().y() == ycur) {
                    vga.rgb().set(0b0000_0000_0000);
                } else {
                    vga.rgb().set(0);
                }
            });

        }
    }

    public static void main(String[] _args) {
        //   VGAFrame vf = new VGAFrame("VGA", 680, 480);
        //  vf.setShader(Compute::top);
        Method method = null;
        try {
            method = Compute.class.getDeclaredMethod("vga", Clk.class, Btn.class,Btn.class,Btn.class, Vga.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        CoreOp.FuncOp javaFunc = Op.ofMethod(method).get();
      //  javaFunc.writeTo(System.out);

        VerilogBuilder verilogBuilder = new VerilogBuilder();
        verilogBuilder.module(javaFunc);
        var code = verilogBuilder.toString();
/*

        CoreOp.FuncOp transformed = javaFunc.transform((builder, op) -> {
            if (op instanceof CoreOp.InvokeOp invokeOp) {
                builder.op(op);
                //  CopyContext cc = builder.context();
                //  Block.Builder bb = builder;
                // var invokePre = CoreOp.invoke(PRE);
                //  RootOp rootOp = new RootOp();
                // builder.op(rootOp);
                //  builder.op(invokeOp);
                //  builder.op(CoreOp.invoke(POST));
            } else {
                builder.op(op);
            }
            return builder;
        }); */


        //  for (Point point = vf.waitForPoint(1000 / 60); point != null; point = vf.waitForPoint(10)) {
        //    System.out.println("You pressed " + point);
        // }
System.out.println(code);

    }

}


