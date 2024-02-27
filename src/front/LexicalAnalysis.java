package front;

import Tools.Utility;
import front.ASD.ErrorRepresent;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;

import java.util.ArrayList;

public class LexicalAnalysis implements Reserves {

    private final StringBuffer token = new StringBuffer();
    private Character c;
    private Symbols symbol;
    private int pointer = 0;
    private final StringBuffer programCode;
    private int lineNum;
    private int formatCharNum;
    private final ArrayList<Token> tokens = new ArrayList<>();

    static {
        resSymbols.put("main", "MAINTK");
        resSymbols.put("const", "CONSTTK");
        resSymbols.put("int", "INTTK");
        resSymbols.put("break", "BREAKTK");
        resSymbols.put("continue", "CONTINUETK");
        resSymbols.put("if", "IFTK");
        resSymbols.put("else", "ELSETK");
        resSymbols.put("while", "WHILETK");
        resSymbols.put("getint", "GETINTTK");
        resSymbols.put("printf", "PRINTFTK");
        resSymbols.put("return", "RETURNTK");
        resSymbols.put("void", "VOIDTK");
        resSymbols.put("bitand", "BITAND");
    }

    public LexicalAnalysis(StringBuffer file) {
        this.programCode = file;
        formatCharNum = 0;
    }

    public boolean isNewLine() {
        if (unReachEnd() && c == '\n') {
            lineNum += 1;
            return true;
        }
        return false;
    }

    public StringBuffer getToken() {
        return token;
    }

    public Symbols getSymbol() {
        return symbol;
    }

    public void clearToken() {
        token.delete(0, token.length());
    }

    public boolean unReachEnd() {
        return pointer < programCode.length() - 1;
    }

    public void catToken() {
        token.append(c);
    }

    public void getChar() {
        c = programCode.charAt(pointer);
        pointer++;
    }

    public void retract() {
        if (pointer > 0) {
            pointer--;
        }
    }

    public int getSym() {
        clearToken();
        getChar();
        while ((Utility.isSpace(c) || isNewLine() || Utility.isTab(c))) {
            if (pointer == programCode.length()) {
                return -1;
            }
            getChar();
        }
        if (Character.isLetter(c) || c == '_') {     //
            while (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
                catToken();
                getChar();
            }
            retract();
            boolean resultValue = resSymbols.containsKey(token.toString());

            if (!resultValue) {
                // 标识符
                symbol = Symbols.IDENFR;
            } else {
                //
                symbol = Symbols.valueOf(resSymbols.get(token.toString()));
            }
        } else if (Character.isDigit(c)) {      // 数字
            while (Character.isDigit(c)) {
                catToken();
                getChar();
            }
            retract();
            symbol = Symbols.INTCON;
        } else if (c == '\"') {                 // FormatString
            formatCharNum = 0;
            boolean hasErr = false;
            do {
                if (programCode.charAt(pointer) == '%') {
                    // <FormatChar>
                    if (unReachEnd() && programCode.charAt(pointer + 1) == 'd') {
                        formatCharNum += 1;
                    } else {
                        // Error a
                        if (!hasErr) {
                            ErrorRecorder.putError(ErrorType.a, lineNum);
                            hasErr = true;
                        }
                    }
                } else if (programCode.charAt(pointer) == '\\') {
                    if (!(unReachEnd() && programCode.charAt(pointer + 1) == 'n')) {
                        // Error a
                        if (!hasErr) {
                            ErrorRecorder.putError(ErrorType.a, lineNum);
                            hasErr = true;
                        }
                    }
                } else if (!(programCode.charAt(pointer) == 32
                        || programCode.charAt(pointer) == 33
                        || (40 <= programCode.charAt(pointer) && programCode.charAt(pointer) <= 126)) && programCode.charAt(pointer) != '\"') {
                    // Error a
                    if (!hasErr) {
                        ErrorRecorder.putError(ErrorType.a, lineNum);
                        hasErr = true;
                    }
                }
                catToken();
                getChar();
            } while (c != '\"');
            catToken();
            symbol = Symbols.STRCON;
        } else if (c == '+') {
            catToken();
            symbol = Symbols.PLUS;
        } else if (c == '-') {
            catToken();
            symbol = Symbols.MINU;
        } else if (c == '*') {
            catToken();
            symbol = Symbols.MULT;
        } else if (c == '(') {
            catToken();
            symbol = Symbols.LPARENT;
        } else if (c == ')') {
            catToken();
            symbol = Symbols.RPARENT;
        } else if (c == '[') {
            catToken();
            symbol = Symbols.LBRACK;
        } else if (c == ']') {
            catToken();
            symbol = Symbols.RBRACK;
        } else if (c == '{') {
            catToken();
            symbol = Symbols.LBRACE;
        } else if (c == '}') {
            catToken();
            symbol = Symbols.RBRACE;
        } else if (c == ',') {
            catToken();
            symbol = Symbols.COMMA;
        } else if (c == ';') {
            catToken();
            symbol = Symbols.SEMICN;
        } else if (c == '/') {
            catToken();
            getChar();
            if (c == '*') {
                do {
                    do {
                        isNewLine();
                        getChar();
                    } while (c != '*');
                    do {
                        isNewLine();
                        getChar();
                        if (c == '/') {
                            catToken();
                            return -1;
                        }
                    } while (c == '*');
                } while (pointer < programCode.length());
            } else if (c == '/') {
                do {
                    getChar();
                } while (!isNewLine());

                return -1;
            } else {
                retract();
                symbol = Symbols.DIV;
            }
        } else if (c == '%') {
            catToken();
            symbol = Symbols.MOD;
        } else if (c == '<') {
            catToken();
            getChar();
            if (c == '=') {
                catToken();
                symbol = Symbols.LEQ;
            } else {
                retract();
                symbol = Symbols.LSS;
            }
        } else if (c == '>') {
            catToken();
            getChar();
            if (c == '=') {
                catToken();
                symbol = Symbols.GEQ;
            } else {
                retract();
                symbol = Symbols.GRE;
            }
        } else if (c == '=') {
            catToken();
            getChar();
            if (c == '=') {
                catToken();
                symbol = Symbols.EQL;
            } else {
                retract();
                symbol = Symbols.ASSIGN;
            }
        } else if (c == '!') {
            catToken();
            getChar();
            if (c == '=') {
                catToken();
                symbol = Symbols.NEQ;
            } else {
                retract();
                symbol = Symbols.NOT;
            }
        } else if (c == '&') {
            catToken();
            getChar();
            if (c == '&') {
                catToken();
                symbol = Symbols.AND;
            } else {
                retract();

                return -1;
            }
        } else if (c == '|') {
            catToken();
            getChar();
            if (c == '|') {
                catToken();
                symbol = Symbols.OR;
            } else {
                retract();

                return -1;
            }
        }

        return 0;
    }

    public void analyze() {
        lineNum = 1;
        while (unReachEnd()) {       // pointer == length()-1
            if (getSym() == 0) {
                if (symbol.equals(Symbols.STRCON)) {
                    tokens.add(new Token(symbol, token.toString(), lineNum, formatCharNum));
                } else {
                    tokens.add(new Token(symbol, token.toString(), lineNum));
                }
            }
        }
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }
}