package mid;

public class StrConst extends Value {
    protected final String str;

    public StrConst(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    @Override
    public String toString() {
        return "\"" + str + "\"";
    }
}
