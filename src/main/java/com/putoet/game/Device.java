package com.putoet.game;

import java.io.*;
import java.util.*;

public class Device implements Runnable {
    private static final DeviceDebugger DEFAULT_DEBUGGER = new DeviceDebugger() {};

    private final Memory memory;
    private final Registers registers;
    private final InputOutput io;
    private final Register ip = new Register();
    private final Stack<Integer> stack = new Stack<>();

    private DeviceDebugger debugger = DEFAULT_DEBUGGER;

    private boolean running = false;

    public Device(Registers registers, Memory memory, InputOutput io) {
        this.registers = registers;
        this.memory = memory;
        this.io = io;
    }

    public void loadResource(String resourceName) {
        try (InputStream is = this.getClass().getResourceAsStream(resourceName)) {
            assert is != null;

            loadStream(is);
        } catch (IOException exc) {
            throw new IllegalArgumentException("Failed to load resource " + resourceName, exc);
        }
    }

    public void loadStream(InputStream is) throws IOException {
        assert is != null;

        final byte[] data = is.readAllBytes();
        for (int i = 0, offset = 0; i < data.length; i += 2) {
            final int word = Memory.bytesToInt(data[i], data[i+1]);
            memory.write(offset++, word);
        }
    }

    public void load(int... program) {
        for (int offset = 0; offset < program.length; offset++)
            memory.write(offset, program[offset]);
    }

    public Memory memory() {
        return memory;
    }

    public InputOutput io() {
        return io;
    }

    public Registers registers() {
        return registers;
    }

    public Register ip() {
        return ip;
    }

    public Stack<Integer> stack() {
        return stack;
    }

    public void setDebugger(DeviceDebugger debugger) {
        this.debugger = debugger;
    }

    public void resetDebugger() {
        this.debugger = DEFAULT_DEBUGGER;
    }

    @Override
    public void run() {
        running = true;
        final Interpreter interpreter = new Interpreter(registers, memory, stack, io);

        var instruction = interpreter.next(ip());
        while (instruction.opcode() != Opcode.HALT && running) {
            debugger.debug(ip, instruction);
            instruction.run();
            instruction = interpreter.next(ip());
        }
    }

    public void exit() {
        running = false;
    }

    public void reset() {
        registers.clear();
        ip.accept(0);

        while (!stack.isEmpty())
            stack.pop();
    }
}
