import java.util.List;

import lombok.OneToMany;
class Customer {
	@java.lang.SuppressWarnings("all")
	public static final lombok.core.data.OneToManyRelation<Customer, Order> Orders = new lombok.core.data.OneToManyRelation<Customer, Order>(){
		
		
		@java.lang.Override
		public java.lang.Long getReferencedKey(final Order item) {
			return item.getCustomerId();
		}
		
		@java.lang.Override
		public void setReferencedObject(final Customer item, final List<Order> ref) {
			item.orders = ref;
		}
		
		@java.lang.Override
		public void setRelatedId(final Order item, final java.lang.Long id) {
			item.setCustomerId(id);
		}
	};

	private long customerId;
	@OneToMany(field = "customerId")
	private List<Order> orders;
	
	@java.lang.SuppressWarnings("all")
	public long getCustomerId() {
		return this.customerId;
	}
	
	@java.lang.SuppressWarnings("all")
	public List<Order> getOrders() {
		return this.orders;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setCustomerId(final long customerId) {
		this.customerId = customerId;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setOrders(final List<Order> orders) {
		this.orders = orders;
	}
}
class Order {
	
	private long orderId;
	private long customerId;
	private String name;
	
	@java.lang.SuppressWarnings("all")
	public long getOrderId() {
		return this.orderId;
	}
	
	@java.lang.SuppressWarnings("all")
	public long getCustomerId() {
		return this.customerId;
	}
	
	@java.lang.SuppressWarnings("all")
	public String getName() {
		return this.name;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setOrderId(final long orderId) {
		this.orderId = orderId;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setCustomerId(final long customerId) {
		this.customerId = customerId;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setName(final String name) {
		this.name = name;
	}
}