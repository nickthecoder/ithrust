/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials are made available under the terms of
 * the GNU Public License v3.0 which accompanies this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.PlainSceneDirector;
import uk.co.nickthecoder.itchy.collision.CollisionStrategy;
import uk.co.nickthecoder.itchy.collision.Neighbourhood;
import uk.co.nickthecoder.itchy.collision.NeighbourhoodCollisionStrategy;
import uk.co.nickthecoder.itchy.collision.StandardNeighbourhood;

public class Level extends PlainSceneDirector
{
    public static final int NEIGHBOURHOOD_SQUARE_SIZE = 60;

    private Neighbourhood neighbourhood;

    public Level()
    {
        neighbourhood = new StandardNeighbourhood(NEIGHBOURHOOD_SQUARE_SIZE);
    }

    @Override
    public CollisionStrategy getCollisionStrategy( Actor actor )
    {
        return new NeighbourhoodCollisionStrategy(actor, neighbourhood);
    }

}
