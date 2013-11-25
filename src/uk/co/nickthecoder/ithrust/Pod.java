/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.AbstractRole;

public class Pod extends AbstractRole
{
    private static final double SPEED = 0.01;

    private Ship ship;

    private Ball ball;

    private double progress = 0;

    public Pod( Ship ship, Ball ball )
    {
        this.ship = ship;
        this.ball = ball;
    }

    private static double easeInOut( double value )
    {
        if (value < 0.5) {
            return value * value * value * 4;
        } else {
            value = 1 - value;
            return 1 - (value * value * value * 4);
        }
    }

    @Override
    public void tick()
    {
        double value = easeInOut(this.progress);

        double x = this.ship.getActor().getX() * (1 - value) + this.ball.getActor().getX() * value;
        double y = this.ship.getActor().getY() * (1 - value) + this.ball.getActor().getY() * value;

        getActor().moveTo(x, y);
        Thrust.director.centerOn(getActor());

        this.progress += SPEED;
        if (this.progress > 1) {
            this.ship.completeSwitchEnds();
            deathEvent("death");
        }
    }

}
