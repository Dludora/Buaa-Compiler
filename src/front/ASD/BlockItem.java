package front.ASD;


import SymTable.SymItem;
import mid.Label;

import java.util.ArrayList;

public class BlockItem implements ASDNode {
    private Decl decl = null;
    private Stmt stmt = null;
    private Label loopLabel = new Label("@NULL");
    private Label endLoopLabel = new Label("@NULL");
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public BlockItem(Decl decl, Stmt stmt) {
        this.decl = decl;
        this.stmt = stmt;
        if (decl == null) {
            this.asdNodes.add(stmt);
        } else {
            this.asdNodes.add(decl);
        }
    }

    @Override
    public void printInfo() {
        if (this.decl == null) {
            this.stmt.printInfo();
        } else {
            this.decl.printInfo();
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
        if (decl == null) {
            stmt.setLoopLabel(loopLabel);
            stmt.setEndLoopLabel(endLoopLabel);
            stmt.genCode();
        } else {
            decl.genCode();
        }
    }

    public void genCode(SymItem funcSymItem) {
        if (decl == null) {
            stmt.setLoopLabel(loopLabel);
            stmt.setEndLoopLabel(endLoopLabel);
            stmt.genCode(funcSymItem);
        } else {
            decl.genCode();
        }
    }

    public void setLoopLabel(Label loopLabel) {
        this.loopLabel = loopLabel;
    }

    public void setEndLoopLabel(Label endLoopLabel) {
        this.endLoopLabel = endLoopLabel;
    }
}
