import Tools.Utility;

import java.io.IOException;
import java.nio.file.Paths;

public class Clap implements Utility {
    public static void main(String[] args) throws IOException {
        long num = Utility.filesCompareByLine(Paths.get("mips.txt"), Paths.get("standard.txt"));
        System.out.println(num);
    }
}
