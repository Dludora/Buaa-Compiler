package front.ASD;

import Tools.Pair;
import front.Symbols;
import front.Token;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class RelExp implements ASDNode {
    private final ArrayList<AddExp> addExps;
    private final ArrayList<Token> Ops;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public RelExp(ArrayList<AddExp> addExps, ArrayList<Token> ops) {
        this.addExps = addExps;
        Ops = ops;
        asdNodes.addAll(addExps);
    }

    @Override
    public void printInfo() {
        int i = 0;
        for (AddExp addExp : addExps) {
            addExp.printInfo();
            System.out.println("<RelExp>");
            if (i < Ops.size()) {
                System.out.println(Ops.get(i).toString());
            }
            i += 1;
        }
    }

    @Override
    public void linkWithSymbolTable() {

    }

    public InstList getInstList() {
        return instList;
    }

    public Pair<Boolean, Integer> getValue() throws Error {
        boolean flag = false;
        Integer value = 0;
        if (addExps.get(0).getValue().getFirst()) {
            value = addExps.get(0).getValue().getSecond();
            flag = true;
            for (int i = 1; i < addExps.size(); i++) {
                flag = flag && addExps.get(i).getValue().getFirst();
                if (!flag) {
                    break;
                }
                if (Ops.get(i - 1).getTokenClass().equals(Symbols.LSS)) {     // a < b
                    value = value < addExps.get(i).getValue().getSecond() ? 1 : 0;
                } else if (Ops.get(i - 1).getTokenClass().equals(Symbols.LEQ)) {   // a <= b
                    value = value <= addExps.get(i).getValue().getSecond() ? 1 : 0;
                } else if (Ops.get(i - 1).getTokenClass().equals(Symbols.GRE)) {
                    value = value > addExps.get(i).getValue().getSecond() ? 1 : 0;
                } else if (Ops.get(i - 1).getTokenClass().equals(Symbols.GEQ)) {
                    value = value >= addExps.get(i).getValue().getSecond() ? 1 : 0;
                }
            }
        }

        return new Pair<>(flag, value);
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }


    public String reverseOp(String op) {
        return op.equals(">=") ? "<" :
                op.equals(">") ? "<=" :
                        op.equals("<=") ? ">" :
                                op.equals("<") ? ">=" : "no!!!!";
    }

    @Override
    public void genCode() {
        Function topFunc = MidCode.topFunction().getSecond();
        Instruction inst = new Instruction();
        int i = 0;
        for (AddExp addExp : addExps) {
            addExp.genCode();

            // 生成中间代码
            Pair<Boolean, Integer> addExpVal = addExp.getValue();
            if (!addExpVal.getFirst())  instList.addAll(addExp.getInstList());
            Value operand = addExpVal.getFirst() ? new IntConst(addExpVal.getSecond()) :
                    instList.getLast();
            if (inst.getOperandNum() == 0) {
                inst.addOperand(operand);
            } else if (inst.getOperandNum() == 1){
                inst.setRegister(topFunc.distributeReg());
                inst.setInstSet(genIRMnemonic(Ops.get(i++)));
                inst.addOperand(operand);
                instList.add(inst);
                inst = new Instruction();
                inst.addOperand(instList.getLast());
            }
        }
    }

    private InstSet genIRMnemonic(Token op) {
        return op.getTokenClass().equals(Symbols.LSS) ? InstSet.icmp_lt :
               op.getTokenClass().equals(Symbols.LEQ) ? InstSet.icmp_le :
               op.getTokenClass().equals(Symbols.GRE) ? InstSet.icmp_gt :
               op.getTokenClass().equals(Symbols.GEQ) ? InstSet.icmp_ge : InstSet.nop;
    }
}
