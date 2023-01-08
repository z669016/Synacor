package com.putoet.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    @Test
    void get() {
        final Register a = new Register();
        a.accept(32758 + 15);
        assertEquals(32773, a.get());
    }
}