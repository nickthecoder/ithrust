/*******************************************************************************
 * Copyright (c) 2013 Nick Robinson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package uk.co.nickthecoder.ithrust;

import java.io.File;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.ActorCollisionStrategy;
import uk.co.nickthecoder.itchy.Game;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.Scene;
import uk.co.nickthecoder.itchy.ScrollableLayer;
import uk.co.nickthecoder.itchy.animation.AlphaAnimation;
import uk.co.nickthecoder.itchy.animation.AnimationListener;
import uk.co.nickthecoder.itchy.animation.CompoundAnimation;
import uk.co.nickthecoder.itchy.animation.NumericAnimation;
import uk.co.nickthecoder.itchy.neighbourhood.Neighbourhood;
import uk.co.nickthecoder.itchy.neighbourhood.NeighbourhoodCollisionStrategy;
import uk.co.nickthecoder.itchy.neighbourhood.StandardNeighbourhood;
import uk.co.nickthecoder.jame.Keys;
import uk.co.nickthecoder.jame.RGBA;
import uk.co.nickthecoder.jame.Rect;
import uk.co.nickthecoder.jame.event.KeyboardEvent;

public class Thrust extends Game
{
    public static final int NEIGHBOURHOOD_SQUARE_SIZE = 60;

    public static double gravity = -0.02;;

    public static final File RESOURCES = new File("resources/ithrust/thrust.xml");

    public static Thrust game;


    public ScrollableLayer backgroundLayer;
    
    public ScrollableLayer foregroundLayer;

    public ScrollableLayer mainLayer;

    public ScrollableLayer glassLayer;

    public ScrollableLayer fadeLayer;

    private Neighbourhood neighbourhood;

    private String sceneName = null;

    private Actor fadeActor;

    private String levelName;

    public static final RGBA BACKGROUND = new RGBA(30, 30, 30);

    
    public Thrust() throws Exception
    {
        super("Thrust", 800,600);
        
        resources.load(RESOURCES);

        this.neighbourhood = new StandardNeighbourhood(NEIGHBOURHOOD_SQUARE_SIZE);

        Rect screenSize = new Rect(0, 0, this.getWidth(), this.getHeight());
        
        this.backgroundLayer= new ScrollableLayer("background",screenSize, BACKGROUND);
        this.layers.add(this.backgroundLayer);
        
        this.mainLayer = new ScrollableLayer("main",screenSize, null);
        this.layers.add(this.mainLayer);
        
        this.foregroundLayer = new ScrollableLayer("foreground",screenSize,null);
        this.layers.add(this.foregroundLayer);
        
        this.glassLayer = new ScrollableLayer("glass",screenSize);
        this.glassLayer.locked = true;
        this.layers.add(this.glassLayer);

        this.fadeLayer = new ScrollableLayer("fade",screenSize);
        this.fadeLayer.locked= true;
        this.layers.add(this.fadeLayer);

        this.fadeActor = new Actor(this.resources.getPose("background"));
        this.fadeActor.moveTo(400, 300);
        this.fadeActor.getAppearance().setAlpha(0);
        this.fadeActor.activate();
        this.fadeLayer.add(this.fadeActor);
   
    }

    @Override
    public void init()
    {
        this.levelName = "tutorial";

        this.mainLayer.disableMouseListener();
        this.mainLayer.enableMouseListener();

        this.startScene("menu");
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
    }


    @Override
    public boolean onKeyDown( KeyboardEvent ke )
    {
        if (ke.isReleased()) {
            return false;
        }

        if ("levels".equals(this.sceneName)) {
            if (ke.symbol == Keys.RETURN) {
                this.play();
                return true;
            }
        }

        if ("menu".equals(this.sceneName)) {
            if (ke.symbol == Keys.ESCAPE) {
                Itchy.singleton.terminate();
                return true;
            }

            if ((ke.symbol == Keys.p) || (ke.symbol == Keys.RETURN)) {
                this.startScene("levels");
                return true;
            }

            if (ke.symbol == Keys.a) {
                this.startScene("about");
            }

        } else {
            if (ke.symbol == Keys.ESCAPE) {
                this.startScene("menu");
            }
            return true;
        }

        return super.onKeyDown(ke);
    }

    @Override
    public boolean onKeyUp( KeyboardEvent ke )
    {
        return false;
    }

    @Override
    public void tick()
    {
    }

    public boolean startScene( String sceneName )
    {

        this.sceneName = sceneName;

        try {
            final Scene scene = this.resources.getScene(this.sceneName);
            if (scene == null) {
                System.err.println("Scene not found " + sceneName);
                return false;
            }

            Thrust.this.mainLayer.deactivateAll();

            AlphaAnimation fadeOut = new AlphaAnimation(10, NumericAnimation.linear, 255);
            AlphaAnimation fadeIn = new AlphaAnimation(10, NumericAnimation.linear, 0);
            CompoundAnimation animation = new CompoundAnimation(true);
            animation.addAnimation(fadeOut);
            animation.addAnimation(fadeIn);

            fadeOut.addAnimationListener(new AnimationListener()
            {
                @Override
                public void finished()
                {
                    Thrust.this.mainLayer.clear();
                    Thrust.this.foregroundLayer.clear();
                    Thrust.this.backgroundLayer.clear();
                    
                    Thrust.this.mainLayer.scrollTo(0, 0);
                    Thrust.this.foregroundLayer.scrollTo(0, 0);
                    Thrust.this.backgroundLayer.scrollTo(0, 0);

                    scene.create(Thrust.this.layers, false);
                    Itchy.showMousePointer(scene.showMouse);

                }
            });

            this.fadeActor.setAnimation(animation);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    public boolean completedLevel( String levelName )
    {
        return getPreferences().getBoolean("completedLevel:" + levelName, false);
    }
    
    public boolean unlockedLevel( String levelName )
    {
        if ( "tutorial".equals( levelName ) ) {
            return true;
        }
        return getPreferences().getBoolean("unlockedLevel:" + levelName, false);
    }
    
    public void play( String levelName )
    {
        getPreferences().putBoolean("completedLevel:" + this.levelName, true);
        getPreferences().putBoolean("unlockedLevel:" + levelName, true);
        this.levelName = levelName;
        this.play();
    }

    public boolean play()
    {
        return this.startScene(this.levelName);
    }

    public void onMessage( String message )
    {
        if ("play".equals(message)) {
            this.startScene("levels");

        } else if ("menu".equals(message)) {
            this.startScene("menu");

        } else if ("about".equals(message)) {
            this.startScene("about");

        } else if ("editor".equals(message)) {
            this.startEditor();

        } else if ("quit".equals(message)) {
            Itchy.singleton.terminate();
        }
    }

    @Override
    public String getIconFilename()
    {
        return "resources/ithrust/icon.bmp";
    }

    public static void main( String argv[] )
    {
        System.out.println("Welcome to Thrust");

        try {
            game = new Thrust();
            
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
