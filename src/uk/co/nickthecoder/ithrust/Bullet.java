/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.extras.Projectile;

public class Bullet extends Projectile implements Fragile
{
    private static final String[] SOLID_TAGS = new String[] { "solid" };

    private static final String[] TARGET_TAGS = new String[] { "fragile" };

    private static final String[] EXCLUDE_TAGS = new String[] { "ship" };

    @Override
    public void init()
    {
        getActor().addTag("fragile");
        this.collisionStrategy = Thrust.game.createCollisionStrategy(getActor());
    }

    @Override
    public void tick()
    {
        super.tick();
        this.collisionStrategy.update();

        for (Actor hit : touching(TARGET_TAGS, EXCLUDE_TAGS)) {
            Behaviour behaviour = hit.getBehaviour();

            if (behaviour instanceof Fragile) {
                ((Fragile) behaviour).hit();
            }
            this.hit();
        }

        if (!touching(SOLID_TAGS).isEmpty()) {
            this.hit();
        }
    }

    @Override
    public void hit()
    {
        getActor().kill();
    }
}
