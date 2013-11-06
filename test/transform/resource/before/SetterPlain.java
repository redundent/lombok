import lombok.Setter;
class SetterPlain {
	@lombok.Setter int i;
	@Setter int foo;
	@Setter java.sql.Timestamp ts;
	@Setter int[] arr;
}