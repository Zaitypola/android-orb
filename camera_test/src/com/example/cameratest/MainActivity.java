package com.example.cameratest;

import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import com.example.camera_test.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2{
	final String TAG = "OPENCV";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat gray = null;
    private Mat descriptors=null;
    public static TextToSpeech tts;

    private native void setup();
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
        	
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:                	
                {
                	System.loadLibrary("camera_test");
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();  
            		setup();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "calrecognizeled onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(View.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        tts=new TextToSpeech(getApplicationContext(), 
			      new TextToSpeech.OnInitListener() {
			      @Override
			      public void onInit(int status) {
			         if(status != TextToSpeech.ERROR){
			             tts.setLanguage(Locale.UK);
			            }				
			         }
			      });
    }    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    @Override
	public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
    @Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	gray = inputFrame.gray();
		RecognizeTask task = new RecognizeTask();
		task.execute(gray);
		return inputFrame.rgba();
	}
	@Override
	public void onCameraViewStarted(int width, int height) {
		
	}
	@Override
	public void onCameraViewStopped() {
		
	}
	
}
