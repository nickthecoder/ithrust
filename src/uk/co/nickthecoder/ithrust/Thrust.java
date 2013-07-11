package uk.co.nickthecoder.ithrust;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import uk.co.nickthecoder.itchy.Actor;
import uk.co.nickthecoder.itchy.Game;
import uk.co.nickthecoder.itchy.Itchy;
import uk.co.nickthecoder.itchy.Scene;
import uk.co.nickthecoder.itchy.ScrollableLayer;
import uk.co.nickthecoder.itchy.animation.AlphaAnimation;
import uk.co.nickthecoder.itchy.animation.AnimationListener;
import uk.co.nickthecoder.itchy.animation.CompoundAnimation;
import uk.co.nickthecoder.itchy.animation.NumericAnimation;
import uk.co.nickthecoder.itchy.editor.Editor;
import uk.co.nickthecoder.jame.Keys;
import uk.co.nickthecoder.jame.RGBA;
import uk.co.nickthecoder.jame.Rect;
import uk.co.nickthecoder.jame.event.KeyboardEvent;

public class Thrust extends Game
{
    public static double gravity = -0.02;;

    public static final String RESOURCES = "resources/ithrust/thrust.xml";

    public static Thrust singleton;

    public ScrollableLayer mainLayer;

    public ScrollableLayer glassLayer;

    public ScrollableLayer fadeLayer;

    private String sceneName = null;

    private Actor fadeActor;

    private int levelNumber;

    private final Set<Integer> completedLevels = new HashSet<Integer>();

    public static final RGBA BACKGROUND = new RGBA(30, 30, 30);

    
    public Thrust() throws Exception
    {
        Itchy.singleton.init(this);
        resources.load(RESOURCES);
    }

    @Override
    public void init()
    {
        Rect screenSize = new Rect(0, 0, this.getWidth(), this.getHeight());
        this.mainLayer = new ScrollableLayer(screenSize, BACKGROUND);
        this.mainLayer.enableMouseListener();
        Itchy.singleton.getGameLayer().add(this.mainLayer);

        this.glassLayer = new ScrollableLayer(screenSize);
        Itchy.singleton.getGameLayer().add(this.glassLayer);

        this.fadeLayer = new ScrollableLayer(screenSize);
        Itchy.singleton.getGameLayer().add(this.fadeLayer);

        this.fadeActor = new Actor(this.resources.getPose("background"));
        this.fadeActor.moveTo(400, 300);
        this.fadeActor.getAppearance().setAlpha(0);
        this.fadeActor.activate();
        this.fadeLayer.add(this.fadeActor);

        this.levelNumber = 1;

        Itchy.singleton.addEventListener(this);
        this.startScene("menu");
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
        System.out.println("Starting scene " + sceneName);

        this.sceneName = sceneName;

        try {
            final Scene scene = this.resources.getScene(this.sceneName);
            if (scene == null) {
                System.out.println("Scene not found " + sceneName);
                return false;
            }

            Thrust.this.mainLayer.deactivateAll();

            AlphaAnimation fadeOut = new AlphaAnimation(10, NumericAnimation.linear, 0, 255);
            AlphaAnimation fadeIn = new AlphaAnimation(10, NumericAnimation.linear, 255, 0);
            CompoundAnimation animation = new CompoundAnimation(true);
            animation.addAnimation(fadeOut);
            animation.addAnimation(fadeIn);

            fadeOut.addAnimationListener(new AnimationListener()
            {
                @Override
                public void finished()
                {
                    Thrust.this.mainLayer.clear();
                    Thrust.this.mainLayer.scrollTo(0, 0);

                    scene.create(Thrust.this.mainLayer, false);

                }
            });

            this.fadeActor.setAnimation(animation);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean completedLevel( int level )
    {
        return this.completedLevels.contains(level);
    }

    public void play( int levelNumber )
    {
        this.levelNumber = levelNumber;
        this.play();
    }

    public boolean play()
    {
        DecimalFormat df = new DecimalFormat("00");
        return this.startScene("level" + df.format(this.levelNumber));
    }

    public void action( String action )
    {
        if ("play".equals(action)) {
            this.startScene("levels");

        } else if ("menu".equals(action)) {
            this.startScene("menu");

        } else if ("about".equals(action)) {
            this.startScene("about");

        } else if ("editor".equals(action)) {
            this.startEditor();


        } else if ("quit".equals(action)) {
            Itchy.singleton.terminate();
        }
    }

    private void startEditor()
    {
        try {
            Editor editor = new Editor(Thrust.singleton);
            editor.init();
            editor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getWidth()
    {
        return 800;
    }

    @Override
    public int getHeight()
    {
        return 600;
    }

    @Override
    public String getTitle()
    {
        return "Thrust";
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
            singleton = new Thrust();
            
            if ((argv.length == 1) && ("--editor".equals(argv[0]))) {
                Thrust.singleton.startEditor();
            } else {
                Thrust.singleton.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Goodbye from Thrust");
    }

}
