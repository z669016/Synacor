package com.putoet.player;

import com.putoet.game.Device;
import com.putoet.game.InputOutput;
import com.putoet.game.Memory;
import com.putoet.game.Registers;
import lombok.SneakyThrows;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Player {

    @SneakyThrows
    public static void main(String[] args) {
         final OutputStream err =  new FileOutputStream("player.txt");

        final Crt crt = new Crt(System.out);
        final Registers registers = new Registers();
        final Memory memory = new Memory();
        final CommandProcessor commandProcessor = new CommandProcessor();
        final Keyboard keyboard = new Keyboard(crt, commandProcessor);
        final InputOutput io = new InputOutput(keyboard, crt, err);
        final Device device = new Device(registers, memory, io);

        device.loadResource("/challenge.bin");
        commandProcessor.setDevice(device);

        final Thread deviceThread = new Thread(device);
//        device.enableDebug();
        deviceThread.start();

        list("/solution.txt").forEach(command -> keyboard.accept(command + "\n"));
        final Thread keyboardThread = new Thread(keyboard);
        keyboardThread.start();

        deviceThread.join();
        keyboardThread.join();

         err.close();
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
