package com.putoet.player;

import com.diogonunes.jcolor.Attribute;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;

import static com.diogonunes.jcolor.Ansi.colorize;

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

    @SneakyThrows
    public void coloredWrite(int b) {
        Attribute txtColor = Attribute.GREEN_TEXT();
        out.write(colorize(String.valueOf((char) b), txtColor).getBytes());
        out.flush();
    }
}
