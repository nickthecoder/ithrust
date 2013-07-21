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

    @Property(label = "Required")
    public int requiredBalls = 1;

    @Property(label = "Next Level")
    public String nextLevel = "menu";

    private int collected = 0;

    @Override
    public void init()
    {
        this.actor.addTag("gate");
        this.actor.addTag("solid");
        this.collisionStrategy = Thrust.game.createCollisionStrategy(this.actor);
    }

    @Override
    public void tick()
    {
        this.actor.deactivate();
    }

    public void collected( Ball ball )
    {
        this.collected++;
        String text;

        if (this.collected >= this.requiredBalls) {

            this.event("open");
            this.actor.removeTag("solid");
            text = "Open";

        } else {
            this.event("collected");
            text = String.valueOf(this.requiredBalls - this.collected);
        }

        Actor message = new ShadowText()
            .text(text)
            .fontSize(32)
            .color(new RGBA(190,190,190))
            .fade(2)
            .offset(0,-4)
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
