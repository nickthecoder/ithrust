/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.util.Property;

public class Path extends Behaviour
{

    @Property(label="From Level")
    public String fromLevelName;
    
    @Property(label="To Level")
    public String toLevelName;

    @Override
    public void init()
    {
        if (!Thrust.game.completedLevel(this.fromLevelName)) {
            getActor().kill();
        }

        if (!Thrust.game.unlockedLevel(this.toLevelName)) {
            getActor().kill();
        }
    }
    
    @Override
    public void tick()
    {
        getActor().deactivate();
    }
    
}
