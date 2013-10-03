/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.jame.event.KeyboardEvent;
import uk.co.nickthecoder.jame.event.Keys;

public class MainMenu extends Menu
{

    @Override
    public void onMessage( String message )
    {
        if ("training".equals(message)) {
            Thrust.game.training();

        } else if ("newGame".equals(message)) {
            Thrust.game.newGame();
        
        } else if ("about".equals(message)) {
            Thrust.game.startScene("about");
        
        } else if ("editor".equals(message)) {
            Thrust.game.startEditor();
        
        } else if ("quit".equals(message)) {
            Itchy.terminate();
        }

        super.onMessage(message);
    }

    public boolean onKeyDown( KeyboardEvent ke )
    {
        if (ke.symbol == Keys.F12){
            Thrust.game.startEditor();
            return true;

        } else if (ke.symbol == Keys.a) {
            Thrust.game.startScene("about");
            return true;
            
        } else if (ke.symbol == Keys.ESCAPE) {
            return true;
        }
  
        return super.onKeyDown(ke);
    }

}
