package com.putoet.player;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

public class Keyboard extends InputStream implements Runnable, Consumer<String>{
    private final Queue<String> queue = new ArrayBlockingQueue <>(1000);
    private final Crt crt;
    private String currentCommand = null;
    private int offset = 0;
    private boolean running = true;

    public Keyboard(Crt crt) {
        assert crt != null;

        this.crt = crt;
    }

    @SneakyThrows
    @Override
    public int read() throws IOException {
        while (currentCommand == null) {
            offset = 0;
            currentCommand = queue.poll();
            if (currentCommand == null)
                Thread.sleep(100);
            else
                if (currentCommand.length() == 0)
                    currentCommand = null;
        }

        final int c = currentCommand.charAt(offset++);
        if (offset >= currentCommand.length()) {
            currentCommand = null;
            offset = 0;
        }

        crt.coloredWrite(c);
        return c;
    }

    @Override
    public void run() {
        final Scanner scan = new Scanner(System.in);
        String command = scan.nextLine();
        while (true && running) {
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
