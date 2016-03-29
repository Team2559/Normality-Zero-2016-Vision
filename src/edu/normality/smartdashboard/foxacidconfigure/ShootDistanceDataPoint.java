package edu.normality.smartdashboard.foxacidconfigure;

import java.util.ArrayList;

import flanagan.interpolation.CubicSpline;

/**
 * The data the was recorded in the workshop for the distance/height/angle from
 * the robot shooter
 * 
 * @author Andy
 *
 */
public class ShootDistanceDataPoint {
	// The real observed data
	private final double distance;
	private final ArrayList<Double> angleData = new ArrayList<Double>();
	private final ArrayList<Double> heightData = new ArrayList<Double>();

	// The spline of the observed data
	private CubicSpline spline;

	/**
	 * Create a new data object for a given distance
	 * 
	 * @param distance
	 *            The distance to the tower
	 */
	public ShootDistanceDataPoint(final double distance) {
		this.distance = distance;
	}

	/**
	 * Add a data point for the angle the shooter was at and where the ball
	 * ended up hitting
	 * 
	 * @param angle
	 *            The angle the shooter was at
	 * @param height
	 *            The height the ball hit at
	 */
	public void addDataPoint(final double angle, final double height) {
		angleData.add(angle);
		heightData.add(height);
	}

	/**
	 * Get the distance these data points are for
	 * 
	 * @return The distance from the tower
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * Get the angles that we shot at
	 * 
	 * @return The angles the shooter was at
	 */
	public double[] getAngleDataArr() {
		final double[] ret = new double[angleData.size()];
		int i = 0;

		for (final Double d : angleData)
			if (d != null)
				ret[i++] = d;

		return ret;
	}

	/**
	 * Get the heights that the ball hit at
	 * 
	 * @return The heights the basll hit the tower at
	 */
	public double[] getHeightDataArr() {
		final double[] ret = new double[heightData.size()];
		int i = 0;

		for (final Double d : heightData)
			if (d != null)
				ret[i++] = d;

		return ret;
	}

	/**
	 * Get the spline for this object to calculate the angle needed to hit a
	 * certain height given that we are at this.distance from the tower
	 * 
	 * @return The height->angle spline for this distance
	 */
	public CubicSpline getSpline() {
		return spline;
	}

	/**
	 * Generate the spline object for the data that has been entered so far
	 */
	public void generateSpline() {
		this.spline = new CubicSpline(this.getHeightDataArr(), this.getAngleDataArr());
	}
}
