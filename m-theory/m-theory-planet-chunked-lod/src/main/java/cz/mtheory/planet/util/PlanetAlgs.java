/**
 * 
 */
package cz.mtheory.planet.util;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class PlanetAlgs {

    private PlanetAlgs() {
        //utility class
    }

    /**
     * Based on tutorial created by Author of "Infinity Queast for Earth"
     * http://
     * www.infinity-universe.com/Infinity/index.php?option=com_smf&Itemid=
     * 75&topic=7284.0
     * 
     * @param planetCamera
     * @param planetRadius
     *            - planetCenter must be Vector3.ZERO
     * @param chunkSphere
     * @return true = chunk is visible
     */
    public static boolean chunkHorizonCulling(final Camera planetCamera, final double planetRadius, final BoundingSphere chunkSphere) {
        Vector3 s1 = Vector3.fetchTempInstance();
        planetCamera.getLocation().negate(s1);
        final double d1 = s1.length();
        boolean status = false;
        if (d1 > planetRadius) { //Camera is not inside planet mass
            final double invD1 = 1.0f / d1;
            s1.multiplyLocal(invD1);
            double pdis = planetRadius * planetRadius * invD1;
            //distance chunk to plane P (plocha spojujici tecne body planety) - presnejsi verze  
            //double chunkDistance = (((-O1C.getX() * chunkSphere.getCenter().getX()) + (-O1C.getY() * chunkSphere.getCenter().getY()) + (-O1C.getZ() * chunkSphere.getCenter().getZ())) - ((-O1C.getX() * (-O1C.getX() * y)) + (-O1C.getY() * (-O1C.getY() * y)) + (-O1C.getZ() * (-O1C.getZ() * y)))) + chunkSphere.getRadius();
            //same but simplify by Mathematica - lisi se obcas na devatem radu v desetinach
            double chunkDistance = (-s1.getX() * s1.getX() * pdis - s1.getY() * s1.getY() * pdis - s1.getZ() *
                    s1.getZ() * pdis - s1.getX() * chunkSphere.getCenter().getX() - s1.getY() *
                    chunkSphere.getCenter().getY() - s1.getZ() * chunkSphere.getCenter().getZ() + chunkSphere.getRadius());
            status = chunkDistance > 0;
            if (!status) {
                Vector3 s2 = Vector3.fetchTempInstance();
                chunkSphere.getCenter().subtract(planetCamera.getLocation(), s2);
                final double d2 = s2.length();
                final double invD2 = 1.0f / d2;
                s2.multiplyLocal(invD2);
                double k = s1.dot(s2);
                double k1 = planetRadius * invD1;
                double k2 = chunkSphere.getRadius() * invD2;
                status = true;
                if (k > k1 * k2) status = (-2.0 * k * k1 * k2 + k1 * k1 + k2 * k2) < (1.0 - k * k);
                Vector3.releaseTempInstance(s2);
            }
        }
        Vector3.releaseTempInstance(s1);
        return status;
    }
}
