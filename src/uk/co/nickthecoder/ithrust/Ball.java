package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.util.ExplosionBehaviour;

public class Ball extends Behaviour
{

	public double speedX = 0;
	public double speedY = 0;
	public double weight = 1.0;
	
	private boolean moving = false;
	private Rod rod;
	
	@Override
	public void init()
	{
		this.actor.addTag("solid");
		this.actor.addTag("ball");
	}
	
	public void connect( Rod rod )
	{
		this.moving = true;
		this.rod = rod;
	}
	
	@Override
	public void tick()
	{
		if ( this.moving ) {
			
	        for ( Actor other : Actor.allByTag( "solid" ) ) {
	        	if ( other == this.actor ) continue;
	            if ( this.actor.touching( other ) ) {
	                this.die();
	                return;
	            }
	        }
	        
	        this.speedY += Thrust.gravity;
	        this.getActor().moveBy( this.speedX, this.speedY );
		}

	}

	public void die()
	{
		if ( this.rod != null ) {
			this.rod.disconnect();
		}
        this.deathEvent( "death" );
        
        new ExplosionBehaviour()
        .projectiles(30).projectilesPerClick(3)
        .distance(20, 40)
        .gravity(Thrust.gravity)
        .rotate(true).spin(-10, 10)
        .scale(0.3, 0.5)
        .alpha(230,255).fade(0.9, 1.5)
        .speed(0.1, 1.5).vx(this.speedX / 4).vy(this.speedY / 4)
        .createActor(this.actor, "fragment")
        .activate();
	}
	
}
