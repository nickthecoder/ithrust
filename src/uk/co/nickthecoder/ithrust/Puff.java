/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Behaviour;

public class Puff extends Behaviour
{
    public double vx;
    
    public double vy;
    
    public double speed = 10;
    
    public double fade = 8;
    
    public double scale = 0.96;

    public Actor createActor( Actor source, String poseName, int distance )
    {
        Actor actor = new Actor( source.getCostume().getPose( poseName ) );
        actor.moveTo( source );
        actor.getAppearance().setDirection( source.getAppearance().getDirection() + 180 );
        actor.moveForward( distance );
        actor.setBehaviour(this);
        source.getLayer().add(actor);
        
        return actor;
    }
    
    @Override
    public void tick()
    {
        getActor().moveBy( vx, vy );
        getActor().moveForward( this.speed );
        getActor().getAppearance().setScale( getActor().getAppearance().getScale() * scale );
        getActor().getAppearance().adjustAlpha( - fade );
        if ( getActor().getAppearance().getAlpha() < 0 ) {
            this.getActor().kill();
        }
    }

}
