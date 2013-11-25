/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.CostumeProperties;
import uk.co.nickthecoder.itchy.property.Property;

public class ShipProperties extends CostumeProperties
{

    @Property(label="Fire Period")
    public double firePeriod = 1;
    
    @Property(label="Rotation Speed")
    public double rotationSpeed = 0.2;
    
    @Property(label="Rotation Damper")
    public double rotationDamper = 0.2;
    
    @Property(label="Weight")
    public double weight = 1;
    
    @Property(label="Thrust")
    public double thrust = 0.2;
    
    @Property(label="Wrapped Costume")
    public String wrappedCostumeName = "wrappedShip1";
    
    @Property(label="Landing Speed")
    public double landingSpeed = 2;
}
