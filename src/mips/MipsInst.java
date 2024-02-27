package mips;

import java.util.ArrayList;
import java.util.Arrays;

public class MipsInst implements Assembly{
    /** The instruction name. **/
    private final Mnemonic mnemonic;
    private final ArrayList<Operand> operands;

    public MipsInst(Mnemonic mnemonic, ArrayList<Operand> operands) {
        this.mnemonic = mnemonic;
        this.operands = operands;
    }

    public MipsInst(Mnemonic mnemonic, Operand... operands) {
        this.mnemonic = mnemonic;
        this.operands = new ArrayList<>(Arrays.asList(operands));
    }

    @Override
    public String toString() {
        if (mnemonic == Mnemonic.lw || mnemonic == Mnemonic.sw) {
            return mnemonic + " " + operands.get(0) + ", " +
                    operands.get(2) + "(" + operands.get(1) + ")";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(mnemonic).append(' ');
            for (int i = 0; i < operands.size(); i++) {
                stringBuilder.append(operands.get(i));
                if (i < operands.size() - 1) stringBuilder.append(", ");
            }
            return stringBuilder.toString();
        }
    }
}
