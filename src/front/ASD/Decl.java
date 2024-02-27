package front.ASD;

import java.util.ArrayList;

public class Decl implements ASDNode{
    public enum Type {
        Const,
        Var
    }
    private final ConstDecl constDecl;
    private final VarDecl varDecl;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    private final Type type;


    public Decl(ConstDecl constDecl, VarDecl varDecl) {
        this.constDecl = constDecl;
        this.varDecl = varDecl;
        if (constDecl == null) {
            this.asdNodes.add(varDecl);
        } else {
            this.asdNodes.add(constDecl);
        }
        type = constDecl == null ? Type.Var : Type.Const;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void printInfo() {
        if (constDecl != null) {
            constDecl.printInfo();
        } else {
            varDecl.printInfo();
        }
        // 不输出<Decl>
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
        if (constDecl == null) {
            varDecl.genCode();
        } else {
            constDecl.genCode();
        }
    }
}
