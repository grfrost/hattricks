package life;

import hat.Accelerator;
import hat.backend.Backend;
import io.github.robertograham.rleparser.RleParser;
import io.github.robertograham.rleparser.domain.PatternData;

import java.lang.invoke.MethodHandles;

public class Life {
    public static void main(String[] args)  {
        boolean headless = Boolean.getBoolean("headless") ||( args.length>0 && args[0].equals("--headless"));

        Accelerator accelerator = new Accelerator(MethodHandles.lookup(),
              //  new JavaSequentialBackend()
              //  new JavaMultiThreadedBackend()
                Backend.FIRST
        );

        PatternData patternData = RleParser.readPatternData(
                Life.class.getClassLoader().getResourceAsStream("orig.rle")
        );
        LifeData lifeData = LifeData.create(accelerator,
                (((patternData.getMetaData().getWidth()+2)/16)+1)*16,
                (((patternData.getMetaData().getHeight()+2)/16)+1)*16
        );
       patternData.getLiveCells().getCoordinates().stream().forEach(c->
           lifeData.array((1+c.getX())+ (1+c.getY())* lifeData.width(), (byte)0xff)
        );

       LifeSupport lifeSupport = LifeSupport.create(accelerator, lifeData);
        final Viewer viewer = new Viewer( "Life", lifeSupport, lifeData);
        viewer.update();
        viewer.waitForDoorbell();
        boolean externalLoop = true;
        if (externalLoop){
            final long startMillis = System.currentTimeMillis();
            while (true) {
                accelerator.compute(cc -> LifeCompute.compute(cc, lifeSupport, lifeData));
                 lifeSupport.next();
                if (lifeSupport.generation()%50==0) {
                    viewer.update();
                    LifeCompute.report(lifeSupport, System.currentTimeMillis() - startMillis);
                }
            }
        }else {
            accelerator.compute(cc -> LifeCompute.loopCompute(cc, lifeSupport, lifeData, viewer));
        }
    }

}
