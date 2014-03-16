package cz.mtheory.roam.planet;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Triangle;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.ShadingState;
import com.ardor3d.renderer.state.ShadingState.ShadingMode;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.DataMode;
import com.ardor3d.scenegraph.hint.NormalsMode;

/**
 * @author Michal NkD Nikodim
 */
public class PlanetTools {

    private PlanetTools() {
        //utility class
    }

    public static void initRenderStates(Mesh mesh) {
        RenderState.setQuickCompares(true);

        ShadingState ss = new ShadingState();
        ss.setEnabled(true);
        ss.setShadingMode(ShadingMode.Smooth);
        mesh.setRenderState(ss);

        MaterialState ms = new MaterialState();
        ms.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        ms.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
        ms.setColorMaterialFace(MaterialFace.Front);
        mesh.setRenderState(ms);

        LightState ls = new LightState();
        ls.setEnabled(true);
        ls.setSeparateSpecular(false);
        mesh.setRenderState(ls);

        CullState cullState = new CullState();
        cullState.setCullFace(Face.Back);
        mesh.setRenderState(cullState);

        mesh.getSceneHints().setNormalsMode(NormalsMode.UseProvided);
        mesh.getSceneHints().setDataMode(DataMode.Arrays);
    }

    public static PlanetTriangle[] initGeometry(PlanetROAM planet) {
        int[] boxData = new int[] { -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1, -1, -1, -1, 1, -1, -1, 1, 1, -1, -1, 1, -1 };
        Vector3 v = Vector3.fetchTempInstance();
        int r = planet.getPlanetConfig().getRadius();
        for (int i = 0; i < boxData.length; i = i + 3) {
            v.set(boxData[i], boxData[i + 1], boxData[i + 2]).normalizeLocal().multiplyLocal(r);
            double terrainDelta = planet.getPlanetConfig().generateTerrainPoint(v, v);
            int index = planet.getVertexBuffer().setVertex(v);
            v.normalizeLocal();
            planet.getVertexBuffer().setNormal(index, v);
            planet.getVertexBuffer().setColor(index, planet.getPlanetConfig().getColor(terrainDelta));
        }

        Vector3.releaseTempInstance(v);

        PlanetTriangle[] pt = new PlanetTriangle[12];
        pt[0] = PlanetTriangle.fetchInstance(null, planet, 0, 2, 3);
        pt[1] = PlanetTriangle.fetchInstance(null, planet, 2, 0, 1);
        pt[2] = PlanetTriangle.fetchInstance(null, planet, 1, 6, 2);
        pt[3] = PlanetTriangle.fetchInstance(null, planet, 6, 1, 5);
        pt[4] = PlanetTriangle.fetchInstance(null, planet, 5, 7, 6);
        pt[5] = PlanetTriangle.fetchInstance(null, planet, 7, 5, 4);
        pt[6] = PlanetTriangle.fetchInstance(null, planet, 4, 3, 7);
        pt[7] = PlanetTriangle.fetchInstance(null, planet, 3, 4, 0);
        pt[8] = PlanetTriangle.fetchInstance(null, planet, 5, 0, 4);
        pt[9] = PlanetTriangle.fetchInstance(null, planet, 0, 5, 1);
        pt[10] = PlanetTriangle.fetchInstance(null, planet, 3, 6, 7);
        pt[11] = PlanetTriangle.fetchInstance(null, planet, 6, 3, 2);
        pt[0].setNeighbors(pt[7], pt[11], pt[1]);
        pt[1].setNeighbors(pt[2], pt[9], pt[0]);
        pt[2].setNeighbors(pt[1], pt[11], pt[3]);
        pt[3].setNeighbors(pt[4], pt[9], pt[2]);
        pt[4].setNeighbors(pt[3], pt[10], pt[5]);
        pt[5].setNeighbors(pt[6], pt[8], pt[4]);
        pt[6].setNeighbors(pt[5], pt[10], pt[7]);
        pt[7].setNeighbors(pt[0], pt[8], pt[6]);
        pt[8].setNeighbors(pt[5], pt[7], pt[9]);
        pt[9].setNeighbors(pt[1], pt[3], pt[8]);
        pt[10].setNeighbors(pt[6], pt[4], pt[11]);
        pt[11].setNeighbors(pt[2], pt[0], pt[10]);
        return pt;
    }

    public static void computeBoundingBox(Triangle triangle, BoundingBox store) {
        double minX = Math.min(triangle.getA().getX(), Math.min(triangle.getB().getX(), triangle.getC().getX()));
        double minY = Math.min(triangle.getA().getY(), Math.min(triangle.getB().getY(), triangle.getC().getY()));
        double minZ = Math.min(triangle.getA().getZ(), Math.min(triangle.getB().getZ(), triangle.getC().getZ()));
        double maxX = Math.max(triangle.getA().getX(), Math.max(triangle.getB().getX(), triangle.getC().getX()));
        double maxY = Math.max(triangle.getA().getY(), Math.max(triangle.getB().getY(), triangle.getC().getY()));
        double maxZ = Math.max(triangle.getA().getZ(), Math.max(triangle.getB().getZ(), triangle.getC().getZ()));
        double cX = (minX + maxX) * 0.5;
        double cY = (minY + maxY) * 0.5;
        double cZ = (minZ + maxZ) * 0.5;
        store.setCenter(cX, cY, cZ);
        store.setXExtent(maxX - cX);
        store.setYExtent(maxY - cY);
        store.setZExtent(maxZ - cZ);
    }

    public static void getAverageNormal(Triangle t1, Triangle t2, Triangle t3, Triangle t4, Vector3 store) {
        double x = (t1.getNormal().getX() + t2.getNormal().getX() + t3.getNormal().getX() + t4.getNormal().getX()) * 0.25;
        double y = (t1.getNormal().getY() + t2.getNormal().getY() + t3.getNormal().getY() + t4.getNormal().getY()) * 0.25;
        double z = (t1.getNormal().getZ() + t2.getNormal().getZ() + t3.getNormal().getZ() + t4.getNormal().getZ()) * 0.25;
        store.set(x, y, z).normalizeLocal();
    }

    public static void getAverageNormal_correction(Vector3 store, PlanetTriangle[] t) {
        double x = 0.0, y = 0.0, z = 0.0;
        int count = 0;
        for (int i = 0; i < t.length; i++) {
            if (t[i] != null) {
                x += t[i].getTriangle().getNormal().getX();
                y += t[i].getTriangle().getNormal().getY();
                z += t[i].getTriangle().getNormal().getZ();
                count++;
            }
        }
        x /= count;
        y /= count;
        z /= count;
        store.set(x, y, z).normalizeLocal();
    }

    private static final double uhel = Math.PI * (1 / 180.0);
    private static final double uhel2 = Math.PI * (45.0 / 180.0);

    public static double[] initSplitThresholds(int radius) {
        Vector3 v1 = new Vector3(1, 1, 1).normalizeLocal().multiplyLocal(radius);
        Vector3 v2 = new Vector3(-1, 1, 1).normalizeLocal().multiplyLocal(radius);
        double dis = v1.distance(v2);
        double obsah = (dis * dis) / 2;
        int deleni = (int) Math.ceil(Math.log(obsah) / Math.log(2));
        double[] splitArray = new double[deleni];
        double strana = 1.0;
        for (int i = deleni - 1; i >= 0; i--) {
            splitArray[i] = strana / Math.tan(uhel);
            strana = ((strana * 2.0) * Math.cos(uhel2));
        }
        return splitArray;
    }

    public static ReadOnlyColorRGBA color(int r, int g, int b) {
        return new ColorRGBA(r / 255f, g / 255f, b / 255f, 1f);
    }

}
