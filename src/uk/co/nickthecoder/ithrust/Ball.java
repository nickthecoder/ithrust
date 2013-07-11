package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.extras.Fragment;
import uk.co.nickthecoder.itchy.util.DoubleProperty;
import uk.co.nickthecoder.itchy.extras.Explosion;

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
        createFragments();
    }
    
    @Override
    public void addProperties()
    {
        addProperty(new DoubleProperty("Weight", "weight"));
    }
    
    void createFragments()
    {
        // Create the fragments for the explosions when I get shot.
        new Fragment().actor(this.actor).pieces(10).pose("shell").create("wrappingFragment");
        new Fragment().actor(this.actor).pieces(4).pose("contents").create("contentsFragment");
    }
    
    public void connect( Rod rod )
    {
        this.moving = true;
        this.rod = rod;
        this.event("taught");
    }

    @Override
    public void tick()
    {
        if (this.moving) {

            this.speedY += Thrust.gravity;
            this.getActor().moveBy(this.speedX, this.speedY);

            for (Actor other : Actor.allByTag("solid")) {
                if (other == this.actor) {
                    continue;
                }
                if (this.actor.touching(other)) {
                    this.die();
                    return;
                }
            }

        }

    }

    public void die()
    {
        if (this.rod != null) {
            this.rod.disconnect();
        }
        this.deathEvent("death");

        new Explosion(this.actor)
            .projectiles(12)
            .gravity(Thrust.gravity)
            .forwards()
            //.spin(-1, 1)
            .fade(0.9, 1.5)
            .speed(0.1, 1.5).vx(this.speedX).vy(this.speedY)
            .createActor("contentsFragment")
            .activate();

        new Explosion(this.actor)
            .projectiles(30)
            .gravity(Thrust.gravity)
            .forwards()
            //.spin(-1, 1)
            .fade(0.9, 1.5)
            .speed(0.1, 1.5).vx(this.speedX).vy(this.speedY)
            .createActor("wrappingFragment")
            .activate();
    }

}
