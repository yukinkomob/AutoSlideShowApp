package jp.techacademy.yuki.nishimura.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    private var mHandler: Handler = Handler()

    private val mMediaUriList = ArrayList<Uri>()
    private var mImgIndex = -1

    private var mIsPlayingSlideshow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        next_button.setOnClickListener {
            if (!mIsPlayingSlideshow) {
                showNextImage()
            }
        }
        back_button.setOnClickListener {
            if (!mIsPlayingSlideshow) {
                showPrevImage()
            }
        }
        play_pause_button.setOnClickListener {
            if (!mIsPlayingSlideshow) {
                playSlideshow()
            } else {
                pauseSlideshow()
            }
        }
    }

    private fun showNextImage() {
        if (mImgIndex == mMediaUriList.size - 1) {
            mImgIndex = 0
        } else {
            mImgIndex++
        }
        imageView.setImageURI(mMediaUriList[mImgIndex])
    }

    private fun showPrevImage() {
        if (mImgIndex == 0) {
            mImgIndex = mMediaUriList.size - 1
        } else {
            mImgIndex--
        }
        imageView.setImageURI(mMediaUriList[mImgIndex])
    }

    private fun playSlideshow() {
        if (mTimer == null) {
            mTimer = Timer()

            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mHandler.post {
                        showNextImage()
                    }
                }
            }, 2000, 2000)
        }
        mIsPlayingSlideshow = true
        next_button.isEnabled = false
        back_button.isEnabled = false
        play_pause_button.text = "停止"
    }

    private fun pauseSlideshow() {
        if (mTimer != null) {
            mTimer!!.cancel()
            mTimer = null
        }
        mIsPlayingSlideshow = false
        next_button.isEnabled = true
        back_button.isEnabled = true
        play_pause_button.text = "再生"
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager
                    .PERMISSION_GRANTED
            ) {
                getContentsInfo()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    finishAffinity()
                    Toast.makeText(applicationContext, "アプリの利用に必要な許可が得られませんでした。", Toast.LENGTH_LONG)
                        .show()
                }
        }
    }

    private fun disableFunctions() {
        next_button.isEnabled = false
        back_button.isEnabled = false
        play_pause_button.isEnabled = false
        noImageText.visibility = View.VISIBLE
        imageView.visibility = View.GONE
    }

    private fun getContentsInfo() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null,
        )

        if (cursor!!.moveToFirst()) {
            do {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media
                        .EXTERNAL_CONTENT_URI, id
                )

                mMediaUriList.add(imageUri)
                Log.d("kotlintest", "URI : " + imageUri.toString())
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (mMediaUriList.size == 0) {
            disableFunctions()
        } else {
            mImgIndex = 0
            imageView.setImageURI(mMediaUriList[mImgIndex])
        }
    }
}