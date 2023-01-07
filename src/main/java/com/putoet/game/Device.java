package com.putoet.game;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Device implements Runnable {
    public static final DeviceDebugger DEFAULT_DEBUGGER = new DeviceDebugger() {};

    private final Memory memory;
    private final Registers registers;
    private final Keyboard in;
    private final Crt out;
    private final Register ip = new Register();
    private final Stack<Integer> stack = new Stack<>();
    private final AtomicReference<Instruction> currentInstruction = new AtomicReference<>(null);

    private DeviceDebugger debugger = DEFAULT_DEBUGGER;

    private boolean running = false;

    public Device(Registers registers, Memory memory, Keyboard in, Crt out) {
        this.registers = registers;
        this.memory = memory;
        this.in = in;
        this.out = out;
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

    public Keyboard in() {
        return in;
    }
    public Crt out() {
        return out;
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

    public boolean isConnected() {
        return debugger != DEFAULT_DEBUGGER;
    }

    public Instruction currentInstruction() {
        return currentInstruction.get();
    }

    @Override
    public void run() {
        running = true;
        final Interpreter interpreter = new Interpreter(registers, memory, stack, in, out);

        currentInstruction.set(interpreter.next(ip()));
        while (currentInstruction.get().opcode() != Opcode.HALT && running) {
            debugger.debug(ip, currentInstruction.get());
            currentInstruction.get().run();

            currentInstruction.set(interpreter.next(ip()));
        }
    }

    public void exit() {
        running = false;
    }

    public boolean exiting() {
        return !running;
    }
}
