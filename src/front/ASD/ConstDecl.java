package front.ASD;

import java.util.ArrayList;

public class ConstDecl implements ASDNode {
    private final BType bType;
    private final ArrayList<ConstDef> constDefs;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public ConstDecl(BType bType, ArrayList<ConstDef> constDefs) {
        this.bType = bType;
        this.constDefs = constDefs;
        asdNodes.add(bType);
        asdNodes.addAll(constDefs);
    }

    @Override
    public void printInfo() {
        System.out.println("CONSTTK const");
        System.out.println("INTTK int");
        boolean flag = false;
        for (ConstDef constDef : constDefs) {
            if (flag) {
                System.out.println("COMMA ,");
            }
            constDef.printInfo();
            flag = true;
        }
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    @Override
    public void genCode() {
        bType.genCode();
        for (ConstDef constDef: constDefs) {
            constDef.genCode();
        }
    }
}
