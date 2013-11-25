/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.extras.Timer;
import uk.co.nickthecoder.itchy.property.Property;

public class DropletFactory extends AbstractRole
{
    @Property(label = "Minimum Period (s)")
    public double minimumPeriod = 1;

    @Property(label = "Maximum Period (s)")
    public double maximumPeriod = 4;

    private Timer timer;

    @Override
    public void onAttach()
    {
        this.timer = Timer.createTimerSeconds(this.minimumPeriod, this.maximumPeriod);
        getActor().getAppearance().setScale(this.timer.getProgress());
    }

    @Override
    public void tick()
    {
        if (this.timer.isFinished()) {
            this.timer.reset();

            createDroplet();
        } else {
            getActor().getAppearance().setScale(this.timer.getProgress());
        }
    }

    public void createDroplet()
    {
        Droplet droplet = new Droplet(getActor());
        Actor actor = droplet.createActor();

        droplet.gravity = Thrust.gravity;
        actor.setRole(droplet);
        actor.moveTo(getActor());
    }
}
