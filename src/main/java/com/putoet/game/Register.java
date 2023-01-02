package com.putoet.game;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Register implements Supplier<Integer>, Consumer<Integer> {
    private int value;

    @Override
    public String toString() {
        return "Register{" +
               "value=" + value +
               '}';
    }

    @Override
    public void accept(Integer value) {
        this.value = value;
    }

    @Override
    public Integer get() {
        return value;
    }
}
