/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.extras.Explosion;
import uk.co.nickthecoder.itchy.extras.Fragment;
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
    public double shipWeight = 2;

    public void createFragments()
    {
        new Fragment().actor(this.getActor()).pieces(10).pose("shell").createPoses("fragment");
        new Fragment().actor(this.getActor()).pieces(4).pose("contents").createPoses("contentsFragment");
    }
    

    public void hit()
    {
        new Explosion(this.getActor())
            .projectiles(12)
            .gravity(Thrust.gravity)
            .forwards()
            .fade(0.9, 1.5)
            .speed(0.1, 1.5).vx(this.speedX).vy(this.speedY)
            .createActor("contentsFragment")
            .activate();
        
        super.hit();
    }
}
