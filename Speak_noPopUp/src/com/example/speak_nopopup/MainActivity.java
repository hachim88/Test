//Hachim Jehouani

package com.example.speak_nopopup;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements RecognitionListener,
OnTouchListener, AnimationListener {

	private SpeechRecognizer 	speech = null;
	private Intent 				recognizerIntent;
	private ImageView			img;
	private ToggleButton		toggle;
	private	int					height,width;
	private Animation			anim;
	private CountDownTimer		count;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getActionBar().hide();

		findViewById(R.id.layout).setOnTouchListener(this);


		img = new ImageView(this);
		img.setVisibility(View.GONE);
		img.setImageResource(R.drawable.ic_launcher);
		((RelativeLayout)findViewById(R.id.layout)).addView(img);
		toggle = (ToggleButton) findViewById(R.id.toggle);

		Display display = getWindowManager().getDefaultDisplay(); 
		width = display.getWidth();
		height = display.getHeight();

		if(findRecognizerService()){
			speech = SpeechRecognizer.createSpeechRecognizer(this);
			speech.setRecognitionListener(this);
			recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"fr");
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,10);
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,20000);
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,5000);

			ViewTreeObserver vto = toggle.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){

				@Override
				public void onGlobalLayout() {
					int[] location = getViewPos(toggle);


					anim = new TranslateAnimation(width/2,location[0],height,location[1]);
					anim.setDuration(2000);
					anim.setRepeatMode(Animation.RESTART);
					anim.setRepeatCount(Animation.INFINITE);
					anim.setAnimationListener(MainActivity.this);
					
					toggle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}


			});
		}


		count = new CountDownTimer(5000,1000) {

			@Override
			public void onTick(long millisUntilFinished) {

				Log.i("timer","timer: "+millisUntilFinished);
			}

			@Override
			public void onFinish() {
				img.setVisibility(View.VISIBLE);
				img.startAnimation(anim);
				Log.v("timer","start animation");
			}
		};
	}




	private int[] getViewPos(View v){

		int[] globalPos = new  int[2]; 
		findViewById(R.id.layout).getLocationOnScreen(globalPos); 
		int  x = globalPos[0]; 
		int  y = globalPos[1];

		v.getLocationOnScreen(globalPos); 
		globalPos[0] -= x; 
		globalPos[1] -= y;

		return globalPos;
	}


	@Override
	protected void onStart() {
		super.onStart();

		try {
			count.start();
		} catch (Exception e) {}

	}


	public void enregister(View v){

		if(((ToggleButton)v).isChecked())
			speech.startListening(recognizerIntent);
		else
			speech.stopListening();
	}

	//find recognition service
	public boolean findRecognizerService(){

		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		if(activities.size() == 0){
			Toast.makeText(this,"pas possible mode offline!!", Toast.LENGTH_LONG).show();
			return false;
		}
		else
			return true;
	}


	@Override
	protected void onPause() {
		super.onPause();
		if (speech != null) {
			speech.destroy();
		}
	}


	@Override
	public void onReadyForSpeech(Bundle params) {
	}


	@Override
	public void onBeginningOfSpeech() {

		Log.i("info","debut enregistrement");
	}


	@Override
	public void onRmsChanged(float rmsdB) {	
	}


	@Override
	public void onBufferReceived(byte[] buffer) {	
	}


	@Override
	public void onEndOfSpeech() {

		Log.i("info","fin d'enregistrement");
		toggle.setChecked(false);
	}


	@Override
	public void onError(int error) {
		Toast.makeText(this,getErrorText(error),Toast.LENGTH_LONG).show();
	}


	@Override
	public void onResults(Bundle results) {

		ArrayList<String> matches = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		String text = "";
		for (String result : matches)
			text += result + "\n";

		((TextView)findViewById(R.id.txt)).setText(text);
	}


	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub

	}


	public static String getErrorText(int errorCode) {
		String message;
		switch (errorCode) {
		case SpeechRecognizer.ERROR_AUDIO:
			message = "Audio recording error";
			break;
		case SpeechRecognizer.ERROR_CLIENT:
			message = "Client side error";
			break;
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			message = "Insufficient permissions";
			break;
		case SpeechRecognizer.ERROR_NETWORK:
			message = "Network error";
			break;
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			message = "Network timeout";
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			message = "No match";
			break;
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			message = "RecognitionService busy";
			break;
		case SpeechRecognizer.ERROR_SERVER:
			message = "error from server";
			break;
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			message = "No speech input";
			break;
		default:
			message = "Didn't understand, please try again.";
			break;
		}
		return message;
	}


	@Override
	protected void onStop() {

		try {
			img.clearAnimation();
			img.setVisibility(View.GONE);
			count.cancel();
		} catch (Exception e) {}

		super.onStop();
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {

		try {
			img.clearAnimation();
			img.setVisibility(View.GONE);
			count.cancel();
		} catch (Exception e) {}


		try {
			count.start();
		} catch (Exception e) {}

		return false;
	}


	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onAnimationEnd(Animation animation) {
		img.setVisibility(View.GONE);
	}


	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}
}
