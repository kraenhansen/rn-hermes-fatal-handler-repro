#include <android/log.h>
#include <hermes/hermes.h>
#include <jni.h>
#include <jsi/jsi.h>
#include <memory>

using facebook::jsi::castInterface;
using facebook::jsi::StringBuffer;

static void controlHandler(const std::string &reason) {
  __android_log_assert(nullptr, "ReproControl", "%s", reason.c_str());
}

static void exhaustHeap() {
  auto config = ::hermes::vm::RuntimeConfig::Builder()
                    .withGCConfig(::hermes::vm::GCConfig::Builder()
                                      .withInitHeapSize(4 << 20)
                                      .withMaxHeapSize(8 << 20)
                                      .withName("repro")
                                      .build())
                    .build();
  auto rt = facebook::hermes::makeHermesRuntime(config);
  rt->evaluateJavaScript(
      std::make_shared<StringBuffer>("var a=[];for(;;)a.push({x:'y'+a.length});"),
      "oom.js");
}

extern "C" JNIEXPORT void JNICALL
Java_com_fatalrepro_MainActivity_triggerFatal(JNIEnv *, jclass, jboolean control) {
  if (control) {
    auto *setter = castInterface<facebook::hermes::ISetFatalHandler>(
        facebook::hermes::makeHermesRootAPI());
    __android_log_print(ANDROID_LOG_INFO, "Repro", "control handler iface=%p", setter);
    setter->setFatalHandler(controlHandler);
  }
  __android_log_print(ANDROID_LOG_INFO, "Repro", "exhausting heap");
  exhaustHeap();
}
