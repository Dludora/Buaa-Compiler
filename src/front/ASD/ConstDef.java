package front.ASD;

import SymTable.LValSymItem;
import SymTable.SymTabStack;
import SymTable.SymType;
import Tools.Pair;
import Tools.Utility;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;
import java.util.Collections;

public class ConstDef implements ASDNode {
    private final Ident ident;
    private final ArrayList<ConstExp> constExps;
    private final ConstInitVal constInitVal;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public ConstDef(Ident ident, ArrayList<ConstExp> constExps, ConstInitVal constInitVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.constInitVal = constInitVal;
        asdNodes.add(ident);
        asdNodes.addAll(constExps);
        asdNodes.add(constInitVal);
    }

    @Override
    public void printInfo() {
        ident.printInfo();
        for (ConstExp constExp : constExps) {
            System.out.println("LBRACK [");
            constExp.printInfo();
            System.out.println("RBRACK ]");
        }
        System.out.println("ASSIGN =");
        constInitVal.printInfo();
        System.out.println("<ConstDef>");
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

    public int getDimension() {
        return constExps.size();
    }

    public Pair<Boolean, ArrayList<Integer>> getArrayShape() {
        ArrayList<Integer> shape = new ArrayList<>();
        for (ConstExp exp : constExps) {
            if (exp.getValue().getFirst()) {
                shape.add(exp.getValue().getSecond());
            } else {
                return new Pair<>(false, shape);
            }
        }
        return new Pair<>(true, shape);
    }

    public Pair<Boolean, ArrayList<Integer>> getValue() {
        return constInitVal.getInitValue();
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
            lValSymItem = new LValSymItem(ident.getName(), SymType.LVal, true, shape.getSecond());
        } else {
            lValSymItem = new LValSymItem(ident.getName(), SymType.LVal, true, getDimension());
        }
        // TODO: 2022/12/2 获得初始值
        Pair<Boolean, ArrayList<Integer>> value = constInitVal.getInitValue();
        if (value.getFirst()) {
            lValSymItem.setValue(value.getSecond());
        }
        SymTabStack.pushVar(lValSymItem);


        // 如果是数组，生成中间代码
        if (constExps.size() != 0) {
//            ident.genCode();
//            for (ConstExp constExp : constExps) {
//                constExp.genCode();
//            }

            if (!MidCode.topFunction().getFirst()) {
                // 是全局变量
                // 1. 全局分配寄存器
                Instruction inst = new Instruction(InstSet.global);
                inst.setRegister(MidCode.distributeVarReg(getName(), lValSymItem));
                // 2. 获取变量数组类型
                ArrayType arrayType = new ArrayType(shape.getSecond());
                inst.addOperand(arrayType);
                // 3. 可以拿到初始值;
                lValSymItem.setZeroInit(false);
                constInitVal.genCode();
                for (Integer integer : value.getSecond()) {
                    mid.IntConst intConst = new mid.IntConst(integer);
                    inst.addOperand(intConst);
                }

                MidCode.add(inst);
                lValSymItem.setDeclInst(inst);
            } else {
                {
                    // 是局部变量
                    // 1. 函数分配寄存器
                    Instruction inst = new Instruction(InstSet.alloca);
                    Function topFunc = MidCode.topFunction().getSecond();
                    VarReg varReg = topFunc.distributeVarReg(getName(), lValSymItem);
                    inst.setRegister(varReg);
                    // 2. 获取要分配的空间大小
                    mid.IntConst intConst = new mid.IntConst(lValSymItem.getSize());
                    inst.addOperand(intConst);
                    lValSymItem.setAdr(topFunc.distributeAdr(lValSymItem.getSize()));   // 分配地址
                    // 加入Function的basicBlock
                    BasicBlock basicBlock = MidCode.topFunction().getSecond().getLastBasicBlock();
                    basicBlock.addInst(inst);
                    lValSymItem.setDeclInst(inst);
                    constInitVal.genCode();
                    lValSymItem.setZeroInit(false);
                    ArrayList<Integer> index = new ArrayList<>(Collections.nCopies(shape.getSecond().size(), 0));
                    ArrayList<ConstExp> constExpList = constInitVal.getConstExpList();
                    InstList instList = new InstList();
                    for (ConstExp constExp : constExpList) {
                        inst = new Instruction(InstSet.getelementptr);
                        inst.setRegister(topFunc.distributeReg());
                        inst.addOperand(new ArrayType(lValSymItem.getShape()));
                        inst.addOperand(lValSymItem.getDeclInst());
                        for (Integer val : index) inst.addOperand(new mid.IntConst(val));
                        instList.add(inst);

                        // 初始值
                        constExp.genCode();
                        Pair<Boolean, Integer> expVal = constExp.getValue();
                        if (expVal.getFirst()) {
                            inst = new Instruction(InstSet.store);
                            inst.addOperand(instList.getLast());
                            inst.addOperand(new IntConst(expVal.getSecond()));
                            instList.add(inst);
                        } else {
                            Instruction addr = instList.getLast();
                            instList.addAll(constExp.getInstList());
                            inst = new Instruction(InstSet.store);
                            inst.addOperand(addr);
                            inst.addOperand(instList.getLast());
                            instList.add(inst);
                        }
                        Utility.nextIndex(shape.getSecond(), index);
                    }

                    basicBlock.addAllInst(instList);

                }
            }
        }
    }
}
