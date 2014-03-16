/**
 * 
 */
package cz.mtheory.planet.config;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.ardor3d.scenegraph.IntBufferData;
import com.ardor3d.util.geom.BufferUtils;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class ChunkBuffers {

    /* InBufferData[] :
     * 0 - full
     * 1 - left
     * 2 - left top
     * 3 - top
     * 4 - top right
     * 5 - right
     * 6 - right bottom
     * 7 - bottom
     * 8 - bottom left */
    private static Map<Integer, IntBufferData[]> indexBuffersCache = new HashMap<Integer, IntBufferData[]>();

    protected static IntBufferData[] createIndexBuffers(int vertexesOnEdge) {
        IntBufferData[] bufData = indexBuffersCache.get(vertexesOnEdge);
        if (bufData == null) {
            bufData = new IntBufferData[9]; //
            IntBuffer[] buf = new IntBuffer[bufData.length];
            int vertexesOnEdgePlusOne = vertexesOnEdge + 1;

            // *************************************************************************************************************************
            int b = 0; // full
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            for (int y = 0; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge);
                }
            }

            // *************************************************************************************************************************
            b = 1; // left
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 3);
            for (int y = 0; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x > 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index + 1);
                    buf[b].put(index + vertexesOnEdge);
                }
            }
            buf[b].put(vertexesOnEdge * vertexesOnEdgePlusOne + 1);
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                buf[b].put(i * vertexesOnEdgePlusOne);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne + 1);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2 + 1);
            }

            // *************************************************************************************************************************
            b = 2; // left top
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 5);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge; x > 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                final int index = (y + 1) * vertexesOnEdgePlusOne;
                buf[b].put(index + 1);
                buf[b].put(index + vertexesOnEdge);
            }
            int foo = (vertexesOnEdge - 1) * vertexesOnEdgePlusOne;
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                buf[b].put(i + foo);
                buf[b].put(i + foo + vertexesOnEdgePlusOne);
                buf[b].put(i + foo - 1);
                buf[b].put(i - 2 + foo + vertexesOnEdgePlusOne);
            }
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                if (i != vertexesOnEdge) buf[b].put(i * vertexesOnEdgePlusOne);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne + 1);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2 + 1);
            }

            // *************************************************************************************************************************
            b = 3; // top
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 5);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                final int index = (y + 1) * vertexesOnEdgePlusOne;
                buf[b].put(index);
                buf[b].put(index + vertexesOnEdge);
            }
            foo = (vertexesOnEdge - 1) * vertexesOnEdgePlusOne;
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                buf[b].put(i + foo);
                buf[b].put(i + foo + vertexesOnEdgePlusOne);
                buf[b].put(i + foo - 1);
                buf[b].put(i - 2 + foo + vertexesOnEdgePlusOne);
            }
            buf[b].put(foo);

            // *************************************************************************************************************************
            b = 4; // top right
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                buf[b].put(t);
                buf[b].put(t + 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne + 1);
            }
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                if (i != vertexesOnEdge) {
                    buf[b].put(i + foo);
                    buf[b].put(i + foo + vertexesOnEdgePlusOne);
                }
                buf[b].put(i + foo - 1);
                buf[b].put(i - 2 + foo + vertexesOnEdgePlusOne);
            }
            buf[b].put(foo);
            buf[b].put(foo);
            buf[b].put(vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge - 1);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 2) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }

            // *************************************************************************************************************************
            b = 5; // right
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 1);
            for (int y = 0; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }
            buf[b].put(vertexesOnEdge * vertexesOnEdgePlusOne);
            buf[b].put(vertexesOnEdge - 1);
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                buf[b].put(t);
                buf[b].put(t + 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne + 1);
            }
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            // *************************************************************************************************************************
            b = 6; // right bottom
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                buf[b].put(i + vertexesOnEdgePlusOne);
                buf[b].put(i);
                buf[b].put(i + vertexesOnEdgePlusOne + 1);
                buf[b].put(i + 2);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                if (i != 0) {
                    buf[b].put(t);
                    buf[b].put(t + 1);
                }
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne + 1);
            }
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdge * 2);
            buf[b].put(vertexesOnEdge * 2);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }

            // *************************************************************************************************************************
            b = 7; // bottom 
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                buf[b].put(i + vertexesOnEdgePlusOne);
                buf[b].put(i);
                buf[b].put(i + vertexesOnEdgePlusOne + 1);
                buf[b].put(i + 2);
            }
            buf[b].put(2 * vertexesOnEdge + 1);
            buf[b].put(2 * vertexesOnEdge + 1);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge);
                }
            }

            // *************************************************************************************************************************
            b = 8; // bottom left
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            buf[b].put(vertexesOnEdge * vertexesOnEdgePlusOne + 1);
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                buf[b].put(i * vertexesOnEdgePlusOne);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne + 1);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2);
                if (i > 2) buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2 + 1);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                if (i != 0) {
                    buf[b].put(i + vertexesOnEdgePlusOne);
                    buf[b].put(i);
                }
                buf[b].put(i + vertexesOnEdgePlusOne + 1);
                buf[b].put(i + 2);
            }
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x >= 1; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index + 1);
                    buf[b].put(index + vertexesOnEdge);
                }
            }
/*
            // *************************************************************************************************************************
            b = 9; // left - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2);
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i * vertexesOnEdgePlusOne + 1;
                buf[b].put(t);
                buf[b].put(t - 1);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t - 1);
                buf[b].put(t - 2 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
                buf[b].put(t - 3 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
            }
            buf[b].put(1);
            buf[b].put(1);
            buf[b].put(vertexesOnEdge);
            buf[b].put(vertexesOnEdge);
            for (int y = 0; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x > 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index + 1);
                    buf[b].put(index + vertexesOnEdge);
                }
            }

            //*************************************************************************************************************************
            b = 10; // left top - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i + (vertexesOnEdge * vertexesOnEdge - 1);
                buf[b].put(t);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t - 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t - 2);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
                buf[b].put(t - 3);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
            }
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i * vertexesOnEdgePlusOne + 1;
                if (i != vertexesOnEdge) {
                    buf[b].put(t);
                    buf[b].put(t - 1);
                    buf[b].put(t - vertexesOnEdgePlusOne);
                    buf[b].put(t - 1);
                }
                buf[b].put(t - 2 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
                buf[b].put(t - 3 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
            }
            buf[b].put(1);
            buf[b].put(1);
            buf[b].put(vertexesOnEdge);
            buf[b].put(vertexesOnEdge);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge; x > 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                final int index = (y + 1) * vertexesOnEdgePlusOne;
                buf[b].put(index + 1);
                buf[b].put(index + vertexesOnEdge);
            }

            // *************************************************************************************************************************
            b = 11; // top - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 2);
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i + (vertexesOnEdge * vertexesOnEdge - 1);
                buf[b].put(t);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t - 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t - 2);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
                buf[b].put(t - 3);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
            }
            buf[b].put(vertexesOnEdge * vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge * vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge);
            buf[b].put(vertexesOnEdge);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                final int index = (y + 1) * vertexesOnEdgePlusOne;
                buf[b].put(index);
                buf[b].put(index + vertexesOnEdge);
            }

            // *************************************************************************************************************************
            b = 12; // top right - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                buf[b].put(t);
                buf[b].put(t + 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 1);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
                buf[b].put(t + 3 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
            }
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i + (vertexesOnEdge * vertexesOnEdge - 1);
                if (i != vertexesOnEdge) {
                    buf[b].put(t);
                    buf[b].put(t + vertexesOnEdgePlusOne);
                    buf[b].put(t - 1);
                    buf[b].put(t + vertexesOnEdgePlusOne);
                }
                buf[b].put(t - 2);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
                buf[b].put(t - 3);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
            }
            buf[b].put(vertexesOnEdge * vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge * vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge - 1);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 2) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }
            // *************************************************************************************************************************
            b = 13; // right - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2);
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                buf[b].put(t);
                buf[b].put(t + 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 1);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
                buf[b].put(t + 3 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
            }
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge - 1);
            for (int y = 0; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }

            // *************************************************************************************************************************
            b = 14; // right bottom - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i + vertexesOnEdgePlusOne;
                buf[b].put(t);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t + 1);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t + 2);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
                buf[b].put(t + 3);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                if (i != 0) {
                    buf[b].put(t);
                    buf[b].put(t + 1);
                    buf[b].put(t + vertexesOnEdgePlusOne);
                    buf[b].put(t + 1);
                }
                buf[b].put(t + 2 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
                buf[b].put(t + 3 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
            }
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdge * 2);
            buf[b].put(vertexesOnEdge * 2);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }

            // *************************************************************************************************************************
            b = 15; // bottom - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i + vertexesOnEdgePlusOne;
                buf[b].put(t);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t + 1);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t + 2);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
                buf[b].put(t + 3);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
            }
            buf[b].put(vertexesOnEdge * 2 + 1);
            buf[b].put(vertexesOnEdge * 2 + 1);
            buf[b].put(2 * vertexesOnEdge + 1);
            buf[b].put(2 * vertexesOnEdge + 1);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge);
                }
            }

            // *************************************************************************************************************************
            b = 16; // bottom left - LEVEL DIF = 2
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i * vertexesOnEdgePlusOne + 1;
                buf[b].put(t);
                buf[b].put(t - 1);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t - 1);
                buf[b].put(t - 2 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
                buf[b].put(t - 3 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i + vertexesOnEdgePlusOne;
                if (i != 0) {
                    buf[b].put(t);
                    buf[b].put(t - vertexesOnEdgePlusOne);
                    buf[b].put(t + 1);
                    buf[b].put(t - vertexesOnEdgePlusOne);
                }
                buf[b].put(t + 2);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
                buf[b].put(t + 3);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
            }
            buf[b].put(vertexesOnEdge * 2 + 1);
            buf[b].put(vertexesOnEdge * 2 + 1);
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x >= 1; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index + 1);
                    buf[b].put(index + vertexesOnEdge);
                }
            }

            //*************************************************************************************************************************
            b = 17; // left[1] top[2] - MIXED LEVEL
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 2);
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i + (vertexesOnEdge * vertexesOnEdge - 1);
                buf[b].put(t);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t - 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t - 2);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
                buf[b].put(t - 3);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
            }
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                if (i != vertexesOnEdge) buf[b].put(i * vertexesOnEdgePlusOne);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne + 1);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2 + 1);
            }
            buf[b].put(1);
            buf[b].put(vertexesOnEdge);
            buf[b].put(vertexesOnEdge);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge; x > 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                final int index = (y + 1) * vertexesOnEdgePlusOne;
                buf[b].put(index + 1);
                buf[b].put(index + vertexesOnEdge);
            }
            //************************************************************************************************************************
            b = 18; // left[2] top[1] - MIXED LEVEL
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            foo = (vertexesOnEdge - 1) * vertexesOnEdgePlusOne;
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                buf[b].put(i + foo);
                buf[b].put(i + foo + vertexesOnEdgePlusOne);
                buf[b].put(i + foo - 1);
                buf[b].put(i - 2 + foo + vertexesOnEdgePlusOne);
            }
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i * vertexesOnEdgePlusOne + 1;
                if (i != vertexesOnEdge) {
                    buf[b].put(t);
                    buf[b].put(t - 1);
                    buf[b].put(t - vertexesOnEdgePlusOne);
                    buf[b].put(t - 1);
                }
                buf[b].put(t - 2 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
                buf[b].put(t - 3 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
            }
            buf[b].put(1);
            buf[b].put(1);
            buf[b].put(vertexesOnEdge);
            buf[b].put(vertexesOnEdge);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge; x > 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                final int index = (y + 1) * vertexesOnEdgePlusOne;
                buf[b].put(index + 1);
                buf[b].put(index + vertexesOnEdge);
            }

         // *************************************************************************************************************************
            b = 19; // top[2] right[1] - MIXED LEVEL
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                buf[b].put(t);
                buf[b].put(t + 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne + 1);
            }
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i + (vertexesOnEdge * vertexesOnEdge - 1);
                if (i != vertexesOnEdge) {
                    buf[b].put(t);
                    buf[b].put(t + vertexesOnEdgePlusOne);
                    buf[b].put(t - 1);
                    buf[b].put(t + vertexesOnEdgePlusOne);
                }
                buf[b].put(t - 2);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
                buf[b].put(t - 3);
                buf[b].put(t + vertexesOnEdgePlusOne - 4);
            }
            buf[b].put(vertexesOnEdge * vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge * vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge - 1);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 2) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }
            
         // *************************************************************************************************************************
            b = 20; // top[1] right[2] - MIXED LEVEL
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                buf[b].put(t);
                buf[b].put(t + 1);
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 1);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
                buf[b].put(t + 3 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
            }
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                if (i != vertexesOnEdge) {
                    buf[b].put(i + foo);
                    buf[b].put(i + foo + vertexesOnEdgePlusOne);
                }
                buf[b].put(i + foo - 1);
                buf[b].put(i - 2 + foo + vertexesOnEdgePlusOne);
            }
            buf[b].put(foo);
            buf[b].put(foo);
            buf[b].put(vertexesOnEdge - 1);
            buf[b].put(vertexesOnEdge - 1);
            for (int y = 0; y < vertexesOnEdge - 1; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 2) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }
            
         // *************************************************************************************************************************
            b = 21; // bottom[2] left[1] - MIXED LEVEL
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 8);
            buf[b].put(vertexesOnEdge * vertexesOnEdgePlusOne + 1);
            for (int i = vertexesOnEdge; i > 1; i = i - 2) {
                buf[b].put(i * vertexesOnEdgePlusOne);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne + 1);
                buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2);
                if (i > 2) buf[b].put(i * vertexesOnEdgePlusOne - vertexesOnEdgePlusOne * 2 + 1);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i + vertexesOnEdgePlusOne;
                if (i != 0) {
                    buf[b].put(t);
                    buf[b].put(t - vertexesOnEdgePlusOne);
                    buf[b].put(t + 1);
                    buf[b].put(t - vertexesOnEdgePlusOne);
                }
                buf[b].put(t + 2);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
                buf[b].put(t + 3);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
            }
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x >= 1; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index + 1);
                    buf[b].put(index + vertexesOnEdge);
                }
            }
            
         // *************************************************************************************************************************
            b = 22; // bottom[1] left[2] - MIXED LEVEL
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            for (int i = vertexesOnEdge; i > 0; i = i - 4) {
                int t = i * vertexesOnEdgePlusOne + 1;
                buf[b].put(t);
                buf[b].put(t - 1);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t - 1);
                buf[b].put(t - 2 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
                buf[b].put(t - 3 * vertexesOnEdgePlusOne);
                buf[b].put(t - 4 * vertexesOnEdgePlusOne - 1);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                if (i != 0) {
                    buf[b].put(i + vertexesOnEdgePlusOne);
                    buf[b].put(i);
                }
                buf[b].put(i + vertexesOnEdgePlusOne + 1);
                buf[b].put(i + 2);
            }
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            buf[b].put(vertexesOnEdgePlusOne * 2 - 1);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge; x >= 1; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index + 1);
                    buf[b].put(index + vertexesOnEdge);
                }
            }
            
         // *************************************************************************************************************************
            b = 23; // right[1] bottom[2] - MIXED LEVEL
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 4);
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i + vertexesOnEdgePlusOne;
                buf[b].put(t);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t + 1);
                buf[b].put(t - vertexesOnEdgePlusOne);
                buf[b].put(t + 2);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
                buf[b].put(t + 3);
                buf[b].put(t - vertexesOnEdgePlusOne + 4);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                if (i != 0) {
                    buf[b].put(t);
                    buf[b].put(t + 1);
                }
                buf[b].put(t + vertexesOnEdgePlusOne);
                buf[b].put(t + 2 * vertexesOnEdgePlusOne + 1);
            }
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdge * 2);
            buf[b].put(vertexesOnEdge * 2);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }
            
         // *************************************************************************************************************************
            b = 24; // right bottom
            buf[b] = BufferUtils.createIntBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2 - 6);
            for (int i = 0; i < vertexesOnEdge; i = i + 2) {
                buf[b].put(i + vertexesOnEdgePlusOne);
                buf[b].put(i);
                buf[b].put(i + vertexesOnEdgePlusOne + 1);
                buf[b].put(i + 2);
            }
            for (int i = 0; i < vertexesOnEdge; i = i + 4) {
                int t = i * vertexesOnEdgePlusOne + vertexesOnEdge - 1;
                if (i != 0) {
                    buf[b].put(t);
                    buf[b].put(t + 1);
                    buf[b].put(t + vertexesOnEdgePlusOne);
                    buf[b].put(t + 1);
                }
                buf[b].put(t + 2 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
                buf[b].put(t + 3 * vertexesOnEdgePlusOne);
                buf[b].put(t + 4 * vertexesOnEdgePlusOne + 1);
            }
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne - 2);
            buf[b].put(vertexesOnEdge * 2);
            buf[b].put(vertexesOnEdge * 2);
            for (int y = 1; y < vertexesOnEdge; y++) {
                for (int x = vertexesOnEdge - 1; x >= 0; x--) {
                    final int index = y * vertexesOnEdgePlusOne + x;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdgePlusOne);
                }
                if (y != vertexesOnEdge - 1) {
                    final int index = (y + 1) * vertexesOnEdgePlusOne;
                    buf[b].put(index);
                    buf[b].put(index + vertexesOnEdge - 1);
                }
            }
            
*/
            // ***************** FINAL CREATION *******************************
            for (int i = 0; i < bufData.length; i++) {
                if (buf[i] != null) {
                    bufData[i] = new IntBufferData(buf[i]);
                }
            }
        }
        return bufData;
    }

    @SuppressWarnings("unused")
    private static void print(IntBuffer ib) {
        ib.rewind();
        System.out.println("********************************");
        for (int i = 0; i < ib.limit(); i++) {
            System.out.println(ib.get());
        }
        System.out.println("********************************");
    }

}
