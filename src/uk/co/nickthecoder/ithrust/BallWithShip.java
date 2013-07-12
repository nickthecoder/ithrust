package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.util.Property;

/**
 * A Ball with a ship in it. You can switch to the other ship by picking up the ball, then pressing
 * "Q".
 * 
 * As well as the usual Ball attributes, it also has attributes for its ship.
 */
public class BallWithShip extends Ball
{

    @Property(label="Ship's Rotation Speed")
    public double rotationSpeed = 3;

    @Property(label="Ship's Thrust")
    public double thrust = 0.1;

    @Property(label="Ship's Weight")
    public double shipWeight = 0.1;

    
}
