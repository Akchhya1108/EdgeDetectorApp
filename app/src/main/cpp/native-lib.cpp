#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_edgeviewer_NativeBridge_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    std::string msg = "JNI working successfully";
    return env->NewStringUTF(msg.c_str());
}
