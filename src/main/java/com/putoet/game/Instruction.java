package com.putoet.game;

public interface Instruction extends Runnable {
    Opcode opcode();
    int size();
    void execute();
}
