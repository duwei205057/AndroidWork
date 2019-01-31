ifeq ($(TARGET_ARCH),arm)
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES:= \
	memcheck_jni.cpp \
        InlineHook/inlineHook.c \
        InlineHook/relocate.c \
        stacktrace/exidxstep.cpp \
        stacktrace/stacktrace.cpp \
        backtrace.cpp

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE)

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/inlineHook \
        $(LOCAL_PATH)/stacktrace \
        $(LOCAL_PATH)/common

LOCAL_CFLAGS += -Wall -Wextra -Wno-non-virtual-dtor -DNDEBUG -DOS_LINUX

ifeq ($(WITH_SYMBOL_TABLE),true)
  LOCAL_CFLAGS += -O0 -ggdb3 -fno-inline -g
else
  LOCAL_CFLAGS += -O2
endif

LOCAL_LDFLAGS += -shared -Wl,--version-script=$(LOCAL_PATH)/dynsym.map

LOCAL_SHARED_LIBRARIES := \
	libnativehelper \
	libcutils \
	libutils \
	libdl 

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE:= libmemcheck

include $(BUILD_SHARED_LIBRARY)
endif
