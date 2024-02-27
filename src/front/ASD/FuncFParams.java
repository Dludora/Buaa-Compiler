package front.ASD;

import SymTable.*;
import Tools.FuncType;
import Tools.Pair;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import mid.FParam;
import mid.Function;
import mid.MidCode;
import mid.VarReg;

import java.util.ArrayList;

public class FuncFParams implements ASDNode {
    private final ArrayList<FuncFParam> funcFParams;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public FuncFParams(ArrayList<FuncFParam> funcFParams) {
        this.funcFParams = funcFParams;
        asdNodes.addAll(funcFParams);
    }

    @Override
    public void printInfo() {
        if (funcFParams.size() == 0) {
            return;
        }
        boolean flag = false;
        for (FuncFParam funcFParam : funcFParams) {
            if (flag) {
                System.out.println("COMMA ,");
            }
            funcFParam.printInfo();
            flag = true;
        }
        System.out.println("<FuncFParams>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Integer getArgc() {
        return this.funcFParams.size();
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return this.funcFParams;
    }


    @Override
    public void genCode() {
        for (ASDNode asdNode: asdNodes) {
            asdNode.genCode();
        }
    }

    public void genCode(SymItem funcSymItem) {
        for (FuncFParam funcFParam: funcFParams) {
            funcFParam.genCode(funcSymItem);
        }
    }
}
