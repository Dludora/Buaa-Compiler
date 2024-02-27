package front.ASD;

import SymTable.*;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import front.Token;
import mid.*;

import java.util.ArrayList;

public class FuncDef implements ASDNode {        // 未完成(getArgc)
    private final FuncType funcType;
    private final Ident ident;
    private final FuncFParams funcFParams;
    private final Block block;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    protected SymItem funcSymItem;

    public FuncDef(FuncType funcType, Ident ident, FuncFParams funcFParams, Block block, ErrorRepresent blockEnd) {
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
        this.asdNodes.add(funcType);
        this.asdNodes.add(ident);
        this.asdNodes.add(funcFParams);
        this.asdNodes.add(block);
        this.asdNodes.add(blockEnd);
    }

    @Override
    public void printInfo() {
        this.funcType.printInfo();
        this.ident.printInfo();
        System.out.println("LPARENT (");
        this.funcFParams.printInfo();
        System.out.println("RPARENT )");
        this.block.printInfo();
        System.out.println("<FuncDef>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public String getName() {
        return this.ident.getName();
    }

    public Integer getArgc() {
        return funcFParams.getArgc();
    }

    public Tools.FuncType getType() {
        return this.funcType.getType();
    }

    public Ident getIdent() {
        return this.ident;
    }

    public ArrayList<Integer> getDimensions() {
        ArrayList<Integer> dimensions = new ArrayList<>();
        for (FuncFParam funcFParam : funcFParams.getFuncFParams()) {
            dimensions.add(funcFParam.getDimension());
        }

        return dimensions;
    }

    public FuncFParams getParams() {
        return funcFParams;
    }

    @Override
    public void genCode() {
        // TODO: 2022/11/29 重定义的名字
        if (SymTabStack.funcDefined(ident.getName())) {
            ErrorRecorder.putError(ErrorType.b, ident.getLineNum());
        }
        funcSymItem = new FuncSymItem(ident.getName(), SymType.Func, funcType.getType());
        SymTabStack.pushFunc(funcSymItem);
        SymTab symTab = new SymTab();
        SymTabStack.push(symTab);

        // TODO: 2022/12/3 生成中间代码
        // 1. 创建Function项对于Function项，要在其子指令之前加入ArrayList中
        Function function = new Function((FuncSymItem) funcSymItem);
        MidCode.add(function);
        funcFParams.genCode(funcSymItem);
        // 2. 向Function中加入BasicBlock
        function.addBasicBlock(new BasicBlock(new Label("func_" + getName() + "_entry")));
        block.genCode(funcSymItem);
        if (!((FuncSymItem) funcSymItem).getHasRet()) {
            // 手动加上返回语句
            Instruction inst = new Instruction(InstSet.ret);
            function.getLastBasicBlock().addInst(inst);
        }
        SymTabStack.pop();
    }
}
