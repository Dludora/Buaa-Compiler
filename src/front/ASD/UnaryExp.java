package front.ASD;

import SymTable.FuncSymItem;
import SymTable.ParamSymItem;
import SymTable.SymItem;
import SymTable.SymTabStack;
import Tools.FuncType;
import Tools.Pair;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import front.Symbols;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;
import java.util.Objects;

public class UnaryExp implements ASDNode {
    public enum Type {
        PrimaryExp, FuncCall, mulUnaryExp
    }

    private final Type type;
    private final InstList instList = new InstList();
    private final ArrayList<ASDNode> asdNodes;

    public UnaryExp(Type type, ArrayList<ASDNode> asdNodes) {
        this.type = type;
        this.asdNodes = asdNodes;
    }

    @Override
    public void printInfo() {
        if (type.equals(Type.PrimaryExp)) {
            asdNodes.get(0).printInfo();
        } else if (type.equals(Type.FuncCall)) {
            asdNodes.get(0).printInfo();
            System.out.println("LPARENT (");
            if (asdNodes.size() > 1) {
                asdNodes.get(1).printInfo();
            }
            System.out.println("RPARENT )");
        } else {
            asdNodes.get(0).printInfo();
            asdNodes.get(1).printInfo();
        }
        System.out.println("<UnaryExp>");
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

    public String getFuncCallName() {
        assert (type.equals(Type.FuncCall));
        return ((Ident) asdNodes.get(0)).getName();
    }

    public String getName() {
        if (type.equals(Type.PrimaryExp)) {
            return ((PrimaryExp) asdNodes.get(0)).getName();
        } else if (type.equals(Type.FuncCall)) {
            return ((Ident) asdNodes.get(0)).getName();
        }
        return null;
    }

    public Pair<Boolean, Integer> getValue() {
        if (this.type.equals(Type.PrimaryExp)) {
            return ((PrimaryExp) this.asdNodes.get(0)).getValue();
        } else if (this.type.equals(Type.mulUnaryExp)) {
            if (asdNodes.get(0).toString().equals("PLUS +")) {
                return ((UnaryExp) this.asdNodes.get(1)).getValue();
            } else if (((UnaryOp) asdNodes.get(0)).getType().equals(Symbols.MINU)) {
                return new Pair<>(((UnaryExp) this.asdNodes.get(1)).getValue().getFirst(), -((UnaryExp) this.asdNodes.get(1)).getValue().getSecond());
            } else if (((UnaryOp) asdNodes.get(0)).getType().equals(Symbols.NOT)) {
                if (((UnaryExp) this.asdNodes.get(1)).getValue().getFirst()) {
                    if (((UnaryExp) this.asdNodes.get(1)).getValue().getSecond() == 0) {
                        return new Pair<>(true, 1);
                    } else {
                        return new Pair<>(true, 0);
                    }
                }
            }
        }
        return new Pair<>(false, 0);
    }

    public int getDimension() {
        if (this.type.equals(Type.FuncCall)) {
            Ident ident = (Ident) asdNodes.get(0);
            // TODO: 2022/11/30 参数类型不匹配 e
            FuncSymItem funcSymItem = (FuncSymItem) SymTabStack.getFunc(ident.getName());
            if (funcSymItem != null && funcSymItem.getFuncType().equals(FuncType.Void)) {
                ErrorRecorder.putError(ErrorType.e, ident.getLineNum());
            }
            return 0;
        } else if (type.equals(Type.PrimaryExp)) {
            return ((PrimaryExp) asdNodes.get(0)).getDimension();
        } else {
            return ((UnaryExp) asdNodes.get(asdNodes.size() - 1)).getDimension();
        }
    }

    public boolean isFunCall() {
        return type.equals(Type.FuncCall) || type.equals(Type.PrimaryExp) && ((PrimaryExp) asdNodes.get(0)).isFunCall();
    }

    public Type getType() {
        return type;
    }


    @Override
    public void genCode() {
        if (type.equals(Type.PrimaryExp)) {
            PrimaryExp primaryExp = (PrimaryExp) asdNodes.get(0);
            primaryExp.genCode();
            Pair<Boolean, Integer> primaryExpVal = primaryExp.getValue();

            // 生成中间代码
            if (!primaryExpVal.getFirst()) instList.addAll(primaryExp.getInstList());


        } else if (type.equals(Type.FuncCall)) {
            Ident ident = (Ident) asdNodes.get(0);
            FuncRParams funcRParams = (FuncRParams) asdNodes.get(1);
            // 从符号表中取出函数项
            FuncSymItem funcSymItem = (FuncSymItem) SymTabStack.getFunc(ident.getName());
            // TODO: 2022/11/29 未定义的名字 c
            if (funcSymItem == null) {
                ErrorRecorder.putError(ErrorType.c, ident.getLineNum());
            } else {
                // TODO: 2022/11/29 函数参数个数不匹配 d
                if (funcSymItem.getParams().size() != funcRParams.size()) {
                    ErrorRecorder.putError(ErrorType.d, ident.getLineNum());
                } else {
                    // TODO: 2022/11/30 函数参数类型不匹配 e
                    for (int i = 0; i < funcRParams.size(); i++) {
                        if (!Objects.equals(((ParamSymItem) funcSymItem.getParams().get(i)).getDimension(), ((Exp) funcRParams.getChild().get(i)).getDimension())) {
                            ErrorRecorder.putError(ErrorType.e, ident.getLineNum());
                        }
                    }
                }
            }
            // 错误处理结束，假定可以查到ident
            ident.genCode();
            // 生成中间代码
            assert funcSymItem != null;
            Function functionCall = new Function(funcSymItem);
            functionCall.setFuncCall(true);     // 标记为函数调用
            // 分析函数的实参
            if (funcRParams.size() != 0) {
                // 函数调用有实参
                funcRParams.setFunctionCall(functionCall);
                funcRParams.genCode();
                instList.addAll(funcRParams.getInstList());
            }
            Instruction inst = new Instruction(InstSet.call);
            inst.addOperand(functionCall);
            if (funcSymItem.getFuncType().equals(FuncType.Int)) {
                Function topFunction = MidCode.topFunction().getSecond();
                inst.setRegister(topFunction.distributeReg());
            }
            instList.add(inst);
        } else {
            UnaryOp unaryOp = (UnaryOp) asdNodes.get(0);
            UnaryExp unaryExp = (UnaryExp) asdNodes.get(1);
            unaryOp.genCode();
            unaryExp.genCode();

            // 生成中间代码
            Pair<Boolean, Integer> unaryExpVal = unaryExp.getValue();
            if (!unaryExpVal.getFirst()) {
                instList.addAll(unaryExp.getInstList());
                Function topFunction = MidCode.topFunction().getSecond();

                Instruction inst = new Instruction(genIRInst(unaryOp));
                inst.setRegister(topFunction.distributeReg());
                inst.addOperand(new IntConst(0));
                inst.addOperand(instList.getLast());

                instList.add(inst);
            }
        }
    }

    public InstList getInstList() {
        return instList;
    }

    private InstSet genIRInst(UnaryOp op) {
        return op.getType().equals(Symbols.NOT) ? InstSet.icmp_eq :
                op.getType().equals(Symbols.MINU) ? InstSet.sub :
                        InstSet.add;
    }
}
