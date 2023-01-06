package com.putoet.game;

public interface DeviceDebugger {
    default void debug(Register ip, Instruction instruction) {}
}
