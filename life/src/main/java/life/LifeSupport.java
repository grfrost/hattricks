package life;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.buffer.BufferAllocator;
import hat.ifacemapper.Schema;

import java.lang.invoke.MethodHandles;

public interface LifeSupport extends Buffer{
    int generation();

    void generation(int generation);

    int from();

    void from(int from);

    int to();

    void to(int height);


    Schema<LifeSupport> schema = Schema.of(LifeSupport.class, lifeSupport -> lifeSupport
            .fields("generation", "from", "to"));

    static LifeSupport create(MethodHandles.Lookup lookup, BufferAllocator bufferAllocator, LifeData lifeData) {
        var instance = schema.allocate(lookup, bufferAllocator);
        instance.generation(0);
        instance.to(lifeData.length()/2);
        instance.from(0);
        return instance;
    }

    static LifeSupport create(Accelerator accelerator, LifeData lifeData) {
        return create(accelerator.lookup, accelerator,lifeData);
    }

    default void next() {
        int generation = generation();
        generation(generation + 1);
        int from = from();
        from(to());
        to(from);
    }
}
