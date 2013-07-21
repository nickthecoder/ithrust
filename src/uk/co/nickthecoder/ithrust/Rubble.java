/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.extras.Pieces;

public class Rubble extends Pieces implements Fragile
{
    public void onActivate()
    {
        super.onActivate();
        if (!getActor().isDead()) {
            getActor().addTag("fragile");
            getActor().addTag("solid");
            this.collisionStrategy = Thrust.game.createCollisionStrategy(getActor());
        }
    }
    
    public void hit()
    {
        getActor().kill();
    }
}
