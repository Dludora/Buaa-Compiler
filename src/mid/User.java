package mid;


import java.util.ArrayList;

public class User extends Value {
    protected final ArrayList<Value> operandList = new ArrayList<>();

    public void addOperand(Value operand) {
        operandList.add(operand);
        operand.addUser(this);
    }

    public Value getOperand(int index) {
        return operandList.get(index);
    }

    public ArrayList<Value> getOperandList() {
        return operandList;
    }

    public int getOperandNum() {
        return operandList.size();
    }
}
