/**
 * 
 */
package cz.mtheory.planet.pool;

/** 
 * M-theory project
 *
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public interface IPoolable {

    public void afterCreateNewInstance(Object initiator);
    
    public void afterGetFromPool();
    
    public void beforeReturnToPool();
    
}
