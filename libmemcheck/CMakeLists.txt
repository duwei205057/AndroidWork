cmake_minimum_required(VERSION 3.4.1)

PROJECT(memcheck)
# Use C++11
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(SRC_PATH
    .
	stacktrace
	inlineHook
	elfHook
	common
    )

file(GLOB_RECURSE CPP_FILES "./*.cpp")
file(GLOB_RECURSE C_FILES "./*.c")

add_library( memcheck

             SHARED

             ${CPP_FILES}
             ${C_FILES}
             )

include_directories(${SRC_PATH})

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( # Specifies the target library.
                       memcheck

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )