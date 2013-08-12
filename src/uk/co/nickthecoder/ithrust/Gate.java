/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.extras.Explosion;
import uk.co.nickthecoder.itchy.extras.ShadowText;
import uk.co.nickthecoder.itchy.util.Property;
import uk.co.nickthecoder.jame.RGBA;

public class Gate extends Behaviour
{

    @Property(label = "Required Fuel")
    public int requiredFuel = 0;

    @Property(label = "Required Water")
    public int requiredWater = 0;

    @Property(label = "Next Level")
    public String nextLevel = "menu";

    @Property(label = "Exit Direction")
    public double exitDirection = 90;

    @Override
    public void init()
    {
        this.actor.addTag("gate");
        updateState();
        this.collisionStrategy = Thrust.game.createCollisionStrategy(this.actor);
    }

    @Override
    public void onActivate()
    {
        if (this.nextLevel.equals(Thrust.game.getPreviousScene())) {
            Ship ship = Thrust.game.getPreviousSceneShip();
            if (ship != null) {
                findRoutesBack();
                ship.startGate(this);
            }
        }
    }

    @Override
    public void tick()
    {
        this.actor.deactivate();
    }

    public void findRoutesBack()
    {
        // System.out.println( "Gate finding routes back" );
        for (Actor escapeRoute : Actor.allByTag(EscapeRoute.ESCAPE_ROUTE)) {
            double distance = escapeRoute.distanceTo(this.actor);
            if (distance < 150) {
                ((EscapeRoute) (escapeRoute.getBehaviour())).addGate(this);
            }
        }
        for (Actor actor : Actor.allByTag(EscapeRoute.ESCAPE_ROUTE)) {
            EscapeRoute er = (EscapeRoute) actor.getBehaviour();
            if (!er.hasWayBack()) {
                actor.kill();
            }
        }
    }

    public void updateState()
    {
        if ((this.requiredFuel <= 0) && (this.requiredWater <= 0)) {
            this.event("on");
            this.actor.removeTag("solid");
        } else {
            this.actor.addTag("solid");
            if ( this.requiredFuel > 0 ) {
                this.event("off");
            } else {
                this.event("standby");
            }
        }
    }

    public void collected( Ball ball )
    {
        this.requiredFuel -= ball.fuel;
        this.requiredWater -= ball.water;
        String text;

        updateState();

        if ((this.requiredFuel <= 0) && (this.requiredWater <= 0)) {

            this.event("opening");
            text = "Open";

        } else {
            this.event("collected");
            if ( this.requiredFuel > 0 ) {
                text = "Needs Fuel";
            } else if ( this.requiredWater > 0 ) {
                text = "Needs Water";
            } else {
                text = "";
            }
        }

        Actor message = new ShadowText()
            .text(text)
            .fontSize(32)
            .color(new RGBA(190, 190, 190))
            .fade(2)
            .offset(0, -4)
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
}
