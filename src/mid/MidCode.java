package mid;

import SymTable.VarSymItem;
import Tools.Pair;
import Tools.VarScope;

import java.util.ArrayList;
import java.util.Collection;

public class MidCode {
    protected static int latestRegNum = -1;
    protected static final ArrayList<Value> values = new ArrayList<>();

    public static VarReg distributeVarReg(String varName, VarSymItem varSymItem) {
        return new VarReg(++latestRegNum, RegType.i32_p, varName, varSymItem, VarScope.global);
    }

    public static void add(Value value) {
        values.add(value);
    }

    public static void addAll(Collection<? extends Value> values) {
        MidCode.values.addAll(values);
    }

    public static ArrayList<Value> getValues() {
        return values;
    }

    public static Pair<Boolean, Function> topFunction() {
        for (int i = values.size() - 1; i >= 0; i--) {
            Value value = values.get(i);
            if (value instanceof Function) {
                Function function = (Function) value;
                return new Pair<>(true, function);
            }
        }
        return new Pair<>(false, null);
    }

    public static void printMidCode() {
        StringBuilder sb = new StringBuilder();
        for (Value value : values) {
            sb.append(value);
        }
        System.out.println(sb);
    }
}
