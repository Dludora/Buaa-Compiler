package mid;

import java.util.ArrayList;

public class ArrayType extends Value{
    private final ArrayList<Integer> shape;

    public int size() {
        return shape.size();
    }

    public int get(int index) {
        return shape.get(index);
    }

    public ArrayType(ArrayList<Integer> shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Integer dim: shape) {
            sb.append(dim).append('*').append('[');
        }
        sb.append("i32");
        for (int i = 0; i < shape.size(); i++) {
            sb.append(']');
        }
        sb.append(']');
        return sb.toString();
    }
}
