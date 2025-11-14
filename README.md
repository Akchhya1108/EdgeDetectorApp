EdgeViewer 🎥
A real-time edge detection Android application that captures camera frames, processes them using OpenCV in C++ (via JNI/NDK), and displays the output using OpenGL ES 2.0. The project also includes a TypeScript-based web viewer for displaying processed frames.
📋 Table of Contents

Features Implemented
Tech Stack
Architecture Overview
Setup Instructions
Project Structure
How It Works
Current Status
Future Enhancements

✅ Features Implemented
Android Application

✅ Camera Feed Integration: Implemented using Camera2 API with ImageReader for frame capture
✅ Real-time Frame Processing: Native C++ processing using OpenCV with JNI bridge
✅ Canny Edge Detection: Applied on camera frames in real-time with configurable thresholds
✅ Runtime Permissions: Proper camera permission handling with user-friendly dialogs
✅ Modular Architecture: Clean separation between camera, native processing, and UI layers

Native C++ Layer

✅ JNI Bridge: Efficient Java ↔ C++ communication for frame data
✅ OpenCV Integration: Canny edge detection with color space conversions (YUV → BGR → Grayscale → RGBA)
✅ Optimized Processing: Handles NV21 format from Camera2 API efficiently

Web Viewer

⚠️ Status: Basic TypeScript project structure prepared (requires implementation)
📝 Planned: Display processed frames with frame stats (FPS, resolution)

OpenGL ES Rendering

⚠️ Status: Not yet implemented
📝 Planned: Texture-based rendering of processed frames

🛠 Tech Stack

Android: Kotlin, Camera2 API
Native Layer: C++17, OpenCV 4.x, JNI/NDK
Build System: Gradle with CMake for native builds
Target SDK: Android API 24+ (Android 7.0+)
Web: TypeScript (planned)

🏗 Architecture Overview
JNI Bridge & Frame Flow
┌─────────────────────────────────────────────────────────────┐
│                     Android App Layer                        │
│  ┌────────────────┐         ┌─────────────────┐            │
│  │  MainActivity  │────────▶│ CameraController │            │
│  └────────────────┘         └─────────────────┘            │
│         │                            │                       │
│         │                            ▼                       │
│         │                   ┌─────────────────┐            │
│         │                   │  ImageReader    │            │
│         │                   │  (YUV_420_888)  │            │
│         │                   └─────────────────┘            │
│         │                            │                       │
│         │                            │ NV21 bytes           │
│         ▼                            ▼                       │
│  ┌──────────────────────────────────────────────┐          │
│  │           NativeBridge (JNI)                 │          │
│  │   processFrame(ByteArray, width, height)     │          │
│  └──────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ JNI Call
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Native C++ Layer                          │
│  ┌──────────────────────────────────────────────┐          │
│  │         opencv_processor.cpp                 │          │
│  │                                               │          │
│  │  1. Convert NV21 → cv::Mat (YUV)            │          │
│  │  2. COLOR_YUV2BGR_NV21 conversion           │          │
│  │  3. Canny Edge Detection (80, 150)          │          │
│  │  4. COLOR_GRAY2RGBA conversion              │          │
│  │  5. Return RGBA ByteArray                   │          │
│  └──────────────────────────────────────────────┘          │
│                                                               │
│  ┌──────────────────────────────────────────────┐          │
│  │         OpenCV 4.x Library                   │          │
│  └──────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Processed RGBA bytes
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   OpenGL ES Renderer                         │
│                  (Not Yet Implemented)                       │
│  ┌──────────────────────────────────────────────┐          │
│  │  • Create OpenGL texture from RGBA data      │          │
│  │  • Render texture to screen                  │          │
│  │  • Handle surface lifecycle                  │          │
│  └──────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
Key Components

MainActivity.kt: Entry point, handles permissions, initializes camera
CameraController.kt: Manages Camera2 API, captures frames in YUV_420_888 format
NativeBridge.kt: JNI interface for native method calls
native-lib.cpp: JNI initialization and utility functions
opencv_processor.cpp: Core image processing with OpenCV

Data Flow

Camera captures frame in YUV_420_888 format
Frame converted to NV21 byte array
Passed to native layer via JNI
OpenCV processes: YUV → BGR → Edge Detection → RGBA
RGBA bytes returned to Java layer
(Future) Uploaded to OpenGL texture and rendered

📦 Setup Instructions
Prerequisites

Android Studio: Arctic Fox or newer
Android NDK: Version 21.0+ (installed via Android Studio SDK Manager)
OpenCV Android SDK: Download from opencv.org

Version 4.5.0 or newer recommended


Minimum Android Device: API 24+ (Android 7.0+)

OpenCV Setup
Since OpenCV is not included in the repository, you need to set it up manually:

Download OpenCV Android SDK:

bash   # Download from https://opencv.org/releases/
   # Extract to a location on your system

Option A: Using Pre-built OpenCV (Recommended):

bash   # Copy OpenCV headers to your project
   mkdir -p app/src/main/cpp/include
   cp -r <OpenCV-SDK>/sdk/native/jni/include/opencv2 app/src/main/cpp/include/
   
   # Copy OpenCV shared libraries
   mkdir -p app/src/main/jniLibs
   cp -r <OpenCV-SDK>/sdk/native/libs/* app/src/main/jniLibs/

Option B: Building OpenCV from Source:

Follow OpenCV Android Build Guide
Place built libraries in app/src/main/jniLibs/


Update CMakeLists.txt:

cmake   # Add OpenCV library path
   add_library(opencv_lib SHARED IMPORTED)
   set_target_properties(opencv_lib PROPERTIES IMPORTED_LOCATION
       ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java4.so)
   
   # Link OpenCV
   target_link_libraries(native-lib opencv_lib ${log-lib})
Building the Project

Clone the repository:

bash   git clone <your-repo-url>
   cd EdgeViewer

Open in Android Studio:

File → Open → Select project directory
Wait for Gradle sync to complete


Configure NDK (if not auto-detected):

File → Project Structure → SDK Location
Set Android NDK location


Build the project:

bash   ./gradlew assembleDebug

Run on device/emulator:

Connect Android device with USB debugging enabled
Run → Run 'app' (or Shift+F10)



Troubleshooting

OpenCV not found: Ensure headers are in app/src/main/cpp/include/opencv2/
Library link errors: Check that .so files are in app/src/main/jniLibs/<abi>/
Camera permission denied: Grant permission manually in device Settings → Apps → EdgeViewer
CMake errors: Verify NDK version compatibility in build.gradle.kts

📁 Project Structure
EdgeViewer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── cpp/                    # Native C++ code
│   │   │   │   ├── CMakeLists.txt      # CMake build configuration
│   │   │   │   ├── native-lib.cpp      # JNI bridge implementation
│   │   │   │   ├── opencv_processor.cpp # OpenCV processing logic
│   │   │   │   └── include/            # OpenCV headers (to be added)
│   │   │   ├── java/com/example/edgeviewer/
│   │   │   │   ├── MainActivity.kt     # Main activity
│   │   │   │   ├── NativeBridge.kt     # JNI interface
│   │   │   │   └── camera/
│   │   │   │       └── CameraController.kt # Camera2 API wrapper
│   │   │   ├── jniLibs/                # OpenCV .so files (to be added)
│   │   │   ├── res/                    # Android resources
│   │   │   └── AndroidManifest.xml     # App manifest with permissions
│   │   └── test/                       # Unit tests
│   ├── build.gradle.kts                # App-level build configuration
│   └── proguard-rules.pro              # ProGuard configuration
├── web/                                 # TypeScript web viewer (to be implemented)
│   ├── src/
│   │   └── index.ts
│   ├── package.json
│   └── tsconfig.json
├── build.gradle.kts                    # Project-level build configuration
├── settings.gradle.kts                 # Project settings
└── README.md                           # This file
🔧 How It Works
Camera Capture
The CameraController class manages the Camera2 API:

Opens the back-facing camera
Configures ImageReader for YUV_420_888 format at 640x480
Captures frames continuously using a repeating capture request
Converts YUV_420_888 to NV21 format (required by OpenCV)

Native Processing
The opencv_processor.cpp implements edge detection:
cpp// 1. Convert NV21 to OpenCV Mat (YUV color space)
cv::Mat yuv(height + height/2, width, CV_8UC1, nv21);

// 2. Convert YUV to BGR
cv::Mat bgr;
cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);

// 3. Apply Canny Edge Detection
cv::Mat edges;
cv::Canny(bgr, edges, 80, 150);  // thresholds: 80, 150

// 4. Convert grayscale edges to RGBA for OpenGL
cv::Mat rgba;
cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);

// 5. Return as byte array to Java
JNI Communication

Native methods declared in NativeBridge.kt
Implemented in C++ with extern "C" and JNIEXPORT
Efficient byte array transfer without unnecessary copies
Proper memory management with ReleaseByteArrayElements

📊 Current Status
Completed ✅

Android project setup with NDK/CMake integration
Camera2 API integration with proper permission handling
JNI bridge for Java ↔ C++ communication
OpenCV Canny edge detection in native layer
Frame format conversions (YUV → BGR → Grayscale → RGBA)
Modular project structure

In Progress 🚧

OpenGL ES 2.0 renderer for displaying processed frames
Performance optimization (target 15-30 FPS)
TypeScript web viewer implementation

Pending 📝

Toggle button for raw vs. edge-detected feed
FPS counter and frame processing time logging
OpenGL shaders for additional effects
WebSocket/HTTP endpoint for web viewer integration
Comprehensive README with screenshots/GIFs

🚀 Future Enhancements
Planned Features

OpenGL Renderer: Texture-based rendering with GLSurfaceView
UI Controls: Toggle between raw and processed feeds
Performance Metrics: Real-time FPS counter and processing time display
Web Viewer: TypeScript-based frame viewer with WebSocket support
Additional Effects: Grayscale, invert, and custom GLSL shaders
Frame Export: Save processed frames to gallery

Optimization Opportunities

GPU acceleration using OpenGL compute shaders
Multi-threading for parallel frame processing
Adaptive resolution based on device capabilities
Frame skipping for consistent FPS

📝 Development Notes
Key Decisions

Camera2 API over deprecated Camera1 for better control
NV21 format for OpenCV compatibility
RGBA output to prepare for OpenGL texture upload
Modular architecture for maintainability

Known Limitations

No visual output yet (OpenGL renderer pending)
Fixed resolution (640x480) - not configurable
Camera permission must be granted manually if denied initially
OpenCV libraries must be added manually (not in repo)
