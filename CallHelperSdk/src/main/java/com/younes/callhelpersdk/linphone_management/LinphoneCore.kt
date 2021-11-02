package com.younes.callhelpersdk.linphone_management

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.linphone.core.Core
import org.linphone.core.Factory
import java.io.File
import java.io.IOException
import java.util.*

class LinphoneCore(var context: Context) {

    private var lc: Core? = null

    private var TAG = "LinphoneCore"
    private var mTimer: Timer? = null
    private var mLPConfigXsd: String? = null
    private var mLinphoneFactoryConfigFile: String? = null
    var mLinphoneConfigFile: String? = null
    private var mLinphoneRootCaFile: String? = null
    private var mRingSoundFile: String? = null
    private var mRingBackSoundFile: String? = null
    private var mPauseSoundFile: String? = null

    fun startLinphone() {
        Factory.instance().setDebugMode(true, "LinphoneSDK")
        val basePath = context.filesDir.absolutePath
        mLPConfigXsd = "$basePath/lpconfig.xsd"
        mLinphoneFactoryConfigFile = "$basePath/linphonerc"
        mLinphoneConfigFile = "$basePath/.linphonerc"
        mLinphoneRootCaFile = "$basePath/rootca.pem"
        mRingSoundFile = "$basePath/oldphone_mono.wav"
        mRingBackSoundFile = "$basePath/ringback.wav"
        mPauseSoundFile = "$basePath/toy_mono.wav"

        try {
            copyAssetsFromPackage(context)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "startLibLinphone: cannot start linphone")
        }
        LinphoneManager.lc = Factory.instance().createCore(
            mLinphoneConfigFile,
            mLinphoneFactoryConfigFile, context
        )
        val transports = LinphoneManager.lc?.transports
        transports?.udpPort = 0
        transports?.tcpPort = 5080
        transports?.tlsPort = 5060
        lc?.transports = transports!!
        lc?.start()
        lc?.enableKeepAlive(true)
        initLibLinphone(context)
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    if (LinphoneManager.lc != null) {
                        LinphoneManager.lc?.iterate()
                    }
                }
            }
        }
        mTimer = Timer("Linphone Scheduler")
        mTimer!!.schedule(task, 0, 20)
    }


    @Throws(IOException::class)
    private fun copyAssetsFromPackage(context: Context) {
        LinphoneUtils.copyIfNotExist(
            context, com.younes.callhelpersdk.R.raw.linphonerc_default,
            LinphoneManager.mLinphoneConfigFile
        )
        LinphoneUtils.copyIfNotExist(
            context,
            com.younes.callhelpersdk.R.raw.linphonerc_factory,
            File(mLinphoneFactoryConfigFile).name
        )
        LinphoneUtils.copyIfNotExist(
            context, com.younes.callhelpersdk.R.raw.lpconfig,
            mLPConfigXsd
        )
    }

    @Synchronized
    private fun initLibLinphone(context: Context) {
        lc!!.setUserAgent("TelScale Restcomm Android Client ", "")
        lc!!.remoteRingbackTone = mRingSoundFile
        lc!!.ring = mRingSoundFile
        lc!!.playFile = mPauseSoundFile
        //        mLc.setCallErrorTone(Reason.NotFound, mErrorToneFile);
        val availableCores = Runtime.getRuntime().availableProcessors()
        Log.w(
            TAG,
            "MediaStreamer : $availableCores cores detected and configured"
        )
        //        mLc.setCpuCount(availableCores);
        lc!!.isNetworkReachable = true
        lc!!.enableEchoCancellation(true)
        lc!!.enableAdaptiveRateControl(true)
        LinphoneUtils.getConfig(context).setInt("audio", "codec_bitrate_limit", 36)
        lc!!.uploadBandwidth = 1536
        lc!!.downloadBandwidth = 1536
        setCodecMime()
    }

    private fun setCodecMime() {
        val ptList = LinphoneManager.lc!!.audioPayloadTypes
        for (pt in ptList) {
            Log.d("payloadaudio", pt.mimeType)
            if ( /*pt.getMimeType().equalsIgnoreCase(codecName) ||*/pt.mimeType.equals(
                    "pcmu",
                    ignoreCase = true
                )
            ) {
                pt.enable(true)
            } else {
                pt.enable(false)
            }
        }
        lc!!.audioPayloadTypes = ptList
    }

    fun destroy() {
        try {
            mTimer!!.cancel()
            lc!!.stop()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            lc = null
        }
    }

    fun getLc():Core = lc!!

    fun getConfigFile() = mLinphoneConfigFile
}