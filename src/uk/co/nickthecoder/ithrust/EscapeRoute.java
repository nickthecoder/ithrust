/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import uk.co.nickthecoder.itchy.AbstractRole;
import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Appearance;
import uk.co.nickthecoder.itchy.Role;
import uk.co.nickthecoder.itchy.util.Tag;

@Tag(names = { EscapeRoute.POSSIBLE_ROUTE })
public class EscapeRoute extends AbstractRole
{
    public static final String POSSIBLE_ROUTE = "possibleRoute";

    public static final String ESCAPE_ROUTE = "escapeRoute";

    public List<EscapeRoute> feeds;

    public EscapeRoute wayBack;

    public boolean used;

    @Override
    public void onBirth()
    {
        this.feeds = new ArrayList<EscapeRoute>();
    }

    @Override
    public void tick()
    {
    }

    private boolean reversed = false;

    public void reverse()
    {
        if (this.reversed) {
            return;
        }
        this.reversed = true;

        Point2D.Double otherEnd = this.getOtherEnd();
        getActor().moveTo(otherEnd.x, otherEnd.y);
        event("reverse");

    }

    private Point2D.Double getOtherEnd()
    {
        Appearance appearance = getActor().getAppearance();

        double length = appearance.getPose().getSurface().getWidth();
        length -= appearance.getPose().getOffsetX() * 2;
        length *= appearance.getScale();

        double x = getActor().getX() + Math.cos(appearance.getDirectionRadians()) * length;
        double y = getActor().getY() + Math.sin(appearance.getDirectionRadians()) * length;

        return new Point2D.Double(x, y);
    }

    public void addGate( Gate gate )
    {
        // System.out.println("\nAdding a escape route for " + gate.getActor());
        findRoutes(gate, null);
    }

    protected void findRoutes( Gate gate, EscapeRoute previousEscapeRoute )
    {
        if (this.used) {
            // System.out.println("Already linked back to that gate");
            return;
        }
        this.used = true;

        // System.out.println("Add way back : " + previousEscapeRoute);
        this.wayBack = previousEscapeRoute;

        Point2D.Double otherEnd = getOtherEnd();

        // System.out.println("Looking for other ER near " + otherEnd.x + "," + otherEnd.y);
        for (Role role : AbstractRole.allByTag(POSSIBLE_ROUTE)) {
            Actor other = role.getActor();
            
            if (role != this) {
                double distance = other.distanceTo(otherEnd.x, otherEnd.y);
                if (distance < 50) {
                    // System.out.println("Found " + other + otherEnd.x + "," + otherEnd.y +
                    // " distance " + distance);
                    ((EscapeRoute) role).findRoutes(gate, this);
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
