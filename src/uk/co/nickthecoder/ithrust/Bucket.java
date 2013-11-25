/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.role.Explosion;

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

            if (getActor().pixelOverlap("water").size() > 0) {
                BucketProperties properties = (BucketProperties) getActor().getCostume().getProperties();
                event("fillWithWater");
                this.weight += properties.waterWeight;
                this.water += properties.volume;
                this.empty = false;
            }
        }

        super.tick();
    }

    @Override
    public void hit()
    {

        if (!this.empty) {
            new Explosion(getActor())
                .projectiles(30)
                .gravity(Thrust.gravity)
                .fade(0.9, 1.5)
                .speed(0.1, 1.5).vx(this.speedX).vy(this.speedY)
                .pose("contents")
                .createActor();
        }

        super.hit();

    }
}
