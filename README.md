<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&pause=1000&color=00C2FF&center=true&vCenter=true&width=1000&lines=Face+Detection+Attendance+System;Android+App+Using+ML+Kit+%26+Firebase;Real-Time+Face+Recognition+Attendance;Built+with+Kotlin+%7C+CameraX+%7C+Firestore" />
</p>

<h1 align="center">📸 Face Detection Attendance System</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/ML_Kit-Face_Detection-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/CameraX-Live_Camera-red?style=for-the-badge"/>
</p>

<p align="center">
  <img src="https://komarev.com/ghpvc/?username=Soulstrife2k69&label=Project+Views&color=0e75b6&style=flat" />
</p>

---

# 🧠 About The Project

<img align="right" src="https://media.giphy.com/media/f3iwJFOVOwuy7K6FFw/giphy.gif" width="280"/>

**Face Detection Attendance System** is an Android app that uses your phone's camera and **Google ML Kit** to detect, enroll, and recognize faces in real-time — automatically logging attendance in **Firebase Firestore** the moment a known face appears.

> 🚫 No manual check-ins.  
> 🚫 No ID cards.  
> ✅ Just look at the camera.

<br clear="right"/>

---

## ✨ Features

<img src="https://user-images.githubusercontent.com/74038190/212284115-f47cd8ff-2ffb-4b04-b5bf-4d1c14c0247f.gif" width="100%"/>

| | Feature | Description |
|---|---|---|
| 📸 | **Real-time Detection** | CameraX + ML Kit detects faces live from camera feed |
| 🧬 | **Facial Signature Engine** | Unique 8-ratio geometric signature from face contours |
| 👤 | **Face Enrollment** | Enroll any face with a name stored in Firestore |
| ✅ | **Auto Attendance** | Recognizes enrolled faces and logs attendance + timestamp |
| 🔁 | **Duplicate Prevention** | 10-second cooldown per person per session |
| 🤳 | **Front Camera Support** | Mirrored preview for accurate selfie-mode detection |
| ☁️ | **Cloud Sync** | All data synced to Firebase Firestore in real-time |

---

## 🏗️ Tech Stack

<div align="center">
<img src="https://skillicons.dev/icons?i=kotlin,androidstudio,firebase,tensorflow,gradle&theme=dark" />
</div>

<br/>

```
FaceDetectionAttendanceSystem/
│
├── 📷  CameraX           → Live camera preview & frame analysis
├── 🤖  ML Kit            → Face detection with full contour mode
├── 🧮  Signature Engine  → 8-point geometric facial ratio system
├── 🎨  FaceBoxView       → Custom Canvas View for bounding boxes
├── ☁️  Firebase Firestore → Enrolled faces & attendance log
└── 🔁  Similarity Matcher → L1-distance match (threshold: 0.25)
```

---

## 🔬 How the Face Signature Works

<div align="center">
<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=14&duration=2000&pause=500&color=00FF88&center=true&vCenter=true&width=620&lines=Generating+facial+signature...;r1%3A+noseBridge+to+leftEye+%2F+eyeDist;r2%3A+noseBridge+to+rightEye+%2F+eyeDist;r3-r8%3A+mouth%2C+nose+%26+cheek+ratios...;Signature+ready+%E2%80%94+Match+threshold+%3C+0.25" alt="Signature Generation"/>
</div>

```kotlin
r1 = noseBridge → leftEye    / eyeDistance
r2 = noseBridge → rightEye   / eyeDistance
r3 = noseBottom → leftMouth  / eyeDistance
r4 = noseBottom → rightMouth / eyeDistance
r5 = mouth width             / eyeDistance
r6 = nose length             / eyeDistance
r7 = leftEye    → leftMouth  / eyeDistance
r8 = rightEye   → rightMouth / eyeDistance

// Output → "r1:r2:r3:r4:r5:r6:r7:r8"  (Match if L1 dist < 0.25)
```

---

## 🚀 Getting Started

<img src="https://user-images.githubusercontent.com/74038190/229223263-cf2e4b07-2615-4f87-9c38-e37600f8381a.gif" width="100%"/>

### Prerequisites

- ✅ Android Studio (Hedgehog or newer)
- ✅ Android device (Min SDK 26 / Android 8.0+)
- ✅ Firebase project with Firestore enabled

### Setup

```bash
# 1️⃣ Clone the repo
git clone https://github.com/Soulstrife2k69/Face-Detection-Attendance-System.git
cd Face-Detection-Attendance-System

# 2️⃣ Add google-services.json to /app folder
# (Download from Firebase Console → Project Settings)

# 3️⃣ Open in Android Studio and hit ▶️ Run
```

---

## 📱 How To Use

<div align="center">

```
┌─────────────────────────────────────┐
│  1️⃣  Launch app → allow camera       │
│  2️⃣  Point camera at a face          │
│  3️⃣  Type person's name in field     │
│  4️⃣  Tap ENROLL to register face     │
│  5️⃣  That face = auto attendance ✅  │
└─────────────────────────────────────┘
```

</div>

---

## 🗄️ Firebase Structure

```
Firestore Database
├── 📁 enrolled_faces/
│   └── {faceSignature}    →  { name: "Shubh" }
│
└── 📁 attendance_log/
    └── {auto-id}          →  { name: "Shubh", timestamp: Timestamp }
```

---

## 📦 Key Dependencies

```kotlin
com.google.mlkit:face-detection:16.1.7
androidx.camera:camera-core:1.5.1
com.google.firebase:firebase-bom:33.1.0
org.tensorflow:tensorflow-lite:2.9.0
```

---

## 🔮 Future Improvements

- [ ] 🔐 Admin login with Firebase Auth
- [ ] 📊 Live attendance dashboard with charts
- [ ] 📤 Export attendance log to Excel / CSV
- [ ] 🧠 Replace signature with TFLite deep embedding model
- [ ] 🌙 Dark / Light theme toggle
- [ ] 📅 Date-wise attendance filtering
- [ ] 🔔 Push notifications for attendance summary

---

## 📊 Project Stats

<div align="center">

![GitHub repo size](https://img.shields.io/github/repo-size/Soulstrife2k69/Face-Detection-Attendance-System?style=for-the-badge&color=a78bfa)
![GitHub last commit](https://img.shields.io/github/last-commit/Soulstrife2k69/Face-Detection-Attendance-System?style=for-the-badge&color=4285F4)
![GitHub stars](https://img.shields.io/github/stars/Soulstrife2k69/Face-Detection-Attendance-System?style=for-the-badge&color=FFCA28)

</div>

---

## 🙋‍♂️ Developer

<div align="center">

<img src="https://github.com/Soulstrife2k69.png" width="100" style="border-radius:50%;"/>

### Shubhojit Nandy

<a href="https://github.com/Soulstrife2k69">
  <img src="https://img.shields.io/badge/GitHub-Follow-181717?style=for-the-badge&logo=github"/>
</a>

<br/><br/>

<img src="https://komarev.com/ghpvc/?username=Soulstrife2k69&label=Profile+Views&color=a78bfa&style=for-the-badge"/>

</div>

---

<div align="center">

<img src="https://user-images.githubusercontent.com/74038190/212284100-561aa473-3905-4a80-b561-0d28506553ee.gif" width="700"/>

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:24243e,50:302b63,100:0f0c29&height=120&section=footer&animation=twinkling" width="100%"/>

</div>
