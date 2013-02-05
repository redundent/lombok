class Customer {
	@java.lang.SuppressWarnings("all")
	public static final lombok.core.data.OneToManyRelation<Customer, Order> PrimaryOrder = new lombok.core.data.OneToManyRelation<Customer, Order>(){
		
		
		@java.lang.Override
		public java.lang.Long getReferencedKey(final Order item) {
			return item.getCustomerId();
		}
		
		@java.lang.Override
		public void setReferencedObject(final Customer item, final java.util.List<Order> ref) {
			if (ref.size() > 0) item.primaryOrder = ref.get(0);
		}
		
		@java.lang.Override
		public void setRelatedId(final Order item, final java.lang.Long id) {
			item.setCustomerId(id);
		}
	};

	private long customerId;
	private Order primaryOrder;
	
	@java.lang.SuppressWarnings("all")
	public long getCustomerId() {
		return this.customerId;
	}
	
	@java.lang.SuppressWarnings("all")
	public Order getPrimaryOrder() {
		return this.primaryOrder;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setCustomerId(final long customerId) {
		this.customerId = customerId;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setPrimaryOrder(final Order primaryOrder) {
		this.primaryOrder = primaryOrder;
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