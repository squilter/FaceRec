package edu.wilsonhs.camera.face;

import static com.googlecode.javacv.cpp.opencv_core.CV_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import edu.wilsonhs.camera.MasterClass;
import edu.wilsonhs.camera.gui.DrawStuff;
import static java.lang.Math.*;

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
		eyes = findEyes(shrunkFace);
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
	 * Not Implemented yet!!
	 * @return an image that has been rotated, scaled, equalized... Basically everything that need to be done before classifying it.  Returns null whenever it can't find what it needs to!
	 */
//	public IplImage getSearchableImage(int outputSize) {
//		// @TODO returns image rotated, scaled, equalized... Whatever needs to
//		// be done in order to be ready for the comparison
//		//scale, rotate,crop
//		
//		//retrieve eyes
//		int[] leftEyeLoc = new int[2];// x coord, then y coord
//		int[] rightEyeLoc = new int[2];
//		if(eyes==null)return null;
//		leftEyeLoc[0] = eyes[0].x() + eyes[0].width() / 2;
//		leftEyeLoc[1] = eyes[0].y() + eyes[0].height() / 2;
//		rightEyeLoc[0] = eyes[1].x() + eyes[1].width() / 2;
//		rightEyeLoc[1] = eyes[1].y() + eyes[1].height() / 2;
//		
//		//retrieve image
//		IplImage image = getOriginalImage();
//		
//		double angleBetweenEyesDegrees = getAngleBetweenEyes();
//		
//		//rotate @TODO if scaling problems arise, make it rotate around the left eye		
//		image = rotateImage(image,360-angleBetweenEyesDegrees);
//		
//		
//		double eyeDistance = distance(leftEyeLoc, rightEyeLoc);
//		
//		//crop
//		if(eyeDistance!=0){
//			try{
//				int x,y,width,height,secondX,secondY;
//				x=(int)(leftEyeLoc[0]-eyeDistance*0.3);
//				y=(int)(leftEyeLoc[1]-eyeDistance*0.6);
//				secondX = (int)(rightEyeLoc[0]+eyeDistance*0.3);
//				secondY = (int)(rightEyeLoc[1]+eyeDistance*1.5);
////				width=(int)(x+eyeDistance*1.0);
////				height=(int)(y+eyeDistance*1.7);
//				width = secondX-x;
//				height = secondY-y;
//				opencv_core.cvSetImageROI(image, new CvRect(x, y, width, height));
//				System.out.println(image.roi().width() + " " + image.roi().height());
//				System.out.println(width + ", " + height);
//				IplImage croppedFaceImage = IplImage.create(width, height, IPL_DEPTH_8U, 3);
//				//IplImage croppedFaceImage = IplImage.create(200, 200, IPL_DEPTH_8U, 3);
//				opencv_core.cvCopy(image, croppedFaceImage);
//				opencv_core.cvResetImageROI(image);
//				
//				//@TODO figure out why it isn't rotating well
//				//@TODO scale it to 200x200 (outputSize, outputSize)
//				
//				
//				if(croppedFaceImage.width()==0||croppedFaceImage.height()==0)return null;
//				//return DrawStuff.drawRedDot(DrawStuff.drawRedDot(image, rightEyeLoc[0], rightEyeLoc[1]), leftEyeLoc[0], leftEyeLoc[1]+100);
//				return croppedFaceImage;
//			}catch(Exception e){System.err.println("some sort of error");}
//		}
//		return null;
//
//	}
	
	/**
	 * @return two eyes, left eye (their right eye) first
	 */
	public CvRect[] findEyes(IplImage src) {
		CvRect[] toReturn = new CvRect[2];
		CvSeq eyes = cvHaarDetectObjects(src, eyeCascade, storage, 1.2,
				3, CV_HAAR_DO_CANNY_PRUNING);
		cvClearMemStorage(storage);

		if (cvGetSeqElem(eyes, 0) == null || cvGetSeqElem(eyes, 1) == null
				|| cvGetSeqElem(eyes, 0).isNull()
				|| cvGetSeqElem(eyes, 1).isNull())
			return null;// if second eye wasn't found (if first eye as well for
						// that matter)
		
		CvRect firstEye;
		CvRect secondEye;
		
		firstEye =  scaleUp(new CvRect(cvGetSeqElem(eyes, 0)));
		secondEye = scaleUp(new CvRect(cvGetSeqElem(eyes, 1)));
		
		
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
	
	public boolean twoEyes(){
		return(eyes!=null);
		
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
	
	private static IplImage rotateImage(IplImage src, double angle){
		CvMat input = src.asCvMat();
	    CvPoint2D32f center = new CvPoint2D32f(input.cols() / 2.0,
	            input.rows() / 2.0);
	    
	    CvMat rotMat = opencv_core.cvCreateMat(2, 3, CV_32F);
	    opencv_imgproc.cv2DRotationMatrix(center, angle, 1, rotMat);
	    CvMat dst = opencv_core.cvCreateMat(input.rows(), input.cols(), input.type());
	    opencv_imgproc.cvWarpAffine(input, dst, rotMat);
		return dst.asIplImage();
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
	
	private double distance(int[] p1, int[]p2){
		double dx=p2[0] - p1[0];
		double dy=p2[1] - p1[1];
		return Math.sqrt(dx*dx+dy*dy);
	}
}
