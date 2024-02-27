package mid;

import SymTable.FuncSymItem;
import SymTable.VarSymItem;
import Tools.FuncType;
import Tools.VarScope;

public class Function extends User {
    protected final FuncSymItem funcSymItem;
    private boolean isFuncCall = false;
    private int latestRegNum = -1;
    private int latestLabelNum = -1;
    private int tVarOffset = 0; // 临时变量区相对于$fp的偏移

    public Function(FuncSymItem funcSymItem) {
        this.funcSymItem = funcSymItem;
    }

    public Function(FuncSymItem funcSymItem, boolean isFuncCall) {
        this.funcSymItem = funcSymItem;
        this.isFuncCall = isFuncCall;
    }

    public FuncSymItem getFuncSymItem() {
        return funcSymItem;
    }

    public String getFuncName() {
        return funcSymItem.getName();
    }

    public FuncType getFuncType() {
        return funcSymItem.getFuncType();
    }

    public Register distributeReg() {
        return new Register(++latestRegNum, RegType.i32);
    }

    public VarReg distributeVarReg(String varName, VarSymItem varSymItem) {
        return new VarReg(++latestRegNum, RegType.i32_p, varName, varSymItem, VarScope.local);
    }

    public Label distributeLabel(Label.Desc desc) {
        return new Label("label_" + getFuncName() + "_" + ++latestLabelNum + "_" + desc);
    }

    public Label distributeLabel(String desc) {
        return new Label("label_" + getFuncName() + "_" + ++latestLabelNum + "_" + desc);
    }

    public int distributeAdr(int size) {
        int before = tVarOffset;
        tVarOffset += size;
        return before;
    }

    public int getTVarOffset() {
        return tVarOffset;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        addOperand(basicBlock);
    }

    public BasicBlock getLastBasicBlock() {
        if (operandList.size() == 0) addBasicBlock(new BasicBlock(new Label("func_" + getFuncName() + "_entry")));
        Value value = operandList.get(operandList.size() - 1);
        if (value instanceof BasicBlock) {
            return (BasicBlock) value;
        } else {
            BasicBlock basicBlock = new BasicBlock(new Label("func_" + getFuncName() + "_entry"));
            addBasicBlock(basicBlock);
            return basicBlock;
        }
    }

    public void addFParam(FParam FParam) {
        addOperand(FParam);
    }

    public boolean isFuncDef() {
        return !isFuncCall;
    }

    public void setFuncCall(boolean funcCall) {
        isFuncCall = funcCall;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isFuncDef()) {
            sb.append("\n");
            sb.append("define ");
            if (funcSymItem.getFuncType().equals(FuncType.Int)) sb.append("i32 ");
            else sb.append("void ");
            sb.append("@").append(getFuncName()).append("(");
            for (int i = 0; i < operandList.size(); i++) {
                Value value = operandList.get(i);
                if (value instanceof FParam) {
                    sb.append(value);
                    if (i + 1 < operandList.size()
                            && operandList.get(i + 1) instanceof FParam)
                        sb.append(", ");
                } else break;
            }
            sb.append(") {\n");
            for (Value value : operandList) {
                if (value instanceof BasicBlock) sb.append(value);
            }
            sb.append("} \n");
        } else {
            sb.append("@").append(getFuncName()).append("(");
            for (int i = 0; i < operandList.size(); i++) {
                Value value = operandList.get(i);
                if (value instanceof Instruction) {
                    Instruction inst = (Instruction) value;
                    sb.append(inst.getRegister().toString());
                } else {
                    sb.append(operandList.get(i));
                }
                if (i < operandList.size() - 1) sb.append(", ");
            }
            sb.append(')');
        }

        return sb.toString();
    }
}
