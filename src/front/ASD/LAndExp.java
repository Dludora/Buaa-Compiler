package front.ASD;

import Tools.Pair;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class LAndExp implements ASDNode {
    private final ArrayList<EqExp> eqExps;
    private Label trueLabel = new Label("@NULL");
    private Label falseLabel = new Label("@NULL");
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public LAndExp(ArrayList<EqExp> eqExps) {
        this.eqExps = eqExps;
        asdNodes.addAll(eqExps);
    }

    @Override
    public void printInfo() {
        eqExps.get(0).printInfo();
        System.out.println("<LAndExp>");
        for (int i = 1; i < eqExps.size(); i++) {
            System.out.println("AND ||");
            eqExps.get(i).printInfo();
            System.out.println("<LAndExp>");
        }
    }

    @Override
    public void linkWithSymbolTable() {

    }

    public void setTrueLabel(Label trueLabel) {
        this.trueLabel = trueLabel;
    }

    public void setFalseLabel(Label falseLabel) {
        this.falseLabel = falseLabel;
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    @Override
    public void genCode() {
        Function topFunc = MidCode.topFunction().getSecond();
        for (EqExp eqExp : eqExps) {
            eqExp.genCode();
            Pair<Boolean, Integer> eqExpVal = eqExp.getValue();

            if (!eqExpVal.getFirst()) topFunc.getLastBasicBlock().addAllInst(eqExp.getInstList());
            Value operand = eqExpVal.getFirst() ? new IntConst(eqExpVal.getSecond()) :
                                    eqExp.getInstList().getLast();
            Instruction inst = new Instruction(InstSet.br_eq);
            inst.addOperand(operand);
            inst.addOperand(new IntConst(0));
            inst.addOperand(falseLabel);

            topFunc.getLastBasicBlock().addInst(inst);
            topFunc.addBasicBlock(new BasicBlock(topFunc.distributeLabel("and")));
        }

        Instruction inst = new Instruction(InstSet.br);
        inst.addOperand(trueLabel);
        topFunc.getLastBasicBlock().addInst(inst);
    }
}
