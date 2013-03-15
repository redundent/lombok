import lombok.Extension;
import java.util.Arrays;
import java.util.List;

class ExtensionMethodGenericList {
	
	private void test8() {
		List<String> foo = null;
		List<String> s = ExtensionMethodGenericList.Objects.orElse(foo, Arrays.asList("bar"));
	}
	
	static class Objects {
		@Extension
		public static <T> List<T> orElse(List<T> value, List<T> orElse) {
			return value == null ? orElse : value;
		}
	}
}