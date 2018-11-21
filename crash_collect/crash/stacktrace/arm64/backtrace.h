#if defined(__aarch64__)
#ifndef BACKTRACE_H_
#define BACKTRACE_H_

#define EH_FRAME 0x6474e550

#include <asm/sigcontext.h>
#include <dlfcn.h>
#include "elf.h"
#include <stdlib.h>
#include "ehstep.h"
#include "eh_frame.h"

int step(sigcontext *sig_context);

#endif
#endif