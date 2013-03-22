package lombok.core.data;


/**
 * Provides a relation between objects, much like a DB foreign key. i.e. Customer -> Orders
 * @author Jason Blackwell
 *
 * @param <T> The base object type. i.e. Customer
 * @param <K> The related object type. i.e. Order
 * @param <E> The load option type for determining if the related object was requested to be loaded.
 */
public abstract class OneToOneRelation<T, K> implements ReferencedBy<T> {
	public abstract Long getReferencedKey(T base);
	public abstract void setReferencedObject(T base, K ref);
}