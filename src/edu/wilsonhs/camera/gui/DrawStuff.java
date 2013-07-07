package edu.wilsonhs.camera.gui;

import static com.googlecode.javacv.cpp.opencv_core.CV_32F;
import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_contrib;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;

public class DrawStuff {
	private CanvasFrame canvas;
	
	public DrawStuff(String name){
		canvas = new CanvasFrame(name);
		canvas.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);	
	}
	public void displayOnCanvas(IplImage img){
		canvas.showImage(img);
	}
	public static IplImage markHeads(IplImage img,CvSeq faces,CvScalar color, double scaleFactor){
	    for (int i = 0; i <faces.total(); i++) {
		      CvRect r = new CvRect(cvGetSeqElem(faces, i));
		      r = new CvRect((int)(r.x()*scaleFactor),(int)(r.y()*scaleFactor), (int)(r.width()*scaleFactor), (int)(r.height()*scaleFactor));
		      img = drawRect(img, r, color);
	    }
	    return img;
	}
	public static IplImage drawRect(IplImage img, CvRect rect, CvScalar color){
		if(rect.isNull()){
			System.out.println("CvRect is null");
			return img;
		}
	      cvRectangle(img, cvPoint( rect.x(), rect.y() ),    
                  cvPoint( (rect.x() + rect.width()), (rect.y() + rect.height()) ), 
                      color, 6, CV_AA, 0);
	      return img;
	}
	
	public static IplImage jetmapHeads(IplImage img,CvSeq faces, double scaleFactor){
		IplImage colorImg = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 3);  
		opencv_core.cvMerge(img, img, img, null, colorImg);
		img.release();
	    for (int i = 0; i <faces.total(); i++) {
		      CvRect r = new CvRect(cvGetSeqElem(faces, i));
		      r = new CvRect((int)(r.x()*scaleFactor), (int)(r.y()*scaleFactor), (int)(r.width()*scaleFactor), (int)(r.height()*scaleFactor));
		      opencv_core.cvSetImageROI(colorImg,r);	      
		      opencv_contrib.applyColorMap(colorImg, colorImg, opencv_contrib.COLORMAP_JET);
		      opencv_core.cvResetImageROI(colorImg);
	    }
	    return colorImg;
	}
	
	public static IplImage rotateImage(IplImage src, double angle){
		CvMat input = src.asCvMat();
	    CvPoint2D32f center = new CvPoint2D32f(input.cols() / 2.0F,
	            input.rows() / 2.0F);
	    
	    CvMat rotMat = opencv_core.cvCreateMat(2, 3, CV_32F);
	    opencv_imgproc.cv2DRotationMatrix(center, angle, 1, rotMat);
	    CvMat dst = opencv_core.cvCreateMat(input.rows(), input.cols(), input.type());
	    opencv_imgproc.cvWarpAffine(input, dst, rotMat);
		return dst.asIplImage();
	}
}