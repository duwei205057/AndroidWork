#ifndef STACKTRACE_H_
#define STACKTRACE_H_

#define ARM_EXIDX 0x70000001

#include <asm/sigcontext.h>
#include <dlfcn.h>
#include "elf.h"
#include <stdlib.h>
#include "common/sys_bits.h"
#include "exidxstep.h"
namespace stacktrace {
  int Step(sigcontext *sig_context);
}
#endif

