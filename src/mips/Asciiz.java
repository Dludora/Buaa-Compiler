package mips;

public class Asciiz {
    private final int strNo;
    private final String string;

    public Asciiz(int strNo, String string) {
        this.strNo = strNo;
        this.string = string;
    }

    @Override
    public String toString() {
        return "str_" + strNo + ": .asciiz \"" + string + '\"';
    }
}
