package mid;

public class Instruction extends User{
    protected InstSet instSet = InstSet.nop;
    protected Register register = new Register(-1, RegType.i32);

    public Instruction() {
    }

    public Instruction(InstSet instSet) {
        this.instSet = instSet;
    }

    public InstSet getInstSet() {
        return instSet;
    }

    public void setInstSet(InstSet instSet) {
        this.instSet = instSet;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public Register getRegister() {
        return register;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (register.regNum != -1) {
            sb.append(register).append(" = ");
        }
        sb.append(instSet).append(" ");
        for (int i = 0; i < operandList.size(); i++) {
            Value value = operandList.get(i);
            if (value instanceof Instruction) {
                Instruction inst = (Instruction) value;
                sb.append(inst.getRegister().toString());
            } else {
                sb.append(value);
            }
            if (i < operandList.size()-1) sb.append(", ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
