/**
 * Memory class
 * Implements the memory component of a device. Memory is a 64K array of bytes, of words (2-bytes) according to
 * little-endian (low byte first).
 * The read and write operations return/take unsigned integer values that get converted from/to little-endian
 * ordered bytes. While accessing memory, there is no checking of memory boundaries, so if an address points outside
 * or the memory, an ArrayIndexOutOfBounds will be thrown.
 */
package com.putoet.device;

public class Memory {
    private final byte[] memory = new byte[Registers.ARCH_MAX_VALUE * 2];
    private int lastAddressUsed = 0;

    /**
     * Memory size in words (size in bytes divided by 2)
     * @return int
     */
    public int size() {
        return memory.length / 2;
    }

    /**
     * value of the last word accessed up until now.
     *
     * @return int
     */
    public int lastAddressUsed() {
        return lastAddressUsed / 2;
    }

    /**
     * Write an integer value into memory at the specified address. The max integer value is 0xffff, the rest is
     * simply discarded. A java integer is 2 words, so the high word is ignored and removed before converting the
     * value into low-byte/high-byte.
     * This method updates the last-address-used value.
     *
     * @param address int
     * @param value int
     */
    public void write(int address, int value) {
        address *= 2;
        final byte[] bytes = intToBytes(value);
        memory[address] = bytes[0];
        memory[address + 1] = bytes[1];
        lastAddressUsed = Math.max(lastAddressUsed, address);
    }

    /**
     * Read a two byte integer value from memory at the given address, while updating the last-address-used value.
     *
     * @param address int
     * @return int
     */
    public int read(int address) {
        address *= 2;
        lastAddressUsed = Math.max(lastAddressUsed, address);
        return bytesToInt(memory[address] ,memory[address + 1]);
    }

    /**
     * Convenience method to convert to memory bytes into an integer
     * @param lowOrder byte
     * @param highOrder byte
     * @return int
     */
    public static int bytesToInt(byte lowOrder, byte highOrder) {
        return (highOrder & 0xff) << 8 | (lowOrder & 0xff);
    }

    /**
     * Convenience method to convert a java integer into a little-endian byte pair. The java high word bytes are
     * discarded before conversion, and the low word is considered an unsigned integer.
     *
     * @param word int
     * @return byte[] (low-byte/high-byte pair)
     */
    public static byte[] intToBytes(int word) {
        word &= 0xffff;
        return new byte[] {(byte) (word & 0xff),  (byte) (word >> 8)};
    }
}
