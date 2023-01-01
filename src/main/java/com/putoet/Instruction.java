package com.putoet;

public interface Instruction extends Runnable {
    Opcode opcode();
    int size();
    void execute();
}
