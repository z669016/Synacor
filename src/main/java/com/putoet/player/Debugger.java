package com.putoet.player;

import com.diogonunes.jcolor.Attribute;
import com.putoet.game.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Debugger implements Runnable, DeviceDebugger {
    private static final Attribute TXT_COLOR = Attribute.YELLOW_TEXT();
    private static final Attribute ERR_COLOR = Attribute.RED_TEXT();

    private final Device device;
    private final Keyboard keyboard;
    private final Set<Integer> breakpoints;

    private boolean running = true;
    private boolean print = false;
    private int nestedCalls = 0;

    private final AtomicBoolean deviceHalted = new AtomicBoolean(false);
    private final AtomicBoolean breakOnNext = new AtomicBoolean(false);
    private final AtomicBoolean breakOnReturn = new AtomicBoolean(false);
    private final AtomicBoolean breakOnOver = new AtomicBoolean(false);

    /**
     * The constructor wraps the device input stream with a Keyboard, and sets itself as the device debugger.
     * Initially the breakpoint for the debugger is empty, and no breakpoint conditions have been set.
     * @param device the device
     */
    public Debugger(Device device) {
        this.keyboard = device.in();
        this.device = device;
        this.breakpoints = new HashSet<>();

        device.setDebugger(this);
    }

    /**
     * The debug callback is called by the device after fetching the next instruction and before
     * execution of the instruction. Depending on requested breakpoint settings, processing of the device
     * is halted by Thread.sleep() until a debugger command releases processing again.
     * When the device is halted, the current ip abd instruction are printed to stdout
     *
     * @param ip device IP address
     * @param instruction device next instruction to be executed
     */
    @Override
    public void debug(Register ip, Instruction instruction) {
        if (breakpoints.contains(ip.get()) || breakOnNext.get()) {
            deviceHalted.set(true);
        }

        if (breakOnReturn.get()) {
            if (instruction.opcode() == Opcode.RET && nestedCalls == 0) {
                breakOnReturn.set(false);
                breakOnNext.set(true);
            } else {
                if (instruction.opcode() == Opcode.CALL)
                    nestedCalls++;
                else if (instruction.opcode() == Opcode.RET)
                    nestedCalls--;
            }
        }

        if (deviceHalted.get()) {
            System.out.println(colorize(deviceState(), TXT_COLOR));
            System.out.println(colorize(currentInstruction(ip, instruction), TXT_COLOR));
            while (deviceHalted.get() && !device.exiting()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            if (breakOnOver.get()) {
                breakOnOver.set(false);
                if (instruction.opcode() == Opcode.CALL)
                    breakOnReturn.set(true);
                else
                    breakOnNext.set(true);
            }

            return;
        }

        if (print) {
            System.out.println(colorize("%05d: %s".formatted(ip.get(), instruction), TXT_COLOR));
        }
    }

    /**
     * Run the 'event loop', so read a command from the standard input (System.in). An emtpy line will be handled
     * by the debugger command executor.
     * A command not starting with a '/' will be forwarded to teh device input stream through the keyboard
     * connected to through the keyboard (which is connected to the device input stream).
     * A command starting with a '/' is executed using the debugger command executor from within a try-catch. Any
     * runtime exception is printed as an error.
     */
    @Override
    public void run() {
        final Scanner scan = new Scanner(System.in);
        while (running) {
            System.out.print(colorize("> ", TXT_COLOR));
            final String command = scan.nextLine();

            if (command.length() == 0)
                execute("");
            else if (!command.startsWith("/"))
                keyboard.accept(command + "\n");
            else {
                try {
                    execute(command.substring(1));
                } catch (RuntimeException exc) {
                    System.out.println(colorize("Running command '" + command + " failed.", ERR_COLOR));
                    System.out.println(colorize(exc.getMessage(), ERR_COLOR));
                    Throwable throwable = exc;
                    while (throwable.getCause() != null) {
                        System.out.println(colorize(exc.getCause().getMessage(), ERR_COLOR));
                        throwable = throwable.getCause();
                    }
                }
            }
        }
    }

    /**
     * Execute debugger command
     * Default (empty string) will be interpreted as 'into'. Before processing the command,
     * the abbreviation will be replaced by the full command. In case of an unknown command
     * an error will be displayed.
     *
     * @param command the debugger command to execute
     */
    private void execute(String command) {
        assert command != null;

        if (command.length() == 0)
            command = "into";

        command = abbreviations(command);

        if (command.startsWith("set ")) {
            setRegister(command);
        } else if (command.startsWith("br")) {
            breakpoint(command);
        } else if (command.startsWith("dump ")) {
            dump(command);
        } else if (command.startsWith("find ")) {
            find(command);
        } else if (command.startsWith("hex ")) {
            hex(command);
        } else {
            switch (command) {
                case "connect" -> device.setDebugger(this);
                case "current instruction" -> System.out.println(colorize(currentInstruction(device.ip(), device.currentInstruction()), TXT_COLOR));
                case "disconnect" -> device.resetDebugger();
                case "disable print" -> this.print = false;
                case "enable print" -> this.print = true;
                case "exit" -> exit();
                case "help" -> help();
                case "into" -> intoInstruction();
                case "over" -> overInstruction();
                case "run" -> runInstruction();
                case "stack" -> stack();
                case "state" -> System.out.println(colorize(deviceState(), TXT_COLOR));
                case "up" -> upInstruction();
                default -> System.out.println(colorize("Invalid command: " + command, ERR_COLOR));
            }
        }
    }


    /**
     * The current instruction as formatter string including memory address
     *
     * @param ip - IP register
     * @param instruction - Instruction
     * @return formatted string
     */
    private String currentInstruction(Register ip, Instruction instruction) {
        return "%05d: %s".formatted(ip.get(), instruction.toString());
    }

    /**
     * Set debugger indication that processing should stop when the next RET statement is
     * encountered and executed.
     */
    private void upInstruction() {
        nestedCalls = 0;
        breakOnReturn.set(true);
        breakOnNext.set(false);
        breakOnOver.set(false);
        deviceHalted.set(false);
    }

    /**
     * Set debugger indication that processing should stop after the next instruction, but before
     * its execution. If the next instruction is a CALL, then stop after the matching RET instruction
     * was executed.
     */
    private void overInstruction() {
        nestedCalls = 0;
        breakOnOver.set(true);
        breakOnNext.set(false);
        breakOnReturn.set(false);
        deviceHalted.set(false);
    }

    /**
     * Proceed execution until another breakpoint is encountered.
     */
    private void runInstruction() {
        breakOnNext.set(false);
        breakOnOver.set(false);
        breakOnReturn.set(false);
        deviceHalted.set(false);
    }

    /**
     * Stop processing after execution of the next instruction.
     */
    private void intoInstruction() {
        breakOnNext.set(true);
        breakOnReturn.set(false);
        breakOnOver.set(false);
        deviceHalted.set(false);
    }

    /**
     * Translate a possible abbreviated command into its full command. Returns the original command
     * if it wasn't a valid abbreviation.
     *
     * @param command possibly abbreviated command string
     * @return full command string
     */
    private static String abbreviations(String command) {
        final var split = command.split(" ");
        final var newCommand = switch (split[0]) {
            case "i" -> "into";
            case "r" -> "run";
            case "o" -> "over";
            case "u" -> "up";
            case "d" -> "dump";
            case "ds" -> "dump smart";
            case "f" -> "find";
            case "h" -> "hex";
            case "s" -> "state";
            case "?" -> "current instruction";
            default -> split[0];
        };

        final StringBuilder sb = new StringBuilder();
        sb.append(newCommand);
        for (int i = 1; i < split.length; i++)
            sb.append(" ").append(split[i]);

        return sb.toString();
    }

    /**
     * COMMAND: help
     * Display the debugger help text
     */
    public void help() {
        final String help = """
            All debugger commands must be preceded by a '/', for all input without preceding
            '/' will be forwarded to the device as a command (with an additional '\\n' required by
            the device as an end-of-line).
            
            Debugger commands:
            connect             - set the device debugger callback (allows stepping through code)
            disable print       - stops printing instructions before execution
            disconnect          - reset the device debugger callback (disables stepping through code)
            dump <pos>          - dumps the instructions at the specified memory position until a HALT or RET is encountered
            dump smart <pos>    - smart dumps the instructions, which means register values are displayed)
            enable print        - starts printing instructions before execution
            exit                - stops the device
            find <text>         - find a text string in memory and make a hex dump from that address (if found)
            help                - displays this text
            hex <from> <size>   - hex dump <size> blocks of 16 bytes of memory starting at <from> (default size is 8)
            into                - execute next statement and halt
            over                - execute nest statement or subroutine in case of a CALL statement and halt
            set <reg> <val>     - set register a-h with the specified value
            stack               - dump contents of the stack
            state               - dump IP and register state
            up                  - execute until and including next RET statement  and halt
            
            For into, up, and over to work, the debugger must be 'connected' first!
            
            the commands dump, hex, run, into, over, and up can be abbreviated wit d, h, r, i, u, and o respectively.
            
            """;
        System.out.println(colorize(help, TXT_COLOR));
    }

    /**
     * COMMAND: state
     * Return a string with the device state (connected state, IP value, and register values)
     *
     * @return string
     */
    private String deviceState() {
        return (device.isConnected() ? "Connected " : "Not connected ") +
               "IP=[" + device.ip() + "], " +
               device.registers().toString();
    }

    /**
     * Print the device stack values
     */
    private void stack() {
        final Stack<Integer> stack = device.stack();
        System.out.println(colorize("Stack (top is last): " + stack.toString(), TXT_COLOR));
    }

    /**
     * Set exit condition for keyboard and device. A linefeed is sent to the keyboard to allow it to
     * process its own exit condition.
     */
    private void exit() {
        device.exit();
        keyboard.exit();
        keyboard.accept("\n");
        running = false;
    }

    /**
     * Set the value of one of the devices register, The command has the formal: set {letter} {value}
     * where letter ia a-h and value is a decimal or hex value (hex value starts with '0x'). The register
     * is set using device.registers().set(id,value).
     * For example:
     *      set a 1200
     *      set c 0x0578
     *
     * @param command the full command string
     */
    private void setRegister(String command) {
        final String[] split = command.substring(4).split(" ");

        final int register = 32768 + split[0].charAt(0) - 'a';
        final int value = fromNumber(split[1]);
        device.registers().set(register, value);
    }

    /**
     * The breakpoint method lists and update the maintained set of breakpoints. The command without parameters will
     * display an ordered list of breakpoints. Parameters contain an action (+|-) and a breakpoint (optional for
     * action '-').
     * For example:
     *      br              - shows a list of breakpoints
     *      br + 1400       - sets a breakpoint at address 1200, and show the updated list of breakpoints
     *      br - 0x0578     - removes the breakpoint at address 1200 (in hex), and show the updated list of breakpoints
     *      br -            - removes all the breakpoint, and show the updated list of breakpoints
     *
     * @param command the full command string
     */
    private void breakpoint(String command) {
        assert command != null;

        final var split = command.split(" ");

        if (split.length > 3 ||
            (split.length == 2 && !split[1].equals("-")) ||
            (split.length == 3 && !(split[1].equals("-") || split[1].equals("+")))) {
            System.out.println(colorize("Invalid breakpoint command:" + command, ERR_COLOR));
            return;
        }

        if (split.length == 2)
                breakpoints.clear();
        else if (split.length == 3) {
            final var action = split[1];
            final var breakpoint = fromNumber(split[2]);

            if ("+".equals(action))
                breakpoints.add(breakpoint);
            else
                breakpoints.remove(breakpoint);
        }

        listBreakpoints();
    }

    /**
     * Print an ordered list of breakpoints
     */
    private void listBreakpoints() {
        if (breakpoints.size() > 0)
            breakpoints.stream().sorted()
                .forEach(b -> System.out.println(colorize("Breakpoint: " + b, TXT_COLOR)));
        else
            System.out.println(colorize("No breakpoints set.", TXT_COLOR));
    }

    /**
     * Dump the instructions starting at the given address up until the first HALT or RET instruction.
     * The address can be a decimal or hex value (hex value starts with '0x').
     * For example:
     *      dump 1400
     *      dump 0x0578
     *
     * @param command the full command string
     */
    private void dump(String command) {
        command = command.substring(5);
        final boolean smart = command.startsWith("smart ");
        if (smart)
            command = command.substring(6);
        final int from = fromNumber(command);
        dump(smart, from, Set.of(Opcode.HALT, Opcode.RET))
                .forEach(line -> System.out.println(colorize(line, TXT_COLOR)));
    }

    /**
     * Dump the hex value of memory of a specified number of block of 16 words (1 word is 2 bytes,
     * default number of blocks is 8), starting at the given address. The address can be a decimal or hex
     * value (hex value starts with '0x').
     *
     * @param command the full command string
     */
    private void hex(String command) {
        final String[] split = command.substring(4).split(" ");
        final int from = fromNumber(split[0]);
        final int size = split.length == 1 ? 8 : Integer.parseInt(split[1]);
        hexDump(from, size)
                .forEach(line -> System.out.println(colorize(line, TXT_COLOR)));
    }

    /**
     * Find the first occurrence of a piece of text in the device memory, and if it is found,
     * display a hex dump starting at that position - 1 (as string are preceded by a word
     * indicating their length).
     *
     * @param command the full command
     */
    private void find(String command) {
        final var text = command.substring(5);
        final var start = findText(text);
        if (start.isEmpty())
            System.out.println(colorize("Text '" + text + "' not found.", ERR_COLOR));
        else
            hex("hex " + (start.getAsInt() - 1));
    }

    /**
     * Create a List<String> with instructions starting at the given address. The individual instructions lines
     * are formatted and preceded with the address in decimal.
     * For example:
     *      1200: SET 32768 1
     *
     * @param smart true if current register values must be displayed
     * @param startAddress starting memory address
     * @param exitCriteria end instruction (to stop the dump
     *
     * @return ordered list of strings containing the dump
     */
    public List<String> dump(boolean smart, int startAddress, Set<Opcode> exitCriteria) {
        final List<String> dump = new ArrayList<>();
        final Interpreter interpreter =
                new Interpreter(device.registers(), device.memory(), device.stack(), device.in(), device.out());

        final Register ip = new Register();
        ip.accept(startAddress);
        while (ip.get() <= device.memory().lastAddressUsed()) {
            var instruction = interpreter.next(ip);
            dump.add("%05d: %s".formatted(ip.get(), instruction.dump(smart)));

            if (exitCriteria.contains(instruction.opcode()))
                break;

            ip.accept(ip.get() + instruction.size());
        }

        return dump;
    }

    /**
     * Create a List<String> with a hex memory dump of a specified size, starting at the given address.
     * The individual hex dump lines are formatted and preceded with the address in decimal.
     *
     * @param startAddress starting memory address
     * @param size number of bytes to dump
     *
     * @return ordered list of strings containing the dump
     */
    public List<String> hexDump(int startAddress, int size) {
        final List<String> dump = new ArrayList<>();

        while (size-- > 0) {
            dump.add(hexDump(startAddress));
            startAddress += 16;
        }

        return dump;
    }

    /**
     * Create one line with a hex memory dump of 32 bytes, starting at the given address.
     * The individual hex dump lines are formatted into three columns and seperated by '|':
     * Column 1 - memory address in decimal
     * Column 2 - 16 2-byte integers in hex value (low byte first, according to architecture)
     * Column 3 - same 16 integers as characters (non-printable characters are printed as ',' )
     *
     * @param startAddress starting memory address
     *
     * @return ordered list of strings containing the dump
     */
    public String hexDump(int startAddress) {
        final StringBuilder sb = new StringBuilder();
        sb.append("%05d".formatted(startAddress)).append(" | ");
        for (int i = 0; i < 16; i++) {
            if (startAddress + i < device.memory().lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(device.memory().read(startAddress + i));
                sb.append("%02x %02x ".formatted(bytes[0], bytes[1]));
            } else {
                sb.append(".. .. ");
            }
        }

        sb.append("| ");
        for (int i = 0; i < 16; i++) {
            if (startAddress + i < device.memory().lastAddressUsed()) {
                final byte[] bytes = Memory.intToBytes(device.memory().read(startAddress + i));
                sb.append(String.valueOf((char) bytes[0]).replaceAll("\\P{Print}", "."));
            } else {
                sb.append(".");
            }
        }

        sb.append(" |");

        return sb.toString();
    }

    /**
     * Transform a number (string) into an integer value. Use radix 10 by default, unless the string starts with
     * '0x' then use radix 16.
     *
     * @param number a decimal ox hex value (like 1200 or 0x0578)
     * @return integer value
     */
    public int fromNumber(String number) {
        assert number != null;

        if (number.length() == 1 && Character.isLetter(number.charAt(0)))
            return device.registers().get(Registers.ARCH_MAX_VALUE + number.charAt(0) - 'a');

        if (number.startsWith("0x"))
            return Integer.parseInt(number.substring(2), 16);

        return Integer.parseInt(number);
    }

    /**
     * Search device memory for a specified sequence of characters
     *
     * @param text String of text to search for
     * @return optional start address
     */
    public OptionalInt findText(String text) {
        return findText(text.getBytes());
    }

    /**
     * Search device memory for a specified sequence of bytes
     *
     * @param bytes Sequence (array) of bytes to search for
     * @return optional start address
     */
    public OptionalInt findText(byte[] bytes) {

        for (int start = 0; start < device.memory().lastAddressUsed(); start++) {
            if (device.memory().read(start) == bytes[0]) {
                for (int end = 1; end < bytes.length; end++) {
                    if (device.memory().read(start+end) != bytes[end])
                        break;

                    if (end == bytes.length - 1)
                        return OptionalInt.of(start);
                }
            }
        }

        return OptionalInt.empty();
    }
}
