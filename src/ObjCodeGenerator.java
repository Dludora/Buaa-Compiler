import SymTable.LValSymItem;
import Tools.Utility;
import mid.*;
import mips.*;
import mips.Label;

import java.util.ArrayList;
import java.util.Stack;

public class ObjCodeGenerator {
    private int tVarOffset = 0;
    private final Stack<Register> temporaryVar = new Stack<>(); // 模拟临时变量区

    public void genObjCode() {
        ArrayList<Value> values = MidCode.getValues();
        for (Value value : values) {
            if (value instanceof Instruction) { // 生成全局变量初始化的asm
                Instruction inst = (Instruction) value;
                ArrayList<Integer> initVal = new ArrayList<>();
                for (Value operand : inst.getOperandList()) {
                    if (operand instanceof IntConst) {
                        initVal.add(((IntConst) operand).getValue());
                    }
                }
                VarReg varReg = (VarReg) inst.getRegister();
                LValSymItem lValSymItem = (LValSymItem) varReg.getVarSymItem();
                if (lValSymItem.isZeroInit()) {
                    // .space <initVal>:<size>
                    Space space = new Space(lValSymItem.getSize(),
                            varReg.getVarName());
                    MipsCode.addSpace(space);
                } else {
                    // .word <initVal[0]>, <initVal[1]>, ..., <initVal[n]>
                    Word word = new Word(varReg.getVarName(), initVal);
                    MipsCode.addWord(word);
                }
            } else if (value instanceof Function) {
                Function function = (Function) value;
                genFunction(function);
            }
        }
    }

    private void genFunction(Function function) {
        // 设置临时变量offset
        tVarOffset = function.getTVarOffset();
        // 生成func的label
        mips.Label label = new Label("\nfunc_" + function.getFuncName());
        MipsCode.addAsm(label);
        // 生成函数asm
        int fParamCounter = 0;
        for (Value value : function.getOperandList()) {
            // 保存前4个形参，编号大于4的形参在call时存入对应位置
            if (value instanceof FParam) {
                FParam fParam = (FParam) value;
                int adr = fParam.getVarReg().getVarSymItem().getAdr();
                if (fParamCounter < 4) {
                    // sw $ax, <adr>($fp)
                    sw(Utility.numToAReg(fParamCounter), Reg.$fp, adr);
                } else {
                    // lw $t0, <fParamCounter * 4>($sp)
                    lw(Reg.$t0, Reg.$sp, fParamCounter * 4);
                    // sw $t0, <adr>($fp)
                    sw(Reg.$t0, Reg.$fp, adr);
                }
                fParamCounter++;
            } else if (value instanceof BasicBlock) {
                // 生成基本块asm
                BasicBlock basicBlock = (BasicBlock) value;
                genBasicBlock(basicBlock);
            }
        }
        temporaryVar.clear();
    }

    private void genBasicBlock(BasicBlock basicBlock) {
        // 生成基本块LABEL
        Label label = new Label(basicBlock.getLabel().getLabel());
        MipsCode.addAsm(label);
        // 生成各个指令的asm
        InstList insts = basicBlock.getInstList();
        for (Instruction inst: insts) {
            genAsm(inst);
        }
    }

    private void genAsm(Instruction irInst) {
        switch (irInst.getInstSet()) {
            case add: genAdd(irInst); break;
            case sub: genSub(irInst); break;
            case mul: genMul(irInst); break;
            case sdiv: genSDiv(irInst); break;
            case srem: genSRem(irInst); break;
            case icmp_eq: genIcmp_eq(irInst); break;
            case icmp_gt: genIcmp_gt(irInst); break;
            case icmp_ge: genIcmp_ge(irInst); break;
            case icmp_lt: genIcmp_lt(irInst); break;
            case icmp_le: genIcmp_le(irInst); break;
            case icmp_neq: genIcmp_neq(irInst); break;
            case and: genAnd(irInst); break;
//            case or: genOr(irInst); break;
            case call: genCall(irInst); break;
            case alloca: genAlloca(irInst); break;
            case load: genLoad(irInst); break;
            case store: genStore(irInst); break;
            case getelementptr: genGetElementPtr(irInst); break;
            case br: genBr(irInst); break;
            case br_eq: genBr_eq(irInst); break;
            case br_ne: genBr_ne(irInst); break;
            case ret: genRet(irInst); break;
            case print: genPrint(irInst); break;
            case getInt: genGetInt(irInst); break;
            default: genNop();
        }
    }

    private void genAdd(Instruction irInst) {
        commutativeOperation(irInst, Mnemonic.add, Mnemonic.add);
    }

    private void genSub(Instruction irInst) {
        unCommutativeOperation(irInst, Mnemonic.sub, Mnemonic.sub);
    }

    private void genMul(Instruction irInst) {
        commutativeOperation(irInst, Mnemonic.mul, Mnemonic.mul);
    }

    private void genSDiv(Instruction irInst) {
        unCommutativeOperation(irInst, Mnemonic.div, Mnemonic.div);
    }

    private void genSRem(Instruction irInst) {
        unCommutativeOperation(irInst, Mnemonic.rem, Mnemonic.rem);
    }

    private void genIcmp_eq(Instruction irInst) {
        commutativeOperation(irInst, Mnemonic.seq, Mnemonic.seq);
    }

    private void genIcmp_gt(Instruction irInst) {
        unCommutativeOperation(irInst, Mnemonic.sgt, Mnemonic.sgt);
    }

    private void genIcmp_ge(Instruction irInst) {
        unCommutativeOperation(irInst, Mnemonic.sge, Mnemonic.sge);
    }

    private void genIcmp_lt(Instruction irInst) {
        Value var2 = irInst.getOperand(1);
        if (var2 instanceof IntConst) {
            IntConst intConst2 = (IntConst) var2;
            // li $t2, <var2>
            li(Reg.$t2, intConst2.getValue());
        } else if (var2 instanceof Instruction) {
            // lw $t2, <offset>($fp)
            lw(Reg.$t2, Reg.$fp, load());
        }

        Value var1 = irInst.getOperand(0);
        if (var1 instanceof IntConst) {
            IntConst intConst1 = (IntConst) var1;
            // li $t1, <var1>
            li(Reg.$t1, intConst1.getValue());
        } else if (var1 instanceof Instruction) {
            // lw $t1, <offset>($fp)
            lw(Reg.$t1, Reg.$fp, load());
        }

        // slt $t0, $t1, $t2
        slt(Reg.$t0, Reg.$t1, Reg.$t2);

        // sw $t0, <offset>($fp)
        int offset = store(irInst.getRegister());
        sw(Reg.$t0, Reg.$fp, offset);
    }

    private void genIcmp_le(Instruction irInst) {
        unCommutativeOperation(irInst, Mnemonic.sle, Mnemonic.sle);
    }

    private void genIcmp_neq(Instruction irInst) {
        commutativeOperation(irInst, Mnemonic.sne, Mnemonic.sne);
    }

    private void genAnd(Instruction irInst) {
        commutativeOperation(irInst, Mnemonic.and, Mnemonic.andi);
    }

//    private void genOr(IRInst irInst) {
//        commutativeOperation(irInst, Mnemonic.or, Mnemonic.ori);
//    }

    private void genCall(Instruction irInst) {
        Function function = (Function) irInst.getOperand(0);
        // 保存现场 p.s.目前只需要存ra
        Reg[] regs = new Reg[]{Reg.$ra};
        saveTheSite(regs);
        // 处理参数传递
        ArrayList<Value> rParams = function.getOperandList();
        // add $sp, $sp, -<rParams.size*4> 分配参数空间
        add(Reg.$sp, Reg.$sp, -rParams.size() * 4);
        for (int i = rParams.size() - 1; i >= 0; i--) {
            Value rParam = rParams.get(i);
            if (rParam instanceof IntConst) {
                IntConst intConst = (IntConst) rParam;
                if (i < 4) {
                    // li $ax, <rParam>
                    li(Utility.numToAReg(i), intConst.getValue());
                } else {
                    // li $t0, <rParam>
                    li(Reg.$t0, intConst.getValue());
                    // sw $t0, <i*4>($sp)
                    sw(Reg.$t0, Reg.$sp, i * 4);
                }
            } else {
                int offset = load();
                if (i < 4) {
                    // lw $ax, <offset>($fp)
                    lw(Utility.numToAReg(i), Reg.$fp, offset);
                } else {
                    // lw $t0, <offset>($fp)
                    lw(Reg.$t0, Reg.$fp, offset);
                    // sw $t0, <i*4>($sp)
                    sw(Reg.$t0, Reg.$sp, i * 4);
                }
            }
        }
        // 移动帧指针 addi $fp, $fp, <tVarOffset + temporaryVar.size() * 4>
        int offset = (temporaryVar.size() << 2) + tVarOffset;
        add(Reg.$fp, Reg.$fp, offset);
        // jalr <function name>
        jal("func_" + function.getFuncName());
        // nop
        nop();
        // 恢复帧指针 addi $fp, $fp, -<tVarOffset>
        add(Reg.$fp, Reg.$fp, -offset);
        // addi $sp, $sp, <rParam.size*4> 销毁传参的空间
        add(Reg.$sp, Reg.$sp, rParams.size() * 4);
        // 恢复现场
        restoreTheSite(regs);
        // 保存返回值，如果有
        if (irInst.getRegister().getRegNum() >= 0) {
            // sw $v0, <offset>($fp)
            offset = store(irInst.getRegister());
            sw(Reg.$v0, Reg.$fp, offset);
        }
    }

    private void genAlloca(Instruction irInst) {
        // 因为内存管理的特殊性，分配内存无需任何操作
    }

    private void genLoad(Instruction irInst) {
        // 将变量从局部变量区或全局变量区load到临时变量区
        // load指令前一定是getelementptr, 地址存在栈顶
        // lw $t1, <offset>($fp)
        int offset = load();
        lw(Reg.$t1, Reg.$fp, offset);
        // lw $t1, 0($t1)
        lw(Reg.$t1, Reg.$t1, 0);
        // sw $t1, <offset>($fp)
        offset = store(irInst.getRegister());
        sw(Reg.$t1, Reg.$fp, offset);
    }

    private void genStore(Instruction irInst) {
        // 将变量从临时变量区store到局部变量区或全局变量区
        // stack top [val, addr,...] bottom
        // 生成值寄存器
        Value value = irInst.getOperand(1);
        if (value instanceof IntConst) {
            IntConst intConst = (IntConst) value;
            // li $t0, <var>
            li(Reg.$t0, intConst.getValue());
        } else if (value instanceof Instruction){
            // lw $t0, <offset>($fp)
            int offset = load();
            lw(Reg.$t0, Reg.$fp, offset);
        }
        // lw $t0, <offset>($fp)
        int offset = load();
        lw(Reg.$t1, Reg.$fp, offset); // 获得地址
        // sw $t0, 0($t1)
        sw(Reg.$t0, Reg.$t1, 0);
    }

    private void genGetElementPtr(Instruction irInst) {
        // 因为最多只有二维数组，所以在目标码生成时直接通过枚举进行
        ArrayType arrayType = (ArrayType) irInst.getOperand(0);
        Instruction base = (Instruction) irInst.getOperand(1);
        // 获取base
        if (base.getInstSet().equals(InstSet.global)) { // 是全局变量
            // la $t0, <var name>
            la(Reg.$t0, ((VarReg) base.getRegister()).getVarName());
        } else if (base.getInstSet().equals(InstSet.alloca)) { // 局部变量
            // 需要区分是地址还是值, 地址是常量指针还是变量指针
            if (arrayType.size() == 0 || arrayType.get(0) > 0) {
                // add $t0, $fp, <var adr>
                add(Reg.$t0, Reg.$fp, ((VarReg) base.getRegister()).getVarSymItem().getAdr());
            } else {
                // 变量指针, 不可以通过查表来获取地址，需要从内存load
                // add $t0, $fp, <ptr adr>
                add(Reg.$t0, Reg.$fp, ((VarReg) base.getRegister()).getVarSymItem().getAdr());
                // lw $t0, <0>($t0)
                lw(Reg.$t0, Reg.$t0, 0);
            }

        }
        // 计算基地址+偏移的结果，注意需要对下标偏移乘4
        if (irInst.getOperandNum() == 3) { // 一维数组 base += subscript
            Value subscript = irInst.getOperand(2);
            if (subscript instanceof IntConst) {
                IntConst intConst = (IntConst) subscript;
                int offset;
                if (arrayType.size() == 2) offset = intConst.getValue() * arrayType.get(1) * 4;
                else offset = intConst.getValue() * 4;
                // addi $t0, $t0, <offset>
                add(Reg.$t0, Reg.$t0, offset);
            } else if (subscript instanceof Instruction) {
                // lw $t1, <offset>($fp)
                lw(Reg.$t1, Reg.$fp, load());
                if (arrayType.size() == 2) {
                    // mul $t1, $t1, arrayType[1]
                    mul(Reg.$t1, Reg.$t1, arrayType.get(1));
                }
                // sll $t1, $t1, 2
                sll(Reg.$t1, Reg.$t1, 2);
                // add $t0, $t0, $t1
                add(Reg.$t0, Reg.$t0, Reg.$t1);
            }
        } else if (irInst.getOperandNum() == 4) { // 二维数组 base += subscript1 * arrayType[1] + subscript2
            Value subscript1 = irInst.getOperand(2);
            Value subscript2 = irInst.getOperand(3);
            if (subscript2 instanceof IntConst) {
                IntConst intConst2 = (IntConst) subscript2;
                if (subscript1 instanceof IntConst) {
                    IntConst intConst1 = (IntConst) subscript1;
                    // (ax + b) * 4
                    int offset = (arrayType.get(1) * intConst1.getValue() + intConst2.getValue()) * 4;
                    // add $t0, $t0, offset
                    add(Reg.$t0, Reg.$t0, offset);
                } else if (subscript1 instanceof Instruction) {
                    // ax * 4 + b * 4
                    // lw $t1, <offset>($fp)
                    lw(Reg.$t1, Reg.$fp, load());
                    // mul $t1, $t1, arrayType[1] * 4
                    mul(Reg.$t1, Reg.$t1, arrayType.get(1) * 4);
                    // add $t1, $t1, <intConst2> * 4
                    add(Reg.$t1, Reg.$t1, intConst2.getValue() * 4);
                    // add $t0, $t0, $t1
                    add(Reg.$t0, Reg.$t0, Reg.$t1);
                }
            } else if (subscript2 instanceof Instruction) {
                // ax + b
                // lw $t1, <offset>($fp)
                lw(Reg.$t1, Reg.$fp, load());
                if (subscript1 instanceof IntConst) {
                    IntConst intConst1 = (IntConst) subscript1;
                    // add $t1, $t1, <intConst> * arrayType[1]
                    add(Reg.$t1, Reg.$t1, intConst1.getValue() * arrayType.get(1));
                } else if (subscript1 instanceof Instruction) {
                    // lw $t2, <offset>($fp)
                    lw(Reg.$t2, Reg.$fp, load());
                    // mul $t2, $t2, arrayType[1]
                    mul(Reg.$t2, Reg.$t2, arrayType.get(1));
                    // add $t1, $t1, $t2
                    add(Reg.$t1, Reg.$t1, Reg.$t2);
                }
                // * 4
                // sll $t1, $t1, 2
                sll(Reg.$t1, Reg.$t1, 2);
                // add $t0, $t0, $t1
                add(Reg.$t0, Reg.$t0, Reg.$t1);
            }
        }
        // sw $t0, <offset>($fp)
        int offset = store(irInst.getRegister());
        sw(Reg.$t0, Reg.$fp, offset);
    }

    private void genBr(Instruction irInst) {
        mid.Label irLabel = (mid.Label) irInst.getOperand(0);
        // j <label>
        j(irLabel.getLabel());
        // nop
        nop();
    }

    private void genBr_eq(Instruction irInst) {
        Value var1 = irInst.getOperand(0);
        Value var2 = irInst.getOperand(1);
        mid.Label irLabel = (mid.Label) irInst.getOperand(2);
        if (var2 instanceof IntConst) {
            IntConst intConst2 = (IntConst) var2;
            if (var1 instanceof IntConst) { // var1 var2 都是常数
                IntConst intConst1 = (IntConst) var1;
                // li $t1, <var1>
                li(Reg.$t1,intConst1.getValue());
            } else if (var1 instanceof Instruction) {
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
            }
            // beq $t1, <var2>, <label>
            beq(Reg.$t1, intConst2.getValue(), irLabel.getLabel());
        } else if (var2 instanceof Instruction) {
            if (var1 instanceof IntConst) {
                IntConst intConst1 = (IntConst) var1;
                // lw $t1, <offset>($fp) 加载var2
                lw(Reg.$t1, Reg.$fp, load());
                // beq $t1, $t1, <var1> 交换var1、var2次序
                beq(Reg.$t1, intConst1.getValue(), irLabel.getLabel());
            } else if (var1 instanceof Instruction) {
                // lw $t2, <offset>($fp) 加载var2
                lw(Reg.$t2, Reg.$fp, load());
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
                // beq $t1, $t2, <label>
                beq(Reg.$t1, Reg.$t2, irLabel.getLabel());
            }
        }
        // nop
        nop();
    }

    private void genBr_ne(Instruction irInst) {
        Value var1 = irInst.getOperand(0);
        Value var2 = irInst.getOperand(1);
        mid.Label irLabel = (mid.Label) irInst.getOperand(2);
        if (var2 instanceof IntConst) {
            IntConst intConst2 = (IntConst) var2;
            if (var1 instanceof IntConst) { // var1 var2 都是常数
                IntConst intConst1 = (IntConst) var1;
                // li $t1, <var1>
                li(Reg.$t1,intConst1.getValue());
            } else if (var1 instanceof Instruction) {
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
            }
            // bne $t1, <var2>, <label>
            bne(Reg.$t1, intConst2.getValue(), irLabel.getLabel());
        } else if (var2 instanceof Instruction) {
            if (var1 instanceof IntConst) {
                IntConst intConst1 = (IntConst) var1;
                // lw $t1, <offset>($fp) 加载var2
                lw(Reg.$t1, Reg.$fp, load());
                // bne $t1, $t1, <var1> 交换var1、var2次序
                bne(Reg.$t1, intConst1.getValue(), irLabel.getLabel());
            } else if (var1 instanceof Instruction) {
                // lw $t2, <offset>($fp) 加载var2
                lw(Reg.$t2, Reg.$fp, load());
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
                // bne $t1, $t2, <label>
                bne(Reg.$t1, Reg.$t2, irLabel.getLabel());
            }
        }
        // nop
        nop();
    }

    private void genRet(Instruction irInst) {
        if (irInst.getOperandNum() != 0) {
            // 生成返回值
            Value value = irInst.getOperand(0);
            if (value instanceof IntConst) {
                IntConst intConst = (IntConst) value;
                // li $v0, <ret val>
                li(Reg.$v0, intConst.getValue());
            } else {
                // lw $v0, <offset>($fp)
                int offset = load();
                lw(Reg.$v0, Reg.$fp, offset);
            }
        }
        // jr $ra
        jr(Reg.$ra);
        // nop
        nop();
    }

    private void genPrint(Instruction irInst) {
        // 先计算占用的地址空间的的位置
        int irInstNum = 0;
        for (Value value: irInst.getOperandList()) {
            if (value instanceof Instruction) irInstNum++;
        }
        int stackTop = (temporaryVar.size() << 2) + tVarOffset;
        int irInstNumTmp = irInstNum;
        for (Value value: irInst.getOperandList()) {
            if (value instanceof StrConst) {
                StrConst strConst = (StrConst) value;
                int strNo = MipsCode.getStrNo();
                MipsCode.addAsciiz(new Asciiz(strNo, strConst.getStr()));
                // la $a0, <str>
                la(Reg.$a0, "str_" + strNo);
                // li $v0, 4
                li(Reg.$v0, 4);
                // syscall
                syscall();
            } else if (value instanceof IntConst) {
                IntConst intConst = (IntConst) value;
                // li $a0, <int>
                li(Reg.$a0, intConst.getValue());
                // li $v0, 1
                li(Reg.$v0, 1);
                // syscall
                syscall();
            } else if (value instanceof Instruction) {
                int offset = stackTop - irInstNumTmp * 4;
                irInstNumTmp--;
                // lw $a0, <offset>($fp)
                lw(Reg.$a0, Reg.$fp, offset);
                // li $v0, 1
                li(Reg.$v0, 1);
                // syscall
                syscall();
            }
        }
        // 弹出所有的printf的exp结果
        for (int i = 0; i < irInstNum; i++) {
            temporaryVar.pop();
        }
    }

    private void genGetInt(Instruction irInst) {
        // li $v0, 5
        li(Reg.$v0, 5);
        // syscall
        syscall();
        // sw $v0, <offset>($fp)
        int offset = store(irInst.getRegister());
        sw(Reg.$v0, Reg.$fp, offset);
    }

    private void genNop() {
        nop();
    }

    private int store(Register irReg) {
        temporaryVar.push(irReg);
        return (temporaryVar.size() << 2) - 4 + tVarOffset;
    }

    private int load() {
        temporaryVar.pop();
        return (temporaryVar.size() << 2) + tVarOffset;
    }

    private void saveTheSite(Reg[] regs) {
        // addi $sp, $sp, -<num of regs>*4
        add(Reg.$sp, Reg.$sp, -regs.length << 2);
        // 保存寄存器
        int offset = 4;
        for (Reg reg: regs) {
            // sw $<reg>, <offset>($sp)
            sw(reg, Reg.$sp, offset);
            offset += 4;
        }
    }

    private void restoreTheSite(Reg[] regs) {
        // 恢复寄存器
        int offset = 4;
        for (Reg reg: regs) {
            // lw $<reg>, <offset>($sp)
            lw(reg, Reg.$sp, offset);
            offset += 4;
        }
        // addi $sp, $sp, <num of regs>*4
        add(Reg.$sp, Reg.$sp, regs.length << 2);
    }

    private void commutativeOperation(Instruction irInst, Mnemonic rr, Mnemonic ri) {
        Value var1 = irInst.getOperand(0);
        Value var2 = irInst.getOperand(1);
        if (var2 instanceof IntConst) {
            IntConst intConst2 = (IntConst) var2;
            if (var1 instanceof IntConst) { // var1 var2 都是常数
                IntConst intConst1 = (IntConst) var1;
                // li $t1, <var1>
                li(Reg.$t1,intConst1.getValue());
            } else if (var1 instanceof Instruction) {
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
            }
            // op $t0, $t1, <var2>
            operation(ri, Reg.$t0, Reg.$t1, intConst2.getValue());
        } else if (var2 instanceof Instruction) {
            if (var1 instanceof IntConst) {
                IntConst intConst1 = (IntConst) var1;
                // lw $t1, <offset>($fp) 加载var2
                lw(Reg.$t1, Reg.$fp, load());
                // op $t0, $t1, <var1> 交换var1、var2次序
                operation(ri, Reg.$t0, Reg.$t1, intConst1.getValue());
            } else if (var1 instanceof Instruction) {
                // lw $t2, <offset>($fp) 加载var2
                lw(Reg.$t2, Reg.$fp, load());
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
                // op $t0, $t1, $t2
                operation(rr, Reg.$t0, Reg.$t1, Reg.$t2);
            }
        }
        // sw $t0, <offset>($fp)
        int offset = store(irInst.getRegister());
        sw(Reg.$t0, Reg.$fp, offset);
    }

    private void unCommutativeOperation(Instruction irInst, Mnemonic rr, Mnemonic ri) {
        Value var1 = irInst.getOperand(0);
        Value var2 = irInst.getOperand(1);
        if (var2 instanceof IntConst) {
            IntConst intConst2 = (IntConst) var2;
            if (var1 instanceof IntConst) { // var1 var2 都是常数
                IntConst intConst1 = (IntConst) var1;
                // li $t1, <var1>
                li(Reg.$t1, intConst1.getValue());
            } else if (var1 instanceof Instruction) {
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
            }
            // op $t0, $t1, <var2>
            operation(ri, Reg.$t0, Reg.$t1, intConst2.getValue());
        } else if (var2 instanceof Instruction) {
            if (var1 instanceof IntConst) {
                IntConst intConst1 = (IntConst) var1;
                // lw $t2, <offset>($fp) 加载var2
                lw(Reg.$t2, Reg.$fp, load());
                // li $t1, <var1>
                li(Reg.$t1, intConst1.getValue());
            } else if (var1 instanceof Instruction) {
                // lw $t2, <offset>($fp) 加载var2
                lw(Reg.$t2, Reg.$fp, load());
                // lw $t1, <offset>($fp) 加载var1
                lw(Reg.$t1, Reg.$fp, load());
            }
            // op $t0, $t1, $t2
            operation(rr, Reg.$t0, Reg.$t1, Reg.$t2);
        }
        // sw $t0, <offset>($fp)
        int offset = store(irInst.getRegister());
        sw(Reg.$t0, Reg.$fp, offset);
    }

    private void operation(Mnemonic rr, Reg rd, Reg rs, Reg rt) {
        switch (rr) {
            case add: add(rd, rs, rt); break;
            case sub: sub(rd, rs, rt); break;
            case mul: mul(rd, rs, rt); break;
            case div: div(rd, rs, rt); break;
            case rem: rem(rd, rs, rt); break;
            case and: and(rd, rs, rt); break;
            case or: or(rd, rs, rt); break;
            case seq: seq(rd, rs, rt); break;
            case sgt: sgt(rd, rs, rt); break;
            case sge: sge(rd, rs, rt); break;
//            case slt: slt(rd, rs, rt); break;
            case sle: sle(rd, rs, rt); break;
            case sne: sne(rd, rs, rt); break;
            default: nop();
        }
    }

    private void operation(Mnemonic ri, Reg rd, Reg rs, int imm) {
        switch (ri) {
            case add: add(rd, rs, imm); break;
            case sub: sub(rd, rs, imm); break;
            case mul: mul(rd, rs, imm); break;
            case div: div(rd, rs, imm); break;
            case rem: rem(rd, rs, imm); break;
            case andi: andi(rd, rs, imm); break;
            case ori: ori(rd, rs, imm); break;
            case seq: seq(rd, rs, imm); break;
            case sgt: sgt(rd, rs, imm); break;
            case sge: sge(rd, rs, imm); break;
            case sle: sle(rd, rs, imm); break;
            case sne: sne(rd, rs, imm); break;
            default: nop();
        }
    }

    private void li(Reg rd, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.li, rd, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void add(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.add, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void add(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.add, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void sub(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sub, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void sub(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sub, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void mul(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.mul, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void mul(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.mul, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void div(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.div, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void div(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.div, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void rem(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.rem, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void rem(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.rem, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void lw(Reg rt, Reg base, int offset) {
        MipsInst mipsInst = new MipsInst(Mnemonic.lw, rt, base, new Immediate(offset));
        MipsCode.addAsm(mipsInst);
    }

    private void sw(Reg rt, Reg base, int offset) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sw, rt, base, new Immediate(offset));
        MipsCode.addAsm(mipsInst);
    }

    private void j(String label) {
        MipsInst mipsInst = new MipsInst(Mnemonic.j, new Label(label));
        MipsCode.addAsm(mipsInst);
    }

    private void jal(String label) {
        MipsInst mipsInst = new MipsInst(Mnemonic.jal, new Label(label));
        MipsCode.addAsm(mipsInst);
    }

    private void la(Reg rd, String label) {
        MipsInst mipsInst = new MipsInst(Mnemonic.la, rd, new Label(label));
        MipsCode.addAsm(mipsInst);
    }

    private void jr(Reg rs) {
        MipsInst mipsInst = new MipsInst(Mnemonic.jr, rs);
        MipsCode.addAsm(mipsInst);
    }

    private void syscall() {
        MipsInst mipsInst = new MipsInst(Mnemonic.syscall);
        MipsCode.addAsm(mipsInst);
    }

    private void seq(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.seq, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void seq(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.seq, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void sgt(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sgt, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void sgt(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sgt, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void sge(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sge, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void sge(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sge, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void slt(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.slt, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void sle(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sle, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void sle(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sle, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void sne(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sne, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void sne(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sne, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void and(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.and, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void andi(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.andi, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void or(Reg rd, Reg rs, Reg rt) {
        MipsInst mipsInst = new MipsInst(Mnemonic.or, rd, rs, rt);
        MipsCode.addAsm(mipsInst);
    }

    private void ori(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.ori, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void beq(Reg rs, Reg rt, String label) {
        MipsInst mipsInst = new MipsInst(Mnemonic.beq, rs, rt, new Label(label));
        MipsCode.addAsm(mipsInst);
    }

    private void beq(Reg rs, int imm, String label) {
        MipsInst mipsInst = new MipsInst(Mnemonic.beq, rs, new Immediate(imm), new Label(label));
        MipsCode.addAsm(mipsInst);
    }

    private void bne(Reg rs, Reg rt, String label) {
        MipsInst mipsInst = new MipsInst(Mnemonic.bne, rs, rt, new Label(label));
        MipsCode.addAsm(mipsInst);
    }

    private void bne(Reg rs, int imm, String label) {
        MipsInst mipsInst = new MipsInst(Mnemonic.bne, rs, new Immediate(imm), new Label(label));
        MipsCode.addAsm(mipsInst);
    }

    private void sll(Reg rd, Reg rs, int imm) {
        MipsInst mipsInst = new MipsInst(Mnemonic.sll, rd, rs, new Immediate(imm));
        MipsCode.addAsm(mipsInst);
    }

    private void nop() {
        MipsInst mipsInst = new MipsInst(Mnemonic.nop);
        MipsCode.addAsm(mipsInst);
    }
}
