package edu.msu.becketta.steampunked;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * A representation of a pipe
 */
public class Pipe implements Serializable {


    /**
     * Enum that describes the pipe type
     */
    public enum pipeType {
        START,
        END,
        STRAIGHT,
        RIGHT_ANGLE,
        CAP,
        TEE
    }

    /**
     * Pipe groups for each player
     */
    public enum PipeGroup {
        PLAYER_ONE,
        PLAYER_TWO
    }

    /**
     * Save the type of the pipe
     */
    private pipeType type;

    /**
     * Group that this pipe is part of
     */
    private PipeGroup group;

    /**
     * Playing area this pipe is a member of
     */
    private transient PlayingArea playingArea = null;

    /**
     * Array that indicates which sides of this pipe
     * has flanges. The order is north, east, south, west.
     *
     * As an example, a T that has a horizontal pipe
     * with the T open to the bottom would be:
     *
     * false, true, true, true
     */
    private boolean[] connect = {false, false, false, false};

    /**
     * X and Y location in the playing area (index into array)
     */
    private int xCoord = 0;
    private int yCoord = 0;

    /**
     * X and Y location on the screen
     */
    private float x = 0f;
    private float y = 0f;

    /**
     * Bitmap used to store the pipe image
     */
    private transient Bitmap bitmap;

    /**
     * Bitmap use to store the start pipe handle if needed
     */
    private transient Bitmap handleBit = null;

    /**
     * Steam bitmap
     */
    private transient Bitmap steamBit = null;

    /**
     * Is the gauge empty or full (only for END pipes
     */
    private boolean gaugeFull = false;

    /**
     * Rotation of the bitmap and handle
     */
    private float bitmapRotation = 0f;
    private float handleRotation = 0f;

    /**
     * Depth-first visited visited
     */
    private transient boolean visited = false;

    /**
     * Paint object for the gauge line
     */
    private transient Paint gaugePaint = null;

    /**
     * Paint object for the player name
     */
    private transient TextPaint namePaint = null;

    /**
     * Name of the player needed if this is a start pipe
     */
    private String playerName = null;

    /**
     * Constructor
     * @param context Context to get the pipe's bitmap from
     * @param type Type of the pipe
     */
    public Pipe(Context context, pipeType type) {
        steamBit = BitmapFactory.decodeResource(context.getResources(), R.drawable.leak);

        this.type = type;
        switch(type) {
            case START:
                setConnections(false, true, false, false);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.straight);
                bitmapRotation = 90f;
                handleBit = BitmapFactory.decodeResource(context.getResources(), R.drawable.handle);
                break;
            case END:
                setConnections(false, false, false, true);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gauge);
                bitmapRotation = -90f;
                break;
            case STRAIGHT:
                setConnections(true, false, true, false);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.straight);
                break;
            case RIGHT_ANGLE:
                setConnections(false, true, true, false);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.a90);
                break;
            case CAP:
                setConnections(false, false, true, false);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cap);
                break;
            case TEE:
                setConnections(true, true, true, false);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.tee);
                break;
        }

        if(type == pipeType.START) {
            namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(Color.BLACK);
            namePaint.setTextSize(50f);
        }

        if(type == pipeType.END) {
            gaugePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            gaugePaint.setColor(Color.RED);
            gaugePaint.setStrokeWidth(6f);
        }
    }

    public static Pipe fieldPipeFromXml(XmlPullParser xml, Context context, PlayingArea field) throws IOException, XmlPullParserException {
        String t = xml.getAttributeValue(null, "type");
        pipeType type = pipeType.values()[Integer.getInteger(t)];
        Pipe newPipe = new Pipe(context, type);

        String g = xml.getAttributeValue(null, "group");
        PipeGroup group = PipeGroup.values()[Integer.getInteger(g)];
        newPipe.setGroup(group);

        float rot = Float.parseFloat(xml.getAttributeValue(null, "rotation"));
        newPipe.setBitmapRotation(rot);
        newPipe.snapRotation();

        int xCoord = Integer.parseInt(xml.getAttributeValue(null, "x"));
        int yCoord = Integer.parseInt(xml.getAttributeValue(null, "y"));
        newPipe.set(field, xCoord, yCoord);

        return newPipe;
    }

    public void fieldPipeToXml(XmlSerializer xml) throws IOException {
        xml.startTag(null, "pipe");

        xml.attribute(null, "type", type.toString());
        xml.attribute(null, "group", group.toString());
        xml.attribute(null, "rotation", Float.toString(bitmapRotation));
        xml.attribute(null, "x", Integer.toString(xCoord));
        xml.attribute(null, "y", Integer.toString(yCoord));
        if (type == pipeType.START) {
            if (playerName != null) {
                xml.attribute(null, "label", playerName);
            } else {
                xml.attribute(null, "label", "");
            }
        }

        xml.endTag(null, "pipe");
    }

    public static Pipe bankPipeFromXml(XmlPullParser xml, Context context) throws IOException, XmlPullParserException {
        String t = xml.getAttributeValue(null, "type");
        pipeType type = pipeType.values()[Integer.getInteger(t)];
        return new Pipe(context, type);
    }

    public void bankPipeToXml(XmlSerializer xml) throws IOException {
        xml.startTag(null, "pipe");

        xml.attribute(null, "type", type.toString());

        xml.endTag(null, "pipe");
    }

    public void resetPipe() {
        setLocation(0f, 0f);
        resetConnections();
    }

    public void resetConnections() {
        bitmapRotation = 0f;
        switch(type) {
            case START:
                setConnections(false, true, false, false);
                bitmapRotation = 90f;
                break;
            case END:
                setConnections(false, false, false, true);
                bitmapRotation = -90f;
                break;
            case STRAIGHT:
                setConnections(true, false, true, false);
                break;
            case RIGHT_ANGLE:
                setConnections(false, true, true, false);
                break;
            case CAP:
                setConnections(false, false, true, false);
                break;
            case TEE:
                setConnections(true, true, true, false);
                break;
        }
    }

    private void setConnections(boolean north, boolean east, boolean south, boolean west) {
        connect[0] = north;
        connect[1] = east;
        connect[2] = south;
        connect[3] = west;
    }

    public float getBitmapRotation() {
        return bitmapRotation;
    }

    public void setBitmapRotation(float bitmapRotation) {
        this.bitmapRotation = bitmapRotation;
    }

    public void snapRotation() {
        // Find what is the closest quarter rotation to the current rotation
        int quarterRotations = Math.round(bitmapRotation / 90f) % 4;

        // Update the rotation to the nearest 90 degrees
        bitmapRotation = quarterRotations * 90f;

        // Change the connect array accordingly
        boolean[] newConnect = new boolean[4];
        for(int i = 0; i < connect.length; i++) {
            int newIndex = (i+quarterRotations) % 4;
            if(newIndex < 0) newIndex += 4;
            newConnect[newIndex] = connect[i];
        }
        connect = newConnect;
    }

    /**
     * Search to see if there are any downstream of this pipe
     *
     * This does a simple depth-first search to find any connections
     * that are not, in turn, connected to another pipe. It also
     * set the visited flag in all pipes it does visit, so you can
     * tell if a pipe is reachable from this pipe by checking that flag.
     * @return True if no leaks in the pipe
     */
    public boolean search() {
        visited = true;

        for(int d=0; d<4; d++) {
            /*
             * If no connection this direction, ignore
             */
            if(!connect[d]) {
                continue;
            }

            Pipe n = neighbor(d);
            if(n == null) {
                // We leak
                // We have a connection with nothing on the other side
                return false;
            }

            // What is the matching location on
            // the other pipe. For example, if
            // we are looking in direction 1 (east),
            // the other pipe must have a connection
            // in direction 3 (west)
            int dp = (d + 2) % 4;
            if(!n.connect[dp]) {
                // We have a bad connection, the other side is not
                // a flange to connect to
                return false;
            }

            if(n.visited) {
                // Already visited this one, so no leaks this way
                continue;
            } else {
                // Is there a lead in that direction
                if(!n.search()) {
                    // We found a leak downstream of this pipe
                    return false;
                }
            }
        }

        // Yah, no leaks
        return true;
    }

    public boolean validConnection() {
        boolean atLeastOneConnection = false;
        for(int d=0; d<4; d++) {
            /*
             * If no connection this direction, ignore
             */
            if(!connect[d]) {
                continue;
            }

            Pipe n = neighbor(d);
            if(n == null) {
                // We have a connection with nothing on the other side
                continue;
            }

            // What is the matching location on
            // the other pipe. For example, if
            // we are looking in direction 1 (east),
            // the other pipe must have a connection
            // in direction 3 (west)
            int coorespondingDirection = (d + 2) % 4;
            if(n.connect[coorespondingDirection]) {
                atLeastOneConnection = true;
                if(n.getGroup() != this.group) {
                    return false;
                }
            }
        }

        if(!atLeastOneConnection) {
            return false;
        }

        return true;
    }

    /**
     * Find the neighbor of this pipe
     * @param d Index (north=0, east=1, south=2, west=3)
     * @return Pipe object or null if no neighbor
     */
    private Pipe neighbor(int d) {
        switch(d) {
            case 0:
                return playingArea.getPipe(xCoord, yCoord-1);

            case 1:
                return playingArea.getPipe(xCoord+1, yCoord);

            case 2:
                return playingArea.getPipe(xCoord, yCoord+1);

            case 3:
                return playingArea.getPipe(xCoord-1, yCoord);
        }

        return null;
    }

    /**
     * Get the playing area
     * @return Playing area object
     */
    public PlayingArea getPlayingArea() {
        return playingArea;
    }

    /**
     * Get the default height of the pipe bitmap when drawn,
     * used to calculate scale in the GameView
     * @return The height of the bitmap
     */
    public float getBitmapHeight() {
        return bitmap.getHeight();
    }
    public float getBitmapWidth() {
        return bitmap.getWidth();
    }

    /**
     * Set the playing area and location for this pipe
     * @param playingArea Playing area we are a member of
     * @param x X index
     * @param y Y index
     */
    public void set(PlayingArea playingArea, int x, int y) {
        this.playingArea = playingArea;
        this.xCoord = x;
        this.yCoord = y;
    }

    /**
     * Set the location of the pipe
     * @param x X location
     * @param y Y location
     */
    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getXLoc() {
        return xCoord;
    }

    public int getYLoc() {
        return yCoord;
    }

    /**
     * Has this pipe been visited by a search?
     * @return True if yes
     */
    public boolean beenVisited() {
        return visited;
    }

    /**
     * Set the visited flag for this pipe
     * @param visited Value to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Draw the pipe in relative location withing the playing area
     * @param canvas Canvas to draw on
     */
    public void drawInPlayingArea(Canvas canvas) {
        if(playingArea != null) {
            // Calculate the x and y locations withing the playing area
            float bitDim = bitmap.getHeight() < bitmap.getWidth() ? bitmap.getHeight() : bitmap.getWidth();
            this.x = coordinateToLocation(xCoord, bitDim);
            this.y = coordinateToLocation(yCoord, bitDim);


            draw(canvas);

            if(type != pipeType.END) {
                // Draw steam at each opening
                for(int d=0; d<4; d++) {
                /*
                 * If no connection this direction, ignore
                 */
                    if(!connect[d]) {
                        continue;
                    }

                    Pipe n = neighbor(d);
                    if(n == null) {
                        // We have a connection with nothing on the other side
                        drawSteam(canvas, d, bitDim);
                        continue;
                    }

                    // What is the matching location on
                    // the other pipe. For example, if
                    // we are looking in direction 1 (east),
                    // the other pipe must have a connection
                    // in direction 3 (west)
                    int coorespondingDirection = (d + 2) % 4;
                    if(!n.connect[coorespondingDirection]) {
                        drawSteam(canvas, d, bitDim);
                    }
                }
            }
        }
    }

    /**
     * Draw the pipe
     * @param canvas Canvas to draw on
     */
    public void draw(Canvas canvas) {
        float bitDim = bitmap.getHeight() < bitmap.getWidth() ? bitmap.getHeight() : bitmap.getWidth();

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(bitmapRotation);
        canvas.translate(-(bitDim / 2), -(bitDim / 2));

        // Draw pipe bitmap
        canvas.drawBitmap(bitmap, 0, 0, null);

        // Draw gauge line if this is an END pipe
        drawGaugeLine(canvas);

        canvas.restore();

        // Draw handle if there is one
        if (handleBit != null) {
            canvas.save();
            canvas.translate(x, y);
            canvas.rotate(handleRotation);
            canvas.translate(-bitDim / 2, -bitDim / 2);
            canvas.drawBitmap(handleBit, 0, 0, null);
            canvas.restore();
        }

        // Draw player name if this is a START pipe
        if(type == pipeType.START && playerName != null) {
            drawPlayerName(canvas);
        }
    }

    private void drawSteam(Canvas canvas, int dir, float blockDim) {
        int x = this.xCoord;
        int y = this.yCoord;
        float rotation = 0f;
        switch(dir) {
            case 0:
                y--;
                break;
            case 1:
                x++;
                rotation = 90f;
                break;
            case 2:
                y++;
                rotation = 180f;
                break;
            case 3:
                x--;
                rotation = -90f;
                break;
        }

        // Translate new x, y coordinates to location in the playing area

        canvas.save();
        canvas.translate(coordinateToLocation(x, blockDim), coordinateToLocation(y, blockDim));
        canvas.rotate(rotation);
        canvas.translate(-(blockDim / 2), -(blockDim / 2));

        // Draw pipe bitmap
        canvas.drawBitmap(steamBit, 0, 0, null);

        canvas.restore();
    }

    private float coordinateToLocation(int coord, float blockDim) {
        return (coord * blockDim) + (blockDim / 2);
    }

    private void drawPlayerName(Canvas canvas) {
        int theMagicNumber = 7;
        canvas.save();
        canvas.translate(x-(bitmap.getWidth()/2)+theMagicNumber, y+(bitmap.getHeight()/2)+theMagicNumber);
        canvas.drawText(playerName, 0, 0, namePaint);
        canvas.restore();
    }

    private void drawGaugeLine(Canvas canvas) {
        if (type == pipeType.END) {
            float x1 = bitmap.getWidth() * 0.71f;
            float y1 = bitmap.getHeight() / 2f - 2;
            float xdiff = 50f;
            float ydiff = 50f;
            if(gaugeFull) {
                // Draw the gauge line at full
                canvas.drawLine(x1, y1, x1 - xdiff, y1 + ydiff, gaugePaint);
            } else {
                // Draw the gauge line at empty
                canvas.drawLine(x1, y1, x1 - xdiff, y1 - ydiff, gaugePaint);
            }
        }
    }

    public void setGaugeFull() {
        gaugeFull = true;
    }

    public void setHandleOpen() {
        handleRotation = 90f;
    }

    /**
     * Check if a location is on the bitmap
     * @param x X location of touch
     * @param y Y location of touch
     * @return True if the x, y location is on the bitmap
     */
    public boolean hit(float x, float y) {
        if(x >= this.x - bitmap.getWidth() && x <= this.x + bitmap.getWidth() &&
           y >= this.y - bitmap.getHeight() && y <= this.y + bitmap.getHeight()) {
            return true;
        }

        return false;
    }

    public PipeGroup getGroup() {
        return group;
    }

    public void setGroup(PipeGroup group) {
        this.group = group;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
