/**
 * 
 */
package cz.mtheory.planet.util;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class Probe {

    public long splitCount = 0;
    public long mergeCount = 0;
    public long chunkNodeCount = 0;
    public long chunkCount = 0;
    public long terrainComputationsCount = 0;

    public void reset() {
        splitCount = 0;
        mergeCount = 0;
        chunkNodeCount = 0;
        chunkCount = 0;
        terrainComputationsCount = 0;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("split=").append(splitCount);
        sb.append(",merge=").append(mergeCount);
        sb.append(",chunkNode=").append(chunkNodeCount);
        sb.append(",chunk=").append(chunkCount);
        sb.append(",tc=").append(terrainComputationsCount);
        return sb.toString();
    }

}
