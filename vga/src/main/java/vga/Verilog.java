package vga;

import jdk.incubator.code.CodeReflection;
import jdk.incubator.code.Op;
import jdk.incubator.code.op.CoreOp;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class Verilog {
    interface Value {
        default Value value(){return this;}
        default void set(Value v){}
    }

    static class Wire implements Value {int width; Wire(int width){this.width = width; } static Wire width(int width){return new Wire(width);}   Wire assign(Value value){return width(width);} Value range(int hi, int lo){return null;}}
    //record Wire(int width) implements Value {static Wire width(int width){return new Wire(width);}  void set(int v){} Wire assign(Value value){return width(width);} Value range(int hi, int lo){return null;}}

    record Clk(int mhz){boolean posEdge(){return true;} void onPosEdge(Consumer<Clk> clkConsumer){clkConsumer.accept(this);}}
    record Reg (int width) implements Value{}
    record Led(int n) implements Value{}
    record Btn(int n){}
    record VgaColor(int width, int value){
        public static VgaColor of(int width, int v){return new VgaColor(width, v);}
        public VgaColor of(int v){return of(width(),v);}
    }

    record VgaRgb(VgaColor r, VgaColor g, VgaColor b ){void set(int rgb){} void set(int r, int g, int b){}}
    record VgaSync(Wire hsync, Wire vsync){}
    record VgaPos(Value x, Value y){}
    record Vga(VgaSync sync, VgaPos pos, VgaRgb rgb){}

static Wire wire(int width){
        return Wire.width(width);
}

static <T>void onPosEdge(T signal, Consumer<T> consumer){

}

    public static void main(String[] args) throws NoSuchMethodException {
        String methodName = "vga";
        Method method =  Main.class.getDeclaredMethod("vga", Clk.class, Btn.class, Vga.class);

        CoreOp.FuncOp javaFunc = Op.ofMethod(method).get();
        javaFunc.writeTo(System.out);


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
        });


    }
}