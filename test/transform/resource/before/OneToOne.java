import lombok.Getter;
import lombok.Setter;
import lombok.OneToOne;

@Getter @Setter
class Customer {
	private long customerId;
	private long orderId;
	@OneToOne(field="orderId")
	private Order primaryOrder;
}

@Getter @Setter
class Order {
	private long orderId;
	private String name;
}
