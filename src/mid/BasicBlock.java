package mid;

public class BasicBlock extends Value{
    protected InstList instList = new InstList();
    protected Label label;

    public BasicBlock(Label label) {
        this.label = label;
    }

    public InstList getInstList() {
        return instList;
    }

    public void addInst(Instruction inst) {
        instList.add(inst);
    }

    public void addAllInst(InstList instList) {
        this.instList.addAll(instList);
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Instruction getLast() {
        return instList.getLast();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // label
        sb.append(label).append(":\n");
        for (Instruction inst: instList) {
            sb.append("\t").append(inst);
        }
        return sb.toString();
    }
}
