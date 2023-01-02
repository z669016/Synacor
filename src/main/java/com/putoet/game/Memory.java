package com.putoet.game;

public class Memory {
    private final byte[] memory = new byte[Registers.ARCH_MAX_VALUE * 2];
    private int lastAddressUsed = 0;

    public int size() {
        return memory.length / 2;
    }

    public int lastAddressUsed() {
        return lastAddressUsed / 2;
    }

    public void write(int address, int value) {
        address *= 2;
        final byte[] bytes = intToBytes(value);
        memory[address] = bytes[0];
        memory[address + 1] = bytes[1];
        lastAddressUsed = Math.max(lastAddressUsed, address);
    }

    public int read(int address) {
        address *= 2;
        lastAddressUsed = Math.max(lastAddressUsed, address);
        return bytesToInt(memory[address] ,memory[address + 1]);
    }

    public static int bytesToInt(byte lowOrder, byte highOrder) {
        return (highOrder & 0xff) << 8 | (lowOrder & 0xff);
    }

    public static byte[] intToBytes(int word) {
        word &= 0xffff;
        return new byte[] {(byte) (word & 0xff),  (byte) (word >> 8)};
    }
}
