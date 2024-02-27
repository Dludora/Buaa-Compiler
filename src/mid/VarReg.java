package mid;

import SymTable.VarSymItem;
import Tools.VarScope;

public class VarReg extends Register{
    protected String varName;
    protected VarSymItem varSymItem;
    protected VarScope varScope;

    public VarReg(int regNum, RegType regType, String varName, VarSymItem varSymItem, VarScope varScope) {
        super(regNum, regType);
        if (varScope.equals(VarScope.global)) {
            this.varName = "var_" + varName;
        } else {
            this.varName =  "var_" + varName;
        }
        this.varSymItem = varSymItem;
        this.varScope = varScope;
    }

    public String getVarName() {
        return varName;
    }

    public VarSymItem getVarSymItem() {
        return varSymItem;
    }

    public VarScope getVarScope() {
        return varScope;
    }

    @Override
    public String toString() {
        if (varScope.equals(VarScope.global)) {
            return "@"+varName;
        } else {
            return "%"+varName + regNum;
        }
    }
}
