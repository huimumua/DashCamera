# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# comment below flag set for build arm64-v8a abi
#set( CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpu=neon" )

add_library( # Sets the name of the library.
             mediabuffer-jni

             # Sets the library as a shared library.
             SHARED
             #STATIC

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             mediabuffer_jni.cpp
            )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       mediabuffer-jni

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                      )

