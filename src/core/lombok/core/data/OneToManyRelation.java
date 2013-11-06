package lombok.core.data;

import java.util.Iterator;
import java.util.List;

/**
 * Provides a relation between objects, much like a DB foreign key. i.e. Customer -> Orders
 * @author Jason Blackwell
 *
 * @param <T> The base object type. i.e. Customer
 * @param <K> The related object type. i.e. Order
 * @param <E> The load option type for determining if the related object was requested to be loaded.
 */
public abstract class OneToManyRelation<T, K> implements ReferencedBy<K> {	
	public abstract Long getReferencedKey(K ref);
	public abstract void setReferencedObject(T base, List<K> ref);
	
	protected final <V> V firstOrDefault(Iterable<V> items) {
		if (items == null) {
			return null;
		}
		
		Iterator<V> i = items.iterator();
		return (i.hasNext() ? i.next() : null);
	}
}
