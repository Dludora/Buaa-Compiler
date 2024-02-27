package mips;

import java.util.ArrayList;

public class Data {
    private final ArrayList<Word> words = new ArrayList<>();
    private final ArrayList<Space> spaces = new ArrayList<>();
    private final ArrayList<Asciiz> asciizes = new ArrayList<>();

    public void addWord(Word word) {
        words.add(word);
    }

    public void addAsciiz(Asciiz asciiz) {
        asciizes.add(asciiz);
    }

    public void addSpace(Space space) {
        spaces.add(space);
    }

    public int getStrNo() {
        return asciizes.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(".data\n");
        for (Word word: words) stringBuilder.append(word).append('\n');
        for (Space space: spaces) stringBuilder.append(space).append('\n');
        for (Asciiz asciiz: asciizes) stringBuilder.append(asciiz).append('\n');
        return stringBuilder.toString();
    }
}
