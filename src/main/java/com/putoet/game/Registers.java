/**
 * Registers class
 * Holds a set of 8 registers, with an ID ranging form 32768 to 32775 which corresponds to the names
 * 'a' through 'h' (although the individual registers ar unaware of any name).
 */
package com.putoet.game;

public class Registers {
    public static final int ARCH_MAX_VALUE = 32768;
    public static final int REGISTERS = 8;

    private final Register[] registers = new Register[REGISTERS];

    /**
     * Constructor, creates 8 new Register instances
     */
    public Registers() {
        for (int i = 0; i < REGISTERS; i++)
            registers[i] = new Register();
    }

    /**
     * Reset all register to value 0
     */
    public void clear() {
        for (Register register : registers) register.accept(0);
    }

    /**
     * Convenience method to enforce an ID is valid (is in the valid range of 32768 to 32775). If not, this method
     * throws an IllegalStateException.
     *
     * @param id int
     */
    public static void checkId(int id) {
        if (!isRegister(id))
            throw new IllegalStateException("Invalid register id: " + id);
    }

    /**
     * Convenience method which checks if an id is a valid register id in the range of 32768 to 32775.
     *
     * @param id int
     * @return true if the value is in the correct range
     */
    public static boolean isRegister(int id) {
        return id >= ARCH_MAX_VALUE && id < ARCH_MAX_VALUE + REGISTERS;
    }

    /**
     * Get the value of the register specified by the ID. Uses checkId() to enforce a valid ID.
     *
     * @param id int
     * @return register value
     */
    public int get(int id) {
        checkId(id);
        return registers[id - ARCH_MAX_VALUE].get();
    }

    /**
     * Set the value of the register specified by the ID. Uses checkId() to enforce a valid ID.
     *
     * @param id int
     * @param value int
     */
    public void set(int id, int value) {
        checkId(id);
        registers[id - ARCH_MAX_VALUE].accept(value);
    }

    /**
     * Returns the name the register specified by the ID between angle brackets. Uses checkId() to enforce a valid ID.
     * @param id int
     * @return String (<a> ... <ha>)
     */
    public static String asLetter(int id) {
        checkId(id);
        return "<" + (char) ('a' + (id - ARCH_MAX_VALUE)) + ">";
    }

    /**
     * String representation of the component, with the register names and the individual values)
     *
     * @return String
     */
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
