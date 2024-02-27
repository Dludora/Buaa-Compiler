package mid;

public enum InstSet {
    add,
    sub,
    mul,
    sdiv,
    srem,
    icmp_eq,        // 比较指令 等于
    icmp_neq,       // 比较指令 不等于
    icmp_lt,        // 比较指令 小于
    icmp_gt,        // 比较指令 大于
    icmp_le,        // 比较指令 小于等于
    icmp_ge,        // 比较指令 大于等于
    and,            // 逻辑与
    or,             // 逻辑或
    call,           // 函数调用
    alloca,         // 分配内存
    load,           // 读取内存
    store,          // 写内存
    getelementptr,  // 计算目标元素的位置
    br,             // 无条件跳转        br label
    br_eq,        // 条件跳转         br_eq i1 a, label1, label2
    br_ne,
    ret,
    global,
    print,          // 输出
    getInt,         // 输入
    nop
}
