/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.role.Explosion;
import uk.co.nickthecoder.itchy.role.Projectile;

public class Droplet extends Projectile
{
    private double oy;

    public Droplet( Actor source )
    {
        super(source);
    }

    @Override
    public void onBirth()
    {
        super.onAttach();
        getActor().setCollisionStrategy(Thrust.director.createCollisionStrategy(getActor()));
        this.oy = getActor().getY();
    }

    @Override
    public void tick()
    {
        super.tick();

        getActor().getCollisionStrategy().update();

        if (this.oy - getActor().getY() > 20) {
            if (!getActor().pixelOverlap("solid", "liquid").isEmpty()) {

                new Explosion(getActor())
                    .projectiles(10)
                    .gravity(Thrust.gravity)
                    .fade(0.9, 1.5)
                    .speed(0.1, 1.5).vx(this.vx).vy(-this.vy / 4)
                    .pose("fragments")
                    .createActor();

                deathEvent("death");
                return;
            }
        }
    }
}
