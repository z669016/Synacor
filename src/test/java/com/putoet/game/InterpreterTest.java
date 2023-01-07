package com.putoet.game;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterpreterTest {
    private Registers registers;
    private Memory memory;
    private Stack<Integer> stack;
    private Register ip;
    private InputStream in;
    private OutputStream out;
    private Interpreter interpreter;

    @BeforeEach
    void setup() {
        in = Mockito.mock(InputStream.class);
        out = Mockito.mock(OutputStream.class);
        memory = mock(Memory.class);

        registers = new Registers();
        stack = new Stack<>();
        ip = new Register();

        interpreter = new Interpreter(registers, memory, stack, in, out);
    }

    @Test
    void halt() {
        when(memory.read(0)).thenReturn(Opcode.HALT.ordinal());
        final var instruction = interpreter.next(ip);

        assertEquals(Opcode.HALT, instruction.opcode());
    }

    @Test
    void set() {
        when(memory.read(0)).thenReturn(1);
        when(memory.read(1)).thenReturn(32770);
        when(memory.read(2)).thenReturn(9);
        when(memory.size()).thenReturn(3);

        registers.set(32770, 3);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.SET, instruction.opcode());
        instruction.run();

        assertEquals(9, registers.get(32770));
        assertEquals(3, ip.get());
    }

    @Test
    void push() {
        when(memory.read(0)).thenReturn(2);
        when(memory.read(1)).thenReturn(32768);
        when(memory.size()).thenReturn(2);

        registers.set(32768, 3);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.PUSH, instruction.opcode());
        instruction.run();

        assertEquals(3, registers.get(32768));
        assertEquals(2, ip.get());
        assertEquals(1, stack.size());
        assertEquals(3, stack.pop());
    }

    @Test
    void pop() {
        when(memory.read(0)).thenReturn(3);
        when(memory.read(1)).thenReturn(32768);
        when(memory.size()).thenReturn(2);
        stack.push(9);

        assertEquals(0, registers.get(32768));

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.POP, instruction.opcode());
        instruction.run();

        assertEquals(9, registers.get(32768));
        assertEquals(2, ip.get());
        assertEquals(0, stack.size());
    }

    @Test
    void eq() {
        when(memory.read(0)).thenReturn(4);
        when(memory.read(1)).thenReturn(32771);
        when(memory.read(2)).thenReturn(32768);
        when(memory.read(3)).thenReturn(7);
        when(memory.size()).thenReturn(4);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.EQ, instruction.opcode());
        instruction.run();

        assertEquals(0, registers.get(32771));
        assertEquals(4, ip.get());

        ip.accept(0);
        registers.set(32768, 7);
        instruction.run();

        assertEquals(1, registers.get(32771));
    }

    @Test
    void gt() {
        when(memory.read(0)).thenReturn(5);
        when(memory.read(1)).thenReturn(32771);
        when(memory.read(2)).thenReturn(32768);
        when(memory.read(3)).thenReturn(7);
        when(memory.size()).thenReturn(4);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.GT, instruction.opcode());
        instruction.run();

        assertEquals(0, registers.get(32771));
        assertEquals(4, ip.get());

        ip.accept(0);
        registers.set(32768, 8);
        instruction.run();

        assertEquals(1, registers.get(32771));
    }

    @Test
    void jmp() {
        when(memory.read(0)).thenReturn(6);
        when(memory.read(1)).thenReturn(10);
        when(memory.size()).thenReturn(2);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.JMP, instruction.opcode());
        instruction.run();

        assertEquals(10, ip.get());
    }

    @Test
    void jt() {
        when(memory.read(0)).thenReturn(7);
        when(memory.read(1)).thenReturn(0);
        when(memory.read(2)).thenReturn(9);
        when(memory.size()).thenReturn(3);

        var instruction = interpreter.next(ip);
        assertEquals(Opcode.JT, instruction.opcode());
        instruction.run();

        assertEquals(3, ip.get());

        when(memory.read(1)).thenReturn(1);
        ip.accept(0);
        instruction = interpreter.next(ip);
        instruction.run();

        assertEquals(9, ip.get());
    }

    @Test
    void jf() {
        when(memory.read(0)).thenReturn(8);
        when(memory.read(1)).thenReturn(0);
        when(memory.read(2)).thenReturn(9);
        when(memory.size()).thenReturn(3);

        var instruction = interpreter.next(ip);
        assertEquals(Opcode.JF, instruction.opcode());
        instruction.run();

        assertEquals(9, ip.get());

        when(memory.read(1)).thenReturn(1);
        ip.accept(0);
        instruction = interpreter.next(ip);
        instruction.run();

        assertEquals(3, ip.get());
    }

    @Test
    void add() {
        when(memory.read(0)).thenReturn(9);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(32758);
        when(memory.read(3)).thenReturn(15);
        when(memory.size()).thenReturn(4);

        registers.set(32768, 3);
        registers.set(32769, 1);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.ADD, instruction.opcode());
        instruction.run();

        assertEquals(5, registers.get(32768));
        assertEquals(4, ip.get());
    }

    @Test
    void mult() {
        when(memory.read(0)).thenReturn(10);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(9);
        when(memory.read(3)).thenReturn(7);
        when(memory.size()).thenReturn(4);

        registers.set(32768, 3);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.MULT, instruction.opcode());
        instruction.run();

        assertEquals(63, registers.get(32768));
        assertEquals(4, ip.get());
    }

    @Test
    void mod() {
        when(memory.read(0)).thenReturn(11);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(27);
        when(memory.read(3)).thenReturn(8);
        when(memory.size()).thenReturn(4);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.MOD, instruction.opcode());
        instruction.run();

        assertEquals(3, registers.get(32768));
        assertEquals(4, ip.get());
    }

    @Test
    void and() {
        when(memory.read(0)).thenReturn(12);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(14);
        when(memory.read(3)).thenReturn(13);
        when(memory.size()).thenReturn(4);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.AND, instruction.opcode());
        instruction.run();

        assertEquals(14 & 13, registers.get(32768));
        assertEquals(4, ip.get());
    }

    @Test
    void or() {
        when(memory.read(0)).thenReturn(13);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(14);
        when(memory.read(3)).thenReturn(13);
        when(memory.size()).thenReturn(4);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.OR, instruction.opcode());
        instruction.run();

        assertEquals(14 | 13, registers.get(32768));
        assertEquals(4, ip.get());
    }

    @Test
    void not() {
        when(memory.read(0)).thenReturn(14);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(0b0000_0000_0000_0001);
        when(memory.size()).thenReturn(3);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.NOT, instruction.opcode());
        instruction.run();

        assertEquals(0b0111_1111_1111_1110, registers.get(32768));
        assertEquals(3, ip.get());
    }

    @Test
    void rmem() {
        when(memory.read(0)).thenReturn(15);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(0);
        when(memory.size()).thenReturn(3);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.RMEM, instruction.opcode());
        instruction.run();

        assertEquals(15, registers.get(32768));
        assertEquals(3, ip.get());
    }

    @Test
    void wmem() {
        when(memory.read(0)).thenReturn(16);
        when(memory.read(1)).thenReturn(32768);
        when(memory.read(2)).thenReturn(1);
        when(memory.size()).thenReturn(3);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.WMEM, instruction.opcode());
        instruction.run();

        verify(memory, times(1)).write(0, 1);
        assertEquals(3, ip.get());
    }

    @Test
    void call() {
        when(memory.read(0)).thenReturn(17);
        when(memory.read(1)).thenReturn(13);
        when(memory.size()).thenReturn(2);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.CALL, instruction.opcode());
        instruction.run();

        assertEquals(1, stack.size());
        assertEquals(2, stack.pop());
        assertEquals(13, ip.get());
    }

    @Test
    void ret() {
        when(memory.read(0)).thenReturn(18);
        when(memory.size()).thenReturn(1);
        stack.push(13);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.RET, instruction.opcode());
        instruction.run();

        assertEquals(0, stack.size());
        assertEquals(13, ip.get());
    }

    @SneakyThrows
    @Test
    void out() {
        when(memory.read(0)).thenReturn(19);
        when(memory.read(1)).thenReturn((int) 'w');
        when(memory.size()).thenReturn(2);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.OUT, instruction.opcode());
        instruction.run();

        assertEquals(2, ip.get());

        verify(out).write('w');
    }

    @SneakyThrows
    @Test
    void in() {
        when(memory.read(0)).thenReturn(20);
        when(memory.read(1)).thenReturn(32768);
        when(in.read()).thenReturn((int) 's');
        when(memory.size()).thenReturn(2);

        final var instruction = interpreter.next(ip);
        assertEquals(Opcode.IN, instruction.opcode());
        instruction.run();

        assertEquals(2, ip.get());
        assertEquals('s', registers.get(32768));
    }
}