#ifndef STACKTRACE_H_
#define STACKTRACE_H_

#if defined(__arm__)
#include "arm/backtrace.h"
#elif defined(__aarch64__)
#include "arm64/backtrace.h"
#endif

namespace stacktrace {
  int Step(sigcontext *sig_context, bool b);
}
#endif

