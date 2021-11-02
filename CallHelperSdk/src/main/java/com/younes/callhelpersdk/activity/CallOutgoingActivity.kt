package com.younes.callhelpersdk.activity

import android.Manifest
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.younes.callhelpersdk.LinphoneImpl
import com.younes.callhelpersdk.R
import com.younes.callhelpersdk.callback.PhoneCallback
import org.linphone.core.Call
import org.linphone.core.CoreListenerStub
import org.linphone.mediastream.Log


class CallOutgoingActivity : AppCompatActivity(), View.OnClickListener,
    PhoneCallback {
    private var name: TextView? = null
    private var number: TextView? = null
    private var contactPicture: ImageView? = null
    private var micro: ImageView? = null
    private var speaker: ImageView? = null
    private var hangUp: ImageView? = null
    private var mCall: Call? = null
    private val mListener: CoreListenerStub? = null
    private var isMicMuted = false
    private var isSpeakerEnabled = false
    var usercalling: String? = ""
    private var mAudioManager: AudioManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.call_outgoing)
        Log.d(TAG, "oncreate")
        LinphoneImpl.addPhoneCallBack(this)
        if (intent.extras != null) usercalling = intent.extras!!.getString("usercalling")
        name = findViewById<View>(R.id.contact_name) as TextView
        number = findViewById<View>(R.id.contact_number) as TextView
        contactPicture = findViewById<View>(R.id.contact_picture) as ImageView
        isMicMuted = false
        isSpeakerEnabled = false
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        micro = findViewById<View>(R.id.micro) as ImageView
        micro!!.setOnClickListener(this)
        speaker = findViewById<View>(R.id.speaker) as ImageView
        speaker!!.setOnClickListener(this)

        // set this flag so this activity will stay in front of the keyguard
//		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
//		getWindow().addFlags(flags);
        hangUp = findViewById<View>(R.id.outgoing_hang_up) as ImageView
        hangUp!!.setOnClickListener(this)
        number!!.text = usercalling

//		LinphoneImpl.addCallback(null, new PhoneCallback() {
//			@Override
//			public void incomingCall(Call linphoneCall) {
//				super.incomingCall(linphoneCall);
//				Log.d(TAG,"incomingCall");
//			}
//
//			@Override
//			public void outgoingInit() {
//				super.outgoingInit();
//				Log.d(TAG,"incomingCall");
//			}
//
//			@Override
//			public void callConnected() {
//				super.callConnected();
//				Log.d(TAG,"callConnected");
//			}
//
//			@Override
//			public void callEnd() {
//				super.callEnd();
//				Log.d(TAG,"callEnd");
//				finish();
//			}
//
//			@Override
//			public void callReleased() {
//				super.callReleased();
//				Log.d(TAG,"callReleased");
//				finish();
//			}
//
//			@Override
//			public void error() {
//				super.error();
//				Log.d(TAG,"error");
//				finish();
//			}
//		});

        //TODO:REPLACE
//		mListener = new CoreListenerStub(){
//			@Override
//			public void onCallStateChanged(Core lc, Call call, State state, String message) {
//				if (call == mCall && State.Connected == state) {
//					/*if (!LinphoneActivity.isInstanciated()) {
//						return;
//					}*/
//					LinphoneManager.getInstance().startIncallActivity(mCall);
//					finish();
//					return;
//				} else if (state == State.Error) {
//					// Convert Core message for internalization
//					if (call.getErrorInfo().getReason() == Reason.Declined) {
//						displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
//						decline();
//					} else if (call.getErrorInfo().getReason() == Reason.NotFound) {
//						displayCustomToast(getString(R.string.error_user_not_found), Toast.LENGTH_SHORT);
//						decline();
//					} else if (call.getErrorInfo().getReason() == Reason.NotAcceptable) {
//						displayCustomToast(getString(R.string.error_incompatible_media), Toast.LENGTH_SHORT);
//						decline();
//					} else if (call.getErrorInfo().getReason() == Reason.Busy) {
//						displayCustomToast(getString(R.string.error_user_busy), Toast.LENGTH_SHORT);
//						decline();
//					} else if (message != null) {
//						displayCustomToast(getString(R.string.error_unknown) + " - " + message, Toast.LENGTH_SHORT);
//						decline();
//					}
//				}else if (state == State.End) {
//					// Convert Core message for internalization
//					if (call.getErrorInfo().getReason() == Reason.Declined) {
//						displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
//						decline();
//					}
//				}
//
//				if (LinphoneManager.getLc().getCallsNb() == 0) {
//					finish();
//					return;
//				}
//			}
//		};
        instance = this
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        instance = this
        mCall = null

        // Only one call ringing at a time is allowed
//		if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
//			List<Call> calls = LinphoneUtils.getCalls(LinphoneManager.getLc());
//			for (Call call : calls) {
//				State cstate = call.getState();
//				if (State.OutgoingInit == cstate || State.OutgoingProgress == cstate
//						|| State.OutgoingRinging == cstate || State.OutgoingEarlyMedia == cstate) {
//					mCall = call;
//					break;
//				}
//				if (State.StreamsRunning == cstate) {
//					/*if (!LinphoneActivity.isInstanciated()) {
//						return;
//					}*/
//					LinphoneManager.getInstance().startIncallActivity(mCall);
//					finish();
//					return;
//				}
//			}
//		}
//		if (mCall == null) {
//			Log.e("Couldn't find outgoing call");
////			finish();
//			return;
//		}


        //TODO: FIND BEST SOLUTION
//		Address address = mCall.getRemoteAddress();
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
        Log.d(TAG, "onStart")
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        //		Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
//		if (lc != null) {
//			lc.removeListener(mListener);
//		}
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "onDestroy")
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.micro) {
            isMicMuted = !isMicMuted
            if (isMicMuted) {
                micro!!.setImageResource(R.drawable.micro_selected)
            } else {
                micro!!.setImageResource(R.drawable.micro_default)
            }
            LinphoneImpl.toggleMicro(isMicMuted)
            //			LinphoneManager.getLc().enableMic(!isMicMuted);
        }
        if (id == R.id.speaker) {
            isSpeakerEnabled = !isSpeakerEnabled
            if (isSpeakerEnabled) {
                speaker!!.setImageResource(R.drawable.speaker_selected)
            } else {
                speaker!!.setImageResource(R.drawable.speaker_default)
            }
            //TODO: SPEAKER
            mAudioManager!!.isSpeakerphoneOn = isSpeakerEnabled
            //			LinphoneManager.getInstance().enableSpeaker(isSpeakerEnabled);
        }
        if (id == R.id.outgoing_hang_up) {
            decline()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
//			LinphoneCallHelper.hangUp();
//			finish();
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun decline() {
        LinphoneImpl.hangUp()
        finish()
    }

    private fun checkAndRequestCallPermissions() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {}
                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    decline()
                    finish()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

  override  fun incomingCall(linphoneCall: Call?) {}
    override    fun outgoingInit() {}
    override  fun callConnected() {}
    override  fun callEnd() {
        finish()
    }

    override  fun callReleased() {
        finish()
    }

    override fun error() {
        finish()
    }

    companion object {
        private var instance: CallOutgoingActivity? = null
        private const val TAG = "OUTGOINGACTIVITY"
        fun instance(): CallOutgoingActivity? {
            return instance
        }

        val isInstanciated: Boolean
            get() = instance != null
    }
}
