package cz.mtheory.example.tool;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

public class RandomRotateController implements SpatialController<Spatial> {
    
    private final Vector3 _axis = new Vector3(Rnd.nextFloat(-1, 1),Rnd.nextFloat(-1, 1),Rnd.nextFloat(-1, 1)).normalizeLocal();
    private final Matrix3 _rotate = new Matrix3();
    private final int _speed = 10;// Rnd.nextInt(15, 30);
    private double _angle = Rnd.nextInt(0, 359);

    @Override
    public void update(final double time, final Spatial caller) {
        _angle = (_angle + (time * _speed)) % 360;
        _rotate.fromAngleNormalAxis(_angle * MathUtils.DEG_TO_RAD, _axis);
        caller.setRotation(_rotate);
    }
}
