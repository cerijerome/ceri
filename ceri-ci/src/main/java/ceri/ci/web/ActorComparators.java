package ceri.ci.web;

import java.util.Comparator;
import ceri.common.comparator.Comparators;

public class ActorComparators {
	public static final Comparator<Actor> NAME =
		Comparators.nonNull((lhs, rhs) -> Comparators.STRING.compare(lhs.name, rhs.name));
	public static final Comparator<Actor> BUILD =
		Comparators.nonNull((lhs, rhs) -> Comparators.STRING.compare(lhs.build, rhs.build));
	public static final Comparator<Actor> JOB =
		Comparators.nonNull((lhs, rhs) -> Comparators.STRING.compare(lhs.job, rhs.job));
	public static final Comparator<Actor> DEFAULT = Comparators.sequence(BUILD, JOB, NAME);

	private ActorComparators() {}

}
