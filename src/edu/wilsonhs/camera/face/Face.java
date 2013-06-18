package edu.wilsonhs.camera.face;

import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import edu.wilsonhs.camera.MasterClass;
import edu.wilsonhs.camera.gui.DrawStuff;

public class Face {
	private static final String EYE_CASCADE_FILE = "C:\\opencv\\data\\haarcascades\\haarcascade_eye_tree_eyeglasses.xml";
	private static CvHaarClassifierCascade eyeCascade;
	private static CvMemStorage storage;
	IplImage originalFace = null;
	IplImage shrunkFace = null;
	
	public Face(){
		eyeCascade = new CvHaarClassifierCascade(cvLoad(EYE_CASCADE_FILE));
		storage = CvMemStorage.create();
	}
	
	public void updateFace(IplImage face){
		originalFace = face;
		if(!(originalFace.isNull())){
			shrunkFace = 
					IplImage.create((int)(originalFace.width()/MasterClass.preHaarFaceScaleFactor),
					(int)(originalFace.height()/MasterClass.preHaarFaceScaleFactor),
					opencv_core.IPL_DEPTH_8U, 3);
			opencv_imgproc.cvResize(originalFace, shrunkFace);
		}
	}
	//**returns two eyes, leftmost first*/
	public CvRect[] findEyes(){
		CvRect[] toReturn = new CvRect[2];
		CvSeq eyes = cvHaarDetectObjects(shrunkFace, eyeCascade, storage, 1.1, 3,CV_HAAR_DO_CANNY_PRUNING);
		//CvSeq eyes = cvHaarDetectObjects(originalFace, eyeCascade, storage, 1.1, 3,CV_HAAR_DO_CANNY_PRUNING);
		cvClearMemStorage(storage);
		
		if(cvGetSeqElem(eyes, 0) == null ||
			cvGetSeqElem(eyes, 1) == null ||
			cvGetSeqElem(eyes, 0).isNull() ||
			cvGetSeqElem(eyes, 1).isNull())
			return null;//if second eye wasn't found (if first eye as well for that matter)
		
		toReturn[0] = scaleUp(new CvRect(cvGetSeqElem(eyes, 0)));
		toReturn[1] = scaleUp(new CvRect(cvGetSeqElem(eyes, 1)));
		return toReturn;
	}
	
	public IplImage getOriginalImage(){
		return originalFace;
	}
	
	public IplImage getShrunkImage(){
		return shrunkFace;
	}
	
	public IplImage getMarkedImage(CvScalar color){
		IplImage toReturn = originalFace;
		CvRect[] eyes = findEyes();
		if(eyes != null){
			toReturn = DrawStuff.drawRect(toReturn, eyes[0], color);
			toReturn = DrawStuff.drawRect(toReturn, eyes[1], color);
		}
		
		return toReturn;
	}
	
	public boolean isNull(){
		return(originalFace.isNull()||shrunkFace.isNull());
	}
	
	private CvRect scaleUp(CvRect original){
		if(original.isNull())return new CvRect();
		return new CvRect((int)(original.x()*MasterClass.preHaarFaceScaleFactor),
						  (int)(original.y()*MasterClass.preHaarFaceScaleFactor),
						  (int)(original.width()*MasterClass.preHaarFaceScaleFactor),
						  (int)(original.height()*MasterClass.preHaarFaceScaleFactor));
	}
}
