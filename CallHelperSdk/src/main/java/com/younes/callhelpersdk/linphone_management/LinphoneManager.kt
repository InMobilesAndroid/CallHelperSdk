package com.younes.callhelpersdk.linphone_management

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.linphone.core.*
import org.linphone.core.Core.LogCollectionUploadState
import java.io.File
import java.io.IOException
import java.util.*

object  LinphoneManager : CoreListener {

    private var TAG = "LinphoneManager"
    private var mTimer: Timer? = null
    private var mLPConfigXsd: String? = null
    private var mLinphoneFactoryConfigFile: String? = null
    var mLinphoneConfigFile: String? = null
    private var mLinphoneRootCaFile: String? = null
    private var mRingSoundFile: String? = null
    private var mRingBackSoundFile: String? = null
    private var mPauseSoundFile: String? = null

    var lc: Core? = null
    private var instance: LinphoneManager? = null
    private var sExited: Boolean = false

    private var linphoneCore:LinphoneCore?=null

    @Synchronized
     fun startLibLinphone(context: Context) {

        if(linphoneCore==null)
            linphoneCore = LinphoneCore(context)

        linphoneCore?.startLinphone()

    }


    fun getLcCore():Core=
        linphoneCore?.getLc()!!


    fun getConifFile():String{
        if (linphoneCore!=null)
        return linphoneCore?.getConfigFile()!!

        else return ""
    }
    private fun doDestroy() {

        linphoneCore?.destroy()
    }

    override fun onTransferStateChanged(lc: Core, transfered: Call, newCallState: Call.State) {}
    override fun onFriendListCreated(lc: Core, list: FriendList) {}
    override fun onSubscriptionStateChanged(lc: Core, lev: Event, state: SubscriptionState) {}
    override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {}
    override fun onCallStateChanged(lc: Core, call: Call, state: Call.State, message: String) {}
    override fun onAuthenticationRequested(lc: Core?, authInfo: AuthInfo?, method: AuthMethod?) {}
    override fun onNotifyPresenceReceivedForUriOrTel(
        lc: Core,
        lf: Friend,
        uriOrTel: String,
        presenceModel: PresenceModel
    ) {
    }

    override fun onChatRoomStateChanged(lc: Core?, cr: ChatRoom?, state: ChatRoom.State?) {}
    override fun onBuddyInfoUpdated(lc: Core, lf: Friend) {}
    override fun onNetworkReachable(lc: Core, reachable: Boolean) {}
    override fun onNotifyReceived(lc: Core?, lev: Event?, notifiedEvent: String?, body: Content?) {}
    override fun onNewSubscriptionRequested(lc: Core, lf: Friend, url: String) {}
    override fun onCallStatsUpdated(lc: Core?, call: Call?, stats: CallStats?) {}
    override fun onNotifyPresenceReceived(lc: Core, lf: Friend) {}
    override fun onEcCalibrationAudioInit(lc: Core) {}
    override fun onMessageReceived(lc: Core?, room: ChatRoom?, message: ChatMessage?) {}
    override fun onEcCalibrationResult(lc: Core?, status: EcCalibratorStatus?, delayMs: Int) {}
    override fun onSubscribeReceived(
        lc: Core?,
        lev: Event?,
        subscribeEvent: String?,
        body: Content?
    ) {
    }

    override fun onInfoReceived(lc: Core, call: Call, msg: InfoMessage) {}
    override fun onChatRoomRead(lc: Core?, room: ChatRoom?) {}
    override fun onRegistrationStateChanged(
        lc: Core,
        cfg: ProxyConfig,
        cstate: RegistrationState,
        message: String
    ) {
    }

    override fun onFriendListRemoved(lc: Core, list: FriendList) {}
    override fun onReferReceived(lc: Core, referTo: String) {}
    override fun onQrcodeFound(lc: Core, result: String) {}
    override fun onConfiguringStatus(lc: Core?, status: ConfiguringState?, message: String?) {}
    override fun onCallCreated(lc: Core, call: Call) {}
    override fun onPublishStateChanged(lc: Core, lev: Event, state: PublishState) {}
    override fun onCallEncryptionChanged(
        lc: Core,
        call: Call,
        on: Boolean,
        authenticationToken: String
    ) {
    }

    override fun onIsComposingReceived(lc: Core?, room: ChatRoom?) {}
    override fun onMessageReceivedUnableDecrypt(
        lc: Core?,
        room: ChatRoom?,
        message: ChatMessage?
    ) {
    }

    override fun onLogCollectionUploadProgressIndication(lc: Core, offset: Int, total: Int) {}
    override fun onChatRoomSubjectChanged(lc: Core?, cr: ChatRoom?) {}
    override fun onVersionUpdateCheckResultReceived(
        lc: Core,
        result: VersionUpdateCheckResult,
        version: String,
        url: String
    ) {
    }

    override fun onEcCalibrationAudioUninit(lc: Core) {}
    override fun onGlobalStateChanged(lc: Core, gstate: GlobalState, message: String) {}
    override fun onLogCollectionUploadStateChanged(
        lc: Core,
        state: LogCollectionUploadState,
        info: String
    ) {
    }

    override fun onDtmfReceived(lc: Core, call: Call, dtmf: Int) {}
    override fun onChatRoomEphemeralMessageDeleted(lc: Core?, cr: ChatRoom?) {}
    override fun onMessageSent(lc: Core?, room: ChatRoom?, message: ChatMessage?) {}

        @get:Synchronized
        val lcIfManagerNotDestroyOrNull: Core?
            get() {
                if (sExited || instance == null) {
                    Log.e(TAG,"Trying to get linphone core while LinphoneManager already destroyed or not created")
                    return null
                }
                return lc
            }
        val isInstanceiated: Boolean
            get() = instance != null

        @Synchronized
        fun getInstance(): LinphoneManager? {
            if (instance != null) {
                return instance
            }
            if (sExited) {
                throw RuntimeException(
                    "Linphone Manager was already destroyed. "
                            + "Better use getLcIfManagerNotDestroyed and check returned value"
                )
            }
            throw RuntimeException("Linphone Manager should be created before accessed")
        }

        fun destroy() {
           doDestroy()
        }
}