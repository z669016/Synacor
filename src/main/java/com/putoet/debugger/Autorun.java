/**
 * Player class
 * Creates a device, loads a program from a resource called "/challenge.bin", and loads keyboard commands from
 * a resource called "/solution.txt" which all get fed into the keyboard buffer for processing.
 * Connects a Debugger to the device,a d runs the device and keyboard in separate threads.
 */
package com.putoet.debugger;

import com.putoet.device.*;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Autorun {

    @SneakyThrows
    public static void main(String[] args) {
        final Crt crt = new Crt();
        final Registers registers = new Registers();
        final Memory memory = new Memory();
        final Keyboard keyboard = new Keyboard(crt);
        final Device device = new Device(registers, memory, keyboard, crt);
        final Debugger debugger = new Debugger(device);

        device.loadResource("/challenge.bin");

        final Thread deviceThread = new Thread(device);
        deviceThread.start();

        list("/solution.txt").forEach(command -> keyboard.accept(command + "\n"));
        final Thread keyboardThread = new Thread(debugger);
        keyboardThread.start();

        deviceThread.join();
        keyboardThread.join();
    }

    /**
     * Load keyboard commands from a resource file
     *
     * @param resourceName String
     * @return List of commands (strings)
     */
    @SneakyThrows
    public static List<String> list(String resourceName) {
        final URL url = Autorun.class.getResource(resourceName);
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
