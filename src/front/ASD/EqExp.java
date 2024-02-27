package front.ASD;

import Tools.Pair;
import front.Symbols;
import front.Token;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class EqExp implements ASDNode{
    private ArrayList<Token> Ops;
    private ArrayList<RelExp> relExps;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public EqExp(ArrayList<Token> ops, ArrayList<RelExp> relExps) {
        Ops = ops;
        this.relExps = relExps;
        asdNodes.addAll(relExps);
    }

    @Override
    public void printInfo() {
        relExps.get(0).printInfo();
        System.out.println("<EqExp>");
        for (int i = 1; i < Ops.size(); i++) {
            System.out.println(Ops.get(i).toString());
            relExps.get(i+1).printInfo();
            System.out.println("<EqExp>");
        }
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Pair<Boolean, Integer> getValue() throws Error {
        boolean flag = false;
        Integer value = 0;
        if (relExps.get(0).getValue().getFirst()) {
            value = relExps.get(0).getValue().getSecond();
            flag = true;
            for (int i = 1; i < relExps.size(); i++) {
                flag = flag && relExps.get(i).getValue().getFirst();
                if (!flag) {
                    break;
                }
                if (Ops.get(i-1).getTokenClass().equals(Symbols.EQL)) {     // a == b
                    value = value.equals(relExps.get(i).getValue().getSecond()) ? 1 : 0;
                } else if (Ops.get(i-1).getTokenClass().equals(Symbols.NEQ)){   // a != b
                    value = !value.equals(relExps.get(i).getValue().getSecond()) ? 1 : 0;
                }
            }
        }

        return new Pair<>(flag, value);
    }

    public String reverseOp(String op) {
        return op.equals("==") ? "!=" :
                op.equals("!=") ? "==" : "no!!!!";
    }

    public InstList getInstList() {
        return instList;
    }

    @Override
    public void genCode() {
        Instruction inst = new Instruction();
        Function topFunc = MidCode.topFunction().getSecond();
        int i = 0;
        for (RelExp relExp: relExps) {
            relExp.genCode();

            // 生成中间代码
            Pair<Boolean, Integer> relExpVal = relExp.getValue();
            if (!relExpVal.getFirst())  instList.addAll(relExp.getInstList());
            Value operand = relExpVal.getFirst() ? new IntConst(relExpVal.getSecond()) :
                    instList.getLast();
            if (inst.getOperandNum() == 0) {
                inst.addOperand(operand);
            } else if (inst.getOperandNum() == 1) {
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
        return op.getTokenClass().equals(Symbols.EQL) ? InstSet.icmp_eq :
               op.getTokenClass().equals(Symbols.NEQ) ? InstSet.icmp_neq : InstSet.nop;
    }
}
