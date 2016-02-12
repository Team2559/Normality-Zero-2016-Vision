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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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

	public static final double kMorphKernelSize = 1.8;
	public static final int kMinHue = 50;
	public static final int kMinSat = 120;
	public static final int kMinVal = 100;

	public static final int kMaxHue = 100;
	public static final int kMaxSat = 255;
	public static final int kMaxVal = 255;
	
	public static final boolean debug = false;
	
	ITable outputTable;

	static Mat src, dest, image;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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
			URL _url = null;
			URLConnection uc = null;
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
						System.out.println("Successfully drawn image: "
								+ img.getWidth() + " x " + img.getHeight());
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
		ArrayList<Point> towers = new ArrayList<Point>();
		double startTime = System.currentTimeMillis();
		FOXACIDCONFIGURE.minHueLabel.setText("minHue: " + FOXACIDCONFIGURE.minHueSlider.getValue());
		FOXACIDCONFIGURE.maxHueLabel.setText("maxHue: " + FOXACIDCONFIGURE.maxHueSlider.getValue());
		FOXACIDCONFIGURE.minSatLabel.setText("minSat: " + FOXACIDCONFIGURE.minSatSlider.getValue());
		FOXACIDCONFIGURE.maxSatLabel.setText("maxSat: " + FOXACIDCONFIGURE.maxSatSlider.getValue());
		FOXACIDCONFIGURE.minValLabel.setText("minVal: " + FOXACIDCONFIGURE.minValSlider.getValue());
		FOXACIDCONFIGURE.maxValLabel.setText("maxVal: " + FOXACIDCONFIGURE.maxValSlider.getValue());
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

		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint matOfPoint = contours.get(i);
			int width = matOfPoint.width(), height = matOfPoint.height();
			if ((matOfPoint.width() * matOfPoint.height() <= 22)) {
				contours.remove(i);
			} else {
				System.out.println("--------------------");
				System.out.println("Passed Width: " + width);
				System.out.println("Passed Height: " + height);
				System.out.println("Total: " + width * height);
				System.out.println("--------------------");
			}
		}

		if (contours.size() == 1) {
			System.out.println("only 1 contour");
			Rect rec1 = Imgproc.boundingRect(contours.get(0));
			Imgproc.rectangle(src, rec1.tl(), rec1.br(),
					new Scalar(255, 255, 0));
			String string = "Target Found at X: " + (rec1.tl().x + rec1.br().x)
					/ 2 + "Y:" + (rec1.tl().y + rec1.br().y) / 2;
			Imgproc.putText(src, string, new Point(1, 20),
					Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 255, 0));
			
			return src;
		} else {
			System.out.println("Contours: " + contours.size());
			ArrayList<Rect> recList = new ArrayList<Rect>();
			for (MatOfPoint mOP : contours) {
				recList.add(Imgproc.boundingRect(mOP));
			}

			try {
				Point tl = recList.get(0).tl();
				Point br = recList.get(0).br();
				int[] x = new int[recList.size()], y = new int[recList.size()];
				ArrayList<Rect> boxes = new ArrayList<Rect>();
				for (int i = 0; i < recList.size(); i++) {
					tl = recList.get(i).tl();
					br = recList.get(i).br();
					if (new Rect(tl, br).height > 30) {
						boxes.add(new Rect(tl, br));
					}

				}
				Rect currentBox = new Rect(), bestFit = new Rect();
				for (int i = 0; i < boxes.size(); i++) {
					currentBox = boxes.get(i);
					double bestDistance = Double.MAX_VALUE;
					bestFit = null;
					for (Rect r : boxes) {
						if (currentBox == r) {

						} else {
							Point currentBoxCent, loopBoxCent;
							currentBoxCent = new Point(
									(currentBox.tl().x + currentBox.br().x) / 2,
									(currentBox.tl().y + currentBox.br().y) / 2);
							loopBoxCent = new Point((r.tl().x + r.br().x) / 2,
									(r.tl().y + r.br().y) / 2);
							double currToLoopDistance = Math.sqrt(Math.pow(
									Math.abs(loopBoxCent.x - currentBoxCent.x),
									2)
									+ Math.pow(
											Math.abs(loopBoxCent.y
													- currentBoxCent.y), 2));
							if ((currToLoopDistance < bestDistance)) {
								bestFit = r;
								bestDistance = currToLoopDistance;
							}

						}
					}
					if(bestFit != null) {
					Point tl_bf = bestFit.tl();
					Point br_bf = bestFit.br();
					Point tl_cb = currentBox.tl();
					Point br_cb = currentBox.br();
					if (tl_bf.x < tl_cb.x) {
						tl.x = tl_bf.x;
					}
					if (tl_bf.y < tl_cb.y) {
						tl.y = tl_bf.y;
					}
					if (br.x < recList.get(i).br().x) {
						br.x = recList.get(i).br().x;
					}
					if (br.y < recList.get(i).br().y) {
						br.y = recList.get(i).br().y;
					}
					tl.x = Math.min(tl_bf.x, tl_cb.x);
					tl.y = Math.min(tl_bf.y, tl_cb.y);
					br.x = Math.max(br_bf.x, br_cb.x);
					br.y = Math.max(br_bf.y, br_cb.y);
					Imgproc.rectangle(src, tl, br, new Scalar(255, 255, 0));
					Imgproc.circle(src, new Point((tl.x + br.x) / 2,
							(tl.y + br.y) / 2), 5, new Scalar(0, 0, 255));
					towers.add(new Point((tl.x + br.x) / 2, (tl.y + br.y) / 2));
					} else {
						Imgproc.putText(src, "Error: check logs", new Point(320, 240),
								Core.FONT_HERSHEY_TRIPLEX, 1, new Scalar(0, 0, 255));
					}
				}

				Rect bb = new Rect(tl, br);
				// bb.size();
				// System.out.println("IT'S THIS TALL: " + bb.height);
				// Imgproc.rectangle(src, tl, br, new Scalar(255, 255, 0));

				System.out.println("Image processed in "
						+ (System.currentTimeMillis() - startTime) + " ms.");
				if (!debug) {
					try {
						outputTable.putBoolean("foundTote", true);
					} catch (Exception e) {

					}
				}
				Point closestPoint = new Point(999999, 999999);
				Point center = new Point(320, 240);
				for(Point p : towers) {
					double closestToCenter = Math.abs(closestPoint.x - center.x);
					double toteToCenter = Math.abs(p.x - center.x);
					if(toteToCenter < closestToCenter) {
						closestPoint = p;
					}
				}
				double distanceFromCenter = center.x - closestPoint.x;
				if (!debug) {
					try {
						outputTable.putNumber("toteOffset", distanceFromCenter);
					} catch (Exception ae) {
						
					}
				}
				return src;
			} catch (Exception e) {
				Imgproc.putText(src, "Error: check logs", new Point(50, 100),
						Core.FONT_HERSHEY_PLAIN, 1, new Scalar(0, 0, 255));
				System.out.print(e);
				if (!debug) {
					try {
						outputTable.putBoolean("foundTote", false);
					} catch (Exception ae) {

					}
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

	public void init() {
		setPreferredSize(new Dimension(100, 100));
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
			System.out.println("Width: " + width);
			System.out.println("Height: " + height);
		} else {
			g.setColor(Color.PINK);
			g.fillRect(0, 0, getBounds().width, getBounds().height);
			g.setColor(Color.BLACK);
			g.drawString("NO CONNECTION", 10, 10);
			System.out.println("Image is null.");
		}
	}

}
