package ceri.common.game;

import static ceri.common.exception.ExceptionUtil.illegalArg;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.data.IntProvider;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.AnsiEscape;
import ceri.common.text.AnsiEscape.Sgr;
import ceri.common.text.StringUtil;
import ceri.common.text.Table;
import ceri.common.text.Table.Orientation;
import ceri.common.util.Align;
import ceri.common.util.Counter;

public class Sudoku {
	private static final int INVALID = -1;
	private static final Pattern LT_HIGH_REGEX = Pattern.compile("(<+)>+");
	private static final Pattern LT_LOW_REGEX = Pattern.compile("(>+)<+");
	private static final Comparator<IntProvider> GROUP_COMPARATOR =
		Comparator.comparingInt(g -> g.getInt(0));
	private static final int RED = 0xf00000;
	private static final int GRAY = 0xc0c0c0;
	private static final int BLACK = 0;
	private static final IntProvider RGBS = IntProvider.of(0xffffbf, 0xffbfff, 0xbfffff, 0xffdfdf,
		0xdfffdf, 0xdfdfff, 0xefefdf, 0xefdfef, 0xdfefef);
	// config
	private final String name;
	private final int size;
	private final Map<Integer, Set<IntProvider>> indexGroups;
	private final Map<IntProvider, BiConsumer<Sudoku, IntProvider>> groupRules;
	private final Display display;
	// state
	private final Map<IntProvider, State> groupStates = new IdentityHashMap<>();
	private final int[] masks;
	private final Set<Integer> changedIndexes = new HashSet<>();
	private final Counter trialCount = Counter.of();
	private long timeMs = -1;

	public static Sudoku easy9x9() {
		return Sudoku.of(9).boxes().fixed( //
			"53  7    ", //
			"6  195   ", //
			" 98    6 ", //
			"8   6   3", //
			"4  8 3  1", //
			"7   2   6", //
			" 6    28 ", //
			"   419  5", //
			"    8  79");
	}

	public static Sudoku medium9x9() {
		return Sudoku.of(9).boxes().fixed( //
			"4  6 8   ", //
			"91  328 6", //
			" 83 1   2", //
			"   8     ", //
			"   1  3 5", //
			"5 8 74   ", //
			"        8", //
			"      2  ", //
			" 7  964 3");
	}

	public static Sudoku hard9x9() {
		return Sudoku.of(9).boxes().fixed( //
			"     6  4", //
			"2      1 ", //
			" 5  3    ", //
			" 6    3  ", //
			"5    9   ", //
			"   1     ", //
			"1  74    ", //
			"   2   8 ", //
			"      6  ");
	}

	public static Sudoku expert9x9a() {
		return Sudoku.of(9).boxes().fixed( //
			" 9      3", //
			" 278     ", //
			"     9   ", //
			"     4   ", //
			"3   5 4  ", //
			" 7    2 1", //
			"  84  9  ", //
			"   7  158", //
			"6   2  4 ");
	}

	public static Sudoku expert9x9b() {
		return Sudoku.of(9).boxes().fixed( //
			"6    9  1", //
			"83     4 ", //
			"       2 ", //
			"  7 3    ", //
			"   5    8", //
			"2 46  7  ", //
			"4 58   6 ", //
			"   924   ", //
			"      3  ");
	}

	public static Sudoku expert9x9c() {
		return Sudoku.of(9).boxes().fixed( //
			" 8     3 ", //
			"7 4      ", //
			"   5   6 ", //
			"  1  6   ", //
			"9  34    ", //
			" 7 8  2  ", //
			"  8  342 ", //
			" 4    91 ", //
			"   25    ");
	}

	public static Sudoku expert9x9d() {
		return Sudoku.of(9).boxes().fixed( //
			"   7     ", //
			"1        ", //
			"   43 2  ", //
			"        6", //
			"   5 9   ", //
			"      418", //
			"    81   ", //
			"  2    5 ", //
			" 4    3  ");
	}

	public static Sudoku hardest9x9a() {
		return Sudoku.of(9).boxes().fixed( //
			"8        ", //
			"  36     ", //
			" 7  9 2  ", //
			" 5   7   ", //
			"    457  ", //
			"   1   3 ", //
			"  1    68", //
			"  85   1 ", //
			" 9    4  ");
	}

	public static Sudoku hardest9x9b() {
		return Sudoku.of(9).boxes().fixed( //
			"12 4  3  ", //
			"3   1  5 ", //
			"  6   1  ", //
			"7   9    ", //
			" 4 6 3   ", //
			"  3  2   ", //
			"5   8 7  ", //
			"  7     5", //
			"       98");
	}

	public static Sudoku hardest9x9c() {
		return Sudoku.of(9).boxes().fixed( //
			"6    894 ", //
			"9    61  ", //
			" 7  4    ", //
			"2  61    ", //
			"      2  ", //
			" 89  2   ", //
			"    6   5", //
			"       3 ", //
			"8    16  ");
	}

	public static Sudoku hardest9x9d() {
		return Sudoku.of(9).boxes().fixed( //
			"1     7  ", //
			" 5   91  ", //
			" 89     5", //
			"      6  ", //
			"   26    ", //
			"9    1  8", //
			"  234    ", //
			" 3     4 ", //
			"8    5  7");
	}

	public static Sudoku futoshiki5x5() {
		return Sudoku.of(5).futoshiki( //
			"     ", "> >>", "    ", //
			"4   2", "    ", "    ", //
			"  4  ", "    ", "    ", //
			"    4", "   <", "    ", //
			"     ", "<<  ", "    ");
	}

	public static Sudoku futoshiki653() {
		return Sudoku.of(9).futoshiki( //
			"8       4", ">>  > > ", "        ", //
			"    7    ", " >      ", " <      ", //
			"         ", "  <>    ", "  >     ", //
			"4       3", "   >   <", "  >  < <", //
			"         ", "        ", "<       ", //
			"7       9", " <<>>   ", "    <   ", //
			"         ", "<   <   ", "  < <<<>", //
			"    4    ", " >  < < ", ">   ><> ", //
			"3       7", "<    <  ", " >    <<");
	}

	public static Sudoku futoshiki659() {
		return Sudoku.of(9).futoshiki( //
			"   6 9   ", " < >   >", "   < << ", //
			"9       5", "   <<   ", "      < ", //
			"         ", ">    >> ", "       >", //
			"         ", "  >     ", "     < <", //
			"4       8", " <> < > ", "    >   ", //
			"         ", "    <  >", "      < ", //
			"         ", ">       ", "> <  > >", //
			"5       6", "   >  < ", " <<<    ", //
			"   4 8   ", "   >  > ", "        ");
	}

	public static Sudoku futoshiki662() {
		return Sudoku.of(9).futoshiki( //
			"         ", "  >    >", "    >   ", //
			"         ", "<       ", ">   > < ", //
			"   8     ", "   < < >", " <  < <>", //
			"         ", "      < ", "      < ", //
			"4  9 6  3", ">       ", "<  <    ", //
			"         ", "        ", "< >  >  ", //
			"     8   ", " >>     ", " <   >  ", //
			"         ", ">> < >> ", "    > < ", //
			"         ", "   << <>", "   <    ");
	}

	public static Sudoku killer9x9a() {
		return Sudoku.of(9).boxes().cages( //
			row("AABBBCBAB", 3, 15, 22, 4, 16, 15), //
			row("CCDDCCBAB", 25, 17), //
			row("CCAACBCCB", 9, 8, 20), //
			row("ADDADBCDB", 6, 14, 17, 17), //
			row("ABBCDBDDA", 13, 20, 12), //
			row("CBACDACCA", 27, 6, 20, 6), //
			row("CAACBAABB", 10, 14), //
			row("CBDBBDDBB", 8, 16, 15), //
			row("CBDBAAACC", 13, 17));
	}

	public static Sudoku killer9x9b() {
		return Sudoku.of(9).boxes().cages( //
			row("AABABABBB", 20, 18, 8, 9, 19, 21), //
			row("AABABAACC", 8), //
			row("BABCCABAB", 10, 13, 9, 9, 6), //
			row("BCAADDBAB", 23, 12, 16), //
			row("ACCBAACCC", 14, 10, 19, 21), //
			row("ACABBAACA", 12, 13), //
			row("BDAACBDDA", 14, 4, 6, 8, 14), //
			row("BDCBCBDDB", 9, 22, 21), //
			row("AACBBAABB", 13, 4));
	}

	public static Sudoku killer9x9c() {
		return Sudoku.of(9).boxes().cages( //
			row("AABBBABAA", 16, 20, 27, 13, 10), //
			row("ABCCBABCC", 14, 13, 22), //
			row("CBBCAABCC", 15), //
			row("CCABCADDA", 15, 7, 24, 12, 12), //
			row("BAABCBBBA", 7, 13), //
			row("BCBACDDCA", 20, 5, 9, 17, 24), //
			row("CCBAADCCA"), //
			row("ADDBBBDDD", 12, 10, 19, 15), //
			row("AACCAAAAA", 15, 19));
	}

	public static Sudoku killer9x9x() {
		return Sudoku.of(9).boxes().cages( //
			row("    ABBBC", 20, 27, 26), //
			row("    AAABC"), //
			row("    CCDDC", 24, 28), //
			row("     CCDC"), //
			row("ABC   CDC", 17, 18, 30), //
			row("ABCC     "), //
			row("ABBCC    "), //
			row("ACDDD    ", 16, 24), //
			row("ACCCD    "));
	}

	/**
	 * Encapsulates partial cages and sums for a single killer sudoku row.
	 */
	public static record CageRow(String fixed, String cage, int... sums) {}

	public static CageRow row(String fixed, String line, int... sums) {
		return new CageRow(fixed, line, sums);
	}

	public static CageRow row(String line, int... sums) {
		return row("", line, sums);
	}

	/**
	 * Display properties.
	 */
	private static class Display {
		public final Set<Integer> fixed = new HashSet<>();
		public final Set<IntProvider> boxes = CollectionUtil.identityHashSet();
		public final Set<IntProvider> futos = CollectionUtil.identityHashSet();
		public final Map<IntProvider, Integer> cageRgbs = new IdentityHashMap<>();
	}

	/**
	 * Internal state.
	 */
	private static class State {
		public int solvedMask = 0;

		private State(int solvedMask) {
			this.solvedMask = solvedMask;
		}
	}

	/**
	 * Trial-and-error entry.
	 */
	private static record Trial(int index, int number) {}

	public static void main(String[] args) {
		List.of( //
			// easy9x9(), medium9x9(), hard9x9(), //
			// expert9x9a(), expert9x9b(), expert9x9c(), expert9x9d(), //
			// hardest9x9a(), hardest9x9b(), hardest9x9c(), hardest9x9d(), //
			// futoshiki5x5(), futoshiki653(), futoshiki659(), futoshiki662(), //
			// killer9x9a(), killer9x9b(), killer9x9c(), //
			killer9x9x() //
		).forEach(s -> {
			s.solve();
			s.print(System.out, Table.UTF);
		});
	}

	public static Sudoku of(int size) {
		return new Sudoku(ReflectUtil.previousMethodName(1), size);
	}

	private Sudoku(String name, int size) {
		this.name = name;
		this.size = size;
		indexGroups = new HashMap<>();
		groupRules = new IdentityHashMap<>();
		display = new Display();
		masks = new int[size * size];
		Arrays.fill(masks, mask(size + 1) - 1);
		addLineGroups();
	}

	private Sudoku(Sudoku sudoku) {
		name = sudoku.name;
		size = sudoku.size;
		indexGroups = sudoku.indexGroups;
		groupRules = sudoku.groupRules;
		display = sudoku.display;
		masks = sudoku.masks.clone();
		sudoku.groupStates.forEach((g, s) -> groupStates.put(g, new State(s.solvedMask)));
	}

	public Sudoku fixed(String... rows) {
		for (int r = 0; r < rows.length; r++)
			fixed(rows[r], r);
		return this;
	}

	public Sudoku boxes() {
		return switch (size) {
			case 9 -> boxes(3, 3);
			case 6 -> boxes(2, 3);
			default -> throw illegalArg("Unknown %1$dx%1$d box rules", size);
		};
	}

	public Sudoku futoshiki(String... lines) { // fixed row, lt row, lt col
		for (int i = 0, n = 0; i < size; i++) {
			fixed(lines[n++], i);
			int j = i;
			futoGroups(lines[n++], c -> index(j, c));
			futoGroups(lines[n++], r -> index(r, j));
		}
		return this;
	}

	public Sudoku cages(CageRow... rows) {
		var map = new LinkedHashMap<Character, List<Set<Integer>>>();
		var sums = new ArrayList<Integer>();
		for (int r = 0; r < rows.length; r++) {
			fixed(rows[r].fixed(), r);
			cage(map, rows[r].cage(), r);
			for (int sum : rows[r].sums())
				sums.add(sum);
		}
		addCages(map, sums);
		return this;
	}

	public boolean solve() {
		var trials = new ArrayDeque<Trial>();
		long t0 = System.currentTimeMillis();
		timeMs = -1;
		boolean solved = solve(trials, trialCount);
		timeMs = System.currentTimeMillis() - t0;
		if (!solved) return false; // unsolvable
		if (trials.isEmpty()) return true; // no trials
		trials.forEach(t -> setMask(t.index(), mask(t.number())));
		return solve(new ArrayDeque<>(), Counter.of());
	}

	private void fixed(String line, int r) {
		char[] chars = line.toCharArray();
		for (int c = 0; c < chars.length; c++) {
			int n = chars[c] - '0';
			if (n >= 1 && n <= size) fixed(index(r, c), n);
		}
	}

	private Sudoku fixed(int index, int number) {
		display.fixed.add(index);
		setMask(index, mask(number));
		return this;
	}

	/* solving support */

	private boolean solve(Deque<Trial> trials, Counter counter) {
		processGroups();
		return complete() || processTrialAndError(trials, counter);
	}

	private boolean complete() {
		for (int index = 0; index < masks.length; index++)
			if (numbers(masks[index]) != 1) return false;
		return true;
	}

	private void processGroups() {
		while (true) {
			var groups = changedGroups();
			if (groups.isEmpty()) break;
			for (var group : groups)
				groupRules.get(group).accept(this, group);
		}
	}

	private boolean processTrialAndError(Deque<Trial> trials, Counter counter) {
		int index = trialIndex();
		if (index == INVALID) return false;
		int mask = masks[index];
		int lowest = lowest(mask);
		int highest = highest(mask);
		for (int number = lowest; number <= highest; number++) {
			if (!overlap(mask, mask(number))) continue;
			trials.add(new Trial(index, number));
			if (trial(trials, counter)) return true;
			trials.removeLast();
		}
		return false;
	}

	private int trialIndex() {
		int numbers = size + 1;
		int index = INVALID;
		for (int i = 0; i < masks.length; i++) {
			int n = numbers(masks[i]);
			if (n == 2) return i;
			if (n == 1 || n >= numbers) continue;
			numbers = n;
			index = i;
		}
		return index;
	}

	private boolean trial(Deque<Trial> trials, Counter counter) {
		var trial = trials.getLast();
		var copy = new Sudoku(this);
		copy.setMask(trial.index(), mask(trial.number()));
		counter.inc();
		try {
			return copy.solve(trials, counter);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/* box support */

	private Sudoku boxes(int rows, int cols) {
		for (int r = 0; r < size / rows; r++)
			for (int c = 0; c < size / cols; c++)
				box(r * rows, c * cols, rows, cols);
		return this;
	}

	private Sudoku box(int row, int col, int rows, int cols) {
		int[] indexes = new int[rows * cols];
		int i = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				indexes[i++] = index(row + r, col + c);
		var group = addFullGroup(indexes);
		display.boxes.add(group);
		return this;
	}

	/* cage support */

	private void addCages(Map<Character, List<Set<Integer>>> map, List<Integer> sums) {
		var groups = new ArrayList<IntProvider>();
		map.forEach((tag, cages) -> {
			for (var cage : cages) {
				var group = group(cage);
				groups.add(group);
				display.cageRgbs.put(group, RGBS.getInt(tag % RGBS.length()));
			}
		});
		Collections.sort(groups, GROUP_COMPARATOR);
		var sumIter = sums.iterator();
		groups.forEach(group -> {
			int sum = sumIter.next();
			addGroup(group, (s, g) -> s.processCage(g, sum));
		});
	}

	private void processCage(IntProvider group, int sum) {
		processUnique(group);
		processMinSum(group, sum);
		processMaxSum(group, sum);
	}

	private void processMinSum(IntProvider group, int sum) {
		for (int index : group) {
			int low = size;
			for (int i : group)
				if (i != index) low = Math.min(low, lowest(masks[i]));
			int min = lowUniqueSum(low, group.length() - 1);
			int rem = MathUtil.limit(sum - min, 0, size);
			setMask(index, masks[index] & (mask(rem + 1) - 1));
		}
	}

	private void processMaxSum(IntProvider group, int sum) {
		for (int index : group) {
			int high = 0;
			for (int i : group)
				if (i != index) high = Math.max(high, highest(masks[i]));
			int max = highUniqueSum(high, group.length() - 1);
			int rem = MathUtil.limit(sum - max, 1, size + 1);
			setMask(index, masks[index] & ~(mask(rem) - 1));
		}
	}

	private void cage(Map<Character, List<Set<Integer>>> map, String line, int r) {
		char[] chars = line.toCharArray();
		for (int c = 0; c < chars.length; c++) {
			char tag = chars[c];
			if (tag == ' ') continue;
			var cages = map.computeIfAbsent(tag, k -> new ArrayList<>());
			addToAdjacentCage(cages, index(r, c));
			mergeAdjacentCages(cages);
		}
	}

	private void addToAdjacentCage(List<Set<Integer>> cages, int index) {
		for (var cage : cages) {
			if (adjacent(cage, index)) {
				cage.add(index);
				return;
			}
		}
		var cage = new HashSet<Integer>();
		cage.add(index);
		cages.add(cage);
	}

	private boolean mergeAdjacentCages(List<Set<Integer>> cages) {
		for (int i = 0; i < cages.size() - 1; i++) {
			var current = cages.get(i);
			for (int j = i + 1; j < cages.size(); j++) {
				var next = cages.get(j);
				if (!adjacent(current, next)) continue;
				current.addAll(next);
				cages.remove(j);
				return true;
			}
		}
		return false;
	}

	private boolean adjacent(Set<Integer> indexes, int index) {
		for (int i : indexes)
			if (i == index || adjacent(i, index)) return true;
		return false;
	}

	private boolean adjacent(Set<Integer> indexes0, Set<Integer> indexes1) {
		for (int i : indexes1)
			if (adjacent(indexes0, i)) return true;
		return false;
	}

	private static IntProvider group(Set<Integer> indexes) {
		var array = indexes.stream().mapToInt(i -> i).toArray();
		Arrays.sort(array);
		return IntProvider.of(array);
	}

	/* futoshiki support */

	private void futoGroups(String line, IntUnaryOperator indexFn) {
		futoPairs(line, indexFn);
		futoLines(LT_LOW_REGEX.matcher(line), indexFn, true);
		futoLines(LT_HIGH_REGEX.matcher(line), indexFn, false);
	}

	private void futoLines(Matcher m, IntUnaryOperator indexFn, boolean low) {
		while (m.find()) {
			var group = futoLineGroup(m, indexFn);
			int i = m.group(1).length();
			addGroup(group, (s, g) -> s.processFutoLine(g, i, low));
		}
	}

	private void processFutoLine(IntProvider group, int offset, boolean low) {
		int index = group.getInt(offset);
		int n = group.length() - 1;
		int lineMask = futoLineMask(group, index);
		int mask = low ? highestMaskN(lineMask, n) : lowestMaskN(lineMask, n);
		setMask(index, this.masks[index] & mask);
	}

	private int futoLineMask(IntProvider group, int index) {
		int mask = 0;
		for (int i : group)
			if (i != index) mask |= masks[i];
		return mask;
	}

	private int lowestMaskN(int mask, int n) {
		if (mask != 0) for (int number = lowest(mask) + 1; number < size; number++) {
			if (overlap(mask, mask(number)) && --n <= 1) return ~(mask(number + 1) - 1);
		}
		return 0;
	}

	private int highestMaskN(int mask, int n) {
		if (mask != 0) for (int number = highest(mask) - 1; number > 1; number--) {
			if (overlap(mask, mask(number)) && --n <= 1) return mask(number) - 1;
		}
		return 0;
	}

	private IntProvider futoLineGroup(Matcher m, IntUnaryOperator indexFn) {
		int[] indexes = new int[m.end() + 1 - m.start()];
		for (int i = 0; i < indexes.length; i++)
			indexes[i] = indexFn.applyAsInt(m.start() + i);
		return IntProvider.of(indexes);
	}

	private void futoPairs(String line, IntUnaryOperator indexFn) {
		for (int i = 0; i < line.length(); i++) {
			var low = futoLow(line.charAt(i));
			if (low == null) continue;
			int i0 = indexFn.applyAsInt(i);
			int i1 = indexFn.applyAsInt(i + 1);
			var group = addGroup(low ? IntProvider.of(i0, i1) : IntProvider.of(i1, i0),
				(s, g) -> s.processFutoPair(g));
			display.futos.add(group);
		}
	}

	private Boolean futoLow(char ch) {
		return switch (ch) {
			case '<' -> true;
			case '>' -> false;
			case ' ' -> null;
			default -> throw illegalArg("Unsupported symbol: %c", ch);
		};
	}

	private void processFutoPair(IntProvider group) {
		int lowIndex = group.getInt(0);
		int highIndex = group.getInt(1);
		int lowestMask = mask(lowest(masks[lowIndex]) + 1) - 1;
		int highestMask = mask(highest(masks[highIndex])) - 1;
		setMask(highIndex, masks[highIndex] & ~lowestMask);
		setMask(lowIndex, masks[lowIndex] & highestMask);
	}

	/* uniqueness processing */

	private void processUnique(IntProvider group) {
		processSingles(group);
		int n = group.length() - numbers(groupState(group).solvedMask);
		for (int i = 2; i < n; i++)
			findTuples(group, i);
	}

	private void processSingles(IntProvider group) {
		var state = groupState(group);
		for (int index : group) {
			int mask = masks[index];
			if (numbers(mask) != 1 || overlap(state.solvedMask, mask)) continue;
			state.solvedMask |= mask;
			for (int i : group)
				if (i != index) setMask(i, masks[i] & ~mask);
		}
	}

	private void findTuples(IntProvider group, int count) {
		int[] offsets = new int[count];
		offsets[0] = -1;
		int i = 0;
		while (true) {
			offsets[i]++;
			if (offsets[i] >= group.length()) {
				if (--i < 0) return;
			} else {
				int n = numbers(mask(group, offsets[i]));
				if (n == 1 || n > count) continue;
				if (numbers(mask(group, offsets, i)) > count) continue;
				if (++i >= count) break;
				offsets[i] = offsets[i - 1];
			}
		}
		removeTuple(group, offsets);
	}

	private void removeTuple(IntProvider group, int[] offsets) {
		int tupleMask = mask(group, offsets, offsets.length - 1);
		for (int index : group)
			if (!containsIndex(group, offsets, index)) setMask(index, masks[index] & ~tupleMask);
	}

	private boolean containsIndex(IntProvider group, int[] offsets, int index) {
		for (int offset : offsets)
			if (group.getInt(offset) == index) return true;
		return false;
	}

	private int mask(IntProvider group, int[] offsets, int i) {
		int mask = 0;
		for (; i >= 0; i--)
			mask |= mask(group, offsets[i]);
		return mask;
	}

	/* group support */

	private Set<IntProvider> changedGroups() {
		Set<IntProvider> changedGroups = StreamUtil
			.toIdentitySet(changedIndexes.stream().flatMap(index -> groups(index).stream()));
		changedIndexes.clear();
		return changedGroups;
	}

	private void addLineGroups() {
		for (int i = 0; i < size; i++) {
			int[] row = new int[size];
			int[] col = new int[size];
			for (int j = 0; j < size; j++) {
				row[j] = index(i, j);
				col[j] = index(j, i);
			}
			addFullGroup(row);
			addFullGroup(col);
		}
	}

	private IntProvider addFullGroup(int... indexes) {
		if (indexes.length != size)
			throw illegalArg("Group size must be %d: %s", size, Arrays.toString(indexes));
		return addGroup(IntProvider.of(indexes), (s, g) -> s.processUnique(g));
	}

	private IntProvider addGroup(IntProvider group, BiConsumer<Sudoku, IntProvider> rule) {
		for (int index : group)
			indexGroups.computeIfAbsent(index, i -> CollectionUtil.identityHashSet()).add(group);
		groupRules.put(group, rule);
		return group;
	}

	private int mask(IntProvider group, int offset) {
		return masks[group.getInt(offset)];
	}

	private Set<IntProvider> groups(int index) {
		return indexGroups.getOrDefault(index, Set.of());
	}

	private State groupState(IntProvider group) {
		return groupStates.computeIfAbsent(group, g -> new State(0));
	}

	/* cell access */

	private boolean setMask(int index, int mask) {
		if (masks[index] == mask) return false;
		if (mask == 0)
			throw illegalArg("No numbers left at %d (%d,%d)", index, row(index), col(index));
		masks[index] = mask;
		changedIndexes.add(index);
		return true;
	}

	private boolean adjacent(int index0, int index1) {
		int diff = Math.abs(index1 - index0);
		return (diff == size) || ((diff == 1) && row(index0) == row(index1));
	}

	private int index(int row, int col) {
		return row * size + col;
	}

	private int row(int index) {
		return index / size;
	}

	private int col(int index) {
		return index % size;
	}

	private boolean valid(int r, int c) {
		return r >= 0 && r < size && c >= 0 && c < size;
	}

	private static int uniqueSum(int n) {
		return (n * (n + 1)) >>> 1;
	}

	private static int lowUniqueSum(int low, int n) {
		return uniqueSum(low + n - 1) - uniqueSum(low - 1);
	}

	private static int highUniqueSum(int high, int n) {
		return uniqueSum(high) - uniqueSum(high - n);
	}

	private static int lowest(int mask) {
		int n = Integer.numberOfTrailingZeros(mask);
		return n == Integer.SIZE ? 0 : n + 1;
	}

	private static int highest(int mask) {
		int n = Integer.numberOfLeadingZeros(mask);
		return n == Integer.SIZE ? 0 : Integer.SIZE - n;
	}

	private static int numbers(int mask) {
		return Integer.bitCount(mask);
	}

	private static boolean overlap(int mask0, int mask1) {
		return (mask0 & mask1) != 0;
	}

	private static int mask(int... numbers) {
		int mask = 0;
		for (int number : numbers)
			mask |= (1 << (number - 1));
		return mask;
	}

	/* printing */

	public void print(PrintStream out, Table frame) {
		int w = Math.max(IntStream.of(masks).map(m -> numbers(m)).max().orElse(1), 3);
		frame.print(out, (r, c, cell) -> {
			if (r < size && c < size) cell.lines(nums(masks[index(r, c)], w));
		}, this::format);
		printStats(out);
	}

	private void printStats(PrintStream out) {
		boolean complete = complete();
		out.print(name + " = ");
		if (complete) out.println("solved");
		else out.printf("incomplete (%s combos)%n", combos());
		if (trialCount.count() > 0) out.printf("tries = %d%n", trialCount.count());
		if (timeMs >= 0) out.printf("time = %dms%n%n", timeMs);
	}

	private String format(Table f, int r, int c, Orientation or, String s) {
		var sgr = AnsiEscape.csi.sgr();
		formatDigits(sgr, or, r, c);
		formatCage(sgr, or, r, c);
		s = formatBox(f, sgr, or, s, r, c);
		s = formatFuto(sgr, or, s, r, c);
		return sgr + s + Sgr.reset;
	}

	private void formatDigits(Sgr sgr, Orientation or, int r, int c) {
		if (or != Orientation.c) return;
		int index = index(r, c);
		if (display.fixed.contains(index)) return;
		if (numbers(masks[index]) == 1) sgr.fgColor24(RED);
		else sgr.fgColor24(GRAY).italic(1);
	}

	private void formatCage(Sgr sgr, Orientation or, int r, int c) {
		var cage = cage(r, c);
		if (cage == null) return;
		if (switch (or) {
			case c -> true;
			case n -> cage == cage(r - 1, c);
			case w -> cage == cage(r, c - 1);
			case nw -> cage == cage(r - 1, c - 1) && cage == cage(r - 1, c)
				&& cage == cage(r, c - 1);
			default -> false;
		}) sgr.bgColor24(display.cageRgbs.get(cage));
	}

	private IntProvider cage(int r, int c) {
		for (var cage : display.cageRgbs.keySet())
			if (contains(cage, r, c)) return cage;
		return null;
	}

	private String formatFuto(Sgr sgr, Orientation or, String s, int r, int c) {
		int index = index(r, c);
		var fmt = sgr.toString();
		if (or == Orientation.n) {
			var futo = futo(index - size, index);
			if (futo != null)
				s = StringUtil.pad("\0", s.length(), s.substring(0, 1), Align.H.center)
					.replace("\0", sgr.fgColor24(BLACK) + (futo ? "^" : "v") + Sgr.reset + fmt);
		} else if (or == Orientation.w) {
			var futo = futo(index - 1, index);
			if (futo != null) return sgr.fgColor24(BLACK) + (futo ? "<" : ">") + Sgr.reset;
		}
		return fmt + s + Sgr.reset;
	}

	private Boolean futo(int index0, int index1) {
		for (var futo : display.futos) {
			if (futo.getInt(0) == index0 && futo.getInt(1) == index1) return true;
			if (futo.getInt(0) == index1 && futo.getInt(1) == index0) return false;
		}
		return null;
	}

	private String formatBox(Table f, Sgr sgr, Orientation or, String s, int r, int c) {
		boolean border = border(or, r, c);
		if (border) s = borderEdge(f, or, s, r, c);
		else if (or != Orientation.c) sgr.fgColor24(GRAY);
		return s;
	}

	private String borderEdge(Table f, Orientation or, String s, int r, int c) {
		return switch (or) {
			case nw -> String.valueOf(boxCross(f, r, c));
			case ne -> String.valueOf(boxCross(f, r, c + 1));
			case sw -> String.valueOf(boxCross(f, r + 1, c));
			case se -> String.valueOf(boxCross(f, r + 1, c + 1));
			default -> s;
		};
	}

	private char boxCross(Table f, int r, int c) {
		boolean n = boxBorder(r - 1, c - 1, Orientation.e) || boxBorder(r - 1, c, Orientation.w);
		boolean s = boxBorder(r, c - 1, Orientation.e) || boxBorder(r, c, Orientation.w);
		boolean e = boxBorder(r - 1, c, Orientation.s) || boxBorder(r, c, Orientation.n);
		boolean w = boxBorder(r - 1, c - 1, Orientation.s) || boxBorder(r, c - 1, Orientation.n);
		if (n && s && e && w) return f.c();
		if (n && s && w) return f.e();
		if (n && s && e) return f.w();
		if (n && s) return f.v();
		if (e && w && n) return f.s();
		if (e && w && s) return f.n();
		if (e && w) return f.h();
		if (n) return w ? f.se() : f.sw();
		if (s) return w ? f.ne() : f.nw();
		return f.c();
	}

	private boolean border(Orientation or, int r, int c) {
		return switch (or) {
			case n -> boxBorder(r, c, Orientation.n) || boxBorder(r - 1, c, Orientation.s);
			case w -> boxBorder(r, c, Orientation.w) || boxBorder(r, c - 1, Orientation.e);
			case s -> boxBorder(r, c, Orientation.s);
			case e -> boxBorder(r, c, Orientation.e);
			case nw -> boxBorder(r, c, Orientation.nw) || boxBorder(r, c - 1, Orientation.ne)
				|| boxBorder(r - 1, c, Orientation.sw) || boxBorder(r - 1, c - 1, Orientation.se);
			case ne -> boxBorder(r, c, Orientation.ne) || boxBorder(r - 1, c, Orientation.se);
			case sw -> boxBorder(r, c, Orientation.sw) || boxBorder(r, c - 1, Orientation.se);
			case se -> boxBorder(r - 1, c, Orientation.se);
			default -> false;
		};
	}

	private boolean boxBorder(int r, int c, Orientation or) {
		var box = box(r, c);
		if (box == null) return false;
		return switch (or) {
			case n -> !contains(box, r - 1, c);
			case s -> !contains(box, r + 1, c);
			case e -> !contains(box, r, c + 1);
			case w -> !contains(box, r, c - 1);
			case nw -> !contains(box, r - 1, c) || !contains(box, r, c - 1);
			case ne -> !contains(box, r - 1, c) || !contains(box, r, c + 1);
			case sw -> !contains(box, r + 1, c) || !contains(box, r, c - 1);
			case se -> !contains(box, r + 1, c) || !contains(box, r, c + 1);
			default -> false;
		};
	}

	private IntProvider box(int r, int c) {
		for (var box : display.boxes)
			if (contains(box, r, c)) return box;
		return null;
	}

	private boolean contains(IntProvider group, int r, int c) {
		return group != null && valid(r, c) && group.indexOf(0, index(r, c)) != -1;
	}

	private String nums(int mask, int w) {
		var b = new StringBuilder();
		for (int i = 1; i <= size; i++)
			if (overlap(mask, mask(i))) b.append(i);
		return StringUtil.pad(b, w, Align.H.center);
	}

	private BigDecimal combos() {
		var count = BigDecimal.ONE;
		for (int i = 0; i < masks.length; i++)
			count = count.multiply(BigDecimal.valueOf(numbers(masks[i])));
		return count;
	}
}
