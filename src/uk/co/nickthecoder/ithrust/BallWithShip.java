package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.util.DoubleProperty;

/**
 * A Ball with a ship in it. You can switch to the other ship by picking up the ball, then pressing
 * "Q".
 * 
 * As well as the usual Ball attributes, it also has attributes for its ship.
 */
public class BallWithShip extends Ball
{

    public double rotationSpeed = 3;

    public double thrust = 0.1;

    public double shipWeight = 0.1;

    @Override
    public void addProperties()
    {
        addProperty(new DoubleProperty("Ship's Thrust", "thrust"));
        addProperty(new DoubleProperty("Ship's Rotation Speed", "rotationSpeed"));
        addProperty(new DoubleProperty("Ship's Weight", "shipWeight"));
    }
}
