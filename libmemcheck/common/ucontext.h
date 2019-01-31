/* Just for ARM */
#ifndef _SYS_UCONTEXT_H
#define _SYS_UCONTEXT_H	1

/*#if defined(__ANDROID__) && !defined(__BIONIC_HAVE_UCONTEXT_T) && \
    defined(__arm__) && !defined(__BIONIC_HAVE_STRUCT_SIGCONTEXT)
*/
#include <asm/sigcontext.h>
#include <asm/signal.h>
//#endif 

/* Number of general registers.  */
#ifdef NGREG
#undef NGREG
#endif
#define NGREG	19
namespace crash_collect {
enum
{
  ARM_R0 = 3,
  ARM_R1, ARM_R2, ARM_R3, ARM_R4, ARM_R5, ARM_R6, ARM_R7, ARM_R8, 
  ARM_R9, ARM_SL, ARM_FP, ARM_IP, ARM_SP, ARM_LR, ARM_PC, ARM_CPSR
};

#if !defined(__BIONIC_HAVE_UCONTEXT_T)
struct ucontext {
  unsigned long uc_flags;
  struct ucontext *uc_link;
  stack_t uc_stack;
  struct sigcontext uc_mcontext;
  unsigned long uc_sigmask;
};
#endif
}
#endif /* ucontext.h */

