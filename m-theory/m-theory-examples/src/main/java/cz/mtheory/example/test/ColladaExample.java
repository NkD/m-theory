package cz.mtheory.example.test;

import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.math.Vector3;
import com.ardor3d.util.ReadOnlyTimer;

import cz.mtheory.core.tool.Analyzer;
import cz.mtheory.example.ExampleBaseMtheory;
import cz.mtheory.helper.AxisGrid;

public class ColladaExample extends ExampleBaseMtheory {

    public static void main(String[] args) {
        start(ColladaExample.class);
    }

    @Override
    protected void initExample() {
        Analyzer.attachToScene(_root, _logicalLayer);
        ColladaStorage colladaStorage;
        try {
            ColladaImporter colladaImporter = new ColladaImporter();
            colladaStorage = colladaImporter.load("spacecraft/spacecraft.dae");
            _root.attachChild(colladaStorage.getScene());
            AxisGrid axisGrid = new AxisGrid(colladaStorage.getScene(), 200, 10, 10, true);
            _root.attachChild(axisGrid);
            //colladaStorage.getScene().addController(new RandomRotateController());
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        _camera.setLocation(30, 30, 30);
        _camera.lookAt(Vector3.ZERO, Vector3.UNIT_Y);
    }

    @Override
    protected void updateExample(ReadOnlyTimer timer) {
        //nothing
    }

}
