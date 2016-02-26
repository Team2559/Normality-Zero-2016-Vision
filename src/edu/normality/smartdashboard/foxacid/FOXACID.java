package edu.normality.smartdashboard.foxacid;

import edu.normality.smartdashboard.editedview.EditedWebcamViewer;
import edu.normality.smartdashboard.foxacidconfigure.FOXACIDCONFIGURE;
import edu.wpi.first.smartdashboard.gui.DashboardPrefs;
import edu.wpi.first.smartdashboard.properties.IPAddressProperty;
import edu.wpi.first.smartdashboard.properties.Property;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.smartdashboard.gui.elements.VideoStreamViewerExtension;
import edu.wpi.first.smartdashboard.robot.Robot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

public class FOXACID extends VideoStreamViewerExtension {

	public static final String NAME = "FOXACID - Tower Tracker";
	private boolean ipChanged = true;
	private String ipString = null;
	private long lastFPSCheck = 5L;
	private int lastFPS = 4;
	private int fpsCounter = 3;
	private BufferedImage imageToDraw;

	public static final double kMorphKernelSize = 1;
	public static final int kMinHue = 50;
	public static final int kMinSat = 120;
	public static final int kMinVal = 100;

	public static final int kMaxHue = 100;
	public static final int kMaxSat = 255;
	public static final int kMaxVal = 255;
	
//	Constants for known variables
//	the height to the top of the target in first stronghold is 97 inches	
	public static final int kTopTargetHeight = 97;
//	the physical height of the camera lens
	public static final int kTopCameraHeight = 32;
	
//	camera details, can usually be found on the datasheets of the camera
	public static final double kVerticalFOV  = 34;
	public static final double kHorizontalFOV  = 61;
	public static final double kCameraAngle = 10;
	
	// shooter offset
	public static final double 	kShooterOffsetDegX = 0,
								kShooterOffsetDegY = 0;
	
	public static final int[] resolution = {640, 360};
	
	public static final boolean debug = false;
	
	public double heading, angleOfShooter;
	
	ITable outputTable;

	static Mat src, dest, image;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		NetworkTable.setClientMode();
	}

	public static void main(String[] args) {
		return;
	}

	public class BGThread extends Thread {
		boolean destroyed = false;

		public BGThread() {
			super();
		}

		long lastRepaint = 0L;

		public void run() {
			outputTable = Robot.getTable();
			while (!this.destroyed) {
				try {
					FOXACID.this.ipChanged = false;
					while ((!this.destroyed) && (!FOXACID.this.ipChanged)) {
						while (System.currentTimeMillis() - this.lastRepaint < 5L) {
							Thread.sleep(1L);
						}
						
						FOXACID.this.fpsCounter++;
						if (System.currentTimeMillis()
								- FOXACID.this.lastFPSCheck > 500L) {
							FOXACID.this.lastFPSCheck = System
									.currentTimeMillis();
							FOXACID.this.lastFPS = (FOXACID.this.fpsCounter * 2);
							FOXACID.this.fpsCounter = 0;
						}
						BufferedImage img = EditedWebcamViewer.getLatestCapture();
						byte[] data = ((DataBufferByte) img.getRaster()
								.getDataBuffer()).getData();
						Mat original = new Mat(img.getHeight(), img.getWidth(),
								CvType.CV_8UC3);
						original.put(0, 0, data);
						Mat processed = findTower(original);
						byte[] byteArray = new byte[processed.rows()
								* processed.cols()
								* (int) (processed.elemSize())];
						processed.get(0, 0, byteArray);
						if (processed.channels() == 3) {
							for (int i = 0; i < byteArray.length; i += 3) {
								byte temp = byteArray[i];
								byteArray[i] = byteArray[i + 2];
								byteArray[i + 2] = temp;
							}
						}
						BufferedImage image = new BufferedImage(
								processed.cols(), processed.rows(),
								BufferedImage.TYPE_3BYTE_BGR);
						image.getRaster().setDataElements(0, 0,
								processed.cols(), processed.rows(), byteArray);
						FOXACID.this.imageToDraw = image;
						original.release();
						processed.release();
//						System.out.println("Successfully drawn image: "
//								+ img.getWidth() + " x " + img.getHeight());
						this.lastRepaint = System.currentTimeMillis();
						FOXACID.this.repaint();
					}
				} catch (Exception e) {
					FOXACID.this.imageToDraw = null;
					FOXACID.this.repaint();
					e.printStackTrace();
				}
				if (!FOXACID.this.ipChanged) {
					try {
						Thread.sleep(500L);
					} catch (InterruptedException ex) {
					}
				}
			}
		}

		public void destroy() {
			this.destroyed = true;
		}
	}

	private Mat findTower(Mat src) {
		boolean[] badApples;
		ArrayList<Point> towers = new ArrayList<Point>();
		double startTime = System.currentTimeMillis();
		FOXACIDCONFIGURE.minHueLabel.setText("minHue: " + FOXACIDCONFIGURE.minHueSlider.getValue());
		FOXACIDCONFIGURE.maxHueLabel.setText("maxHue: " + FOXACIDCONFIGURE.maxHueSlider.getValue());
		FOXACIDCONFIGURE.minSatLabel.setText("minSat: " + FOXACIDCONFIGURE.minSatSlider.getValue());
		FOXACIDCONFIGURE.maxSatLabel.setText("maxSat: " + FOXACIDCONFIGURE.maxSatSlider.getValue());
		FOXACIDCONFIGURE.minValLabel.setText("minVal: " + FOXACIDCONFIGURE.minValSlider.getValue());
		FOXACIDCONFIGURE.maxValLabel.setText("maxVal: " + FOXACIDCONFIGURE.maxValSlider.getValue());
		FOXACIDCONFIGURE.minHeightLabel.setText("minHeight: " + FOXACIDCONFIGURE.minHeightSlider.getValue());
		FOXACIDCONFIGURE.minWidthLabel.setText("minWidth: " + FOXACIDCONFIGURE.minWidthSlider.getValue());
		FOXACIDCONFIGURE.minAspectLabel.setText("minAspect: " + (double)FOXACIDCONFIGURE.minAspectSlider.getValue() / 500d);
		FOXACIDCONFIGURE.maxAspectLabel.setText("maxAspect: " + (double)FOXACIDCONFIGURE.maxAspectSlider.getValue() / 200d);
		
		Mat hsv = new Mat(), thresh = new Mat(), heirarchy = new Mat();
		Scalar lowerBound = new Scalar(FOXACIDCONFIGURE.getMinHue(),
				FOXACIDCONFIGURE.getMinSat(), FOXACIDCONFIGURE.getMinVal()), upperBound = new Scalar(
				FOXACIDCONFIGURE.getMaxHue(), FOXACIDCONFIGURE.getMaxSat(),
				FOXACIDCONFIGURE.getMaxVal());
		List<MatOfPoint> contours = new ArrayList<>();
		// read the image
		// src = Imgcodecs.imread(filePath + extension);
		image = src;
		// convert color to HSV
		Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
		// perform a threshhold with given range
		Core.inRange(hsv, lowerBound, upperBound, thresh);
		// create a morph kernel to do morphing with later
		Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(kMorphKernelSize, kMorphKernelSize));
		// morph the image
		Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_OPEN, morphKernel);
		// find the contours
		Imgproc.findContours(thresh.clone(), contours, heirarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		System.out.println("Contours size before: " + contours.size());
		badApples = new boolean[contours.size()];
		for (int dre = 0; dre < contours.size(); dre++) {
			Rect tempRec = Imgproc.boundingRect(contours.get(dre));
			int width = tempRec.width, height = tempRec.height;
			double aspect = (double)width /height;
			
			boolean shouldRemove = false;			
			if (width <= FOXACIDCONFIGURE.getMinWidth()) {
				shouldRemove = true;
			}
			if (height <= FOXACIDCONFIGURE.getMinHeight()) {
				shouldRemove = true;
			}			
			if (aspect < FOXACIDCONFIGURE.getMinAspect()) {
				shouldRemove = true;
			}			
			if (aspect > FOXACIDCONFIGURE.getMaxAspect()) {
				shouldRemove = true;
			}
			
			if (shouldRemove) {
				badApples[dre] = true;
			}
		}		
		System.out.println("Contours size after: " + contours.size());
		System.out.println("-------------------");
		
		if(contours.size() == 0) {
			try {
				outputTable.putBoolean("foundTower", false);
				outputTable.putNumber("towerXOffset", 0);
				outputTable.putNumber("towerYOffset", 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			heading = outputTable.getNumber("heading", 0);
			angleOfShooter = outputTable.getNumber("angleOfShooter", 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		// only one object found
		if (contours.size() == 1) {
			Rect rec1 = Imgproc.boundingRect(contours.get(0));
			
			int width = rec1.width;
			int height = rec1.height;
			double aspect = (double)width /height;
			
			if (badApples[0]) {
				try {
					outputTable.putBoolean("foundTower", false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return src;
			}
			
			// draw a circle at midpoint
			Point centerPoint = new Point((rec1.tl().x + rec1.br().x) / 2,
					(rec1.tl().y + rec1.br().y) / 2);
			
			Point imageCenter = new Point(resolution[0]/2, resolution[1]/2);
			double xOffset = imageCenter.x - centerPoint.x;
			double yOffset = imageCenter.y - centerPoint.y;
			
			try {
				outputTable.putBoolean("foundTower", true);
				outputTable.putNumber("towerXOffset", xOffset);
				outputTable.putNumber("towerYOffset", yOffset);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Imgproc.rectangle(src, rec1.tl(), rec1.br(),
					new Scalar(255, 255, 0));
			Imgproc.circle(src, centerPoint, 5, new Scalar(0, 0, 255));
						
			// calculate center pixel and display info to screen at 1, 20
			String string = "(only one) Target Found at X: " + (rec1.tl().x + rec1.br().x)
					/ 2 + "Y:" + (rec1.tl().y + rec1.br().y) / 2;
			
			
			Imgproc.putText(src, string, new Point(1, 20),
					Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 255, 0));
			Imgproc.putText(src, "Width: " + width + ", Height: " + height + ", Aspect: " + aspect,
					new Point(1, 50), Core.FONT_HERSHEY_DUPLEX, 1, new Scalar(255, 255, 0));
			String xString = "X: " + (rec1.tl().x + rec1.br().x)
					/ 2;
			String yString = "Y:" + (rec1.tl().y + rec1.br().y) / 2;
			Point center = new Point(rec1.br().x-rec1.width / 2 - 15, (rec1.br().y - rec1.height / 2) - 50);
			Point centerHigher = new Point(rec1.br().x-rec1.width / 2 - 15, (rec1.br().y - rec1.height / 2) - 30);
			
			double distanceCenterX = (rec1.tl().x + rec1.br().x) / 2;
			distanceCenterX = (2 * (distanceCenterX / src.width())) - 1;
			double distanceCenterY = (rec1.tl().y + rec1.br().y) / 2;
			distanceCenterY = -((2 * (distanceCenterY / src.height())) - 1);
			
			double azimuth = this.boundAngle0to360Degrees(distanceCenterX * kHorizontalFOV/2.0 + heading - kShooterOffsetDegX);
			double altitude = this.boundAngle0to360Degrees(distanceCenterY * kVerticalFOV/2.0 + angleOfShooter - kShooterOffsetDegY);
            double range = (kTopTargetHeight - kTopCameraHeight) / Math.tan((distanceCenterY * kVerticalFOV / 2.0 + kCameraAngle)*Math.PI/180.0);

            try {
				outputTable.putNumber("distanceFromTarget", range);
				outputTable.putNumber("azimuth", azimuth);
				outputTable.putNumber("altitude", altitude);
			} catch (Exception e) {
				e.printStackTrace();
			}
            
			Imgproc.putText(src, xString, center, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 255, 0));
			Imgproc.putText(src, yString, centerHigher, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 255, 0));
			return src;
		} else {
			Imgproc.putText(src, "Contours: " + contours.size(), new Point(1, 80), Core.FONT_HERSHEY_DUPLEX, 0.5, new Scalar(255, 255, 0));
			for (int i = 0; i < contours.size(); i++) {
				Rect rec1 = Imgproc.boundingRect(contours.get(i));
				
				int width = rec1.width;
				int height = rec1.height;
				double aspect = (double)width /height;
				
				if (badApples[i]) {
					
				} else {				
					Point centerPoint = new Point((rec1.tl().x + rec1.br().x) / 2,
							(rec1.tl().y + rec1.br().y) / 2);
					
					Point imageCenter = new Point(resolution[0]/2, resolution[1]/2);
					double xOffset = imageCenter.x - centerPoint.x;
					double yOffset = imageCenter.y - centerPoint.y;
					
					try {
						outputTable.putBoolean("foundTower", true);
						outputTable.putNumber("towerXOffset", xOffset);
						outputTable.putNumber("towerYOffset", yOffset);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					double distanceCenterX = (rec1.tl().x + rec1.br().x) / 2;
					distanceCenterX = (2 * (distanceCenterX / src.width())) - 1;
					double distanceCenterY = (rec1.tl().y + rec1.br().y) / 2;
					distanceCenterY = -((2 * (distanceCenterY / src.height())) - 1);
					
					double azimuth = this.boundAngle0to360Degrees(distanceCenterX * kHorizontalFOV/2.0 + heading - kShooterOffsetDegX);
					double altitude = this.boundAngle0to360Degrees(distanceCenterY * kVerticalFOV/2.0 + angleOfShooter - kShooterOffsetDegY);
		            double range = (kTopTargetHeight - kTopCameraHeight) / Math.tan((distanceCenterY * kVerticalFOV / 2.0 + kCameraAngle)*Math.PI/180.0);

		            try {
						outputTable.putNumber("distanceFromTarget", range);
						outputTable.putNumber("azimuth", azimuth);
						outputTable.putNumber("altitude", altitude);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					Point tempCenter = new Point((rec1.tl().x + rec1.br().x) / 2,
							(rec1.tl().y + rec1.br().y) / 2);
					Imgproc.rectangle(src, rec1.tl(), rec1.br(),
							new Scalar(255, 255, 0));
					Imgproc.circle(src, tempCenter, 5, new Scalar(0, 0, 255));
					String xString = "X: " + (rec1.tl().x + rec1.br().x)
						/ 2;
					String yString = "Y:" + (rec1.tl().y + rec1.br().y) / 2;
					Point center = new Point(rec1.br().x-rec1.width / 2 - 15, (rec1.br().y - rec1.height / 2) - 50);
					Point centerHigher = new Point(rec1.br().x-rec1.width / 2 - 15, (rec1.br().y - rec1.height / 2) - 30);
	
					Imgproc.putText(src, xString, centerHigher, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 255, 0));
					Imgproc.putText(src, yString, center, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 255, 0));
					}
			}
			return src;
		}
	}		


	private BGThread bgThread = new BGThread();
	private final int team = DashboardPrefs.getInstance().team.getValue()
			.intValue();
	public final IPAddressProperty ipProperty = new IPAddressProperty(
			this,
			"Camera IP Address",
			new int[] {
					10,
					DashboardPrefs.getInstance().team.getValue().intValue() / 100,
					DashboardPrefs.getInstance().team.getValue().intValue() % 100,
					11 });
	
    public double boundAngle0to360Degrees(double angle)
    {
        while(angle >= 360.0)
        {
            angle -= 360.0;
        }
        while(angle < 0.0)
        {
            angle += 360.0;
        }
        return angle;
    }

	public void init() {
		setPreferredSize(new Dimension(resolution[0], resolution[1]));
		this.ipString = this.ipProperty.getSaveValue();
		this.bgThread.start();
		revalidate();
		repaint();
	}

	public void propertyChanged(Property property) {
		if (property == this.ipProperty) {
			this.ipString = this.ipProperty.getSaveValue();
			this.ipChanged = true;
		}
	}

	public void disconnect() {
		this.bgThread.destroy();
		super.disconnect();
	}

	protected void paintComponent(Graphics g) {
		BufferedImage drawnImage = this.imageToDraw;
		if (drawnImage != null) {
			int width = getBounds().width;
			int height = getBounds().height;
			double scale = Math.min(width / drawnImage.getWidth(), height
					/ drawnImage.getHeight());
			scale = 0.9;
			g.drawImage(drawnImage,
					(int) (width - scale * drawnImage.getWidth()) / 2,
					(int) (height - scale * drawnImage.getHeight()) / 2,
					(int) ((width + scale * drawnImage.getWidth()) / 2.0D),
					(int) (height + scale * drawnImage.getHeight()) / 2, 0, 0,
					drawnImage.getWidth(), drawnImage.getHeight(), null);

			g.setColor(Color.PINK);
			g.drawString("FPS: " + this.lastFPS, 10, 10);
		} else {
			g.setColor(Color.PINK);
			g.fillRect(0, 0, getBounds().width, getBounds().height);
			g.setColor(Color.BLACK);
			g.drawString("NO CONNECTION", 10, 10);
			System.out.println("Image is null.");
		}
	}

}
