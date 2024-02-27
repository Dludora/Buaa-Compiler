package front.ASD;

import front.Token;

import java.util.ArrayList;

public class Return implements ASDNode{
    private final Token token;

    public Return(Token token) {
        this.token = token;
    }

    public Integer getLineNum() {
        return token.getLineNum();
    }


    @Override
    public void printInfo() {

    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return null;
    }

    @Override
    public void genCode() {

    }
}
