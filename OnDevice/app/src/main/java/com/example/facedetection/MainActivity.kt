package com.example.facedetection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ResolveInfoFlags
import android.content.pm.ResolveInfo
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.IOException
import java.security.AccessController.getContext
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val img: ImageView = findViewById(R.id.imageFace)
        // assets folder image file name with extension
        val fileName = "face-test.jpg"
        // get bitmap from assets folder
        val bitmap: Bitmap? = assetsToBitmap(fileName)
        bitmap?.apply{
            img.setImageBitmap(this)
        }

        val btn: Button = findViewById(R.id.btnTest)
        btn.setOnClickListener {
            val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
            val detector = FaceDetection.getClient(highAccuracyOpts)
            val image = InputImage.fromBitmap(bitmap!!, 0)
            val result = detector.process(image)
                .addOnSuccessListener { faces ->
                    // Task completed successfully
                    // ...
                    bitmap?.apply {
                        img.setImageBitmap(drawWithRectangle(faces))
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
        }


        var photoName: String? = null
        val takePhoto = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { didTakePhoto: Boolean ->
            // Handle the result
            if (didTakePhoto && photoName != null) {
                // save filename into DB
            }
        }

        val photoCamera: ImageButton = findViewById(R.id.btnTakePhoto)
        photoCamera.setOnClickListener {
            photoName = "IMG_${Date()}.JPG"
//            val photoFile = File(requireContext().applicationContext.filesDir, photoName)
            val photoFile = File(this.filesDir, photoName)
            val photoUri = FileProvider.getUriForFile(
//                requireContext(),
                this,
                "com.bignerdranch.android.criminalintent.fileprovider",
                photoFile
            )
            takePhoto.launch(photoUri)
        }

        val captureImageIntent = takePhoto.contract.createIntent(
            // Context(),
            this,
//            null
            Uri.parse("")
        )
        photoCamera.isEnabled = canResolveIntent(captureImageIntent)

    }


    fun Bitmap.drawWithRectangle(faces: List<Face>):Bitmap?{
        val bitmap = copy(config, true)
        val canvas = Canvas(bitmap)
        for (face in faces){
            val bounds = face.boundingBox
            Paint().apply {
                color = Color.GREEN
                style = Paint.Style.STROKE
                strokeWidth = 4.0f
                isAntiAlias = true
                // draw rectangle on canvas
                canvas.drawRect(
                    bounds, this
                )
            }
        }
        return bitmap
    }

    // helper function to get bitmap from assets
    fun Context.assetsToBitmap(fileName: String): Bitmap?{
        return try {
            with(assets.open(fileName)){
                BitmapFactory.decodeStream(this)
            }
        } catch (e: IOException) { null }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = this.packageManager
        return (intent.resolveActivity(packageManager) != null)
        /*  for  < API 33
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null */
    }

}