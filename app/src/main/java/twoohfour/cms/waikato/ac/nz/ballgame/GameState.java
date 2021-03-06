package twoohfour.cms.waikato.ac.nz.ballgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Stuart on 25/09/2015.
 */
public class GameState {

    //region Variables
    protected float[] _grav;
    protected List<GenericSprite> _sprites;
    protected DrawableView _view;
    protected int[] _viewSize;
    protected Point _levelSize;
    protected int _score;
    protected PlayerSprite _player;
    protected String _title;
    protected int _ticks;
    protected boolean _isComplete;
    protected PointF _offset;
    protected State _state;

    public enum State {Waiting, Playing, Spectating}

    public enum Level {Random, Scrolling, Empty, LevelOne, Death, Happy, Maze}
    //endregion

    public final static Random RANDOM = new Random();

    /**
     * Class used to pass values between drawing and updating classes
     */
    public GameState(String title, Point levelSize, PointF playerPosition) {
        this(title, levelSize, playerPosition, new ArrayList<GenericSprite>());
    }

    /**
     * Class used to pass values between drawing and updating classes
     */
    public GameState(String title, Point levelSize, PointF playerPosition, List<GenericSprite> sprites) {
        this(title, levelSize, playerPosition, sprites, State.Playing);
    }

    /**
     * Class used to pass values between drawing and updating classes
     */
    public GameState(String title, Point levelSize, PointF playerPosition, List<GenericSprite> sprites, State state) {
        _grav = new float[3];
        _sprites = sprites; // As it's a reference, external class can still add sprites
        _levelSize = levelSize;
        _player = new PlayerSprite(playerPosition.x, playerPosition.y);
        _sprites.add(_player);
        _title = title;
        _offset = new PointF(0, 0);
        _state = state;
    }

    //region Getters & Setters

    /**
     * Gets the title of the level to display
     *
     * @return Title
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Gets the gravity value array
     *
     * @return Gravity values
     */
    public float[] getGravity() {
        return _grav;
    }

    /**
     * Gets the DrawableView associated with this GameState
     *
     * @return
     */
    public DrawableView getView() {
        return _view;
    }

    /**
     * Gets the dimensions of the level
     *
     * @return Level size
     */
    public Point getLevelSize() {
        return _levelSize;
    }

    /**
     * Gets the player sprite in this game
     *
     * @return Player
     */
    public PlayerSprite getPlayer() {
        return _player;
    }

    /**
     * Gets a list of all sprites in the GameState
     *
     * @return List of GenericSprite
     */
    public List<GenericSprite> getSprites() {
        return _sprites;
    }

    /**
     * Gets the score of this game
     *
     * @return
     */
    public int getScore() {
        return _score;
    }

    /**
     * Gets the number of updates the game has had
     *
     * @return Number of ticks
     */
    public int getGameTime() {
        return _ticks;
    }

    /**
     * Gets the number of updates the game has had
     *
     * @return Number of ticks
     */
    public State getState() {
        return _state;
    }

    /**
     * Sets the values of gravity in the array
     *
     * @param x Acceleration in X direction
     * @param y Acceleration in Y direction
     * @param z Acceleration in Z direction
     */
    public void setGravity(float x, float y, float z) {
        _grav[0] = x;
        _grav[1] = y;
        _grav[2] = z;
    }

    /**
     * Sets the view held by this Game State
     * Because of the method in DrawableView, there's a bit of a circular reference
     * This is intentional behaviour. It tries to make sure
     * that the view used by the GameActivity is the same one that's
     * beign given all this information
     *
     * @param view
     */
    public void setView(DrawableView view) {
        if (view != null)
            _view = view;
        else
            throw new IllegalArgumentException("Given view is set to null");
    }

    /**
     * Sets the score to the specified value
     *
     * @param score New score
     */
    public void setScore(int score) {
        _score = score;
    }

    /**
     * Adds the given value onto the score
     *
     * @param score Number to add to the score
     */
    public void addScore(int score) {
        _score += score;
    }
    //endregion

    /**
     * Update loop
     * Handles all updating of the game
     */
    public void update() {
        // Allows us to handle updates separately
        // from drawing if we want to
        // Principle of game design
        // Stops things going wrong when FPS forced to different values
        _ticks++;

        // Do all processing before updating
        List<GenericSprite> toRemove = new ArrayList<GenericSprite>();
        for (GenericSprite s : _sprites) {

            for (GenericSprite t : _sprites) {
                if (t != s) {
                    if (t instanceof ICollidable && s instanceof ICollides && t.intersects(s)) {

                        t.reflect(s);

                    } else if (t instanceof FinishSprite && s instanceof PlayerSprite) {
                        if (t.intersects(s)) {
                            _state = State.Spectating;
                            addScore(1);
                        }
                    } else if (t instanceof DeathSprite && s instanceof PlayerSprite) {
                        if (t.intersects(s)) {
                            _state = State.Spectating;
                        }
                    } else if (t instanceof DisappearingWallSprite && s instanceof PlayerSprite) {
                        if (t.intersects(s)) {
                            toRemove.add(t);
                        }
                    }
                }
            }
        }

        for (GenericSprite s : toRemove)
            _sprites.remove(s);

        for (GenericSprite s : _sprites) {
            if (s instanceof PlayerSprite && _state == State.Spectating) {
                continue;
            }
            s.update(this);
        }
    }

    public void draw(Canvas canvas, float ratio) {

        // Draw all sprites
        _player.draw(canvas, ratio, _offset);
        for (GenericSprite s : _sprites) {
            s.draw(canvas, ratio, _offset);
        }
    }

    /**
     * Generates a level in the form of a GameState object
     *
     * @param l Level to generate
     * @param c Context. Used to get resources
     * @return A new level
     */
    public static GameState GENERATE(Level l, Context c) {
        if (l == Level.Random) {
            int levelX = GameState.RANDOM.nextInt(10) + 10;
            int levelY = GameState.RANDOM.nextInt(10) + 10;
            // Create simple sprites
            List<GenericSprite> sprites = new ArrayList<GenericSprite>();
            // Adding in our walls etc

            sprites.add(new WallSprite(0, -1, levelX, 1));
            sprites.add(new WallSprite(-1, 0, 1, levelY));
            sprites.add(new WallSprite(0, levelY, levelX, 1));
            sprites.add(new WallSprite(levelX, 0, 1, levelY));

            for (int i = 0; i < 5; i++) {

                // _state.getViewSize()[1]
                //_state.getViewSize()[0]
                int left = GameState.RANDOM.nextInt(levelX - 1) + 1;
                int top = GameState.RANDOM.nextInt(levelY - 1) + 1;
                int width = GameState.RANDOM.nextInt(levelX - left) + 1;
                int height = GameState.RANDOM.nextInt(levelY - top) + 1;

                WallSprite wallSprite = new WallSprite(left, top, width, height);
                sprites.add(wallSprite);

            }

            for (int i = 0; i < 5; i++) {

                int left = GameState.RANDOM.nextInt(levelX - 1) + 1;
                int top = GameState.RANDOM.nextInt(levelY - 1) + 1;

                BumperSprite bSprite = new BumperSprite(left, top);
                sprites.add(bSprite);

            }

            sprites.add(new FinishSprite(0, levelY - 1));

            GameState newLevel = new GameState(c.getString(R.string.level_random), new Point(levelX, levelY), new PointF(0, 0), sprites);
            return newLevel;
        } else if (l == Level.Scrolling) {
            List<GenericSprite> sprites = new ArrayList<GenericSprite>();

            FinishSprite fs = new FinishSprite(10, 0, 1, 5);
            sprites.add(fs);

            sprites.add(new WallSprite(3, 0, 4, 1));
            sprites.add(new WallSprite(3, 2, 1, 3));
            sprites.add(new WallSprite(5, 1, 2, 2));
            sprites.add(new WallSprite(5, 4, 1, 1));
            sprites.add(new WallSprite(7, 3, 1, 1));
            sprites.add(new WallSprite(8, 1, 1, 1));
            sprites.add(new WallSprite(9, 2, 1, 3));
            sprites.add(new WallSprite(10, 0, 1, 1));
            sprites.add(new WallSprite(10, 2, 1, 3));
            sprites.add(new WallSprite(11, 0, 5, 5));

            DeathSprite ds = new DeathSprite(-0.75f, 0, 1, 5);
            ds.setMotion(0.01f, 0);
            sprites.add(ds);

            StickyWallSprite sws = new StickyWallSprite(4.75f, 0, 1, 5);
            sws.setMotion(0.01f, 0f);
            sprites.add(sws);

            sprites.add(new WallSprite(0, -1, 10, 1));
            sprites.add(new WallSprite(0, 5, 10, 1));

            //sprites.add(new WallSprite(1, 1, 3, 3));

            return new ScrollingGameState(c.getString(R.string.level_scrolling), new Point(5, 5), new PointF(2.5f, 2.5f), sprites, -0.01f);
        } else if (l == Level.Empty) {
            List<GenericSprite> sprites = new ArrayList<GenericSprite>();
            sprites.add(new WallSprite(0, -1, 10, 1));
            sprites.add(new WallSprite(-1, 0, 1, 10));
            sprites.add(new WallSprite(0, 10, 10, 1));
            sprites.add(new WallSprite(10, 0, 1, 10));
            return new GameState(c.getString(R.string.level_empty), new Point(10, 10), new PointF(5, 5), sprites);
        } else if (l == Level.LevelOne) {
            List<GenericSprite> sprites = new ArrayList<GenericSprite>();

            FinishSprite fs = new FinishSprite(6, 2, 1, 1);
            sprites.add(fs);

            sprites.add(new WallSprite(1, 0, 1, 1));
            sprites.add(new WallSprite(0, 2, 4, 1));
            sprites.add(new WallSprite(3, 1, 1, 1));
            sprites.add(new WallSprite(0, 3, 1, 3));
            sprites.add(new WallSprite(2, 4, 4, 1));
            sprites.add(new WallSprite(5, 0, 1, 4));
            sprites.add(new WallSprite(7, 5, 1, 1));
            sprites.add(new WallSprite(9, 4, 1, 1));
            sprites.add(new WallSprite(6, 3, 3, 1));
            sprites.add(new WallSprite(7, 1, 1, 2));
            sprites.add(new WallSprite(9, 0, 2, 2));
            sprites.add(new WallSprite(10, 2, 1, 1));

            sprites.add(new WallSprite(0, -1, 11, 1));
            sprites.add(new WallSprite(-1, 0, 1, 6));
            sprites.add(new WallSprite(0, 6, 11, 1));
            sprites.add(new WallSprite(11, 0, 1, 6));

            return new GameState(c.getString(R.string.level_one), new Point(11, 6), new PointF(0, 0), sprites);
        } else if (l == Level.Death) {
            List<GenericSprite> sprites = new ArrayList<GenericSprite>();

            FinishSprite fs = new FinishSprite(1, 1, 1, 1);
            sprites.add(fs);

            sprites.add(new DeathSprite(0, 0, 4, 1));
            sprites.add(new DeathSprite(0, 1, 1, 4));
            sprites.add(new DeathSprite(1, 4, 4, 1));
            sprites.add(new DeathSprite(4, 0, 1, 4));
            sprites.add(new DeathSprite(1, 2, 2, 1));

            return new GameState(c.getString(R.string.level_death), new Point(5, 5), new PointF(1.25f, 3.25f), sprites);
        } else if (l == Level.Happy) {
            List<GenericSprite> sprites = new ArrayList<GenericSprite>();

            FinishSprite fs = new FinishSprite(1, 1, 1, 1);
            sprites.add(fs);

            sprites.add(new WallSprite(0, 0, 4, 1));
            sprites.add(new WallSprite(0, 1, 1, 4));
            sprites.add(new WallSprite(1, 4, 4, 1));
            sprites.add(new WallSprite(4, 0, 1, 4));
            sprites.add(new WallSprite(1, 2, 2, 1));

            return new GameState(c.getString(R.string.level_happy), new Point(5, 5), new PointF(1.25f, 3.25f), sprites);
        } else if (l == Level.Maze) {
            List<GenericSprite> sprites = new ArrayList<GenericSprite>();

            FinishSprite fs = new FinishSprite(5, 17, 1, 1);
            sprites.add(fs);

            //region LONG LIST OF SPRITES; KEEP COLLAPSED
            sprites.add(new WallSprite(0, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 2, 1, 1));
            sprites.add(new WallSprite(0, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 4, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 6, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 7, 1, 1));
            sprites.add(new WallSprite(0, 8, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 10, 1, 1));
            sprites.add(new WallSprite(0, 11, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 12, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 13, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 15, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 16, 1, 1));
            sprites.add(new WallSprite(0, 17, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 18, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(0, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 0, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 2, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 4, 1, 1));
            sprites.add(new WallSprite(1, 5, 1, 1));
            sprites.add(new WallSprite(1, 6, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 7, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 8, 1, 1));
            sprites.add(new WallSprite(1, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 10, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 11, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 12, 1, 1));
            sprites.add(new WallSprite(1, 13, 1, 1));
            sprites.add(new WallSprite(1, 14, 1, 1));
            sprites.add(new WallSprite(1, 15, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 16, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 17, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 18, 1, 1));
            sprites.add(new WallSprite(1, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(1, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 0, 1, 1));
            sprites.add(new WallSprite(2, 1, 1, 1));
            sprites.add(new WallSprite(2, 2, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 3, 1, 1));
            sprites.add(new WallSprite(2, 4, 1, 1));
            sprites.add(new WallSprite(2, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 6, 1, 1));
            sprites.add(new WallSprite(2, 7, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 8, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 10, 1, 1));
            sprites.add(new WallSprite(2, 11, 1, 1));
            sprites.add(new WallSprite(2, 12, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 13, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 15, 1, 1));
            sprites.add(new WallSprite(2, 16, 1, 1));
            sprites.add(new WallSprite(2, 17, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 18, 1, 1));
            sprites.add(new DisappearingWallSprite(2, 19, 1, 1));
            sprites.add(new WallSprite(2, 20, 1, 1));
            sprites.add(new WallSprite(3, 0, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 2, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 3, 1, 1));
            sprites.add(new WallSprite(3, 4, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 6, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 7, 1, 1));
            sprites.add(new WallSprite(3, 8, 1, 1));
            sprites.add(new WallSprite(3, 9, 1, 1));
            sprites.add(new WallSprite(3, 10, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 11, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 12, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 13, 1, 1));
            sprites.add(new WallSprite(3, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 15, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 16, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 17, 1, 1));
            sprites.add(new WallSprite(3, 18, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(3, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 0, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 1, 1, 1));
            sprites.add(new WallSprite(4, 2, 1, 1));
            sprites.add(new WallSprite(4, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 4, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 5, 1, 1));
            sprites.add(new WallSprite(4, 6, 1, 1));
            sprites.add(new WallSprite(4, 7, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 8, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 10, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 11, 1, 1));
            sprites.add(new WallSprite(4, 12, 1, 1));
            sprites.add(new WallSprite(4, 13, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 15, 1, 1));
            sprites.add(new WallSprite(4, 16, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 17, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 18, 1, 1));
            sprites.add(new WallSprite(4, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(4, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 0, 1, 1));
            sprites.add(new WallSprite(5, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 2, 1, 1));
            sprites.add(new WallSprite(5, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 4, 1, 1));
            sprites.add(new WallSprite(5, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 6, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 7, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 8, 1, 1));
            sprites.add(new WallSprite(5, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 10, 1, 1));
            sprites.add(new WallSprite(5, 11, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 12, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 13, 1, 1));
            sprites.add(new WallSprite(5, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 15, 1, 1));
            sprites.add(new WallSprite(5, 16, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 17, 1, 1));
            sprites.add(new WallSprite(5, 18, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(5, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 0, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 2, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 4, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 6, 1, 1));
            sprites.add(new WallSprite(6, 7, 1, 1));
            sprites.add(new WallSprite(6, 8, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 10, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 11, 1, 1));
            sprites.add(new WallSprite(6, 12, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 13, 1, 1));
            sprites.add(new WallSprite(6, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 15, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 16, 1, 1));
            sprites.add(new WallSprite(6, 17, 1, 1));
            sprites.add(new WallSprite(6, 18, 1, 1));
            sprites.add(new DisappearingWallSprite(6, 19, 1, 1));
            sprites.add(new WallSprite(6, 20, 1, 1));
            sprites.add(new WallSprite(7, 0, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 2, 1, 1));
            sprites.add(new WallSprite(7, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 4, 1, 1));
            sprites.add(new WallSprite(7, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 6, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 7, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 8, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 9, 1, 1));
            sprites.add(new WallSprite(7, 10, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 11, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 12, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 13, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 14, 1, 1));
            sprites.add(new WallSprite(7, 15, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 16, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 17, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 18, 1, 1));
            sprites.add(new WallSprite(7, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(7, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 0, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 1, 1, 1));
            sprites.add(new WallSprite(8, 2, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 3, 1, 1));
            sprites.add(new WallSprite(8, 4, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 6, 1, 1));
            sprites.add(new WallSprite(8, 7, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 8, 1, 1));
            sprites.add(new WallSprite(8, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 10, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 11, 1, 1));
            sprites.add(new WallSprite(8, 12, 1, 1));
            sprites.add(new WallSprite(8, 13, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 15, 1, 1));
            sprites.add(new WallSprite(8, 16, 1, 1));
            sprites.add(new WallSprite(8, 17, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 18, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(8, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 0, 1, 1));
            sprites.add(new WallSprite(9, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 2, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 4, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 5, 1, 1));
            sprites.add(new WallSprite(9, 6, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 7, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 8, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 9, 1, 1));
            sprites.add(new WallSprite(9, 10, 1, 1));
            sprites.add(new WallSprite(9, 11, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 12, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 13, 1, 1));
            sprites.add(new WallSprite(9, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 15, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 16, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 17, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 18, 1, 1));
            sprites.add(new WallSprite(9, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(9, 20, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 0, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 1, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 2, 1, 1));
            sprites.add(new WallSprite(10, 3, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 4, 1, 1));
            sprites.add(new WallSprite(10, 5, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 6, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 7, 1, 1));
            sprites.add(new WallSprite(10, 8, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 9, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 10, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 11, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 12, 1, 1));
            sprites.add(new WallSprite(10, 13, 1, 1));
            sprites.add(new WallSprite(10, 14, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 15, 1, 1));
            sprites.add(new WallSprite(10, 16, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 17, 1, 1));
            sprites.add(new WallSprite(10, 18, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 19, 1, 1));
            sprites.add(new DisappearingWallSprite(10, 20, 1, 1));
            //endregion

            sprites.add(new WallSprite(0, -1, 11, 1));
            sprites.add(new WallSprite(-1, 0, 1, 21));
            sprites.add(new WallSprite(0, 21, 11, 1));
            sprites.add(new WallSprite(11, 0, 1, 21));

            return new GameState(c.getString(R.string.level_maze), new Point(11, 21), new PointF(0.25f, 0.25f), sprites);
        }


        throw new IllegalArgumentException("Level not defined yet");
    }

    /**
     * Gets the GameState.Level that corresponds to the given string
     *
     * @param name String representation of the level name
     * @return Requested GameState.Level
     */
    public static Level getLevelFromString(String name) {
        switch (name) {
            case "Random":
                return Level.Random;
            case "Level One":
                return Level.LevelOne;
            case "Scrolling":
                return Level.Scrolling;
            case "Empty":
                return Level.Empty;
            case "Happy Valley":
                return Level.Happy;
            case "Death Valley":
                return Level.Death;
            case "Maze":
                return Level.Maze;
            default:
                throw new IllegalArgumentException("Level not defined yet");
        }
    }
}
