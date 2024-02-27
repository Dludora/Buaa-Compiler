package front.ASD;

import Tools.Pair;
import front.Symbols;
import front.Token;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class AddExp implements ASDNode {
    private final ArrayList<MulExp> mulExps;
    private final ArrayList<Token> Ops;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public AddExp(ArrayList<MulExp> mulExps, ArrayList<Token> ops) {
        this.mulExps = mulExps;
        Ops = ops;
        asdNodes.addAll(mulExps);
    }

    @Override
    public void printInfo() {
        int i = 0;
        for (MulExp mulExp : mulExps) {
            mulExp.printInfo();
            System.out.println("<AddExp>");
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
            return mulExps.get(0).getName();
        }
        return "@AddExp";
    }

    public Pair<Boolean, Integer> getValue() throws Error {
        boolean flag = false;
        Integer value = 0;
        if (mulExps.get(0).getValue().getFirst()) {
            value = mulExps.get(0).getValue().getSecond();
            flag = true;
            for (int i = 1; i < mulExps.size(); i++) {
                flag = flag && mulExps.get(i).getValue().getFirst();
                if (!flag) {
                    break;
                }
                if (Ops.get(i-1).getTokenClass().equals(Symbols.PLUS)) {
                    value = value + mulExps.get(i).getValue().getSecond();
                } else {
                    value = value - mulExps.get(i).getValue().getSecond();
                }
            }

        }

        return new Pair<>(flag, value);
    }

    public int getDimension() {
        if (!Ops.isEmpty()) {
            return 0;
        }
        return mulExps.get(0).getDimension();
    }

    public boolean isFunCall() {
        return this.mulExps.size() == 1 && mulExps.get(0).isFunCall();
    }


    @Override
    public void genCode() {
        Instruction inst = new Instruction();
        for (int i = 0; i < mulExps.size(); i++) {
            MulExp mulExp = mulExps.get(i);

            if (mulExp != null) {
                mulExp.genCode();

                Pair<Boolean, Integer> mulExpVal = mulExp.getValue();
                // 生成中间代码
                if (!mulExpVal.getFirst())  instList.addAll(mulExp.getInstList());
                Value operand = mulExpVal.getFirst() ? new IntConst(mulExpVal.getSecond()) : instList.getLast();
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
        return op.getTokenClass().equals(Symbols.MINU) ? InstSet.sub : InstSet.add;
    }
}
