package front.ASD;

import Tools.Pair;
import mid.Function;
import mid.InstList;
import mid.IntConst;

import java.util.ArrayList;

public class FuncRParams implements ASDNode{
    public ArrayList<Exp> exps;
    private Function functionCall;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    private final ArrayList<Integer> dimensions = new ArrayList<>();

    public FuncRParams(ArrayList<Exp> exps) {
        this.exps = exps;
        asdNodes.addAll(exps);
    }

    @Override
    public void printInfo() {
        if (exps.size() == 0) {
            return;
        }
        boolean flag = false;
        for (Exp exp: exps) {
            if (flag) {
                System.out.println("COMMA ,");
            }
            exp.printInfo();
            flag = true;
        }
        System.out.println("<FuncRParams>");
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    @Override
    public void genCode() {
        for (Exp exp: exps) {
            exp.genCode();
            Pair<Boolean, Integer> expVal = exp.getValue();
            if (expVal.getFirst()) {
                functionCall.addOperand(new IntConst(expVal.getSecond()));
            } else {
                instList.addAll(exp.getInstList());
                functionCall.addOperand(instList.getLast());
            }
        }
    }

    public void setFunctionCall(Function functionCall) {
        this.functionCall = functionCall;
    }

    public InstList getInstList() {
        return instList;
    }

    public int size() {
        return exps.size();
    }
}
