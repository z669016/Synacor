package com.putoet;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class DeviceTest {
    private Registers registers;
    private Memory memory;
    private InputOutput io;
    private Stack<Integer> stack;
    private Register ip;
    private InputStream in;
    private PrintStream out;
    private Device device;

    @BeforeEach
    void setup() {
        in = Mockito.mock(InputStream.class);
        out = Mockito.mock(PrintStream.class);

        memory = new Memory();
        registers = new Registers();
        io = new InputOutput(in, out);
        stack = new Stack<>();
        ip = new Register();

        device = new Device(registers, memory, io);
    }

    @SneakyThrows
    @Test
    void run() {
        device.load(9,32768,32769,4,19,32768, 0);
        registers.set(32769, 'a');

        device.run();
        assertEquals('a' + 4, registers.get(32768));
        verify(out).write('a' + 4);
    }

    @Test
    void dump() {
        device.load(9,32768,32769,4,19,32768, 0);
        assertEquals(List.of("ADD 32768 32769 4", "OUT 32768", "HALT"), device.dump());
    }
}