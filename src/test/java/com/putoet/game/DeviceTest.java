package com.putoet.game;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class DeviceTest {
    private Registers registers;
    private OutputStream out;
    private Device device;

    @BeforeEach
    void setup() {
        InputStream in = Mockito.mock(InputStream.class);
        out = Mockito.mock(OutputStream.class);

        Memory memory = new Memory();
        registers = new Registers();
        InputOutput io = new InputOutput(in, out);

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