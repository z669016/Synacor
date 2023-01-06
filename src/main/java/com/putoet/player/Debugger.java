package com.putoet.player;

import com.diogonunes.jcolor.Attribute;
import com.putoet.game.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Debugger implements Runnable, DeviceDebugger {
    private static final Attribute TXT_COLOR = Attribute.YELLOW_TEXT();
    private static final Attribute ERR_COLOR = Attribute.RED_TEXT();

    private final Device device;
    private final Keyboard keyboard;
    private final Set<Integer> breakpoints;

    private boolean running = true;
    private boolean print = false;

    public Debugger(Keyboard keyboard, Device device) {
        this.keyboard = keyboard;
        this.device = device;
        this.breakpoints = new HashSet<>();
    }

    @Override
    public void debug(Register ip, Instruction instruction) {
        if (print) {
            System.out.println(colorize("%05d: %s".formatted(ip.get(), instruction), TXT_COLOR));
        }
    }

    @Override
    public void run() {
        final Scanner scan = new Scanner(System.in);
        while (running) {
            System.out.println(colorize(deviceState(), TXT_COLOR));
            final String command = scan.nextLine();
            if (command.startsWith("/"))
                execute(command.substring(1));
            else
                keyboard.accept(command + "\n");
        }
    }

    public void execute(String command) {
        assert command != null;

        if ("help".equals(command)) {
            help();
        } else if ("state".equals(command)) {
            System.out.println(colorize(deviceState(), TXT_COLOR));
        } else if ("stack".equals(command)) {
            stack();
        } else if ("exit".equals(command)) {
            exit();
        } else if (command.startsWith("set ")) {
            setRegister(command);
        } else if (command.startsWith("br")) {
            breakpoint(command);
        } else if (command.startsWith("dump ")) {
            dump(command);
        } else if (command.startsWith("hex ")) {
            hex(command);
        } else {
            switch (command) {
                case "connect" -> device.setDebugger(this);
                case "disconnect" -> device.resetDebugger();
                case "disable print" -> this.print = false;
                case "enable print" -> this.print = true;
                default -> System.out.println(colorize("IInvalid command: " + command, ERR_COLOR));
            }
        }
    }

    public void help() {

        final String help = """
            All debugger commands must be preceded by a '/', for all input without preceding
            '/' will be forwarded to the device as a command (with an additional '\\n' required by
            the device as an end-of-line).
            
            Debugger commands:
            help                - displays this text
            enable debug        - starts printing instructions before execution
            exit                - stops the device
            disable debug       - stops printing instructions before execution
            dump <pos>          - dumps the instructions at the specified memory position until a HALT or RET is encountered
            hex <from> <size>   - hex dump <size> blocks of 16 bytes of memory starting at <from> (default size is 8)
            set <reg> <val>     - set register a-h with the specified value
            stack               - dump contents of the stack
            state               - dump IP and register state
            
            """;
        System.out.println(colorize(help, TXT_COLOR));
    }

    private String deviceState() {
        return "IP=[" + device.ip() + "], " + device.registers().toString();
    }

    private void stack() {
        final Stack<Integer> stack = device.stack();
        System.out.println(colorize("Stack (top is last): " + stack.toString(), TXT_COLOR));
    }

    private void exit() {
        device.exit();
        keyboard.exit();
        keyboard.accept("\n");
        running = false;
    }

    private void setRegister(String command) {
        final String[] split = command.substring(4).split(" ");

        final int register = 32768 + split[0].charAt(0) - 'a';
        final int value = Integer.parseInt(split[1]);
        device.registers().set(register, value);
    }

    private void breakpoint(String command) {
        final String[] split = command.split(" ");

        if (split.length == 1) {
            if (breakpoints.size() > 0)
                breakpoints.stream().sorted()
                    .forEach(b -> System.out.println(colorize("Breakpoint: " + b, TXT_COLOR)));
            else
                System.out.println(colorize("No breakpoints set.", ERR_COLOR));
        }
    }

    private void dump(String command) {
        final int from = Integer.parseInt(command.substring(5));
        dump(from, Set.of(Opcode.HALT, Opcode.RET))
                .forEach(line -> System.out.println(colorize(line, TXT_COLOR)));
    }

    private void hex(String command) {
        final String[] split = command.substring(4).split(" ");
        final int from = Integer.parseInt(split[0]);
        final int size = split.length == 1 ? 8 : Integer.parseInt(split[1]);
        hexDump(from, size)
                .forEach(line -> System.out.println(colorize(line, TXT_COLOR)));
    }

    public List<String> dump(int startAddress, Set<Opcode> exitCriteria) {
        final List<String> dump = new ArrayList<>();
        final Interpreter interpreter =
                new Interpreter(device.registers(), device.memory(), device.stack(), device.io());

        final Register ip = new Register();
        ip.accept(startAddress);
        while (ip.get() <= device.memory().lastAddressUsed()) {
            var instruction = interpreter.next(ip);
            dump.add("%05d: %s".formatted(ip.get(), instruction.toString()));

            if (exitCriteria.contains(instruction.opcode()))
                break;

            ip.accept(ip.get() + instruction.size());
        }

        return dump;
    }

    public List<String> hexDump() {
        final List<String> dump = new ArrayList<>();

        int offset = 0;
        while (offset < device.memory().lastAddressUsed()) {
            dump.add(hexDump(offset));
            offset += 16;
        }

        return dump;
    }
    public List<String> hexDump(int offset, int size) {
        final List<String> dump = new ArrayList<>();

        while (size-- > 0) {
            dump.add(hexDump(offset));
            offset += 16;
        }

        return dump;
    }

    public String hexDump(int offset) {
        final StringBuilder sb = new StringBuilder();
        sb.append("%04x".formatted(offset)).append(" | ");
        for (int i = 0; i < 16; i++) {
            if (offset + i < device.memory().lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(device.memory().read(offset + i));
                sb.append("%02x %02x ".formatted(bytes[0], bytes[1]));
            } else {
                sb.append(".. .. ");
            }
        }

        sb.append("| ");

        for (int i = 0; i < 16; i++) {
            if (offset + i < device.memory().lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(device.memory().read(offset + i));
                sb.append(new String(bytes, StandardCharsets.UTF_8)
                                .replaceAll("\\P{Print}", "."))
                        .append(" ");
            } else {
                sb.append(".. ");
            }
        }

        sb.append("| ");
        for (int i = 0; i < 16; i++) {
            if (offset + i < device.memory().lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(device.memory().read(offset + i));
                sb.append(String.valueOf((char) bytes[0]).replaceAll("\\P{Print}", "."));
            } else {
                sb.append(".");
            }
        }

        sb.append(" |");

        return sb.toString();
    }
}
