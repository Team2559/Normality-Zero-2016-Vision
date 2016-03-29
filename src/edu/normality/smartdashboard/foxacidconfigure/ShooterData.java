package edu.normality.smartdashboard.foxacidconfigure;

import java.util.ArrayList;
import java.util.HashMap;

import flanagan.interpolation.CubicSpline;

/**
 * Data to create splines to calculate the angle to shoot at for a given
 * distance
 * 
 * @author Andy
 *
 */
public class ShooterData {
	public final static int MIN_HIT_HEIGHT = 85;
	public final static int MAX_HIT_HEIGHT = 100;

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

	/**
	 * Example of what is happening
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
	}
}
