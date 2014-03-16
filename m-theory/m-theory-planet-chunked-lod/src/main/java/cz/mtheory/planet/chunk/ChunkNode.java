/**
 * 
 */
package cz.mtheory.planet.chunk;

import java.util.ArrayList;
import java.util.List;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.renderer.Camera.FrustumIntersect;

import cz.mtheory.planet.DEBUG;
import cz.mtheory.planet.Planet;
import cz.mtheory.planet.enums.CubeFace;
import cz.mtheory.planet.enums.Quadrant;
import cz.mtheory.planet.threads.TextureGenerator;
import cz.mtheory.planet.util.PlanetAlgs;
import cz.mtheory.planet.util.Probe;

/**
 * @author Michal NkD Nikodim
 */
public class ChunkNode {

    private static final List<ChunkNode> pool = new ArrayList<ChunkNode>(1000);

    public final static ChunkNode fetchInstance() {
        return pool.isEmpty() ? new ChunkNode() : pool.remove(pool.size() - 1);
    }

    public final static void releaseInstance(ChunkNode chunkNode) {
        chunkNode.clearBeforeReturnToPool();
        pool.add(chunkNode);
    }

    private Planet planet;

    private int level;
    private CubeFace cubeFace;
    private Quadrant quadrant;
    private ChunkNode parent;
    private boolean hasChildren = false;
    private ChunkNode[] children = new ChunkNode[4];
    private ChunkNode[] neighbor = new ChunkNode[4];
    
    private BoundingSphere boundingSphere = new BoundingSphere();
    private Chunk chunk;
    private FrustumIntersect frustumIntersect;

    private ChunkNode() {
        //
    }

    public final ChunkNode init(Planet planet, ChunkNode parent, Quadrant quadrant, CubeFace cubeFace) {
        this.planet = planet;
        this.parent = parent;
        this.level = parent == null ? 0 : parent.level + 1;
        this.quadrant = quadrant;
        this.cubeFace = cubeFace;
        return this;
    }

    public final void setNeighbors(ChunkNode left, ChunkNode top, ChunkNode right, ChunkNode bottom) {
        this.neighbor[0] = left;
        this.neighbor[1] = top;
        this.neighbor[2] = right;
        this.neighbor[3] = bottom;
    }

    public final Chunk getChunk() {
        return chunk;
    }

    public final void split(Probe probe) {
        if (level == planet.getConfig().getConstants().splitMaxLevel) return;
        probe.splitCount++;
        for (int i = 0; i < 4; i++) {
            Quadrant childQuadrant = planet.getQuadrant()[i];
            children[i] = ChunkNode.fetchInstance().init(planet, this, childQuadrant, cubeFace);
            children[i].chunk = Chunk.getPool().fetch(planet.getConfig());
            children[i].chunk.setChunkNode(children[i]);
            children[i].chunk.initSmaller(chunk, childQuadrant);
        }
        updateNeighbors(true);
        Chunk.getPool().release(chunk);
        chunk = null;
        hasChildren = true;
    }

    public final void merge(Probe probe) {
        probe.mergeCount++;
        chunk = Chunk.getPool().fetch(planet.getConfig());
        chunk.setChunkNode(this);
        chunk.initBigger(children[0].chunk, children[1].chunk, children[2].chunk, children[3].chunk);
        updateNeighbors(false);
        
        for (int i = 0; i < 4; i++) {
            ChunkNode.releaseInstance(children[i]);
            children[i] = null;
        }
        hasChildren = false;
    }

    private void updateNeighbors(boolean splitPhase) {
        if (splitPhase) {
            children[0].neighbor[2] = children[1];
            children[0].neighbor[1] = children[2];
            children[1].neighbor[0] = children[0];
            children[1].neighbor[1] = children[3];
            children[2].neighbor[3] = children[0];
            children[2].neighbor[2] = children[3];
            children[3].neighbor[3] = children[1];
            children[3].neighbor[0] = children[2];
        }
        if (neighbor[0].hasChildren) {
            int i1 = cubeFace.findNeighborIndex(neighbor[0].cubeFace, children[0].quadrant);
            if (i1 == -1) i1 = 1;
            int i2 = cubeFace.findNeighborIndex(neighbor[0].cubeFace, children[2].quadrant);
            if (i2 == -1) i2 = 3;
            int n = neighbor[0].cubeFace.neighborSide(cubeFace);
            if (n == -1) n = 2;
            if (splitPhase) {
                neighbor[0].children[i1].neighbor[n] = children[0];
                neighbor[0].children[i2].neighbor[n] = children[2];
                children[0].neighbor[0] = neighbor[0].children[i1];
                children[2].neighbor[0] = neighbor[0].children[i2];
                //if (neighbor[0].children[i1].hasChildren) neighbor[0].children[i1].updateNeighbors(true);
                //if (neighbor[0].children[i2].hasChildren) neighbor[0].children[i2].updateNeighbors(true);
            } else {
                neighbor[0].children[i1].neighbor[n] = this;
                neighbor[0].children[i2].neighbor[n] = this;
                /*if (neighbor[0].children[i1].hasChildren){
                    neighbor[0].children[i1].children[i1].neighbor[n] = this;
                    neighbor[0].children[i1].children[i2].neighbor[n] = this;
                }
                if (neighbor[0].children[i2].hasChildren){
                    neighbor[0].children[i2].children[i1].neighbor[n] = this;
                    neighbor[0].children[i2].children[i2].neighbor[n] = this;
                }*/
            }
        } else if (splitPhase) {
            children[0].neighbor[0] = neighbor[0];
            children[2].neighbor[0] = neighbor[0];
        }
        if (neighbor[1].hasChildren) {
            int i1 = cubeFace.findNeighborIndex(neighbor[1].cubeFace, children[2].quadrant);
            if (i1 == -1) i1 = 0;
            int i2 = cubeFace.findNeighborIndex(neighbor[1].cubeFace, children[3].quadrant);
            if (i2 == -1) i2 = 1;
            int n = neighbor[1].cubeFace.neighborSide(cubeFace);
            if (n == -1) n = 3;
            if (splitPhase) {
                neighbor[1].children[i1].neighbor[n] = children[2];
                neighbor[1].children[i2].neighbor[n] = children[3];
                children[2].neighbor[1] = neighbor[1].children[i1];
                children[3].neighbor[1] = neighbor[1].children[i2];
                //if (neighbor[1].children[i1].hasChildren) neighbor[1].children[i1].updateNeighbors(true);
                //if (neighbor[1].children[i2].hasChildren) neighbor[1].children[i2].updateNeighbors(true);
            } else {
                neighbor[1].children[i1].neighbor[n] = this;
                neighbor[1].children[i2].neighbor[n] = this;
                /*if (neighbor[1].children[i1].hasChildren){
                    neighbor[1].children[i1].children[i1].neighbor[n] = this;
                    neighbor[1].children[i1].children[i2].neighbor[n] = this;
                }
                if (neighbor[1].children[i2].hasChildren){
                    neighbor[1].children[i2].children[i1].neighbor[n] = this;
                    neighbor[1].children[i2].children[i2].neighbor[n] = this;
                }*/
            }
        } else if (splitPhase) {
            children[2].neighbor[1] = neighbor[1];
            children[3].neighbor[1] = neighbor[1];
        }
        if (neighbor[2].hasChildren) {
            int i1 = cubeFace.findNeighborIndex(neighbor[2].cubeFace, children[1].quadrant);
            if (i1 == -1) i1 = 0;
            int i2 = cubeFace.findNeighborIndex(neighbor[2].cubeFace, children[3].quadrant);
            if (i2 == -1) i2 = 2;
            int n = neighbor[2].cubeFace.neighborSide(cubeFace);
            if (n == -1) n = 0;
            if (splitPhase) {
                neighbor[2].children[i1].neighbor[n] = children[1];
                neighbor[2].children[i2].neighbor[n] = children[3];
                children[1].neighbor[2] = neighbor[2].children[i1];
                children[3].neighbor[2] = neighbor[2].children[i2];
                //if (neighbor[2].children[i1].hasChildren) neighbor[2].children[i1].updateNeighbors(true);
                //if (neighbor[2].children[i2].hasChildren) neighbor[2].children[i2].updateNeighbors(true);
            } else {
                neighbor[2].children[i1].neighbor[n] = this;
                neighbor[2].children[i2].neighbor[n] = this;
                /*if (neighbor[2].children[i1].hasChildren){
                    neighbor[2].children[i1].children[i1].neighbor[n] = this;
                    neighbor[2].children[i1].children[i2].neighbor[n] = this;
                }
                if (neighbor[2].children[i2].hasChildren){
                    neighbor[2].children[i2].children[i1].neighbor[n] = this;
                    neighbor[2].children[i2].children[i2].neighbor[n] = this;
                }*/
            }
        } else if (splitPhase) {
            children[1].neighbor[2] = neighbor[2];
            children[3].neighbor[2] = neighbor[2];
        }
        if (neighbor[3].hasChildren) {
            int i1 = cubeFace.findNeighborIndex(neighbor[3].cubeFace, children[0].quadrant);
            if (i1 == -1) i1 = 2;
            int i2 = cubeFace.findNeighborIndex(neighbor[3].cubeFace, children[1].quadrant);
            if (i2 == -1) i2 = 3;
            int n = neighbor[3].cubeFace.neighborSide(cubeFace);
            if (n == -1) n = 1;
            if (splitPhase) {
                neighbor[3].children[i1].neighbor[n] = children[0];
                neighbor[3].children[i2].neighbor[n] = children[1];
                children[0].neighbor[3] = neighbor[3].children[i1];
                children[1].neighbor[3] = neighbor[3].children[i2];
                //if (neighbor[3].children[i1].hasChildren) neighbor[3].children[i1].updateNeighbors(true);
                //if (neighbor[3].children[i2].hasChildren) neighbor[3].children[i2].updateNeighbors(true);
            } else {
                neighbor[3].children[i1].neighbor[n] = this;
                neighbor[3].children[i2].neighbor[n] = this;
                /*if (neighbor[3].children[i1].hasChildren){
                    neighbor[3].children[i1].children[i1].neighbor[n] = this;
                    neighbor[3].children[i1].children[i2].neighbor[n] = this;
                }
                if (neighbor[3].children[i2].hasChildren){
                    neighbor[3].children[i2].children[i1].neighbor[n] = this;
                    neighbor[3].children[i2].children[i2].neighbor[n] = this;
                }*/
            }
        } else if (splitPhase) {
            children[0].neighbor[3] = neighbor[3];
            children[1].neighbor[3] = neighbor[3];
        }
    }

    protected final CubeFace getCubeFace() {
        return cubeFace;
    }

    protected final int getIndexOfIndexBuffer() {
        if (quadrant != null && DEBUG.solveNeighbors) {
            int levelMinusOne = level - 1;
            //int levelMinusTwo = level - 2;
            switch (quadrant.getIndex()) {
                case 0:
                    if (neighbor[3].level == levelMinusOne && neighbor[0].level == levelMinusOne) return 8;
                    //if (neighbor[3].level == levelMinusTwo && neighbor[0].level == levelMinusTwo) return 16;
                    //if (neighbor[3].level == levelMinusOne && neighbor[0].level == levelMinusTwo) return 22;
                    //if (neighbor[3].level == levelMinusTwo && neighbor[0].level == levelMinusOne) return 21;
                    if (neighbor[3].level == levelMinusOne) return 7;
                    if (neighbor[0].level == levelMinusOne) return 1;
                    //if (neighbor[3].level == levelMinusTwo) return 15;
                    //if (neighbor[0].level == levelMinusTwo) return 9;
                    break;
                case 1:
                    if (neighbor[2].level == levelMinusOne && neighbor[3].level == levelMinusOne) return 6;
                    //if (neighbor[2].level == levelMinusTwo && neighbor[3].level == levelMinusTwo) return 14;
                    //if (neighbor[2].level == levelMinusOne && neighbor[3].level == levelMinusTwo) return 23;
                    //if (neighbor[2].level == levelMinusTwo && neighbor[3].level == levelMinusOne) return 24;
                    if (neighbor[2].level == levelMinusOne) return 5;
                    if (neighbor[3].level == levelMinusOne) return 7;
                    //if (neighbor[2].level == levelMinusTwo) return 13;
                    //if (neighbor[3].level == levelMinusTwo) return 15;
                    break;
                case 2:
                    if (neighbor[0].level == levelMinusOne && neighbor[1].level == levelMinusOne) return 2;
                    //if (neighbor[0].level == levelMinusTwo && neighbor[1].level == levelMinusTwo) return 10;
                    //if (neighbor[0].level == levelMinusOne && neighbor[1].level == levelMinusTwo) return 17;
                    //if (neighbor[0].level == levelMinusTwo && neighbor[1].level == levelMinusOne) return 18;
                    if (neighbor[0].level == levelMinusOne) return 1;
                    if (neighbor[1].level == levelMinusOne) return 3;
                    //if (neighbor[0].level == levelMinusTwo) return 9;
                    //if (neighbor[1].level == levelMinusTwo) return 11;
                    break;
                case 3:
                    if (neighbor[1].level == levelMinusOne && neighbor[2].level == levelMinusOne) return 4;
                    //if (neighbor[1].level == levelMinusTwo && neighbor[2].level == levelMinusTwo) return 12;
                    //if (neighbor[1].level == levelMinusOne && neighbor[2].level == levelMinusTwo) return 20;
                    //if (neighbor[1].level == levelMinusTwo && neighbor[2].level == levelMinusOne) return 19;
                    if (neighbor[1].level == levelMinusOne) return 3;
                    if (neighbor[2].level == levelMinusOne) return 5;
                    //if (neighbor[1].level == levelMinusTwo) return 11;
                    //if (neighbor[2].level == levelMinusTwo) return 13;
                    break;
            }
        }
        return 0;
    }

    public final void afterUpdate(){
        if (hasChildren){
            for (int i = 0; i < 4; i++) {
                children[i].afterUpdate();
            } 
        }
        if (chunk != null) {
            if (planet.getRenderList().contains(chunk)) {
                chunk.setVisible(true);
                int priority = (int) chunk.getBoundingSphere().getCenter().distanceSquared(planet.getPlanetCamera().getLocation()); 
                chunk.setPriority(priority);
                if (!chunk.haveTexture() && !chunk.isInQueue()){
                    chunk.setInQueue(true);
                    TextureGenerator.add(chunk);
                }
            } else {
                chunk.setVisible(false);
                chunk.setPriority(Integer.MAX_VALUE);
                TextureGenerator.remove(chunk);
                chunk.setInQueue(false);
            }
            
        }
    }
    
    public final void update(Probe probe) {
        probe.chunkNodeCount++;
        if (chunk != null) {
            probe.chunkCount++;
        }
        update(probe, frustumTest() != FrustumIntersect.Outside);
    }

    private void update(Probe probe, boolean insideFrustum) {
        if (hasChildren) {
            if (haveToMerge()) {
                merge(probe);
                if (insideFrustum) addChunkToRenderList(chunk);
            } else {
                for (int i = 0; i < 4; i++) {
                    children[i].update(probe);
                }
            }
        } else {
            if (haveToSplit()) {
                split(probe);
                if (insideFrustum) {
                    if (hasChildren) {
                        for (int i = 0; i < 4; i++) {
                            if (children[i].frustumTest() != FrustumIntersect.Outside) {
                                addChunkToRenderList(children[i].chunk);
                                //children[i].update(probe);
                            }
                        }
                    } else {
                        addChunkToRenderList(chunk);
                    }
                }
            } else {
                if (insideFrustum) addChunkToRenderList(chunk);
            }
        }
    }

    private void updateParentBoundingSphere(BoundingSphere bb) {
        if (parent != null) {
            boundingSphere.mergeLocal(bb);
            parent.updateParentBoundingSphere(boundingSphere);
        }
    }

    private void addChunkToRenderList(Chunk renderedChunk) {
        if (!DEBUG.horizontCulling ||
                PlanetAlgs.chunkHorizonCulling(planet.getPlanetCamera(), planet.getConfig().getPlanetRadius(), renderedChunk.getBoundingSphere())) {
            planet.getRenderList().add(renderedChunk);
        }
    }

    private boolean haveToSplit() {
        return distanceSquare() < planet.getConfig().getConstants().splitByLevelValues[level];
    }

    private boolean haveToMerge() {
        return canMerge() && distanceSquare() > planet.getConfig().getConstants().mergeByLevelValues[level];
    }

    private boolean canMerge() {
        return hasChildren && !children[0].hasChildren && !children[1].hasChildren && !children[2].hasChildren &&
                !children[3].hasChildren;
    }

    private double distanceSquare() {
        return boundingSphere.distanceSquaredTo(planet.getPlanetCamera().getLocation()) -
                (boundingSphere.getRadius() * boundingSphere.getRadius());
    }

    private FrustumIntersect frustumTest() {
        frustumIntersect = parent != null ? parent.frustumIntersect : FrustumIntersect.Intersects;
        if (frustumIntersect != FrustumIntersect.Outside) {
            final int state = planet.getPlanetCamera().getPlaneState();
            frustumIntersect = planet.getPlanetCamera().contains(boundingSphere);
            planet.getPlanetCamera().setPlaneState(state);
        }
        return frustumIntersect;
    }

    private void clearBeforeReturnToPool() {
        parent = null;
        for (int i = 0; i < 4; i++) {
            children[i] = null;
            neighbor[i] = null;
        }
        if (chunk != null) Chunk.getPool().release(chunk);
        chunk = null;
        planet = null;
        hasChildren = false;
    }

    public Quadrant getQuadrant() {
        return quadrant;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder(indent());
        sb.append(quadrant == null ? "ROOT_PATCH" : quadrant);
        sb.append("(").append(level).append(")");
        if (chunk != null) sb.append(" - havePatchMesh");
        sb.append("\n");
        if (hasChildren) {
            for (int i = 0; i < 4; i++) {
                sb.append(children[i]);
            }
        }
        return sb.toString();
    }

    private String indent() {
        return "                                                                                                                                ".substring(0, level * 2);
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    protected final void updateBoundingSphere() {
        chunk.getBoundingSphere().clone(boundingSphere);
        updateParentBoundingSphere(boundingSphere);
    }

}
