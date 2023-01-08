package com.putoet.game;

/**
 * Interface to support debugging
 */
public interface DebuggerSupport {
    DeviceDebugger DEFAULT_DEBUGGER = new DeviceDebugger() {};

    void setDebugger(DeviceDebugger debugger);
    void resetDebugger();
}
