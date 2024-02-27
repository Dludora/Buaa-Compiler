package front.ASD;

import java.util.ArrayList;

public class VarDecl implements ASDNode{
    private final BType bType;
    private final ArrayList<VarDef> varDefs;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public VarDecl(BType bType, ArrayList<VarDef> varDefs) {
        this.bType = bType;
        this.varDefs = varDefs;
        asdNodes.add(bType);
        asdNodes.addAll(varDefs);
    }

    @Override
    public void printInfo() {
        bType.printInfo();
        boolean flag = false;
        for (VarDef varDef : varDefs) {
            if (flag) {
                System.out.println("COMMA ,");
            }
            varDef.printInfo();
            flag = true;
        }
        System.out.println("SEMICN ;");
        System.out.println("<VarDecl>");
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
//        for (ASDNode asdNode: asdNodes) {
//            asdNode.genCode();
//        }
        bType.genCode();
        for (VarDef varDef: varDefs) {
            varDef.genCode();
        }
    }
}
