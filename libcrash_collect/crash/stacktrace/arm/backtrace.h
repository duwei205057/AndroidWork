#if defined(__arm__)
#ifndef BACKTRACE_H_
#define BACKTRACE_H_

#define ARM_EXIDX 0x70000001

#include <asm/sigcontext.h>
#include <dlfcn.h>
#include "elf.h"
#include <stdlib.h>
#include "sys_bits.h"
#include "exidxstep.h"

int step(sigcontext *sig_context, bool bNextCode);

#endif
#endif