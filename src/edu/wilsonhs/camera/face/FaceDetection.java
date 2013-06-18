package edu.wilsonhs.camera.face;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

public class FaceDetection {
	private static final String FACE_CASCADE_FILE = "C:\\opencv\\data\\haarcascades\\haarcascade_frontalface_alt.xml";
	static String imgPath = "C:\\Users\\Seb\\Desktop\\group.jpg";
	private static FrameGrabber grabber;
	private static CvMemStorage storage;
	private static CvHaarClassifierCascade faceCascade;
	
	public static void init() {
		System.out.println("Starting OpenCV...");
		Loader.load(opencv_objdetect.class);
		try {
			grabber = FrameGrabber.createDefault(0);
			grabber.setImageHeight(480);
			grabber.setImageWidth(640);
			grabber.start();
		} catch (Exception e) {}
		// create temp storage, used during object detection
		storage = CvMemStorage.create();

		// instantiate a classifier cascade for face detection
		faceCascade = new CvHaarClassifierCascade(cvLoad(FACE_CASCADE_FILE));
	}

	public static IplImage getImage() throws Exception {
		return grabber.grab();
	}

	public static IplImage filter(IplImage img) {
		// grayscale
		IplImage grayImg = IplImage.create(img.width(), img.height(),
				IPL_DEPTH_8U, 1);
		cvCvtColor(img, grayImg, CV_BGR2GRAY);

		// equalize
		IplImage equImg = IplImage.create(grayImg.width(), grayImg.height(),
				IPL_DEPTH_8U, 1);
		opencv_imgproc.cvEqualizeHist(grayImg, equImg);
		return equImg;
	}


	/**
	 * Returns an array of all cropped faces in their own IplImages
	 */
	public static IplImage[] getFaces(IplImage img, CvSeq faces){
		IplImage[] toReturn = new IplImage[faces.total()];
		for(int i=0;i<faces.total();i++){
			CvRect r = new CvRect(cvGetSeqElem(faces, i));
			opencv_core.cvSetImageROI(img,r);
			IplImage face = IplImage.create(r.width(),r.height(), IPL_DEPTH_8U, 3);
			opencv_core.cvCopy(img, face);
			toReturn[i]=face;
			opencv_core.cvResetImageROI(img);
		}
		System.out.println(toReturn.length + " Returned faces.");
		return toReturn;
	}
	
	public static CvRect biggestRect(CvSeq faces){
		if(faces.sizeof()==0)return null;
		if(faces.sizeof()==1)return(new CvRect(cvGetSeqElem(faces, 0)));
		int biggestIndex = 0;
		for (int i = 1; i <faces.total(); i++){
			CvRect biggest = new CvRect(cvGetSeqElem(faces, biggestIndex));
			CvRect current = new CvRect(cvGetSeqElem(faces, i));
			if(current.width()*current.height()>biggest.width()*biggest.height())
				biggestIndex = i;
		}
		return new CvRect(cvGetSeqElem(faces, biggestIndex));
	}
	
	public static CvRect scaleUp(CvRect original, double scaleFactor){
		return new CvRect(
				(int)(original.x()*scaleFactor),
				(int)(original.y()*scaleFactor), 
				(int)(original.width()*scaleFactor), 
				(int)(original.height()*scaleFactor));	
	}
	
	public static CvSeq findFaces(IplImage img) {		
		CvSeq faces = cvHaarDetectObjects(img, faceCascade, storage, 1.1, 3,CV_HAAR_DO_CANNY_PRUNING);
		cvClearMemStorage(storage);
		return faces;
	}		

}
