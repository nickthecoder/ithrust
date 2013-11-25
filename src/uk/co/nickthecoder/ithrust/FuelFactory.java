/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.property.Property;

public class FuelFactory extends BallFactory
{
    @Property(label = "Fuel")
    public int fuel = 1;

    public Ball createRole()
    {
        Ball ball = new Ball();
        ball.fuel = this.fuel;    
        
        return ball;
    }

}
