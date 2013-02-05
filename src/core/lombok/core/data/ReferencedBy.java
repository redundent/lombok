package lombok.core.data;

public interface ReferencedBy<T> {
	void setRelatedId(T item, Long id);
}
