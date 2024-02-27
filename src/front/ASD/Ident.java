package front.ASD;

import front.Token;
import java.util.ArrayList;

public class Ident implements ASDNode{      // 未完成 getLineNum
    private final Token token;
    private final String name;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Ident(Token token) {
        this.token = token;
        this.name = token.getName();
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

    public String getName() {
        return name;
    }

    public int getLineNum() {
        return token.getLineNum();
    }

    @Override
    public void genCode() {

    }
}
