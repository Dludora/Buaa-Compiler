package front.ASD;

import Tools.Pair;

import java.util.ArrayList;

public class Number implements ASDNode{
    private final IntConst intConst;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Number(IntConst intConst) {
        this.intConst = intConst;
        asdNodes.add(intConst);
    }

    @Override
    public void printInfo() {
        intConst.printInfo();
        System.out.println("<Number>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Pair<Boolean, Integer> getValue() {
        return intConst.getValue();
    }

    @Override
    public void genCode() {
        Pair<Boolean, Integer> numVal = getValue();

    }
}
