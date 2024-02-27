package SymTable;

import mid.Instruction;

import java.util.ArrayList;

public class LValSymItem extends VarSymItem{
    protected final boolean isConst;
    protected ArrayList<Integer> shape;
    protected boolean zeroInit = true;
    protected int size = 4;
    protected final ArrayList<Integer> value = new ArrayList<>();

    public LValSymItem(String name, SymType symType, boolean isConst, ArrayList<Integer> shape) {
        super(name, symType, shape);
        this.isConst = isConst;
        this.shape = shape;
        for (Integer integer: shape) {
            size *= integer;
        }
    }

    public int getSize() {
        return size;
    }

    public LValSymItem(String name, SymType symType, boolean isConst, Integer dimension) {
        super(name, symType, dimension);
        this.isConst = isConst;
    }

    public void setValue(ArrayList<Integer> value) {
        this.value.addAll(value);
    }

    public boolean isConst() {
        return isConst;
    }

    public Integer getDimension() {
        return dimension;
    }

    public boolean isInit() {
        return value.size() != 0;
    }

    public Integer getValue() {
        return value.get(0);
    }

    public boolean isZeroInit() {
        return zeroInit;
    }

    public void setZeroInit(boolean zeroInit) {
        this.zeroInit = zeroInit;
    }

    public Integer getValue(int x) {
        return value.get(x);
    }

    public Integer getValue(int x, int y) {
        return value.get(x * shape.get(1) + y);
    }

    public Integer getValue(int ...x) {
        if (x.length == 0) {
          return getValue();
        } else if (x.length == 1) {
            return getValue(x[0]);
        } else {
            return getValue(x[0], x[1]);
        }
    }

    public Integer getValue(ArrayList<Integer> x) {
        if (x.size() == 0) {
            return getValue();
        } else if (x.size() == 1) {
            return getValue(x.get(0));
        } else {
            return getValue(x.get(0), x.get(1));
        }
    }

}
