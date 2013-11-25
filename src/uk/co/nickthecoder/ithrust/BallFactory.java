/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Costume;
import uk.co.nickthecoder.itchy.PlainRole;
import uk.co.nickthecoder.itchy.animation.Animation;
import uk.co.nickthecoder.itchy.animation.AnimationListener;
import uk.co.nickthecoder.itchy.extras.Timer;
import uk.co.nickthecoder.itchy.property.Property;

public abstract class BallFactory extends AbstractRole
{
    @Property(label = "Quantitfy")
    public int quantity = -1;

    @Property(label = "Delay")
    public int delay = 2;

    private Timer newBallTimer;

    @Override
    public void onBirth()
    {
        addTag("solid");
        getActor().setCollisionStrategy(Thrust.director.createCollisionStrategy(getActor()));
        createBall();
    }

    @Override
    public void tick()
    {
        if (this.newBallTimer != null) {
            if (this.newBallTimer.isFinished()) {
                createBall();
                this.newBallTimer = null;
            }
        }
    }

    public void createBall()
    {
        if (this.quantity != 0) {
            this.quantity -= 1;

            String costumeName = getActor().getCostume().getString("ballCostume", "fuel");
            Costume costume = Thrust.director.getGame().resources.getCostume(costumeName);
            final Actor actor = new Actor(costume);

            Animation animation = getActor().getCostume().getAnimation("appear");
            actor.setAnimation(animation);
            actor.getAnimation().addAnimationListener(new AnimationListener() {

                @Override
                public void finished()
                {
                    Ball ball = createRole();
                    ball.ballFactory = BallFactory.this;

                    actor.setRole(ball);
                }
            });
            actor.setRole(new PlainRole());
            actor.moveTo(getActor());
            actor.setZOrder(getActor().getZOrder()-1);
            getActor().getStage().add(actor);
        }
    }

    public abstract Ball createRole();

    public void onTaken()
    {
        this.newBallTimer = Timer.createTimerSeconds(this.delay);
    }
}
