package life;

import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.backend.Backend;
import hat.buffer.Buffer;
import hat.buffer.BufferAllocator;
import hat.ifacemapper.Schema;
import life.rle.RleParser;
import life.rle.domain.PatternData;
import java.io.File;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.runtime.CodeReflection;
import java.net.URISyntaxException;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Life {

    public interface S08Array extends Buffer {
        int from();
        void from(int from);
        int to();
        void to(int to);
        int width();
        void width(int width);
        int height();
        void height(int height);
        int length();
        void length(int length);
        byte array(long idx);
        void array(long idx, byte b);

        Schema<S08Array> schema = Schema.of(S08Array.class, s32Array->s32Array
                .fields("from","to","width","height")
                .arrayLen("length").array("array"));

        static S08Array create(MethodHandles.Lookup lookup, BufferAllocator bufferAllocator, int width, int height){
            var instance = schema.allocate(lookup,bufferAllocator, width*height*2);
            instance.length(width*height*2);
            instance.from(0);
            instance.to(width*height);
            instance.width(width);
            instance.height(height);
            return instance;
        }
        static S08Array create(Accelerator accelerator,int width, int height){
            return create(accelerator.lookup, accelerator,  width,  height);
        }

        default void swap(){
            int from = from();
            int to = to();
            from(to);
            to(from);
        }

        ValueLayout  valueLayout = JAVA_BYTE;
        long headerOffset =JAVA_INT.byteOffset()*4;
        default S08Array copyfrom(byte[] bytes) {
            MemorySegment.copy(bytes, 0, Buffer.getMemorySegment(this), valueLayout, headerOffset, length());
            return this;
        }

        default S08Array copyTo(byte[] bytes) {
            MemorySegment.copy(Buffer.getMemorySegment(this), valueLayout, headerOffset, bytes, 0, length());
            return this;
        }
    }



    public static class LifeCompute{
        @CodeReflection
        public static void life(KernelContext kc, S08Array s08Array) {
            if (kc.x < kc.maxX) {
                int gid = kc.x;
                byte ALIVE = (byte)0xff;
                byte DEAD = (byte)0x00;
                int width = s08Array.width();
                int height = s08Array.height();
                int to = gid + s08Array.to();
                int from = gid + s08Array.from();
                int x = gid % width;
                int y = gid / width;

                byte center=s08Array.array(from);
                if (((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1)))) {
                    // This pixel is on the border of the view, just keep existing value
                    s08Array.array(to,center);
                } else {
                    int north = from-width;
                    int south = from+width;
                    int neighbors=
                            (s08Array.array(north)&1)   // north
                            +(s08Array.array(from-1)&1)    // east
                            +(s08Array.array(from+1)&1)    // west
                            +(s08Array.array(south)&1)   // south
                            +(s08Array.array(north-1)&1)  // northeast
                            +(s08Array.array(south-1)&1)   // southeast
                            +(s08Array.array(north+1)&1)   // northwest
                            +(s08Array.array(south+1)&1);  // southwest

                    byte newCenter = center;
                    if ((neighbors == 3) || ((neighbors == 2) && (center == ALIVE))) {
                       newCenter = ALIVE;
                    } else {
                        newCenter = DEAD;
                    }
                    s08Array.array(to, newCenter);

                }
            }
        }


        @CodeReflection
        static public void compute(final ComputeContext computeContext, S08Array s08Array) {
            computeContext.dispatchKernel(s08Array.length(), kc -> LifeCompute.life(kc, s08Array));
        }

    }

    public static void main(String[] args) throws URISyntaxException {
        boolean headless = Boolean.getBoolean("headless") ||( args.length>0 && args[0].equals("--headless"));

        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);

        PatternData patternData = RleParser.readPatternData(
                new File("/Users/grfrost/github/babylon-grfrost-fork/hat/hattricks/life/src/main/resources/clock.rle")
                        .toURI());

        var h = ((patternData.getMetaData().getHeight()/16)+1)*16;
        var w = ((patternData.getMetaData().getWidth()/16)+1)*16;

        S08Array s32Array = S08Array.create(accelerator,w,h);

        final Viewer view = new Viewer( "Life", s32Array, w, h, .1);

       var lc =  patternData.getLiveCells();
       lc.getCoordinates().stream().forEach(c->{
            int ybyWidth = c.getY()* view.width;
            int pos = c.getX() +ybyWidth;
            s32Array.array(pos, (byte)0xff);
            s32Array.array(pos+ybyWidth, (byte)0xff);
        });
        view.syncWithRGB();

        view.waitForDoorbell();

int generation = 0;
        final long startMillis = System.currentTimeMillis();
        while (true) {

            accelerator.compute(cc-> LifeCompute.compute(cc, s32Array));
            view.syncWithRGB();

            s32Array.swap();
            if (generation%20==0) {
                final long elapsedMillis = System.currentTimeMillis() - startMillis;
                System.out.println("Generation = " + generation + " generations/sec = " + ((generation * 1000L) / elapsedMillis));
            }
        }

    }

}
