package front.ASD;

import Tools.Pair;
import mid.InstList;

import java.util.ArrayList;

public class Exp implements ASDNode{
    private final AddExp addExp;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Exp(AddExp addExp) {
        this.addExp = addExp;
        asdNodes.add(addExp);
    }

    @Override
    public void printInfo() {
        addExp.printInfo();
        System.out.println("<Exp>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public String getName() {
        return addExp.getName();
    }

    public Pair<Boolean, Integer> getValue() {
        return this.addExp.getValue();
    }

    public boolean isFunCall() {
        return this.addExp.isFunCall();
    }

    public Integer getDimension() {
        return addExp.getDimension();
    }

    public InstList getInstList() {
        return instList;
    }

    @Override
    public void genCode() {
        addExp.genCode();
        Pair<Boolean, Integer> addExpVal = addExp.getValue();

        // 生成中间代码
        if (!addExpVal.getFirst()) instList.addAll(addExp.getInstList());
    }
}
