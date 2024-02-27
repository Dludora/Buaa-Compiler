package mips;

public class Space {
    private final int size;
    private final String name;

    public Space(int size, String name) {
        this.size = size;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ": .space " + size;
    }
}
