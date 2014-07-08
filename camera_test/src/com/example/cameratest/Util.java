package com.example.cameratest;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;

public abstract class Util {
	
	protected static void drawKeypoints(Mat frame){
		
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		detector.detect(frame, keypoints);
		Features2d.drawKeypoints(frame, keypoints, frame);
	}
	

}
