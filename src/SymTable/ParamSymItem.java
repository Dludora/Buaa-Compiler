package SymTable;

import java.util.ArrayList;

public class ParamSymItem extends VarSymItem{
    protected ArrayList<Integer> shape;

    public ParamSymItem(String name, SymType symType, Integer dimension, ArrayList<Integer> shape) {
        super(name, symType, shape);
        this.shape = shape;
    }

    public ParamSymItem(String name, SymType symType, Integer dimension) {
        super(name, symType, dimension);
    }

    public Integer getDimension() {
        return dimension;
    }
}
