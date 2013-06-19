import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.OneToMany;
import lombok.core.data.OneToManyRelation;
@Getter @Setter class Customer {
  private static final @java.lang.SuppressWarnings("all") OneToManyRelation<Customer, Order> ORDERS = new OneToManyRelation<Customer, Order>() {
    x() {
      super();
    }
    public @java.lang.Override java.lang.Long getReferencedKey(final Order item) {
      return item.getCustomerId();
    }
    public @java.lang.Override void setReferencedObject(final Customer item, final List<Order> related) {
      item.orders = related;
    }
    public @java.lang.Override void setRelatedId(final Order item, final java.lang.Long id) {
      item.setCustomerId(id);
    }
  };
  private long customerId;
  private @OneToMany(field = "customerId") List<Order> orders;
  <clinit>() {
  }
  Customer() {
    super();
  }
  public @java.lang.SuppressWarnings("all") long getCustomerId() {
    return this.customerId;
  }
  public @java.lang.SuppressWarnings("all") List<Order> getOrders() {
    return this.orders;
  }
  public @java.lang.SuppressWarnings("all") void setCustomerId(final long customerId) {
    this.customerId = customerId;
  }
  public @java.lang.SuppressWarnings("all") void setOrders(final List<Order> orders) {
    this.orders = orders;
  }
}
@Getter @Setter class Order {
  private long orderId;
  private long customerId;
  private String name;
  Order() {
    super();
  }
  public @java.lang.SuppressWarnings("all") long getOrderId() {
    return this.orderId;
  }
  public @java.lang.SuppressWarnings("all") long getCustomerId() {
    return this.customerId;
  }
  public @java.lang.SuppressWarnings("all") String getName() {
    return this.name;
  }
  public @java.lang.SuppressWarnings("all") void setOrderId(final long orderId) {
    this.orderId = orderId;
  }
  public @java.lang.SuppressWarnings("all") void setCustomerId(final long customerId) {
    this.customerId = customerId;
  }
  public @java.lang.SuppressWarnings("all") void setName(final String name) {
    this.name = name;
  }
}