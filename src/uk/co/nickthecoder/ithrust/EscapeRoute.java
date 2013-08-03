/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Appearance;
import uk.co.nickthecoder.itchy.Behaviour;

public class EscapeRoute extends Behaviour
{
    public static final String ESCAPE_ROUTE = "escapeRoute";

    public List<EscapeRoute> feeds;

    public EscapeRoute wayBack;

    public boolean used;

    @Override
    public void init()
    {
        this.feeds = new ArrayList<EscapeRoute>();
        getActor().addTag(ESCAPE_ROUTE);
    }

    @Override
    public void onActivate()
    {
    }

    @Override
    public void tick()
    {
        System.out.println("Reversing ER");
        Point2D.Double otherEnd = this.getOtherEnd();
        this.getActor().moveTo(otherEnd.x, otherEnd.y);
        this.getActor().event("reverse");

        // Escape routes are never seen, so remove them from their layer.
        // Its quicker than setting their alpha to zero.
        this.getActor().getLayer().remove(this.getActor());

        this.actor.deactivate();
    }

    private Point2D.Double getOtherEnd()
    {
        Appearance appearance = getActor().getAppearance();

        double length = appearance.getPose().getSurface().getWidth() * appearance.getScale();
        length -= appearance.getPose().getOffsetX() * 2;

        double x = this.actor.getX() + Math.cos(appearance.getDirectionRadians()) * length;
        double y = this.actor.getY() + Math.sin(appearance.getDirectionRadians()) * length;

        return new Point2D.Double(x, y);
    }

    public void addGate( Gate gate )
    {
        // System.out.println("\nAdding a escape route for " + gate.getActor());
        findRoutes(gate, null);
    }

    protected void findRoutes( Gate gate, EscapeRoute previousEscapeRoute )
    {
        // System.out.println("Find Routes " + this.getActor().getX() + "," + this.getActor().getY()
        // +
        // " dir=" + this.getActor().getAppearance().getDirection());

        if (this.used) {
            System.out.println("Already linked back to that gate");
            return;
        }
        this.used = true;

        // System.out.println("Add way back : " + previousEscapeRoute);
        this.wayBack = previousEscapeRoute;

        Point2D.Double otherEnd = getOtherEnd();

        // System.out.println("Looking for other ER near " + otherEnd.x + "," + otherEnd.y);
        for (Actor other : Actor.allByTag(ESCAPE_ROUTE)) {
            if (other != this.actor) {
                double distance = other.distanceTo(otherEnd.x, otherEnd.y);
                // System.out.println("Found " + other + " distance " + distance);
                if (distance < 50) {
                    ((EscapeRoute) (other.getBehaviour())).findRoutes(gate, this);
                }
            }
        }

    }

    public EscapeRoute getWayBack()
    {
        return this.wayBack;
    }

    public boolean hasWayBack()
    {
        return this.used;
    }

    @Override
    public String toString()
    {
        return "ER (" + getActor().getX() + "," + getActor().getY() + ") dir=" +
            getActor().getAppearance().getDirection();
    }
}
