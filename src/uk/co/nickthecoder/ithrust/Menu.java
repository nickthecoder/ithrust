/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.PlainSceneDirector;

public class Menu extends PlainSceneDirector
{
    @Override
    public void onMessage( String message )
    {
        if ("menu".equals(message)) {
            Thrust.director.startScene("menu");
        }
        super.onMessage(message);
    }

}
