import cv2
from cv2.cv import CV_HAAR_DO_CANNY_PRUNING
import sys
import Image, time
import numpy as np
import os

cv2.namedWindow('test')
# img = numpy.array(cv2.imread("c:/Users/Seb/Desktop/arnold.jpg"),dtype=numpy.uint8)
#cv2.imwrite("/faces/" + id + "/" + nextImage + ".jpg",img)

def read_images(path):
    x,y = [],[]
    for folder in os.listdir(path):
        if folder.find('.')==-1: #no dots.  Rudimentary check to make sure it is a folder, not a file
            for file in os.listdir(path + folder):
                extension = file.split('.')
                if len(extension)>1 and extension[1].lower() == 'jpg':
                    img = cv2.imread(path+folder+"/"+file,cv2.IMREAD_COLOR)
                    #x.append(np.asarray(img, dtype=np.uint8))
                    x.append(np.asarray(img, dtype=np.uint8))
                    y.append(folder)
    return [x,y]
                    

for img in read_images("c:/users/seb/desktop/test/")[0]:
    cv2.imshow('test',img)
    cv2.waitKey(2000)

# cv2.imshow('test', img)
# 
# ch = cv2.waitKey(15000)
# cv2.destroyAllWindows()