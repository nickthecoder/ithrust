package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Font;
import uk.co.nickthecoder.itchy.MouseListener;
import uk.co.nickthecoder.itchy.TextPose;
import uk.co.nickthecoder.itchy.util.Property;

import uk.co.nickthecoder.jame.RGBA;
import uk.co.nickthecoder.jame.event.MouseButtonEvent;
import uk.co.nickthecoder.jame.event.MouseMotionEvent;

public class PickLevelBehaviour extends Behaviour implements MouseListener
{
    @Property(label="Level Number")
    public int levelNumber;

    @Property(label="Font")
    public Font font;

    @Property(label="Font Size")
    public int fontSize = 22;

    @Property(label="Font Colour", alpha=false)
    public RGBA fontColor = new RGBA( 255, 255, 255 );

    @Property(label="Shadow Colour", alpha=false)
    public RGBA shadowColor = new RGBA( 0,0,0 );

    @Override
    public void init()
    {
        if ( Thrust.singleton.completedLevel( this.levelNumber ) ) {
            this.actor.event( "completed" );
        }

        if ( this.font != null ) {
            TextPose shadowPose = new TextPose( String.valueOf( this.levelNumber ), this.font, this.fontSize, this.shadowColor );

            TextPose textPose = new TextPose( String.valueOf( this.levelNumber ), this.font, this.fontSize, this.fontColor );
            this.actor.getAppearance().superimpose( shadowPose, 2, 2 );
            this.actor.getAppearance().superimpose( textPose, 0, 0 );
        }
    }

    @Override
    public void tick()
    {
    }

    @Override
    public boolean onMouseDown( MouseButtonEvent event )
    {
        if ( this.actor.contains( event.x, event.y ) ) {
        	Thrust.singleton.play( this.levelNumber );
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseUp( MouseButtonEvent event )
    {
        return false;
    }

    @Override
    public boolean onMouseMove( MouseMotionEvent event )
    {
        return false;
    }

}
