# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY src/main/jniLibs/${ANDROID_ABI})

ADD_SUBDIRECTORY(src/main/cpp/mediabuffer)