#ifndef ENVIRONMENT_H_
#define ENVIRONMENT_H_

#define ANR_FILE_DIR "/sdcard/anr"
//#define ANR_FILE_DIR "/data/data/com.dw.debug/files/anr"
//#define ANR_FILE_DIR "/data/anr"
#define OPEN_STACK true

#include <stdio.h>

namespace native_crash_collector {

enum {
  CRASH_SWITCH = 1,
  ANR_SWITCH = 2
};

}

#endif
