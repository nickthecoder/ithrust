package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.ScrollableLayer;
import uk.co.nickthecoder.itchy.util.ExplosionBehaviour;
import uk.co.nickthecoder.itchy.util.Fragment;
import uk.co.nickthecoder.itchy.util.ProjectileBehaviour;
import uk.co.nickthecoder.jame.Keys;
import uk.co.nickthecoder.jame.Sound;

public class Ship extends Behaviour
{
    private static final double DEAD_SLOW_DOWN = 0.99;
    
    public double rotationSpeed = 3;

    public double thrust = 0.1;

    public double speedX = 0.0;
    
    public double speedY = 0.0;

    public double pickupDistance = 200;

    private Rod rod;

    @Override
    public void init()
    {
        this.actor.addTag("solid");

        // Create the fragments for the explosions when I get shot.
        new Fragment().actor(this.actor).create("fragment");
    }

    @Override
    public void tick()
    {

        if ( this.actor.isDying()) {
            // Gently brake the ship so that the scroll layer still scrolls, but not too far.
            this.speedX *= DEAD_SLOW_DOWN;
            this.speedY *= DEAD_SLOW_DOWN;
        } else {
            this.speedY += Thrust.gravity;            
        }
        
        this.actor.moveBy(this.speedX, this.speedY);

        ((ScrollableLayer) this.actor.getLayer()).ceterOn(this.actor);

        if (this.actor.isDying()) {
            return;
        }

        if (Itchy.singleton.isKeyDown(Keys.LEFT)) {
            turn(this.rotationSpeed);
        }
        if (Itchy.singleton.isKeyDown(Keys.RIGHT)) {
            turn(-this.rotationSpeed);
        }
        if (Itchy.singleton.isKeyDown(Keys.SPACE) || Itchy.singleton.isKeyDown(Keys.UP)) {
            this.thrust();
        }
        if ((this.rod == null) && (Itchy.singleton.isKeyDown(Keys.a))) {
            Actor ballActor = this.actor.nearest("ball");
            if ((ballActor != null) && (ballActor.distanceTo(this.actor) < this.pickupDistance)) {
                this.rod = new Rod(this, (Ball) ballActor.getBehaviour());
            }
        }
        if ((this.rod != null) && (Itchy.singleton.isKeyDown(Keys.z))) {
            this.rod.disconnect();
        }

        for (Actor other : Actor.allByTag("solid")) {
            if (other == this.actor) {
                continue;
            }
            if (this.actor.touching(other)) {
                this.die();
                return;
            }
        }
    }

    public void rodDisconnected()
    {
        this.rod = null;
    }

    public void turn( double amount )
    {
        this.actor.getAppearance().adjustDirection(amount);

        Sound sound = this.actor.getCostume().getSound("turn");
        if (sound != null) {
            sound.playOnce();
        }
    }

    public void thrust()
    {
        double direction = this.actor.getAppearance().getDirectionRadians();
        double cos = Math.cos(direction);
        double sin = Math.sin(direction);

        this.speedX += this.thrust * cos;
        this.speedY += this.thrust * sin;

        Actor puff = new ProjectileBehaviour()
            .vx(this.speedX).vy(this.speedY)
            .fade(3)
            .speed(1)
            .createActor(this.actor, "exhaust");

        puff.getAppearance().adjustDirection(180);
        puff.moveForward(30);
        puff.activate();

        Sound sound = this.actor.getCostume().getSound("thrust");
        if (sound != null) {
            sound.playOnce();
        }
    }

    public void die()
    {
        if (this.rod != null) {
            this.rod.disconnect();
        }
        this.deathEvent("death");

        new ExplosionBehaviour(this.actor)
            .projectiles(30)
            .forwards()
            .spin(-.2, .2)
            .fade(0.8, 1.6)
            .speed(0.5,2).vx(this.speedX).vy(this.speedY)
            .gravity(Thrust.gravity)
            .createActor("fragment")
            .activate();
    }

}
