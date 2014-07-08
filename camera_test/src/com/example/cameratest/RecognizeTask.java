package com.example.cameratest;


import java.util.Locale;

import org.opencv.core.Mat;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class RecognizeTask extends AsyncTask<Mat,Void,Void> {


	static{		
    	System.loadLibrary("camera_test");
	}
	
	public native int recognitionjni(long frame);
	@Override
	protected Void doInBackground(Mat... params) {
		Mat frame = params[0];
				
		String s = "The origin of species";
		
		int n = recognitionjni(frame.getNativeObjAddr());
		Log.i("JNI",Integer.toString(n));
		
		if(n>4){
			
			MainActivity.tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
		}
		
		return null;
	}

}
