package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.extras.Explosion;
import uk.co.nickthecoder.itchy.extras.Follower;
import uk.co.nickthecoder.itchy.extras.Fragment;
import uk.co.nickthecoder.itchy.extras.Projectile;
import uk.co.nickthecoder.itchy.util.Property;
import uk.co.nickthecoder.jame.Keys;
import uk.co.nickthecoder.jame.Sound;

public class Ship extends Behaviour
{
    private static final double DEAD_SLOW_DOWN = 0.99;

    @Property(label="Rotation Speed")
    public double rotationSpeed = 3;

    @Property(label="Thrust")
    public double thrust = 0.1;

    @Property(label="Weight")
    public double weight = 1.0;

    public double speedX = 0.0;

    public double speedY = 0.0;

    public double pickupDistance = 200;

    private boolean switchingEnds = false;

    private Rod rod;

    private Follower wrapping;

    private Follower unwrapping;

    @Override
    public void init()
    {
        this.actor.addTag("solid");
        this.createFragments();
    }

    /**
     * Create the fragments for the explosions when I get shot.
     */
    void createFragments()
    {
        new Fragment().actor(this.actor).create("fragment");
    }

    @Override
    public void tick()
    {

        if (this.actor.isDying()) {
            // Gently brake the ship so that the scroll layer still scrolls, but not too far.
            this.speedX *= DEAD_SLOW_DOWN;
            this.speedY *= DEAD_SLOW_DOWN;
        } else {
            this.speedY += Thrust.gravity;
        }

        this.actor.moveBy(this.speedX, this.speedY);

        if (!this.switchingEnds) {
            Thrust.game.centerOn(this.actor);
        }

        if (this.actor.isDying()) {
            return;
        }

        if (this.switchingEnds) {
            return;
        }

        if (connected() && Itchy.singleton.isKeyDown(Keys.q)) {
            beginSwitchEnds();
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

    public boolean connected()
    {
        if (this.rod == null) {
            return false;
        }
        return this.rod.connected;
    }

    public void beginSwitchEnds()
    {
        if (this.switchingEnds) {
            return;
        }
        this.switchingEnds = true;

        System.out.println("Switching ends");
        Ball ball = this.rod.ball;
        Actor ballActor = ball.getActor();

        this.wrapping = new Follower(this.actor);
        this.wrapping.createActor(ballActor.getCostume()).activate();
        this.wrapping.event("wrapping");

        this.unwrapping = new Follower(ballActor);
        this.unwrapping.createActor(ballActor.getCostume()).activate();
        this.unwrapping.deathEvent("unwrapping");
        ballActor.event("contents");

        Pod pod = new Pod(this, ball);
        pod.createActor(this.actor.getLayer(), "pod").activate();

    }

    public void completeSwitchEnds()
    {
        System.out.println("completeSwitchEnds");
        this.switchingEnds = false;

        this.wrapping.getActor().kill();
        this.unwrapping.getActor().kill();
        
        Actor shipActor = this.actor;

        if (this.rod != null) {
            Ball ball = this.rod.ball;
            Actor ballActor = ball.getActor();

            ball.event("touched"); // Make a noise

            // Change the costumes.
            // The ball changes to whatever the current ship looks like when wrapped.
            // The ship changes to whatever the ball looks like unwrapped.
            String wrappedCostumeName = this.actor.getCostume().getString("wrappedCostume");
            String unwrappedCostumeName = ballActor.getCostume().getString("unwrappedCostume");

            System.out.println("Costume for ball : " + wrappedCostumeName);
            System.out.println("Costume for ship : " + unwrappedCostumeName);

            this.actor.setCostume(Itchy.singleton.getResources().getCostume(unwrappedCostumeName));
            ballActor.setCostume(Itchy.singleton.getResources().getCostume(wrappedCostumeName));
            this.actor.event("default");
            ballActor.event("default");

            // The new costumes may not have their fragments created yet...
            ball.createFragments();
            this.createFragments();

            double tx = this.getActor().getX();
            double ty = this.getActor().getY();

            // MORE switch vx,vy and other attributes.
            double tmp;
            tmp = this.speedX;
            this.speedX = ball.speedX;
            ball.speedX = tmp;
            tmp = this.speedY;
            this.speedY = ball.speedY;
            ball.speedY = tmp;

            this.getActor().moveTo(ballActor);
            ballActor.moveTo(tx, ty);

        }

        shipActor.getAppearance().setDirection(90);

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

        Actor puff = new Projectile()
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

        new Explosion(this.actor)
            .projectiles(30)
            .forwards()
            .spin(-.2, .2)
            .fade(0.8, 1.6)
            .speed(0.5, 2).vx(this.speedX).vy(this.speedY)
            .gravity(Thrust.gravity)
            .createActor("fragment")
            .activate();
    }

}
