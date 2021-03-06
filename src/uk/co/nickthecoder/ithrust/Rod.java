/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.Pose;
import uk.co.nickthecoder.jame.event.Keys;

public class Rod extends AbstractRole
{

    public static final String[] SOLID_TAGS = new String[] { "solid" };

    public static final String[] EXCLUDE_TAGS = new String[] { "gate", "ball", "ship" };

    public Ship ship;

    public Ball ball;

    public boolean extended;

    public boolean connected;

    private int poseWidth;

    public Rod( Ship ship, Ball ball )
    {
        super();
        this.ship = ship;
        this.ball = ball;
        this.connected = false;
        this.extended = false;

        Pose pose = ship.getActor().getCostume().getPose("rod");
        Actor rodActor = new Actor(pose);
        this.poseWidth = pose.getSurface().getWidth();
        rodActor.getAppearance().setScale(0.1);
        rodActor.moveTo(ship.getActor());
        rodActor.setRole(this);
        rodActor.setZOrder(0);
        ball.getActor().getStage().add(rodActor);
    }

    @Override
    public void tick()
    {
        getActor().moveTo(this.ship.getActor());
        getActor().setDirection( this.ship.getActor().directionOf(this.ball.getActor()));

        double shipBallDistance = this.ship.getActor().distanceTo(this.ball.getActor());

        if (!this.extended) {

            if (Itchy.isKeyDown(Keys.a)) {
                getActor().getAppearance().adjustScale(0.01);
                // Has the rod extended far enough to reach the ball?
                if (getActor().getAppearance().getScale() * this.poseWidth >= shipBallDistance) {
                    this.extended = true;
                    this.ball.event("touched");
                }
            } else {
                getActor().getAppearance().adjustScale(-0.02);
                if (getActor().getAppearance().getScale() <= 0) {
                    this.disconnect();
                    return;
                }

            }

            if (shipBallDistance > this.ship.pickupDistance) {
                this.disconnect();
                return;
            }

        } else {

            getActor().getAppearance().setScale(shipBallDistance / this.poseWidth);

            if (!this.connected) {

                if (getActor().distanceTo(this.ball.getActor()) >= this.ship.pickupDistance) {
                    this.ball.connected(this);
                    this.connected = true;
                }

            } else {

                getCollisionStrategy().update();
                if (!getCollisionStrategy().collisions(getActor(), SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
                    this.disconnect();
                    return;
                }

                double dist = getActor().distanceTo(this.ball.getActor());
                double dd = dist - this.ship.pickupDistance;

                double angle = getActor().getAppearance().getDirectionRadians();
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                double dx1 = cos * dd;
                double dy1 = sin * dd;

                double ballFactor = this.ship.weight / (this.ball.weight + this.ship.weight);
                double shipFactor = this.ball.weight / (this.ball.weight + this.ship.weight);

                this.ship.getActor().moveBy(dx1 * shipFactor, dy1 * shipFactor);
                this.ship.speedX += dx1 * shipFactor;
                this.ship.speedY += dy1 * shipFactor;

                this.ball.getActor().moveBy(-dx1 * ballFactor, -dy1 * ballFactor);
                this.ball.speedX -= dx1 * ballFactor;
                this.ball.speedY -= dy1 * ballFactor;

            }
        }

    }

    public void disconnect()
    {
        this.ball.disconnected();
        this.ship.disconnected();
        getActor().kill();
    }

}
