import java.util.ArrayList;
import lombok.Extension;

class ExtensionMethodAndVar {

	public static Iterable<String> foobar() {
		return new ArrayList<String>();
	}

	private void test() {
		for (String s : foobar()) {
		}
		final java.lang.reflect.Method handler = Object.class.getDeclaredMethods()[0];
		for (String s : foobar()) {
		}
	}

	static class Objects {
		@Extension
		public static <T> T orElse(T value, T orElse) {
			return value == null ? orElse : value;
		}
	}
}