package cominmobiles.callhelpersdk

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.younes.callhelpersdk.LinphoneImpl
import com.younes.callhelpersdk.callback.RegistrationCallback

class MainActivity : AppCompatActivity(), RegistrationCallback {
    var btnCall:Button?=null
    var btnRegister:Button?=null
    var etUserName:EditText?=null
    var etPassword:EditText?=null
    var etCallUser:EditText?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        LinphoneImpl.addCallback(this)

        btnCall = findViewById<View>(com.younes.callhelpersdk.R.id.btnCall) as Button
        btnRegister = findViewById<View>(com.younes.callhelpersdk.R.id.btnRegister) as Button
        etUserName = findViewById<View>(com.younes.callhelpersdk.R.id.etUserName) as EditText
        etPassword = findViewById<View>(com.younes.callhelpersdk.R.id.etPassword) as EditText
        etCallUser = findViewById<View>(com.younes.callhelpersdk.R.id.etCallUser) as EditText

        btnCall?.setOnClickListener(View.OnClickListener { v: View? ->
            if (!TextUtils.isEmpty(
                    etCallUser?.text.toString()
                )
            ) LinphoneImpl.callTo(
                etCallUser?.text.toString().trim(),
                this@MainActivity
            )
        })

        btnRegister?.setOnClickListener { v: View? ->
            if (!TextUtils.isEmpty(etUserName?.text.toString()) && !TextUtils.isEmpty(
                    etPassword?.text.toString()
                )
            ) LinphoneImpl.setAccount(
                etUserName?.text.toString().trim(),
                etPassword?.text.toString().trim()
            )
        }
    }

    override fun registrationNone() {
        Log.d("RegCallBAck", "registrationNone")
    }

    override fun registrationProgress() {
        Log.d("RegCallBAck", "registrationProgress")
    }

    override fun registrationOk() {
        Log.d("RegCallBAck", "registrationOk")
    }

    override fun registrationCleared() {
        Log.d("RegCallBAck", "registrationCleared")
    }

    override fun registrationFailed() {
        Log.d("RegCallBAck", "registrationFailed")
    }
}