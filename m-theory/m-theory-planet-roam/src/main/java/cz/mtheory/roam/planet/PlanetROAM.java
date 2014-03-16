package cz.mtheory.roam.planet;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;

import cz.mtheory.roam.planet.buffer.IndexBuffer;
import cz.mtheory.roam.planet.buffer.VertexBuffer;
import cz.mtheory.roam.planet.config.IPlanetConfig;

/**
 * @author Michal NkD Nikodim
 */
public class PlanetROAM extends Mesh {

    private final IPlanetConfig planetConfig;

    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;
    
    private Atmosphere atmosphere;

    private final Camera sceneCamera;
    private final Camera planetCamera;
    private final CameraPoints cameraPoints = new CameraPoints();
    private final PlanetTriangle[] planetTriangle;
    private final Flags flags = new Flags();

    protected final PlanetTriangle[] tmpPTArray = new PlanetTriangle[8];
    protected final double[] splitThresholds;

    public PlanetROAM(IPlanetConfig planetConfig, Camera camera) {
        this.planetConfig = planetConfig;
        this.sceneCamera = camera;
        this.planetCamera = new Camera(camera);
        this.vertexBuffer = new VertexBuffer(planetConfig.getVertexCount());
        this.indexBuffer = new IndexBuffer(planetConfig.getVertexCount());

        PlanetTools.initRenderStates(this);
        this.planetTriangle = PlanetTools.initGeometry(this);
        this.splitThresholds = PlanetTools.initSplitThresholds(planetConfig.getRadius());

        this._meshData.setVertexBuffer(this.vertexBuffer.getFloatBufferVertex());
        this._meshData.setNormalBuffer(this.vertexBuffer.getFloatBufferNormal());
        this._meshData.setColorBuffer(this.vertexBuffer.getFloatBufferColor());
        this._meshData.setIndexBuffer(this.indexBuffer.getIndexBuffer());
        this._meshData.setIndexMode(IndexMode.Triangles);

        this.flags.planetChanged = true;
        this.updateBuffers();
        this.setModelBound(new BoundingBox());
    }


    public void updateGeometry() {
        updatePlanetCamera();
        if (atmosphere != null) atmosphere.update();
        for (int i = 0; i < planetTriangle.length; i++) {
            planetTriangle[i].update(flags);
        }
    }

    public void updateBuffers() {
        if (flags.planetChanged) {
            for (int i = 0; i < planetTriangle.length; i++) {
                planetTriangle[i].fillIndexBuffer(flags);
            }
            vertexBuffer.update();
            indexBuffer.update();
            flags.planetChanged = false;
            updateModelBound();
        } else {
            for (int i = 0; i < planetTriangle.length; i++) {
                planetTriangle[i].clearStates(flags);
            }
        }
    }

    private void updatePlanetCamera() {
        cameraPoints.set(planetCamera);

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

        boolean cameraRotate = !cameraPoints.equalsRotation(planetCamera);
        boolean cameraMove = !cameraPoints.equalsLocation(planetCamera);
        if (cameraRotate) {
            flags.frustrumChangeKey++;
        }
        if (cameraMove) {
            if (!cameraRotate) flags.frustrumChangeKey++;
            flags.normalChangeKey++;
            flags.distanceChangeKey++;
        }
    }
    
    @Override
    public void render(Renderer renderer, MeshData meshData) {
        if (atmosphere != null) atmosphere.render(renderer);
        super.render(renderer, meshData);
    }

    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public IndexBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public Camera getPlanetCamera() {
        return planetCamera;
    }

    public void takeStatistic(PlanetStatistic ps) {
        for (int i = 0; i < planetTriangle.length; i++) {
            planetTriangle[i].takeStatistic(ps, flags);
        }
    }

    private static class CameraPoints {
        Vector3 vLoc = new Vector3();
        Vector3 vDir = new Vector3();
        Vector3 vUp = new Vector3();
        Vector3 vLeft = new Vector3();

        private void set(Camera camera) {
            vLoc.set(camera.getLocation());
            vDir.set(camera.getDirection());
            vUp.set(camera.getUp());
            vLeft.set(camera.getLeft());
        }

        private boolean equalsRotation(Camera camera) {
            return vDir.equals(camera.getDirection()) && vLeft.equals(camera.getLeft()) && vUp.equals(camera.getUp());
        }

        private boolean equalsLocation(Camera camera) {
            return vLoc.equals(camera.getLocation());
        }
    }

    protected final static class Flags {
        protected boolean planetChanged = false;

        protected long normalChangeKey = 0;
        protected long frustrumChangeKey = 0;
        protected long distanceChangeKey = 0;

        /* public int frustrumTestsCount;
         public int normalTestsCount;
         public int cameraDistanceTestsCount;
         public int splited;
         public int merged;*/
    }

    public IPlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    private static final double min = 0.01;

    public double makeCameraCorrection(Camera camera) {
        Vector3 vTerrain = Vector3.fetchTempInstance();
        Vector3 vSurfaceOffset = Vector3.fetchTempInstance();
        Vector3 vCameraLoc = Vector3.fetchTempInstance();
        vCameraLoc.set(camera.getLocation());
        getWorldTransform().applyInverse(vCameraLoc);
        getPlanetConfig().generateTerrainPoint(vCameraLoc, vTerrain);
        vSurfaceOffset.set(vTerrain).normalizeLocal().multiplyLocal(min);
        vTerrain.addLocal(vSurfaceOffset);
        double d1 = vCameraLoc.distance(0, 0, 0);
        double d2 = vTerrain.distance(0, 0, 0);
        double distanceToSurface = 0;
        if (d1 < d2) {
            getWorldTransform().applyForward(vTerrain);
            camera.setLocation(vTerrain);
        } else {
            distanceToSurface = d1 - d2 + min;
        }
        Vector3.releaseTempInstance(vCameraLoc);
        Vector3.releaseTempInstance(vSurfaceOffset);
        Vector3.releaseTempInstance(vTerrain);
        return distanceToSurface;
    }

    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(Atmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

}
