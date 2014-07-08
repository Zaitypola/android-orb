package com.example.cameratest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import android.util.Log;

public class ObjectRecognizer {
	
	DescriptorMatcher matcher;
	FeatureDetector detector;
	DescriptorExtractor extractor;
	
	Mat book_desc,book,descriptors;
	MatOfKeyPoint book_kps,keypoints;
	List <MatOfDMatch> matches;
	List <DMatch> good_matches;
			
	public ObjectRecognizer(){
		
		detector = FeatureDetector.create(FeatureDetector.ORB);
		extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		matches = new ArrayList<MatOfDMatch>();
		good_matches = new ArrayList<DMatch>();
		descriptors=new Mat();
		book_desc = new Mat();
		book_kps = new MatOfKeyPoint();
		keypoints = new MatOfKeyPoint();
		book = Highgui.imread("/storage/emulated/0/Download/Stop_sign.png");
		detector.detect(book,book_kps);
		extractor.compute(book, book_kps, book_desc);		
	}

	public void recognize(Mat img){
		detector.detect(img, keypoints);
		extractor.compute(img, keypoints, descriptors);
		matcher.knnMatch(descriptors,book_desc, matches, 2);

		int n=filter(matches);
		if(n>=4){
			find_object(img);
			Log.i("FOUND","Object found.");
		}
		Log.i("good_matches",Integer.toString(n)+"/"+Integer.toString(matches.size()));
	}
	
	private void find_object(Mat img) {
		LinkedList<Point> obj = new LinkedList<Point>();
		LinkedList<Point> scene = new LinkedList<Point>();
		MatOfPoint2f obj_mat = new MatOfPoint2f();
		MatOfPoint2f scene_mat = new MatOfPoint2f();
		
		for (int i = 0 ; i < good_matches.size(); i++){
			obj.add(book_kps.toArray()[good_matches.get(i).trainIdx].pt);
			scene.add(keypoints.toArray()[good_matches.get(i).queryIdx].pt);
		}
		
		obj_mat.fromList(obj);
		scene_mat.fromList(scene);
		
		Mat H = Calib3d.findHomography(obj_mat, scene_mat,Calib3d.RANSAC,0.5);
				
		Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
		Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);
		obj_corners.put(0, 0, new double[] {0,0});
		obj_corners.put(1, 0, new double[] {book.cols(),0});
		obj_corners.put(2, 0, new double[] {book.cols(),book.rows()});
		obj_corners.put(3, 0, new double[] {0,book.rows()});
		
		Core.perspectiveTransform(obj_corners,scene_corners, H);
		
		Core.line(img, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(0, 255, 0),4);
		Core.line(img, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(0, 255, 0),4);
		Core.line(img, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(0, 255, 0),4);
		Core.line(img, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(0, 255, 0),4);

	}

	private int filter(List<MatOfDMatch> matches){
		
		for(MatOfDMatch match : matches){
			
			DMatch first = match.toArray()[0];
			DMatch second = match.toArray()[1];
			if (first.distance < 40)
				if (first.distance < 0.75*second.distance){
					good_matches.add(first);
				}
		}

		return good_matches.size();
	}

}