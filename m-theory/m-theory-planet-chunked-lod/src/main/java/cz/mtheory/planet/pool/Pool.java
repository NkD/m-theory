/**
 * 
 */
package cz.mtheory.planet.pool;

import java.util.ArrayList;
import java.util.List;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class Pool<T extends IPoolable> {

    public static final <T extends IPoolable> Pool<T> create(final Class<T> clazz, final int size) {
        return new Pool<T>(clazz, size);
    }

    private final Class<T> clazz;
    private final List<T> pool;
    private final int maxSize;

    private Pool(final Class<T> clazz, final int maxSize) {
        this.clazz = clazz;
        this.pool = new ArrayList<T>(maxSize);
        this.maxSize = maxSize;
    }

    private T createInstance() {
        try {
            return clazz.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Can not make new instance of " + clazz + ". Probably have not empty constructor", e);
        }
    }

    public T fetch(Object initiator) {
        T poolable;
        if (pool.isEmpty()) {
            poolable = createInstance();
            poolable.afterCreateNewInstance(initiator);
        } else {
            poolable = pool.remove(pool.size() - 1);
            poolable.afterGetFromPool();
        }
        return poolable;
    }

    public void release(T poolable) {
        poolable.beforeReturnToPool();
        if (maxSize > pool.size()) pool.add(poolable);
    }
    
    public int getActualSize(){
        return pool.size();
    }

}
