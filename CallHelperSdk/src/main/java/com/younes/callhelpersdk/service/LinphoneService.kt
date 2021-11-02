package com.younes.callhelpersdk.service

import android.app.*
import android.content.Intent
import android.media.*
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.younes.callhelpersdk.R
import com.younes.callhelpersdk.activity.CallIncomingActivity
import com.younes.callhelpersdk.activity.CallOutgoingActivity
import com.younes.callhelpersdk.callback.PhoneCallback
import com.younes.callhelpersdk.callback.RegistrationCallback
import com.younes.callhelpersdk.linphone_management.LinphoneManager

import org.linphone.core.*
import org.linphone.core.Core.LogCollectionUploadState
import org.linphone.mediastream.video.capture.hwconf.Hacks


class LinphoneService : Service(), CoreListener {
    private var instance: LinphoneService? = null

    private var ringingCall: Call? = null
    private var mAudioManager: AudioManager? = null
    private var mAudioFocused = false
    private var mRingerPlayer: MediaPlayer? = null
    private var mVibrator: Vibrator? = null
    var callingUSer = ""
    var isCallRunning = false
    var isIncomingCall = true
    private var notificationManager: NotificationManager? = null
    var notif: NotificationCompat.Builder? = null
    var notification: Notification? = null
    override fun onCreate() {
        super.onCreate()

        instance = this
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setupChannels()
        notif = NotificationCompat.Builder(this, "channeladmin")
        notif!!.setContentTitle("Linphone Service")
        notif!!.setContentText("")
        notif!!.setSmallIcon(R.mipmap.ic_launcher)
        notif!!.setCategory(Notification.CATEGORY_SERVICE)
        notification = notif!!.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, notification)
        }
        notificationManager!!.notify(1, notification)
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        mVibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels() {
        val adminChannelName: CharSequence = "notifications_admin_channel_name"
        val adminChannelDescription = "notifications_admin_channel_description"
        val adminChannel = NotificationChannel(
            "channeladmin",
            adminChannelName,
            NotificationManager.IMPORTANCE_LOW
        )
        adminChannel.description = adminChannelDescription
        adminChannel.setSound(null, null)
        notificationManager!!.createNotificationChannel(adminChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "LinphoneService onDestroy execute")
        removeAllCallback()
        LinphoneManager.lc?.stop()
        LinphoneManager.destroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun removeAllCallback() {
        removePhoneCallback()
        removeRegistrationCallback()
    }

    private fun callThroughMobile(channel: String) {
//        mChannel = channel;
//        if (LinphoneManager.getLc().isIncall()) {
//            LinphoneUtils.getInstance().hangUp();
//            MediaUtils.stop();
//        }
//        SPUtils.save(this, "channel", mChannel);
//        callNowChannel();
    }

    private fun callNowChannel() {
//        if (!LinphoneManager.getLc().isIncall()) {
//            if (!mChannel.equals("")) {
//                PhoneBean phone = new PhoneBean();
//                phone.setUserName(mChannel);
//                phone.setHost("115.159.84.73");
//                LinphoneUtils.getInstance().startSingleCallingTo(phone);
//            }
//        }
    }

    override fun onTransferStateChanged(core: Core, transfered: Call, callState: Call.State) {
        Log.e(TAG, "onTransferStateChanged: " + callState.name)
    }

    override fun onFriendListCreated(core: Core, friendList: FriendList) {}
    override fun onSubscriptionStateChanged(
        core: Core,
        linphoneEvent: Event,
        state: SubscriptionState
    ) {
    }

    override fun onCallLogUpdated(core: Core, callLog: CallLog) {
        Log.e(TAG, "onCallLogUpdated: " + "callLog : " + callLog.errorInfo.reason.name)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCallStateChanged(core: Core, call: Call, state: Call.State, message: String) {
        Log.e(TAG, "callState: $state  $message")
        if (state == Call.State.IncomingReceived) {
            if (sPhoneCallback != null) sPhoneCallback?.incomingCall(call)
            Log.d("testAddress", call.remoteAddress.username)
            if (core.callsNb == 1) {
                requestAudioFocus(AudioManager.STREAM_RING)
                ringingCall = call
                startRinging()
                val intent = Intent(this@LinphoneService, CallIncomingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(
                    "usercalling",
                    call.remoteAddress.username + "@" + call.remoteAddress.domain
                )
                intent.putExtra("isCallRunning", false)
                this.startActivity(intent)
                // otherwise there is the beep
            }
            //            startActivity(new Intent(getApplicationContext(), CallIncomingActivity.class));
        } else if (call === ringingCall && isRinging) {
            //previous state was ringing, so stop ringing
            stopRinging()
        }
        if (state == Call.State.OutgoingInit) {
            try {
                Log.d(TAG, "outgoing  " + call.toAddress.username)
            } catch (e: Exception) {
            }
            if (sPhoneCallback != null) sPhoneCallback?.outgoingInit()
            setAudioManagerInCallMode()
            requestAudioFocus(AudioManager.STREAM_VOICE_CALL)
            startBluetooth()
            val intent = Intent(this@LinphoneService, CallOutgoingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(
                "usercalling",
                call.remoteAddress.username + "@" + call.remoteAddress.domain
            )
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            notif!!.setFullScreenIntent(pendingIntent, true)
            notification = notif!!.build()
            notificationManager!!.notify(1, notification)
            setupChannels()
            startForeground(1, notification)
        }
        if (state == Call.State.Connected) {
            if (sPhoneCallback != null) sPhoneCallback?.callConnected()
            val intent: Intent
            if (call.dir == Call.Dir.Incoming) {
                setAudioManagerInCallMode()
                //mAudioManager.abandonAudioFocus(null);
                requestAudioFocus(AudioManager.STREAM_VOICE_CALL)
                isCallRunning = true
                isIncomingCall = true
                callingUSer = call.remoteAddress.username + "@" + call.remoteAddress.domain
                intent = Intent(this@LinphoneService, CallIncomingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("usercalling", callingUSer)
                intent.putExtra("isCallRunning", true)
                val pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                notif!!.setFullScreenIntent(pendingIntent, true)
            } else if (call.dir == Call.Dir.Outgoing) {
                intent = Intent(this@LinphoneService, CallOutgoingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(
                    "usercalling",
                    call.remoteAddress.username + "@" + call.remoteAddress.domain
                )
                val pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                notif!!.setFullScreenIntent(pendingIntent, true)
                isCallRunning = true
                isIncomingCall = false
                callingUSer = call.remoteAddress.username + "@" + call.remoteAddress.domain
            }

            //It is for incoming calls, because outgoing calls enter MODE_IN_COMMUNICATION immediately when they start.
            //However, incoming call first use the MODE_RINGING to play the local ring.
//                    enableSpeaker(true);
            notification = notif!!.build()
            notificationManager!!.notify(1, notification)
            setupChannels()
            startForeground(1, notification)
        }
        if (state == Call.State.Error) {
            clearPendingIntent()
            isCallRunning = false
            if (sPhoneCallback != null) sPhoneCallback?.error()
            var res = 0
            if (mAudioFocused) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (focusRequest != null) res = mAudioManager!!.abandonAudioFocusRequest(
                        focusRequest!!
                    )
                } else {
                    res = mAudioManager!!.abandonAudioFocus(null)
                }

                /* int res = mAudioManager.abandonAudioFocus(null);*/org.linphone.mediastream.Log.d(
                    "Audio focus released a bit later: " + if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied"
                )
                mAudioFocused = false
            }
            val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (tm.callState == TelephonyManager.CALL_STATE_IDLE) {
                org.linphone.mediastream.Log.d("---AudioManager: back to MODE_NORMAL")
                mAudioManager!!.mode = AudioManager.MODE_NORMAL
                org.linphone.mediastream.Log.d("All call terminated, routing back to earpiece")
                routeAudioToReceiver()
            }
        }
        if (state == Call.State.End) {
            clearPendingIntent()
            isCallRunning = false
            if (sPhoneCallback != null) sPhoneCallback?.callEnd()
            var res = 0
            if (mAudioFocused) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (focusRequest != null) res = mAudioManager!!.abandonAudioFocusRequest(
                        focusRequest!!
                    )
                } else {
                    res = mAudioManager!!.abandonAudioFocus(null)
                }

                /* int res = mAudioManager.abandonAudioFocus(null);*/org.linphone.mediastream.Log.d(
                    "Audio focus released a bit later: " + if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied"
                )
                mAudioFocused = false
            }
            val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (tm.callState == TelephonyManager.CALL_STATE_IDLE) {
                org.linphone.mediastream.Log.d("---AudioManager: back to MODE_NORMAL")
                mAudioManager!!.mode = AudioManager.MODE_NORMAL
                org.linphone.mediastream.Log.d("All call terminated, routing back to earpiece")
                routeAudioToReceiver()
            }
        }
        if (state == Call.State.Released) {
            clearPendingIntent()
            isCallRunning = false
            if (sPhoneCallback != null) sPhoneCallback?.callReleased()
        }
        if (state == Call.State.StreamsRunning) {
            isCallRunning = true
            startBluetooth()
            requestAudioFocus(AudioManager.STREAM_VOICE_CALL)
            setAudioManagerInCallMode()
            //            enableSpeaker(true);
        }
    }

    private fun clearPendingIntent() {
        stopForeground(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notif = NotificationCompat.Builder(this, "channeladmin")
            notif!!.setContentTitle("Linphone Service")
            notif!!.setContentText("")
            notif!!.setSmallIcon(R.mipmap.ic_launcher)
            notif!!.setCategory(Notification.CATEGORY_SERVICE)
            notification = notif!!.build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupChannels()
                startForeground(2, notification)
            }
            notificationManager!!.notify(2, notification)
        } else {
            notificationManager!!.cancelAll()
        }
    }

    override fun onAuthenticationRequested(core: Core, authInfo: AuthInfo, method: AuthMethod) {
        Log.e(
            TAG,
            "onAuthenticationRequested: " + authInfo.realm.toString() + "  " + "method :  " + method.name
        )
    }

    override fun onNotifyPresenceReceivedForUriOrTel(
        core: Core,
        linphoneFriend: Friend,
        uriOrTel: String,
        presenceModel: PresenceModel
    ) {
    }

    override fun onChatRoomStateChanged(core: Core, chatRoom: ChatRoom, state: ChatRoom.State) {}
    override fun onBuddyInfoUpdated(core: Core, linphoneFriend: Friend) {}
    override fun onNetworkReachable(core: Core, reachable: Boolean) {}
    override fun onNotifyReceived(
        core: Core,
        linphoneEvent: Event,
        notifiedEvent: String,
        body: Content
    ) {
    }

    override fun onNewSubscriptionRequested(core: Core, linphoneFriend: Friend, url: String) {}
    override fun onCallStatsUpdated(core: Core, call: Call, callStats: CallStats) {}
    override fun onNotifyPresenceReceived(core: Core, linphoneFriend: Friend) {}
    override fun onEcCalibrationAudioInit(core: Core) {}
    override fun onMessageReceived(core: Core, chatRoom: ChatRoom, message: ChatMessage) {}
    override fun onEcCalibrationResult(core: Core, status: EcCalibratorStatus, delayMs: Int) {}
    override fun onSubscribeReceived(
        core: Core,
        linphoneEvent: Event,
        subscribeEvent: String,
        body: Content
    ) {
    }

    override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {}
    override fun onChatRoomRead(core: Core, chatRoom: ChatRoom) {}
    override fun onRegistrationStateChanged(
        core: Core,
        proxyConfig: ProxyConfig,
        state: RegistrationState,
        message: String
    ) {
        Log.d(TAG, "proxyconfig  " + proxyConfig.contact.username)
        Log.d(TAG, "registrationState: " + state.name)
        if (sRegistrationCallback != null) {
            if (state == RegistrationState.None) {
                sRegistrationCallback?.registrationNone()
            } else if (state == RegistrationState.Progress) {
                sRegistrationCallback?.registrationProgress()
            } else if (state == RegistrationState.Ok) {
                sRegistrationCallback?.registrationOk()
            } else if (state == RegistrationState.Cleared) {
                sRegistrationCallback?.registrationCleared()
            } else if (state == RegistrationState.Failed) {
                sRegistrationCallback?.registrationFailed()
            }
        }
    }

    override fun onFriendListRemoved(core: Core, friendList: FriendList) {}
    override fun onReferReceived(core: Core, referTo: String) {}
    override fun onQrcodeFound(core: Core, result: String?) {}
    override fun onConfiguringStatus(core: Core, status: ConfiguringState, message: String?) {}
    override fun onCallCreated(core: Core, call: Call) {}
    override fun onPublishStateChanged(core: Core, linphoneEvent: Event, state: PublishState) {}
    override fun onCallEncryptionChanged(
        core: Core,
        call: Call,
        mediaEncryptionEnabled: Boolean,
        authenticationToken: String?
    ) {
    }

    override fun onIsComposingReceived(core: Core, chatRoom: ChatRoom) {}
    override fun onMessageReceivedUnableDecrypt(
        core: Core,
        chatRoom: ChatRoom,
        message: ChatMessage
    ) {
    }

    override fun onLogCollectionUploadProgressIndication(core: Core, offset: Int, total: Int) {}
    override fun onChatRoomSubjectChanged(lc: Core, cr: ChatRoom) {}

    //    @Override
    //    public void onChatRoomSubjectChanged(Core lc, ChatRoom cr) {
    //
    //    }
    override fun onVersionUpdateCheckResultReceived(
        core: Core,
        result: VersionUpdateCheckResult,
        version: String,
        url: String?
    ) {
    }

    override fun onEcCalibrationAudioUninit(core: Core) {}
    override fun onGlobalStateChanged(core: Core, state: GlobalState, message: String) {}
    override fun onLogCollectionUploadStateChanged(
        core: Core,
        state: LogCollectionUploadState,
        info: String
    ) {
    }

    override fun onDtmfReceived(core: Core, call: Call, dtmf: Int) {}
    override fun onChatRoomEphemeralMessageDeleted(lc: Core, cr: ChatRoom) {}

    //    @Override
    //    public void onChatRoomEphemeralMessageDeleted(Core lc, ChatRoom cr) {
    //
    //    }
    override fun onMessageSent(core: Core, chatRoom: ChatRoom, message: ChatMessage) {}

    //    public void startEcCalibration() throws CoreException {
    //        routeAudioToSpeaker();
    //        setAudioManagerInCallMode();
    //        org.linphone.mediastream.Log.i("Set audio mode on 'Voice Communication'");
    //        requestAudioFocus(STREAM_VOICE_CALL);
    //        int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
    //        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
    //        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
    //        mLc.startEchoCancellerCalibration();
    //        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
    //    }
    //    public int startEchoTester() throws CoreException {
    //        routeAudioToSpeaker();
    //        setAudioManagerInCallMode();
    //        org.linphone.mediastream.Log.i("Set audio mode on 'Voice Communication'");
    //        requestAudioFocus(STREAM_VOICE_CALL);
    //        int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
    //        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
    //        int sampleRate = 44100;
    //        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    //            String sampleRateProperty = mAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
    //            sampleRate = Integer.parseInt(sampleRateProperty);
    //        }
    //        /*int status = */
    //        mLc.startEchoTester(sampleRate);
    //        /*if (status > 0)*/
    ////        echoTesterIsRunning = true;
    //		/*else {
    //			echoTesterIsRunning = false;
    //			routeAudioToReceiver();
    //			mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
    //			((AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
    //			Log.i("Set audio mode on 'Normal'");
    //		}*/
    //        return 1;
    //        //return status;
    //    }
    //    public int stopEchoTester() throws CoreException {
    //        echoTesterIsRunning = false;
    //        /*int status = */
    //        mLc.stopEchoTester();
    //        routeAudioToReceiver();
    //        ((AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
    //        org.linphone.mediastream.Log.i("Set audio mode on 'Normal'");
    //        return 1;//status;
    //    }
    //    public boolean getEchoTesterStatus() {
    //        return echoTesterIsRunning;
    //    }
    private var isRinging = false
    private fun requestAudioFocus(stream: Int) {
        if (!mAudioFocused) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setLegacyStreamType(stream)
                    .build()
                focusRequest =
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener { }
                        .build()
                val res = mAudioManager!!.requestAudioFocus(focusRequest!!)
                org.linphone.mediastream.Log.d("Audio focus requested: " + if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied")
                if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true
            } else {
                val res = mAudioManager!!.requestAudioFocus(
                    null,
                    stream,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
                )
                org.linphone.mediastream.Log.d("Audio focus requested: " + if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied")
                if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true
            }
        }
    }

    var focusRequest: AudioFocusRequest? = null

    //    public void enableDeviceRingtone(boolean use) {
    //        if (use) {
    //            mLc.setRing(null);
    //        } else {
    //            mLc.setRing(mRingSoundFile);
    //        }
    //    }
    @Synchronized
    private fun startRinging() {
        routeAudioToSpeaker()
        mAudioManager!!.mode = AudioManager.MODE_RINGTONE
        try {
            if ((mAudioManager!!.ringerMode == AudioManager.RINGER_MODE_VIBRATE || mAudioManager!!.ringerMode == AudioManager.RINGER_MODE_NORMAL) && mVibrator != null) {
                val patern = longArrayOf(0, 1000, 1000)
                //                mVibrator.vibrate(patern, 1);
            }
            if (mRingerPlayer == null) {
                requestAudioFocus(AudioManager.STREAM_RING)
                mRingerPlayer = MediaPlayer()
                mRingerPlayer!!.setAudioStreamType(AudioManager.STREAM_RING)
                val ringuri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mRingerPlayer!!.setDataSource(this, ringuri)
                mRingerPlayer!!.prepare()
                mRingerPlayer!!.isLooping = true
                mRingerPlayer!!.start()
            } else {
                org.linphone.mediastream.Log.w("already ringing")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            org.linphone.mediastream.Log.e(e, "cannot handle incoming call")
        }
        isRinging = true
    }

    @Synchronized
    private fun stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer!!.stop()
            mRingerPlayer!!.release()
            mRingerPlayer = null
        }
        if (mVibrator != null) {
//            mVibrator.cancel();
        }
        if (Hacks.needGalaxySAudioHack()) mAudioManager!!.mode = AudioManager.MODE_NORMAL
        isRinging = false


        //TODO : TO BE APPLIED LATER

        // You may need to call galaxys audio hack after this method
//        if (!BluetoothManager.getInstance().isBluetoothHeadsetAvailable()) {
//            if (mServiceContext.getResources().getBoolean(R.bool.isTablet)) {
//                org.linphone.mediastream.Log.d("Stopped ringing, routing back to speaker");
//                routeAudioToSpeaker();
//            } else {
//                org.linphone.mediastream.Log.d("Stopped ringing, routing back to earpiece");
//                routeAudioToReceiver();
//            }
//        }
    }

    //TODO: TO BE APPLIED LATER
    fun startBluetooth() {
//        if (BluetoothManager.getInstance().isBluetoothHeadsetAvailable()) {
//            BluetoothManager.getInstance().routeAudioToBluetooth();
//        }
    }

    fun setAudioManagerInCallMode() {
        if (mAudioManager!!.mode == AudioManager.MODE_IN_COMMUNICATION) {
            org.linphone.mediastream.Log.w("[AudioManager] already in MODE_IN_COMMUNICATION, skipping...")
            return
        }
        org.linphone.mediastream.Log.d("[AudioManager] Mode: MODE_IN_COMMUNICATION")
        mAudioManager!!.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    private fun routeAudioToSpeakerHelper(speakerOn: Boolean) {
        org.linphone.mediastream.Log.w("Routing audio to " + (if (speakerOn) "speaker" else "earpiece") + ", disabling bluetooth audio route")
        enableSpeaker(speakerOn)
    }

    fun enableSpeaker(enable: Boolean) {
        mAudioManager!!.isSpeakerphoneOn = enable
    }

    fun routeAudioToSpeaker() {
        routeAudioToSpeakerHelper(true)
    }

    fun routeAudioToReceiver() {
        routeAudioToSpeakerHelper(false)
    }

    companion object {
        private var sPhoneCallback: PhoneCallback? = null
        private var sRegistrationCallback: RegistrationCallback? = null
        private const val TAG = "LinphoneService"
         val isReady: Boolean = true

        fun addPhoneCallback(phoneCallback: PhoneCallback) {
            sPhoneCallback = phoneCallback
        }

        fun removePhoneCallback() {
            if (sPhoneCallback != null) {
                sPhoneCallback = null
            }
        }

        fun addRegistrationCallback(registrationCallback: RegistrationCallback) {
            sRegistrationCallback = registrationCallback
        }

        fun removeRegistrationCallback() {
            if (sRegistrationCallback != null) {
                sRegistrationCallback = null
            }
        }
    }
}