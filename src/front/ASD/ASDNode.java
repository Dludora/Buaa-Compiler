package front.ASD;


import java.util.ArrayList;

public interface ASDNode {
    public void printInfo();
    public void linkWithSymbolTable();
    ArrayList<ASDNode> getChild();
    public void genCode();
}
