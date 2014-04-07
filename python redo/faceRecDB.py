import os.path
import sys
import numpy as np
import cv2
from cv2.cv import CV_HAAR_FIND_BIGGEST_OBJECT
from cv2.cv import CV_HAAR_DO_CANNY_PRUNING
import shutil

""""Made by Seb, July 2013
This script should be saved at the root level of the usb drive
for each image in ./newCapture/:
   cropFace
   place in /faces/<ID#>/<number>.jpg
Run createCSV.  Outputs to /classifiers/faceIndex.csv
Train all 3 types of classifiers into /classifiers/<type>.xml"""

#@TODO implement crop scale rotate of face
def clean_image(img,leftEye, rightEye):
    return img

def get_eyes(img, cascade):
    """returns a list with locations of both eyes in this order: leftX, leftY, rightX, rightY
    TESTED: SUCCESS
     """
    rects = cascade.detectMultiScale(img,scaleFactor=1.3, minNeighbors=4, minSize=(8, 8), flags = CV_HAAR_DO_CANNY_PRUNING)
    if(len(rects) != 2):
        return None
    rects[:,2:] += rects[:,:2]
    toReturn = []
    for x1, y1, x2, y2 in rects:
        toReturn.append((x1+x2)/2)
        toReturn.append((y1+y2)/2)
    #make sure left eye comes first:
    if(toReturn[2]<toReturn[0]):
        toReturn.append(toReturn[0])
        toReturn.append(toReturn[1])
        del toReturn[0:2]
    return toReturn

def next_file_to_write(path):
    """Returns the number of the next image to write.  If subject has 1.jpg, 2.jpg and 3.jpg in their folder, this will return the string '4.jpg'
        TESTED:SUCCESS
    """
    imgs=os.listdir(path)
    names = []
    for img in imgs:
        names.append(int(img.split('.')[0]))
    return str(max(names)+1) +".jpg"  

def read_images(path):
    """reads all necessary faces into arrays.  returns two arrays, x: images, y:identifiers
        TESTED:SUCCESS
    """
    x,y = [],[]
    for folder in os.listdir(path):
        if folder.find('.')==-1: #no dots.  Rudimentary check to make sure it is a folder, not a file
            for file in os.listdir(path + folder):
                extension = file.split('.')
                if len(extension)>1 and extension[1].lower() == 'jpg':
                    img = cv2.imread(path+folder+"/"+file,cv2.IMREAD_COLOR)
                    x.append(np.asarray(img, dtype=np.uint8))
                    y.append(folder)
    return [x,y]

if __name__ == "__main__":
    #clean Faces and put them into their folders
    print("Loading Eye Classifier...")
    cascade = cv2.CascadeClassifier("./classifiers/haarcascade_eye_tree_eyeglasses.xml")
    for imgName in os.listdir("./newCaptures"):
        extension = imgName.split('.')
        if len(extension)>1 and extension[1].lower() == 'jpg':
            sys.stdout.write(imgName + ':')
            img = cv2.imread(imgName)
            eyes = get_eyes(img)
            if(len(eyes)==2):
                print("pass")
                img = clean_image(img,eyes[0],eyes[1])
                faceIdentifier = imgName[:7]
                pathForImage = "./faces/" + faceIdentifier + "/"
                if not os.path.exists(pathForImage):
                    os.mkdir(pathForImage)
                cv2.imwrite(pathForImage+next_file_to_write(pathForImage),img)
                os.remove("./newCaptures/" + imgName)
            else:
                print("fail")
                shutil.copyfile("./newCaptures/" + imgName,"./failedPhotos/" + imgName)
    
    print "Loading Images into Memory"
    [X,y] = read_images("./faces/")
    y = np.asarray(y, dtype=np.int32)
    model = cv2.createLBPHFaceRecognizer()
    print "Training LBPH Face Recognizer"
    model.train(np.asarray(X), np.asarray(y))           
    #train those classifiers and save them        
    #cv2.createEigenFaceRecognizer()    
    #use facerec_demo.py    