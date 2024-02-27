package front.Error;

public class Error implements Comparable {
    private final ErrorType errorType;
    private final int lineNum;

    public Error(ErrorType errorType, int lineNum) {
        this.errorType = errorType;
        this.lineNum = lineNum;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public String toString() {
        return lineNum + " " + errorType;
    }

    @Override
    public int compareTo(Object o) {
        return this.lineNum - ((Error) o).lineNum;
    }
}
