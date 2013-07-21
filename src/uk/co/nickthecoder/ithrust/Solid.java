/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Behaviour;

public class Solid extends Behaviour {
	
	@Override
	public void onActivate()
	{
        if ( this.actor.getLayer() != Thrust.game.foregroundLayer ) {           
            this.actor.addTag("solid");
            this.collisionStrategy = Thrust.game.createCollisionStrategy(this.actor);
        }
	}
	
	@Override
	public void tick()
	{
		this.actor.deactivate();
	}
	
}
