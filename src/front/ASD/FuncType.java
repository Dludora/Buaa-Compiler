package front.ASD;

import java.util.ArrayList;

public class FuncType implements ASDNode{       // 完成

    private final Tools.FuncType type;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public FuncType(Tools.FuncType type) {
        this.type = type;
    }

    @Override
    public void printInfo() {
        if (type.equals(Tools.FuncType.Int)) {
            System.out.println("INTTK int");
        } else {
            System.out.println("VOIDTK void");
        }
        System.out.println("<FuncType>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Tools.FuncType getType() {
        return this.type;
    }


    @Override
    public void genCode() {

    }
}
