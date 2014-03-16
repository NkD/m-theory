package cz.mtheory.roam.planet;

import java.util.ArrayList;
import java.util.List;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Triangle;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera.FrustumIntersect;

import cz.mtheory.roam.planet.PlanetROAM.Flags;

/**
 * @author Michal NkD Nikodim
 */
public class PlanetTriangle {

    private static final List<PlanetTriangle> pool = new ArrayList<PlanetTriangle>(10000);

    public static PlanetTriangle fetchInstance(PlanetTriangle parent, PlanetROAM planet, int index0, int index1, int index2) {
        PlanetTriangle inst = pool.isEmpty() ? new PlanetTriangle() : pool.remove(pool.size() - 1);
        inst.parent = parent;
        inst.level = parent == null ? 0 : parent.level + 1;
        inst.planet = planet;
        inst.index0 = index0;
        inst.index1 = index1;
        inst.index2 = index2;
        inst.normalChangeKey = -1;
        inst.frustumChangeKey = -1;
        inst.distanceChangeKey = -1;
        inst.hasChildren = false;
        Vector3 v = Vector3.fetchTempInstance();
        planet.getVertexBuffer().getVertex(index0, v);
        inst.triangle.setA(v);
        planet.getVertexBuffer().getVertex(index1, v);
        inst.triangle.setB(v);
        planet.getVertexBuffer().getVertex(index2, v);
        inst.triangle.setC(v);
        Vector3.lerp(inst.triangle.getA(), inst.triangle.getB(), 0.5, v);
        PlanetTools.computeBoundingBox(inst.triangle, inst.boundingBox);
        inst.terrainDelta = planet.getPlanetConfig().generateTerrainPoint(v, inst.terrainPoint);
        inst.terrainError = inst.terrainPoint.distance(v);
        Vector3.releaseTempInstance(v);
        return inst;
    }

    public static void releaseInstance(PlanetTriangle inst) {
        inst.parent = null;
        inst.leftChild = null;
        inst.rightChild = null;
        inst.leftNeighbor = null;
        inst.rightNeighbor = null;
        inst.baseNeighbor = null;
        pool.add(inst);
    }

    private int level = -1;
    private int index0 = -1;
    private int index1 = -1;
    private int index2 = -1;

    private PlanetROAM planet;

    private boolean hasChildren = false;

    private PlanetTriangle parent = null;

    private PlanetTriangle leftChild = null;
    private PlanetTriangle rightChild = null;

    private PlanetTriangle leftNeighbor = null;
    private PlanetTriangle rightNeighbor = null;
    private PlanetTriangle baseNeighbor = null;

    private final Triangle triangle = new Triangle();

    private final Vector3 terrainPoint = new Vector3();
    private double terrainError;
    private double terrainDelta;

    private final BoundingBox boundingBox = new BoundingBox();

    private long normalChangeKey = 0;
    private long frustumChangeKey = 0;
    private long distanceChangeKey = 0;
    private boolean normalTest;
    private double cameraDistance;
    private FrustumIntersect frustumIntersect = null;

    protected void fillIndexBuffer(Flags flags) {
        if (hasChildren) {
            leftChild.fillIndexBuffer(flags);
            rightChild.fillIndexBuffer(flags);
        } else {
            planet.getIndexBuffer().addIndexes(index0, index1, index2);
        }
        frustumIntersect = null;
    }
    
    public void clearStates(Flags flags) {
        if (hasChildren) {
            leftChild.clearStates(flags);
            rightChild.clearStates(flags);
        }
        frustumIntersect = null;
    }

    protected void update(Flags flags) {
        if (parent == null) frustumIntersect = null;
        if (parent != null && frustumIntersect == null) frustumIntersect = parent.frustumIntersect == FrustumIntersect.Intersects ? null : parent.frustumIntersect;
        if (frustumIntersect == null) frustumIntersect = getFrustrumIntersect(flags);
        if (canBeMerged() && (isOutOfViewTestForMerge(flags) || mergeErrorMetric(flags))) {
            merge(flags);
        } else {
            if (hasChildren) {
                leftChild.update(flags);
                rightChild.update(flags);
            } else {
                if ((frustumIntersect == FrustumIntersect.Inside || frustumIntersect == FrustumIntersect.Intersects) && isVisibleAlongNormal(flags) && splitErrorMetric(flags)) split(flags);
            }
        }
    }

    private void split(Flags flags) {
        if (baseNeighbor.level != level) baseNeighbor.split(flags);
        flags.planetChanged = true;
        //flags.splited++;

        int index = planet.getVertexBuffer().setVertex(terrainPoint);

        leftChild = PlanetTriangle.fetchInstance(this, planet, index2, index0, index);
        rightChild = PlanetTriangle.fetchInstance(this, planet, index1, index2, index);
        baseNeighbor.leftChild = PlanetTriangle.fetchInstance(this, planet, baseNeighbor.index2, baseNeighbor.index0, index);
        baseNeighbor.rightChild = PlanetTriangle.fetchInstance(this, planet, baseNeighbor.index1, baseNeighbor.index2, index);
        hasChildren = true;
        baseNeighbor.hasChildren = true;

        replaceNeighbor(leftNeighbor, this, leftChild);
        replaceNeighbor(rightNeighbor, this, rightChild);
        replaceNeighbor(baseNeighbor.leftNeighbor, baseNeighbor, baseNeighbor.leftChild);
        replaceNeighbor(baseNeighbor.rightNeighbor, baseNeighbor, baseNeighbor.rightChild);
        leftChild.setNeighbors(rightChild, baseNeighbor.rightChild, leftNeighbor);
        rightChild.setNeighbors(baseNeighbor.leftChild, leftChild, rightNeighbor);
        baseNeighbor.leftChild.setNeighbors(baseNeighbor.rightChild, rightChild, baseNeighbor.leftNeighbor);
        baseNeighbor.rightChild.setNeighbors(leftChild, baseNeighbor.leftChild, baseNeighbor.rightNeighbor);

        Vector3 v = Vector3.fetchTempInstance();
        PlanetTools.getAverageNormal(leftChild.triangle, rightChild.triangle, baseNeighbor.leftChild.triangle, baseNeighbor.rightChild.triangle, v);
        planet.getVertexBuffer().setNormal(index, v);
        Vector3.releaseTempInstance(v);
        planet.getVertexBuffer().setColor(index, planet.getPlanetConfig().getColor(terrainDelta));

        correctNormal(index0, leftChild);
        correctNormal(index2, rightChild);
        correctNormal(baseNeighbor.index0, baseNeighbor.leftChild);
        correctNormal(baseNeighbor.index2, baseNeighbor.rightChild);
    }

    private void merge(Flags flags) {
        flags.planetChanged = true;
        //flags.merged++;
        planet.getVertexBuffer().releaseVertex(leftChild.index2);
        hasChildren = false;
        baseNeighbor.hasChildren = false;

        replaceNeighbor(leftChild.baseNeighbor, leftChild, this);
        replaceNeighbor(rightChild.baseNeighbor, rightChild, this);
        replaceNeighbor(baseNeighbor.leftChild.baseNeighbor, baseNeighbor.leftChild, baseNeighbor);
        replaceNeighbor(baseNeighbor.rightChild.baseNeighbor, baseNeighbor.rightChild, baseNeighbor);

        leftNeighbor = leftChild.baseNeighbor;
        rightNeighbor = rightChild.baseNeighbor;
        baseNeighbor.leftNeighbor = baseNeighbor.leftChild.baseNeighbor;
        baseNeighbor.rightNeighbor = baseNeighbor.rightChild.baseNeighbor;

        PlanetTriangle.releaseInstance(leftChild);
        PlanetTriangle.releaseInstance(rightChild);
        PlanetTriangle.releaseInstance(baseNeighbor.leftChild);
        PlanetTriangle.releaseInstance(baseNeighbor.rightChild);

        leftChild = null;
        rightChild = null;
        baseNeighbor.leftChild = null;
        baseNeighbor.rightChild = null;
    }

    protected void setNeighbors(PlanetTriangle leftNeighbor, PlanetTriangle rightNeighbor, PlanetTriangle baseNeighbor) {
        this.leftNeighbor = leftNeighbor;
        this.rightNeighbor = rightNeighbor;
        this.baseNeighbor = baseNeighbor;
    }

    private void replaceNeighbor(PlanetTriangle planetTriangle, PlanetTriangle oldNeighbor, PlanetTriangle newNeighbor) {
        if (planetTriangle.leftNeighbor == oldNeighbor) {
            planetTriangle.leftNeighbor = newNeighbor;
        } else if (planetTriangle.rightNeighbor == oldNeighbor) {
            planetTriangle.rightNeighbor = newNeighbor;
        } else {
            planetTriangle.baseNeighbor = newNeighbor;
        }
    }

    private boolean canBeMerged() {
        return (hasChildren && baseNeighbor.hasChildren && !leftChild.hasChildren && !rightChild.hasChildren && !baseNeighbor.leftChild.hasChildren && !baseNeighbor.rightChild.hasChildren);
    }

    private boolean isOutOfViewTestForMerge(Flags flags) {
        return (frustumIntersect == FrustumIntersect.Outside && baseNeighbor.getFrustrumIntersect(flags) == FrustumIntersect.Outside);
    }

    

    private boolean splitErrorMetric(Flags flags) {
        if (level > 36) return false;
        double dis = getCameraDistance(flags);
        boolean distanceThreshold = false;
        if (level < planet.splitThresholds.length - 3) distanceThreshold = dis < planet.splitThresholds[level];
        double t = (planet.getPlanetConfig().getAngleForTerrainErrorMetric() * dis);
        boolean terrainErrorThreshold = (t > 0.001) && (terrainError > t);
        return terrainErrorThreshold || distanceThreshold;
    }

    private boolean mergeErrorMetric(Flags flags) {
        if (level == 0) return false;
        double dis = getCameraDistance(flags);
        boolean distanceThreshold = true;
        if (level < planet.splitThresholds.length - 3) distanceThreshold = dis > planet.splitThresholds[level] + 200;
        double t = (planet.getPlanetConfig().getAngleForTerrainErrorMetric() * dis) - 0.01;
        boolean terrainErrorThreshold = (terrainError < t);
        return terrainErrorThreshold && distanceThreshold;
    }

    private boolean isVisibleAlongNormal(Flags flags) {
        if (normalChangeKey != flags.normalChangeKey) {
            normalChangeKey = flags.normalChangeKey;
            double x = planet.getPlanetCamera().getLocation().getX() - triangle.getCenter().getX();
            double y = planet.getPlanetCamera().getLocation().getY() - triangle.getCenter().getY();
            double z = planet.getPlanetCamera().getLocation().getZ() - triangle.getCenter().getZ();
            normalTest = triangle.getNormal().dot(x, y, z) > 0.0;
            //flags.normalTestsCount++;
        }
        return normalTest;
    }

    private FrustumIntersect getFrustrumIntersect(Flags flags) {
        if (frustumChangeKey != flags.frustrumChangeKey) {
            frustumChangeKey = flags.frustrumChangeKey;
            final int state = planet.getPlanetCamera().getPlaneState();
            frustumIntersect = planet.getPlanetCamera().contains(boundingBox);
            planet.getPlanetCamera().setPlaneState(state);
            //flags.frustrumTestsCount++;
        }
        return frustumIntersect;
    }

    private double getCameraDistance(Flags flags) {
        if (distanceChangeKey != flags.distanceChangeKey) {
            distanceChangeKey = flags.distanceChangeKey;
            cameraDistance = planet.getPlanetCamera().getLocation().distance(triangle.getCenter());
            //flags.cameraDistanceTestsCount++;
        }
        return cameraDistance;
    }

    public void takeStatistic(PlanetStatistic ps, Flags flags) {
        if (hasChildren) {
            leftChild.takeStatistic(ps, flags);
            rightChild.takeStatistic(ps, flags);
            ps.getLevelsStatistic(level).incPlanetTriangles();
        } else {
            ps.getLevelsStatistic(level).incPlanetTriangles();
            ps.getLevelsStatistic(level).incLastPlanetTriangle();
            boolean f = getFrustrumIntersect(flags) == FrustumIntersect.Inside;
            boolean n = isVisibleAlongNormal(flags);
            if (f) ps.getLevelsStatistic(level).incInFrustrum();
            if (n) ps.getLevelsStatistic(level).incCorrectNormal();
            if (f && n) ps.getLevelsStatistic(level).incDrawPlanetTriangle();
        }
    }

    private void correctNormal(int index, PlanetTriangle pt) {
        findPlanetTrianglesAlongIndex(index, pt, planet.tmpPTArray, 0);
        Vector3 v = Vector3.fetchTempInstance();
        PlanetTools.getAverageNormal_correction(v, planet.tmpPTArray);
        planet.getVertexBuffer().setNormalSpecial(index, v);
        Vector3.releaseTempInstance(v);
        for (int i = 0; i < planet.tmpPTArray.length; i++) {
            planet.tmpPTArray[i] = null;
        }
    }

    private void findPlanetTrianglesAlongIndex(int index, PlanetTriangle pt, PlanetTriangle[] arrPT, int arrIndex) {
        arrPT[arrIndex] = pt;
        if (pt.leftNeighbor.containsIndex(index) && !containsPT(arrPT, pt.leftNeighbor)) {
            findPlanetTrianglesAlongIndex(index, pt.leftNeighbor, arrPT, ++arrIndex);
        } else if (pt.rightNeighbor.containsIndex(index) && !containsPT(arrPT, pt.rightNeighbor)) {
            findPlanetTrianglesAlongIndex(index, pt.rightNeighbor, arrPT, ++arrIndex);
        } else if (pt.baseNeighbor.containsIndex(index) && !containsPT(arrPT, pt.baseNeighbor)) {
            findPlanetTrianglesAlongIndex(index, pt.baseNeighbor, arrPT, ++arrIndex);
        }
    }

    private boolean containsPT(PlanetTriangle[] arrPT, PlanetTriangle pt) {
        for (int i = 0; i < arrPT.length; i++) {
            if (arrPT[i] == pt) return true;
        }
        return false;
    }

    private boolean containsIndex(int index) {
        return index0 == index || index1 == index || index2 == index;
    }

    public Triangle getTriangle() {
        return triangle;
    }

}
