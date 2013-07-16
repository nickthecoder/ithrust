/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;

public class Solid extends Behaviour {

	@Override
	public void init()
	{
		this.actor.addTag("solid");
	}
	
	@Override
	public void tick()
	{
		this.actor.deactivate();
	}
	
	public void hit( Actor other )
	{	
	}
}
