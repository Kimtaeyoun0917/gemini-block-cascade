# 테트리스 안드로이드 앱 (APK)

저장소의 `tetris/` 웹앱(HTML·JS·CSS·아이콘)을 그대로 안에 담아 `file:///android_asset/www/index.html`로
띄우는 최소 WebView 앱입니다. 인터넷 없이도 바로 실행됩니다.

## 구조

```
android/
├─ settings.gradle / build.gradle / gradle.properties
└─ app/
   ├─ build.gradle                 # applicationId com.kimtaeyoun.tetris, versionName 1.0.0, minSdk 24, targetSdk 34
   └─ src/main/
      ├─ AndroidManifest.xml       # 앱 이름 "테트리스", 세로 고정, 진동 권한
      ├─ java/com/kimtaeyoun/tetris/MainActivity.java
      ├─ res/                      # 아이콘(적응형 + 레거시), 테마, 문자열
      └─ assets/www/               # 빌드 때 tetris/ 파일이 복사되는 자리(비어 있음)
```

## 웹앱 유지

- GitHub Pages 웹앱 주소와 `tetris/` 폴더는 손대지 않았습니다.
  https://kimtaeyoun0917.github.io/gemini-block-cascade/tetris/
- 앱은 빌드 시점의 `tetris/` 파일을 복사해 쓰므로, 웹앱을 고치면 새로 빌드하면 됩니다.

## 빌드 (GitHub Actions)

`.github/workflows/build-apk.yml`이 push되면 ubuntu 러너(JDK 17 + Android SDK + Gradle 8.7)에서

1. `tetris/` → `app/src/main/assets/www/` 복사
2. `gradle :app:assembleRelease`
3. `zipalign -p 4` → `apksigner`(v1+v2+v3, 빌드용 임시 keystore) 서명
4. 내용 검증 후 `tetris.apk` 아티팩트 업로드

를 수행합니다.

> 서명 키는 빌드마다 새로 만들어집니다. 그래서 지금 설치한 앱 위에 나중에 새로 빌드한 APK를
> 덮어 설치하려면 서명이 달라 실패하고, 지우고 다시 설치해야 합니다. 계속 업데이트하려면
> 같은 keystore를 저장소 시크릿으로 고정하는 작업이 따로 필요합니다.
