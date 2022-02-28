package top.focess.qq.api.command.data;

/**
 * A buffer which stores data
 *
 * @param <T>  the elements' type in the buffer
 */
public abstract class DataBuffer<T> {

    /**
     * Flip all the buffers. Make them all readable.
     */
    public abstract void flip();

    /**
     * Put the element into the buffer
     *
     * @param t the element need to be put in the buffer
     */
    public abstract void put(T t);

    /**
     * Get element in the buffer in order
     *
     * @return element in the buffer in order
     */
    public abstract T get();
}
