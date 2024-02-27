package front.ASD;

import Tools.Pair;

import java.util.ArrayList;

public class ConstInitVal implements ASDNode{
    public enum Type {
        mulInitVal,
        Exp
    }

    private final Type type;
    private final ArrayList<ConstExp> constExpList = new ArrayList<>();
    private final ArrayList<ASDNode> asdNodes;

    public ConstInitVal(Type type, ArrayList<ASDNode> asdNodes) {
        this.asdNodes = asdNodes;
        this.type = type;
    }

    @Override
    public void printInfo() {
        if (this.type.equals(ConstInitVal.Type.Exp)) {
            this.asdNodes.get(0).printInfo();
        } else {
            System.out.println("LBRACE {");
            boolean tag = false;
            for (ASDNode asdNode : asdNodes) {
                if (tag) {
                    System.out.println("COMMA ,");
                }
                asdNode.printInfo();
                tag = true;
            }
            System.out.println("RBRACE }");
        }
        System.out.println("<ConstInitVal>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        if (asdNodes == null) {
            return new ArrayList<>();
        }
        return asdNodes;
    }

    public Pair<Boolean, ArrayList<Integer>> getInitValue() {
        Pair<Boolean, ArrayList<Integer>> value = new Pair<>(true, new ArrayList<>());
        if (this.type.equals(Type.Exp)) {
            if (((ConstExp) asdNodes.get(0)).getValue().getFirst()) {
                value.getSecond().add(((ConstExp) asdNodes.get(0)).getValue().getSecond());
            } else {
                value.setFirst(false);
            }
        } else {
            for (ASDNode asdNode : asdNodes) {
                if (((ConstInitVal) asdNode).getInitValue().getFirst()) {
                    value.getSecond().addAll(((ConstInitVal) asdNode).getInitValue().getSecond());
                } else {
                    value.setFirst(false);
                    break;
                }
            }
        }

        return value;
    }


    @Override
    public void genCode() {
        if (type.equals(ConstInitVal.Type.Exp)) {
            ConstExp constExp = (ConstExp) asdNodes.get(0);
            // 特殊处理
            constExpList.add(constExp);
        } else {
            for (ASDNode asdNode: asdNodes) {
                ConstInitVal constInitVal = (ConstInitVal) asdNode;
                constInitVal.genCode();
                constExpList.addAll(constInitVal.getConstExpList());
            }
        }
    }

    public ArrayList<ConstExp> getConstExpList() {
        return constExpList;
    }
}
