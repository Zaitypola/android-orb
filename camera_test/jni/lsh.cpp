#include <jni.h>
#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>

using namespace cv;
using namespace std;
using namespace flann;

	class ObjectRecognizer{
		public:
			static FlannBasedMatcher flannmatcher;
			OrbFeatureDetector detector;
			OrbDescriptorExtractor extractor;
			Mat descriptors;
			vector<KeyPoint> keypoints;
			vector< vector<DMatch> > matches;
			vector<DMatch> good_matches;
		public:
			int recognize(Mat frame);
			int filter(vector< vector<DMatch> > matches);
	};

	FlannBasedMatcher ObjectRecognizer::flannmatcher(new LshIndexParams(10,15,2));

	int ObjectRecognizer::recognize(Mat frame){

		detector.detect(frame, keypoints);
		extractor.compute(frame, keypoints, descriptors);
		flannmatcher.knnMatch(descriptors, matches, 2);

		return filter(matches);
	}

	bool compare_matches(const DMatch& a, const DMatch& b){

		return a.imgIdx<b.imgIdx;

	}

	void find_max(vector<DMatch> matches){

		if(good_matches.size()>0){

			sort(good_matches.begin(),good_matches.end(),compare_matches);
			int candidate=good_matches[0].imgIdx;
			int current_max=good_matches[0].imgIdx;
			int count=0;
			int count_max=0;

			for(int i = 0; i<good_matches.size();i++){

				if(candidate==good_matches[i].imgIdx){
					count=count+1;
					if(count>count_max)
						count_max=count;
					candidate=good_matches[i].imgIdx;

				}else if(count>count_max){
					count_max=count;
					current_max=candidate;
					candidate=good_matches[i].imgIdx;
					count=1;
				}else{
					candidate=good_matches[i].imgIdx;
					count=1;
				}
			}

			char idx[1];

			sprintf(idx,"%d-%d",current_max,count_max);

			__android_log_write(ANDROID_LOG_VERBOSE, "idx", idx);

	}

	int ObjectRecognizer::filter(vector < vector<DMatch> > matches){

		for(int i = 0;i<matches.size();i++){

			if(matches[i].size()==2){
				DMatch first = matches[i][0];
				DMatch second = matches[i][1];

				if(first.distance<=40 && (first.distance<0.75*second.distance)){

					good_matches.push_back(first);

				}
			}
		}
		/*From the good_matches, we need to find the image that has been repeated
		the most and see if it's repeated enought times.
		*/


		}
		return good_matches.size();
	}
extern "C"{
	int Java_com_example_cameratest_RecognizeTask_recognitionjni(JNIEnv *env, jobject obj, jlong img){

		ObjectRecognizer recognizer;
		Mat* frame = (Mat*) img;
		Mat f = *frame;
		int n = recognizer.recognize(f);
		return n;
	}

	void Java_com_example_cameratest_MainActivity_setup(JNIEnv *env, jobject obj){
		ObjectRecognizer recognizer;
		Mat train,train_desc;
		vector<KeyPoint> train_kps;
		vector<Mat> train_descriptors;

		train=imread("/storage/emulated/0/Download/download.jpg");
		recognizer.detector.detect(train,train_kps);
		recognizer.extractor.compute(train,train_kps,train_desc);
		train_descriptors.push_back(train_desc);

		train=imread("/storage/emulated/0/Download/Stop_sign.png");
		recognizer.detector.detect(train,train_kps);
		recognizer.extractor.compute(train,train_kps,train_desc);
		train_descriptors.push_back(train_desc);

		train=imread("/storage/emulated/0/Download/origin.jpg");
		recognizer.detector.detect(train,train_kps);
		recognizer.extractor.compute(train,train_kps,train_desc);
		train_descriptors.push_back(train_desc);

		/*MATCHING: What is what??? Find To which Mat of descriptors the matches belong.
		* 1- Map k:Hash(descriptors) v:Name of the file (name of the object)
		* 2- TTS name of the object.
		* 3- Find to which descriptors the best matches belong! To just one? Many?
		* 4- Scalability : LSH, Bag of words,BF - Outperform which?
		* 5-
		*/
		recognizer.flannmatcher.add(train_descriptors);
		recognizer.flannmatcher.train();
	}
}
