#if defined(__arm__)
#include "exidxstep.h"

int get_off_pre31(uint32_t pre31) {
  return ((int)pre31<<1)>>1;
}

int arm_exidx_step(t_cursor *c) {
  uint8_t buf[32];
  int ret;

  ret = arm_exidx_extract(c, buf);
  if(ret > 0)
    ret = arm_exidx_decode(buf, ret, c);
  if(c->step_status == STEP_STOP||ret == -1) {
    return -1;
  }else if(c->step_status == STEP_PC_LR){
    return -2;
  }
  return 0;
}

/* 
 * 获取当前指令的exidx数据
 * 成功返回 >0，失败返回 0，找不到exidx数据，设置回溯状态
 */
int arm_exidx_extract (t_cursor *c, uint8_t *buf) {

  int nbuf = 0;
  uint32_t value;
  /* 
   * 一条指令的exidx中包含一个key-value对，key和value分别占32bits
   * 其中key表示函数的起始地址，value表示该函数的恢复现场的操作指令
   * 通过当前指令pc的值查找key，取到对应的value进行分析
   */
  uint32_t key_value_addr = search_exidx_value((addr_s *)c->exidx_start_addr, (addr_s *)c->exidx_end_addr, c->base_addr,c->pc_offest);
  if(key_value_addr == 0) {
    /* 找不到exidx数据，终止回溯 */
    c->step_status = STEP_STOP;
    return 0;
  }
  value = *((uint32_t *)key_value_addr + 1);

  /*
   * one
   * +-+----------------------+
   * |0| prel31_offset_to_fnc |
   * +-+----------------------+
   * 31 30                    0
   *                              two
   *                              +----------------------+-+
   *                              |                      |1|
   *                              +----------------------+-+
   *                              31                     1 0
   *                              two
   * The ex table entry itself    +-+----------------------+
   * encoded in 31bit             |1|     ex_tbl_entry     |
   *                              +-+----------------------+
   *                              31 30                    0
   *                              two
   * prel32 offset of the start   +-+----------------------+
   * of the table entry for       |0|   tbl_entry_offset   |
   * this function                +-+----------------------+
   *                              31 30                    0
   */
  if (value == ARM_EXIDX_CANT_UNWIND) {
      c->step_status = STEP_STOP;
      return 0;

  }else if (value & ARM_EXIDX_COMPACT) {
    /*
     * compact model:
     *  +-+-----+-----+----------+ +-----------------------
     *  |1| 0   | idx | prs_data | | optional_prs_data
     *  +-+-----+-----+----------+ +-----------------------
     *  31 30-28 27-24 23        0
     */
    buf[nbuf++] = value >> 16;
    buf[nbuf++] = value >> 8;
    buf[nbuf++] = value;
    CRASH_LOGD("compact model");
  }else {  
    /* not compact model, need to get data from extable */
    CRASH_LOGD("not compact model");
    uint32_t tbl_entry_offset = value;
    uint32_t tbl_entry_addr = (key_value_addr + 4 + get_off_pre31(tbl_entry_offset)); 
    uint32_t extal_value = *(uint32_t *)tbl_entry_addr;
    uint32_t n_add_insn = 0;
    if (extal_value & ARM_EXIDX_COMPACT) {
      /* get idx
       * short:
       *  +-+-----+-----+----------+
       *  |1|  0  |  0  | prs_data |
       *  +-+-----+-----+----------+
       *  31 30-28 2724/ 23       0 \
       *              /              \
       *              +----+----+----+
       *              |insn|insn|insn|
       *              +----+----+----+
       *              23-16 15-8 7 - 0
       * long:
       *  +-+-----+-----+----------+ +----------------------
       *  |1|  0  | 1or2| prs_data | |prs_data
       *  +-+-----+-----+----------+ +----------------------
       *  31 30-28 2724/ 23       0                         \
       *              /                                      \
       *              +----+----+----++----+----+----+----+---
       *              |  N |insn|insn||insn|insn|insn|insn|...
       *              +----+----+----++----+----+----+----+---
       *              23-16 15-8 7 - 0
       *  With 'N' specifying the number of additional insn (N 32bits)
       */
      int idx = (extal_value >> 24) & 0x0f;
      if (idx == 1 || idx == 2) {
        n_add_insn = (extal_value >> 16) & 0xff;
        tbl_entry_addr += 4;
      }else
        buf[nbuf++] = extal_value >> 16;
      buf[nbuf++] = extal_value >> 8;
      buf[nbuf++] = extal_value;
    }else {
      /* mark (not check, have a bug)
       * TODO: this has a bug.
       * generic model:
       *  +-+----------------------+ +-----------------------
       *  |0|    prs_fnc_offset    | | prs_data
       *  +-+----------------------+ +-----------------------
       *  31 30                    0
       */
      /*
      tbl_entry_addr = (get_off_pre31(extal_value) + tbl_entry_addr + 4);
      extal_value = *(uint32_t *)tbl_entry_addr;

      n_add_insn = extal_value >> 24;
      buf[nbuf++] = extal_value >> 16;
      buf[nbuf++] = extal_value >> 8;
      buf[nbuf++] = extal_value;
      tbl_entry_addr += 4;
      */
      //bug so rerurn -1
      return -1;
    }

    for (uint8_t i = 0; i < n_add_insn; i++) {
      /* n_add_insn size in 32bits */
      extal_value = *(uint32_t *) tbl_entry_addr;
      tbl_entry_addr += 4;
      buf[nbuf++] = extal_value >> 24;
      buf[nbuf++] = extal_value >> 16;
      buf[nbuf++] = extal_value >> 8;
      buf[nbuf++] = extal_value >> 0;
    }

  }
  
  if (nbuf > 0 && buf[nbuf - 1] != ARM_EXTBL_OP_FINISH)
    buf[nbuf++] = ARM_EXTBL_OP_FINISH;
  CRASH_LOGD("nbuf = %d", nbuf);
  return nbuf;
}

/* 
 * 解析指令
 * buf 保存指令码的地址
 * 正常返回 0 失败返回 -1
 */
int arm_exidx_decode (const uint8_t *buf, uint8_t len, t_cursor *c) {
  #define READ_OP() *buf++
  const uint8_t *end = buf + len;
  int ret;
  struct arm_exbuf_data edata;
  /* ----------------------------------------------------------------------------------------------------------------
   * Instruction       | Mnemonic                 | description
   * 00xxxxxx          | ARM_EXIDX_CMD_DATA_POP   | vsp = vsp + (xxxxxx << 2) + 4. Covers range 0x04-0x100 inclusive
   * 01xxxxxx          | ARM_EXIDX_CMD_DATA_PUSH  | vsp = vsp – (xxxxxx << 2) - 4. Covers range 0x04-0x100 inclusive
   * 10000000 00000000 | ARM_EXIDX_CMD_REFUSED    | refuse to unwind (0x80 followed by 0x00)
   * 1000iiii iiiiiiii | ARM_EXIDX_CMD_REG_POP    | Pop up to 12 integer registers under masks {r15-r12}, {r11-r4}
   * 1001nnnn          | ARM_EXIDX_CMD_REG_TO_SP  | Set vsp = r[nnnn]
   * 10100nnn          | ARM_EXIDX_CMD_REG_POP    | Pop r4-r[4+nnn]
   * 10101nnn          | ARM_EXIDX_CMD_REG_POP    | Pop r4-r[4+nnn], r14
   * 10110000          | ARM_EXIDX_CMD_FINISH     | Finish
   * 10110001 0000iiii | ARM_EXIDX_CMD_REG_POP    | Pop integer registers under mask {r3, r2, r1, r0}
   * 10110010 uleb128  | ARM_EXIDX_CMD_DATA_POP   | vsp = vsp + 0x204 + (uleb128 << 2)
   * 10110011 sssscccc | ARM_EXIDX_CMD_VFP_POP    | Pop VFP double-precision registers D[ssss]-D[ssss+cccc]
   * 10111nnn          | ARM_EXIDX_CMD_VFP_POP    | Pop VFP double-precision registers D[8]-D[8+nnn]
   * 11010nnn          | ARM_EXIDX_CMD_VFP_POP    | Pop VFP double-precision registers D[8]-D[8+nnn]
   * 11000nnn          | ARM_EXIDX_CMD_WREG_POP   | Intel Wireless MMX pop
   * 11000111 0000iiii | ARM_EXIDX_CMD_WCGR_POP   | Intel Wireless MMX pop WCGR registers under mask {wCGR3,2,1,0}
   * all other         | ARM_EXIDX_CMD_RESERVED   | Reserved or Spare
   * -----------------------------------------------------------------------------------------------------------------
   */
  while (buf < end) {
    uint8_t op = READ_OP();
    if ((op & 0xc0) == 0x00) {
      edata.cmd = ARM_EXIDX_CMD_DATA_POP;
      edata.data = (((int)op & 0x3f) << 2) + 4;
    }else if ((op & 0xc0) == 0x40) {
      edata.cmd = ARM_EXIDX_CMD_DATA_PUSH;
      edata.data = (((int)op & 0x3f) << 2) + 4;
    }else if ((op & 0xf0) == 0x80) {
      uint8_t op2 = READ_OP ();
      if (op == 0x80 && op2 == 0x00)
        edata.cmd = ARM_EXIDX_CMD_REFUSED;
      else {
        edata.cmd = ARM_EXIDX_CMD_REG_POP;
        edata.data = ((op & 0xf) << 8) | op2;
        edata.data = edata.data << 4;
      }
    }else if ((op & 0xf0) == 0x90) {
      if (op == 0x9d || op == 0x9f)
        edata.cmd = ARM_EXIDX_CMD_RESERVED;
      else {
        edata.cmd = ARM_EXIDX_CMD_REG_TO_SP;
        edata.data = op & 0x0f;
      }
    }else if ((op & 0xf0) == 0xa0) {
      unsigned end = (op & 0x07);
      edata.data = (1 << (end + 1)) - 1;
      edata.data = edata.data << 4;
      if (op & 0x08)
        edata.data |= 1 << 14;
      edata.cmd = ARM_EXIDX_CMD_REG_POP;
    }else if (op == ARM_EXTBL_OP_FINISH) {
      edata.cmd = ARM_EXIDX_CMD_FINISH;
      buf = end;
    }else if (op == 0xb1) {
      uint8_t op2 = READ_OP ();
      if (op2 == 0 || (op2 & 0xf0))
        edata.cmd = ARM_EXIDX_CMD_RESERVED;
      else {
        edata.cmd = ARM_EXIDX_CMD_REG_POP;
        edata.data = op2 & 0x0f;
      }
    }else if (op == 0xb2) {
      uint32_t offset = 0;
      uint8_t byte, shift = 0;
      do {
        byte = READ_OP ();
        offset |= (byte & 0x7f) << shift;
        shift += 7;
      }while (byte & 0x80);
      edata.data = offset * 4 + 0x204;
      edata.cmd = ARM_EXIDX_CMD_DATA_POP;
    }else if (op == 0xb3 || op == 0xc8 || op == 0xc9) {
      edata.cmd = ARM_EXIDX_CMD_VFP_POP;
      edata.data = READ_OP ();
      if (op == 0xc8)
        edata.data |= ARM_EXIDX_VFP_SHIFT_16;
      if (op != 0xb3)
        edata.data |= ARM_EXIDX_VFP_DOUBLE;
    }else if ((op & 0xf8) == 0xb8 || (op & 0xf8) == 0xd0) {
      edata.cmd = ARM_EXIDX_CMD_VFP_POP;
      edata.data = 0x80 | (op & 0x07);
      if ((op & 0xf8) == 0xd0)
        edata.data |= ARM_EXIDX_VFP_DOUBLE;
    }else if (op >= 0xc0 && op <= 0xc5) {
      edata.cmd = ARM_EXIDX_CMD_WREG_POP;
      edata.data = 0xa0 | (op & 0x07);
    }else if (op == 0xc6) {
      edata.cmd = ARM_EXIDX_CMD_WREG_POP;
      edata.data = READ_OP ();
    }else if (op == 0xc7) {
      uint8_t op2 = READ_OP ();
      if (op2 == 0 || (op2 & 0xf0))
        edata.cmd = ARM_EXIDX_CMD_RESERVED;
      else {
        edata.cmd = ARM_EXIDX_CMD_WCGR_POP;
        edata.data = op2 & 0x0f;
      }
    }else
      edata.cmd = ARM_EXIDX_CMD_RESERVED;
    //CRASH_LOGD(false, "cmd = %d\n", edata.cmd);
    ret = arm_exidx_apply_cmd(&edata, c);
    if (ret < 0)
      return ret;
  }
  return 0;
}

/*
 * 执行恢复现场指令
 * 正常返回 0 失败返回 -1
 */
int arm_exidx_apply_cmd (arm_exbuf_data *edata, t_cursor *c) {

  int ret = 0;
  unsigned i;

  switch (edata->cmd) {

    case ARM_EXIDX_CMD_FINISH:
      /* Set LR to PC if not set already.  */
      CRASH_LOGD("pc = %08x\n", c->regs[ARM_LR]);
      CRASH_LOGD("finish");
      if(c->regs[ARM_PC] == c->regs[ARM_LR]) {
        c->step_status = STEP_PC_LR;
        CRASH_LOGD("pc = lr to end\n");
        return 0;
      }
      c->regs[ARM_PC] = c->regs[ARM_LR];
      break;

    case ARM_EXIDX_CMD_DATA_PUSH:
      /* vsp = vsp - data */
      CRASH_LOGD("sp = sp - %08x\n", edata->data);
      c->regs[ARM_SP] -= edata->data;
      break;

    case ARM_EXIDX_CMD_DATA_POP:
      /* vsp = vsp + data */
      CRASH_LOGD("sp = sp + %08x\n", edata->data);
      c->regs[ARM_SP] += edata->data;
      break;

    case ARM_EXIDX_CMD_REG_POP:
      for (i = 0; i < 16; i++)
        if (edata->data & (1 << i)) {
          /* pop r[i] */
          CRASH_LOGD("pop r[%d]\n", i);
          c->regs[i] = *(uint32_t *)c->regs[ARM_SP];
          c->regs[ARM_SP] += 4;
        }

      break;

    case ARM_EXIDX_CMD_REG_TO_SP:
      CRASH_LOGD("sp = %08x\n", edata->data);
      c->regs[ARM_SP] = c->regs[edata->data];
      break;

    case ARM_EXIDX_CMD_VFP_POP:
      /* Skip VFP registers, but be sure to adjust stack */

      break;

    case ARM_EXIDX_CMD_WREG_POP:
      
      break;

    case ARM_EXIDX_CMD_WCGR_POP:
      
      break;

    case ARM_EXIDX_CMD_REFUSED:
    case ARM_EXIDX_CMD_RESERVED:
      ret = -1;
      break;
    }

  return ret;
}

/* 
 * 根据key的值二分查找value
 * return 目标key-value的地址
 */
addr_s search_exidx_value(addr_s *start, addr_s *end, addr_s base_addr, uint32_t key) {
    if(end <= start) return 0;
    /*一个key-value是64bits*/
    addr_s *mid = start + ((((addr_s)end - (addr_s)start)/8)/2)*2;
    /*取前32bits*/
    uint32_t key_pre = (*(mid-2) + (addr_s)(mid-2) - base_addr) & 0x7FFFFFFF;
    uint32_t key_mid = (*mid + (addr_s)mid - base_addr) & 0x7FFFFFFF;
    uint32_t key_flo = (*(mid+2) + (addr_s)(mid+2) - base_addr) & 0x7FFFFFFF;
    if(key >= key_mid && key <= key_flo) {
      return (addr_s )mid;
    }else if(key <= key_mid && key >= key_pre) {
      return (addr_s )(mid - 2);
    }else if(key >= key_mid) {
      return search_exidx_value(mid + 2, end, base_addr, key);
    }else if(key < key_mid) {
      return search_exidx_value(start, mid - 2, base_addr, key);
    }else
      return 0;
}

int copyregs_to_cursor(sigcontext *sig_ctx, t_cursor *c) {
    memcpy(c->regs, &sig_ctx->arm_r0, NUM_REGS * sizeof(unsigned long));
    return 0;
}

int copyregs_to_sigctx(t_cursor *c, sigcontext *sig_ctx) {
    memcpy(&sig_ctx->arm_r0, c->regs, NUM_REGS * sizeof(unsigned long));
    return 0;
}

#endif
