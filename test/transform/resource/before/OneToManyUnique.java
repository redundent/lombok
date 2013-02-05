import lombok.Getter;
import lombok.Setter;
import lombok.OneToMany;

@Getter @Setter
class Customer {
	private long customerId;
	@OneToMany(field="customerId", unique=true)
	private Order primaryOrder;
}

@Getter @Setter
class Order {
	private long orderId;
	private long customerId;
	private String name;
}
