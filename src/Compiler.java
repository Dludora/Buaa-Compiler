import front.Error.Error;
import front.Error.ErrorRecorder;
import front.LexicalAnalysis;
import front.SyntacticAnalysis;
import front.ASD.CompUnit;
import mid.MidCode;
import mips.MipsCode;

import java.io.*;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) throws IOException {
        System.setIn(new FileInputStream("testfile.txt"));
        System.setOut(new PrintStream(new FileOutputStream("output.txt")));
        Scanner sc = new Scanner(System.in);

        String str;
        StringBuffer sb = new StringBuffer();

        while (sc.hasNextLine()) {
            str = sc.nextLine();
            sb.append(str).append("\n");
        }

        LexicalAnalysis la = new LexicalAnalysis(sb);
        la.analyze();
        SyntacticAnalysis sa = new SyntacticAnalysis(la.getTokens());
        sa.analyze();
        CompUnit compUnit = sa.getASDTree();
        compUnit.genCode();
        System.setOut(new PrintStream(new FileOutputStream("error.txt")));
        ErrorRecorder.PrintErrors();
        System.setOut(new PrintStream(new FileOutputStream("testfile2_20373820_寇书瑞_优化前中间代码.txt")));
        MidCode.printMidCode();
        System.setOut(new PrintStream(new FileOutputStream("testfile2_20373820_寇书瑞_优化后中间代码.txt")));
        MidCode.printMidCode();
        MipsCode mipsCode = new MipsCode();
        ObjCodeGenerator objCodeGenerator = new ObjCodeGenerator();
        objCodeGenerator.genObjCode();
        System.setOut(new PrintStream(new FileOutputStream("testfile2_20373820_寇书瑞_优化前目标代码.txt")));
        System.out.println(mipsCode);
        System.setOut(new PrintStream(new FileOutputStream("testfile2_20373820_寇书瑞_优化后目标代码.txt")));
        System.out.println(mipsCode);
    }
}
