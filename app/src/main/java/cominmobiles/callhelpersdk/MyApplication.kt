package cominmobiles.callhelpersdk

import android.app.Application
import android.util.Log
import com.younes.callhelpersdk.LinphoneImpl

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("testAPplication","oncreate")
        LinphoneImpl.startLinphone(this)
    }
}