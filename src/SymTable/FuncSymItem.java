package SymTable;

import Tools.FuncType;

import java.util.ArrayList;

public class FuncSymItem extends SymItem {
    protected final ArrayList<SymItem> params = new ArrayList<>();
    protected final FuncType funcType;
    protected boolean hasRet = false;

    public FuncSymItem(String name, SymType symType, Tools.FuncType funcType) {
        super(name, symType);
        this.funcType = funcType;
    }

    public void setHasRet(boolean hasRet) {
        this.hasRet = hasRet;
    }

    public boolean getHasRet() {
        return hasRet;
    }

    public ArrayList<SymItem> getParams() {
        return params;
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public void addParam(SymItem paramSymItem) {
        params.add(paramSymItem);
    }
}
