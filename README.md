# Hermes fatal handler reproducer

A stock React Native 0.88.1 app showing that a Hermes fatal error on the New Architecture has no reason in logcat or the tombstone, because `setFatalHandler` is only called from the legacy `HermesExecutorHolder`.

All changes to the template are in:

- `android/app/src/main/jni/`: `repro/repro.cpp` and a `CMakeLists.txt` that adds it next to React Native's default setup
- `android/app/src/main/java/com/fatalrepro/MainActivity.kt`: triggers the fatal error 8 seconds after launch
- `android/app/build.gradle`: uses that `CMakeLists.txt` and builds the extra target
- `android/gradle.properties`: builds `arm64-v8a` only

The trigger creates a second Hermes runtime with an 8 MB heap and exhausts it. That makes the failure deterministic, and fatal handling is process-global. With the default 3 GB heap, Android usually kills the app first.

## Run

```sh
npm install
(cd android && ./gradlew :app:assembleRelease)
adb install -r -t android/app/build/outputs/apk/release/app-release.apk

adb logcat -c
adb shell am start -n com.fatalrepro/.MainActivity --es mode default
sleep 12
adb logcat -d | grep -E ' Repro|HermesGC: OOM|Fatal signal|Abort message|Cause:'
```

Use `--es mode control` to install a handler like React Native's `hermesFatalHandler` first.

| Mode | Crash | Reason in logcat | `Abort message` |
| --- | --- | --- | --- |
| `default` | `SIGSEGV`, null pointer dereference | Only the out-of-memory specific `HermesGC` line | None |
| `control` | `SIGABRT` | Yes | Yes |
