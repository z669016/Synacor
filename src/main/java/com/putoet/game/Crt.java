package com.putoet.game;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;

public class Crt extends OutputStream {
    private final OutputStream out;

    public Crt(OutputStream out) {
        this.out = out;
    }

    @SneakyThrows
    @Override
    public void flush() {
        out.flush();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        flush();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
        flush();
    }
}
