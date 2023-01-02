package com.putoet.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistersTest {
    public static final int LOWER = 32768;
    public static final int UPPER = LOWER + 7;

    private Registers registers;

    @BeforeEach
    void setup() {
        registers = new Registers();
    }

    @Test
    void clear() {
        registers.set(LOWER, 11);
        registers.set(UPPER, 13);
        registers.clear();

        for (int i = LOWER; i <= UPPER; i++)
            assertEquals(0, registers.get(i));
    }

    @Test
    void checkId() {
        assertThrows(IllegalStateException.class, () -> registers.get(LOWER - 1));
        assertThrows(IllegalStateException.class, () -> registers.set(UPPER + 1, 7));
    }

    @Test
    void isRegister() {
        for (int i = LOWER; i <= UPPER; i++)
            assertTrue(Registers.isRegister(i));

        assertFalse(Registers.isRegister(LOWER - 1));
        assertFalse(Registers.isRegister(UPPER + 1));
    }

    @Test
    void get() {
        registers.set(LOWER, 11);
        assertEquals(11, registers.get(LOWER));
        registers.set(UPPER, 13);
        assertEquals(13, registers.get(UPPER));
    }

    @Test
    void asLetter() {
        assertEquals("a", Registers.asLetter(LOWER));
        assertEquals("h", Registers.asLetter(UPPER));
    }
}