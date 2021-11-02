package com.younes.callhelpersdk.activity

import android.Manifest
import android.app.Activity
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.younes.callhelpersdk.LinphoneImpl
import com.younes.callhelpersdk.R
import com.younes.callhelpersdk.callback.PhoneCallback
import com.younes.callhelpersdk.linphone_management.LinphoneManager
import com.younes.callhelpersdk.linphone_management.LinphoneUtils
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.mediastream.Log


class CallIncomingActivity : Activity(), PhoneCallback {
    private var name: TextView? = null
    private var number: TextView? = null
    private var contactPicture: ImageView? = null
    private var accept: ImageView? = null
    private var decline: ImageView? = null
    private var arrow: ImageView? = null
    private var hang_up: ImageView? = null
    private var micro: ImageView? = null
    private var speaker: ImageView? = null
    private var mCall: Call? = null
    private val mListener: CoreListenerStub? = null
    private var acceptUnlock: LinearLayout? = null
    private var declineUnlock: LinearLayout? = null
    private var alreadyAcceptedOrDeniedCall = false
    private val begin = false
    private val answerX = 0f
    private val oldMove = 0f
    private val declineX = 0f
    private var menu: LinearLayout? = null
    private var callHangup: LinearLayout? = null
    private var isMicMuted = false
    private var isSpeakerEnabled = false
    private var isCallRunning = false
    var usercalling: String? = ""
    private var mAudioManager: AudioManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.call_incoming)
        LinphoneImpl.addPhoneCallBack(this)
        if (intent.extras != null) {
            usercalling = intent.extras!!.getString("usercalling")
            isCallRunning = intent.extras!!.getBoolean("isCallRunning")
        }
        name = findViewById<View>(R.id.contact_name) as TextView
        number = findViewById<View>(R.id.contact_number) as TextView
        contactPicture = findViewById<View>(R.id.contact_picture) as ImageView
        menu = findViewById<View>(R.id.menu) as LinearLayout
        callHangup = findViewById<View>(R.id.call_hangup) as LinearLayout
        hang_up = findViewById<View>(R.id.hang_up) as ImageView
        micro = findViewById<View>(R.id.micro) as ImageView
        speaker = findViewById<View>(R.id.speaker) as ImageView
        isMicMuted = false
        isSpeakerEnabled = false
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        // set this flag so this activity will stay in front of the keyguard
        val flags =
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        window.addFlags(flags)
        val screenWidth = resources.displayMetrics.widthPixels
        acceptUnlock = findViewById<View>(R.id.acceptUnlock) as LinearLayout
        declineUnlock = findViewById<View>(R.id.declineUnlock) as LinearLayout
        accept = findViewById<View>(R.id.accept) as ImageView
        lookupCurrentCall()
        decline = findViewById<View>(R.id.decline) as ImageView
        arrow = findViewById<View>(R.id.arrow_hangup) as ImageView
        accept!!.setOnClickListener {
            LinphoneUtils.lc?.enableMic(true)
            answer()
            menu!!.visibility = View.GONE
            callHangup!!.visibility = View.VISIBLE
            Log.d("testMic", LinphoneImpl.lC.micEnabled())
            //				decline.setVisibility(View.GONE);
            //				acceptUnlock.setVisibility(View.VISIBLE);
        }
        hang_up!!.setOnClickListener {
            hangUp()
            finish()
        }
        micro!!.setOnClickListener { v: View? ->
            isMicMuted = !isMicMuted
            if (isMicMuted) {
                micro!!.setImageResource(R.drawable.micro_selected)
            } else {
                micro!!.setImageResource(R.drawable.micro_default)
            }
            LinphoneImpl.toggleMicro(isMicMuted)
        }
        speaker!!.setOnClickListener { v: View? ->
            isSpeakerEnabled = !isSpeakerEnabled
            if (isSpeakerEnabled) {
                speaker!!.setImageResource(R.drawable.speaker_selected)
            } else {
                speaker!!.setImageResource(R.drawable.speaker_default)
            }
            //TODO: SPEAKER
            mAudioManager!!.isSpeakerphoneOn = isSpeakerEnabled
        }
        number!!.text = usercalling

//		accept.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View view, MotionEvent motionEvent) {
//				float curX;
//				switch (motionEvent.getAction()) {
//					case MotionEvent.ACTION_DOWN:
//						acceptUnlock.setVisibility(View.VISIBLE);
//						decline.setVisibility(View.GONE);
//						answerX = motionEvent.getX()+accept.getWidth()/2;
//						begin = true;
//						oldMove = 0;
//						break;
//					case MotionEvent.ACTION_MOVE:
//						curX = motionEvent.getX();
//						view.scrollBy((int) (answerX - curX), view.getScrollY());
//						oldMove -= answerX - curX;
//						answerX = curX;
//						if (oldMove < -25)
//							begin = false;
//						if (curX < arrow.getWidth() && !begin) {
//							answer();
//							return true;
//						}
//						break;
//					case MotionEvent.ACTION_UP:
//						view.scrollTo(0, view.getScrollY());
//						decline.setVisibility(View.VISIBLE);
//						acceptUnlock.setVisibility(View.GONE);
//						break;
//				}
//				return true;
//			}
//		});
//
//		decline.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View view, MotionEvent motionEvent) {
//				float curX;
//				switch (motionEvent.getAction()) {
//					case MotionEvent.ACTION_DOWN:
//						declineUnlock.setVisibility(View.VISIBLE);
//						accept.setVisibility(View.GONE);
//						declineX = motionEvent.getX();
//						break;
//					case MotionEvent.ACTION_MOVE:
//						curX = motionEvent.getX();
//						view.scrollBy((int) (declineX - curX), view.getScrollY());
//						declineX = curX;
//						if (curX > (screenWidth-arrow.getWidth()*4)) {
//							decline();
//							return true;
//						}
//						break;
//					case MotionEvent.ACTION_UP:
//						view.scrollTo(0, view.getScrollY());
//						accept.setVisibility(View.VISIBLE);
//						declineUnlock.setVisibility(View.GONE);
//						break;
//				}
//				return true;
//			}
//		});
        decline!!.setOnClickListener {
            decline()
            //				accept.setVisibility(View.GONE);
            //				acceptUnlock.setVisibility(View.VISIBLE);
        }
        if (isCallRunning) {
            menu!!.visibility = View.GONE
            callHangup!!.visibility = View.VISIBLE
        } else {
            menu!!.visibility = View.VISIBLE
            callHangup!!.visibility = View.GONE
        }
        instance = this
    }

    private fun hangUp() {
        LinphoneImpl.hangUp()
    }

    override fun onResume() {
        super.onResume()
        instance = this
        val lc: Core = LinphoneManager.lc!!
        if (lc != null) {
            lc.addListener(mListener)
        }
        alreadyAcceptedOrDeniedCall = false
        mCall = null

        // Only one call ringing at a time is allowed
        lookupCurrentCall()
        //		if (mCall == null) {
//			//The incoming call no longer exists.
//			Log.d("Couldn't find incoming call");
//			finish();
//			return;
//		}

        //TODO:FIND NAME
//		LinphoneContact contact = ContactsManager.getInstance().findContactFromAddress(address);
//		if (contact != null) {
//			LinphoneUtils.setImagePictureFromUri(this, contactPicture, contact.getPhotoUri(), contact.getThumbnailUri());
//			name.setText(contact.getFullName());
//		} else {
//			name.setText(LinphoneUtils.getAddressDisplayName(address));
//		}
//		number.setText(address.asStringUriOnly());
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestCallPermissions()
    }

    override fun onPause() {
        val lc: Core = LinphoneManager.lc!!
        if (lc != null) {
            lc.removeListener(mListener)
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //TODO: Terminate call
//		if (LinphoneManager.isInstanciated() && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
//			LinphoneManager.getLc().terminateCall(mCall);
//			finish();
//		}
        return super.onKeyDown(keyCode, event)
    }

    private fun lookupCurrentCall() {
        val currentCall: Call = LinphoneManager.lc!!.getCurrentCall()
        if (Call.State.IncomingReceived == currentCall.state) {
            mCall = currentCall
        }
        //		if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
//			List<Call> calls = LinphoneUtils.getCalls(LinphoneManager.getLc());
//			for (Call call : calls) {
//				if (State.IncomingReceived == call.getState()) {
//					mCall = call;
//					break;
//				}
//			}
//		}
    }

    private fun decline() {
        if (alreadyAcceptedOrDeniedCall) {
            return
        }
        alreadyAcceptedOrDeniedCall = true

//		LinphoneManager.getLc().terminateCall(mCall);
        LinphoneImpl.declineCall()
        finish()
    }

    private fun answer() {
        if (alreadyAcceptedOrDeniedCall) {
            return
        }
        alreadyAcceptedOrDeniedCall = true
        LinphoneImpl.acceptCall()

//		CallParams params = LinphoneManager.getLc().createCallParams(mCall);
//
//		boolean isLowBandwidthConnection = !LinphoneUtils.isHighBandwidthConnection(LinphoneService.instance().getApplicationContext());
//
//		if (params != null) {
//			params.enableLowBandwidth(isLowBandwidthConnection);
//		}else {
//			Log.e("Could not create call params for call");
//		}
//
//		if (params == null || !LinphoneManager.getInstance().acceptCallWithParams(mCall, params)) {
//			// the above method takes care of Samsung Galaxy S
//			Toast.makeText(this, R.string.couldnt_accept_call, Toast.LENGTH_LONG).show();
//		} else {
//			if (!LinphoneActivity.isInstanciated()) {
//				return;
//			}
//			LinphoneManager.getInstance().routeAudioToReceiver();
//			LinphoneManager.getInstance().startIncallActivity(mCall);
//		}
    }

    private fun checkAndRequestCallPermissions() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {}
                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    decline()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

   override fun incomingCall(linphoneCall: Call?) {}
    override  fun outgoingInit() {}
    override   fun callConnected() {}
    override   fun callEnd() {
        finish()
    }

    override   fun callReleased() {
        finish()
    }

    override   fun error() {
        finish()
    }

    companion object {
        private var instance: CallIncomingActivity? = null
        fun instance(): CallIncomingActivity? {
            return instance
        }

        val isInstanciated: Boolean
            get() = instance != null
    }
}