package mips;

public class MipsCode {
    private static final Data data = new Data();
    private static final Text text = new Text();

    public static void addWord(Word word) {
        data.addWord(word);
    }

    public static void addAsciiz(Asciiz asciiz) {
        data.addAsciiz(asciiz);
    }

    public static int getStrNo() {
        return data.getStrNo();
    }

    public static void addSpace(Space space) {
        data.addSpace(space);
    }

    public static void addAsm(Assembly assembly) {
        text.addAsm(assembly);
    }

    @Override
    public String toString() {
        return data + "\n" + text;
    }
}
