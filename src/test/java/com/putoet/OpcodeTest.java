package com.putoet;

import com.putoet.Opcode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpcodeTest {

    @Test
    void values() {
        assertEquals(0, Opcode.HALT.ordinal());
        assertEquals(1, Opcode.SET.ordinal());
        assertEquals(2, Opcode.PUSH.ordinal());
        assertEquals(3, Opcode.POP.ordinal());
        assertEquals(4, Opcode.EQ.ordinal());
        assertEquals(5, Opcode.GT.ordinal());
        assertEquals(6, Opcode.JMP.ordinal());
        assertEquals(7, Opcode.JT.ordinal());
        assertEquals(8, Opcode.JF.ordinal());
        assertEquals(9, Opcode.ADD.ordinal());
        assertEquals(10, Opcode.MULT.ordinal());
        assertEquals(11, Opcode.MOD.ordinal());
        assertEquals(12, Opcode.AND.ordinal());
        assertEquals(13, Opcode.OR.ordinal());
        assertEquals(14, Opcode.NOT.ordinal());
        assertEquals(15, Opcode.RMEM.ordinal());
        assertEquals(16, Opcode.WMEM.ordinal());
        assertEquals(17, Opcode.CALL.ordinal());
        assertEquals(18, Opcode.RET.ordinal());
        assertEquals(19, Opcode.OUT.ordinal());
        assertEquals(20, Opcode.IN.ordinal());
        assertEquals(21, Opcode.NOOP.ordinal());
    }
}