package com.putoet;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class InputOutputTest {
    private InputStream in;
    private PrintStream out;

    @BeforeEach
    void setup() {
        in = Mockito.mock(InputStream.class);
        out = Mockito.mock(PrintStream.class);
    }

    @SneakyThrows
    @Test
    void out() {
        final InputOutput io = new InputOutput(in, out);
        io.out('q');

        Mockito.verify(out).write('q');
    }

    @SneakyThrows
    @Test
    void in() {
        Mockito.when(in.read()).thenReturn((int) 'z');
        final InputOutput io = new InputOutput(in, out);
        assertEquals('z', io.in());
    }
}