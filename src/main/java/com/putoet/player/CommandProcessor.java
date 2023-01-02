package com.putoet.player;

import com.putoet.game.Device;
import com.putoet.game.Opcode;

import java.util.Set;

public class CommandProcessor {
    private Device device;

    public boolean execute(String command) {
        assert command != null;

        if (command.startsWith("dump ")) {
            device.dump(Integer.parseInt(command.substring(5)), Set.of(Opcode.HALT, Opcode.RET))
                    .forEach(System.err::println);
            return true;
        }

        if (command.startsWith("set ")) {
            final String[] split = command.substring(4).split(" ");
            final int register = 32768 + split[0].charAt(0) - 'a';
            final int value = Integer.parseInt(split[1]);
            device.registers().set(register, value);
            return true;
        }

        switch (command) {
            case "enable debug" -> device.enableDebug();
            case "disable debug" -> device.disableDebug();
            case "enable step" -> device.enableStep();
            case "disable step" -> device.disableStep();
            case "exit" -> device.exit();
            default -> {
                return false;
            }
        }

        return true;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
