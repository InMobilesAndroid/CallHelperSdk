package com.younes.callhelpersdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.younes.callhelpersdk.activity.CallOutgoingActivity
import com.younes.callhelpersdk.callback.PhoneCallback
import com.younes.callhelpersdk.callback.RegistrationCallback
import com.younes.callhelpersdk.linphone_management.LinphoneManager
import com.younes.callhelpersdk.linphone_management.LinphoneUtils
import com.younes.callhelpersdk.linphone_management.User
import com.younes.callhelpersdk.service.LinphoneService

import org.linphone.core.*


object LinphoneImpl {
    private var mRegServiceWaitThread: RegServiceWaitThread? = null
    private var mPhoneServiceWaitThread: PhoneServiceWaitThread? = null
    private var mUsername: String? = null
    private var mPassword: String? = null
    private var mServerIP: String? = null
    fun startLinphone(context: Context) {
        startService(context)
    }

    private fun startService(context: Context) {
        LinphoneManager.startLibLinphone(context)
//        if (!LinphoneService.isReady) {
//            val intent = Intent(context, LinphoneService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(
//                intent
//            ) else context.startService(intent)
//        }
    }

    fun setAccount(username: String?, password: String?) {
        mUsername = username
        mPassword = password
        mServerIP = "sip.inmobiles.net:5060"
        login()
    }

    fun addCallback(registrationCallback: RegistrationCallback?) {
        if (LinphoneService.isReady) {
            LinphoneService.addRegistrationCallback(registrationCallback!!)
        } else {
            mRegServiceWaitThread = RegServiceWaitThread(registrationCallback)
            mRegServiceWaitThread!!.start()
        }
    }

    fun addPhoneCallBack(phoneCallback: PhoneCallback?) {
        if (LinphoneService.isReady) {
            LinphoneService.addPhoneCallback(phoneCallback!!)
        } else {
            mPhoneServiceWaitThread = PhoneServiceWaitThread(phoneCallback)
            mPhoneServiceWaitThread!!.start()
        }
    }

    private fun login() {
        Thread {
            while (!LinphoneService.isReady) {
                try {
                    Thread.sleep(80)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            loginToServer()
        }.start()
    }

    fun callTo(num: String, context: Activity) {
        if (!LinphoneService.isReady || !LinphoneManager.isInstanceiated) {
            Log.d("testStartActivyt", "dinotstart")
            return
        }
        val intent = Intent(context, CallOutgoingActivity::class.java)
        intent.putExtra("usercalling", "$num@$mServerIP")
        context.startActivity(intent)
        Log.d("testStartActivyt", "started")
        if (num != "") {
            val phone = User()
            phone.userName = num
            phone.host = mServerIP
            LinphoneUtils.instance?.startSingleCallingTo(phone, context)
        }
    }

    fun acceptCall() {
        val currentCall: Call = LinphoneManager.lc!!.currentCall
        val params: CallParams = LinphoneManager.lc!!.createCallParams(currentCall)
        params.enableVideo(false)
        currentCall.acceptWithParams(params)
    }

    fun declineCall() {
        val currentCall: Call = LinphoneManager.lc!!.currentCall
        currentCall.decline(Reason.Declined)
    }

    fun hangUp() {
        LinphoneUtils.instance!!.hangUp()
    }

    fun toggleMicro(isMicMuted: Boolean) {
        LinphoneUtils.instance!!.toggleMicro(isMicMuted)
    }

    private fun loginToServer() {
        try {
            if (mUsername == null || mPassword == null || mServerIP == null) {
                throw RuntimeException("The sip account is not configured.")
            }
            LinphoneUtils.instance!!.registerUserAuth(mUsername!!, mPassword!!, mServerIP!!)
        } catch (e: CoreException) {
            Log.d("testCoreException", e.toString())
            e.printStackTrace()
        }
    }

    val lC: Core
        get() = LinphoneManager.lc!!

    private class RegServiceWaitThread internal constructor(registrationCallback: RegistrationCallback?) :
        Thread() {
        private val mRegistrationCallback: RegistrationCallback?
        override fun run() {
            super.run()
            while (!LinphoneService.isReady) {
                try {
                    sleep(80)
                } catch (e: InterruptedException) {
                    throw RuntimeException("waiting thread sleep() has been interrupted")
                }
            }
            LinphoneService.addRegistrationCallback(mRegistrationCallback!!)
            mRegServiceWaitThread = null
        }

        init {
            mRegistrationCallback = registrationCallback
        }
    }

    private class PhoneServiceWaitThread internal constructor(phoneCallback: PhoneCallback?) :
        Thread() {
        private val mPhoneCallback: PhoneCallback?
        override fun run() {
            super.run()
            while (!LinphoneService.isReady) {
                try {
                    sleep(80)
                } catch (e: InterruptedException) {
                    throw RuntimeException("waiting thread sleep() has been interrupted")
                }
            }
            LinphoneService.addPhoneCallback(mPhoneCallback!!)
            mPhoneServiceWaitThread = null
        }

        init {
            mPhoneCallback = phoneCallback
        }
    }
}