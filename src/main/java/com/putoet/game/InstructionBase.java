/**
 * Instruction base class
 * Implements default methods for any instruction like run(), dump(boolean), and methods for getting the instruction
 * attributes. Child classes should, in general, not implement run() but should implement execute(), as run takes care
 * of updating the IP register based on the specified instruction length (derived from the operand count).
 * After fetching and before execution of the instruction, the values of registers can be changed by the debugger. The
 * operands of the instruction however have already been fetched and cannot change (at least, the Instruction interface
 * doesn't allow them to be changed).
 */
package com.putoet.game;

public class InstructionBase implements Instruction {
    protected final Opcode opcode;
    private final int operandCount;
    protected final Register ip;
    protected final int[] operand;
    private final Registers registers;

    /**
     * Convenience constructor for instructions without operands.
     *
     * @param opcode Opcode
     * @param ip Register to be updated after execution of the instruction
     */
    public InstructionBase(Opcode opcode, Register ip) {
        this.opcode = opcode;
        this.operandCount = 0;
        this.ip = ip;
        this.operand = new int[0];
        this.registers = null;
    }

    /**
     * Constructor for a new Instruction is also known as "fetching". Child classes should, in general, not
     * implement run() but should implement execute(), as run takes care of updating the IP register based on the
     * specified instruction length (derived from the operand count).
     * During construction, memory size is checked to ensure that the IP won't be increased beyond the last
     * byte in memory. Fetching includes operands to be fetched and copied from memory.
     *
     * @param opcode Opcode
     * @param operandCount Operand count
     * @param ip Register to be updated after execution of the instruction
     * @param memory Memory component
     * @param registers Registers component
     */
    public InstructionBase(Opcode opcode, int operandCount, Register ip, Memory memory, Registers registers) {
        if (ip.get() + operandCount >= memory.size())
            throw new OutOfMemoryError("IP=" + ip.get() + " using operand " + operandCount);

        this.opcode = opcode;
        this.operandCount = operandCount;
        this.ip = ip;
        this.registers = registers;

        this.operand = new int[operandCount];
        for (int i = 0; i < operandCount; i++) {
            this.operand[i] = memory.read(ip.get() + i + 1);
        }
    }

    /**
     * Operation code of the instruction
     *
     * @return Opcode
     */
    @Override
    public Opcode opcode() {
        return opcode;
    }

    /**
     * Execute the feature of the instruction (calculation, comparison, etc). This method should be overridden for
     * instructions that do not manipulate the IP register, and rely on run() to perform the IP upgrade for them.
     */
    @Override
    public void execute() {
    }

    /**
     * Returns the size of the instruction in words (1 + number of operands), used to increase the IP register
     * after execution of the instruction.
     *
     * @return int
     */
    @Override
    public int size() {
        return 1 + operandCount;
    }

    /**
     * Executes the instruction by calling Instruction.execute() and then increases the IP with the size of
     * the Instruction.size().
     */
    @Override
    public void run() {
        execute();
        ip.accept(ip.get() + size());
    }

    /**
     * Creates a string representation of the instruction. Register ID's (32768+ values) are replaced with a letter
     * using Registers.asLetter(id) for readability.
     *
     * @param smart if true, and if an operand refers to a register, adds the value of the register between parenthesis
     * @return String
     */
    @Override
    public String dump(boolean smart) {
        final StringBuilder sb = new StringBuilder();
        sb.append(opcode);
        for (var op : operand) {
            sb.append(" ");
            if (Registers.isRegister(op)) {
                sb.append(Registers.asLetter(op));
                if (smart)
                    sb.append(" (").append(registers.get(op)).append(")");
            }
            else
                sb.append(op);
        }
        return sb.toString();
    }
}
