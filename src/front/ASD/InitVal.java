package front.ASD;

import Tools.Pair;
import mid.InstList;

import java.util.ArrayList;

public class InitVal implements ASDNode{
    public enum Type {
        mulInitVal,
        Exp
    }
    private final Type type;
    private final ArrayList<Exp> expList = new ArrayList<>();
    private final ArrayList<ASDNode> asdNodes;

    public InitVal(Type type, ArrayList<ASDNode> asdNodes) {
        this.type = type;
        this.asdNodes = asdNodes;
    }

    @Override
    public void printInfo() {
        if (type.equals(Type.Exp)) {
            asdNodes.get(0).printInfo();
        } else {
            System.out.println("LBRACE {");
            boolean flag = false;
            for (ASDNode asdNode : asdNodes) {
                if (flag) {
                    System.out.println("COMMA ,");
                }
                asdNode.printInfo();
                flag = true;
            }
            System.out.println("RBRACE }");
        }
        System.out.println("<InitVal>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    public Integer getDimension() {
        if (type.equals(Type.Exp)) {
            return 0;
        } else {
            return asdNodes.size();
        }
    }

    public Pair<Boolean, ArrayList<Integer>> getInitValue() {
        Pair<Boolean, ArrayList<Integer>> value = new Pair<>(true, new ArrayList<>());
        if (this.type.equals(InitVal.Type.Exp)) {
            if (((Exp) asdNodes.get(0)).getValue().getFirst()) {
                value.getSecond().add(((Exp) asdNodes.get(0)).getValue().getSecond());
            } else {
                value.setFirst(false);
            }
        } else {
            for (ASDNode asdNode : asdNodes) {
                if (((InitVal) asdNode).getInitValue().getFirst()) {
                    value.getSecond().addAll(((InitVal) asdNode).getInitValue().getSecond());
                } else {
                    value.setFirst(false);
                    break;
                }
            }
        }

        return value;
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    @Override
    public void genCode() {
        if (type.equals(Type.Exp)) {
            Exp exp = (Exp) asdNodes.get(0);
            // 特殊处理
            expList.add(exp);
        } else {
            for (ASDNode asdNode: asdNodes) {
                InitVal initVal = (InitVal) asdNode;
                initVal.genCode();
                expList.addAll(initVal.getExpList());
            }
        }
    }

    public ArrayList<Exp> getExpList() {
        return expList;
    }
}
