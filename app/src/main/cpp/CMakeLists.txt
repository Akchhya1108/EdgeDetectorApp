cmake_minimum_required(VERSION 3.18.1)

project(NativeEdgeProcessor)

# Path to OpenCV (we just pasted it)
set(OpenCV_DIR "${CMAKE_SOURCE_DIR}/src/main/cpp/opencv")

find_package(OpenCV REQUIRED CONFIG)

add_library(
        native-lib
        SHARED
        opencv_processor.cpp
)

target_include_directories(native-lib PRIVATE
        ${OpenCV_INCLUDE_DIRS}
)

target_link_libraries(
        native-lib
        ${OpenCV_LIBS}
        log
        android
)
