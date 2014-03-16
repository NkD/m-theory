package cz.mtheory.example.test;

import com.ardor3d.math.Vector3;
import com.ardor3d.util.ReadOnlyTimer;

import cz.mtheory.core.tool.Analyzer;
import cz.mtheory.example.ExampleBaseMtheory;
import cz.mtheory.example.tool.SceneBuilder;
import cz.mtheory.helper.AxisGrid;

public class SceneBuilderExample extends ExampleBaseMtheory {

    public static void main(String[] args) {
        start(SceneBuilderExample.class);
    }

    @Override
    protected void initExample() {
        Analyzer.attachToScene(_root, _logicalLayer);

        SceneBuilder.randomRotatingSpheres(_root, 20);
        SceneBuilder.randomRotatingBoxes(_root, 20);
       
        AxisGrid axis = new AxisGrid(null, 100,1,10,true);
        _root.attachChild(axis);
        
        _camera.setLocation(100, 100, 100);
        _camera.lookAt(Vector3.ZERO, Vector3.UNIT_Y);
       
    }

    @Override
    protected void updateExample(ReadOnlyTimer timer) {
        //nothing
    }

}
