/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import java.awt.geom.Point2D;
import java.util.Iterator;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.ActorsLayer;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Costume;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.extras.Explosion;
import uk.co.nickthecoder.itchy.extras.Follower;
import uk.co.nickthecoder.itchy.extras.Fragment;
import uk.co.nickthecoder.itchy.extras.Projectile;
import uk.co.nickthecoder.itchy.extras.Timer;
import uk.co.nickthecoder.itchy.util.CubicSpline;
import uk.co.nickthecoder.jame.Keys;
import uk.co.nickthecoder.jame.Sound;

public class Ship extends Behaviour implements Fragile
{
    private static final double DEAD_SLOW_DOWN = 0.99;

    public static final String[] SOLID_TAGS = new String[] { "solid" };

    public static final String[] EXCLUDE_TAGS = new String[] { "soft" };

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

    private Timer fireTimer;

    private Gate startGate;

    @Override
    public void onAttach()
    {
        this.getActor().addTag("fragile");
        this.getActor().addTag("solid");
        this.getActor().addTag("ship");

        this.fireTimer = new Timer((int) (1000 * this.firePeriod));

        this.createFragments();
        this.collisionStrategy = Thrust.game.createCollisionStrategy(this.getActor());
        this.updateCostumeData();
    }

    @Override
    public void onActivate()
    {
        this.pickupDistance = getActor().getCostume().getPose("rod").getSurface().getWidth();

        // If this is the first level, then use this ship and have it come out of the appropriate
        // gate if there is one.
        if (Thrust.game.getPreviousSceneShip() == null) {
            Actor actor = getActor().nearest("gate");
            if (actor != null) {
                Gate gate = (Gate) (actor.getBehaviour());
                gate.findRoutesBack();
                this.startGate(gate);
                return;
            }

        } else {
            // This isn't the first level, and so we can destroy this ship. It was added into the
            // scene to allow running a single scene on its own, for debugging.
            this.getActor().kill();
        }
    }

    @Override
    public void onDetach()
    {
        endEvent("rotate");
        endEvent("thrust");
    }

    /**
     * Takes info from the costume, such as the thrust force, so that each type of ship handles
     * differently.
     */
    private void updateCostumeData()
    {
        Costume costume = getActor().getCostume();
        this.rotationSpeed = costume.getDouble("rotationSpeed", 0.5);
        this.rotationDamper = costume.getDouble("rotationDamper", 0.94);
        this.thrust = costume.getDouble("thrust", 0.2);
        this.weight = costume.getDouble("weight", 2.0);
        this.firePeriod = costume.getDouble("firePeriod", 1);
        this.landingSpeed = costume.getDouble("landingSpeed", 2.0);
    }

    /**
     * Create the fragments for the explosions when I get shot.
     */
    void createFragments()
    {
        new Fragment().actor(this.getActor()).createPoses("fragment");
    }

    @Override
    public void tick()
    {

        if (!this.switchingEnds) {

            if (isConnected() && Itchy.isKeyDown(Keys.q)) {
                if (this.rod.ball instanceof BallWithShip) {
                    beginSwitchEnds();
                }
            }

            if (Itchy.isKeyDown(Keys.UP)) {
                this.event("thrust");
                this.thrust();
            } else {
                this.endEvent("thrust");
            }

            if ((this.rod == null) && (Itchy.isKeyDown(Keys.a))) {
                Actor ballActor = this.getActor().nearest("ball");
                if ((ballActor != null) && (ballActor.distanceTo(this.getActor()) < this.pickupDistance)) {
                    this.rod = new Rod(this, (Ball) ballActor.getBehaviour());
                }
            }
            if ((this.rod != null) && (Itchy.isKeyDown(Keys.z))) {
                this.rod.disconnect();
            }

            if ((Itchy.isKeyDown(Keys.SPACE)) && (this.fireTimer.isFinished())) {
                this.fireTimer.reset();
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
        this.getActor().moveBy(this.speedX, this.speedY);
        this.collisionStrategy.update();

        this.currentRotationSpeed *= this.rotationDamper;
        if (Itchy.isKeyDown(Keys.LEFT)) {
            event("rotate");
            this.currentRotationSpeed += this.rotationSpeed;
        } else if (Itchy.isKeyDown(Keys.RIGHT)) {
            event("rotate");
            this.currentRotationSpeed -= this.rotationSpeed;
        } else {
            endEvent("rotate");
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
            Thrust.game.centerOn(this.getActor());
        }

        if (!touching("liquid").isEmpty()) {
            hit();
            return;
        }

        if (!touching(SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
            hit();
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

        this.wrapping = new Follower(this.getActor());
        this.wrapping.createActor(ballActor.getCostume()).activate();
        this.wrapping.event("wrapping");

        this.unwrapping = new Follower(ballActor);
        this.unwrapping.createActor(ballActor.getCostume()).activate();
        this.unwrapping.deathEvent("unwrapping");
        ballActor.event("contents");

        Pod pod = new Pod(this, ball);
        pod.createActor(this.getActor().getLayer(), "pod").activate();

    }

    public void completeSwitchEnds()
    {
        this.switchingEnds = false;

        this.wrapping.getActor().kill();
        this.unwrapping.getActor().kill();

        Actor shipActor = this.getActor();

        if (this.rod != null) {
            Ball ball = this.rod.ball;
            Actor ballActor = ball.getActor();

            ball.event("touched"); // Make a noise

            // Change the costumes.
            // The ball changes to whatever the current ship looks like when wrapped.
            // The ship changes to whatever the ball looks like unwrapped.
            String wrappedCostumeName = this.getActor().getCostume().getString("wrappedCostume");
            String unwrappedCostumeName = ballActor.getCostume().getString("unwrappedCostume");

            this.getActor().setCostume(Itchy.getResources().getCostume(unwrappedCostumeName));
            ballActor.setCostume(Itchy.getResources().getCostume(wrappedCostumeName));
            this.getActor().event("default");
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
        this.getActor().getAppearance().adjustDirection(amount);

        Sound sound = this.getActor().getCostume().getSound("turn");
        if (sound != null) {
            sound.playOnce();
        }
    }

    public void thrust()
    {
        double direction = this.getActor().getAppearance().getDirectionRadians();
        double cos = Math.cos(direction);
        double sin = Math.sin(direction);

        this.speedX += this.thrust * cos;
        this.speedY += this.thrust * sin;

        Actor puff = new Projectile()
            .vx(this.speedX).vy(this.speedY)
            .fade(3)
            .speed(1)
            .createActor(this.getActor(), "exhaust");

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

        if (this.startGate != null) {
            this.event("death");
            this.event("escapePod");
            this.getActor().setBehaviour(new EscapePod());
        } else {
            this.deathEvent("death");
            this.getActor().setBehaviour(new Dying());
        }

        new Explosion(this.getActor())
            .projectiles(30)
            .forwards()
            .spin(-.2, .2)
            .fade(0.8, 1.6)
            .speed(0.5, 2).vx(this.speedX).vy(this.speedY)
            .gravity(Thrust.gravity)
            .createActor("fragment")
            .activate();

    }

    /**
     * This ship needs to appear from the gate specified. The animation is roughly the opposite of
     * the ship entering a gate.
     * 
     * @param gate
     */
    public void startGate( Gate gate )
    {
        this.startGate = gate;
        double direction = gate.exitDirection;

        this.getActor().getAppearance().setDirection(direction);
        this.getActor().getAppearance().setScale(0.01);
        this.getActor().getAppearance().setAlpha(0);

        this.getActor().moveTo(gate.getActor());
        this.getActor().activate();
        this.getActor().setBehaviour(new ExitingGate());
        ActorsLayer layer = gate.getActor().getLayer();
        layer.add(this.getActor());
        this.getActor().event("default"); // Ensure its got the right pose
        this.getActor().event("exitGate");

    }

    public class ExitingGate extends Behaviour
    {
        @Override
        public void tick()
        {
            Thrust.game.centerOn(this.getActor());
        }

        @Override
        public void onMessage( String message )
        {
            if ("exitGate".equals(message)) {
                Ship.this.speedX = 0;
                Ship.this.speedY = 0;
                this.getActor().setBehaviour(Ship.this);
            }
        }
    }

    public class Dying extends Behaviour
    {

        @Override
        public void tick()
        {
            this.getActor().getAppearance().setAlpha(0);

            // Gently brake the ship so that the scroll layer still scrolls, but not too far.
            Ship.this.speedX *= DEAD_SLOW_DOWN;
            Ship.this.speedY *= DEAD_SLOW_DOWN;

            this.getActor().moveBy(Ship.this.speedX, Ship.this.speedY);
            this.collisionStrategy.update();

        }

    }

    public class EscapePod extends Behaviour
    {
        public Actor target;

        Iterator<Point2D.Float> path;

        @Override
        public void onAttach()
        {
            // Make any doors open slightly to allow the pod through.
            for (Actor actor : Actor.allByTag("door")) {
                actor.getBehaviour().onMessage("escapePod");
            }

            this.target = Ship.this.startGate.getActor();

            Actor escapeRoute = getActor().nearest("escapeRoute");
            if (escapeRoute != null) {
                double erDist = escapeRoute.distance(getActor());
                double gateDist = Ship.this.startGate.getActor().distance(getActor());
                if (erDist < gateDist) {
                    buildPath((EscapeRoute) escapeRoute.getBehaviour());
                } else {
                    buildPath(null);
                }
            } else {
                buildPath(null);
            }
        }

        private void buildPath( EscapeRoute er )
        {
            CubicSpline spline = new CubicSpline();
            spline.add(getActor().getX(), getActor().getY());

            if (er == null) {
                spline.add(getActor().getX(), getActor().getY());
                spline.add(Ship.this.startGate.getActor().getX(), Ship.this.startGate.getActor()
                    .getY());
            }

            while (er != null) {
                spline.add(er.getActor().getX(), er.getActor().getY());
                er = er.getWayBack();
            }
            spline
                .add(Ship.this.startGate.getActor().getX(), Ship.this.startGate.getActor().getY());
            spline.steps = 30;

            this.path = spline.iterate();
        }

        @Override
        public void tick()
        {
            if (this.path.hasNext()) {

                Point2D.Float point = this.path.next();
                getActor().moveTo(point.x, point.y);

            } else {
                getActor().setBehaviour(Ship.this);
                for (Actor actor : Actor.allByTag("door")) {
                    actor.getBehaviour().onMessage("reset");
                }
                Ship.this.startGate(Ship.this.startGate);
            }

            Thrust.game.centerOn(this.getActor());

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
            this.getActor().getAppearance().adjustDirection(this.turnSpeed);
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
                Thrust.game.nextScene(Ship.this, this.gate.nextLevel);
            }
        }

    }

}
