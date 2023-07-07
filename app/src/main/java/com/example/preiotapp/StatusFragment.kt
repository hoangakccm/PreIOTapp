package com.example.preiotapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.testapi.*
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class StatusFragment : Fragment() {
    private lateinit var tvLed1: TextView
    private lateinit var tvLed2: TextView
    private lateinit var tvButton: TextView
    private lateinit var tvFreg: TextView
    private lateinit var tvFreg2: TextView
    private lateinit var slider : SeekBar
    private lateinit var slider2 : SeekBar
    private lateinit var switchOnOff : Switch
    private lateinit var imgLed1 : ImageView
    private lateinit var imgLed2 : ImageView
    private var currentSeekbar = 0
    private var currentSeekbar2 = 0
    private var s1 = false
    private var s2 = false

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_status, container, false)
        tvLed1 = view.findViewById(R.id.led1_id_tv)
        tvLed2 = view.findViewById(R.id.led2_id_tv)
        tvButton = view.findViewById(R.id.tv_button)
        tvFreg = view.findViewById(R.id.tv_freg)
        tvFreg2 = view.findViewById(R.id.tv_freg2)
        slider = view.findViewById(R.id.control_blink)
        slider2 = view.findViewById(R.id.control_blink2)
        switchOnOff = view.findViewById(R.id.switch_button)
        imgLed1 = view.findViewById(R.id.imgLed1)
        imgLed2 = view.findViewById(R.id.imgLed2)

        getMyData()
        SeekBarEvent()
        eventHandler()
        return view
    }

    private fun eventHandler() {

//        switchOnOff.setOnCheckedChangeListener { _, isChecked ->
//
//            val newSwitch1 = if (isChecked) "true" else "false"
//            tvButton.text = newSwitch1
//            mData.child("Button").child("status").setValue(newSwitch1)
//            postMyData()
//        }
    }

    private fun SeekBarEvent() {
        slider.max = 50
        slider.progress = currentSeekbar
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvFreg.text = progress.toString()
                currentSeekbar = progress

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                postMyData()
            }
        })
        slider2.max = 50
        slider2.progress = currentSeekbar2
        slider2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvFreg2.text = progress.toString()
                currentSeekbar2 = progress


            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {postMyData()}
        })
        tvFreg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let { value ->
                    if (value.isNotEmpty()) {
                        val progress = value.toIntOrNull()
                        progress?.let {
                            if (it >= slider.min && it <= slider.max) {
                                slider.progress = it
                                currentSeekbar = it
                                if(currentSeekbar==0){
                                    val drawable = resources.getDrawable(R.drawable.led_off, null)
                                    imgLed1.setImageDrawable(drawable)
                                }else{
                                    val drawable = resources.getDrawable(R.drawable.led_on, null)
                                    imgLed1.setImageDrawable(drawable)
                                }

                            }
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        tvFreg2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let { value ->
                    if (value.isNotEmpty()) {
                        val progress = value.toIntOrNull()
                        progress?.let {
                            if (it >= slider2.min && it <= slider2.max) {
                                slider2.progress = it
                                currentSeekbar2 = it
                                if(currentSeekbar2==0){
                                    val drawable = resources.getDrawable(R.drawable.led_off, null)
                                    imgLed2.setImageDrawable(drawable)
                                }else{
                                    val drawable = resources.getDrawable(R.drawable.led_on, null)
                                    imgLed2.setImageDrawable(drawable)
                                }
                            }
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

    }


    private fun getMyData() {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)
        val retrofitData = retrofitBuilder.getData()
        retrofitData.enqueue(object : Callback<MyDataItem?> {
            override fun onResponse(call: Call<MyDataItem?>, response: Response<MyDataItem?>) {
                val responseBody = response.body()
                if (responseBody != null) {
                    updateUIWithData(responseBody)
                }
                Log.d("TAG", "onResponse GET DATA: ")

                // Lặp lại việc gọi hàm getMyData sau mỗi khoảng thời gian UPDATE_INTERVAL_MS
                Handler(Looper.getMainLooper()).postDelayed({
                    getMyData()
                }, UPDATE_INTERVAL_MS)
            }

            override fun onFailure(call: Call<MyDataItem?>, t: Throwable) {
                Log.d("TAG", "onFailure GET DATA: ${t.message}")

                // Nếu gặp lỗi, cũng lặp lại việc gọi hàm getMyData sau mỗi khoảng thời gian UPDATE_INTERVAL_MS
                Handler(Looper.getMainLooper()).postDelayed({
                    getMyData()
                }, UPDATE_INTERVAL_MS)
            }
        })
    }

    private fun updateUIWithData(data: MyDataItem) {
        var led1 = data.Led1.status

        var led2 = data.Led2.status
        var switch = data.Button
        var freg = data.Led1.freq
        var freg2 = data.Led2.freq
        var filename =  data.fileUpdate
        s1=led1
        s2=led2
        tvLed1.text = "LED 1: $led1"
        tvLed2.text = "LED 2: $led2"
        tvButton.text = switch.toString()
        tvFreg.text = freg.toString()
        tvFreg2.text = freg2.toString()

        switchOnOff.isChecked = switch

    }

    private fun postMyData() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // Địa chỉ base URL của API
            .addConverterFactory(GsonConverterFactory.create()) // Sử dụng GsonConverterFactory để chuyển đổi dữ liệu thành JSON
            .build()
        val apiInterface = retrofit.create(ApiInterface::class.java)



        val myDataItem = MyDataItem(
            Button = false,
            id = "1",
            myLocation = MyLocation("12.1111111","105.213515"),
            Led1 = Led(freq = currentSeekbar,true),
            Led2 = Led(freq = currentSeekbar2,true),
            fileUpdate = ""
        )

        val call = apiInterface.postData(myDataItem)
        call.enqueue(object : Callback<MyDataItem> {
            override fun onResponse(call: Call<MyDataItem?>, response: Response<MyDataItem?>) {
                if (response.isSuccessful) {
                    // Request thành công, xử lý dữ liệu phản hồi
                    val responseData = response.body()
                    // Thực hiện các xử lý với dữ liệu trả về (responseData)
                    Log.i("TAG", "Request thành công")
                } else {
                    // Request không thành công, xử lý lỗi
                    val errorBody = response.errorBody()?.string()
                    Log.e("TAG", "Request không thành công. Mã phản hồi: ${response.code()}, Lỗi: $errorBody")
                }
            }

            override fun onFailure(call: Call<MyDataItem?>, t: Throwable) {
                // Xử lý khi có lỗi trong quá trình gửi request
                Log.e("TAG", "Lỗi trong quá trình gửi request: ${t.message}")
            }

        })

    }

    companion object {
        const val UPDATE_INTERVAL_MS = 1000L
        const val BASE_URL = "http://192.168.4.1/"
    }

}