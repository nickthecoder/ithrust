/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Behaviour;
import uk.co.nickthecoder.itchy.Font;
import uk.co.nickthecoder.itchy.MouseListener;
import uk.co.nickthecoder.itchy.TextPose;
import uk.co.nickthecoder.itchy.util.Property;
import uk.co.nickthecoder.itchy.util.StringUtils;
import uk.co.nickthecoder.jame.RGBA;
import uk.co.nickthecoder.jame.event.MouseButtonEvent;
import uk.co.nickthecoder.jame.event.MouseMotionEvent;

public class PickLevelBehaviour extends Behaviour implements MouseListener
{
    @Property(label = "Label")
    public String label;

    @Property(label = "Level Name")
    public String levelName;

    @Property(label = "Font")
    public Font font;

    @Property(label = "Font Size")
    public int fontSize = 22;

    @Property(label = "Font Colour", alpha = false)
    public RGBA fontColor = new RGBA(255, 255, 255);

    @Property(label = "Shadow Colour", alpha = false)
    public RGBA shadowColor = new RGBA(0, 0, 0);

    @Override
    public void init()
    {
        if (Thrust.game.completedLevel(this.levelName)) {
            this.actor.event("complete");
        }

        if (!Thrust.game.unlockedLevel(this.levelName)) {
            getActor().kill();
            return;
        }

        if ((this.font != null) && (!StringUtils.isBlank(this.label))) {
            TextPose shadowPose = new TextPose(this.label, this.font, this.fontSize,
                this.shadowColor);

            TextPose textPose = new TextPose(this.label, this.font, this.fontSize,
                this.fontColor);
            this.actor.getAppearance().superimpose(shadowPose, 4, 44);
            this.actor.getAppearance().superimpose(textPose, 0, 40);
        }
    }

    @Override
    public void tick()
    {
    }

    @Override
    public boolean onMouseDown( MouseButtonEvent event )
    {
        if (this.actor.contains(event.x, event.y)) {
            Thrust.game.play(this.levelName);
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
