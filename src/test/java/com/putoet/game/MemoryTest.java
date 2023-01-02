package com.putoet.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {
    private Memory memory;

    @BeforeEach
    void setup() {
        memory = new Memory();
    }

    @Test
    void size() {
        assertEquals(Registers.ARCH_MAX_VALUE, memory.size());
    }

    @Test
    void lastAddressUsed() {
        assertEquals(0, memory.lastAddressUsed());
        memory.read(13);
        assertEquals(13, memory.lastAddressUsed());
        memory.write(31, 13);
        assertEquals(31, memory.lastAddressUsed());
    }

    @Test
    void read() {
        assertEquals(0, memory.read(13));
        memory.write(13, 31);
        assertEquals(31, memory.read(13));
    }
}