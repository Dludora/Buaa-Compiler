package front.ASD;

import SymTable.*;
import Tools.Pair;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class LVal implements ASDNode {
    public Ident ident;
    public ArrayList<Exp> exps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    private final InstList instList = new InstList();
    private Pair<Boolean, Integer> lValVal = null;

    public LVal(Ident ident, ArrayList<Exp> exps) {
        this.ident = ident;
        this.exps = exps;
        asdNodes.add(ident);
        asdNodes.addAll(exps);
    }

    @Override
    public void printInfo() {
        ident.printInfo();
        for (Exp exp : exps) {
            System.out.println("LBRACK [");
            exp.printInfo();
            System.out.println("RBRACK ]");
        }
        System.out.println("<LVal>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public int getDimension() {
        // TODO: 2022/12/1 获取维度
        VarSymItem varSymItem = SymTabStack.getVar(getName());
        if (varSymItem != null) {
            return varSymItem.getDimension() - exps.size();
        }
        return exps.size();
    }

    public boolean isPointer() {
        VarSymItem varSymItem = SymTabStack.getVar(getName());
        if (varSymItem != null) {
            return varSymItem.getDimension() != exps.size();
        }
        return false;
    }

    public Pair<Boolean, Integer> getValue() {
        if (lValVal == null) {
            lValVal = new Pair<>(false, 0);
            VarSymItem varSymItem = SymTabStack.getVar(ident.getName());
            if (varSymItem == null) {
                return lValVal;
            } else {
                if (varSymItem.getSymType().equals(SymType.Param)) {
                    return lValVal;
                } else {
                    LValSymItem lValSymItem = (LValSymItem) varSymItem;
                    if (!lValSymItem.isConst()) {
                        return lValVal;
                    } else {
                        ArrayList<Integer> index = new ArrayList<>();
                        boolean flag = true;
                        for (Exp exp: exps) {
                            if (!flag) {
                                break;
                            }
                            Pair<Boolean, Integer> expVal = exp.getValue();
                            if (!expVal.getFirst()) {
                                flag = false;
                            } else {
                                index.add(expVal.getSecond());
                            }
                        }
                        if (flag) {
                            lValVal.setFirst(true);
                            lValVal.setSecond(lValSymItem.getValue(index));
                            return lValVal;
                        } else {
                            return lValVal;
                        }
                    }
                }

            }

        }
        return lValVal;
    }

    public String getName() {
        return this.ident.getName();
    }

    @Override
    public void genCode() {
        // TODO: 2022/11/29 未定义的名字 c
        if (SymTabStack.getVar(ident.getName()) == null) {
            ErrorRecorder.putError(ErrorType.c, ident.getLineNum());
        }
        ident.genCode();
        VarSymItem varSymItem = SymTabStack.getVar(ident.getName());

        if (lValVal == null) {
            lValVal = new Pair<>(false, 0);
        }
        ArrayList<Value> subscript = new ArrayList<>();
        boolean valid = true;
        for (Exp exp : exps) {
            exp.genCode();

            // 生成中间代码
            Pair<Boolean, Integer> expVal = exp.getValue();
            if (expVal.getFirst()) { // 可以直接获取值
                subscript.add(new IntConst(exp.getValue().getSecond()));
            } else {                         // 不能直接获取值，需要生成指令
                valid = false;
                InstList expInstList = exp.getInstList();
                instList.addAll(expInstList);
                subscript.add(expInstList.getLast());
            }
        }

        assert varSymItem != null;
        // 如果该变量是常量并且可以直接拿到值
        if (valid && (varSymItem.getSymType().equals(SymType.LVal) && ((LValSymItem) varSymItem).isConst())) {
            ArrayList<Integer> index = new ArrayList<>();
            for (Value value : subscript) {
                IntConst intConst = (IntConst) value;
                index.add(intConst.getValue());
            }
            lValVal.setFirst(true);
            LValSymItem lValSymItem = (LValSymItem) varSymItem;
            lValVal.setSecond(lValSymItem.getValue(index));
        } else {
            // 获取变量数组形状
            ArrayType arrayType = new ArrayType(varSymItem.getShape());
            Function topFunc = MidCode.topFunction().getSecond();
            Instruction inst = new Instruction(InstSet.getelementptr);
            inst.setRegister(topFunc.distributeReg());
            inst.addOperand(arrayType);
            inst.addOperand(varSymItem.getDeclInst());
            for (Value value : subscript) {
                inst.addOperand(value);
            }
            instList.add(inst);
        }
    }

    public InstList getInstList() {
        return instList;
    }
}
