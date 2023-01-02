package com.putoet.game;

import java.util.Stack;

public class Interpreter {
    public static final int ILLEGAL_NUMBER = 32776;

    private final Registers registers;
    private final Memory memory;
    private final Stack<Integer> stack;
    private final InputOutput io;

    public Interpreter(Registers registers, Memory memory, Stack<Integer> stack, InputOutput io) {
        this.registers = registers;
        this.memory = memory;
        this.stack = stack;
        this.io = io;
    }

    public Instruction next(Register ip) {
        var opcode = Opcode.values()[memory.read(ip.get())];

        return switch (opcode) {
            case HALT -> new InstructionBase(opcode, ip);

            case SET -> new InstructionBase(opcode,2, ip, memory) {
                @Override
                public void execute() {
                    registers.set(operand[0], value(ip, registers, operand[1]));
                }
            };

            case PUSH -> new InstructionBase(opcode, 1, ip, memory) {
                @Override
                public void execute() {
                    stack.push(value(ip, registers, operand[0]));
                }
            };

            case POP -> new InstructionBase(opcode, 1, ip, memory) {
                @Override
                public void execute() {
                    var value = stack.pop();
                    registers.set(operand[0], value);
                }
            };

            case EQ -> new InstructionBase(opcode, 3, ip, memory) {
                @Override
                public void execute() {
                    final var value1 = value(ip, registers, operand[1]);
                    final var value2 = value(ip, registers, operand[2]);
                    registers.set(operand[0], value1 == value2 ? 1 : 0);
                }
            };

            case GT -> new InstructionBase(opcode, 3, ip, memory) {
                @Override
                public void execute() {
                    final var value1 = value(ip, registers, operand[1]);
                    final var value2 = value(ip, registers, operand[2]);
                    registers.set(operand[0], value1 > value2 ? 1 : 0);
                }
            };

            case JMP -> new InstructionBase(opcode, 1, ip, memory) {
                @Override
                public void execute() {
                }

                @Override
                public void run() {
                    ip.accept(value(ip, registers, operand[0]));
                }
            };

            case JT -> new InstructionBase(opcode, 2, ip, memory) {
                @Override
                public void run() {
                    final var value = value(ip, registers, operand[0]);
                    if (value != 0)
                        ip.accept(value(ip, registers, operand[1]));
                    else
                        ip.accept(ip.get() + 3);
                }
            };

            case JF -> new InstructionBase(opcode, 2, ip, memory) {
                @Override
                public void run() {
                    final var value = value(ip, registers, operand[0]);
                    if (value == 0)
                        ip.accept(value(ip, registers, operand[1]));
                    else
                        ip.accept(ip.get() + 3);
                }
            };

            case ADD -> new InstructionBase(opcode, 3, ip, memory) {
                @Override
                public void execute() {
                    final var value1 = value(ip, registers, operand[1]);
                    final var value2 = value(ip, registers, operand[2]);
                    registers.set(operand[0], (value1 + value2) % Registers.ARCH_MAX_VALUE);
                }
            };

            case MULT -> new InstructionBase(opcode, 3, ip, memory) {
                @Override
                public void execute() {
                    final var value1 = value(ip, registers, operand[1]);
                    final var value2 = value(ip, registers, operand[2]);
                    registers.set(operand[0], (value1 * value2) % Registers.ARCH_MAX_VALUE);
                }
            };

            case MOD -> new InstructionBase(opcode, 3, ip, memory) {
                @Override
                public void execute() {
                    final var value1 = value(ip, registers, operand[1]);
                    final var value2 = value(ip, registers, operand[2]);
                    registers.set(operand[0], (value1 % value2) % Registers.ARCH_MAX_VALUE);
                }
            };

            case AND -> new InstructionBase(opcode, 3, ip, memory) {
                @Override
                public void execute() {
                    final var value1 = value(ip, registers, operand[1]);
                    final var value2 = value(ip, registers, operand[2]);
                    registers.set(operand[0], value1 & value2);
                }
            };

            case OR -> new InstructionBase(opcode, 3, ip, memory) {
                @Override
                public void execute() {
                    final var value1 = value(ip, registers, operand[1]);
                    final var value2 = value(ip, registers, operand[2]);
                    registers.set(operand[0], value1 | value2);
                }
            };

            case NOT -> new InstructionBase(opcode, 2, ip, memory) {
                @Override
                public void execute() {
                    final var value = value(ip, registers, operand[1]);
                    registers.set(operand[0], (~value) & 0b0111_1111_1111_1111);
                }
            };

            case RMEM -> new InstructionBase(opcode, 2, ip, memory) {
                @Override
                public void execute() {
                    final var value = memory.read(value(ip, registers, operand[1]));
                    registers.set(operand[0], value);
                }
            };

            case WMEM -> new InstructionBase(opcode, 2, ip, memory) {
                @Override
                public void execute() {
                    final var value = value(ip, registers, operand[1]);
                    final var address = value(ip, registers, operand[0]);
                    memory.write(address, value);
                }
            };

            case CALL -> new InstructionBase(opcode, 1, ip, memory) {
                @Override
                public void run() {
                    stack.push(ip.get() + size());

                    final var value = value(ip, registers, operand[0]);
                    ip.accept(value);
                }
            };

            case RET -> new InstructionBase(opcode, ip) {
                @Override
                public void run() {
                    ip.accept(stack.pop());
                }
            };

            case OUT -> new InstructionBase(opcode, 1, ip, memory) {
                @Override
                public void execute() {
                    final var value = value(ip, registers, operand[0]);
                    io.out(value);
                }
            };

            case IN -> new InstructionBase(opcode, 1, ip, memory) {
                @Override
                public void execute() {
                    var value = io.in();
                    registers.set(operand[0], value);
                }
            };

            case NOOP -> new InstructionBase(opcode, ip) {};
        };
    }

    private static int value(Register ip, Registers registers, int number) {
        if (number >= ILLEGAL_NUMBER)
            throw new IllegalStateException("Invalid number encountered '" + number + "' for instruction at " + ip.get());

        if (Registers.isRegister(number)) {
            return registers.get(number);
        }

        return number;
    }
}
