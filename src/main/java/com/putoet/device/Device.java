/**
 * Device class
 * Implemented according to the Synacor architecture description (https://challenge.synacor.com/)
 * A Device is build from Memory, Registers, an In and Out device, and holds a Stack, an
 * Instruction Pointer (register), the Current Instruction, and a default DeviceDebugger (which
 * does nothing).
 * The Device implements Runnable, so it can run on a separate thread.
 * A device can load a program from a Resource, an InputStream, and a int[] array. Beware, according
 * to architecture, a program is build from 2-byte words with the low-byte first which is also known
 * as little endian.
 * While running, the set debugger is called after fetching but before execution of the next statement.
 * Fetching means that currentStatement is set to the next to-be-executed statement.
 * The default debugger is the empty implementation of the DeviceDebugger interface.
 */
package com.putoet.device;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Device implements Runnable, DebuggerSupport {
    private final Memory memory;
    private final Registers registers;
    private final Keyboard in;
    private final Crt out;
    private final Register ip = new Register();
    private final Stack<Integer> stack = new Stack<>();
    private final AtomicReference<Instruction> currentInstruction = new AtomicReference<>(null);

    private DeviceDebugger debugger = DebuggerSupport.DEFAULT_DEBUGGER;

    private boolean running = false;

    /**
     * Constructor
     *
     * @param registers Registers instance
     * @param memory Memory instance
     * @param in Keyboard instance, must implement InputStream
     * @param out Crt instance, must implement OutputStream
     */
    public Device(Registers registers, Memory memory, Keyboard in, Crt out) {
        this.registers = registers;
        this.memory = memory;
        this.in = in;
        this.out = out;
    }

    /**
     * Opens the resource as InputStream and calls load(InputStream). Any exception during loading
     * is wrapped in an IllegalArgumentException and rethrown.
     *
     * @param resourceName name of the resource ro be loaded
     */
    public void loadResource(String resourceName) {
        try (InputStream is = this.getClass().getResourceAsStream(resourceName)) {
            assert is != null;

            loadStream(is);
        } catch (IOException exc) {
            throw new IllegalArgumentException("Failed to load resource " + resourceName, exc);
        }
    }

    /**
     * Loads a program into memory, starting at address 0, from a little-endian byte stream. Errors occurring
     * during load, are not caught but passed on to the calling method.
     *
     * @param is InputStream
     * @throws IOException in case of any read error from the InputStream.
     */
    public void loadStream(InputStream is) throws IOException {
        assert is != null;

        final byte[] data = is.readAllBytes();
        for (int i = 0, offset = 0; i < data.length; i += 2) {
            final int word = Memory.bytesToInt(data[i], data[i+1]);
            memory.write(offset++, word);
        }
    }

    /**
     * Loads a program into memory starting at address 0 from an int array.
     *
     * @param program int[]
     */
    public void load(int... program) {
        for (int offset = 0; offset < program.length; offset++)
            memory.write(offset, program[offset]);
    }

    /**
     * Memory component of the device
     *
     * @return Memory
     */
    public Memory memory() {
        return memory;
    }

    /**
     * Input component of the device
     *
     * @return Keyboard
     */
    public Keyboard in() {
        return in;
    }

    /**
     * Output component of the device
     *
     * @return Crt
     */
    public Crt out() {
        return out;
    }

    /**
     * Registers component of the device
     *
     * @return Registers
     */
    public Registers registers() {
        return registers;
    }

    /**
     * Instruction Pointer Register of the device
     *
     * @return Register
     */
    public Register ip() {
        return ip;
    }

    /**
     * Stack component of the device
     *
     * @return Stack<Integer>
     */
    public Stack<Integer> stack() {
        return stack;
    }

    /**
     * Set debugger component of the device
     *
     * @param debugger DeviceDebugger
     */
    public void setDebugger(DeviceDebugger debugger) {
        this.debugger = debugger;
        this.in.setDebugger(debugger);
    }

    /**
     * Reset the debugger to the default DeviceDebugger implementation
     */
    public void resetDebugger() {
        this.debugger = DebuggerSupport.DEFAULT_DEBUGGER;
        this.in.resetDebugger();
    }

    /**
     * Checks if the debugger is NOT the default debugger.
     *
     * @return true if a custom debugger is set
     */
    public boolean isConnected() {
        return debugger != DebuggerSupport.DEFAULT_DEBUGGER;
    }

    /**
     * Current instruction, is the next fetched but not yet executed instruction.
     *
     * @return Instruction
     */
    public Instruction currentInstruction() {
        return currentInstruction.get();
    }

    /**
     * Running the device, creates an Interpreter for fetching instructions from memory, and executes the
     * instructions one by one, until a HALT instruction is encountered or the running flag was set to false.
     * After fetching of the next instruction and before executing it, the debugger is called with the
     * current IP and Instruction.
     */
    @Override
    public void run() {
        running = true;
        final Interpreter interpreter = new Interpreter(registers, memory, stack, in, out);

        currentInstruction.set(interpreter.next(ip()));
        while (currentInstruction.get().opcode() != Opcode.HALT && running) {
            var instruction = debugger.debug(ip, currentInstruction.get());
            instruction.run();

            currentInstruction.set(interpreter.next(ip()));
        }
    }

    /**
     * Set the running flag to false, to enforce the run() method to break out of its loop.
     */
    public void exit() {
        running = false;
    }

    /**
     * Reports the value of the running flag
     *
     * @return true if the value of the running flag was set to false
     */
    public boolean exiting() {
        return !running;
    }
}
