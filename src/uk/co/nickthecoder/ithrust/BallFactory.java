/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Costume;
import uk.co.nickthecoder.itchy.animation.Animation;
import uk.co.nickthecoder.itchy.animation.AnimationListener;
import uk.co.nickthecoder.itchy.util.Property;

public abstract class BallFactory extends Behaviour
{
    @Property(label = "Quantitfy")
    public int quantity = -1;

    @Property(label = "Delay")
    public int delay = 5;

    @Override
    public void onActivate()
    {
        getActor().addTag("solid");
        this.collisionStrategy = Thrust.game.createCollisionStrategy(getActor());
        createBall();
    }

    @Override
    public void tick()
    {
    }

    public void createBall()
    {
        if (this.quantity != 0) {
            this.quantity -= 1;

            String costumeName = getActor().getCostume().getString("ballCostume", "fuel");
            Costume costume = Thrust.game.resources.getCostume(costumeName);
            final Actor actor = new Actor(costume);

            Animation animation = getActor().getCostume().getAnimation("appear");
            actor.setAnimation(animation);
            actor.getAnimation().addAnimationListener(new AnimationListener() {

                @Override
                public void finished()
                {
                    Ball ball = createBehaviour();
                    ball.ballFactory = BallFactory.this;

                    actor.setBehaviour(ball);
                }
            });
            actor.moveTo(getActor());
            getActor().getLayer().addBelow(actor, getActor());
            actor.activate();
        }
    }

    public abstract Ball createBehaviour();

    @Override
    public void onMessage( String message )
    {
        if ("taken".equals(message)) {
            sleep(this.delay);
            createBall();
        }
    }
}
