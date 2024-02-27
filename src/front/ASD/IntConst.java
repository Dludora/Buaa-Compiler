package front.ASD;

import Tools.Pair;
import front.Token;

import java.util.ArrayList;

public class IntConst implements ASDNode {
    private final Token token;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public IntConst(Token token) {
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

    public Pair<Boolean, Integer> getValue() {
        return new Pair<>(true, token.getIntValue());
    }


    @Override
    public void genCode() {

    }
}
