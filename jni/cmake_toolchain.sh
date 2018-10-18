if [ $# -eq 2 ];then
NDK_LOCATION=$1
CMAKE_TOOL_LOCATION=$2
echo "NDK_LOCATION=${NDK_LOCATION}"
echo "CMAKE_TOOL_LOCATION=${CMAKE_TOOL_LOCATION}"
CMD="${CMAKE_TOOL_LOCATION} \
-DANDROID_ABI=armeabi-v7a \
-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY='./' \
-DANDROID_PLATFORM=android-19 \
-DCMAKE_BUILD_TYPE=Release \
-DANDROID_NDK='${NDK_LOCATION}' \
-DCMAKE_TOOLCHAIN_FILE='${NDK_LOCATION}/build/cmake/android.toolchain.cmake' \
-DANDROID_TOOLCHAIN='clang' \
-DANDROID_STL='c++_static'  \
-DSGIME_PLATFORM='ANDROID' \
-DANDROID_ARM_NEON='true' \
-DWITH_CRASH_LOG='true'"
echo "CMD=${CMD}"
${CMD}
else
echo "需要传入ndk根目录及SDK中cmake工具路径   如./cmake_toolchain.sh /home/hostname/java/android-ndk-r14b /home/hostname/java/adt-bundle-linux-x86_64-20140321/sdk/cmake/3.6.4111459/bin/cmake" 
fi 
