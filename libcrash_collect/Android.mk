LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng

CRASH_SRC    :=     $(LOCAL_PATH) \
                    $(LOCAL_PATH)/utils \
                    $(LOCAL_PATH)/anr \
                    $(LOCAL_PATH)/crash \
                    $(LOCAL_PATH)/crash/stacktrace \
                    $(LOCAL_PATH)/crash/stacktrace/arm \
                    $(LOCAL_PATH)/crash/stacktrace/arm64


LOCAL_SRC_FILES += \
        $(foreach DIR, $(CRASH_SRC), $(wildcard $(addsuffix /*.cpp, $(DIR))))

LOCAL_C_INCLUDES += $(JNI_H_INCLUDE)

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	$(CRASH_SRC)

LOCAL_CFLAGS += -Wall -Wextra -Wno-non-virtual-dtor -DNDEBUG -DOS_LINUX

ifeq ($(WITH_SYMBOL_TABLE),true)
  LOCAL_CFLAGS += -O0 -ggdb3 -fno-inline -g
else
  LOCAL_CFLAGS += -O2
endif

ifeq ($(WITH_CRASH_LOG),true)
  LOCAL_CFLAGS += -DWITH_CRASH_LOG=1
endif

LOCAL_LDFLAGS += -shared -Wl,--version-script=$(LOCAL_PATH)/dynsym.map

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

#LOCAL_SHARED_LIBRARIES := libdl

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE:= crash_collect

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_STATIC_LIBRARY)
