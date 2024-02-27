package mips;

public class Label implements Assembly, Operand{
    private final String label;

    public Label(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
