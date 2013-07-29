/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.Pose;
import uk.co.nickthecoder.jame.Keys;

public class Rod extends Behaviour
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
        rodActor.setBehaviour(this);
        ship.getActor().getLayer().addBelow(rodActor, ship.getActor());
        rodActor.activate();
    }

    @Override
    public void init()
    {
        this.collisionStrategy = Thrust.game.createCollisionStrategy(this.actor);
    }

    @Override
    public void tick()
    {
        this.actor.moveTo(this.ship.getActor());
        this.actor.getAppearance().setDirection(this.ball.getActor());

        double shipBallDistance = this.ship.getActor().distanceTo(this.ball.getActor());

        if (!this.extended) {

            if (Itchy.singleton.isKeyDown(Keys.a)) {
                this.actor.getAppearance().adjustScale(0.01);
                // Has the rod extended far enough to reach the ball?
                if (this.actor.getAppearance().getScale() * this.poseWidth >= shipBallDistance) {
                    this.extended = true;
                    this.ball.event("touched");
                }
            } else {
                this.actor.getAppearance().adjustScale(-0.02);
                if (this.actor.getAppearance().getScale() <= 0) {
                    this.disconnect();
                    return;
                }

            }

            if (shipBallDistance > this.ship.pickupDistance) {
                this.disconnect();
                return;
            }

        } else {

            this.actor.getAppearance().setScale(shipBallDistance / this.poseWidth);

            if (!this.connected) {

                if (this.actor.distanceTo(this.ball.getActor()) >= this.ship.pickupDistance) {
                    this.ball.connected(this);
                    this.connected = true;
                }

            } else {

                this.collisionStrategy.update();
                if (!touching(SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
                    this.disconnect();
                    return;
                }

                double dist = this.actor.distanceTo(this.ball.getActor());
                double dd = dist - this.ship.pickupDistance;

                double angle = this.actor.getAppearance().getDirectionRadians();
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
        this.actor.kill();
    }

}
