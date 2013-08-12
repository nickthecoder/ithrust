/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.extras.Recharge;
import uk.co.nickthecoder.itchy.util.Property;

public class DropletFactory extends Behaviour
{
    @Property(label = "Minimum Period (s)")
    public double minimumPeriod = 1;

    @Property(label = "Maximum Period (s)")
    public double maximumPeriod = 4;

    private Recharge recharge;

    @Override
    public void init()
    {
        this.recharge = Recharge.createRechargeSeconds(this.minimumPeriod, this.maximumPeriod);
        getActor().getAppearance().setScale(this.recharge.getCharge());
    }

    @Override
    public void tick()
    {
        if (this.recharge.isCharged()) {
            this.recharge.reset();

            createDroplet();
        } else {
            getActor().getAppearance().setScale(this.recharge.getCharge());
        }
    }

    public void createDroplet()
    {
        Actor actor = new Actor(getActor().getCostume());
        Droplet droplet = new Droplet();
        droplet.gravity = Thrust.gravity;
        actor.setBehaviour(droplet);
        actor.moveTo(getActor());
        getActor().getLayer().addAbove(actor, getActor());
        actor.activate();
    }
}
