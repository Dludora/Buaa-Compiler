package mid;

public class Label extends Value{
    public enum Desc {
        entry,
        if_entry,
        else_entry,
        if_end,
        while_entry,
        while_end,
    }
    private final String label;

    public Label(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
