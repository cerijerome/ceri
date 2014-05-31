package ceri.ci.web;

import java.util.Comparator;
import ceri.common.comparator.BaseComparator;
import ceri.common.comparator.Comparators;

public class ActorComparators {
	public static Comparator<Actor> NAME = new BaseComparator<Actor>() {
		@Override
		protected int compareNonNull(Actor lhs, Actor rhs) {
			return Comparators.STRING.compare(lhs.name, rhs.name);
		}
	};
	public static Comparator<Actor> BUILD = new BaseComparator<Actor>() {
		@Override
		protected int compareNonNull(Actor lhs, Actor rhs) {
			return Comparators.STRING.compare(lhs.build, rhs.build);
		}
	};
	public static Comparator<Actor> JOB = new BaseComparator<Actor>() {
		@Override
		protected int compareNonNull(Actor lhs, Actor rhs) {
			return Comparators.STRING.compare(lhs.job, rhs.job);
		}
	};
	public static Comparator<Actor> DEFAULT = Comparators.sequence(BUILD, JOB, NAME);
	
	private ActorComparators() {}

}
