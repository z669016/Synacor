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
    private final CommandProcessor commandProcessor;

    private String currentCommand = null;
    private int offset = 0;

    public Keyboard(Crt crt, CommandProcessor commandProcessor) {
        assert crt != null;
        assert commandProcessor != null;

        this.crt = crt;
        this.commandProcessor = commandProcessor;
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

    public void run() {
        final Scanner scan = new Scanner(System.in);
        String command = scan.nextLine();
        while (true) {
            if (!commandProcessor.execute(command)) {
                queue.offer(command + "\n");
            }

            if ("exit".equals(command)) {
                queue.offer("\n"); // enforce the io-loop of the device to run
                break;
            }

            command = scan.nextLine();
        }
    }

    @Override
    public void accept(String command) {
        queue.offer(command);
    }
}
