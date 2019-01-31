#define MALLOC 1
#define REALLOC 2
#define FREE 3

#include <list>
#include <string>

void show_stack(size_t size, void *old_addr, void *new_addr, int op);
void show_leak();
using namespace std;

class MemInfo {
    uint32_t *alloc_addr;
    uint32_t *recover_addr;
    int mem_op;
    list<string> instruct_list;
};
