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
import uk.co.nickthecoder.itchy.util.StringUtils;

public class Door extends Behaviour implements Fragile
{
    @Property(label = "ID")
    public String id = "door1";

    /**
     * The distance the door opens/closes
     */
    @Property(label = "Distance")
    public double distance = 100;

    /**
     * The angle the door opens/closes.
     */
    @Property(label = "Angle (degrees)")
    public double angle = 0;

    @Property(label = "shootable")
    public boolean shootable = true;

    @Property(label = "Close After (s)")
    public double closeAfter = 2;

    @Property(label = "Buddy ID")
    public String buddyId;

    /**
     * The number of ticks it takes to open/close the door.
     */
    @Property(label = "Ticks")
    public int ticks = 100;

    private Door buddy;

    private Recharge closeRecharge;

    /**
     * -1 when returning to its original position, 0 for stationary and 1 for moving away from its
     * original position.
     */
    private int direction = 0;

    /**
     * The amount of ticks away from its start position.
     */
    private int tickCount = 0;

    @Override
    public void init()
    {
        getActor().addTag("door");
        getActor().addTag("solid");
        if (this.shootable) {
            getActor().addTag("fragile");
        }
        this.collisionStrategy = Thrust.game.createCollisionStrategy(getActor());
    }

    @Override
    public void onActivate()
    {
        if (!StringUtils.isBlank(this.buddyId)) {
            for (Actor actor : Actor.allByTag("door")) {
                Door other = (Door) actor.getBehaviour();
                if (this.buddyId.equals(other.id)) {
                    this.buddy = other;
                    break;
                }
            }
        }
    }

    @Override
    public void tick()
    {
        if ((this.closeRecharge != null) && (this.closeRecharge.isCharged())) {
            this.close();
            this.closeRecharge = null;
        }

        if (this.direction != 0) {
            if ((this.tickCount == 0) && (this.direction == -1)) {
                this.direction = 0;
                return;
            }
            if ((this.tickCount == this.ticks) && (this.direction == 1)) {
                this.direction = 0;
                return;
            }

            // The update is in three parts, so that if it is rotated AND translated, then the operations
            // will be done in the reverse order on the way back, ensuring that the door ends up in its
            // original position.
            if (this.direction == 1) {
                getActor().moveForward(this.distance / this.ticks);
            }
            double angle = this.direction * (this.angle / this.ticks);
            getActor().getAppearance().adjustDirection(angle);
            if (this.direction == -1) {
                getActor().moveForward(-this.distance / this.ticks);
            }

            this.tickCount += this.direction;

            this.collisionStrategy.update();
        }
    }

    public void open()
    {
        if (this.direction != 1) {
            this.direction = 1;
            if (this.closeAfter > 0) {
                this.closeRecharge = new Recharge((int) (this.closeAfter * 1000));
                this.closeRecharge.reset();
            }
            if (this.buddy != null) {
                this.buddy.open();
            }
        }
    }

    public void close()
    {
        if (this.direction != -1) {
            this.direction = -1;
            if (this.buddy != null) {
                this.buddy.close();
            }
        }
    }

    public void toggle()
    {
        if (this.direction == 0) {
            if (this.tickCount == 0) {
                this.open();
            } else {
                this.close();
            }
        } else {
            if (this.direction == 1) {
                this.close();
            } else {
                this.open();
            }
        }
    }

    @Override
    public void hit()
    {
        if (this.shootable) {
            toggle();
        }
    }
}