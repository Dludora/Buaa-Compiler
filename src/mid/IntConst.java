package mid;

public class IntConst extends Value {
    protected final int value;

    public IntConst(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "i32 " + value;
    }
}
