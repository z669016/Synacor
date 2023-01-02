package com.putoet.game;

import lombok.SneakyThrows;

import java.io.*;

public class InputOutput {
    private final InputStream in;
    private final OutputStream out;

    public InputOutput(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }


    @SneakyThrows
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
