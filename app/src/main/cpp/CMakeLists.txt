cmake_minimum_required(VERSION 3.10.2)
project(edgeviewer_native)

set(CMAKE_CXX_STANDARD 17)

add_library(
        native-lib
        SHARED
        native-lib.cpp
        opencv_processor.cpp
)

find_library(
        log-lib
        log
)

# If you add OpenCV headers later, we include them here (MUST DO THIS)
include_directories(${CMAKE_SOURCE_DIR}/include)

target_link_libraries(
        native-lib
        ${log-lib}
)
