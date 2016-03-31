package edu.normality.smartdashboard.foxacidconfigure;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import flanagan.analysis.Normality;
import flanagan.interpolation.CubicSpline;

/**
 * Data to create splines to calculate the angle to shoot at for a given
 * distance
 * 
 * @author Andy
 *
 */
public class ShooterData {
	// The min and max height we can aim at
	public final static int MIN_HIT_HEIGHT = 85;
	public final static int MAX_HIT_HEIGHT = 100;

	// The height and width of the window and the size of the ball
	private final static double targetStartY = 97;
	private final static double targetEndY = 117;
	// We want to hit the center X of window so we want 1/2 of width each side
	private final static double targetStartX = -10;
	private final static double targetEndX = 10;
	private final static double ballDiam = 5;

	// What distance was the data captured at
	private final static double dataCaptureDist = 120;

	// For calculating probability of shot
	private final static SummaryStatistics statsX = new SummaryStatistics();
	private final static SummaryStatistics statsY = new SummaryStatistics();

	// The initial splines we will calculate values from
	// This is real/observed data
	private final static ArrayList<ShootDistanceDataPoint> allData = new ArrayList<ShootDistanceDataPoint>();
	private final static ShootDistanceDataPoint dist190 = new ShootDistanceDataPoint(190);
	private final static ShootDistanceDataPoint dist176 = new ShootDistanceDataPoint(176);
	private final static ShootDistanceDataPoint dist164 = new ShootDistanceDataPoint(164);
	private final static ShootDistanceDataPoint dist136 = new ShootDistanceDataPoint(136);
	private final static ShootDistanceDataPoint dist114 = new ShootDistanceDataPoint(114);
	private final static ShootDistanceDataPoint dist92 = new ShootDistanceDataPoint(92);
	private final static ShootDistanceDataPoint dist82 = new ShootDistanceDataPoint(82);

	// A hash map of hit height to spline: I.e.
	// hit 90 inches: spline(dist) -> angle
	// hit 91 inches: spline(dist) -> angle
	// Users will pass the hit height they want and the distance they are at.
	// We will pick the correct spline and feed it the distance so that it can
	// calculate the angle
	private final static HashMap<Integer, CubicSpline> hitHeightDistAngleSpline = new HashMap<Integer, CubicSpline>();

	static {
		/**
		 * First we will enter all of the observed data we have about shots.
		 * This tells us at what height the ball hit the wall with a given
		 * shooter angle at a given distance
		 */
		// Add the data when we were 190 away from target
		dist190.addDataPoint(34.166, 109);
		dist190.addDataPoint(32.145, 96);
		dist190.addDataPoint(29.596, 92.5);
		dist190.addDataPoint(25.817, 71);

		dist176.addDataPoint(30.299, 82.5);
		dist176.addDataPoint(32.145, 91);
		dist176.addDataPoint(33.463, 94.5);
		dist176.addDataPoint(34.869, 104);
		dist176.addDataPoint(36.627, 108);

		dist164.addDataPoint(30.299, 88);
		dist164.addDataPoint(29.596, 80);
		dist164.addDataPoint(35.221, 101);
		dist164.addDataPoint(37.594, 110);

		dist136.addDataPoint(36.276, 96);
		dist136.addDataPoint(35.924, 97);
		dist136.addDataPoint(36.891, 101.5);
		dist136.addDataPoint(36.891, 79.5);
		dist136.addDataPoint(39.703, 104);

		dist114.addDataPoint(39.703, 98.5);
		dist114.addDataPoint(35.571, 82.5);
		dist114.addDataPoint(37.242, 90);
		dist114.addDataPoint(41.416, 104);
		dist114.addDataPoint(43.219, 111);

		dist92.addDataPoint(43.17, 93);
		dist92.addDataPoint(39.703, 82);
		dist92.addDataPoint(36.891, 76);
		dist92.addDataPoint(38.297, 80);
		dist92.addDataPoint(41.11, 88);
		dist92.addDataPoint(46.383, 101);

		dist82.addDataPoint(44.977, 92);
		dist82.addDataPoint(46.735, 96);
		dist82.addDataPoint(50.162, 104);
		dist82.addDataPoint(52.65, 112);
		dist82.addDataPoint(42.067, 80.5);
		dist82.addDataPoint(43.219, 86);

		allData.add(dist190);
		allData.add(dist176);
		allData.add(dist164);
		allData.add(dist136);
		allData.add(dist114);
		allData.add(dist92);
		allData.add(dist82);

		/**
		 * Now we create the splines for each distance. Our X will be height,
		 * and our Y will be the shooter angle. This allows use to enter a
		 * height and the spline will tell us at what angle the shooter needs to
		 * be. I.e.:
		 * 
		 * <pre>
		 * 1. We are at 175 distance
		 * 2. We want the ball to hit at 97 inches as that is the middle of the window
		 * 3. We can use the 175 spline.interpolate(97)
		 *    - even though we do not have the data to hit 97 inches, the spline
		 *      will estimate the angle based on the heights we did hit at different
		 *      angles
		 * </pre>
		 */
		// Create the splines for each distance
		for (final ShootDistanceDataPoint dp : allData)
			dp.generateSpline();

		/**
		 * We now have splines for each of the distances measured. I.e. if I am
		 * at 190 away from the target I can calculate the angle to hit at 90
		 * inches, or 95 inches, or 100 inches, etc. I can also calculate all of
		 * the angles to hit those heights at 175 away as we also have that
		 * data.
		 * 
		 * However, what if I'm 180 away? We don't have the data for that. So
		 * what we are going to do is interpolate from the splines we do have.
		 * We will calculate the angles needed to hit various heights using all
		 * of the splines we have, then we will interpolate what the splines
		 * in-between look like
		 * 
		 * Like: Dist/Hit height 90 91 92 93 94 (inches we want to hit) 190 30
		 * 34 36 38 40 (degrees to hit at target height) 175 40 45 50 55 60
		 * 
		 * Then we interpolate 180 as something like: 180 36 43 46 52 58
		 * 
		 * So the splines we will create to interpolate "180" will be a spline
		 * for each hit height. We are asking, given that I want to hit at
		 * height 90 and I am at 180 away what angle do I need to shoot at? We
		 * repeat this for each hit height MIN_HIT_HEIGHT - MAX_HIT_HEIGHT to
		 * generate the 180 spline.
		 * 
		 * NOTE: We could generate this data each time we get a new distance
		 * from target, but as we have static shooting data we can just generate
		 * it once and use it for the entire run. In a move complex system you
		 * could feedback the data about your shots as you make them so that the
		 * system learns as it makes more shots
		 */
		// Use each spline to interpolate the angles needed to hit various hit
		// heights
		// for (final ShootDistanceDataPoint dp : allData)
		// dp.interpolateHeightAngleData(MIN_HIT_HEIGHT, MAX_HIT_HEIGHT);

		// Use the interpolated data to generate a dist->angle spline for each
		// height we might want to hit
		for (int i = MIN_HIT_HEIGHT; i <= MAX_HIT_HEIGHT; i++) {
			final int hitHeight = i;
			final double[] distFromTarget = new double[allData.size()];
			final double[] angleToShoot = new double[allData.size()];

			// Get the interpolated
			for (int currentDp = 0; currentDp < allData.size(); currentDp++) {
				final ShootDistanceDataPoint dp = allData.get(currentDp);
				distFromTarget[currentDp] = dp.getDistance();
				angleToShoot[currentDp] = dp.getSpline().interpolate(hitHeight);
			}

			final CubicSpline distToAngleForGivenHeight = new CubicSpline(distFromTarget, angleToShoot);
			hitHeightDistAngleSpline.put(hitHeight, distToAngleForGivenHeight);
		}

		/**
		 * Data for variance of where ball hits. I.e. how accurate are we? How
		 * likely is the ball to go in?
		 */
		// For this the distance and angle needs to be the same
		// The height error the ball hit when at the same distance and angle
		// I.e. if you aimed for 97 and it hit at 96, then -1
		double[] dataForY = new double[] { 0, -2, 1, -0.5, -1, 2, 1, 0 };

		// Create Y dist for hitting
		for (double d : dataForY)
			statsY.addValue(d);

		// The amount the ball way left/right of center when the ball hit
		double[] dataForX = new double[] { 0, -2, 1, -0.5, -1, 2, 1, 0 };

		// Create the X distribution for hitting
		for (double d : dataForX)
			statsX.addValue(d);

	}

	/**
	 * Calculate the angle to shoot at for a given distance and height
	 * 
	 * @param distance
	 *            To the tower
	 * @param hitHeight
	 *            How high up the tower to hit
	 * @return The angle to set the shooter
	 * @throws ArrayIndexOutOfBoundsException
	 *             If you are trying to hit a height we cannot calculate, or if
	 *             you are too close or far away
	 */
	public static double getShooterAngle(final double distance, final int hitHeight)
			throws ArrayIndexOutOfBoundsException {
		// If we do not have a spline to hit the tower at that height then we
		// cannot calculate it
		if (!hitHeightDistAngleSpline.containsKey(hitHeight)) {
			System.err.println("ERROR: We do not have a split to hit the tower at " + hitHeight);
			throw new ArrayIndexOutOfBoundsException("We do not have a split to hit the tower at " + hitHeight);
		}

		final CubicSpline cs = hitHeightDistAngleSpline.get(hitHeight);

		// If the distance is out of bounds then we cannot estimate
		if (distance < cs.getXmin())
			throw new ArrayIndexOutOfBoundsException("We are too close to calculate the angle: " + distance);
		if (distance > cs.getXmax())
			throw new ArrayIndexOutOfBoundsException("We are too far to calculate the angle: " + distance);

		return cs.interpolate(distance);
	}

	public static double hitChance(double distanceFromTarget, double targetAngleXError, double targetAimHeight,
			double targetAngleYError) {
		// First calculate the probability we get the ball in the target with
		// regards to our X (left/right) position. What matters here is how far
		// away we are and what error there is in us aligning with the target

		// A is the angle error the shooter is at for left/right
		double A = Math.abs(targetAngleXError);
		double Arad = Math.toRadians(A);

		// B is at the tower, it's flat so a 90 degree angle of distance we will
		// be off
		double B = 90;
		double Brad = Math.toRadians(B);

		// C is the angle at the end of the error
		double C = (180 - A) - B;
		double Crad = Math.toRadians(C);

		// c is the distance from the tower
		double c = distanceFromTarget;

		// System.out.println("c " + c + " crad " + Crad + " C " + C + " Arad "
		// + " A ");
		double xErrorDistAtTarget = ShooterAngleError.saeSinRuleTwoAng(Crad, c, Arad);

		// Now we have the error size at the target we can calculate the
		// probability we hit it
		// The probability that the ball goes in is:
		// - Where the center of the ball will hit
		// - Plus the diameter of the ball
		// - Plus the distribution of the variance in the shooting X
		// Let's start that the ball is 0 size and hits the middle
		double xEdge = 0;

		// Add the error due to the X angle error
		xEdge += xErrorDistAtTarget;

		// Add the width of the ball
		xEdge += ballDiam;

		// We will now calculate the probability that the ball goes in. Normally
		// i would have to hit between the windows, to account for the shooting
		// X error we will shift the window to the side

		// Create a dist accounting for distance, i.e. further away is more
		// difficult as error variance will be multiplied at distance. I.e. if
		// it was 1 inch of at 10 feet, at 20 feet back the same shot would be 2
		// inches off target
		double distanceFactor = distanceFromTarget / dataCaptureDist;
		NormalDistribution xShotDist = new NormalDistribution(statsX.getMean(), statsX.getVariance() * distanceFactor);

		// As we did Math.abs(error_angle) the error will always offset in the
		// same direction
		System.out.println("X Distance error at tower: " + xErrorDistAtTarget + " inches");
		double xHitProb = xShotDist.probability(targetStartX - xEdge, targetEndX - xEdge);
		System.out.println("X Probability of scoring: " + xHitProb + " I.e. " + xHitProb * 100 + "%");

		// We can also do a similar thing with Y distribution
		double yA = Math.abs(targetAngleYError);
		double yArad = Math.toRadians(yA);
		double yB = 90;
		double yBrad = Math.toRadians(yB);
		double yC = (180 - yA) - yB;
		double yCrad = Math.toRadians(yC);
		double yc = distanceFromTarget;
		double yErrorDistAtTarget = ShooterAngleError.saeSinRuleTwoAng(yCrad, yc, yArad);

		double yEdge = 0;
		yEdge += yErrorDistAtTarget;
		yEdge += ballDiam;

		// change back to right sign
		if (targetAngleYError < 0)
			yEdge = yEdge * -1.0;

		NormalDistribution yShotDist = new NormalDistribution(statsY.getMean(), statsY.getVariance() * distanceFactor);
		// Off set window with aim higher, and then Y error
		double yHitProb = yShotDist.probability((targetStartY - targetAimHeight) + yEdge,
				(targetEndY - targetAimHeight) + yEdge);
		System.out.println("Y Distance error at tower: " + yErrorDistAtTarget + " inches");
		System.out.println("Y Probability of scoring: " + yHitProb + " I.e. " + yHitProb * 100 + "%");

		return xHitProb * yHitProb;
	}

	/**
	 * Example of what is happening
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// This is the first thing that will happen. For a distance we have
		// measured we can get the angle to hit a certain height
		System.out.println("Example spline for when we are 190 away, what angle to hit different heights:");
		for (double hitHeight = 90; hitHeight < 100.0; hitHeight++)
			System.out.println(
					"To hit at height " + hitHeight + " we need angle: " + dist190.getSpline().interpolate(hitHeight));

		// What splines did we generate?
		System.out.println("We generated splines for these hit heights: ");
		for (Integer i : hitHeightDistAngleSpline.keySet()) {
			System.out.println("For hit height: " + i + " to hit at distance 170 we need angle : "
					+ hitHeightDistAngleSpline.get(i).interpolate(170.0));
		}
		System.out.println();

		// The splines can only calculate at the mins and max of the data we
		// gave them
		System.out.println("The splines have these mins and maxes: ");
		for (Integer i : hitHeightDistAngleSpline.keySet()) {
			System.out.print("For hit height: " + i);
			hitHeightDistAngleSpline.get(i).displayLimits();
		}
		System.out.println();

		// At the end we can choose a height we want to hit, like 97, and get
		// the angle we need to shoot at given the distance we are away
		int heightToHit = 100;
		CubicSpline cs = hitHeightDistAngleSpline.get(heightToHit);

		System.out.println(
				"Example spline for when we want to hit a certain height at a certain distance, what angle is needed:");
		System.out.println("Compare these to the observed values to see if you think it's working");
		for (double distance = 165; distance <= 175; distance += 5)
			System.out.println("To hit at height " + heightToHit + " at distance " + distance + " we need angle: "
					+ cs.interpolate(distance));

		heightToHit = 90;
		cs = hitHeightDistAngleSpline.get(heightToHit);

		System.out.println(
				"\nExample spline for when we want to hit a certain height at a certain distance, what angle is needed:");
		System.out.println("Compare these to the observed values to see if you think it's working");
		for (double distance = 165; distance <= 175; distance += 5)
			System.out.println("To hit at height " + heightToHit + " at distance " + distance + " we need angle: "
					+ cs.interpolate(distance));

		// Example of what vision will do:
		// Exact data point from Excel is : 164.084 , 88 needed angle 30.299
		// We want to hit tower at 88 inches and we are 164.084 away
		try {
			System.out.println("\nWe are 164.084 away trying to hit 88 (30.299 observed angle). Estimate angle: "
					+ getShooterAngle(164.084, 88));
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("We don't have the data to calculate the angle");
		}
		// Example prob of hit from 10 feet away
		// 1* error
		System.out.println("Total hit chance: " + hitChance(10 * 12, 1, 107, 1));
		// 2* error
		System.out.println("Total hit chance: " + hitChance(10 * 12, 2, 107, 2));
		// 4* error
		System.out.println("Total hit chance: " + hitChance(10 * 12, 4, 107, 4));

		// Get better if we are closer
		// 2* error far
		System.out.println("Total hit chance: " + hitChance(15 * 12, 2, 107, 2));
		// 2* error close
		System.out.println("Total hit chance: " + hitChance(5 * 12, 2, 107, 2));

	}
}
