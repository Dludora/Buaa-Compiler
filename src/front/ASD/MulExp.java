package front.ASD;

import Tools.Pair;
import front.Symbols;
import front.Token;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class MulExp implements ASDNode{
    private final ArrayList<UnaryExp> unaryExps;
    private final ArrayList<Token> Ops;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public MulExp(ArrayList<UnaryExp> unaryExps, ArrayList<Token> ops) {
        this.unaryExps = unaryExps;
        Ops = ops;
        asdNodes.addAll(unaryExps);
    }

    @Override
    public void printInfo() {
        int i = 0;
        for (UnaryExp unaryExp: unaryExps) {
            unaryExp.printInfo();
            System.out.println("<MulExp>");
            if (i < Ops.size()) {
                System.out.println(Ops.get(i).toString());
            }
            i += 1;
        }
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public String getName() {
        if (Ops.isEmpty()) {
            return this.unaryExps.get(0).getName();
        }
        return "@MulExp";
    }

    public Pair<Boolean, Integer> getValue() {
        boolean flag = false;
        Integer value = 0;
        if (unaryExps.get(0).getValue().getFirst()) {
            value = unaryExps.get(0).getValue().getSecond();
            flag = true;
            for (int i = 1; i < unaryExps.size(); i++) {
                flag = flag && unaryExps.get(i).getValue().getFirst();
                if (!flag) {
                    break;
                }
                if (Ops.get(i-1).getTokenClass().equals(Symbols.MULT)) {
                    value = value * unaryExps.get(i).getValue().getSecond();
                } else if (Ops.get(i-1).getTokenClass().equals(Symbols.DIV)){
                    value = value / unaryExps.get(i).getValue().getSecond();
                } else if (Ops.get(i-1).getTokenClass().equals(Symbols.MOD)){
                    value = value % unaryExps.get(i).getValue().getSecond();
                } else if (Ops.get(i-1).getTokenClass().equals(Symbols.BITAND)) {
                    value = value & unaryExps.get(i).getValue().getSecond();
                }
            }

        }

        return new Pair<>(flag, value);
    }

    public int getDimension() {
        if (!Ops.isEmpty()) {
            return 0;
        }
        return this.unaryExps.get(0).getDimension();
    }

    public boolean isFunCall() {
        return unaryExps.size() == 1 && unaryExps.get(0).isFunCall();
    }


    @Override
    public void genCode() {
        Instruction inst = new Instruction();
        for (int i = 0; i < unaryExps.size(); i++) {
            UnaryExp unaryExp = unaryExps.get(i);

            if (unaryExp != null) {
                unaryExp.genCode();

                Pair<Boolean, Integer> unaryExpVal = unaryExp.getValue();
                // 生成中间代码
                if (!unaryExpVal.getFirst())  instList.addAll(unaryExp.getInstList());
                Value operand = unaryExpVal.getFirst() ? new IntConst(unaryExpVal.getSecond()) : instList.getLast();
                if (inst.getOperandNum() == 0) {
                    inst.addOperand(operand);
                } else if (inst.getOperandNum() == 1 && i >= 1) {
                    Function function = MidCode.topFunction().getSecond();
                    inst.setRegister(function.distributeReg());
                    inst.setInstSet(genInstSet(Ops.get(i-1)));
                    inst.addOperand(operand);
                    instList.add(inst);
                    inst = new Instruction();
                    inst.addOperand(instList.getLast());
                }
            }
        }
    }

    public InstList getInstList() {
        return instList;
    }

    private InstSet genInstSet(Token op) {
        return op.getTokenClass().equals(Symbols.MOD) ? InstSet.srem :
                op.getTokenClass().equals(Symbols.DIV) ? InstSet.sdiv :
                op.getTokenClass().equals(Symbols.MULT) ? InstSet.mul : InstSet.and;
    }
}
