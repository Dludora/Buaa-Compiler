package SymTable;

public class SymItem {
    protected final String name;
    protected final SymType symType;

    public SymItem(String name, SymType symType) {
        this.name = name;
        this.symType = symType;
    }

    public String getName() {
        return name;
    }

    public SymType getSymType() {
        return symType;
    }
}
