package front.ASD;

import front.Token;

import java.util.ArrayList;

public class FormatString implements ASDNode {
    private final Token token;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public FormatString(Token token) {
        this.token = token;
    }

    @Override
    public void printInfo() {
        System.out.println(token.toString());
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    @Override
    public String toString() {
        return this.token.toString();
    }

    public String getString() {
        return token.getString();
    }

    public ArrayList<String> getStrings() {
        return token.getStrings();
    }

    public int getFormatCharNum() {
        return token.getFormatCharNum();
    }

    @Override
    public void genCode() {

    }
}
