package com.yk.common.propagator;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class TaskPropagationDriver<T> implements TaskPropagator<T> {

    private final ConcurrentMap<Object, GenericTaskPropagator<T>> threadHolder = new ConcurrentHashMap<>();

    private final Function<T, Object> idResolver;
    private final Function<T, Runnable> taskResolver;

    private TaskPropagationDriver(Function<T, Object> idResolver, Function<T, Runnable> taskResolver) {
        this.idResolver = idResolver;
        this.taskResolver = taskResolver;
    }

    @Contract(value = "_ -> new", pure = true)
    public static <S> @NonNull TaskPropagationDriverBuilder<S> forType(Class<S> driverType) {
        return new TaskPropagationDriverBuilder<>(driverType);
    }

    @Override
    public void propagete(T t) {
        var taskPropagator = threadHolder.computeIfAbsent(idResolver.apply(t),
                (object) -> new GenericTaskPropagator<>(taskResolver, () -> threadHolder.remove(idResolver.apply(t))));
        taskPropagator.propagete(t);
    }

    @Override
    public void propagete(T t, Function<T, Boolean> newCondition) {
        var taskPropagator = threadHolder.computeIfAbsent(idResolver.apply(t),
                (object) -> new GenericTaskPropagator<>(taskResolver, () -> threadHolder.remove(idResolver.apply(t))));
        taskPropagator.setCondition(newCondition);
        taskPropagator.propagete(t);
    }

    public static class TaskPropagationDriverBuilder<T> {
        private final Class<T> tClass;
        private Function<T, Object> idResolver;
        private Function<T, Runnable> taskResolver;

        private TaskPropagationDriverBuilder(@NonNull Class<T> tClass) {
            this.tClass = tClass;
        }

        public TaskPropagationDriverBuilder<T> byIdResolver(@NonNull Function<T, Object> idResolver) {
            this.idResolver = idResolver;
            return this;
        }

        public TaskPropagationDriverBuilder<T> withTaskResolver(Function<T, Runnable> taskResolver) {
            this.taskResolver = taskResolver;
            return this;
        }

        public TaskPropagationDriver<T> build() {
            if (taskResolver == null) {
                throw new RuntimeException("Contract violation: Task resolver has to be provided");
            }
            return new TaskPropagationDriver<>(idResolver, taskResolver);
        }

    }
}
