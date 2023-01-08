package com.putoet.device;

/**
 * Interface for communication between a component and the debugger. Through the debug() method, a component
 * can inform the debugger on the next Instruction to be executed, or it can pass a command.
 * The debugger can return the Instruction or command String after which the componen can resume processing,
 * or the debugger can replace it with another instance to be executed.
 */
public interface DeviceDebugger {
    default Instruction debug(Register ip, Instruction instruction) {
        return instruction;
    }
    default String debug(String command) {
        return command;
    }
}
