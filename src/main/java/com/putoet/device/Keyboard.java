/**
 * Keyboard class
 * Can be used as an input component for a device class. It extends InputStream (so it can be connected to an
 * Instruction as the In component), Consumer<String> to accept commands from another source, and Runnable
 * in order to run on an independent thread from the running device.
 * Beware, from InputStream only read() is implemented, so any other InputStream methods should not be used!
 * All words requested by the device are passed on to it, through the implemented read() method. Any commands passed
 * through the Consumer, or keyboard interface are offered to a queue, from which the read() method fetches the
 * commands one by one, sending each command character as a separate int value.
 * This approach allows for automatic replay of a scenario, by using the keyboard as Consumer when the keyboard input
 * is fetched when run() executes on a separate thread. It also allows for a separate external debugger to offer
 * commands.
 * Each character offered to the In component is echoed to an OutputStream connected to the keyboard on construction.
 * This class uses com.diogonunes.jcolor to colorize the echoed character to the OutputStream.
 */
package com.putoet.device;

import com.diogonunes.jcolor.Attribute;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Keyboard extends InputStream implements Runnable, Consumer<String>, DebuggerSupport {
    public static final Attribute TXT_COLOR = Attribute.GREEN_TEXT();
    private final Queue<String> queue = new ArrayBlockingQueue <>(1000);
    private final OutputStream out;
    private String currentCommand = null;
    private DeviceDebugger debugger = DebuggerSupport.DEFAULT_DEBUGGER;

    private int offset = 0;
    private boolean running = true;

    /**
     * Constructor, connects the keyboard to an output stream to echo the character passed to the in component.
     *
     * @param out OutputStream
     */
    public Keyboard(OutputStream out) {
        assert out != null;

        this.out = out;
    }

    /**
     * This method offers the next available character of the current command to the In component. When no more
     * characters are available from the current command, a new current command if polled from the command queue.
     * WHen there is no (new) current command, the method sleeps for 100 milliseconds before checking again.
     * Commands are strings, and empty strings are ignored.
     * Before a character is passed to the In component it is echoed to the configured OutputStream with color
     * (using com.diogonunes.jcolor).
     *
     * @return int next character of the current command to be processed
     * @throws IOException cannot happen, but required by its interface
     */
    @Override
    public int read() throws IOException {
        // while no command available
        while (currentCommand == null) {
            offset = 0;
            currentCommand = queue.poll();

            // ignore empty commands
            if ("\n".equals(currentCommand))
                currentCommand = null;

            // If command starts with # it's comment, just ignore it
            if (currentCommand != null && currentCommand.startsWith("#"))
                currentCommand = null;

            // Pass the command through the debugger first
            if (currentCommand != null) {
                currentCommand = debugger.debug(currentCommand);
            }

            // if no command available sleep for a while
            if (currentCommand == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }

        final int c = currentCommand.charAt(offset++);
        if (offset >= currentCommand.length()) {
            currentCommand = null;
            offset = 0;
        }

        out.write(colorize(String.valueOf((char) c), TXT_COLOR).getBytes());
        return c;
    }

    /**
     * When running (on a separate thread) the run() method read lines from System.in using a Scanner, and offers
     * the entered commands (with an added newline) to the keyboard queue for processing.
     * This allows for user input to the device, while also a connected debugger can send commands to the keyboard.
     * The method runs while the running flag is set to true.
     */
    @Override
    public void run() {
        final Scanner scan = new Scanner(System.in);
        String command = scan.nextLine();
        while (running) {
            accept(command + "\n");
            command = scan.nextLine();
        }
    }

    /**
     * Adds the provided command (which must include a newline) to the queue for processing by the device.
     *
     * @param command String (including final newline)
     */
    @Override
    public void accept(String command) {
        queue.offer(command);
    }

    /**
     * Set the running flag to false, to enable graceful shutdown if the keyboard runs on a separate thread.
     */
    public void exit() {
        running = false;
    }

    @Override
    public void setDebugger(DeviceDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public void resetDebugger() {
        this.debugger = DebuggerSupport.DEFAULT_DEBUGGER;
    }
}
