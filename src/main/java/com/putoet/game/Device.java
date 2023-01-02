package com.putoet.game;

import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Device implements Runnable {

    private final Memory memory;
    private final Registers registers;
    private final InputOutput io;
    private final Register ip = new Register();
    private final Stack<Integer> stack = new Stack<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private boolean debug;
    private boolean step;

    public Device(Registers registers, Memory memory, InputOutput io) {
        this.registers = registers;
        this.memory = memory;
        this.io = io;
    }

    public void load(String binaryFileName) {
        try (InputStream is = new FileInputStream(binaryFileName)) {
            loadStream(is);
        } catch (IOException exc) {
            throw new IllegalArgumentException("Failed to load file " + binaryFileName, exc);
        }
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

    public void enableDebug() {
        this.debug = true;
    }

    public void disableDebug() {
        this.debug = true;
    }

    public void enableStep() {
        this.step = true;
        enableDebug();
    }

    public void disableStep() {
        this.step = false;
        disableDebug();
    }

    @Override
    public void run() {
        running.set(true);

        final Interpreter interpreter = new Interpreter(registers, memory, stack, io);

        var instruction = interpreter.next(ip());
        while (instruction.opcode() != Opcode.HALT && running.get()) {
            if (debug)
                io.err("%05d: %s%n".formatted(ip.get(), instruction));

            if (step) {
                final Scanner scan = new Scanner(System.in);
                scan.nextLine();
            }

            instruction.run();
            instruction = interpreter.next(ip());
        }
    }

    public void exit() {
        running.set(false);
    }

    public void reset() {
        registers.clear();
        ip.accept(0);

        while (!stack.isEmpty())
            stack.pop();
    }

    public List<String> dump(int startAddress) {
        final List<String> dump = new ArrayList<>();
        final Interpreter interpreter = new Interpreter(registers, memory, stack, io);

        final Register ip = new Register();
        ip.accept(startAddress);
        try {
            while (ip.get() <= memory.lastAddressUsed()) {
                var instruction = interpreter.next(ip);
                dump.add(instruction.toString());
                ip.accept(ip.get() + instruction.size());
            }
        } catch (RuntimeException exc) {
            dump.add("Failed decompilation at address " + ip.get() + "(" + exc.getMessage() + ")");
        }

        return dump;
    }

    public List<String> dump(int startAddress, Set<Opcode> exitCriteria) {
        final List<String> dump = new ArrayList<>();
        final Interpreter interpreter = new Interpreter(registers, memory, stack, io);

        final Register ip = new Register();
        ip.accept(startAddress);
        while (ip.get() <= memory.lastAddressUsed()) {
            var instruction = interpreter.next(ip);
            dump.add(instruction.toString());

            if (exitCriteria.contains(instruction.opcode()))
                break;

            ip.accept(ip.get() + instruction.size());
        }

        return dump;
    }

    public List<String> hexDump() {
        final List<String> dump = new ArrayList<>();

        int offset = 0;
        while (offset < memory.lastAddressUsed()) {
            dump.add(hexDump(offset));
            offset += 16;
        }

        return dump;
    }

    public String hexDump(int offset) {
        final StringBuilder sb = new StringBuilder();
        sb.append("%04x".formatted(offset)).append(" | ");
        for (int i = 0; i < 16; i++) {
            if (offset + i < memory.lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(memory().read(offset + i));
                sb.append("%02x %02x ".formatted(bytes[0], bytes[1]));
            } else {
                sb.append(".. .. ");
            }
        }

        sb.append("| ");

        for (int i = 0; i < 16; i++) {
            if (offset + i < memory.lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(memory().read(offset + i));
                sb.append(new String(bytes, StandardCharsets.UTF_8)
                                .replaceAll("\\P{Print}", "."))
                        .append(" ");
            } else {
                sb.append(".. ");
            }
        }

        sb.append("| ");
        for (int i = 0; i < 16; i++) {
            if (offset + i < memory.lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(memory().read(offset + i));
                sb.append(String.valueOf((char) bytes[0]).replaceAll("\\P{Print}", "."));
            } else {
                sb.append(".");
            }
        }

        sb.append(" |");

        return sb.toString();
    }

    @SneakyThrows
    public static void main(String[] args) {
        final Registers registers = new Registers();
        final Memory memory = new Memory();
        final InputOutput io = new InputOutput(System.in, System.out);
        final Device device = new Device(registers, memory, io);

        device.loadResource("/challenge.bin");
        device.dump(0).forEach(System.err::println);
        System.out.println();
        device.hexDump().forEach(line -> io.err(line + "\n"));
    }
}
