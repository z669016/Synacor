package com.putoet.game;

public class Registers {
    public static final int ARCH_MAX_VALUE = 32768;
    public static final int REGISTERS = 8;

    private final Register[] registers = new Register[REGISTERS];

    public Registers() {
        for (int i = 0; i < REGISTERS; i++)
            registers[i] = new Register();
    }

    public void clear() {
        for (Register register : registers) register.accept(0);
    }

    public static void checkId(int id) {
        if (!isRegister(id))
            throw new IllegalStateException("Invalid register id: " + id);
    }

    public static boolean isRegister(int id) {
        return id >= ARCH_MAX_VALUE && id < ARCH_MAX_VALUE + REGISTERS;
    }

    public int get(int id) {
        checkId(id);
        return registers[id - ARCH_MAX_VALUE].get();
    }

    public void set(int id, int value) {
        checkId(id);
        registers[id - ARCH_MAX_VALUE].accept(value);
    }

    public static String asLetter(int id) {
        checkId(id);
        return "<" + (char) ('a' + (id - ARCH_MAX_VALUE)) + ">";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Registers=['a'").append('=').append(registers[0]);
        for (int i = 1; i < registers.length; i++) {
            sb.append(", ").append((char) (i + 'a')).append('=').append(registers[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
