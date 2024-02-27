package mid;

import java.util.ArrayList;

public class InstList extends ArrayList<Instruction> {
    public Instruction getLast() {
        if (size() > 0) return this.get(this.size() - 1);
        return new Instruction(InstSet.nop);
    }
}
