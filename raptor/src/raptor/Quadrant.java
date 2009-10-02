package raptor;

/**
 * Represents a quadrant to place a RaptorWindowItem. The following is a diagram
 * of the quadrants. The dashes in the below illustration are adjustable
 * sliders.
 * 
 * <pre>
 * 
 *                  I
 * --------------------------------------------
 *        |                |
 *        |    III         |    IV
 *        |                | 
 *        |                |             
 *        |-------------------------------------              
 *   II   |         V              |
 *        |                        |
 *        |-------------------------     VII             
 *        |         VI             |
 *        |                        |
 *        |                        |
 * ---------------------------------------------
 *                   VIII
 * 
 * </pre>
 */
public enum Quadrant {
	I, II, III, IV, V, VI, VII, VIII
}