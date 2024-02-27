package mips;

public enum Mnemonic {
    sw,
    lw,
    li,
    add,
    sub,
    mul,
    div,
    rem,
    jal,
    jr,
    la,
    syscall,
    seq,
    sgt,
    sge,
    slt, // 由于位数的原因，不使用slti，需要特殊处理
    sle,
    sne,
    and,
    andi,
    or,
    ori,
    j,
    beq,
    bne,
    sll,
    nop
}