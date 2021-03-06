package twoohfour.cms.waikato.ac.nz.ballgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 11/10/2015.
 */
public class MultiPlayerGhostSprite extends CircleSprite {

    boolean _ready = false;

    private static final int[] COLORS = new int[]{
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.YELLOW
    };

    public MultiPlayerGhostSprite(float x, float y) {
        this(x, y, 1);
    }

    public MultiPlayerGhostSprite(float x, float y, float weight) {
        super(x, y, 0.5f, 0.5f, weight);

        int colorIndex = GameState.RANDOM.nextInt(COLORS.length);
        _paint.setColor(COLORS[colorIndex]);
        _paint.setAlpha(128);
    }

    public boolean isReady() {
        return _ready;
    }

    public void setReady() {
        _ready = true;
    }


    /**
     * Draw the player
     *
     * @param canvas Canvas to draw on
     * @param scale  Scale at which to draw
     */
    @Override
    public void draw(Canvas canvas, float scale, PointF offset) {
        float radius = getWidth() / 2;
        canvas.drawCircle((getXPos() + radius + offset.x) * scale, (getYPos() + radius + offset.y) * scale, radius * scale, _paint);
    }

    /**
     * Update the player
     *
     * @param state Current state of the game
     */
    @Override
    public void update(GameState state) {
        // Update the player's motion

        super.update(state);
    }

}
