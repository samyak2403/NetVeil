cmake_minimum_required(VERSION 3.22.1)

project(netveil_native)

file(GLOB NETVEIL_SRC netveil/*.c)
list(REMOVE_ITEM NETVEIL_SRC ${CMAKE_CURRENT_SOURCE_DIR}/netveil/win_service.c)

add_library(netveil SHARED ${NETVEIL_SRC} native-lib.c utils.c)
target_include_directories(netveil PRIVATE netveil ${CMAKE_CURRENT_SOURCE_DIR})

target_compile_options(netveil PRIVATE -std=c99 -O2 -D_XOPEN_SOURCE=500)
target_compile_definitions(netveil PRIVATE ANDROID_APP)

target_link_libraries(netveil PRIVATE android log)
