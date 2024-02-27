package front.ASD;

import Tools.Pair;
import mid.InstList;
import mid.InstSet;
import mid.Instruction;
import mid.MidCode;

import java.util.ArrayList;

public class PrimaryExp implements ASDNode {         // getValue
    public enum Type {
        Exp, LVal, Number
    }

    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes;
    public Integer value;
    public Type type;

    public PrimaryExp(Type type, ArrayList<ASDNode> asdNodes) {
        this.asdNodes = asdNodes;
        this.type = type;
    }

    @Override
    public void printInfo() {
        if (type == Type.Exp) {
            System.out.println("LPARENT (");
            asdNodes.get(0).printInfo();
            System.out.println("RPARENT )");
        } else if (type == Type.LVal || type == Type.Number) {
            asdNodes.get(0).printInfo();
        }
        System.out.println("<PrimaryExp>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public String getName() {
        if (type.equals(Type.Number)) {
            return null;
        } else if (type.equals(Type.Exp)) {
            return ((Exp) asdNodes.get(0)).getName();
        } else {
            return ((LVal) asdNodes.get(0)).getName();
        }
    }

    public Pair<Boolean, Integer> getValue() throws Error{
        Pair<Boolean, Integer> pair;
        if (value != null) {
            return new Pair<>(true, value);
        }
        if (type.equals(Type.Exp)) {
            pair = ((Exp) asdNodes.get(0)).getValue();
            if (pair.getFirst()) {
                value = pair.getSecond();
            }
            return pair;
        } else if (type.equals(Type.Number)) {
            pair = ((Number) asdNodes.get(0)).getValue();
            if (pair.getFirst()) {
                value = pair.getSecond();
            }
            return pair;
        } else {            // LVal
            pair = ((LVal) asdNodes.get(0)).getValue();
            if (pair.getFirst()) {
                value = pair.getSecond();
            }
            return pair;
        }
    }

    public int getDimension() {
        if (type.equals(Type.LVal)) {
            return ((LVal) asdNodes.get(0)).getDimension();
        } else if (type.equals(Type.Exp)) {
            return ((Exp) asdNodes.get(0)).getDimension();
        } else {
            return 0;
        }
    }

    public boolean isFunCall() {
        return type.equals(Type.Exp) && ((Exp) asdNodes.get(0)).isFunCall();
    }


    @Override
    public void genCode() {
        if (type.equals(Type.LVal)) {
            LVal lVal = (LVal) asdNodes.get(0);
            lVal.genCode();
            Pair<Boolean, Integer> lValVal = lVal.getValue();
            if (!lValVal.getFirst()) {
                instList.addAll(lVal.getInstList());
                // 判断是值还是指针
                if (!lVal.isPointer()) {
                    Instruction inst = new Instruction(InstSet.load);
                    inst.setRegister(MidCode.topFunction().getSecond().distributeReg());
                    inst.addOperand(instList.getLast());

                    instList.add(inst);
                }
            }



        } else if (type.equals(Type.Exp)) {
            Exp exp = (Exp) asdNodes.get(0);
            exp.genCode();

            Pair<Boolean, Integer> expVal = exp.getValue();
            // 生成中间代码
            if (!expVal.getFirst()) instList.addAll(exp.getInstList());
        } else if (type.equals(Type.Number)){
            Number number = (Number) asdNodes.get(0);
            number.genCode();
        }
    }

    public InstList getInstList() {
        return instList;
    }
}
