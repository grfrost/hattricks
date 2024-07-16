package life;

import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.backend.Backend;
import hat.backend.JavaMultiThreadedBackend;
import hat.backend.JavaSequentialBackend;
import hat.buffer.Buffer;
import hat.buffer.BufferAllocator;
import hat.ifacemapper.Schema;
import io.github.robertograham.rleparser.RleParser;
import io.github.robertograham.rleparser.domain.PatternData;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.runtime.CodeReflection;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Life {
    public interface LifeData{
        int width();

        int height();

        int generation();
        void next();
    }
    public interface LifeDataInt extends Buffer, LifeData {
        int generation();
        void generation(int generation);

        int width();
        void width(int width);
        int height();
        void height(int height);
        int length();
        void length(int length);
        int array(long idx);
        void array(long idx, int b);

        Schema<LifeDataInt> schema = Schema.of(LifeDataInt.class, s32Array->s32Array
                .fields("generation","width","height")
                .arrayLen("length").array("array"));

        static LifeDataInt create(MethodHandles.Lookup lookup, BufferAllocator bufferAllocator, int width, int height){
            var instance = schema.allocate(lookup,bufferAllocator, width*height*2);
            instance.generation(0);
            instance.length(width*height*2);
            instance.width(width);
            instance.height(height);
            return instance;
        }
        static LifeDataInt create(Accelerator accelerator, int width, int height){
            return create(accelerator.lookup, accelerator,  width,  height);
        }

        default void next(){
            int generation=generation();
            generation(generation+1);
        }

        ValueLayout  valueLayout = JAVA_INT;
        long headerOffset =JAVA_INT.byteOffset()*6;
        default LifeDataInt copySliceTo(int[] ints) {

            long offset = headerOffset + ((long) generation() % 2) *width()*height()*valueLayout.byteOffset();
            MemorySegment.copy(Buffer.getMemorySegment(this), valueLayout, headerOffset, ints, 0, length()/2);
            return this;
        }
    }

    public interface LifeDataByte extends Buffer, LifeData {
        int generation();
        void generation(int generation);

        int width();
        void width(int width);
        int height();
        void height(int height);
        int length();
        void length(int length);
        byte array(long idx);
        void array(long idx, byte b);

        Schema<LifeDataByte> schema = Schema.of(LifeDataByte.class, lifeData->lifeData
                .fields("generation","width","height")
                .arrayLen("length").array("array"));

        static LifeDataByte create(MethodHandles.Lookup lookup, BufferAllocator bufferAllocator, int width, int height){
            var instance = schema.allocate(lookup,bufferAllocator, width*height*2);
            instance.generation(0);
            instance.length(width*height*2);
            instance.width(width);
            instance.height(height);
            return instance;
        }
        static LifeDataByte create(Accelerator accelerator, int width, int height){
            return create(accelerator.lookup, accelerator,  width,  height);
        }

        default void next(){
            int generation=generation();
            generation(generation+1);
        }

        ValueLayout  valueLayout = JAVA_BYTE;
        long headerOffset =JAVA_INT.byteOffset()*6;
        default LifeDataByte copySliceTo(byte[] bytes) {

            long offset = headerOffset + ((long) generation() % 2) *width()*height()*valueLayout.byteOffset();
            MemorySegment.copy(Buffer.getMemorySegment(this), valueLayout, headerOffset, bytes, 0, length()/2);
            return this;
        }
    }


    public static class LifeComputeInt {


        @CodeReflection
        public static void life(KernelContext kc, LifeDataInt lifeData) {
            if (kc.x < kc.maxX) {
                int gid = kc.x;
                int ALIVE = 0xffffffff;
                int DEAD = 0x00000000;
                int generation = lifeData.generation();
                int width = lifeData.width();
                int height = lifeData.height();
                int from = gid + (generation % 2) * width*height;
                int to = gid + ((generation + 1) % 2) * width*height;
                int x = gid % width;
                int y = gid / width;

                int centerValue = lifeData.array(from);
                int newCenterValue = centerValue;
                if (((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1)))) {
                    // This pixel is on the border of the view, just keep existing value
                    lifeData.array(to, newCenterValue);
                } else {
                    int north = from - width;
                    int south = from + width;
                    int east = -1;
                    int west = 1;
                    int neighbors =
                            ((lifeData.array(north) & 1))             // north
                                    + ((lifeData.array(from + east) & 1))    // east
                                    + ((lifeData.array(from + west) & 1))    // west
                                    + ((lifeData.array(south) & 1))            // south
                                    + ((lifeData.array(north + east) & 1))   // northeast
                                    + ((lifeData.array(south + east) & 1))   // southeast
                                    + ((lifeData.array(north + west) & 1))   // northwest
                                    + ((lifeData.array(south + west) & 1));  // southwest
                    if ((neighbors == 3) || ((neighbors == 2) && (centerValue == ALIVE))) {
                        newCenterValue = ALIVE;
                    } else {
                        newCenterValue = DEAD;
                    }
                    lifeData.array(to, newCenterValue);
                }
            }
        }

        @CodeReflection
        static public void compute(final ComputeContext computeContext, LifeDataInt lifeData) {
            computeContext.dispatchKernel(lifeData.length() / 2, kc -> LifeComputeInt.life(kc, lifeData));
        }
    }
    public static class LifeComputeByte {
@CodeReflection
        public static void life(KernelContext kc, LifeDataByte lifeDataByte) {
            if (kc.x < kc.maxX) {
                int gid = kc.x;
                byte ALIVE = (byte)0xff;
                byte DEAD = 0x00;
                int generation = lifeDataByte.generation();
                int width = lifeDataByte.width();
                int height = lifeDataByte.height();
                int from = gid + (generation%2) * width*height;
                int to = gid + ((generation+1)%2) * width*height;
                int x = gid % width;
                int y = gid / width;

                byte centerValue= lifeDataByte.array(from);
                byte newCenterValue = centerValue;
                if (((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1)))) {
                    // This pixel is on the border of the view, just keep existing value
                    lifeDataByte.array(to,newCenterValue);
                } else {
                    int north = from-width;
                    int south = from+width;
                    int east = -1;
                    int west = 1;
                    int neighbors=
                            ((lifeDataByte.array(north)&1))                     // north
                                    +((lifeDataByte.array(from+east)&1))    // east
                                    +((lifeDataByte.array(from+west)&1))    // west
                                    +((lifeDataByte.array(south)&1))            // south
                                    +((lifeDataByte.array(north+east)&1))   // northeast
                                    +((lifeDataByte.array(south+east)&1))   // southeast
                                    +((lifeDataByte.array(north+west)&1))   // northwest
                                    +((lifeDataByte.array(south+west)&1));  // southwest
                    if ((neighbors == 3) || ((neighbors == 2) && (centerValue == ALIVE))) {
                        newCenterValue = ALIVE;
                    } else {
                        newCenterValue = DEAD;
                    }
                    lifeDataByte.array(to, newCenterValue);
                }
            }
        }

        @CodeReflection
        static public void compute(final ComputeContext computeContext, LifeDataByte lifeDataByte) {
            computeContext.dispatchKernel(lifeDataByte.length()/2, kc -> LifeComputeByte.life(kc, lifeDataByte));
        }

    }

    public static void main(String[] args)  {
        boolean headless = Boolean.getBoolean("headless") ||( args.length>0 && args[0].equals("--headless"));

        Accelerator accelerator = new Accelerator(MethodHandles.lookup(),
              //  new JavaSequentialBackend()
              //  new JavaMultiThreadedBackend()
                Backend.FIRST
        );

        PatternData patternData = RleParser.readPatternData(Life.class.getClassLoader().getResourceAsStream("clock.rle"));

        var h = (((patternData.getMetaData().getHeight()+2)/16)+1)*16;
        var w = (((patternData.getMetaData().getWidth()+2)/16)+1)*16;

        LifeDataInt lifeDataInt = LifeDataInt.create(accelerator,w,h);
        LifeDataByte lifeDataByte = LifeDataByte.create(accelerator,w,h);



       var lc =  patternData.getLiveCells();
       lc.getCoordinates().stream().forEach(c->{
           int pos = (1+c.getX())+ (1+c.getY())*w;
            lifeDataInt.array(pos, 0xffffffff);
            lifeDataByte.array(pos, (byte)0xff);
        });
        final Viewer view = new Viewer( "Life", lifeDataByte);
        view.viewer.repaint();
        view.waitForDoorbell();

        final long startMillis = System.currentTimeMillis();
        while (true) {

          //  accelerator.compute(cc-> LifeComputeInt.compute(cc, lifeDataInt));lifeDataInt.next();
            accelerator.compute(cc-> LifeComputeByte.compute(cc, lifeDataByte));lifeDataByte.next();
          //  if (lifeDataByte.generation()%50==0) {
                view.viewer.repaint();
                final long elapsedMillis = System.currentTimeMillis() - startMillis;
            System.out.println("Generation = " + lifeDataByte.generation() + " generations/sec = " + ((lifeDataByte.generation() * 1000f) / elapsedMillis));

            //System.out.println("Generation = " + lifeDataInt.generation() + " generations/sec = " + ((lifeDataInt.generation() * 1000f) / elapsedMillis));
          //  }
        }

    }

}
