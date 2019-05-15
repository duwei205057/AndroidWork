//
// Created by songtao on 2019/5/14.
//

#ifndef TESTCRASH_MEMCHECK_H
#define TESTCRASH_MEMCHECK_H
#include "sys_bits.h"
int check_mem_readable(addr_s addr, int len);
addr_s get_real_pc(addr_s pc);
#endif //TESTCRASH_MEMCHECK_H
