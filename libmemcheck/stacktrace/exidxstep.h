#include <stdint.h>
#include <asm/sigcontext.h>
#include <common/log.h>
#define ARM_EXIDX_CANT_UNWIND	0x00000001
#define ARM_EXIDX_COMPACT	0x80000000

#define ARM_EXTBL_OP_FINISH	0xb0
#define STEP_STOP   -1
#define STEP_PC_LR   -2
#define NUM_REGS 17
typedef struct trace_cursor {
  unsigned long regs[NUM_REGS]; //保存寄存器的数组
  uint32_t base_addr;  //当前so的基地址
  uint32_t pc_offest;  //当前指令的偏移地址
  int step_status;  //-1 栈回溯结束
  uint32_t exidx_start_addr; //去除了cantunwind数据后的exidx段的物理起始地址
  uint32_t exidx_end_addr; //exidx段最后一条key-value的地址
} t_cursor;

enum
{
  ARM_R0 = 0,
  ARM_R1, ARM_R2, ARM_R3, ARM_R4, ARM_R5, ARM_R6, ARM_R7, ARM_R8, 
  ARM_R9, ARM_SL, ARM_FP, ARM_IP, ARM_SP, ARM_LR, ARM_PC, ARM_CPSR
};

typedef enum arm_exbuf_cmd {
  ARM_EXIDX_CMD_FINISH,
  ARM_EXIDX_CMD_DATA_PUSH,
  ARM_EXIDX_CMD_DATA_POP,
  ARM_EXIDX_CMD_REG_POP,
  ARM_EXIDX_CMD_REG_TO_SP,
  ARM_EXIDX_CMD_VFP_POP,
  ARM_EXIDX_CMD_WREG_POP,
  ARM_EXIDX_CMD_WCGR_POP,
  ARM_EXIDX_CMD_RESERVED,
  ARM_EXIDX_CMD_REFUSED,
} arm_exbuf_cmd_t;

struct arm_exbuf_data
{
  arm_exbuf_cmd_t cmd;
  uint32_t data;
};

enum arm_exbuf_cmd_flags {
  ARM_EXIDX_VFP_SHIFT_16 = 1 << 16,
  ARM_EXIDX_VFP_DOUBLE = 1 << 17,
};

int arm_exidx_step(t_cursor *c);
int arm_exidx_extract (t_cursor *c, uint8_t *buf);
int arm_exidx_decode (const uint8_t *buf, uint8_t len, t_cursor *c);
int arm_exidx_apply_cmd (arm_exbuf_data *edata, t_cursor *c);
uint32_t search_exidx_value(uint32_t *start, uint32_t *end, uint32_t base_addr, uint32_t key);
int copyregs_to_cursor(sigcontext *sig_ctx, t_cursor *c);
int copyregs_to_sigctx(t_cursor *c, sigcontext *sig_ctx);
