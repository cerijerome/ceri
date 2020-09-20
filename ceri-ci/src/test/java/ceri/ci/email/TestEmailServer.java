package ceri.ci.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestEmailServer {
	private final List<List<Email>> responses = new ArrayList<>();
	private int response = 0;

	public void addResponse(Email... emails) {
		addResponse(Arrays.asList(emails));
	}

	public void addResponse(Collection<Email> emails) {
		responses.add(new ArrayList<>(emails));
	}

	public List<Email> nextResponse() {
		if (response >= responses.size()) return Collections.emptyList();
		return responses.get(response++);
	}

}
