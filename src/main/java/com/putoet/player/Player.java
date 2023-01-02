package com.putoet.player;

import com.putoet.game.Device;
import com.putoet.game.InputOutput;
import com.putoet.game.Memory;
import com.putoet.game.Registers;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Player {

    public static void main(String[] args) {
        final Crt crt = new Crt(System.out);
        final Keyboard keyboard = new Keyboard(crt);
        final Registers registers = new Registers();
        final Memory memory = new Memory();
        final InputOutput io = new InputOutput(keyboard, crt);
        final Device device = new Device(registers, memory, io);

        device.loadResource("/challenge.bin");

        final Thread deviceThread = new Thread(device);
        deviceThread.start();

        list("/solution.txt").forEach(command -> keyboard.accept(command + "\n"));
    }

    @SneakyThrows
    public static List<String> list(String resourceName) {
        final URL url = Player.class.getResource(resourceName);
        if (url == null)
            throw new IllegalArgumentException("Invalid resource name '" + resourceName + "'");

        final Path path = Paths.get(url.toURI());
        try (var stream = Files.lines(path)){
            return stream.toList();
        } catch (IOException exc) {
            throw new IllegalArgumentException("Invalid resource name '" + resourceName + "'", exc);
        }
    }
}
