/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Behaviour;

public class Liquid extends Behaviour
{

    @Override
    public void init()
    {
        this.collisionStrategy = Thrust.game.createCollisionStrategy(this.actor);
        getActor().addTag("liquid");
        
        if ( getActor().getCostume().getString("water") != null ) {
            getActor().addTag("water");
        }
    }

    @Override
    public void tick()
    {
        getActor().deactivate();
    }
}
