package com.example.preiotapp

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.preiotapp.R
import com.example.testapi.ApiInterface
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

class UpdateFragment : Fragment() {
    private lateinit var uploadButton: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_update, container, false)
        uploadButton = view.findViewById(R.id.btnUpload)
        uploadButton.setOnClickListener {
            selectFile()
        }
        return view
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            val fileUri = data?.data
            fileUri?.let {
                val file = File(getRealPathFromUri(requireContext(), it))
                uploadFile(file)
            }
        }
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String {
        var realPath = ""

        // Check if the Uri scheme is "content"
        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    val fileName = it.getString(columnIndex)
                    val cacheDir = context.cacheDir
                    val file = File(cacheDir, fileName)
                    realPath = file.absolutePath
                }
            }
        }
        // If the Uri scheme is "file", simply return the path
        else if (ContentResolver.SCHEME_FILE == uri.scheme) {
            realPath = uri.path.orEmpty()
        }

        return realPath
    }


    private fun uploadFile(file: File) {
        val requestFile = RequestBody.create("*/*".toMediaTypeOrNull(), file)
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiInterface::class.java)
        val call = apiService.uploadFile(multipartBody)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "File uploaded successfully")
                    // Handle successful response
                } else {
                    Log.d(TAG, "Error uploading file")
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "Failed to upload file")
                // Handle failure
                t.printStackTrace()
                return  // Add this line
            }
        })

    }

    companion object {
        private const val FILE_REQUEST_CODE = 100
        private const val BASE_URL = "https://your-api-url.com/"
        private const val TAG = "UpdateFragment"
    }
}
