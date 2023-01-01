package com.putoet;

import java.io.*;

public class InputOutput {
    private final InputStream in;
    private final PrintStream out;

    public InputOutput(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }


    public void out(int c) {
        out.write((char) c);
    }

    public int in() {
        try {
            return in.read();
        } catch (IOException exc) {
            throw new IllegalStateException(exc);
        }
    }
}
