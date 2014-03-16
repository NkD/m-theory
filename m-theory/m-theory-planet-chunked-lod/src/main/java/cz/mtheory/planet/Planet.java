/**
 * 
 */
package cz.mtheory.planet;

import java.util.ArrayList;
import java.util.List;

import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.TextureState.CorrectionType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.hint.NormalsMode;
import com.ardor3d.scenegraph.hint.TextureCombineMode;

import cz.mtheory.planet.chunk.Chunk;
import cz.mtheory.planet.chunk.ChunkNode;
import cz.mtheory.planet.config.IConfig;
import cz.mtheory.planet.enums.CubeFace;
import cz.mtheory.planet.enums.Quadrant;
import cz.mtheory.planet.util.Probe;

/**
 * @author Michal NkD Nikodim
 */
public class Planet extends Mesh {

    private final ChunkNode[] chunkNode = new ChunkNode[6];
    private final Camera sceneCamera;
    private final Camera planetCamera = new Camera();
    private final IConfig config;
    private final List<Chunk> renderList = new ArrayList<Chunk>(1000);
    private final Quadrant[] quadrant = new Quadrant[4];
    private final IndexMode[] indexModes = new IndexMode[] { IndexMode.TriangleStrip };
    private TextureState textureState;

    public Planet(String name, Camera sceneCamera, IConfig config) {
        super(name);
        this.sceneCamera = sceneCamera;
        this.planetCamera.set(sceneCamera);
        this.config = config;

        for (int i = 0; i < 4; i++) {
            quadrant[i] = new Quadrant(i, config.getConstants());
        }

        for (int i = 0; i < 6; i++) {
            Chunk chunk = Chunk.getPool().fetch(config);
            chunkNode[i] = ChunkNode.fetchInstance().init(this, null, null, CubeFace.find(i));
            chunk.setChunkNode(chunkNode[i]);
            chunkNode[i].setChunk(chunk);
            chunk.initRoot();
        }

        chunkNode[0].setNeighbors(chunkNode[3], chunkNode[4], chunkNode[1], chunkNode[5]);
        chunkNode[1].setNeighbors(chunkNode[0], chunkNode[4], chunkNode[2], chunkNode[5]);
        chunkNode[2].setNeighbors(chunkNode[1], chunkNode[4], chunkNode[3], chunkNode[5]);
        chunkNode[3].setNeighbors(chunkNode[2], chunkNode[4], chunkNode[0], chunkNode[5]);
        chunkNode[4].setNeighbors(chunkNode[3], chunkNode[2], chunkNode[1], chunkNode[0]);
        chunkNode[5].setNeighbors(chunkNode[3], chunkNode[0], chunkNode[1], chunkNode[2]);

        CullState cs = new CullState();
        cs.setCullFace(Face.Back);
        setRenderState(cs);

        textureState = new TextureState();
        textureState.setEnabled(true);
        textureState.setCorrectionType(CorrectionType.Perspective);

        getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
    }

    @Override
    public void onDraw(final Renderer r) {
        draw(r);
    }

    @Override
    public void render(final Renderer renderer, final MeshData meshData) {

        if (DEBUG.showDrawedChunksCount) {
            System.out.println("DEBUG.showDrawedChunksCount = " + renderList.size());
            DEBUG.showDrawedChunksCount = false;
        }

        if (renderList.isEmpty()) return;

        for (final StateType type : StateType.values) {
            if (type != StateType.GLSLShader && type != StateType.FragmentProgram && type != StateType.VertexProgram) {
                if (type != StateType.Light) {
                    renderer.applyState(type, _states.get(type));
                }
            }
        }
        final boolean transformed = renderer.doTransforms(_worldTransform);
        renderer.applyNormalsMode(NormalsMode.Off, null);

        for (int i = 0; i < renderList.size(); i++) {

            Chunk chunk = renderList.get(i);
            if (chunk.haveTexture()) {
                    if (chunk.getLock().tryLock()) {
                        try {
                        if (!chunk.isTextureUpdated()) {
                            int size = config.getConstants().textureSize;
                            renderer.updateTexture2DSubImage(chunk.getTexture(), 0, 0, size, size, chunk.getTextureBuffer(), 0, 0, size);
                            chunk.setTextureUpdated(true);
                            //System.out.println("Update textury pro " + chunk);
                        }
                        textureState.setTexture(chunk.getTexture());
                        } finally {
                            chunk.getLock().unlock(); 
                        }
                    }
               
                renderer.applyState(StateType.Texture, textureState);
            } else {
                renderer.applyState(StateType.Texture, null);
            }

            renderer.applyDefaultColor(chunk.getColor());
            renderer.setupInterleavedDataVBO(chunk.getInterleavedData(), chunk.getVertexBufferData(),chunk.getNormalBufferData(), null, config.getConstants().textureCoords);
            renderer.drawElementsVBO(config.getChunkIndexBufferData(chunk.getIndex()), null, indexModes, 1);
            //renderer.drawElementsVBO(config.getChunkIndexBufferData(24), null, indexModes);
        }
        if (transformed) renderer.undoTransforms(_worldTransform);
    }

    public final ChunkNode getChunkNodeRoot(CubeFace cubeFace) {
        return chunkNode[cubeFace.getIndex()];
    }

    public final void update(Probe probe) {
        if (DEBUG.updatePlanetCamera) updatePlanetCamera();

        renderList.clear();

        for (int i = 0; i < 6; i++) {
            chunkNode[i].update(probe);
        }
        for (int i = 0; i < 6; i++) {
            chunkNode[i].afterUpdate();
        }
    }

    public final Camera getPlanetCamera() {
        return planetCamera;
    }

    private final void updatePlanetCamera() {
        Vector3 vLoc = Vector3.fetchTempInstance();
        Vector3 vDir = Vector3.fetchTempInstance();
        Vector3 vLeft = Vector3.fetchTempInstance();
        Vector3 vUp = Vector3.fetchTempInstance();

        vLoc.set(sceneCamera.getLocation());
        vLoc.add(sceneCamera.getDirection(), vDir);
        vLoc.add(sceneCamera.getLeft(), vLeft);
        vLoc.add(sceneCamera.getUp(), vUp);

        getWorldTransform().applyInverse(vLoc);
        getWorldTransform().applyInverse(vDir).subtractLocal(vLoc).normalizeLocal();
        getWorldTransform().applyInverse(vLeft).subtractLocal(vLoc).normalizeLocal();
        getWorldTransform().applyInverse(vUp).subtractLocal(vLoc).normalizeLocal();

        planetCamera.setFrame(vLoc, vLeft, vUp, vDir);

        Vector3.releaseTempInstance(vUp);
        Vector3.releaseTempInstance(vLeft);
        Vector3.releaseTempInstance(vDir);
        Vector3.releaseTempInstance(vLoc);
    }

    public final double makeCameraCorrection(Camera c) {
        Vector3 vTerrain = Vector3.fetchTempInstance();
        Vector3 vSurfaceOffset = Vector3.fetchTempInstance();
        Vector3 vCameraLoc = Vector3.fetchTempInstance();
        Vector3 vUnitSphere = Vector3.fetchTempInstance();
        vCameraLoc.set(c.getLocation());
        getWorldTransform().applyInverse(vCameraLoc);
        vCameraLoc.normalize(vUnitSphere);
        config.terrainPoint(vUnitSphere, vTerrain);
        vSurfaceOffset.set(vUnitSphere).multiplyLocal(0.15);
        vTerrain.addLocal(vSurfaceOffset);
        double d1 = vCameraLoc.distance(0, 0, 0);
        double d2 = vTerrain.distance(0, 0, 0);
        double distanceToSurface = 0.15;
        if (d1 < d2) {
            getWorldTransform().applyForward(vTerrain);
            c.setLocation(vTerrain);
        } else {
            distanceToSurface = d1 - d2 + 0.15;
        }

        //vzdalenost od idealniho povrchu planety
        //distanceToSurface = vTerrain.set(c.getLocation()).normalizeLocal().multiplyLocal(config.getPlanetRadius()).distance(c.getLocation());

        Vector3.releaseTempInstance(vUnitSphere);
        Vector3.releaseTempInstance(vCameraLoc);
        Vector3.releaseTempInstance(vSurfaceOffset);
        Vector3.releaseTempInstance(vTerrain);

        return distanceToSurface;
    }

    public IConfig getConfig() {
        return config;
    }

    public Quadrant[] getQuadrant() {
        return quadrant;
    }

    public List<Chunk> getRenderList() {
        return renderList;
    }

}
