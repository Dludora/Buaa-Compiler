package front.ASD;

import mid.Label;

import java.util.ArrayList;

public class Cond implements ASDNode{
    private final LOrExp lOrExp;
    private Label trueLabel = new Label("@NULL");
    private Label falseLabel = new Label("@NULL");
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
        asdNodes.add(lOrExp);
    }

    @Override
    public void printInfo() {
        lOrExp.printInfo();
        System.out.println("<Cond>");
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
        lOrExp.setTrueLabel(trueLabel);
        lOrExp.setFalseLabel(falseLabel);
        lOrExp.genCode();
    }
}
