package com.example.camerakitbasic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.snap.camerakit.Session
import com.snap.camerakit.invoke
import com.snap.camerakit.lenses.LensesComponent
import com.snap.camerakit.lenses.whenHasFirst
import com.snap.camerakit.support.camerax.CameraXImageProcessorSource
import com.snap.camerakit.supported

class CameraViewActivity : AppCompatActivity(R.layout.activity_camera_view) {

    private lateinit var cameraKitSession: Session
    private lateinit var imageProcessorSource: CameraXImageProcessorSource

    companion object {
        const val LENS_GROUP_ID = "ed39b326-6f8c-4d3f-8ea0-d4002487a8d9"
        const val LENS_ID = "b28ef636-a50a-4309-9164-716ccc9fbc04"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Checking if Camera Kit is supported on this device or not.
        if (!supported(this)) {
            Toast.makeText(this, "Camera Kit Not Supported", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imageProcessorSource = CameraXImageProcessorSource(
            context = this, lifecycleOwner = this
        )

        // If camera permission is granted, then start the preview
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startPreview()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        cameraKitSession = Session(context = this) {
            imageProcessorSource(imageProcessorSource)
            attachTo(findViewById(R.id.camera_kit_stub))
        }.apply {
            lenses.repository.observe(
                LensesComponent.Repository.QueryCriteria.ById(LENS_ID, LENS_GROUP_ID)
            ) { result ->
                result.whenHasFirst { requestedLens ->
                    lenses.processor.apply(requestedLens)
                }
            }
        }
    }

    // Initialize a permission request launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startPreview()
            } else {
                // Explain to the user that Camera Kit is unavailable because the
                // requested camera permission is denied by the user, then attempt retry.
            }
        }

    private fun startPreview() {
        // starting preview with world facing camera
        imageProcessorSource.startPreview(false)
    }

    override fun onDestroy() {
        cameraKitSession.close()
        super.onDestroy()
    }
}