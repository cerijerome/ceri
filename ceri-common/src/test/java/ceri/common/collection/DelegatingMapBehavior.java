package ceri.common.collection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class DelegatingMapBehavior {

	@Test
	public void shouldDelegateMethods() {
		@SuppressWarnings("unchecked")
		Map<Integer, String> map = Mockito.mock(Map.class);
		DelegatingMap<Integer, String> delegate = new DelegatingMap<>(map);
		delegate.size();
		delegate.isEmpty();
		delegate.containsKey(0);
		delegate.containsValue(0);
		delegate.get(0);
		delegate.put(0, "0");
		delegate.remove(0);
		delegate.putAll(Collections.singletonMap(0, "0"));
		delegate.clear();
		delegate.keySet();
		delegate.values();
		delegate.entrySet();
		verify(map).size();
		verify(map).isEmpty();
		verify(map).containsKey(0);
		verify(map).containsValue(0);
		verify(map).get(0);
		verify(map).put(0, "0");
		verify(map).remove(0);
		verify(map).putAll(Collections.singletonMap(0, "0"));
		verify(map).clear();
		verify(map).keySet();
		verify(map).values();
		verify(map).entrySet();
		verifyNoMoreInteractions(map);
	}

}
