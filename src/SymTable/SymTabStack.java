package SymTable;

import java.util.Stack;

public class SymTabStack {
    protected static Stack<SymTab> symTabStack = new Stack<>();

    public static void push(SymTab symTab) {
        symTabStack.push(symTab);
    }

    public static void pop() {
        symTabStack.pop();
    }

    public static SymTab peek() {
        return symTabStack.peek();
    }

    public static VarSymItem getVar(String name) {
        for (int i = symTabStack.size() - 1; i >= 0; i--) {
            SymTab symTab = symTabStack.get(i);
            if (symTab.hasVar(name)) {
                return (VarSymItem) symTab.get(symTab.genVarKey(name));
            }
        }
        return null;
    }

    public static FuncSymItem getFunc(String name) {
        for (int i = symTabStack.size() - 1; i >= 0; i--) {
            SymTab symTab = symTabStack.get(i);
            if (symTab.hasFunc(name)) {
                return (FuncSymItem) symTab.get(symTab.genFuncKey(name));
            }
        }
        return null;
    }

    public static String pushVar(SymItem symItem) {
        SymTab symTab = symTabStack.peek();
        String key = symTab.genVarKey(symItem.name);
        symTab.put(key, symItem);

        return key;
    }

    public static String pushFunc(SymItem symItem) {
        SymTab symTab = symTabStack.peek();
        String key = symTab.genFuncKey(symItem.name);
        symTab.put(key, symItem);

        return key;
    }

    public static boolean varDefined(String name) {
        if (!symTabStack.isEmpty()) {
            return symTabStack.peek().hasVar(name);
        }
        return false;
    }

    public static boolean funcDefined(String name) {
        if (symTabStack.peek() != null) {
            return symTabStack.peek().hasFunc(name);
        }
        return false;
    }

}
