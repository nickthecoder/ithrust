/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials are made available under the terms of
 * the GNU Public License v3.0 which accompanies this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Role;
import uk.co.nickthecoder.itchy.makeup.Shadow;
import uk.co.nickthecoder.itchy.property.Property;
import uk.co.nickthecoder.itchy.role.Explosion;
import uk.co.nickthecoder.itchy.role.Projectile;
import uk.co.nickthecoder.jame.RGBA;

public class Gate extends AbstractRole
{

    @Property(label = "Required Fuel")
    public int requiredFuel = 0;

    @Property(label = "Required Water")
    public int requiredWater = 0;

    @Property(label = "Next Level")
    public String nextLevel = "menu";

    @Property(label = "Exit Direction")
    public double exitDirection = 90;

    private boolean doRoutesBack = true;

    @Override
    public void onBirth()
    {
        addTag("gate");
        updateState();
        getActor().setCollisionStrategy(Thrust.director.createCollisionStrategy(getActor()));
    }

    @Override
    public void tick()
    {
        if (this.doRoutesBack) {
            String previousSceneName = Thrust.director.getPreviousScene();
            if (this.nextLevel.equals(previousSceneName)) {
                findRoutesBack();
                Ship ship = Thrust.director.getPreviousSceneShip();
                if (ship != null) {
                    Actor actor = new Actor(ship.getActor().getCostume());
                    actor.setRole(ship);
                    ship.startGate(this);
                }
            }
            this.doRoutesBack = false;
        }
    }

    public void findRoutesBack()
    {
        for (Role role : AbstractRole.allByTag(EscapeRoute.POSSIBLE_ROUTE)) {
            EscapeRoute escapeRoute = (EscapeRoute) role;
            double distance = escapeRoute.getActor().distanceTo(getActor());
            if (distance < 150) {
                escapeRoute.addGate(this);
            }
        }

        for (Role role : AbstractRole.allByTag(EscapeRoute.POSSIBLE_ROUTE)) {
            EscapeRoute er = (EscapeRoute) role;
            Actor actor = er.getActor();

            if (er.hasWayBack()) {
                er.reverse();

                actor.getAppearance().setColorize(new RGBA(0, 200, 0)); // Debug
                er.addTag(EscapeRoute.ESCAPE_ROUTE);

            } else {
                actor.getAppearance().setColorize(new RGBA(200, 0, 0)); // Debug

                // Comment out the following line to debug unused escape routes.
                // actor.kill();
            }
        }
    }

    public void updateState()
    {
        if ((this.requiredFuel <= 0) && (this.requiredWater <= 0)) {
            this.event("on");
            removeTag("solid");
        } else {
            addTag("solid");
            if (this.requiredFuel > 0) {
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
            if (this.requiredFuel > 0) {
                text = "Needs Fuel";
            } else if (this.requiredWater > 0) {
                text = "Needs Water";
            } else {
                text = "";
            }
        }

        Actor message = new Projectile(getActor())
            .companion("message").text(text).fontSize(32).color(new RGBA(190, 190, 190)).fade(2)
            .createActor();

        message.getAppearance().setMakeup( new Shadow().offset(2, 2).color(RGBA.BLACK));
        message.getAppearance().fixAppearance();
        
        new Explosion(message)
            .pose(message.getAppearance().getPose())
            .projectiles(15)
            .adjustZOrder(-1)
            .spin(-.2, .2)
            .alpha(128).fade(1, 2)
            .speed(0.5, 2)
            .gravity(Thrust.gravity)
            .createActor();
    }
}
