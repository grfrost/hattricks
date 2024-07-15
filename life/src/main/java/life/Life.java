package life;

import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.backend.Backend;
import hat.buffer.Buffer;
import hat.buffer.BufferAllocator;
import hat.ifacemapper.Schema;
import io.github.robertograham.rleparser.RleParser;
import io.github.robertograham.rleparser.domain.PatternData;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.runtime.CodeReflection;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Life {

    public interface LifeData extends Buffer {
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

        Schema<LifeData> schema = Schema.of(LifeData.class, s32Array->s32Array
                .fields("generation","width","height")
                .arrayLen("length").array("array"));

        static LifeData create(MethodHandles.Lookup lookup, BufferAllocator bufferAllocator, int width, int height){
            var instance = schema.allocate(lookup,bufferAllocator, width*height*2);
            instance.generation(0);
            instance.length(width*height*2);
            instance.width(width);
            instance.height(height);
            return instance;
        }
        static LifeData create(Accelerator accelerator, int width, int height){
            return create(accelerator.lookup, accelerator,  width,  height);
        }

        default void next(){
            int generation=generation();
            generation(generation+1);
        }

        ValueLayout  valueLayout = JAVA_INT;
        long headerOffset =JAVA_INT.byteOffset()*6;
        default LifeData copySliceTo(int[] ints) {

            long offset = headerOffset + ((long) generation() % 2) *width()*height()*valueLayout.byteOffset();
            MemorySegment.copy(Buffer.getMemorySegment(this), valueLayout, headerOffset, ints, 0, length()/2);
            return this;
        }
    }



    public static class LifeCompute{
        static final int ALIVE=0xffffffff;
        static final int DEAD=0x00000000;

        @CodeReflection
        public static void life(KernelContext kc, LifeData lifeData) {
            if (kc.x < kc.maxX) {
                int gid = kc.x;
                int ALIVE = 0xffffffff;
                int DEAD = 0x00000000;
                int generation = lifeData.generation();
                int width = lifeData.width();
                int height = lifeData.height();
                int from = gid + (generation%2) * kc.maxX;
                int to = gid + ((generation+1)%2) * kc.maxX;
                int x = gid % width;
                int y = gid / width;
               // int half = width*height;
               // int halfPlusGid = half+gid;
              //  int len = s08Array.length();
               // if (to>=len || from>=len){
                //    System.out.println("What");
               // }

                int centerValue= lifeData.array(from);
                int newCenterValue = centerValue;
                if (((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1)))) {
                    // This pixel is on the border of the view, just keep existing value
                    lifeData.array(to,newCenterValue);
                } else {
                    int north = from-width;
                    int south = from+width;
                    int east = -1;
                    int west = 1;
                    int neighbors=
                            ((lifeData.array(north)&1))             // north
                            +((lifeData.array(from+east)&1))    // east
                            +((lifeData.array(from+west)&1))    // west
                            +((lifeData.array(south)&1))            // south
                            +((lifeData.array(north+east)&1))   // northeast
                            +((lifeData.array(south+east)&1))   // southeast
                            +((lifeData.array(north+west)&1))   // northwest
                            +((lifeData.array(south+west)&1));  // southwest
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
        static public void compute(final ComputeContext computeContext, LifeData lifeData) {
            computeContext.dispatchKernel(lifeData.length()/2, kc -> LifeCompute.life(kc, lifeData));
        }

    }

    public static void main(String[] args)  {
        boolean headless = Boolean.getBoolean("headless") ||( args.length>0 && args[0].equals("--headless"));

        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), /*new JavaMultiThreadedBackend());//;*/Backend.FIRST);

        PatternData patternData = RleParser.readPatternData(Life.class.getClassLoader().getResourceAsStream("clock.rle"));

        var h = ((patternData.getMetaData().getHeight()/16)+1)*16;
        var w = ((patternData.getMetaData().getWidth()/16)+1)*16;

        LifeData lifeData = LifeData.create(accelerator,w,h);

        final Viewer view = new Viewer( "Life", lifeData);

       var lc =  patternData.getLiveCells();
       lc.getCoordinates().stream().forEach(c->{
            int ybyWidth = c.getY()* lifeData.width();
            int pos = c.getX() +ybyWidth;
            lifeData.array(pos, LifeCompute.ALIVE);
            lifeData.array(pos+ybyWidth, LifeCompute.ALIVE);
        });
        view.viewer.repaint();

        view.waitForDoorbell();

        final long startMillis = System.currentTimeMillis();
        while (true) {
            accelerator.compute(cc-> LifeCompute.compute(cc, lifeData));
            lifeData.next();
          //  if (lifeData.generation()%3==0) {
                view.viewer.repaint();
                final long elapsedMillis = System.currentTimeMillis() - startMillis;
                System.out.println("Generation = " + lifeData.generation() + " generations/sec = " + ((lifeData.generation() * 1000L) / elapsedMillis));
           // }
        }

    }

}
