import lombok.Getter;
class GetterPlain {
	@lombok.Getter int i;
	@Getter int foo;
	@Getter java.sql.Timestamp ts;
	@Getter int[] arr;
}