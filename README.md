# Normality-Zero-2016-Vision
The vision processing and tracking code for Normality Zero's 2016 season.

## Dependencies
#### Robot
- NetworkTables.jar: C:\Users\$user\wpilib\java\current\lib
- SmartDashboard.jar: C:\Users\$user\wpilib\tools
- WPILib.jar: C:\Users\$user\wpilib\java\current\lib (not sure if this is needed, it's on my build path and it could be left over from debugging)

#### OpenCV

OpenCV requires a user library to be created, in Eclipse, for it. This project currently uses OpenCV version 3.1. [Here](https://sourceforge.net/projects/opencvlibrary/files/opencv-win/3.1.0/opencv-3.1.0.exe/download) is the download link for OpenCV 3.1.0 on Windows. Download and extract it to somewhere you'll remember.

We now have to create a user library for it. Right click on the project and then select "Properties". Now, go to Java Build Path -> Libraries -> Add Library and select User Library. Click "Next" and you'll a list of all of your user libraries. On the right side of the window, click "User Libraries..." and then the "New..." on the window that pops up.
Name it whatever you'd like and leave the system library checkbox unchecked. Now, edit your library to follow this format:

![alt text](http://i.imgur.com/J2juWge.png "OpenCV User Library")

The native library location is very important. Click "OK" and then add your new User Library to the build path. Refresh the project and then the errors should have gone away!
