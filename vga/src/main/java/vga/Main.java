package vga;

import jdk.incubator.code.CodeReflection;

import java.awt.Point;

public class Main {
static class Compute {
    @CodeReflection
    public static void top(byte[] vga, int x, int y, int width, int height, long frame) {
        if ((x > 24 && x < 50) && y == 24) {
            vga[y * width + x] = (byte) 0xff;
        } else {
            vga[y * width + x] = (byte) 0x00;
        }
    }
}
    public static void main(String[] _args) {
        VGAFrame vf = new VGAFrame("VGA", 680, 480);
        vf.setShader(Compute::top);
        for (Point point = vf.waitForPoint(1000 / 60); point != null; point = vf.waitForPoint(10)) {
            System.out.println("You pressed " + point);
        }
    }

}


