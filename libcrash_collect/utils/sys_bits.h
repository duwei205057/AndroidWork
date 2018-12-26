/* 64位系统下 ADDR为64位整型， 32位系统下则为32位 */
#if defined(__aarch64__)
    typedef unsigned long long addr_s;
#else
    typedef unsigned long addr_s;
#endif
