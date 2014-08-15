/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials are made available under the terms of
 * the GNU Public License v3.0 which accompanies this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import java.awt.geom.Point2D;
import java.util.Iterator;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Costume;
import uk.co.nickthecoder.itchy.Input;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.Role;
import uk.co.nickthecoder.itchy.Stage;
import uk.co.nickthecoder.itchy.extras.Fragment;
import uk.co.nickthecoder.itchy.extras.Timer;
import uk.co.nickthecoder.itchy.role.Explosion;
import uk.co.nickthecoder.itchy.role.Follower;
import uk.co.nickthecoder.itchy.util.CubicSpline;
import uk.co.nickthecoder.jame.Sound;

public class Ship extends AbstractRole implements Fragile
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
    
    private Input thrustForwards;
    
    private Input turnLeft;
    
    private Input turnRight;
    
    private Input switchEnds;
    
    private Input extendRod;
    
    private Input releaseRod;
    
    private Input fire;
    
    private Input cheat;

    @Override
    public void onBirth()
    {
        this.updateCostumeData();

        this.fireTimer = Timer.createTimerSeconds(this.firePeriod);

        this.createFragments();

        Ship ship = Thrust.director.getPreviousSceneShip();
        // If this is the first level, then use this ship and have it come out of the appropriate
        // gate if there is one.
        if (ship == null) {
            Role role = getActor().nearest("gate");
            if (role != null) {
                Gate gate = (Gate) role;
                // gate.findRoutesBack();
                this.startGate(gate);
                return;
            }
            exited();

        } else {
            if (ship != this) {
                // This isn't the first level, and so we can destroy this ship. It was added into
                // the scene to allow running a single scene on its own, for debugging.
                getActor().kill();
            }
        }
        
        this.thrustForwards = Input.find("thrust");
        this.turnLeft = Input.find("left");
        this.turnRight = Input.find("right");
        this.switchEnds = Input.find("switchEnds");
        this.extendRod = Input.find("extendRod");
        this.releaseRod = Input.find("releaseRod");
        this.fire = Input.find("fire");
        this.cheat = Input.find("cheat");
    }

    /**
     * Called after the ship has exited the gate at after the scene has just begun. May also be called from Ship.onBirth if there was no
     * gate to come through, but this shouldn't happen during real game play.
     */
    public void exited()
    {
        addTag("fragile");
        addTag("solid");
        addTag("ship");
        this.pickupDistance = getActor().getCostume().getPose("rod").getSurface().getWidth();

    }

    @Override
    public void onDetach()
    {
        endEvent("rotate");
        endEvent("thrust");
    }

    /**
     * Takes info from the costume, such as the thrust force, so that each type of ship handles differently.
     */
    private void updateCostumeData()
    {
        Costume costume = getActor().getCostume();
        ShipProperties properties;

        try {
            properties = (ShipProperties) costume.getProperties();
        } catch (Exception e) {
            e.printStackTrace();
            properties = new ShipProperties();
        }

        this.rotationSpeed = properties.rotationSpeed;
        this.thrust = properties.thrust;
        this.weight = properties.weight;
        this.firePeriod = properties.firePeriod;

        this.landingSpeed = properties.landingSpeed;
        this.rotationDamper = properties.rotationDamper;
    }

    /**
     * Create the fragments for the explosions when I get shot.
     */
    void createFragments()
    {
        new Fragment().actor(getActor()).createPoses("fragment");
    }

    private boolean cheating = false;

    @Override
    public void tick()
    {

        if (!this.switchingEnds) {

            if (isConnected() && this.switchEnds.pressed()) {
                if (this.rod.ball instanceof BallWithShip) {
                    beginSwitchEnds();
                }
            }

            if (this.thrustForwards.pressed()) {
                this.event("thrust");
                this.thrust();
            } else {
                this.endEvent("thrust");
            }

            if ((this.rod == null) && this.extendRod.pressed()) {
                Ball ball = (Ball) getActor().nearest("ball");
                if ((ball != null) &&
                    (ball.getActor().distanceTo(getActor()) < this.pickupDistance)) {
                    this.rod = new Rod(this, ball);
                }
            }
            if ((this.rod != null) && this.releaseRod.pressed()) {
                this.rod.disconnect();
            }

            if (this.cheat.pressed()) {
                this.cheating = true;
            }

            if (this.fire.pressed() && (this.fireTimer.isFinished())) {
                this.fireTimer.reset();
                fire();
            }

            for (Role role : getCollisionStrategy().collisions(getActor(),"gate")) {
                Gate gate = (Gate) role;
                this.disconnect();
                getActor().setRole(new NextLevel(gate));
                return;
            }
        }

        this.speedY += Thrust.gravity;
        getActor().moveBy(this.speedX, this.speedY);
        getCollisionStrategy().update();

        this.currentRotationSpeed *= this.rotationDamper;
        if (this.turnLeft.pressed()) {
            event("rotate");
            this.currentRotationSpeed += this.rotationSpeed;
        } else if (this.turnRight.pressed()) {
            event("rotate");
            this.currentRotationSpeed -= this.rotationSpeed;
        } else {
            endEvent("rotate");
            this.currentRotationSpeed *= this.rotationDamper;
        }
        turn(this.currentRotationSpeed);

        if (!getCollisionStrategy().collisions(getActor(), "soft").isEmpty()) {
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
            Thrust.director.centerOn(getActor());
        }

        if (!getCollisionStrategy().collisions(getActor(),"liquid").isEmpty()) {
            hit();
            return;
        }

        if (!getCollisionStrategy().collisions(getActor(),SOLID_TAGS, EXCLUDE_TAGS).isEmpty()) {
            if (!this.cheating) {
                hit();
                return;
            }
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
        Bullet bullet = new Bullet(getActor());
        bullet.direction(getActor().getDirection());
        Actor actor = bullet.pose("bullet").createActor();
        actor.moveForwards(30);
        bullet.speed(6).gravity(Thrust.gravity);
    }

    public void beginSwitchEnds()
    {
        if (this.switchingEnds) {
            return;
        }
        this.switchingEnds = true;

        Ball ball = this.rod.ball;
        Actor ballActor = ball.getActor();

        this.wrapping = new Follower(getActor()).costume(ballActor.getCostume());
        this.wrapping.createActor();
        this.wrapping.event("wrapping");

        this.unwrapping = new Follower(ballActor).costume(ballActor.getCostume());
        this.unwrapping.createActor();
        this.unwrapping.deathEvent("unwrapping");
        ballActor.event("contents");

        Pod pod = new Pod(this, ball);
        Actor actor = new Actor(getActor().getCostume(), "pod");
        actor.setRole(pod);
        actor.setZOrder(getActor().getZOrder());
        getActor().getStage().add(actor);
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
            String wrappedCostumeName = getActor().getCostume().getString("wrappedCostume");
            String unwrappedCostumeName = ballActor.getCostume().getString("unwrappedCostume");

            getActor().setCostume(Itchy.getResources().getCostume(unwrappedCostumeName));
            ballActor.setCostume(Itchy.getResources().getCostume(wrappedCostumeName));
            getActor().event("default");
            ballActor.event("default");

            // The new costumes may not have their fragments created yet...
            ball.createFragments();
            this.createFragments();

            double tx = getActor().getX();
            double ty = getActor().getY();

            // MORE switch vx,vy and other attributes.
            double tmp;
            tmp = this.speedX;
            this.speedX = ball.speedX;
            ball.speedX = tmp;
            tmp = this.speedY;
            this.speedY = ball.speedY;
            ball.speedY = tmp;

            getActor().moveTo(ballActor);
            ballActor.moveTo(tx, ty);

        }

        shipActor.setDirection(90);

    }

    public void turn( double amount )
    {
        getActor().adjustDirection(amount);

        Sound sound = getActor().getCostume().getSound("turn");
        if (sound != null) {
            sound.playOnce();
        }
    }

    public void thrust()
    {
        double direction = getActor().getAppearance().getDirectionRadians();
        double cos = Math.cos(direction);
        double sin = Math.sin(direction);

        this.speedX += this.thrust * cos;
        this.speedY += this.thrust * sin;

        new Explosion(getActor())
            .projectiles(4).follow().projectilesPerTick(1)
            .spread(getActor().getHeading() + 160, getActor().getHeading() + 200).distance(40)
            .randomSpread().speed(1, 2, 0, 0).fade(3).pose("exhaust")
            .createActor();

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
            getActor().setRole(new EscapePod());
        } else {
            this.deathEvent("death");
            getActor().setRole(new Dying());
        }

        new Explosion(getActor())
            .projectiles(30)
            .spin(-.2, .2)
            .fade(0.8, 1.6)
            .speed(0.5, 2).vx(this.speedX).vy(this.speedY)
            .gravity(Thrust.gravity)
            .pose("fragment")
            .createActor();
    }

    /**
     * This ship needs to appear from the gate specified. The animation is roughly the opposite of the ship entering a gate.
     * 
     * @param gate
     */
    public void startGate( Gate gate )
    {
        this.startGate = gate;
        double direction = gate.exitDirection;

        getActor().setDirection(direction);
        getActor().getAppearance().setScale(0.01);
        getActor().getAppearance().setAlpha(0);

        getActor().setRole(new ExitingGate());
        getActor().moveTo(gate.getActor());
        Stage stage = gate.getActor().getStage();
        getActor().setZOrder(gate.getActor().getZOrder() + 1);
        stage.add(getActor());
        getActor().event("default"); // Ensure its got the right pose
        getActor().event("exitGate");
    }

    public class ExitingGate extends AbstractRole
    {
        @Override
        public void tick()
        {
            Thrust.director.centerOn(getActor());
        }

        @Override
        public void onMessage( String message )
        {
            if ("exitGate".equals(message)) {
                Ship.this.speedX = 0;
                Ship.this.speedY = 0;
                getActor().setRole(Ship.this);
                Ship.this.exited();
            }
        }
    }

    public class Dying extends AbstractRole
    {

        @Override
        public void tick()
        {
            getActor().getAppearance().setAlpha(0);

            // Gently brake the ship so that the scroll layer still scrolls, but not too far.
            Ship.this.speedX *= DEAD_SLOW_DOWN;
            Ship.this.speedY *= DEAD_SLOW_DOWN;

            getActor().moveBy(Ship.this.speedX, Ship.this.speedY);
            getCollisionStrategy().update();

        }

    }

    public class EscapePod extends AbstractRole
    {
        public Actor target;

        Iterator<Point2D.Float> path;

        @Override
        public void onAttach()
        {
            // Make any doors open slightly to allow the pod through.
            for (Role role : AbstractRole.allByTag("door")) {
                role.onMessage("escapePod");
            }

            this.target = Ship.this.startGate.getActor();

            EscapeRoute escapeRoute = (EscapeRoute) getActor().nearest(EscapeRoute.ESCAPE_ROUTE);
            if (escapeRoute != null) {
                double erDist = escapeRoute.getActor().distance(getActor());
                double gateDist = Ship.this.startGate.getActor().distance(getActor());
                if (erDist < gateDist) {
                    buildPath(escapeRoute);
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
                getActor().setRole(Ship.this);
                for (Role role : AbstractRole.allByTag("door")) {
                    role.onMessage("reset");
                }
                Ship.this.startGate(Ship.this.startGate);
            }

            Thrust.director.centerOn(getActor());

        }

    }

    public class NextLevel extends AbstractRole
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
            getCollisionStrategy().update();
            getActor().getAppearance().adjustDirection(this.turnSpeed);
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
                Thrust.director.nextScene(Ship.this, this.gate.nextLevel);
            }
        }

    }

}
