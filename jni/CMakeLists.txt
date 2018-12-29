#最低要求版本号
cmake_minimum_required(VERSION 3.4.1)


set(LOCAL_PATH ${CMAKE_CURRENT_SOURCE_DIR})
set(PROJECT_PATH ${LOCAL_PATH}/..)
#set(CMAKE_SYSTEM_VERSION,21)

macro(debug msg)
    message(STATUS "DEBUG ${msg}")
endmacro()

macro(debugValue var)
    debug("${var}=\${${var}}")
endmacro()

add_library(myso

             # Sets the library as a shared library.动态库，会被动态链接，在运行时被加载
             SHARED

             # Provides a relative path to your source file(s).要编译的源代码文件的路径，如果你的不是cpp下的，这里记得同步改下
             ${LOCAL_PATH}/main_jni.cpp )

debugValue(WITH_CRASH_LOG)
debugValue(NOENCRYPT)
if (NOENCRYPT STREQUAL "true")
    set(LOCAL_FLAGS "-DNOENCRYPT")
else()
    set(LOCAL_FLAGS "")
endif()

ADD_DEFINITIONS( ${LOCAL_FLAGS})

set_target_properties(myso PROPERTIES LINK_FLAGS "-llog")


if (NOT TARGET crash_collector)
    add_subdirectory("${PROJECT_PATH}/libcrash_collect" "${PROJECT_PATH}/libcrash_collect")
endif()

