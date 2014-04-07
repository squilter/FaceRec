package edu.wilsonhs.camera;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;

import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_videostab.TwoPassStabilizer;
import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.wilsonhs.camera.face.Face;
import edu.wilsonhs.camera.face.FaceDetection;
import edu.wilsonhs.camera.gui.DrawStuff;

public class MasterClass {
//for face recognizer:	
public static final double preHaarScaleFactor = 5;
public static final double preHaarFaceScaleFactor = 1.5;
	public static void main(String[] args) throws Exception {
		FaceDetection.init();
		DrawStuff bigCanvas = new DrawStuff("Webcam");
		DrawStuff faceCanvas = new DrawStuff("Face");
		IplImage grabbedImage = null; //original image
		IplImage filteredImage = null; //after filters
		IplImage shrunkImage = null;
		Face biggestFace = new Face();
		while(true){
			//get Image and process it
			grabbedImage = FaceDetection.getImage();
			filteredImage = FaceDetection.filter(grabbedImage);
			shrunkImage = IplImage.create((int)(640/preHaarScaleFactor), (int)(480/preHaarScaleFactor), opencv_core.IPL_DEPTH_8U, 1);
			opencv_imgproc.cvResize(filteredImage, shrunkImage);
			
			//Detect faces
			CvSeq faces = FaceDetection.findFaces(shrunkImage);
			
			//find biggest face
			CvRect biggestFaceRect = FaceDetection.biggestRect(faces);
			if(!biggestFaceRect.isNull()){
				biggestFaceRect = FaceDetection.scaleUp(biggestFaceRect, preHaarScaleFactor);
				opencv_core.cvSetImageROI(grabbedImage,biggestFaceRect);
				IplImage biggestFaceImg = IplImage.create(biggestFaceRect.width(), biggestFaceRect.height(), IPL_DEPTH_8U, 3);
				opencv_core.cvCopy(grabbedImage, biggestFaceImg);
				opencv_core.cvResetImageROI(grabbedImage);
				biggestFace.updateFace(biggestFaceImg);
			}
			
			//@TODO scale image to 200x200 by ratio from one eye to another (so that left eye and right eye are always in same place of every image)
			
			//show stuff on screen:
			IplImage mainFrame = DrawStuff.markHeads(grabbedImage,faces,CvScalar.GREEN,preHaarScaleFactor);//normal
			if(!biggestFaceRect.isNull() && !(biggestFace.isNull()) && (biggestFace.twoEyes())){
				mainFrame = DrawStuff.drawRect(mainFrame, biggestFaceRect, CvScalar.RED);
				//IplImage faceFrame = biggestFace.getSearchableImage(200);
				if(faceFrame!=null)
				faceCanvas.displayOnCanvas(faceFrame);
			}
			bigCanvas.displayOnCanvas(mainFrame);

		}
		
//		grabbedImage.release();
//		filteredImage.release();
//		final FaceRecognition faceRecognition = new FaceRecognition();
//		// faceRecognition.learn("data/some-training-faces.txt");
//		faceRecognition.learn("data/all10.txt");
//		// faceRecognition.recognizeFileList("data/some-test-faces.txt");
//		faceRecognition.recognizeFileList("data/lower3.txt");
		
	}
}
