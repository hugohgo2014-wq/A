cmake_minimum_required(VERSION 3.22.1)
project(assistant_native LANGUAGES C CXX)

include(FetchContent)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
# llama.cpp is fetched and compiled as part of the Android CMake build.
# Pinning a release prevents an unreviewed upstream API change from breaking builds.
FetchContent_Declare(llama_cpp
  GIT_REPOSITORY https://github.com/ggml-org/llama.cpp.git
  GIT_TAG v0.7.0
  GIT_SHALLOW TRUE)
FetchContent_MakeAvailable(llama_cpp)

add_library(assistant_native SHARED native-lib.cpp gl_renderer.cpp)
find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv3)
target_link_libraries(assistant_native PRIVATE llama ${log-lib} ${android-lib} ${gles-lib})
target_compile_options(assistant_native PRIVATE -O3 -fvisibility=hidden)
