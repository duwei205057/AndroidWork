#最低要求版本号
cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(LOCAL_PATH ${CMAKE_CURRENT_SOURCE_DIR})
set(PROJECT_PATH ${LOCAL_PATH}/..)

add_library(nativehelper

             # Sets the library as a shared library.动态库，会被动态链接，在运行时被加载
             SHARED

             # Provides a relative path to your source file(s).要编译的源代码文件的路径，如果你的不是cpp下的，这里记得同步改下
             ${LOCAL_PATH}/main_jni.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.日志支持库并将其路径存储在 log-lib 中
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.为了确保您的原生库可以在 log 库中调用函数
                       myso

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

if (NOT TARGET crash_collector)
    add_subdirectory("${PROJECT_PATH}/crash_collect" "${PROJECT_PATH}/crash_collect")
endif()

