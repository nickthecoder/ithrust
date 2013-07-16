package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Behaviour;

public class Pod extends Behaviour
{
    private static final double SPEED = 0.01;

    private Ship ship;

    private Ball ball;

    private double progress = 0;
    
    public Pod( Ship ship, Ball ball )
    {
        this.ship = ship;
        this.ball = ball;
    }

    private static double easeInOut( double value )
    {
        if (value < 0.5) {
            return value * value * value * 4;
        } else {
            value = 1 - value;
            return 1 - (value * value * value * 4);
        }        
    }
    
    @Override
    public void tick()
    {
        double value = easeInOut( this.progress);
        
        double x = this.ship.getActor().getX() * (1 - value) + this.ball.getActor().getX() * value;
        double y = this.ship.getActor().getY() * (1 - value) + this.ball.getActor().getY() * value;

        this.actor.moveTo(x, y);
        Thrust.game.centerOn(this.actor);

        this.progress += SPEED;
        if (this.progress > 1) {
            this.ship.completeSwitchEnds();
            deathEvent("death");
        }
    }

}
