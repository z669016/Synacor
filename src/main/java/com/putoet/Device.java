package com.putoet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Device implements Runnable {

    private final Memory memory;
    private final Registers registers;
    private final InputOutput io;
    private final Register ip = new Register();
    private final Stack<Integer> stack = new Stack<>();

    public Device(Registers registers, Memory memory, InputOutput io) {
        this.registers = registers;
        this.memory = memory;
        this.io = io;
    }

    public void load(String binaryFileName) {
        int offset = 0;
        try (InputStream inputStream = new FileInputStream(binaryFileName)) {
            int byte1Read, byte2Read;

            while ((byte1Read = inputStream.read()) != -1 && (byte2Read = inputStream.read()) != -1) {
                memory.write(offset++, Memory.bytesToInt((byte) byte1Read, (byte) byte2Read));
            }
        } catch (IOException exc) {
            throw new IllegalArgumentException("Failed to load file " + binaryFileName, exc);
        }
    }

    public void load(int... program) {
        for (int offset = 0; offset < program.length; offset++)
            memory.write(offset, program[offset]);
    }

    public void load(List<Integer> program) {
        for (int offset = 0; offset < program.size(); offset++)
            memory.write(offset, program.get(offset));
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

    @Override
    public void run() {
        final Interpreter interpreter = new Interpreter(registers, memory, stack, io);

        var instruction = interpreter.next(ip());
        while (instruction.opcode() != Opcode.HALT) {
            instruction.run();
            instruction = interpreter.next(ip());
        }
    }

    public void reset() {
        registers.clear();
        ip.accept(0);

        while (!stack.isEmpty())
            stack.pop();
    }

    public List<String> dump() {
        final List<String> dump = new ArrayList<>();
        final Interpreter interpreter = new Interpreter(registers, memory, stack, io);

        final Register ip = new Register();
        while (ip.get() <= memory.lastAddressUsed()) {
            var instruction = interpreter.next(ip);
            dump.add(instruction.toString());
            ip.accept(ip.get() + instruction.size());
        }

        return dump;
    }

    public static void main(String[] args) {
        final Registers registers = new Registers();
        final Memory memory = new Memory();
        final InputOutput io = new InputOutput(System.in, System.out);
        final Device device = new Device(registers, memory, io);

        device.load("/Users/renevanputten/Downloads/synacor-challenge/challenge.bin");
        device.run();
//        device.dump().forEach(System.out::println);
    }
}
