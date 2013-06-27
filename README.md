FaceRec
=======

This Program currently takes a live webcam stream and displays images on two frames:
Frame 1: Full webcam image w/ boxes around all detected faces
Frame 2: Frame with only closest face.  Eyes are marked with boxes

/Flowcharts contains a basic flowchart of the systems involved in FaceRec ID login.

Using OpenCV version 2.4.5 and javacv

Read @TODO's in the issues section

Getting Started:
-Install OpenCV 2.4.5 from their website.  Extract to C:\opencv
-Download javacv 2.4.5.  Extract to wherever.
-Install eGit (an eclipse plugin), and clone this project into the workspace.  Once it's set up correctly, "Right click>Team" should show tons of options including commit, push pull...
-Right click on project>configure build path>add libraries
-Make a new library called javacv-2.4.5 and add <javacv-bin>/javacv.jar, <javacv-bin>/javacpp.jar, and <javacv-bin>/javacv-<your architecture>.jar
-Be sure to add it to the project after creating it 