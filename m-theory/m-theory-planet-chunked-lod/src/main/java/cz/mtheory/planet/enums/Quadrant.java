/**
 * 
 */
package cz.mtheory.planet.enums;

import cz.mtheory.planet.config.ChunkConstants;

/** 
 * M-theory project
 *
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class Quadrant {
    private static final String[] INDEX_NAME = new String[] { "LEFT_BOTTOM", "RIGHT_BOTTOM", "LEFT_TOP", "RIGHT_TOP" };

    private final int index;
    private final int offsetLeft;
    private final int offsetTop;
    private final int offsetRight;
    private final int offsetBottom;

    public Quadrant(int index, ChunkConstants c) {
        this.index = index;
        switch (index) {
            case 1:
                this.offsetLeft = c.vertexesOnHalfEdge;
                this.offsetTop = 0;
                break;
            case 2:
                this.offsetLeft = 0;
                this.offsetTop = c.vertexesOnHalfEdge;
                break;
            case 3:
                this.offsetLeft = c.vertexesOnHalfEdge;
                this.offsetTop = c.vertexesOnHalfEdge;
                break;
            default:
                this.offsetLeft = 0;
                this.offsetTop = 0;
                break;
        }
        this.offsetRight = this.offsetLeft + c.vertexesOnHalfEdgePlusOne;
        this.offsetBottom = this.offsetTop + c.vertexesOnHalfEdgePlusOne;
    }

    @Override
    public String toString() {
        return INDEX_NAME[index];
    }

    public int getIndex() {
        return index;
    }

    public int getOffsetLeft() {
        return offsetLeft;
    }

    public int getOffsetTop() {
        return offsetTop;
    }

    public int getOffsetRight() {
        return offsetRight;
    }

    public int getOffsetBottom() {
        return offsetBottom;
    }

}
