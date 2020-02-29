package jbse.common;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TypeTest {
	@Test
	public void test1() {
		final String methodSignature = "<H::Ljava/lang/Runnable;L:Ljava/util/HashMap<Ljava/lang/Integer;Ljava/lang/String;>;:Ljava/util/function/Function<Ljava/lang/Integer;Ljava/lang/String;>;>(TH;)V";
		final String methodParameters = Type.splitMethodGenericSignatureTypeParameters(methodSignature);
		final Map<String, String> actual = Type.splitTypeParameters(methodParameters);
		final Map<String, String> expected = new HashMap<>();
		expected.put("L", "Ljava/util/HashMap<Ljava/lang/Integer;Ljava/lang/String;>;");
		assertEquals(expected, actual);
	}
}
