package com.putoet.game;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class DeviceTest {
    private Registers registers;
    private Crt out;
    private Device device;

    @BeforeEach
    void setup() {
        Keyboard in = Mockito.mock(Keyboard.class);
        out = Mockito.mock(Crt.class);

        Memory memory = new Memory();
        registers = new Registers();

        device = new Device(registers, memory, in, out);
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
    void sub1531() {
//        SET <a> 26851
//        SET <b> 1531
//        ADD <c> 14984 15478

//        AND <c> <a> <b>
//        NOT <c> <c>
//        OR <a> <a> <b>
//        AND <a> <a> <c>




    }
}