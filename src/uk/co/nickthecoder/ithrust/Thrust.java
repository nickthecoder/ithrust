/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import java.io.File;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.ActorCollisionStrategy;
import uk.co.nickthecoder.itchy.Game;
import uk.co.nickthecoder.itchy.Resources;
import uk.co.nickthecoder.itchy.ScrollableLayer;
import uk.co.nickthecoder.itchy.extras.SceneTransition;
import uk.co.nickthecoder.itchy.neighbourhood.Neighbourhood;
import uk.co.nickthecoder.itchy.neighbourhood.NeighbourhoodCollisionStrategy;
import uk.co.nickthecoder.itchy.neighbourhood.StandardNeighbourhood;
import uk.co.nickthecoder.jame.RGBA;
import uk.co.nickthecoder.jame.Rect;
import uk.co.nickthecoder.jame.event.KeyboardEvent;
import uk.co.nickthecoder.jame.event.Keys;

public class Thrust extends Game
{
    public static final int NEIGHBOURHOOD_SQUARE_SIZE = 60;

    public static double gravity = -0.02;;

    public static final File RESOURCES = new File("resources/ithrust/thrust.xml");

    public static Thrust game;

    public ScrollableLayer backgroundLayer;

    public ScrollableLayer foregroundLayer;

    public ScrollableLayer mainLayer;

    public ScrollableLayer escapeRouteLayer;

    public ScrollableLayer glassLayer;

    private Neighbourhood neighbourhood;

    public static final RGBA BACKGROUND = new RGBA(30, 30, 30);

    private Ship previousLevelShip;

    private String previousSceneName;

    public Thrust(Resources resources) throws Exception
    {
        super(resources);

        this.neighbourhood = new StandardNeighbourhood(NEIGHBOURHOOD_SQUARE_SIZE);
    }

    @Override
    public void createLayers()
    {
        Rect screenRect = new Rect(0, 0, getWidth(), getHeight());

        this.backgroundLayer = new ScrollableLayer("background", screenRect, BACKGROUND);
        this.layers.add(this.backgroundLayer);

        this.mainLayer = new ScrollableLayer("main", screenRect, null);
        this.layers.add(this.mainLayer);

        this.foregroundLayer = new ScrollableLayer("foreground", screenRect, null);
        this.layers.add(this.foregroundLayer);

        this.escapeRouteLayer = new ScrollableLayer("escapeRoute", screenRect, null);
        this.layers.add(this.escapeRouteLayer);
        this.escapeRouteLayer.setVisible(false);

        this.glassLayer = new ScrollableLayer("glass", screenRect);
        this.glassLayer.locked = true;
        this.layers.add(this.glassLayer);

    }

    @Override
    public void onActivate()
    {
        super.onActivate();

        this.mainLayer.disableMouseListener(this);
        this.mainLayer.enableMouseListener(this);
    }

    public ActorCollisionStrategy createCollisionStrategy( Actor actor )
    {
        return new NeighbourhoodCollisionStrategy(actor, this.neighbourhood);
    }

    public void centerOn( Actor actor )
    {
        this.mainLayer.ceterOn(actor);
        this.backgroundLayer.ceterOn(actor);
        this.foregroundLayer.ceterOn(actor);
        this.escapeRouteLayer.ceterOn(actor);
    }

    public void training()
    {
        this.previousLevelShip = null;
        startScene("training-hub");
    }

    public void newGame()
    {
        this.previousLevelShip = null;
        startScene("start");
    }

    @Override
    public void testScene( String sceneName )
    {
        this.previousLevelShip = null;
        this.previousSceneName = null;
        super.testScene(sceneName);
    }

    public String getPreviousScene()
    {
        return this.previousSceneName;
    }

    public void nextScene( Ship ship, String sceneName )
    {
        this.previousSceneName = getSceneName();
        this.previousLevelShip = ship;
        startScene(sceneName);
    }

    public Ship getPreviousSceneShip()
    {
        return this.previousLevelShip;
    }

    @Override
    public void startScene( final String sceneName )
    {
        System.out.println("\nStarting Scene " + sceneName + "\n");

        new SceneTransition().transition(sceneName);
    }

    @Override
    public boolean onKeyDown( KeyboardEvent ke )
    {
        if (super.onKeyDown(ke)) {
            return true;
        }

        if (ke.isPressed()) {
            if (ke.symbol == Keys.ESCAPE) {
                startScene("menu");
                return true;
            }

            if (ke.symbol == Keys.F1) {
                debug();
            }
        }

        return false;
    }

    private void debug()
    {
        System.out.println("Debug\n");

        System.out.println("\nEnd Debug\n");
    }

    public static void main( String argv[] )
    {
        System.out.println("Welcome to Thrust");

        try {
            Resources resources = new Resources();
            resources.load(RESOURCES);

            Thrust.game = new Thrust(resources);

            if ((argv.length == 1) && ("--editor".equals(argv[0]))) {
                Thrust.game.startEditor();
            } else {
                Thrust.game.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Goodbye from Thrust");
    }

}
