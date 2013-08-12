/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.extras.Explosion;
import uk.co.nickthecoder.itchy.extras.Projectile;

public class Droplet extends Projectile
{
    private double oy;

    @Override
    public void init()
    {
        super.init();
        this.collisionStrategy = Thrust.game.createCollisionStrategy(getActor());

    }

    @Override
    public void onActivate()
    {
        this.oy = getActor().getY();
    }

    @Override
    public void tick()
    {
        super.tick();

        this.collisionStrategy.update();

        if ( oy - getActor().getY() > 20 ) {
            if (! touching("solid", "liquid").isEmpty()) {

                new Explosion(this.actor)
                    .projectiles(10)
                    .gravity(Thrust.gravity)
                    .forwards()
                    .fade(0.9, 1.5)
                    .speed(0.1, 1.5).vx(this.vx).vy(-this.vy / 4)
                    .createActor("fragments")
                    .activate();

                deathEvent("death");
                return;
            }
        }
    }
}
