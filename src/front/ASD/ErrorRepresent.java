package front.ASD;

import front.Token;
import java.util.ArrayList;

public class ErrorRepresent implements ASDNode {
    private Token token;

    public ErrorRepresent(Token sym) {
        token = sym;
    }

    @Override
    public void printInfo() {

    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return new ArrayList<>();
    }


    public Token getToken() {
        return token;
    }

    @Override
    public void genCode() {

    }
}
