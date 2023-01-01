package com.putoet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class InstructionBaseTest {
    private Register ip;
    private Memory memory;
    private Instruction instruction;

    @BeforeEach
    void setup() {
        ip = Mockito.mock(Register.class);
        memory = Mockito.mock(Memory.class);

        Mockito.when(ip.get()).thenReturn(0);
        Mockito.when(memory.size()).thenReturn(10);
        Mockito.when(memory.read(1)).thenReturn(3);
        Mockito.when(memory.read(2)).thenReturn(7);
        Mockito.when(memory.read(3)).thenReturn(19);
        instruction = new InstructionBase(Opcode.ADD, 3, ip, memory);
    }

    @Test
    void opcode() {
        assertEquals(Opcode.ADD, instruction.opcode());
    }

    @Test
    void execute() {
        instruction.execute();
        Mockito.verify(memory, Mockito.times(1)).read(1);
        Mockito.verify(memory, Mockito.times(1)).read(2);
        Mockito.verify(memory, Mockito.times(1)).read(3);
        Mockito.verify(ip, Mockito.times(4)).get();
        Mockito.verify(ip, Mockito.times(0)).accept(4);
    }

    @Test
    void size() {
        assertEquals(4, instruction.size());
    }

    @Test
    void run() {
        instruction.run();
        Mockito.verify(ip).accept(4);
    }

    @Test
    void testToString() {
        assertEquals("ADD 3 7 19", instruction.toString());
    }
}