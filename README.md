# RaidManager

Kotlin 기반 libGDX 멀티플랫폼 게임 기본 프로젝트입니다.

## 모듈

- `core`: Android/macOS에서 공유하는 게임 로직
- `android`: Android 런처
- `lwjgl3`: macOS 데스크톱 런처

## 실행

```bash
./gradlew lwjgl3:run
./gradlew android:assembleDebug
```

Android Studio에서 프로젝트를 열어 `android` 실행 구성을 사용하거나 생성된 APK를 설치할 수 있습니다.

> 요청하신 `libGDK`는 일반적으로 사용되는 `libGDX`를 의미하는 것으로 보고 구성했습니다.
