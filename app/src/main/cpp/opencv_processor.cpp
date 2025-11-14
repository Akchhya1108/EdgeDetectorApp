#include <jni.h>
#include <opencv2/core.hpp>   // More specific include
#include <opencv2/imgproc.hpp> // For cvtColor and Canny

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeviewer_NativeBridge_processFrame(
        JNIEnv *env,
        jobject thiz,
        jbyteArray frame,
        jint width,
        jint height
) {
    jbyte *nv21 = env->GetByteArrayElements(frame, nullptr);
    cv::Mat yuv(height + height / 2, width, CV_8UC1, (unsigned char*)nv21);

    cv::Mat bgr;
    cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);

    cv::Mat edges;
    cv::Canny(bgr, edges, 80, 150);

    cv::Mat rgba;
    cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);

    int size = rgba.total() * rgba.elemSize(); // width*height*4
    jbyteArray result = env->NewByteArray(size);
    env->SetByteArrayRegion(result, 0, size, reinterpret_cast<jbyte*>(rgba.data));

    env->ReleaseByteArrayElements(frame, nv21, JNI_ABORT);
    return result;
}
