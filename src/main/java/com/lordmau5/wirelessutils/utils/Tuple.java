package com.lordmau5.wirelessutils.utils;

import java.util.Objects;

public class Tuple<A, B> {

    public final A first;
    public final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public Tuple(net.minecraft.util.Tuple<A, B> other) {
        this.first = other.getFirst();
        this.second = other.getSecond();
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(first, tuple.first) &&
                Objects.equals(second, tuple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
