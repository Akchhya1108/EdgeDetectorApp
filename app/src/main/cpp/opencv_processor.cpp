#include <jni.h>
#include <opencv2/opencv.hpp>
#include <vector>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeviewer_NativeBridge_processFrame(
        JNIEnv *env,
        jobject thiz,
        jbyteArray frame,
        jint width,
        jint height
) {
    // Convert NV21 to OpenCV Mat
    jbyte *nv21 = env->GetByteArrayElements(frame, nullptr);
    cv::Mat yuv(height + height / 2, width, CV_8UC1, nv21);

    cv::Mat bgr;
    cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);

    // Apply Canny Edge Detection
    cv::Mat edges;
    cv::Canny(bgr, edges, 80, 150);

    // Convert grayscale → RGBA so OpenGL can use it
    cv::Mat rgba;
    cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);

    int size = rgba.total() * 4;
    jbyteArray result = env->NewByteArray(size);
    env->SetByteArrayRegion(result, 0, size, (jbyte *) rgba.data);

    env->ReleaseByteArrayElements(frame, nv21, JNI_ABORT);
    return result;
}
