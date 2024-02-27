package SymTable;

import java.util.LinkedHashMap;

public class SymTab extends LinkedHashMap<String, SymItem> {
    public boolean hasFunc(String name) {
        return this.containsKey(genFuncKey(name));
    }

    public boolean hasVar(String name) {
        return this.containsKey(genVarKey(name));
    }

    public String genFuncKey(String name) {
        return "@func_" + name;
    }

    public String genVarKey(String name) {
        return "@var_" + name;
    }
}
