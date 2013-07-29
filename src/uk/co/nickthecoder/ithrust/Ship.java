/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Costume;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.extras.Explosion;
import uk.co.nickthecoder.itchy.extras.Follower;
import uk.co.nickthecoder.itchy.extras.Fragment;
import uk.co.nickthecoder.itchy.extras.Projectile;
import uk.co.nickthecoder.itchy.extras.Recharge;
import uk.co.nickthecoder.itchy.extras.ShadowText;
import uk.co.nickthecoder.itchy.util.Property;
import uk.co.nickthecoder.jame.Keys;
import uk.co.nickthecoder.jame.Sound;

public class Ship extends Behaviour implements Fragile
{
    private static final double DEAD_SLOW_DOWN = 0.99;

    public static final String[] SOLID_TAGS = new String[] { "solid" };

    public static final String[] EXCLUDE_TAGS = new String[] { "soft" };

    @Property(label = "Start Message")
    public String startMessage;

    // The following attributes can vary from one costume to another.
    public double rotationSpeed;

    public double rotationDamper;
    
    public double thrust;

    public double weight;

    public double firePeriod;

    public double landingSpeed;

    public double pickupDistance;
    // End of costume attributes.
    
    public double speedX = 0.0;

    public double speedY = 0.0;

    private double currentRotationSpeed;
    
    private boolean switchingEnds = false;

    Rod rod;

    private Follower wrapping;

    private Follower unwrapping;

    private Sound thrustSound;

    private Sound rotateSound;

    private Recharge fireRecharge;

    @Override
    public void init()
    {
        this.actor.addTag("fragile");
        this.actor.addTag("ship");

        this.fireRecharge = new Recharge((int) (1000 * this.firePeriod));

        this.createFragments();
        this.collisionStrategy = Thrust.game.createCollisionStrategy(this.actor);
        this.updateCostumeData();
    }

    /**
     * Takes info from the costume, such as the thrust force, so that each type of ship handles differently.
     */
    private void updateCostumeData()
    {
        Costume costume = getActor().getCostume();
        this.rotationSpeed = costume.getDouble("rotationSpeed", 0.5 );
        this.rotationDamper = costume.getDouble("rotationDamper", 0.94 );
        this.thrust = costume.getDouble( "thrust", 0.2 );
        this.weight = costume.getDouble( "weight", 2.0);
        this.firePeriod = costume.getDouble("firePeriod", 1); 
        this.landingSpeed = costume.getDouble( "landingSpeed", 2.0);
        this.pickupDistance = costume.getDouble( "pickupDistance", 200);
    }
    
    /**
     * Create the fragments for the explosions when I get shot.
     */
    void createFragments()
    {
        new Fragment().actor(this.actor).createPoses("fragment");
    }

    @Override
    public void onActivate()
    {        
        if (this.startMessage != null) {
            Actor message = new ShadowText()
                .text(this.startMessage)
                .fontSize(32)
                .offset(0, -60)
                .fade(2)
                .growFactor(0.995)
                .createActor(getActor());

            message.activate();

            new Explosion(message)
                .projectiles(15)
                .below()
                .forwards()
                .spin(-.2, .2)
                .alpha(128).fade(1, 2)
                .speed(0.5, 2)
                .gravity(Thrust.gravity)
                .createActor("fragment")
                .activate();

        }

        this.pickupDistance = getActor().getCostume().getPose("rod").getSurface().getWidth();
    }

    @Override
    public void tick()
    {

        if (!this.switchingEnds) {

            if (isConnected() && Itchy.singleton.isKeyDown(Keys.q)) {
                if (this.rod.ball instanceof BallWithShip) {
                    beginSwitchEnds();
                }
            }

            if (Itchy.singleton.isKeyDown(Keys.UP)) {
                if (this.thrustSound == null) {
                    this.thrustSound = getActor().getCostume().getSound("thrust");
                    this.thrustSound.play();
                }
                this.thrust();
            } else {
                if (this.thrustSound != null) {
                    this.thrustSound.fadeOut(1000);
                    this.thrustSound = null;
                }
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

            if ((Itchy.singleton.isKeyDown(Keys.SPACE)) && (this.fireRecharge.isCharged())) {
                this.fireRecharge.reset();
                fire();
            }

            for (Actor actor : touching("gate")) {
                Gate gate = (Gate) actor.getBehaviour();
                this.disconnect();
                getActor().setBehaviour(new NextLevel(gate));
                return;
            }
        }

        this.speedY += Thrust.gravity;
        this.actor.moveBy(this.speedX, this.speedY);
        this.collisionStrategy.update();

        this.currentRotationSpeed *= this.rotationDamper;
        if (Itchy.singleton.isKeyDown(Keys.LEFT)) {
            if (this.rotateSound == null) {
                this.rotateSound = getActor().getCostume().getSound("rotate");
                this.rotateSound.play();
            }
            this.currentRotationSpeed += this.rotationSpeed;
        } else if (Itchy.singleton.isKeyDown(Keys.RIGHT)) {
            if (this.rotateSound == null) {
                this.rotateSound = getActor().getCostume().getSound("rotate");
                this.rotateSound.play();
            }
            this.currentRotationSpeed -= this.rotationSpeed;
        } else {
            if (this.rotateSound != null) {
                this.rotateSound.fadeOut(1000);
                this.rotateSound = null;
            }
            this.currentRotationSpeed *= this.rotationDamper;
        }
        turn(this.currentRotationSpeed);
        
        if (!touching("soft").isEmpty()) {
            if (this.rod == null) {
                double speed = Math.sqrt(this.speedX * this.speedX + this.speedY * this.speedY);
                double upright = Math.abs(getActor().getAppearance().getDirection() - 90) % 360.0;
                if (upright > 180) {
                    upright = 360 - upright;
                }
                if ((speed < this.landingSpeed) && (upright < 5)) {
                    getActor().moveBy(-this.speedX, -this.speedY);
                    turn(-this.currentRotationSpeed);
                    this.speedX = 0;
                    this.speedY = 0;
                } else {
                    hit();
                }
            } else {
                hit();
            }

        } else {

        }

        if (!this.switchingEnds) {
            Thrust.game.centerOn(this.actor);
        }

        if (!touching(SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
            this.hit();
            return;
        }

    }

    public void disconnected()
    {
        this.rod = null;
    }

    private void disconnect()
    {
        if (this.rod != null) {
            this.rod.disconnect();
        }
    }

    public boolean isConnected()
    {
        if (this.rod == null) {
            return false;
        }
        return this.rod.connected;
    }

    private void fire()
    {
        Bullet bullet = new Bullet();
        Actor actor = bullet.createActor(getActor(), "bullet");
        actor.moveForward(30);
        bullet.speed(6).gravity(Thrust.gravity);
        actor.activate();
    }

    public void beginSwitchEnds()
    {
        if (this.switchingEnds) {
            return;
        }
        this.switchingEnds = true;

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
    }

    @Override
    public void hit()
    {
        if (this.rod != null) {
            this.rod.disconnect();
        }
        this.deathEvent("death");
        this.actor.setBehaviour(new Dying());

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

    public class Dying extends Behaviour
    {

        @Override
        public void tick()
        {
            // Gently brake the ship so that the scroll layer still scrolls, but not too far.
            Ship.this.speedX *= DEAD_SLOW_DOWN;
            Ship.this.speedY *= DEAD_SLOW_DOWN;

            this.actor.moveBy(Ship.this.speedX, Ship.this.speedY);
            this.collisionStrategy.update();

        }

    }

    public class NextLevel extends Behaviour
    {
        public Gate gate;

        public double speed = 3;

        public double turnSpeed = 1;

        public NextLevel( Gate gate )
        {
            this.gate = gate;
        }

        @Override
        public void tick()
        {
            Ship.this.speedX *= 0.98;
            Ship.this.speedY *= 0.98;
            getActor().moveBy(Ship.this.speedX, Ship.this.speedY);
            getActor().moveTowards(this.gate.getActor(), this.speed);
            this.collisionStrategy.update();
            this.actor.getAppearance().adjustDirection(this.turnSpeed);
            this.turnSpeed *= 1.005;
            this.turnSpeed += 0.03;

            if (getActor().distance(this.gate.getActor()) < this.speed) {
                getActor().moveTo(this.gate.getActor());
                this.speed = 0;
                Ship.this.speedX = 0;
                Ship.this.speedY = 0;
                this.deathEvent("travelGate");
            }

        }

        @Override
        public void onMessage( String message )
        {
            if ("exitGate".equals(message)) {
                Thrust.game.play(this.gate.nextLevel);
            }
        }

    }

}
