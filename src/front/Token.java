package front;


import java.util.ArrayList;

public class Token {
    private final Symbols tokenClass;        // TokenClass Symbol
    private final String originStr;   // original string
    private final int intValue;       // value(only `INTCON` have)
    private final int lineNum;
    private final int formatCharNum;
    private final ArrayList<String> strings = new ArrayList<>();

    public Token(Symbols tokenClass, String originStr, int lineNum) {
        this.tokenClass = tokenClass;
        this.originStr = originStr;
        this.lineNum = lineNum;
        if (tokenClass.equals(Symbols.INTCON)) {
            this.intValue = Integer.parseInt(originStr);
        } else {
            this.intValue = 0;
        }
        this.formatCharNum = 0;
        if (tokenClass.equals(Symbols.STRCON)) {
            strings.add(originStr.substring(1, originStr.length()-1));
        } else {
            strings.add(originStr);
        }
    }

    public Token(Symbols tokenClass, String originStr, int lineNum, int formatCharNum) {
        this.tokenClass = tokenClass;
        this.originStr = originStr;
        this.lineNum = lineNum;
        if (tokenClass.equals(Symbols.INTCON)) {
            this.intValue = Integer.parseInt(originStr);
        } else {
            this.intValue = 0;
        }
        this.formatCharNum = formatCharNum;
        if (formatCharNum != 0) {
            StringBuilder str = new StringBuilder();
            int i = 1;
            while (i < originStr.length() - 1) {
                if (originStr.charAt(i) == '%' && i < originStr.length() - 2 && originStr.charAt(i + 1) == 'd') {
                    if (str.length() > 0) strings.add(str.toString());
                    str.delete(0, str.length());
                    strings.add("%d");
                    i += 2;
                } else {
                    str.append(originStr.charAt(i));
                    i++;
                }
            }
            if (str.length() > 0) strings.add(str.toString());
        } else {
            strings.add(originStr.substring(1, originStr.length()-1));
        }
    }

    @Override
    public String toString() {
        return tokenClass + " " + originStr;
    }

    public Symbols getTokenClass() {
        return tokenClass;
    }

    public String getName() {
        return originStr;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getIntValue() {
        return intValue;
    }

    public int getFormatCharNum() {
        return formatCharNum;
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

    public String getString() {
        return this.originStr.replaceAll("\"", "");
    }
}
