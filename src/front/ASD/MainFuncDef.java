package front.ASD;

import SymTable.*;
import Tools.FuncType;
import mid.BasicBlock;
import mid.Function;
import mid.Label;
import mid.MidCode;

import java.util.ArrayList;

public class MainFuncDef implements ASDNode{
    private final Block block;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    protected SymItem mainSymItem;

    public MainFuncDef(Block block, ErrorRepresent blockEnd) {
        this.block = block;
        asdNodes.add(block);
        asdNodes.add(blockEnd);
    }

    @Override
    public void printInfo() {
        System.out.println("INTTK int");
        System.out.println("MAINTK main");
        System.out.println("LPARENT (");
        System.out.println("RPARENT )");
        block.printInfo();
        System.out.println("<MainFuncDef>");
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
        mainSymItem = new FuncSymItem("main", SymType.Func, FuncType.Int);
        SymTabStack.pushFunc(mainSymItem);
        SymTab symTab = new SymTab();
        SymTabStack.push(symTab);
        // TODO: 2022/12/3 生成中间代码
        // 1. 创建Function项，对于Function项，要在其子指令之前加入ArrayList中
        Function mainFunction = new Function((FuncSymItem) mainSymItem);
        // 2. 向mainFunction中加入BasicBlock
        mainFunction.addBasicBlock(new BasicBlock(new Label("func_main_entry")));
        MidCode.add(mainFunction);

        block.genCode(mainSymItem);
        SymTabStack.pop();
    }
}
