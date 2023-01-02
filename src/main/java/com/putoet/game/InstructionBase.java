package com.putoet.game;

public class InstructionBase implements Instruction {
    protected final Opcode opcode;
    private final int operandCount;
    protected final Register ip;
    protected final int[] operand;

    public InstructionBase(Opcode opcode, Register ip) {
        this.opcode = opcode;
        this.operandCount = 0;
        this.ip = ip;
        this.operand = new int[0];
    }

    public InstructionBase(Opcode opcode, int operandCount, Register ip, Memory memory) {
        if (ip.get() + operandCount >= memory.size())
            throw new OutOfMemoryError("IP=" + ip.get() + " using operand " + operandCount);

        this.opcode = opcode;
        this.operandCount = operandCount;
        this.ip = ip;

        this.operand = new int[operandCount];
        for (int i = 0; i < operandCount; i++) {
            this.operand[i] = memory.read(ip.get() + i + 1);
        }
    }

    @Override
    public Opcode opcode() {
        return opcode;
    }

    @Override
    public void execute() {
    }

    @Override
    public int size() {
        return 1 + operandCount;
    }

    @Override
    public void run() {
        execute();
        ip.accept(ip.get() + size());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(opcode);
        for (var op : operand) {
            sb.append(" ");
            sb.append(op);
        }
        return sb.toString();
    }
}
