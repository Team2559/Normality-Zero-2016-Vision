package edu.normality.smartdashboard.foxacidconfigure;

/**
 * Calculate the error in the robots angle given that we are aligning the camera
 * and not the shooter. We have a triangle of three objects
 * 
 * <pre>
 * A = the shooter - use the center of the ball position
 * B = the camera - use the position of the sensor
 * C = the target - the center of the tower window
 * </pre>
 * 
 * @author Andy Turner
 *
 */
public class ShooterAngleError {
	// distance from A to B is side c (opposite c) - center shooter to camera
	private static final double c = 8;

	// BC = distance from camera to tower - will be provided by user in function
	// AC = distance from center shooter to tower - is unknown

	// The angle at B. I think this is static? A and B are always the same
	// distance part, and C is always directly in front of B. I.e. imagine both
	// shooter and camera were both right at the front of the robot then there
	// is always a 90 degree angle once the tower is aligned with the camera
	//
	// If the shooter is behind the camera then the angle is larger. i.e. > 90
	// If the camera is behind the shooter then the angle is smaller. i.e. < 90

	// I'm assuming the shooter is a bit behind the camera
	private static double B = 90;
	private static double Brad = Math.toRadians(B);

	// A = local angle between shooter and tower. Will be larger the further
	// away the tower. Unknown
	//
	// C = the angle error between the camera looking at the tower and the
	// shooter looking at the tower. The angle will be smaller as the robot is
	// further away. This is because the angle will have more effect when
	// further away as the total difference in horizontal travel will be the
	// angle * distance
	//

	/**
	 * Get the angle error at the tower between the camera being aligned and the
	 * shooter being aligned
	 * 
	 * @param distToTower
	 *            Distance to tower in same units as shooter-camera units
	 *            (inches)?
	 * @return The angle error at the tower
	 */
	public static double calculateError(final double distToTower) {
		// Distance to tower (between B and C is opposite a)
		final double a = distToTower;

		// Find length of side b
		final double b = saeCosRule(a);

		// We want to calculate the angle error at C
		final double C = saeSinRule(b);

		// Return the number of degrees
		return Math.toDegrees(C);
	}

	private static double saeCosRule(final double a) {
		// a² = b² + c² - 2bc cosA
		return Math.sqrt((a * a) + (c * c) - (2 * a * c * Math.cos(Brad)));
	}

	private static double saeSinRule(final double b) {
		// a/sin(A) = b/sin(B)
		// sin B = (b sin(A)) / a
		return Math.asin((c * Math.sin(B)) / b);
	}

	public static void main(String args[]) {
		System.out.println("For testing");
		System.out.println("When 5 feet away b dist is: " + saeCosRule(5*12));
		System.out.println("When 8 feet away b dist is: " + saeCosRule(8*12));
		System.out.println("When 15 feet away b dist is: " + saeCosRule(15*12));

		System.out.println("When 5 feet away C is: " + calculateError(5*12));
		System.out.println("When 8 feet away C is: " + calculateError(8*12));
		System.out.println("When 15 feet away C is: " + calculateError(15*12));
	}
}
