package raptor.util;

import java.awt.Polygon;

public class PolygonUtils {

	/**
	 * Returns an SWT polygon rotated from the passed in java.awt.Polygon.
	 */
	public static int[] rotate(Polygon polygon, double theta, int anchorX,
			int anchory) {

		// AffineTransform transform =
		// AffineTransform.getRotateInstance(theta);//,
		// // anchorX, anchory);
		// PathIterator pathIterator = polygon.getPathIterator(transform);
		//		
		// int[] result = new int[polygon.xpoints.length * 2];
		// for (int i = 0; i < polygon.xpoints.length; i++) {
		// double[] doubles = new double[6];
		// int seg = pathIterator.currentSegment(doubles);
		// if (seg == PathIterator.SEG_MOVETO) {
		// System.err.println("Move to " + doubles[0] + "," + doubles[1]);
		// // result[2*i] = (int) doubles[0];
		// // result[2*i + 1] = (int) doubles[1];
		// }
		// if (seg == PathIterator.SEG_LINETO) {
		// System.err.println("Line to " + doubles[0] + "," + doubles[1]);
		// result[2*i] = (int) doubles[0];
		// result[2*i + 1] = (int) doubles[1];
		// }
		// if (seg == PathIterator.)
		// }

		// int[] result =
		// for (int i = 0; i < transformed.xpoints.length; i++) {
		// result[2 * i] = transformed.xpoints[i];
		// result[2 * i + 1] = transformed.ypoints[i];
		// }
		//
		return null;
	}

}
