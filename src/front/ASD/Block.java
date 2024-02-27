package front.ASD;

import SymTable.FuncSymItem;
import SymTable.SymItem;
import SymTable.SymTab;
import SymTable.SymTabStack;
import mid.Label;

import java.util.ArrayList;

public class Block implements ASDNode{
    private final ArrayList<BlockItem> blockItems;
    private Label loopLabel = new Label("@NULL");
    private Label endLoopLabel = new Label("@NULL");
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Block(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
        asdNodes.addAll(blockItems);
    }

    @Override
    public void printInfo() {
        System.out.println("LBRACE {");
        for (BlockItem blockItem: blockItems) {
            blockItem.printInfo();
        }
        System.out.println("RBRACE }");
        System.out.println("<Block>");
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
        // 新建符号表
        SymTab symTab = new SymTab();
        SymTabStack.push(symTab);
        for (BlockItem blockItem: blockItems) {
            if (blockItem != null) {
                blockItem.setLoopLabel(loopLabel);
                blockItem.setEndLoopLabel(endLoopLabel);
                blockItem.genCode();
            }
        }
        SymTabStack.pop();
    }

    public void genCode(SymItem funcSymItem) {
        for (BlockItem blockItem: blockItems) {
            if (blockItem != null) {
                blockItem.setLoopLabel(loopLabel);
                blockItem.setEndLoopLabel(endLoopLabel);
                blockItem.genCode(funcSymItem);
            }
        }
    }

    public void setLoopLabel(Label loopLabel) {
        this.loopLabel = loopLabel;
    }

    public void setEndLoopLabel(Label endLoopLabel) {
        this.endLoopLabel = endLoopLabel;
    }
}
