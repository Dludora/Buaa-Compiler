package SymTable;

import mid.InstSet;
import mid.Instruction;

import java.util.ArrayList;

public class VarSymItem extends SymItem{
    protected ArrayList<Integer> shape;
    protected Instruction declInst = new Instruction(InstSet.nop);
    protected int adr = -1;
    protected Integer dimension;

    public VarSymItem(String name, SymType symType, ArrayList<Integer> shape) {
        super(name, symType);
        this.dimension = shape.size();
        this.shape = shape;
    }

    public VarSymItem(String name, SymType symType, Integer dimension) {
        super(name, symType);
        this.dimension = dimension;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public ArrayList<Integer> getShape() {
        return shape;
    }

    public void setDeclInst(Instruction declInst) {
        this.declInst = declInst;
    }

    public Instruction getDeclInst() {
        return declInst;
    }

    public void setAdr(int adr) {
        this.adr = adr;
    }

    public int getAdr() {
        return adr;
    }
}
