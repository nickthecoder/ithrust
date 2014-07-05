/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import java.io.File;
import java.util.List;

import uk.co.nickthecoder.itchy.AbstractDirector;
import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.GenericCompoundView;
import uk.co.nickthecoder.itchy.Game;
import uk.co.nickthecoder.itchy.Resources;
import uk.co.nickthecoder.itchy.Stage;
import uk.co.nickthecoder.itchy.StageView;
import uk.co.nickthecoder.itchy.View;
import uk.co.nickthecoder.itchy.ZOrderStage;
import uk.co.nickthecoder.itchy.collision.ActorCollisionStrategy;
import uk.co.nickthecoder.itchy.collision.Neighbourhood;
import uk.co.nickthecoder.itchy.collision.NeighbourhoodCollisionStrategy;
import uk.co.nickthecoder.itchy.collision.StandardNeighbourhood;
import uk.co.nickthecoder.itchy.extras.SceneTransition;
import uk.co.nickthecoder.jame.RGBA;
import uk.co.nickthecoder.jame.Rect;
import uk.co.nickthecoder.jame.event.KeyboardEvent;
import uk.co.nickthecoder.jame.event.Keys;

public class Thrust extends AbstractDirector
{
    public static double gravity = -0.02;;

    public static final File RESOURCES = new File("resources/ithrust/thrust.itchy");

    public static Thrust director;

    public ZOrderStage backgroundStage;

    public ZOrderStage foregroundStage;

    public ZOrderStage escapeRouteStage;

    public StageView backgroundView;

    public StageView foregroundView;

    public StageView escapeRouteView;

    public static final RGBA BACKGROUND = new RGBA(30, 30, 30);

    private Ship previousLevelShip;

    private String previousSceneName;

    @Override
    public void onStarted()
    {

        Rect screenRect = new Rect(0, 0, this.game.getWidth(), this.game.getHeight());

        this.backgroundStage = new ZOrderStage("background");
        this.foregroundStage = new ZOrderStage("foreground");
        this.escapeRouteStage = new ZOrderStage("escapeRoute");

        this.backgroundView = new StageView(screenRect, this.backgroundStage);
        this.foregroundView = new StageView(screenRect, this.foregroundStage);
        this.escapeRouteView = new StageView(screenRect, this.escapeRouteStage);

        GenericCompoundView<View> views = this.game.getViews();
        views.add(this.backgroundView);
        List<Stage> stages = this.game.getStages();

        stages.add(this.backgroundStage);

        this.mainStage = new ZOrderStage("main");
        this.mainView = new StageView(screenRect, this.mainStage);
        stages.add(this.mainStage);
        views.add(this.mainView);

        this.mainView.enableMouseListener(this.game);
        views.add(this.foregroundView);
        views.add(this.escapeRouteView);

        stages.add(this.foregroundStage);
        stages.add(this.escapeRouteStage);
    }

    @Override
    public void onActivate()
    {
        director = this;
        super.onActivate();
    }

    public void centerOn( Actor actor )
    {
        this.backgroundView.ceterOn(actor);
        this.mainView.ceterOn(actor);
        this.foregroundView.ceterOn(actor);
        this.escapeRouteView.ceterOn(actor);
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

    public String getPreviousScene()
    {
        return this.previousSceneName;
    }

    public void nextScene( Ship ship, String sceneName )
    {
        this.previousLevelShip = ship;
        startScene(sceneName);
    }

    public Ship getPreviousSceneShip()
    {
        return this.previousLevelShip;
    }

    @Override
    public boolean startScene( final String sceneName )
    {
        this.previousSceneName = this.game.getSceneName();
        return new SceneTransition().transition(sceneName);
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

            Game game = resources.getGame();

            if ((argv.length == 1) && ("--editor".equals(argv[0]))) {
                game.startEditor();
            } else {
                game.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Goodbye from Thrust");
    }

}
