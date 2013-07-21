/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.extras.Explosion;
import uk.co.nickthecoder.itchy.extras.Fragment;
import uk.co.nickthecoder.itchy.extras.Recharge;
import uk.co.nickthecoder.itchy.util.Property;

public class Ball extends Behaviour implements Fragile
{
    public static final String[] SOLID_TAGS = new String[] { "solid" };

    public static final String[] EXCLUDE_TAGS = new String[] { "gate" };

    @Property(label = "Weight")
    public double weight = 1.0;

    private boolean moving = false;

    public double speedX = 0;

    public double speedY = 0;

    private Rod rod;

    private Gate gate;

    private double gateSpeed = 3.0;
    
    @Override
    public void init()
    {
        this.actor.addTag("fragile");
        this.actor.addTag("solid");
        this.actor.addTag("ball");
                
        createFragments();
        this.collisionStrategy = Thrust.game.createCollisionStrategy(this.actor);
    }

    public void createFragments()
    {
        new Fragment().actor(this.actor).pieces(10).pose("default").createPoses("fragment");
    }

    public void connect( Rod rod )
    {
        this.moving = true;
        this.rod = rod;
        this.event("taught");
    }

    @Override
    public void tick()
    {
        if (this.gate != null) {

            if (this.gateSpeed > 0) {
                this.speedX *= 0.98;
                this.speedY *= 0.98;
                getActor().moveBy(this.speedX, this.speedY);
                getActor().moveTowards(this.gate.getActor(), this.gateSpeed);
                this.collisionStrategy.update();

                if (getActor().distance(this.gate.getActor()) < this.gateSpeed) {
                    getActor().moveTo(this.gate.getActor());
                    this.gateSpeed = 0;
                    this.deathEvent("travelGate");
                }
            }

        } else if (this.moving) {

            this.speedY += Thrust.gravity;
            this.getActor().moveBy(this.speedX, this.speedY);
            this.collisionStrategy.update();
            
            if (!touching(SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
                hit();
                return;
            }

            for (Actor actor : touching("gate")) {
                Gate gate = (Gate) actor.getBehaviour();
                this.event("touchedGate");
                this.gate = gate;
                getActor().removeTag("ball");
                disconnect();
                break;
            }

        }

    }

    @Override
    public void onMessage( String message )
    {
        if ("collected".equals(message)) {
            if (this.gate != null) {
                this.gate.collected(this);
            }
        }
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
        this.deathEvent("death");

        new Explosion(this.actor)
            .projectiles(30)
            .gravity(Thrust.gravity)
            .forwards()
            .fade(0.9, 1.5)
            .speed(0.1, 1.5).vx(this.speedX).vy(this.speedY)
            .createActor("fragment")
            .activate();
    }

}
