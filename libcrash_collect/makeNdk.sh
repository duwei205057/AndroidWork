ndk-build -B  NDK_APPLICATION_MK=./Application.mk APP_BUILD_SCRIPT=./Android.mk NDK_PROJECT_PATH=./
cp libs/armeabi-v7a/libcrash_collect.so ../app/jniLibs/armeabi-v7a/libcrash_collect.so
