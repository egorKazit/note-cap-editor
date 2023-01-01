package com.yk.common.propagator;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Log4j2
class GenericTaskPropagator<T> {

    private final ConcurrentLinkedQueue<T> objectHolder = new ConcurrentLinkedQueue<>();

    @Setter
    private Function<T, Boolean> condition;

    GenericTaskPropagator(Function<T, Runnable> taskResolver, Runnable taskOnStop) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            T object;
            do {
                object = objectHolder.poll();
                if (object == null) continue;
                log.atInfo().log("start task processing for object " + object);
                if (condition != null && !condition.apply(object))
                    continue;
                taskResolver.apply(object).run();
                log.atInfo().log("end task task processing");
            } while (!objectHolder.isEmpty());
            taskOnStop.run();
        });
    }

    void propagete(@NotNull T t) {
        objectHolder.add(t);
    }

}
