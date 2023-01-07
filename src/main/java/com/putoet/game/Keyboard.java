package com.putoet.game;

import com.diogonunes.jcolor.Attribute;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Keyboard extends InputStream implements Runnable, Consumer<String>{
    public static final Attribute TXT_COLOR = Attribute.GREEN_TEXT();
    private final Queue<String> queue = new ArrayBlockingQueue <>(1000);
    private final OutputStream out;
    private String currentCommand = null;
    private int offset = 0;
    private boolean running = true;

    public Keyboard(OutputStream out) {
        assert out != null;

        this.out = out;
    }

    @Override
    public int read() throws IOException {
        while (currentCommand == null) {
            offset = 0;
            currentCommand = queue.poll();
            if (currentCommand == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
            else
                if (currentCommand.length() == 0)
                    currentCommand = null;
        }

        final int c = currentCommand.charAt(offset++);
        if (offset >= currentCommand.length()) {
            currentCommand = null;
            offset = 0;
        }

        out.write(colorize(String.valueOf((char) c), TXT_COLOR).getBytes());
        return c;
    }

    @Override
    public void run() {
        final Scanner scan = new Scanner(System.in);
        String command = scan.nextLine();
        while (running) {
            queue.offer(command + "\n");
            command = scan.nextLine();
        }
    }

    @Override
    public void accept(String command) {
        queue.offer(command);
    }

    public void exit() {
        running = false;
    }
}
