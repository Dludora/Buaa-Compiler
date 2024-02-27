package mid;

public class FParam extends Value {
    private final VarReg varReg;

    public FParam(VarReg varReg) {
        this.varReg = varReg;
    }

    public VarReg getVarReg() {
        return varReg;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int dimension = varReg.getVarSymItem().getDimension();

        if (dimension > 0) sb.append(RegType.i32_p).append(" ");
        else sb.append(RegType.i32).append(" ");
        sb.append(varReg);

        return sb.toString();
    }
}
