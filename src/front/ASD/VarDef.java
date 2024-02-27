package front.ASD;

import SymTable.*;
import Tools.Pair;
import Tools.Utility;
import Tools.VarScope;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;
import java.util.Collections;

public class VarDef implements ASDNode {
    public enum Type {
        normal,
        getint
    }
    public final Ident ident;
    private final ArrayList<ConstExp> constExps;
    private final InitVal initVal;
    private Type type = Type.normal;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public VarDef(Ident ident, ArrayList<ConstExp> constExps) {
        this.ident = ident;
        this.constExps = constExps;
        this.initVal = null;
        asdNodes.add(ident);
        asdNodes.addAll(constExps);
    }

    public VarDef(Ident ident, ArrayList<ConstExp> constExps, InitVal initVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.initVal = initVal;
        asdNodes.add(ident);
        asdNodes.addAll(constExps);
        asdNodes.add(initVal);
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void printInfo() {
        ident.printInfo();
        for (ConstExp constExp : constExps) {
            System.out.println("LBRACK [");
            constExp.printInfo();
            System.out.println("RBRACK ]");
        }
        if (initVal != null) {
            System.out.println("ASSIGN =");
            initVal.printInfo();
        }
        System.out.println("<VarDef>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public String getName() {
        return ident.getName();
    }

    public InitVal getInitVal() {
        return initVal;
    }

    public Ident getIdent() {
        return ident;
    }

    public int getDimension() {
        return this.constExps.size();
    }

    public Pair<Boolean, ArrayList<Integer>> getArrayShape() {
        ArrayList<Integer> shape = new ArrayList<>();
        for (ConstExp constExp : constExps) {
            if (constExp.getValue().getFirst()) {
                shape.add(constExp.getValue().getSecond());
            } else {
                return new Pair<>(false, shape);
            }
        }
        return new Pair<>(true, shape);
    }

    @Override
    public void genCode() {
        // TODO: 2022/11/29 重定义的名字 b
        if (SymTabStack.varDefined(ident.getName())) {
            ErrorRecorder.putError(ErrorType.b, ident.getLineNum());
        }
        // TODO: 2022/11/29 完善shape
        Pair<Boolean, ArrayList<Integer>> shape = getArrayShape();
        LValSymItem lValSymItem;
        if (shape.getFirst()) {     // 可以直接拿到shape(通常情况下可以直接拿到)
            lValSymItem = new LValSymItem(ident.getName(), SymType.LVal, false, shape.getSecond());
        } else {
            lValSymItem = new LValSymItem(ident.getName(), SymType.LVal, false, getDimension());
        }
        SymTabStack.pushVar(lValSymItem);    // 入栈

        // TODO: 2022/12/3 生成中间代码
//        ident.genCode();
//        for (ConstExp constExp : constExps) {
//            constExp.genCode();
//        }
        if (!MidCode.topFunction().getFirst()) {
            // 是全局变量                    完成!
            // 1. 全局分配寄存器
            Instruction inst = new Instruction(InstSet.global);
            inst.setRegister(MidCode.distributeVarReg(getName(), lValSymItem));
            // 2. 获取变量数组类型
            ArrayType arrayType = new ArrayType(shape.getSecond());
            inst.addOperand(arrayType);
            if (initVal != null) {
                // 3. 可以拿到初始值;
                lValSymItem.setZeroInit(false);
                initVal.genCode();
                Pair<Boolean, ArrayList<Integer>> value = initVal.getInitValue();
                if (value.getFirst()) { // 可有可无
                    lValSymItem.setValue(value.getSecond());
                }
                for (Integer integer : value.getSecond()) {
                    mid.IntConst intConst = new mid.IntConst(integer);
                    inst.addOperand(intConst);
                }
            }
            MidCode.add(inst);
            lValSymItem.setDeclInst(inst);
        } else {
            // 是局部变量
            // 1. 函数分配寄存器
            Instruction inst = new Instruction(InstSet.alloca);
            Function topFunc = MidCode.topFunction().getSecond();
            VarReg varReg = topFunc.distributeVarReg(getName(), lValSymItem);
            inst.setRegister(varReg);
            // 2. 获取要分配的空间大小
            mid.IntConst intConst = new IntConst(lValSymItem.getSize());
            inst.addOperand(intConst);
            lValSymItem.setAdr(topFunc.distributeAdr(lValSymItem.getSize()));   // 分配地址
            // 加入Function的basicBlock
            BasicBlock basicBlock = MidCode.topFunction().getSecond().getLastBasicBlock();
            basicBlock.addInst(inst);
            lValSymItem.setDeclInst(inst);
            if (initVal != null) {
                initVal.genCode();
                lValSymItem.setZeroInit(false);
                ArrayList<Integer> index = new ArrayList<>(Collections.nCopies(shape.getSecond().size(), 0));
                ArrayList<Exp> expList = initVal.getExpList();
                InstList instList = new InstList();
                for (Exp exp : expList) {
                    inst = new Instruction(InstSet.getelementptr);
                    inst.setRegister(topFunc.distributeReg());
                    inst.addOperand(new ArrayType(lValSymItem.getShape()));
                    inst.addOperand(lValSymItem.getDeclInst());
                    for (Integer val : index) inst.addOperand(new IntConst(val));
                    instList.add(inst);

                    // 初始值
                    exp.genCode();
                    Pair<Boolean, Integer> expVal = exp.getValue();
                    if (expVal.getFirst()) {
                        inst = new Instruction(InstSet.store);
                        inst.addOperand(instList.getLast());
                        inst.addOperand(new IntConst(expVal.getSecond()));
                        instList.add(inst);
                    } else {
                        Instruction addr = instList.getLast();
                        instList.addAll(exp.getInstList());
                        inst = new Instruction(InstSet.store);
                        inst.addOperand(addr);
                        inst.addOperand(instList.getLast());
                        instList.add(inst);
                    }
                    Utility.nextIndex(shape.getSecond(), index);
                }

                basicBlock.addAllInst(instList);
            } else {
                if (type.equals(Type.getint)) {
                    Instruction inst1 = new Instruction(InstSet.getelementptr);
                    inst1.setRegister(topFunc.distributeReg());
                    inst1.addOperand(new ArrayType(new ArrayList<>()));
                    inst1.addOperand(basicBlock.getLast());
                    basicBlock.addInst(inst1);
                    inst = new Instruction(InstSet.getInt);
                    inst.setRegister(topFunc.distributeReg());
                    Instruction addr = basicBlock.getLast();
                    basicBlock.addInst(inst);
                    inst = new Instruction(InstSet.store);
                    inst.addOperand(addr);
                    inst.addOperand(basicBlock.getLast());
                    basicBlock.addInst(inst);
                }
            }
        }
    }
}
