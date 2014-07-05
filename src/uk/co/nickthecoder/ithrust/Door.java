/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Role;
import uk.co.nickthecoder.itchy.extras.Timer;
import uk.co.nickthecoder.itchy.property.Property;
import uk.co.nickthecoder.itchy.util.StringUtils;

public class Door extends AbstractRole implements Fragile
{
    @Property(label = "ID")
    public String id = "door1";

    /**
     * The distance the door opens/closes
     */
    @Property(label = "Slide")
    public double slide = 100;

    @Property(label = "Lift")
    public double lift = 0;

    /**
     * The angle the door opens/closes.
     */
    @Property(label = "Angle (degrees)")
    public double angle = 0;

    @Property(label = "shootable")
    public boolean shootable = true;

    @Property(label = "Close After (s)")
    public double closeAfter = 0;

    @Property(label = "Buddy ID")
    public String buddyId;

    /**
     * The number of ticks it takes to open/close the door.
     */
    @Property(label = "Ticks")
    public int ticks = 100;

    /**
     * The number of ticks from the initial position to the position needed for an escape pod to get
     * through.
     */
    @Property(label = "ajar")
    public int ajar = 30;

    private Door buddy;

    private Timer closeTimer;

    /**
     * -1 when returning to its original position, 0 for stationary and 1 for moving away from its
     * original position.
     */
    private int direction = 0;

    /**
     * The amount of ticks away from its start position.
     */
    private int tickCount = 0;

    /**
     * The target position of the door. When target == tickCount, then the door will stop moving.
     */
    private int target = 0;

    @Override
    public void onAttach()
    {
        addTag("door");
        addTag("solid");
        if (this.shootable) {
            addTag("fragile");
        }

        if (!StringUtils.isBlank(this.buddyId)) {
            for (Role role : AbstractRole.allByTag("door")) {
                Door other = (Door) role;
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
        if ((this.target != this.ajar) && (this.closeTimer != null) &&
            (this.closeTimer.isFinished())) {

            this.close();
            this.closeTimer = null;
        }

        if (this.target == this.tickCount) {
            this.direction = 0;
            return;
        }

        if (this.direction != 0) {

            // The update is in three parts, so that if it is rotated AND translated, then the
            // operations
            // will be done in the reverse order on the way back, ensuring that the door ends up in
            // its
            // original position.
            if (this.direction == 1) {
                getActor().moveForwards(this.slide / this.ticks, this.lift / this.ticks);
            }
            double angle = this.direction * (this.angle / this.ticks);
            getActor().getAppearance().adjustDirection(angle);
            if (this.direction == -1) {
                getActor().moveForwards(-this.slide / this.ticks, -this.lift / this.ticks);
            }

            this.tickCount += this.direction;

            getCollisionStrategy().update();
        }
    }

    public void open()
    {
        if (this.direction != 1) {
            this.direction = 1;
            this.target = this.ticks;
            if (this.closeAfter > 0) {
                this.closeTimer = Timer.createTimerSeconds(this.closeAfter);
                this.closeTimer.reset();
            }
            if (this.buddy != null) {
                this.buddy.open();
            }
        }
    }

    public void close()
    {
        if (this.direction != -1) {
            this.target = 0;
            this.direction = -1;
            if (this.buddy != null) {
                this.buddy.close();
            }
        }
    }

    public void ajar()
    {
        this.target = this.ajar;
        if (this.target == this.tickCount) {
            this.direction = 0;
        } else {
            this.direction = this.tickCount < this.target ? 1 : -1;
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
    public void onMessage( String message )
    {
        if ("escapePod".equals(message)) {
            this.ajar();
        } else if ("reset".equals(message)) {
            this.close();
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
