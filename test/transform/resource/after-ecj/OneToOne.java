import lombok.Getter;
import lombok.Setter;
import lombok.OneToOne;
import lombok.core.data.OneToOneRelation;
@Getter @Setter class Customer {
  private static final @java.lang.SuppressWarnings("all") OneToOneRelation<Customer, Order> PRIMARY_ORDER = new OneToOneRelation<Customer, Order>() {
    x() {
      super();
    }
    public @java.lang.Override java.lang.Long getReferencedKey(final Customer item) {
      return item.getOrderId();
    }
    public @java.lang.Override void setReferencedObject(final Customer item, final Order related) {
      item.primaryOrder = related;
    }
    public @java.lang.Override void setRelatedId(final Customer item, final java.lang.Long id) {
      item.setOrderId(id);
    }
  };
  private long customerId;
  private long orderId;
  private @OneToOne(field = "orderId") Order primaryOrder;
  <clinit>() {
  }
  Customer() {
    super();
  }
  public @java.lang.SuppressWarnings("all") long getCustomerId() {
    return this.customerId;
  }
  public @java.lang.SuppressWarnings("all") long getOrderId() {
    return this.orderId;
  }
  public @java.lang.SuppressWarnings("all") Order getPrimaryOrder() {
    return this.primaryOrder;
  }
  public @java.lang.SuppressWarnings("all") void setCustomerId(final long customerId) {
    this.customerId = customerId;
  }
  public @java.lang.SuppressWarnings("all") void setOrderId(final long orderId) {
    this.orderId = orderId;
  }
  public @java.lang.SuppressWarnings("all") void setPrimaryOrder(final Order primaryOrder) {
    this.primaryOrder = primaryOrder;
  }
}
@Getter @Setter class Order {
  private long orderId;
  private String name;
  Order() {
    super();
  }
  public @java.lang.SuppressWarnings("all") long getOrderId() {
    return this.orderId;
  }
  public @java.lang.SuppressWarnings("all") String getName() {
    return this.name;
  }
  public @java.lang.SuppressWarnings("all") void setOrderId(final long orderId) {
    this.orderId = orderId;
  }
  public @java.lang.SuppressWarnings("all") void setName(final String name) {
    this.name = name;
  }
}