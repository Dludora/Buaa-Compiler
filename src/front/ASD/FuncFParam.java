package front.ASD;

import SymTable.*;
import Tools.Pair;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;
import mid.*;
import mid.IntConst;

import java.util.ArrayList;

public class FuncFParam implements ASDNode {
    private final BType bType;
    private final Ident ident;
    private final ArrayList<ConstExp> constExps;
    private final boolean isArray;

    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    private final int dimension;

    public FuncFParam(BType bType, Ident indent, ArrayList<ConstExp> constExps, int dimension) {
        this.bType = bType;
        this.ident = indent;
        this.constExps = constExps;
        this.dimension = dimension;
        asdNodes.add(bType);
        asdNodes.add(indent);
        asdNodes.addAll(constExps);
        this.isArray = dimension > 0;
    }

    @Override
    public void printInfo() {
        bType.printInfo();
        ident.printInfo();
        if (isArray) {
            System.out.println("LBRACK [");
            System.out.println("RBRACK ]");
            for (ConstExp constExp : constExps) {
                System.out.println("LBRACK [");
                constExp.printInfo();
                System.out.println("RBRACK ]");
            }
        }
        System.out.println("<FuncFParam>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public String getName() {
        return ident.getName();
    }

    public boolean getIsArray() {
        return isArray;
    }

    public Ident getIdent() {
        return ident;
    }

    public int getDimension() {
        return dimension;
    }

    public Pair<Boolean, ArrayList<Integer>> getArrayShape() {
        ArrayList<Integer> shape = new ArrayList<>();
        if (dimension == 1) {
            shape.add(-1);
        } else if (dimension == 2) {
            shape.add(-1);
            for (ConstExp exp : constExps) {
                if (exp.getValue().getFirst()) {
                    shape.add(exp.getValue().getSecond());
                } else {
                    return new Pair<>(false, shape);
                }
            }
        }

        return new Pair<>(true, shape);
    }

    @Override
    public void genCode() {
        for (ASDNode asdNode : asdNodes) {
            asdNode.genCode();
        }
    }

    public void genCode(SymItem funcSymItem) {
        // TODO: 2022/12/1 名字重定义 b
        if (SymTabStack.varDefined(this.getName())) {
            ErrorRecorder.putError(ErrorType.b, this.getLineNum());
        }

        Pair<Boolean, ArrayList<Integer>> shape = getArrayShape();


        // 生成 ParamSymItem表项并加入 符号表 和 对应函数 中
        VarSymItem paramSymItem = new ParamSymItem(this.getName(), SymType.Param, this.getDimension(), shape.getSecond());
        ((FuncSymItem) funcSymItem).addParam(paramSymItem);
        SymTabStack.pushVar(paramSymItem);

        // TODO: 2022/12/3 生成中间代码
        // 1. 获得顶部Function
        Pair<Boolean, Function> topFunc = MidCode.topFunction();
        if (topFunc.getFirst()) {
            VarReg varReg = topFunc.getSecond().distributeVarReg(getName(), paramSymItem);
            FParam fParam = new FParam(varReg);
            topFunc.getSecond().addOperand(fParam);

            Instruction inst = new Instruction(InstSet.alloca);
            inst.setRegister(varReg);
            IntConst intConst = new IntConst(4);
            inst.addOperand(intConst);
            paramSymItem.setDeclInst(inst);
            paramSymItem.setAdr(topFunc.getSecond().distributeAdr(4));
        }

        for (ASDNode asdNode : asdNodes) {
            asdNode.genCode();
        }
    }

    public Integer getLineNum() {
        return ident.getLineNum();
    }
}
