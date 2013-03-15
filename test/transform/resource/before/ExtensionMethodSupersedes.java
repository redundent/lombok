import java.text.Normalizer;
import java.text.Normalizer.Form;

import lombok.Extension;
import lombok.ExtensionMethod;

@ExtensionMethod(ExtensionMethodSupersedes.Strings.class)
class ExtensionMethodSupersedes {
	
	private void test6() {
		"foobar".matches("^f.*ar$");
	}
	
	static class Strings {
		@Extension
		public static boolean matches(final String value, final CharSequence regex) {
			return value.matches(Normalizer.normalize(regex, Form.NFKC));
		}
	}
}