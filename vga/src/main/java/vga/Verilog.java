package vga;

import java.util.function.Consumer;

public class Verilog {
    public static boolean isConnection(Class<?> clazz) {
        return Verilog.Connection.class.isAssignableFrom(clazz);
    }

    public static boolean isInOrOut(Class<?> inOutClass) {
        return Verilog.Input.isAssignable(inOutClass) || Verilog.Output.isAssignable(inOutClass);
    }

    public static void assign(WireMarker xcur, range range) {
    }
   /* static abstract class Connection {
        public int v;


    } */

    static abstract class Bus  {
        final public int hi;
        final public int lo;
          public int v;
        public Bus(int hi, int lo) {
            this.hi = hi;
            this.lo = lo;
        }
        public range range(int hi, int low) {
           return new range(){

           };// return v >> low & (v << (hi - low) - 1);
        }
    }

    interface Connection{}



    record Output<T extends Connection>(T wire) {
        public static boolean isAssignable(Class<?> test) {
            return Verilog.Output.class.isAssignableFrom(test);
        }
    }

    record Input<T extends Connection>(T wire) {
        public static boolean isAssignable(Class<?> test) {
            return Verilog.Input.class.isAssignableFrom(test);
        }
    }


    interface range{
        default void assign(range r) {}
    }

    interface range_1_0 extends range{
        static final int max =0;
        static final int min =0;
    }
    interface range_2_0 extends range{
        static final int max =1;
        static final int min =0;
    }
    interface range_3_0 extends range{
        static final int max =2;
        static final int min =0;
    }
    interface range_4_0 extends range{
        static final int max =3;
        static final int min =0;
    }
    interface range_5_0 extends range{
        static final int max =4;
        static final int min =0;
    }
    interface range_6_0 extends range{
        static final int max =5;
        static final int min =0;

       ;
    }
    interface range_7_0 extends range{
        static final int max =6;
        static final int min =0;
    }
    interface range_8_0 extends range{
        static final int max =7;
        static final int min =0;
    }
    interface range_9_0 extends range{
        static final int max =8;
        static final int min =0;
    }
    interface range_9_3 extends range{
        static final int max =9;
        static final int min =3;
    }
    interface range_10_0 extends range{
        static final int max =9;
        static final int min =0;
    }
    interface range_12_0 extends range{
        static final int max =11;
        static final int min =0;
    }
    interface range_16_0 extends range{
        static final int max =15;
        static final int min =0;
    }

    interface WireMarker extends Connection{

    }
    interface RegisterMarker extends Connection{

    }

    static class Wire extends Bus implements range_1_0,WireMarker{
        Wire() {
            super(range_1_0.max, range_1_0.min);
        }
    }

    static Wire wire_1() {
        return new Wire();
    }
    static class Wire_2 extends Bus implements range_2_0,WireMarker {
        Wire_2() {
            super(range_2_0.max, range_2_0.min);
        }
    }
    static Wire_2 wire_2() {
        return new Wire_2();
    }

    static class Wire_3 extends Bus implements range_3_0 ,WireMarker{
        Wire_3() {
            super(range_3_0.max, range_3_0.min);
        }
    }
    static Wire_3 wire_3() {
        return new Wire_3();
    }
    static class Wire_4 extends Bus implements range_2_0,WireMarker{
        Wire_4() {
            super(range_4_0.max, range_4_0.min);
        }
    }
    static Wire_4 wire_4() {
        return new Wire_4();
    }
    static class Wire_5 extends Bus implements range_5_0 ,WireMarker{
        Wire_5() {
            super(range_5_0.max, range_5_0.min);
        }
    }
    static Wire_5 wire_5() {
        return new Wire_5();
    }
    static class Wire_6 extends Bus implements range_6_0,WireMarker {
        Wire_6() {
            super(range_6_0.max, range_6_0.min);
        }
    }
    static Wire_6 wire_6() {
        return new Wire_6();
    }
    static class Wire_7 extends Bus implements range_7_0 ,WireMarker{
        Wire_7() {
            super(range_7_0.max, range_7_0.min);
        }
    }
    static Wire_7 wire_7() {
        return new Wire_7();
    }

    static class Wire_8 extends Bus implements range_8_0 ,WireMarker{
        Wire_8() {
            super(range_8_0.max, range_8_0.min);
        }
    }
    static Wire_8 wire_8() {
        return new Wire_8();
    }



    static class Wire_9 extends Bus implements range_9_0 ,WireMarker{
        Wire_9() {
            super(range_9_0.max, range_9_0.min);
        }
    }
    static Wire_9 wire_9() {
        return new Wire_9();
    }

    static class Wire_9_3 extends Bus implements range_9_3 ,WireMarker{
        Wire_9_3() {
            super(range_9_3.max, range_9_3.min);
        }
    }

    static Wire_9_3 wire_9_3() {
        return new Wire_9_3();
    }
    static class Wire_12 extends Bus implements range_12_0 ,WireMarker{
        Wire_12() {
            super(range_12_0.max, range_12_0.min);
        }
    }
    static Wire_12 wire_12() {
        return new Wire_12();
    }
    static class Wire_16 extends Bus implements range_16_0 ,WireMarker{
        Wire_16() {
            super(  range_16_0.max, range_16_0.min);
        }
    }
    static Wire_16 wire_16() {
        return new Wire_16();
    }

    static class Reg extends Bus implements range_1_0, RegisterMarker{

        Reg() {
            super(range_1_0.max,range_1_0.min);
        }
    }
    static Reg reg() {
        return new Reg();
    }
    static class Reg_4 extends Bus implements range_4_0, RegisterMarker{

        Reg_4() {
            super(range_4_0.max, range_4_0.min);
        }
    }


    static Reg_4 reg_4() {
        return new Reg_4();
    }



    static class Clk extends Bus implements range_1_0, WireMarker{
        public Clk() {
            super(range_1_0.max, range_1_0.min);
        }
    }

    public static class Led extends Bus implements range_1_0, WireMarker{
        public Led() {
            super(range_1_0.max, range_1_0.min);
        }
    }
    public static Led led() {
        return new Led();
    }
    public static class Btn extends Bus implements range_1_0 ,WireMarker{
        public Btn() {
            super(range_1_0.max, range_1_0.min);
        }
    }
    public static Btn btn() {
        return new Btn();
    }
    public static class Btn2 extends Bus implements range_2_0, RegisterMarker {
        public Btn2() {
            super(range_2_0.max, range_2_0.min);
        }
    }



    public static Btn2 btn2() {
        return new Btn2();
    }

    static <T> void onPosEdge(T signal, Consumer<T> consumer) {

    }

}