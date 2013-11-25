/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Role;
import uk.co.nickthecoder.itchy.extras.Fragment;
import uk.co.nickthecoder.itchy.role.Explosion;

public class Ball extends AbstractRole implements Fragile
{

    public static final String[] SOLID_TAGS = new String[] { "solid" };

    public static final String[] EXCLUDE_TAGS = new String[] { "gate", "soft" };

    public double weight = 1.0;

    public double landingSpeed = 2.0;

    private boolean moving = false;

    public double speedX = 0;

    public double speedY = 0;

    private Rod rod;

    public int fuel;

    public int water;

    public BallFactory ballFactory = null;

    @Override
    public void onAttach()
    {
        addTag("fragile");
        addTag("solid");
        addTag("ball");

        createFragments();
        getActor().setCollisionStrategy(Thrust.director.createCollisionStrategy(getActor()));

        BallProperties properties = (BallProperties) getActor().getCostume().getProperties();
        this.fuel = properties.fuel;
        this.water = properties.water;
        this.weight = properties.weight;
    }

    public void createFragments()
    {
        new Fragment().actor(getActor()).pieces(10).pose("default").createPoses("fragment");
    }

    @Override
    public void tick()
    {

        if (this.moving) {

            // Damp down the velocity when touching liquids
            if (getActor().pixelOverlap("liquid").size() > 0) {
                this.speedX *= 0.95;
                this.speedY *= 0.95;
                this.speedY += Thrust.gravity / 5; // Make the ball buoyant
            } else {
                this.speedY += Thrust.gravity;
            }

            getActor().moveBy(this.speedX, this.speedY);
            getActor().getCollisionStrategy().update();

            if (!getActor().pixelOverlap(SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
                hit();
                return;
            }

            for (Role role : getActor().pixelOverlap("gate")) {
                Gate gate = (Gate) role;
                getActor().setRole(new EnterGate(gate));
                return;
            }

            if (!getActor().pixelOverlap("soft").isEmpty()) {
                if (this.rod == null) {
                    double speed = Math.sqrt(this.speedX * this.speedX + this.speedY * this.speedY);
                    if (speed < this.landingSpeed) {
                        this.moving = false;
                        getActor().moveBy(-this.speedX, -this.speedY);
                        this.speedX = 0;
                        this.speedY = 0;
                    } else {
                        hit();
                    }
                } else {
                    hit();
                }
            }

        }

    }

    public void connected( Rod rod )
    {
        this.moving = true;
        this.rod = rod;
        this.event("taught");
        if (this.ballFactory != null) {
            this.ballFactory.onTaken();
        }
    }

    public void disconnected()
    {
        event("disconnect");
        this.rod = null;
    }

    public void disconnect()
    {
        if (this.rod != null) {
            this.rod.disconnect();
        }
    }

    @Override
    public void hit()
    {
        if (this.rod != null) {
            this.rod.disconnect();
        }

        new Explosion(getActor())
            .projectiles(30)
            .gravity(Thrust.gravity)
            .fade(0.9, 1.5)
            .speed(0.1, 1.5).vx(this.speedX).vy(this.speedY)
            .pose("fragment")
            .createActor();

        this.deathEvent("death");
    }

    public class EnterGate extends AbstractRole
    {
        private Gate gate;

        private double gateSpeed = 3.0;

        public EnterGate( Gate gate )
        {
            this.gate = gate;
        }

        @Override
        public void onAttach()
        {
            disconnect();
            removeTag("ball");
            event("touchedGate");
        }

        @Override
        public void tick()
        {
            if (this.gateSpeed > 0) {
                Ball.this.speedX *= 0.98;
                Ball.this.speedY *= 0.98;
                getActor().moveBy(Ball.this.speedX, Ball.this.speedY);
                getActor().moveTowards(this.gate.getActor(), this.gateSpeed);
                getActor().getCollisionStrategy().update();

                if (getActor().distance(this.gate.getActor()) < this.gateSpeed) {
                    getActor().moveTo(this.gate.getActor());
                    this.gateSpeed = 0;
                    this.deathEvent("travelGate");
                }
            }

        }

        @Override
        public void onMessage( String message )
        {
            if ("collected".equals(message)) {
                if (this.gate != null) {
                    this.gate.collected(Ball.this);
                }
            }
        }

    }

}
