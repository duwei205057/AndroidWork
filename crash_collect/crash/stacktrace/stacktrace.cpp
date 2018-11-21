#include "stacktrace.h"

namespace stacktrace {
    int Step(sigcontext *sig_ctx) {
        return step(sig_ctx);
    }
}