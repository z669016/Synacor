/**
 * Register class, implements Supplier<Integer>, and Consumer<Integer>
 * An register can get() and accept() values
 */
package com.putoet.device;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Register implements Supplier<Integer>, Consumer<Integer> {
    private int value;

    /**
     * Returns a string version of the register value.
     *
     * @return String
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Set the register value
     *
     * @param value int
     */
    @Override
    public void accept(Integer value) {
        this.value = value;
    }

    /**
     * Retrieve the register value
     *
     * @return int
     */
    @Override
    public Integer get() {
        return value;
    }
}
