package raptor;

/**
 * Represents a quadrant to place a RaptorWindowItem. The following is a diagram
 * of the quadrants.
 * 
 * <pre>
 * --------------------------------------------
 *                  II
 * --------------------------------------------
 *        |                |
 *        |    III         |    IV
 *        |                | 
 *        |                |             
 *        |-------------------------------------              
 *   I    |         V              |
 *        |                        |
 *        |-------------------------     VII             
 *        |         VI             |
 *        |                        |
 *        |                        |
 * ---------------------------------------------
 *                   VIII
 * ---------------------------------------------
 * </pre>
 */
public enum Quadrant {
	I, II, III, IV, V
}