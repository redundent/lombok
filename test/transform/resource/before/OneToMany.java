import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.OneToMany;

@Getter @Setter
class Customer {
	private long customerId;
	@OneToMany(field="customerId")
	private List<Order> orders;
}

@Getter @Setter
class Order {
	private long orderId;
	private long customerId;
	private String name;
}
