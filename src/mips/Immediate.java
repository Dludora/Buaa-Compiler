// -*- coding: utf-8 -*-
// @Time    : 2022/11/2 11:17
// @Author  : LuJiuxi
// @File    : IntConst.java
// @Software: IntelliJ IDEA
// @Comment :

package mips;

public class Immediate implements Operand{
    private final int value;

    public Immediate(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
