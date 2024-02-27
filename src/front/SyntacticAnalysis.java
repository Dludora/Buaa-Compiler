package front;

import front.ASD.*;
import front.ASD.Number;
import front.Error.ErrorRecorder;
import front.Error.ErrorType;

import java.util.ArrayList;

public class SyntacticAnalysis {
    private ASDNode ASDRoot = null;
    private final ArrayList<Token> tokens;
    private static int p = 1;
    private Symbols symbol;
    private boolean inCond = false;
    private boolean needReturn = false;
    private boolean inLoop = false;

    public SyntacticAnalysis(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.symbol = tokens.get(0).getTokenClass();
    }

    private void getSym() {
        if (p == tokens.size()) {
            error();
        }
        System.out.println(tokens.get(p - 1));
        symbol = tokens.get(p++).getTokenClass();
    }

    private Token getToken() {
        return tokens.get(p - 1);
    }

    private Symbols preView(int num) {
        if (p + num >= tokens.size()) {
            return Symbols.NONE;
        }
        return tokens.get(p + num - 1).getTokenClass();
    }

    private void error() {
        System.out.println(getToken().getLineNum());
        System.exit(1);
    }

    private FormatString FormatString() {
        return new FormatString(tokens.get(p - 1));
    }

    private Ident Ident() {
        return new Ident(tokens.get(p - 1));
    }

    private BType BType() {
        getSym();
        return new BType(BType.Type.Int);
    }

    private Number Number() {
        IntConst intConst = null;
        if (symbol == Symbols.INTCON) {
            intConst = new IntConst(getToken());
            getSym();
        }

        System.out.println("<Number>");
        return new Number(intConst);
    }

    private Exp Exp() {
        /*
         *   表达式
         *   AddExp 已完成
         * */
        AddExp addExp = AddExp();

        System.out.println("<Exp>");
        return new Exp(addExp);
    }

    private Cond Cond() {
        inCond = true;
        LOrExp lOrExp = null;
        if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
            lOrExp = LOrExp();
        }
        inCond = false;


        System.out.println("<Cond>");
        return new Cond(lOrExp);
    }

    private LVal LVal() {
        /*
         *   左值表达式
         *   Exp 已完成
         * */
        Ident ident = null;
        ArrayList<Exp> exps = new ArrayList<>();
        if (symbol == Symbols.IDENFR) {
            ident = Ident();
            getSym();
            if (symbol == Symbols.LBRACK) { // 可能是一维数组
                getSym();
                if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                    exps.add(Exp());
                }
                if (symbol == Symbols.RBRACK) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少右中括号’]’ k
                    ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
                }
                if (symbol == Symbols.LBRACK) {    // 可能是二维数组
                    getSym();
                    if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                        exps.add(Exp());
                    }
                    if (symbol == Symbols.RBRACK) {
                        getSym();
                    } else {
                        // TODO: 2022/11/29 缺少右中括号’]’ i
                        ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
                    }
                }
            }
        }

        System.out.println("<LVal>");
        return new LVal(ident, exps);
    }

    private PrimaryExp PrimaryExp() {
        /*
         *   基本表达式
         *   Exp 已完成
         *   Number 已完成
         *   LVal 已完成
         * */
        PrimaryExp.Type type = null;
        ArrayList<ASDNode> asdNodes = new ArrayList<>();
        // '(' Exp ')'
        if (symbol == Symbols.LPARENT) {
            type = PrimaryExp.Type.Exp;
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                asdNodes.add(Exp());
            }
            if (symbol == Symbols.RPARENT) {
                getSym();
            } else {
                // TODO: 2022/12/1 缺少右小括号 j
                ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
            }
        } else if (symbol == Symbols.INTCON) {  // Number
            type = PrimaryExp.Type.Number;
            asdNodes.add(Number());
        } else if (symbol == Symbols.IDENFR) {  // LVal
            type = PrimaryExp.Type.LVal;
            asdNodes.add(LVal());
        }

        System.out.println("<PrimaryExp>");
        return new PrimaryExp(type, asdNodes);
    }

    private UnaryOp UnaryOP() {
        UnaryOp unaryOp = null;
        if (symbol == Symbols.PLUS || symbol == Symbols.MINU) {
            unaryOp = new UnaryOp(getToken());
            getSym();
        } else if (symbol == Symbols.NOT && inCond) {
            unaryOp = new UnaryOp(getToken());
            // 追加判断在条件表达式中
            getSym();
        }

        System.out.println("<UnaryOp>");
        return unaryOp;
    }

    private UnaryExp UnaryExp() {
        /*
         *   一元表达式
         *   FuncRParams
         *   PrimaryExp 已完成
         *   UnaryOp 已完成
         * */
        UnaryExp.Type type;
        ArrayList<ASDNode> asdNodes = new ArrayList<>();
        // Ident '(' [FuncRParams] ') || LVal(in PrimaryExp) = Ident { '[' Exp ']' }
        if (symbol == Symbols.IDENFR) {
            // Ident '(' [FuncRParams] ')
            if (preView(1) == Symbols.LPARENT) {
                asdNodes.add(Ident());
                type = UnaryExp.Type.FuncCall;
                getSym();
                getSym();
                if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                    asdNodes.add(FuncRParams());
                } else {
                    asdNodes.add(new FuncRParams(new ArrayList<>()));
                }
                if (!symbol.equals(Symbols.RPARENT)) {
                    // TODO: 2022/11/29 缺少右小括号 j
                    ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                } else {
                    getSym();
                }
            } else {    // LVal = Ident { '[' Exp ']' } in PrimaryExp
                type = UnaryExp.Type.PrimaryExp;
                asdNodes.add(PrimaryExp());
            }
        } else if (symbol == Symbols.LPARENT || symbol == Symbols.INTCON) {     // PrimaryExp
            type = UnaryExp.Type.PrimaryExp;
            asdNodes.add(PrimaryExp());
        } else {
            type = UnaryExp.Type.mulUnaryExp;
            asdNodes.add(UnaryOP());
            asdNodes.add(UnaryExp());
        }

        System.out.println("<UnaryExp>");
        return new UnaryExp(type, asdNodes);
    }

    private MulExp MulExp() {
        ArrayList<UnaryExp> unaryExps = new ArrayList<>();
        ArrayList<Token> Ops = new ArrayList<>();

        unaryExps.add(UnaryExp());
        while (symbol == Symbols.MULT || symbol == Symbols.DIV || symbol == Symbols.MOD || symbol.equals(Symbols.BITAND)) {
            Ops.add(getToken());
            System.out.println("<MulExp>");
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                unaryExps.add(UnaryExp());
            }
        }

        System.out.println("<MulExp>");
        return new MulExp(unaryExps, Ops);
    }

    private AddExp AddExp() {
        ArrayList<MulExp> mulExps = new ArrayList<>();
        ArrayList<Token> Ops = new ArrayList<>();

        mulExps.add(MulExp());
        while (symbol == Symbols.PLUS || symbol == Symbols.MINU) {
            Ops.add(getToken());
            System.out.println("<AddExp>");
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                mulExps.add(MulExp());
            }
        }

        System.out.println("<AddExp>");
        return new AddExp(mulExps, Ops);
    }

    private RelExp RelExp() {
        ArrayList<AddExp> addExps = new ArrayList<>();
        ArrayList<Token> Ops = new ArrayList<>();

        addExps.add(AddExp());
        while (symbol == Symbols.LSS || symbol == Symbols.LEQ || symbol == Symbols.GRE || symbol == Symbols.GEQ) {
            Ops.add(getToken());
            System.out.println("<RelExp>");
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                addExps.add(AddExp());
            }
        }

        System.out.println("<RelExp>");
        return new RelExp(addExps, Ops);
    }

    private EqExp EqExp() {
        ArrayList<Token> Ops = new ArrayList<>();
        ArrayList<RelExp> relExps = new ArrayList<>();

        relExps.add(RelExp());
        while (symbol == Symbols.EQL || symbol == Symbols.NEQ) {
            Ops.add(getToken());
            System.out.println("<EqExp>");
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                relExps.add(RelExp());
            }
        }

        System.out.println("<EqExp>");
        return new EqExp(Ops, relExps);
    }

    private LAndExp LAndExp() {
        ArrayList<EqExp> eqExps = new ArrayList<>();

        eqExps.add(EqExp());
        while (symbol == Symbols.AND) {
            System.out.println("<LAndExp>");
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                eqExps.add(EqExp());
            }
        }

        System.out.println("<LAndExp>");
        return new LAndExp(eqExps);
    }

    private LOrExp LOrExp() {
        ArrayList<LAndExp> lAndExps = new ArrayList<>();

        lAndExps.add(LAndExp());
        while (symbol == Symbols.OR) {
            System.out.println("<LOrExp>");
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                lAndExps.add(LAndExp());
            }
        }

        System.out.println("<LOrExp>");
        return new LOrExp(lAndExps);
    }

    private FuncRParams FuncRParams() {
        ArrayList<Exp> exps = new ArrayList<>();

        exps.add(Exp());
        while (symbol == Symbols.COMMA) {
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                exps.add(Exp());
            }
        }

        System.out.println("<FuncRParams>");
        return new FuncRParams(exps);
    }

    private ConstExp ConstExp() {
        AddExp addExp = AddExp();

        System.out.println("<ConstExp>");
        return new ConstExp(addExp);
    }

    private Stmt Stmt() {
        Stmt.Type type = null;
        ArrayList<ASDNode> asdNodes = new ArrayList<>();

        if (symbol == Symbols.IFTK) {
            type = Stmt.Type.ifBranch;
            getSym();
            if (symbol == Symbols.LPARENT) {
                getSym();
                if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                    asdNodes.add(Cond());
                }
                if (symbol == Symbols.RPARENT) {
                    getSym();

                    asdNodes.add(Stmt());
                    if (symbol == Symbols.ELSETK) {
                        getSym();
                        asdNodes.add(Stmt());
                    }
                } else {
                    // TODO: 2022/11/29 缺少右小括号) j
                    ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                }
            } else {
                error();
            }
        } else if (symbol == Symbols.WHILETK) {
            type = Stmt.Type.whileBranch;
            getSym();
            if (symbol == Symbols.LPARENT) {
                getSym();
                if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.NOT) || symbol.equals(Symbols.INTCON)) {
                    asdNodes.add(Cond());
                }
                if (symbol == Symbols.RPARENT) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少右小括号) j
                    ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                }
                inLoop = true;
                asdNodes.add(Stmt());
            } else {
                error();
            }
            inLoop = false;
        } else if (symbol == Symbols.BREAKTK || symbol == Symbols.CONTINUETK) {
            // TODO: 2022/11/29 在非循环块中使用break和continue语句 m
            if (!inLoop) {
                ErrorRecorder.putError(ErrorType.m, getToken().getLineNum());
            }
            type = symbol == Symbols.BREAKTK ? Stmt.Type.breakStmt : Stmt.Type.continueStmt;
            int lineNum = getToken().getLineNum();
            getSym();
            if (symbol == Symbols.SEMICN) {
                getSym();
            } else {
                // TODO: 2022/11/29 缺少分号 i
                ErrorRecorder.putError(ErrorType.i, lineNum);
            }
        } else if (symbol == Symbols.RETURNTK) {
            needReturn = false;
            type = Stmt.Type.returnStmt;
            asdNodes.add(new Return(getToken()));
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                asdNodes.add(Exp());
            }
            if (symbol.equals(Symbols.SEMICN)) {
                getSym();
            } else {
                // TODO: 2022/11/29 缺少分号 i
                ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
            }
        } else if (symbol == Symbols.PRINTFTK) {
            int lineNum = getToken().getLineNum();
            // 'printf''('FormatString{','Exp}')'';'
            type = Stmt.Type.Output;
            getSym();
            if (symbol == Symbols.LPARENT) {
                getSym();
                if (symbol == Symbols.STRCON) {
                    // getFormatString
                    FormatString asdNode = FormatString();
                    asdNodes.add(asdNode);
                    int formNum = asdNode.getFormatCharNum();
                    int expNum = 0;
                    getSym();
                    while (symbol == Symbols.COMMA) {
                        getSym();
                        expNum++;
                        if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                            asdNodes.add(Exp());
                        }
                    }
                    // TODO: 2022/11/29 printf中格式字符与表达式个数不匹配 l
                    if (formNum != expNum) {
                        ErrorRecorder.putError(ErrorType.l, lineNum);
                    }
                    if (symbol == Symbols.RPARENT) {
                        getSym();
                        if (symbol == Symbols.SEMICN) {
                            getSym();
                        } else {
                            // TODO: 2022/11/29 缺少分号; i
                            ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
                        }
                    } else {
                        // TODO: 2022/11/29 缺少右小括号) j
                        ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                    }
                }
            }
        } else if (symbol == Symbols.LBRACE) {
            // Block
            type = Stmt.Type.Block;
            asdNodes.add(Block());
        } else if (symbol == Symbols.IDENFR) {
            /*
             * Exp;
             * LVal = Exp;
             * LVal = getint()
             * */
            boolean flag = false;
            for (int i = 1; preView(i) != Symbols.SEMICN; i++) {
                if (preView(i) == Symbols.ASSIGN) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                asdNodes.add(LVal());
                if (symbol == Symbols.ASSIGN) {
                    getSym();
                    if (symbol == Symbols.GETINTTK) {
                        //  LVal '=' 'getint''('')'';'
                        type = Stmt.Type.Input;
                        getSym();
                        if (symbol == Symbols.LPARENT) {
                            getSym();
                            if (symbol == Symbols.RPARENT) {
                                getSym();
                                if (symbol == Symbols.SEMICN) {
                                    getSym();
                                } else {
                                    // TODO: 2022/11/29 缺少分号; i
                                    ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
                                }
                            } else {
                                // TODO: 2022/11/29 缺少右小括号) j
                                ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                            }
                        } else {
                            error();
                        }
                    } else {
                        //  LVal '=' Exp ';'
                        type = Stmt.Type.ASSIGN;
                        if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                            asdNodes.add(Exp());
                        }
                        if (symbol == Symbols.SEMICN) {
                            getSym();
                        } else {
                            // TODO: 2022/11/29 缺少分号; i
                            ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
                        }
                    }
                } else {
                    error();
                }
            } else {
                // Exp;
                type = Stmt.Type.Exp;
                if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                    asdNodes.add(Exp());
                }
                if (symbol == Symbols.SEMICN) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少分号; i
                    ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
                }
            }
        } else {
            if (symbol == Symbols.SEMICN) {
                // ;
                type = Stmt.Type.None;
                getSym();
            } else {
                // Exp;
                type = Stmt.Type.Exp;
                if (symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                    asdNodes.add(Exp());
                }
                if (symbol == Symbols.SEMICN) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少分号; i
                    ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
                }
            }
        }

        System.out.println("<Stmt>");
        return new Stmt(type, asdNodes);
    }

    private BlockItem BlockItem() {
        /*
         *   语句块项
         *   Decl
         *   Stmt
         * */
        Decl decl = null;
        Stmt stmt = null;
        if (symbol == Symbols.CONSTTK || symbol == Symbols.INTTK) {
            decl = Decl();
        } else {
            stmt = Stmt();
        }

        return new BlockItem(decl, stmt);
    }

    private Block Block() {
        /*
         *   语句块
         *   BlockItem
         * */
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        if (symbol == Symbols.LBRACE) {
            getSym();
            while (symbol != Symbols.RBRACE) {
                blockItems.add(BlockItem());
            }
            if (p < tokens.size()) {
                getSym();
            } else {
                System.out.println(getToken());
            }
        } else {
            error();
        }

        System.out.println("<Block>");
        return new Block(blockItems);
    }

    private MainFuncDef MainFuncDef() {
        MainFuncDef mainFuncDef = null;
        if (symbol == Symbols.INTTK) {
            getSym();
            if (symbol == Symbols.MAINTK) {
                getSym();
                if (symbol == Symbols.LPARENT) {
                    getSym();
                    if (symbol == Symbols.RPARENT) {
                        getSym();
                        needReturn = true;
                        // 构造
                        ErrorRepresent blockEnd = new ErrorRepresent(tokens.get(p - 1));
                        mainFuncDef = new MainFuncDef(Block(), blockEnd);
                        // TODO: 2022/11/29 有返回值的函数缺少Return语句 g
                        if (needReturn) {
                            ErrorRecorder.putError(ErrorType.g, getToken().getLineNum());
                            needReturn = false;
                        }
                    } else {
                        // TODO: 2022/11/30 缺少右小括号 j
                        ErrorRecorder.putError(ErrorType.j, getToken().getLineNum());
                    }
                } else {
                    error();
                }
            } else {
                error();
            }
        } else {
            error();
        }

        System.out.println("<MainFuncDef>");
        return mainFuncDef;
    }

    private FuncFParam FuncFParam() {
        /*
         *   函数形参
         *   BType 完成
         *   ConstExp
         * */
        BType bType = BType();
        Ident ident = null;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        int dimension = 0;

        if (symbol == Symbols.IDENFR) {
            ident = Ident();
            getSym();
        } else {
            error();
        }
        if (symbol == Symbols.LBRACK) {
            dimension += 1;
            getSym();
            if (symbol == Symbols.RBRACK) {
                getSym();
            } else {
                // TODO: 2022/11/29 缺少右中括号’]’; k
                ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
            }
            if (symbol == Symbols.LBRACK) {
                dimension += 1;
                getSym();
                constExps.add(ConstExp());
                if (symbol == Symbols.RBRACK) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少分号; i
                    ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
                }
            }
        }

        System.out.println("<FuncFParam>");
        return new FuncFParam(bType, ident, constExps, dimension);
    }

    private FuncFParams FuncFParams() {
        /*
         *   函数形参表
         *   FuncFParam 已完成
         * */
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        funcFParams.add(FuncFParam());
        while (symbol == Symbols.COMMA) {
            getSym();
            funcFParams.add(FuncFParam());
        }

        System.out.println("<FuncFParams>");
        return new FuncFParams(funcFParams);
    }

    private ConstDef ConstDef() {
        Ident ident;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        ConstInitVal constInitVal = null;

        ident = Ident();
        getSym();
        if (symbol == Symbols.LBRACK) {
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                constExps.add(ConstExp());
            }
            if (symbol == Symbols.RBRACK) {
                getSym();
            } else {
                // TODO: 2022/11/29 缺少右中括号] k
                ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
            }
            if (symbol == Symbols.LBRACK) { // 可能是二维
                getSym();
                if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON)) {
                    constExps.add(ConstExp());
                }
                if (symbol == Symbols.RBRACK) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少右中括号] k
                    ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
                }
            }
        }
        if (symbol == Symbols.ASSIGN) {
            getSym();
            if (symbol.equals(Symbols.IDENFR) || symbol.equals(Symbols.PLUS) || symbol.equals(Symbols.LPARENT) || symbol.equals(Symbols.CONSTTK) || symbol.equals(Symbols.MINU) || symbol.equals(Symbols.INTCON) || symbol.equals(Symbols.LBRACE)) {
                constInitVal = ConstInitVal();
            }
        } else {
            error();
        }

        System.out.println("<ConstDef>");
        return new ConstDef(ident, constExps, constInitVal);
    }

    private ConstInitVal ConstInitVal() {
        ConstInitVal.Type type;
        ArrayList<ASDNode> asdNodes = new ArrayList<>();

        if (symbol == Symbols.LBRACE) {
            type = ConstInitVal.Type.mulInitVal;
            getSym();
            if (symbol == Symbols.RBRACE) {
                getSym();
            } else {
                asdNodes.add(ConstInitVal());
                while (symbol == Symbols.COMMA) {
                    getSym();
                    asdNodes.add(ConstInitVal());
                }
                if (symbol == Symbols.RBRACE) {
                    getSym();
                } else {
                    error();
                }
            }
        } else {
            type = ConstInitVal.Type.Exp;
            asdNodes.add(ConstExp());
        }

        System.out.println("<ConstInitVal>");
        return new ConstInitVal(type, asdNodes);
    }

    private VarDecl VarDecl() {
        BType bType = BType();
        ArrayList<VarDef> varDefs = new ArrayList<>();

        varDefs.add(VarDef());
        while (symbol == Symbols.COMMA) {
            getSym();
            varDefs.add(VarDef());
        }
        if (symbol == Symbols.SEMICN) {
            getSym();
        } else {
            // TODO: 2022/11/29 缺少分号; i
            ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
        }

        System.out.println("<VarDecl>");
        return new VarDecl(bType, varDefs);
    }

    private VarDef VarDef() {
        Ident ident = null;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        InitVal initVal = null;
        VarDef varDef = null;

        if (symbol == Symbols.IDENFR) {
            ident = Ident();
            getSym();
            if (symbol == Symbols.LBRACK) {
                getSym();
                constExps.add(ConstExp());
                if (symbol == Symbols.RBRACK) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少右中括号] k
                    ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
                }
                if (symbol == Symbols.LBRACK) {
                    getSym();
                    constExps.add(ConstExp());
                    if (symbol == Symbols.RBRACK) {
                        getSym();
                    } else {
                        // TODO: 2022/11/29 缺少右中括号] k
                        ErrorRecorder.putError(ErrorType.k, tokens.get(p - 2).getLineNum());
                    }
                }
            }
            if (symbol == Symbols.ASSIGN) {
                getSym();

                if (symbol.equals(Symbols.GETINTTK)) {
                    varDef = new VarDef(ident, constExps);
                    varDef.setType(VarDef.Type.getint);
                    getSym();
                    if (symbol == Symbols.LPARENT) {
                        getSym();
                        if (symbol == Symbols.RPARENT) {
                            getSym();
                            if (symbol == Symbols.SEMICN) {
                                getSym();
                            } else {
                                // TODO: 2022/11/29 缺少分号; i
                                ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
                            }
                        } else {
                            // TODO: 2022/11/29 缺少右小括号) j
                            ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                        }
                    } else {
                        error();
                    }
                } else {
                    initVal = InitVal();
                    varDef = new VarDef(ident, constExps, initVal);
                    varDef.setType(VarDef.Type.normal);
                }
            }
        } else {
            error();
        }
        varDef = varDef == null ? new VarDef(ident, constExps) : varDef;

        System.out.println("<VarDef>");

        return varDef;
    }

    private InitVal InitVal() {
        InitVal.Type type;
        ArrayList<ASDNode> asdNodes = new ArrayList<>();

        if (symbol == Symbols.LBRACE) {
            // 是数组初始值
            type = InitVal.Type.mulInitVal;
            getSym();
            if (symbol == Symbols.RBRACE) {
                // 是空数组
                getSym();
            } else {
                // 非空数组
                asdNodes.add(InitVal());
                while (symbol == Symbols.COMMA) {
                    getSym();
                    asdNodes.add(InitVal());
                }
                if (symbol == Symbols.RBRACE) {
                    getSym();
                } else {
                    error();
                }
            }
        } else {
            // 是普通变量初始值
            asdNodes.add(Exp());
            type = InitVal.Type.Exp;
        }

        System.out.println("<InitVal>");
        return new InitVal(type, asdNodes);
    }

    private FuncType FuncType() {
        /*
         *   函数类型
         *   已完成
         * */
        if (symbol != Symbols.VOIDTK && symbol != Symbols.INTTK) {
            error();
        }
        Tools.FuncType type = symbol.equals(Symbols.VOIDTK) ? Tools.FuncType.Void : Tools.FuncType.Int;
        if (type.equals(Tools.FuncType.Int)) {
            needReturn = true;
        }
        getSym();
        System.out.println("<FuncType>");

        return new FuncType(type);
    }

    private FuncDef FuncDef() {
        /*
         *   函数定义
         *   FuncType
         *   FuncFParams
         *   Block
         * */
        FuncType funcType = FuncType();
        Ident ident = null;
        FuncFParams funcFParams = null;
        Block block = null;

        if (symbol == Symbols.IDENFR) {
            ident = Ident();
            getSym();
        } else {
            error();
        }
        if (symbol == Symbols.LPARENT) {
            getSym();
            // 无形参函数
            if (symbol != Symbols.INTTK) {
                // TODO: 2022/11/29 缺少右小括号) j
                if (!symbol.equals(Symbols.RPARENT)) {
                    ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                } else {
                    getSym();
                }
                block = Block();
                funcFParams = new FuncFParams(new ArrayList<>());
            } else {
                // 有形参函数
                funcFParams = FuncFParams();
                if (symbol == Symbols.RPARENT) {
                    getSym();
                } else {
                    // TODO: 2022/11/29 缺少右小括号) j
                    ErrorRecorder.putError(ErrorType.j, tokens.get(p - 2).getLineNum());
                }
                block = Block();
            }
        } else {
            error();
        }
        ErrorRepresent blockEnd = new ErrorRepresent(tokens.get(p - 1));
        // TODO: 2022/11/29 需要返回值的函数缺少Return语句 g
        if (needReturn) {
            ErrorRecorder.putError(ErrorType.g, tokens.get(p - 2).getLineNum());
            needReturn = false;
        }
        System.out.println("<FuncDef>");
        return new FuncDef(funcType, ident, funcFParams, block, blockEnd);
    }

    private ConstDecl ConstDecl() {
        BType bType = null;
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        getSym();
        if (symbol.equals(Symbols.INTTK)) {
            bType = BType();
        }
        if (symbol.equals(Symbols.IDENFR)) {
            constDefs.add(ConstDef());
        }
        while (symbol == Symbols.COMMA) {
            getSym();
            if (symbol.equals(Symbols.IDENFR)) {
                constDefs.add(ConstDef());
            }
        }
        if (symbol == Symbols.SEMICN) {
            getSym();
        } else {
            // TODO: 2022/11/29 缺少分号; i
            ErrorRecorder.putError(ErrorType.i, tokens.get(p - 2).getLineNum());
        }

        System.out.println("<ConstDecl>");
        return new ConstDecl(bType, constDefs);
    }

    private Decl Decl() {
        ConstDecl constDecl = null;
        VarDecl varDecl = null;
        if (symbol == Symbols.CONSTTK) {
            constDecl = ConstDecl();
        } else {
            varDecl = VarDecl();
        }
        return new Decl(constDecl, varDecl);
    }

    private CompUnit CompUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        while (p < tokens.size()) {
            if (symbol == Symbols.INTTK) {
                if (preView(1) == Symbols.MAINTK) {
                    mainFuncDef = MainFuncDef();
                } else {
                    if (preView(2) == Symbols.LPARENT) {     // == '('
                        funcDefs.add(FuncDef());
                    } else {
                        decls.add(Decl());
                    }
                }
            } else if (symbol == Symbols.CONSTTK) {
                decls.add(Decl());
            } else if (symbol == Symbols.VOIDTK) {
                funcDefs.add(FuncDef());

            } else {
                error();
            }
        }

        System.out.println("<CompUnit>");
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    public void analyze() {
        this.ASDRoot = CompUnit();
    }

    public CompUnit getASDTree() {
        return (CompUnit) ASDRoot;
    }
}
