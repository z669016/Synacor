package com.putoet.device;

public interface Instruction extends Runnable {
    Opcode opcode();
    int size();
    void execute();

    String dump(boolean smart);
}
