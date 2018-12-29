#最低要求版本号
cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

project (crash_collector)
set(LOCAL_MODULE crash_collect)
set(LOCAL_PATH ${CMAKE_CURRENT_SOURCE_DIR})

# Use C++11
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_SYSTEM_VERSION,21)

macro(debug msg)
    message(STATUS "DEBUG ${msg}")
endmacro()

macro(debugValue var)
    debug("${var}=\${${var}}")
endmacro()

debugValue(LOCAL_PATH)

if (WITH_BREAKPAD STREQUAL "true")
    add_subdirectory(external/libbreakpad)
    set(BREAKPADLIB "breakpad")
    list(APPEND BREAKPADHEAD external/libbreakpad/src external/libbreakpad/src/common/android/include)
    set(STACK_FLAGS "-DBREAKPAD_ENABLE")
else()
    list(APPEND STACKSOURCE ${LOCAL_PATH}/crash
                    ${LOCAL_PATH}/crash/stacktrace
                    ${LOCAL_PATH}/crash/stacktrace/arm
                    ${LOCAL_PATH}/crash/stacktrace/arm64)
endif()

set(SOURCE_CORE    ${LOCAL_PATH}
                   ${LOCAL_PATH}/utils
                   ${LOCAL_PATH}/anr
                   ${STACKSOURCE}
                   )

debugValue(SOURCE_CORE)

# add all *.cpp in pathlist (not recursively) to outputlist
MACRO(ADD_SOURCE_FILES pathlist outputlist)
    set(src_list "")
    FOREACH(dir ${pathlist})
        file(GLOB new_list1 ${dir}/*.cpp)
        set(src_list ${src_list} ${new_list1})
    ENDFOREACH()
    list(REMOVE_DUPLICATES src_list)
    set(${outputlist} ${src_list})
ENDMACRO()

# add all dirs and "${dir}/inc" in pathlist into outputlist
MACRO(ADD_HEADER_DIRS pathlist outputlist)
    set(dir_list "")
    FOREACH(dir ${pathlist})
        list(APPEND dir_list ${dir} ${dir}/inc)
    ENDFOREACH()
    set(${outputlist} ${dir_list})
ENDMACRO()

ADD_HEADER_DIRS("${SOURCE_CORE}" HEADERS_FILES)

ADD_SOURCE_FILES("${SOURCE_CORE}" SOURCE_FILES)
debugValue(SOURCE_FILES)

##### 将所有源码添加到编译目标
add_library(${LOCAL_MODULE} SHARED
           ${SOURCE_FILES} )

include_directories(${HEADERS_FILES} ${BREAKPADHEAD})
##### 设置link language
set_target_properties(${LOCAL_MODULE} PROPERTIES LINKER_LANGUAGE CXX)

debugValue(WITH_CRASH_LOG)
if (WITH_CRASH_LOG STREQUAL "true")
    set(LOCAL_FLAGS "-DWITH_CRASH_LOG")
else()
    set(LOCAL_FLAGS "")
endif()


ADD_DEFINITIONS(
	"-Wall -Wextra -Wno-non-virtual-dtor -DNDEBUG -DOS_LINUX"
	"-O2"
   ${LOCAL_FLAGS}
   ${STACK_FLAGS})

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 ")

set_target_properties(${LOCAL_MODULE} PROPERTIES LINK_FLAGS "-llog -shared -Wl,--version-script=${LOCAL_PATH}/dynsym.map")

find_library( # Sets the name of the path variable.日志支持库并将其路径存储在 log-lib 中
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log
              )


debugValue(log-lib)
debugValue(ANDROID_ABI)
debugValue(LOCAL_PATH)
debugValue(ANDROID_NDK)
debugValue(breakpad_client)
target_link_libraries( ${LOCAL_MODULE}
		       ${log-lib}
		       ${BREAKPADLIB}
		       )
