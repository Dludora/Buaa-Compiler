package front.ASD;


import SymTable.SymTab;
import SymTable.SymTabStack;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class CompUnit implements ASDNode{           // 完成
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public CompUnit(ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs, MainFuncDef mainFuncDef) {
        this.decls = decls;
        this.mainFuncDef = mainFuncDef;
        this.funcDefs = funcDefs;
        asdNodes.addAll(decls);
        asdNodes.addAll(funcDefs);
        asdNodes.add(mainFuncDef);
    }

    @Override
    public void printInfo() {
        PrintStream out = System.out;
        try {
            PrintStream os = new PrintStream("output.txt");
            System.setOut(os);
        } catch (IOException ignored) {
        }

        for (Decl decl : decls) {
            decl.printInfo();
        }
        for (FuncDef funcDef : funcDefs) {
            funcDef.printInfo();
        }
        mainFuncDef.printInfo();
        System.out.println("<CompUnit>");
        System.setOut(out);
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
        // 创建全局符号表
        SymTab globalTab = new SymTab();
        SymTabStack.push(globalTab);
        for (Decl decl: decls) {
            decl.genCode();
        }
        for (FuncDef funcDef: funcDefs) {
            funcDef.genCode();
        }
        mainFuncDef.genCode();
    }
}
