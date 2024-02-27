package Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.jar.Attributes;

public interface Utility {

    static boolean isSpace(char c) {
        return c == ' ';
    }

    static boolean isTab(char c) {
        return c == '\t';
    }

    static long filesCompareByLine(Path path1, Path path2)
            throws IOException {
        try (BufferedReader bf1 = Files.newBufferedReader(path1);
             BufferedReader bf2 = Files.newBufferedReader(path2)) {
            // 用于标识两文件是否相同，-1是完全相同，
            // 否则是不同的行的行号，某种条件下也是较小文件的最大行号
            long lineNumber = 1;
            String line1 = "", line2 = "";
            while ((line1 = bf1.readLine()) != null) {
                line2 = bf2.readLine();
                if (!line1.equals(line2)) {
                    return lineNumber;
                }
                lineNumber++;
            }
            if (bf2.readLine() == null) {
                return -1;
            }
            else {
                return lineNumber;
            }
        }
    }

    static int getAbsoluteIndex(ArrayList<Integer> shape, int dimension, int... subscript) {
        int index = 0;
        if (dimension == 1) {
            index = subscript[0];
        } else {
            index = shape.get(0) * subscript[0] + subscript[1] - 1;
        }

        return index;
    }

    static void nextIndex(ArrayList<Integer> shape, ArrayList<Integer> index) {
        boolean carry = true;
        for (int i = index.size() - 1; i >= 0; i--) {
            if (carry) {
                int val = index.get(i);
                if (val + 1 < shape.get(i)) {
                    index.set(i, val + 1);
                    carry = false;
                } else{
                    index.set(i, 0);
                }
            } else {
                break;
            }
        }
    }

    public static mips.Reg numToAReg(int num) {
        if (num == 0) return mips.Reg.$a0;
        else if (num == 1) return mips.Reg.$a1;
        else if (num == 2) return mips.Reg.$a2;
        else return mips.Reg.$a3;
    }
}