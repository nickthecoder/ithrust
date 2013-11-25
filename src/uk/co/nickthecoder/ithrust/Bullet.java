/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Role;
import uk.co.nickthecoder.itchy.role.Projectile;

public class Bullet extends Projectile implements Fragile
{
    private static final String[] SOLID_TAGS = new String[] { "solid" };

    private static final String[] TARGET_TAGS = new String[] { "fragile" };

    private static final String[] EXCLUDE_TAGS = new String[] { "ship" };

    public Bullet( Actor source )
    {
        super(source);
    }

    @Override
    public void onAttach()
    {
        addTag("fragile");
        getActor().setCollisionStrategy(Thrust.director.createCollisionStrategy(getActor()));
    }

    @Override
    public void tick()
    {
        super.tick();
        getActor().getCollisionStrategy().update();

        for (Role role: getActor().pixelOverlap(TARGET_TAGS, EXCLUDE_TAGS)) {

            if (role instanceof Fragile) {
                ((Fragile) role).hit();
            }
            this.hit();
        }

        if (!getActor().pixelOverlap(SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
            this.hit();
        }
    }

    @Override
    public void hit()
    {
        getActor().kill();
    }
}
