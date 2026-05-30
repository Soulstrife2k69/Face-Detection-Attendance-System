package com.example.facedetectionattendancesystem

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.Timestamp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var faceBoxView: FaceBoxView
    private lateinit var nameEditText: EditText
    private lateinit var enrollButton: Button
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var faceDetector: FaceDetector? = null
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    private val db = Firebase.firestore
    private val enrolledFaces = mutableMapOf<String, String>() // <Signature, Name>
    private var currentFaceSignature: String? = null
    private val recentlyMarked = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        faceBoxView = findViewById(R.id.face_box_view)
        nameEditText = findViewById(R.id.name_edit_text)
        enrollButton = findViewById(R.id.enroll_button)
        cameraExecutor = Executors.newSingleThreadExecutor()

        enrollButton.setOnClickListener { enrollFace() }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) setupCamera() else Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera()
            loadEnrolledFaces()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun loadEnrolledFaces() {
        db.collection("enrolled_faces").get().addOnSuccessListener { result ->
            for (document in result) {
                enrolledFaces[document.id] = document.getString("name") ?: ""
            }
            Log.d(TAG, "Loaded ${enrolledFaces.size} faces.")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error loading faces.", e)
            Toast.makeText(this, "Error loading faces: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun enrollFace() {
        val name = nameEditText.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        currentFaceSignature?.let { newSignature ->
            val similarFace = findSimilarEnrolledFace(newSignature)
            if (similarFace != null) {
                Toast.makeText(this, "Face already enrolled as ${similarFace.second}", Toast.LENGTH_SHORT).show()
                return@let
            }

            db.collection("enrolled_faces").document(newSignature).set(hashMapOf("name" to name)).addOnSuccessListener {
                enrolledFaces[newSignature] = name
                nameEditText.text.clear()
                Toast.makeText(this, "Enrolled $name successfully!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Enrollment failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "No stable face detected. Please hold still.", Toast.LENGTH_SHORT).show()
    }

    private fun markAttendance(signature: String, name: String) {
        if (recentlyMarked.contains(signature)) return
        recentlyMarked.add(signature)
        val attendanceRecord = hashMapOf("name" to name, "timestamp" to Timestamp.now())
        db.collection("attendance_log").add(attendanceRecord).addOnSuccessListener {
            Log.d(TAG, "Marked $name present at ${attendanceRecord["timestamp"]}")
            Toast.makeText(this, "Marked $name present", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to mark attendance for $name", e)
        }
        Handler(Looper.getMainLooper()).postDelayed({ recentlyMarked.remove(signature) }, 10000)
    }

    private fun findSimilarEnrolledFace(newSignature: String): Pair<String, String>? {
        val newSigValues = newSignature.split(":").mapNotNull { it.toFloatOrNull() }
        if (newSigValues.size != 8) return null

        for ((existingSig, name) in enrolledFaces) {
            val existingSigValues = existingSig.split(":").mapNotNull { it.toFloatOrNull() }
            if (existingSigValues.size != 8) continue

            var dist = 0f
            for (i in 0 until 8) {
                dist += abs(newSigValues[i] - existingSigValues[i])
            }

            if (dist < SIMILARITY_THRESHOLD) {
                return Pair(existingSig, name)
            }
        }
        return null
    }

    private fun setupCamera() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL) // Ensure contours are detected
            .build()
        faceDetector = FaceDetection.getClient(options)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({ bindCameraUseCases(cameraProviderFuture.get()) }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
//        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analyzer ->
                analyzer.setAnalyzer(cameraExecutor, FaceAnalyzer(faceDetector!!, faceBoxView, cameraSelector, enrolledFaces, {
                    currentFaceSignature = it
                }, this::findSimilarEnrolledFace) { signature, name ->
                    markAttendance(signature, name)
                })
            }

        try {
            if (!cameraProvider.hasCamera(cameraSelector)) cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    class FaceAnalyzer(
        private val detector: FaceDetector,
        private val faceBoxView: FaceBoxView,
        private val cameraSelector: CameraSelector,
        private val enrolledFaces: Map<String, String>,
        private val onFaceSignatureReady: (String?) -> Unit,
        private val findSimilarFace: (String) -> Pair<String, String>?,
        private val onAttendanceMark: (String, String) -> Unit
    ) : ImageAnalysis.Analyzer {

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
            val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image).addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val mainFace = faces[0]
                    val signature = generateSignature(mainFace)
                    onFaceSignatureReady(signature)

                    val recognizedFace = signature?.let { findSimilarFace(it) }
                    val existingSignature = recognizedFace?.first
                    val recognizedName = recognizedFace?.second

                    if (recognizedName != null && existingSignature != null) {
                        onAttendanceMark(existingSignature, recognizedName)
                    }

                    val displayName = recognizedName ?: if (signature != null) "New Face" else "Detecting..."
                    faceBoxView.update(listOf(DisplayableFace(mainFace.boundingBox, null, displayName)), image.width, image.height, cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA, enrolledFaces)
                } else {
                    onFaceSignatureReady(null)
                    faceBoxView.update(emptyList(), image.width, image.height, cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA, enrolledFaces)
                }
            }.addOnCompleteListener { imageProxy.close() }
        }

        private fun getContourCentroid(contour: FaceContour?): PointF? {
            return contour?.points?.takeIf { it.isNotEmpty() }?.let {
                val sumX = it.sumOf { p -> p.x.toDouble() }
                val sumY = it.sumOf { p -> p.y.toDouble() }
                PointF((sumX / it.size).toFloat(), (sumY / it.size).toFloat())
            }
        }

        private fun distance(p1: PointF, p2: PointF): Float {
            return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
        }

        private fun generateSignature(face: Face): String? {
            val leftEye = getContourCentroid(face.getContour(FaceContour.LEFT_EYE))
            val rightEye = getContourCentroid(face.getContour(FaceContour.RIGHT_EYE))
            val noseBridge = getContourCentroid(face.getContour(FaceContour.NOSE_BRIDGE))
            val noseBottom = getContourCentroid(face.getContour(FaceContour.NOSE_BOTTOM))

            // Robust mouth corner detection by averaging lip points
            val leftMouthTop = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points?.firstOrNull()
            val leftMouthBottom = face.getContour(FaceContour.LOWER_LIP_TOP)?.points?.firstOrNull()
            val rightMouthTop = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points?.lastOrNull()
            val rightMouthBottom = face.getContour(FaceContour.LOWER_LIP_TOP)?.points?.lastOrNull() // Corrected typo here

            if (leftEye == null || rightEye == null || noseBridge == null || noseBottom == null || leftMouthTop == null || leftMouthBottom == null || rightMouthTop == null || rightMouthBottom == null) return null

            val leftMouth = PointF((leftMouthTop.x + leftMouthBottom.x) / 2, (leftMouthTop.y + leftMouthBottom.y) / 2)
            val rightMouth = PointF((rightMouthTop.x + rightMouthBottom.x) / 2, (rightMouthTop.y + rightMouthBottom.y) / 2)

            val eyeDistance = distance(leftEye, rightEye)
            if (eyeDistance == 0f) return null

            // Generate 8 ratios for a more unique signature
            val r1 = distance(noseBridge, leftEye) / eyeDistance
            val r2 = distance(noseBridge, rightEye) / eyeDistance
            val r3 = distance(noseBottom, leftMouth) / eyeDistance
            val r4 = distance(noseBottom, rightMouth) / eyeDistance
            val r5 = distance(leftMouth, rightMouth) / eyeDistance // Mouth width
            val r6 = distance(noseBridge, noseBottom) / eyeDistance // Nose length
            val r7 = distance(leftEye, leftMouth) / eyeDistance
            val r8 = distance(rightEye, rightMouth) / eyeDistance

            return String.format("%.4f:%.4f:%.4f:%.4f:%.4f:%.4f:%.4f:%.4f", r1, r2, r3, r4, r5, r6, r7, r8)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceDetector?.close()
    }

    companion object {
        private const val TAG = "FaceDetectionApp"
        private const val SIMILARITY_THRESHOLD = 0.25f // Stricter threshold
    }
}
