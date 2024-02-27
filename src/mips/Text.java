package mips;

import java.util.ArrayList;

public class Text {
    private final ArrayList<Assembly> asmList = new ArrayList<>();

    public void addAsm(Assembly assembly) {
        asmList.add(assembly);
    }

    public void addAllAsm(ArrayList<Assembly> asmList) {
        this.asmList.addAll(asmList);
    }

    public ArrayList<Assembly> getAsmList() {
        return asmList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(".text\n");
        stringBuilder.append("li $fp, 0x10040000\n" + "jal func_main\n" + "nop\n" + "li $v0, 10\n" + "syscall\n");
        for (Assembly assembly: asmList) {
            if (assembly instanceof Label) {
                stringBuilder.append(assembly);
                stringBuilder.append(':');
            } else {
                stringBuilder.append('\t').append(assembly.toString());
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
