/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.extras.Explosion;

/**
 * A container, which can pick up liquids
 */
public class Bucket extends Ball
{
    private boolean empty = true;

    @Override
    public void tick()
    {
        if (this.empty) {

            if (touching("water").size() > 0) {
                System.out.println("Filling bucket with water");
                event("fillWithWater");
                this.weight += getActor().getCostume().getInt("waterWeight", 2);
                this.water += getActor().getCostume().getInt("volume", 1);
                empty = false;
            }
        }

        super.tick();
    }
    
    @Override
    public void hit()
    {
        
        if ( ! empty ) {
            new Explosion(this.actor)
            .projectiles(30)
            .gravity(Thrust.gravity)
            .forwards()
            .fade(0.9, 1.5)
            .speed(0.1, 1.5).vx(this.speedX).vy(this.speedY)
            .createActor("contents")
            .activate();

        }

        super.hit();

    }
}
