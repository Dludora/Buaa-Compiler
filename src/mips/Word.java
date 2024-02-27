package mips;

import java.util.ArrayList;

public class Word {
    private final String name;
    private final ArrayList<Integer> values;

    public Word(String name, ArrayList<Integer> values) {
        this.name = name;
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append(": ");
        stringBuilder.append(".word ");
        for (int i = 0; i < values.size(); i++) {
            stringBuilder.append(values.get(i));
            if (i < values.size() - 1) stringBuilder.append(", ");
        }
        return stringBuilder.toString();
    }
}
