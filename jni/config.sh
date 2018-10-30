/home/dw/java/adt-bundle-linux-x86_64-20140321/sdk/cmake/3.6.4111459/bin/cmake \
-DANDROID_ABI="armeabi-v7a" \
-DCMAKE_ANDROID_ARCH_ABI="arm64-v8a" \
-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY="./" \
-DANDROID_PLATFORM="android-21" \
-DCMAKE_BUILD_TYPE=Release \
-DANDROID_NDK="/home/dw/java/android-ndk-r14b" \
-DCMAKE_TOOLCHAIN_FILE="/home/dw/java/android-ndk-r14b/build/cmake/android.toolchain.cmake" \
-DANDROID_TOOLCHAIN="clang" \
-DANDROID_STL="c++_static"  \
-DSGIME_PLATFORM="ANDROID" \
-DCMAKE_SYSTEM_VERSION=21 \
-DCMAKE_SYSROOT="/home/dw/java/android-ndk-r14b/platforms/android-21/arch-arm64/" \
-DCMAKE_CXX_FLAGS="-frtti -fexceptions --sysroot=/home/dw/java/android-ndk-r14b/platforms/android-21/arch-arm64"
#-DANDROID_ARM_NEON="true"


#-DCMAKE_C_COMPILER="" \
#-DCMAKE_CXX_COMPILER=""
#-DCMAKE_C_FLAGS="-s" \
