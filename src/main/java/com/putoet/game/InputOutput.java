package com.putoet.game;

import lombok.SneakyThrows;

import java.io.*;

public class InputOutput {
    private final InputStream in;
    private final OutputStream out;
    private final OutputStream err;

    public InputOutput(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        this.err = System.err;
    }

    public InputOutput(InputStream in, OutputStream out, OutputStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
    }

    @SneakyThrows
    public void out(int c) {
        out.write((char) c);
    }

    @SneakyThrows
    public void err(int c) {
        err.write((char) c);
    }

    @SneakyThrows
    public void err(String line) {
        err.write(line.getBytes());
    }

    public int in() {
        try {
            return in.read();
        } catch (IOException exc) {
            throw new IllegalStateException(exc);
        }
    }
}
