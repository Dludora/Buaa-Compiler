package front.ASD;

import Tools.Pair;
import mid.InstList;

import java.util.ArrayList;

public class ConstExp implements ASDNode{       // 未完成getValue
    private final AddExp addExp;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
        asdNodes.add(addExp);
    }

    @Override
    public void printInfo() {
        addExp.printInfo();
        System.out.println("<ConstExp>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Pair<Boolean, Integer> getValue() {
        return addExp.getValue();
    }

    public InstList getInstList() {
        return instList;
    }

    @Override
    public void genCode() {
        addExp.genCode();

        // 生成中间代码
        instList.addAll(addExp.getInstList());
    }
}
