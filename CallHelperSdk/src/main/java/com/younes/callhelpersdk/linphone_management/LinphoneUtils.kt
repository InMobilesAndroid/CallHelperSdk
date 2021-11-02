package com.younes.callhelpersdk.linphone_management

import android.app.Activity
import android.content.Context
import android.util.Log
import org.linphone.core.*
import java.io.File
import java.io.IOException

class LinphoneUtils private constructor() {
    private var mLinphoneCore: Core? = null
    @Throws(CoreException::class)
    fun registerUserAuth(name: String, password: String, host: String) {

        mLinphoneCore = LinphoneManager.getLcCore()
        if (mLinphoneCore!=null){
            mLinphoneCore?.enableEchoCancellation(true)
            mLinphoneCore?.enableEchoLimiter(true)
        }


        val identify = "sip:$name@$host;transport=tls"
        val prxCfg = mLinphoneCore?.createProxyConfig()
        prxCfg?.edit()
        val identifyAddr = Factory.instance().createAddress(identify)
        identifyAddr.transport = TransportType.Tls
        //        identifyAddr.setPort(5060);
        prxCfg?.identityAddress = identifyAddr
        prxCfg?.serverAddr = host
        prxCfg?.enableQualityReporting(false)
        prxCfg?.qualityReportingCollector = null
        prxCfg?.qualityReportingInterval = 0
        prxCfg?.enableRegister(true)
        prxCfg?.done()
        Log.e(TAG, "registerUserAuth name = $name")
        Log.e(TAG, "registerUserAuth pw = $password")
        Log.e(TAG, "registerUserAuth host = $host")
        val authInfo = Factory.instance().createAuthInfo(
            name, null, password,
            null, host, host
        )
        if (mLinphoneCore?.proxyConfigList!!.isNotEmpty()) {
            for (proxyConfig in mLinphoneCore!!.proxyConfigList) {
                if (prxCfg?.identityAddress!!.username === proxyConfig.identityAddress.username) return else {
                    mLinphoneCore!!.removeProxyConfig(proxyConfig)
                    mLinphoneCore!!.addProxyConfig(prxCfg)
                    mLinphoneCore!!.addAuthInfo(authInfo)
                    mLinphoneCore!!.defaultProxyConfig = prxCfg
                }
            }
        } else {
            mLinphoneCore?.addProxyConfig(prxCfg)
            mLinphoneCore?.addAuthInfo(authInfo)
            mLinphoneCore?.defaultProxyConfig = prxCfg
        }
    }

    fun startSingleCallingTo(bean: User, activity: Activity?): Call? {
        mLinphoneCore!!.enableMic(true)
        var call: Call? = null
        val address: Address = mLinphoneCore!!.interpretUrl(
            "sip:" + bean.userName.toString() + "@" + bean.host
                .toString() + ";transport=tls"
        )
        address.displayName = bean.displayName
        address.domain = bean.host
        address.transport = TransportType.Tls
        //        address.setPort(5080);
        val params = mLinphoneCore!!.createCallParams(null)
        params.enableVideo(false)
        params.enableAudio(true)
        Log.d("rtpprofile", params.rtpProfile)
        params.addCustomSdpMediaAttribute(StreamType.Audio, "rtpmap", "0 PCMU/8000")
        call = mLinphoneCore!!.inviteAddressWithParams(address, params)
        return call
    }

    fun hangUp() {
        val currentCall = mLinphoneCore!!.currentCall
        if (currentCall != null) {
            mLinphoneCore!!.terminateAllCalls()
        } else if (mLinphoneCore!!.isInConference) {
            mLinphoneCore!!.terminateConference()
        } else {
            mLinphoneCore!!.terminateAllCalls()
        }
    }

    fun toggleMicro(isMicMuted: Boolean) {
        mLinphoneCore!!.enableMic(!isMicMuted)
    }

    companion object {
        private const val TAG = "LinphoneUtils"

        @Volatile
        private var sLinphoneUtils: LinphoneUtils? = null
        val instance: LinphoneUtils?
            get() {
                if (sLinphoneUtils == null) {
                    synchronized(LinphoneUtils::class.java) {
                        if (sLinphoneUtils == null) {
                            sLinphoneUtils = LinphoneUtils()
                        }
                    }
                }
                return sLinphoneUtils
            }

        @Throws(IOException::class)
        fun copyIfNotExist(context: Context, resourceId: Int, target: String?) {
            val fileToCopy = File(target)
            if (!fileToCopy.exists()) {
                copyFromPackage(context, resourceId, fileToCopy.name)
            }
        }

        @Throws(IOException::class)
        fun copyFromPackage(context: Context, resourceId: Int, target: String?) {
            val outputStream = context.openFileOutput(target, 0)
            val inputStream = context.resources.openRawResource(resourceId)
            var readByte: Int
            val buff = ByteArray(8048)
            while (inputStream.read(buff).also { readByte = it } != -1) {
                outputStream.write(buff, 0, readByte)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        }

        fun getConfig(context: Context): Config {
//            if (LinphoneManager.isInstanceiated) {
//                org.linphone.mediastream.Log.w("LinphoneManager not instanciated yet...")
//                return Factory.instance()
//                    .createConfig(context.filesDir.absolutePath + "/.linphonerc")
//            }
//
            if (LinphoneManager.getConifFile().isNullOrEmpty()) {
                return Factory.instance()
                    .createConfig(context.filesDir.absolutePath + "/.linphonerc")
            }
            else
                return Factory.instance()
                    .createConfig(LinphoneManager.getConifFile())


        }

        fun sleep(time: Int) {
            try {
                Thread.sleep(time.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }


    }
}