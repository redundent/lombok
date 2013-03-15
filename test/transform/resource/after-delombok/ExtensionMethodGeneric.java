import lombok.Extension;

class ExtensionMethodGeneric {
	
	private void test6() {
		String foo = null;
		String s = ExtensionMethodGeneric.Objects.orElse(foo, "bar");
	}
	
	static class Objects {
		@Extension
		public static <T> T orElse(T value, T orElse) {
			return value == null ? orElse : value;
		}
	}
}