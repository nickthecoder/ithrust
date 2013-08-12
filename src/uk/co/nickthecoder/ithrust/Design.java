/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

public class Design
{
    public static void main(String[] argv) throws Exception
    {
        String sceneName = "start-lakeside";

        if (argv.length > 0) {
            sceneName = argv[0];
        }
        Thrust.game = new Thrust();
        Thrust.game.startEditor( sceneName );
    }
}
