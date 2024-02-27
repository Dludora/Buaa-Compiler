package front.ASD;

import mid.*;

import java.util.ArrayList;

public class LOrExp implements ASDNode{
    private final ArrayList<LAndExp> lAndExps;
    private Label trueLabel = new Label("@NULL");
    private Label falseLabel = new Label("@NULL");
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public LOrExp(ArrayList<LAndExp> lAndExps) {
        this.lAndExps = lAndExps;
        asdNodes.addAll(lAndExps);
    }

    @Override
    public void printInfo() {
        lAndExps.get(0).printInfo();
        System.out.println("<LOrExp>");
        for (int i = 1; i < lAndExps.size(); i++) {
            System.out.println("OR ||");
            lAndExps.get(i).printInfo();
            System.out.println("<LOrExp>");
        }
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public void setTrueLabel(Label trueLabel) {
        this.trueLabel = trueLabel;
    }

    public void setFalseLabel(Label falseLabel) {
        this.falseLabel = falseLabel;
    }

    @Override
    public void genCode() {
        Function topFunc = MidCode.topFunction().getSecond();
        if (lAndExps.size() == 1) {
            lAndExps.get(0).setTrueLabel(trueLabel);
            lAndExps.get(0).setFalseLabel(falseLabel);
            lAndExps.get(0).genCode();
        } else {
            for (LAndExp lAndExp: lAndExps) {
                Label orLabel = topFunc.distributeLabel("or");
                lAndExp.setTrueLabel(trueLabel);
                lAndExp.setFalseLabel(orLabel);
                lAndExp.genCode();
                // 是跳转指令，新建基本块
                topFunc.addBasicBlock(new BasicBlock(orLabel));
            }
            // 如果前面的不跳转，则跳转到falseLabel
            Instruction inst = new Instruction(InstSet.br);
            inst.addOperand(falseLabel);
            topFunc.getLastBasicBlock().addInst(inst);
        }
    }
}
