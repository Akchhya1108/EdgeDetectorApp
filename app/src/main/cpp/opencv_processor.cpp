#include <jni.h>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeviewer_NativeBridge_processFrame(
        JNIEnv *env,
        jclass clazz,
        jbyteArray input,
        jint width,
        jint height) {

    // Temporary placeholder
    jbyteArray result = env->NewByteArray(0);
    return result;
}
