class Customer {
	@java.lang.SuppressWarnings("all")
	public static final lombok.core.data.OneToOneRelation<Customer, Order> PrimaryOrder = new lombok.core.data.OneToOneRelation<Customer, Order>(){
		
		
		@java.lang.Override
		public java.lang.Long getReferencedKey(final Customer item) {
			return item.getOrderId();
		}
		
		@java.lang.Override
		public void setReferencedObject(final Customer item, final Order ref) {
			item.primaryOrder = ref;
		}
		
		@java.lang.Override
		public void setRelatedId(final Customer item, final java.lang.Long id) {
			item.setOrderId(id);
		}
	};

	private long customerId;
	private long orderId;
	private Order primaryOrder;
	
	@java.lang.SuppressWarnings("all")
	public long getCustomerId() {
		return this.customerId;
	}
	
	@java.lang.SuppressWarnings("all")
	public long getOrderId() {
		return this.orderId;
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
	public void setOrderId(final long orderId) {
		this.orderId = orderId;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setPrimaryOrder(final Order primaryOrder) {
		this.primaryOrder = primaryOrder;
	}
}
class Order {
	
	private long orderId;
	private String name;
	
	@java.lang.SuppressWarnings("all")
	public long getOrderId() {
		return this.orderId;
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
	public void setName(final String name) {
		this.name = name;
	}
}