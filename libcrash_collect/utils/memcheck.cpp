//
// Created by songtao on 2019/5/14.
//
#include <unistd.h>
#include <dlfcn.h>
#include <stdio.h>
#include <vector>
#include "memcheck.h"

static bool g_bInited = false;
struct t_pairMemory {
    addr_s m_pStart;
    addr_s m_pEnd;
};  //[start,end)
typedef std::vector<t_pairMemory> t_blocks;
static t_blocks g_memBlock;

int init_mem_block()
{
    pid_t pid ;
    char access, maps[32] , buff[1024]={0};
    addr_s start_addr, end_addr, last_addr;
    FILE *fmap;

    pid = getpid();
    sprintf(maps, "/proc/%d/maps", pid);
    fmap = fopen(maps, "rb");
    if(!fmap){
        printf("open %s file failed!/n", maps);
        return -1;
    }

    while(fgets(buff, sizeof(buff)-1, fmap) != NULL) {
        /* "%*c"表示忽略第一个字符 */
#if defined(__aarch64__)
        sscanf(buff, "%lx-%lx %c", &start_addr, &end_addr, &access);
#else
        sscanf(buff, "%x-%x %c", &start_addr, &end_addr, &access);
#endif
        if('r' != access){
            continue;
        }
        t_pairMemory pair = {start_addr, end_addr};
        g_memBlock.push_back(pair);
    }
    fclose(fmap);
    return 0;
}

int check_mem_readable(addr_s addr, int len) {
    if (!g_bInited) {
        g_bInited = true;
        init_mem_block();
    }
    int nLen = 0;
    int nCnt = g_memBlock.size();
    for (int i = 0; i < nCnt; i++) {
        addr_s start_addr = g_memBlock[i].m_pStart;
        addr_s end_addr = g_memBlock[i].m_pEnd;
        if ( addr < start_addr ) {
            break;
        } else if (addr < end_addr) {
            if ((addr + len) < end_addr) {
                nLen += len;
                break;
            } else {
                unsigned long long last_addr = end_addr;
                len = len - (end_addr - addr);
                addr = last_addr;
                nLen += (end_addr - addr);
            }
        }
    }
    return nLen;
}

addr_s get_real_pc(addr_s pc)
{
    if ( !check_mem_readable(pc, 4) ) {
        return pc; //not correct
    }
    //correct pc for unwind result is next pc
    if ( pc & 1) { //for thumb instruction
        if ( !check_mem_readable(pc - 5, 4) ) {
            return pc; //not correct
        }
        uint32_t code = *(uint32_t*)(pc-5);
        if ( (code & 0xF8000000) > 0xE0000000 ) { //32bit code
            pc -= 4;
        } else { //16bit code
            pc -= 2;
        }
    } else {
        pc -= 4;
    }
    return pc;
}

