package life;

import hat.ComputeContext;
import hat.KernelContext;

import java.lang.runtime.CodeReflection;

public class LifeCompute {
    public final static byte ALIVE = (byte) 0xff;
    public final static byte DEAD = 0x00;
    @CodeReflection
    public static void life(KernelContext kc, LifeSupport lifeSupport, LifeData lifeData) {
        if (kc.x < kc.maxX) {
            int gid = kc.x;
            int width = lifeData.width();
            int height = lifeData.height();

            int from = gid + lifeSupport.from();
            int to = gid + lifeSupport.to();
            int x = gid % width;
            int y = gid / width;

            byte centerValue = lifeData.array(from);
            byte newCenterValue = centerValue;
            // If x,y is on the border just keep existing value
            if (((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1)))) {
                lifeData.array(to, newCenterValue);
            } else {
                int north = from - width;
                int south = from + width;
                int east = -1;
                int west = 1;
                int neighbors = ((lifeData.array(north) & 1))        // north
                        + ((lifeData.array(from + east) & 1))    // east
                        + ((lifeData.array(from + west) & 1))    // west
                        + ((lifeData.array(south) & 1))              // south
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

    static public  void report (LifeSupport lifeSupport, long elapsedMs) {
        int generation = lifeSupport.generation();
        System.out.println("Generation = " + generation
                + " generations/sec = " + ((generation * 1000f) / elapsedMs));
    }

    @CodeReflection
    static public void loopCompute(final ComputeContext computeContext, LifeSupport lifeSupport, LifeData lifeData, Viewer viewer) {
        final long startMillis = System.currentTimeMillis();
        while(true) {
           computeContext.dispatchKernel(lifeData.length()/2, kc -> LifeCompute.life(kc, lifeSupport, lifeData));
           lifeSupport.next();
           if (lifeSupport.generation()%50==0) {
              viewer.update();
              report(lifeSupport, System.currentTimeMillis() - startMillis);
           }
        }
    }

    @CodeReflection
    static public void compute(final ComputeContext computeContext, LifeSupport lifeSupport, LifeData lifeData) {
        computeContext.dispatchKernel(lifeData.length()/2, kc -> LifeCompute.life(kc, lifeSupport, lifeData));
   }

}
