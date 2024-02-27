package Tools;

public interface ErrorJudge {
    public static boolean JudgeA(StringBuilder token) {
        int ascii;
        for (int i = 0; i < token.length(); i++) {
            ascii = (int) token.charAt(i);
            if (ascii == 32 || ascii == 33 || (40 <= ascii  && ascii <= 126)) {
                // <NormalChar>
                if (ascii == 92 && (i == token.length()-1 || (int) token.charAt(i+1) != 110)) {
                    return false;
                }
            } else if (ascii == 37 && (i == token.length()-1 || (int) token.charAt(i+1) != 100)) {
                // <FormatChar>
                return false;
            }
        }
        return true;
    }
}
