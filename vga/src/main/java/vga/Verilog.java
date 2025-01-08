package vga;

import jdk.incubator.code.Op;
import jdk.incubator.code.op.CoreOp;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class Verilog {


    static abstract class connection {
        public int v;
        public int range(int hi, int low) {
            return  v>>low & (v<<(hi-low)-1);
        }
    }

    static abstract class bus extends connection {
        final public int hi;
        final public int lo;

        public  bus(int hi, int lo) {
            this.hi = hi;
            this.lo = lo;
        }


    }
    record output<T extends connection>(T wire){}
    record input<T extends connection>(T wire){}
    static class reg extends bus {

        reg( ) {
            super( 1,0);
        }
    }
    static class reg_4 extends bus {

        reg_4( ) {
            super(4, 0);
        }
    }
    // interface wire {
        static wire wire_1(){return new wire();}
        static wire_9_3 wire_9_3(){return new wire_9_3();}
        static wire_9 wire_9(){return new wire_9();}
         static wire_8 wire_8() {return new wire_8();}
         static wire_4 wire_4() {return new wire_4();}
         static wire_5 wire_5() {return new wire_5();}
         static wire_12 wire_12() {return new wire_12();}
         static wire_16 wire_16() {return new wire_16();}
    static reg reg() {return new reg();}
    static reg_4 reg_4() {return new reg_4();}
     //}

    static class wire extends bus {
        wire() {
            super(1,0);
        }
    }

    static class wire_9_3   extends bus{
        wire_9_3() {
            super(9,3);
        }
    }

    static class wire_9  extends bus{
        wire_9() {
            super(9,0);
        }
    }

    static class wire_16  extends bus{
        wire_16() {
            super(16,0);
        }
    }

    static class wire_12  extends bus{
        wire_12() {
            super(12,0);
        }
    }
    static class wire_8  extends bus{
        wire_8() {
            super(8,0);
        }
    }
    static class wire_5  extends bus{
        wire_5() {
            super(4,0);
        }
    }

    static class wire_4  extends bus{
        wire_4() {
            super(4,0);
        }
    }

   static  class clk extends bus {
        public clk(int hi, int lo) {
            super(hi, lo);
        }
    }


    public static class led extends bus{
        public led(int hi, int lo) {
            super(hi, lo);
        }
    }
    public static class btn extends bus{
        public btn(int hi, int lo) {
            super(hi, lo);
        }
    }
    public static class btn2 extends bus{
        public btn2(int hi, int lo) {
            super(hi, lo);
        }
    }

    public static btn btn(){return new btn(1,0);}
    public static btn2 btn2(){return new btn2(2,0);}

    static <T> void onPosEdge(T signal, Consumer<T> consumer) {

    }

}