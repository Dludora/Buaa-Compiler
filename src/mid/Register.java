package mid;

public class Register {
    protected int regNum;
    protected RegType regType;

    public Register(int regNum, RegType regType) {
        this.regNum = regNum;
        this.regType = regType;
    }

    public int getRegNum() {
        return regNum;
    }

    public RegType getRegType() {
        return regType;
    }

    public void setRegNum(int regNum) {
        this.regNum = regNum;
    }

    public void setRegType(RegType regType) {
        this.regType = regType;
    }

    @Override
    public String toString() {
        if (regNum == -1)   return "";
        return "%t" + regNum;
    }


}
