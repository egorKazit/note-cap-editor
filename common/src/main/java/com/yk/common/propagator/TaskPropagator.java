package com.yk.common.propagator;

import java.util.function.Function;

public interface TaskPropagator<T> {
    void propagete(T t);

    void propagete(T t, Function<T, Boolean> newCondition);
}
