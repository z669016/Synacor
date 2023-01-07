/**
 * Crt class to connect to the OUT port of a device.
 * Device.out is an OutputStream, and Crt forwards the characters it receives to another OutputStream,
 * which could be a file, or the display. The "write(int)" and "write(byte[])" operations immediately flush
 * the stream to prevent loss of output and make is immediately visible in the connected stream.
 * Beware, only write(int) and write(byte[]) reroute to the target output stream, other OutputStream
 * methods are not implemented and thus not connected!
 */
package com.putoet.game;

import java.io.IOException;
import java.io.OutputStream;

public class Crt extends OutputStream {
    private final OutputStream out;

    /**
     * Default constructor, reroutes to System.out
     */
    public Crt() {
        this.out = System.out;
    }

    /**
     * Constructor that reroutes to whatever OutputStream is provided.
     *
     * @param out OutputStream uses for rerouting
     */
    public Crt(OutputStream out) {
        this.out = out;
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
