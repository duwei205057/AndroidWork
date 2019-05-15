#include "stacktrace.h"

namespace stacktrace {
    int Step(sigcontext *sig_ctx, bool bNextCode) {
        return step(sig_ctx, bNextCode);
    }
}