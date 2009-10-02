package raptor;

/**
 * Represents a quadrant to place a RaptorWindowItem. The following is a diagram
 * of the quadrants.
 * 
 * <pre>
 *        |                |
 *        |                |
 *        |    III         |    IV
 *   I    |                | 
 *        |                |             
 *--------|----------------------------------              
 *        |         V              |
 *        |                        |
 *        |-------------------------     VII             
 *        |         VI             |
 *   II   |                        |
 *        |                        |
 * </pre>
 */
public enum Quadrant {
	I, II, III, IV, V
}