package edu.wilsonhs.camera.face;

import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import edu.wilsonhs.camera.MasterClass;
import edu.wilsonhs.camera.gui.DrawStuff;

/**
 * @author Seb
 *	A face object stores an image of the face and computes many details, i.e. location of both eyes, and angle/distance between them.
 *	every time a new picture is received, call updateFace()
 */
public class Face {
	private static final String EYE_CASCADE_FILE = "C:\\opencv\\data\\haarcascades\\haarcascade_eye_tree_eyeglasses.xml";
	private static CvHaarClassifierCascade eyeCascade;
	private static CvMemStorage storage;
	IplImage originalFace = null;
	IplImage shrunkFace = null;
	CvRect[] eyes = null;
	
	public Face() {
		eyeCascade = new CvHaarClassifierCascade(cvLoad(EYE_CASCADE_FILE));
		storage = CvMemStorage.create();
	}
	
	
	/**
	 * @param face takes a new face image and saves it and computes all of the stuff that it will need later.
	 */
	public void updateFace(IplImage face) {
		originalFace = face;
		if (!(originalFace.isNull())) {
			shrunkFace = IplImage
					.create((int) (originalFace.width() / MasterClass.preHaarFaceScaleFactor),
							(int) (originalFace.height() / MasterClass.preHaarFaceScaleFactor),
							opencv_core.IPL_DEPTH_8U, 3);
			opencv_imgproc.cvResize(originalFace, shrunkFace);
		}
		eyes = findEyes();
	}

	
	public IplImage getOriginalImage() {
		return originalFace;
	}

	public IplImage getShrunkImage() {
		return shrunkFace;
	}

	public IplImage getMarkedImage(CvScalar color) {
		IplImage toReturn = originalFace;
		if (eyes != null) {
			toReturn = DrawStuff.drawRect(toReturn, eyes[0], color);
			toReturn = DrawStuff.drawRect(toReturn, eyes[1], color);
		}

		return toReturn;
	}

	/**
	 * @return an image that has been rotated, scaled, equalized... Basically everything that need to be done before classifying it.
	 */
	public IplImage getSearchableImage() {
		// @TODO returns image rotated, scaled, equalized... Whatever needs to
		// be done in order to be ready for the comparison
		return null;
	}
	
	/**
	 * @return two eyes, left eye (their right eye) first
	 */
	public CvRect[] findEyes() {
		CvRect[] toReturn = new CvRect[2];
		CvSeq eyes = cvHaarDetectObjects(shrunkFace, eyeCascade, storage, 1.1,
				3, CV_HAAR_DO_CANNY_PRUNING);
		cvClearMemStorage(storage);

		if (cvGetSeqElem(eyes, 0) == null || cvGetSeqElem(eyes, 1) == null
				|| cvGetSeqElem(eyes, 0).isNull()
				|| cvGetSeqElem(eyes, 1).isNull())
			return null;// if second eye wasn't found (if first eye as well for
						// that matter)
		
		CvRect firstEye = toReturn[0] = scaleUp(new CvRect(cvGetSeqElem(eyes, 0)));
		CvRect secondEye = toReturn[1] = scaleUp(new CvRect(cvGetSeqElem(eyes, 1)));
		if(firstEye.x()<secondEye.x()){
			toReturn[0] = firstEye;
			toReturn[1] = secondEye;
		}else{
			toReturn[0] = secondEye;
			toReturn[1] = firstEye;
		}
		return toReturn;
	}

	public boolean isNull() {
		return (originalFace.isNull() || shrunkFace.isNull());
	}

	private CvRect scaleUp(CvRect original) {
		if (original.isNull())
			return new CvRect();
		return new CvRect(
				(int) (original.x() * MasterClass.preHaarFaceScaleFactor),
				(int) (original.y() * MasterClass.preHaarFaceScaleFactor),
				(int) (original.width() * MasterClass.preHaarFaceScaleFactor),
				(int) (original.height() * MasterClass.preHaarFaceScaleFactor));
	}

	public double getAngleBetweenEyes() {
		// left eye center position stored in leftEye. same for right.
		int[] leftEyeLoc = new int[2];// x coord, then y coord
		int[] rightEyeLoc = new int[2];
		
		if(eyes==null)return 0.0;
		leftEyeLoc[0] = eyes[0].x() + eyes[0].width() / 2;
		leftEyeLoc[1] = eyes[0].y() + eyes[0].height() / 2;
		rightEyeLoc[0] = eyes[1].x() + eyes[1].width() / 2;
		rightEyeLoc[1] = eyes[1].y() + eyes[1].height() / 2;
		
		if (leftEyeLoc[0] - rightEyeLoc[0] == 0){
			return 0;
		}	
		if(eyes[0].y()>eyes[1].y()){
			return (Math.atan(
					(double)Math.abs(leftEyeLoc[1] - rightEyeLoc[1])/
					(double)Math.abs(leftEyeLoc[0] - rightEyeLoc[0])
					)*180/Math.PI
					);
		}else{
			return 360-(Math.atan(
					(double)Math.abs(leftEyeLoc[1] - rightEyeLoc[1])/
					(double)Math.abs(leftEyeLoc[0] - rightEyeLoc[0])
					)*180/Math.PI
					);
		}
		
	}
}
