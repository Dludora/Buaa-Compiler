package front.ASD;

import SymTable.*;
import Tools.FuncType;
import Tools.Pair;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class Stmt implements ASDNode {
    public enum Type {
        ASSIGN, Exp, Block, ifBranch, whileBranch, breakStmt, continueStmt,
        returnStmt, Input, Output, None,
    }

    private final Type type;
    private Label loopLabel = new Label("@NULL");
    private Label endLoopLabel = new Label("@NULL");
    private final ArrayList<ASDNode> asdNodes;

    public Stmt(Type type, ArrayList<ASDNode> asdNodes) {
        this.type = type;
        this.asdNodes = asdNodes;
    }

    @Override
    public void printInfo() {

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

    public Type getType() {
        return this.type;
    }

    public void setLoopLabel(Label loopLabel) {
        this.loopLabel = loopLabel;
    }

    public void setEndLoopLabel(Label endLoopLabel) {
        this.endLoopLabel = endLoopLabel;
    }

    @Override
    public void genCode() {                                     // 只有一个判断return的错误处理的区别
        if (type.equals(Type.ASSIGN)) {
            genAssign();
        } else if (type.equals(Type.Exp)) {
            genExp();
        } else if (type.equals(Type.Block)) {
            genBlock();
        } else if (type.equals(Type.ifBranch)) {
            genIf();
        } else if (type.equals(Type.whileBranch)) {
            genWhile();
        } else if (type.equals(Type.breakStmt)) {
            genBreak();
        } else if (type.equals(Type.continueStmt)) {
            genContinue();
        } else if (type.equals(Type.returnStmt)) {
            genReturn();
        } else if (type.equals(Type.Input)) {
            genInput();
        } else if (type.equals(Type.Output)) {
            genOutput();
        } else {        // None

        }
    }

    public void genCode(SymItem funcSymItem) {
        if (type.equals(Type.ASSIGN)) {
            genAssign();
        } else if (type.equals(Type.Exp)) {
            genExp();
        } else if (type.equals(Type.Block)) {
            genBlock();
        } else if (type.equals(Type.ifBranch)) {
            genIf();
        } else if (type.equals(Type.whileBranch)) {
            genWhile();
        } else if (type.equals(Type.breakStmt)) {
            genBreak();
        } else if (type.equals(Type.continueStmt)) {
            genContinue();
        } else if (type.equals(Type.returnStmt)) {
            Return ret = (Return) asdNodes.get(0);
            // TODO: 2022/11/29 无返回值的函数存在不匹配的return语句 f
            if (((FuncSymItem) funcSymItem).getFuncType().equals(FuncType.Void) && asdNodes.size() != 1) {
                ErrorRecorder.putError(ErrorType.f, ret.getLineNum());
            }
            ((FuncSymItem) funcSymItem).setHasRet(true);
            genReturn();
        } else if (type.equals(Type.Input)) {
            genInput();
        } else if (type.equals(Type.Output)) {
            genOutput();
        } else {        // None

        }
    }

    private void genAssign() {
        LVal lVal = (LVal) asdNodes.get(0);
        Exp exp = (Exp) asdNodes.get(1);
        VarSymItem varSymItem = SymTabStack.getVar(lVal.getName());
        // TODO: 2022/11/29 不能该改变常量的值 h
        if (varSymItem instanceof LValSymItem && ((LValSymItem) varSymItem).isConst()) {
            ErrorRecorder.putError(ErrorType.h, lVal.ident.getLineNum());
        }

        // TODO: 2022/12/15 生成中间代码
        Function topFunc = MidCode.topFunction().getSecond();
        InstList instList = new InstList();
        lVal.genCode();
        instList.addAll(lVal.getInstList());
        // exp中间代码生成
        exp.genCode();
        Pair<Boolean, Integer> expVal = exp.getValue();
        if (expVal.getFirst()) {
            // 可以直接获得常数值
            Instruction inst = new Instruction(InstSet.store);
            inst.addOperand(instList.getLast());
            inst.addOperand(new IntConst(expVal.getSecond()));
            instList.add(inst);
        } else {
            Instruction addr = instList.getLast();
            instList.addAll(exp.getInstList());
            Instruction inst = new Instruction(InstSet.store);
            inst.addOperand(addr);
            inst.addOperand(instList.getLast());
            instList.add(inst);
        }

        topFunc.getLastBasicBlock().addAllInst(instList);
    }

    private void genExp() {
        Exp exp;
        if (asdNodes.size() == 1) {
            exp = (Exp) asdNodes.get(0);
            exp.genCode();
            Pair<Boolean, Integer> expVal = exp.getValue();
            if (!expVal.getFirst()) {
                Function topFunc = MidCode.topFunction().getSecond();
                topFunc.getLastBasicBlock().addAllInst(exp.getInstList());
            }
        }
    }

    private void genBlock() {
        Block block = (Block) asdNodes.get(0);
        block.setLoopLabel(loopLabel);
        block.setEndLoopLabel(endLoopLabel);
        block.genCode();
    }

    private void genIf() {
        Function topFunc = MidCode.topFunction().getSecond();
        Label ifLabel = topFunc.distributeLabel("if");
        Label endIfLabel = new Label("@NULL");
        if (asdNodes.size() == 2) {     // 没有else
            endIfLabel = topFunc.distributeLabel("endif");
            // 短路求值
            Cond cond = (Cond) asdNodes.get(0);
            cond.setTrueLabel(ifLabel);
            cond.setFalseLabel(endIfLabel);
            cond.genCode();
            // 新建基本块
            topFunc.addBasicBlock(new BasicBlock(ifLabel));
            // 分析if基本块内的语句
            Stmt ifStmt = (Stmt) asdNodes.get(1);
            ifStmt.setLoopLabel(loopLabel);
            ifStmt.setEndLoopLabel(endLoopLabel);
            ifStmt.genCode();
        } else if (asdNodes.size() == 3) {      // 有else
            Label elseLabel = topFunc.distributeLabel("else");
            Cond cond = (Cond) asdNodes.get(0);
            cond.setTrueLabel(ifLabel);
            cond.setFalseLabel(elseLabel);
            cond.genCode();
            // 新建基本块
            topFunc.addBasicBlock(new BasicBlock(ifLabel));
            // 分析if基本块内的语句
            Stmt ifStmt = (Stmt) asdNodes.get(1);
            ifStmt.setLoopLabel(loopLabel);
            ifStmt.setEndLoopLabel(endLoopLabel);
            ifStmt.genCode();
            // br <endif>
            endIfLabel = topFunc.distributeLabel("endif");
            Instruction inst = new Instruction(InstSet.br);
            inst.addOperand(endIfLabel);
            topFunc.getLastBasicBlock().addInst(inst);
            // else基本块
            topFunc.addBasicBlock(new BasicBlock(elseLabel));
            // 分析else基本块的语句
            Stmt elseStmt = (Stmt) asdNodes.get(2);
            elseStmt.setLoopLabel(loopLabel);
            elseStmt.setEndLoopLabel(endLoopLabel);
            elseStmt.genCode();
        }
        topFunc.addBasicBlock(new BasicBlock(endIfLabel));
    }

    private void genWhile() {
        Function topFunc = MidCode.topFunction().getSecond();
        Label loopLabel = topFunc.distributeLabel("loop");
        Label stmtLabel = topFunc.distributeLabel("loop_stmt");
        Label endLoopLabel = topFunc.distributeLabel("endLoop");

        topFunc.addBasicBlock(new BasicBlock(loopLabel));
        // Cond
        Cond cond = (Cond) asdNodes.get(0);
        cond.setTrueLabel(stmtLabel);
        cond.setFalseLabel(endLoopLabel);
        cond.genCode();
        // <stmtLabel>
        topFunc.addBasicBlock(new BasicBlock(stmtLabel));
        // <Stmt>
        Stmt stmt = (Stmt) asdNodes.get(1);
        stmt.setLoopLabel(loopLabel);
        stmt.setEndLoopLabel(endLoopLabel);
        stmt.genCode();
        // br <loopLabel>
        Instruction inst = new Instruction(InstSet.br);
        inst.addOperand(loopLabel);
        topFunc.getLastBasicBlock().addInst(inst);
        // <endLoopLabel>
        topFunc.addBasicBlock(new BasicBlock(endLoopLabel));
    }

    private void genBreak() {
        Function topFunc = MidCode.topFunction().getSecond();
        Instruction inst = new Instruction(InstSet.br);
        inst.addOperand(endLoopLabel);
        topFunc.getLastBasicBlock().addInst(inst);
    }

    private void genContinue() {
        Function topFunc = MidCode.topFunction().getSecond();
        Instruction inst = new Instruction(InstSet.br);
        inst.addOperand(loopLabel);
        topFunc.getLastBasicBlock().addInst(inst);
    }

    private void genReturn() {
        Return ret = (Return) asdNodes.get(0);

        ret.genCode();
        Instruction inst = new Instruction(InstSet.ret);
        InstList instList = new InstList();
        Function topFunc = MidCode.topFunction().getSecond();
        Exp exp;
        if (asdNodes.size() == 2) {
            exp = (Exp) asdNodes.get(1);
            Pair<Boolean, Integer> expVal = exp.getValue();
            if (expVal.getFirst()) {
                inst.addOperand(new IntConst(expVal.getSecond()));
            } else {
                exp.genCode();
                instList.addAll(exp.getInstList());
                inst.addOperand(instList.getLast());
            }
        }
        instList.add(inst);
        topFunc.getLastBasicBlock().addAllInst(instList);
    }

    private void genOutput() {
        FormatString formatString = (FormatString) asdNodes.get(0);
        formatString.genCode();
        ArrayList<String> strings = formatString.getStrings();
        Instruction inst = new Instruction(InstSet.print);
        InstList instList = new InstList();

        int i = 1;
        for (String str : strings) {
            if (str.equals("%d")) {
                Exp exp = (Exp) asdNodes.get(i);
                i++;
                exp.genCode();
                Pair<Boolean, Integer> expVal = exp.getValue();
                if (expVal.getFirst()) {
                    inst.addOperand(new IntConst(expVal.getSecond()));
                } else {
                    instList.addAll(exp.getInstList());
                    inst.addOperand(instList.getLast());
                }
            } else {
                inst.addOperand(new StrConst(str));
            }
        }
        instList.add(inst);
        MidCode.topFunction().getSecond().getLastBasicBlock().addAllInst(instList);
    }

    private void genInput() {
        LVal lVal = (LVal) asdNodes.get(0);
        lVal.genCode();
        VarSymItem varSymItem = (VarSymItem) SymTabStack.getVar(lVal.getName());
        // TODO: 2022/11/29 不能该改变常量的值 h
        if (varSymItem instanceof LValSymItem && ((LValSymItem) varSymItem).isConst()) {
            ErrorRecorder.putError(ErrorType.h, lVal.ident.getLineNum());
        }

        // 生成中间代码
        Function topFunc = MidCode.topFunction().getSecond();
        Instruction inst = new Instruction(InstSet.getInt);
        inst.setRegister(topFunc.distributeReg());
        InstList instList = new InstList();
        instList.addAll(lVal.getInstList());
        Instruction addr = instList.getLast();
        instList.add(inst);
        inst = new Instruction(InstSet.store);
        inst.addOperand(addr);
        inst.addOperand(instList.getLast());
        instList.add(inst);

        topFunc.getLastBasicBlock().addAllInst(instList);
    }
}
