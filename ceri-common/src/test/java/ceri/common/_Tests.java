package ceri.common;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.collection.ArrayIteratorBehavior;
import ceri.common.collection.ArrayUtilTest;
import ceri.common.collection.CollectionUtilTest;
import ceri.common.collection.DelegatingMapBehavior;
import ceri.common.collection.FixedSizeCacheBehavior;
import ceri.common.collection.ImmutableByteArrayBehavior;
import ceri.common.collection.ImmutableUtilTest;
import ceri.common.collection.MapBuilderBehavior;
import ceri.common.collection.StreamUtilTest;
import ceri.common.comparator.ComparatorSequenceBehavior;
import ceri.common.comparator.ComparatorsTest;
import ceri.common.comparator.EnumComparatorsTest;
import ceri.common.concurrent.AsyncRunnerBehavior;
import ceri.common.concurrent.BooleanConditionBehavior;
import ceri.common.concurrent.ConcurrentUtilTest;
import ceri.common.concurrent.RuntimeInterruptedExceptionBehavior;
import ceri.common.concurrent.ValueConditionBehavior;
import ceri.common.date.CalendarFieldBehavior;
import ceri.common.date.ImmutableDateBehavior;
import ceri.common.date.ImmutableTimeZoneBehavior;
import ceri.common.date.MultiDateParserBehavior;
import ceri.common.date.TimeUnitBehavior;
import ceri.common.email.EmailUtilTest;
import ceri.common.event.EventTrackerBehavior;
import ceri.common.event.IntListenersBehavior;
import ceri.common.event.ListenersBehavior;
import ceri.common.factory.DateFactoriesTest;
import ceri.common.factory.FactoriesTest;
import ceri.common.factory.FactoryExceptionBehavior;
import ceri.common.factory.NumberFactoriesTest;
import ceri.common.factory.StringFactoriesTest;
import ceri.common.filter.CollectionFiltersTest;
import ceri.common.filter.FiltersTest;
import ceri.common.io.BitIteratorBehavior;
import ceri.common.io.BlockingBufferStreamBehavior;
import ceri.common.io.ByteArrayDataInputBehavior;
import ceri.common.io.ByteArrayDataOutputBehavior;
import ceri.common.io.ByteBufferStreamBehavior;
import ceri.common.io.FileFiltersTest;
import ceri.common.io.FileIteratorBehavior;
import ceri.common.io.FileTrackerBehavior;
import ceri.common.io.FilenameIteratorBehavior;
import ceri.common.io.InputStreamIteratorBehavior;
import ceri.common.io.IoTimeoutExceptionBehavior;
import ceri.common.io.IoUtilTest;
import ceri.common.io.PollingInputStreamBehavior;
import ceri.common.io.RegexFilenameFilterBehavior;
import ceri.common.io.RuntimeIoExceptionBehavior;
import ceri.common.io.StringPrintStreamBehavior;
import ceri.common.property.BasePropertiesBehavior;
import ceri.common.property.KeyBehavior;
import ceri.common.property.PropertyUtilTest;
import ceri.common.reflect.CallerBehavior;
import ceri.common.reflect.CreateExceptionBehavior;
import ceri.common.reflect.ReflectUtilTest;
import ceri.common.score.CollectionScorersTest;
import ceri.common.score.ScoreLookupBehavior;
import ceri.common.score.ScorersTest;
import ceri.common.test.DebuggerBehavior;
import ceri.common.test.RegexMatcherBehavior;
import ceri.common.test.TestPrinterBehavior;
import ceri.common.test.TestRunAdapterBehavior;
import ceri.common.test.TestRunPrinterBehavior;
import ceri.common.test.TestStateBehavior;
import ceri.common.test.TestThreadBehavior;
import ceri.common.test.TestTimerBehavior;
import ceri.common.test.TestUtil;
import ceri.common.test.TestUtilTest;
import ceri.common.text.RegexUtilTest;
import ceri.common.text.StringUtilTest;
import ceri.common.text.TextUtilTest;
import ceri.common.text.ToStringHelperBehavior;
import ceri.common.text.Utf8UtilTest;
import ceri.common.tree.NodeTreeBehavior;
import ceri.common.tree.TreeIteratorBehavior;
import ceri.common.tree.TreeNodeBehavior;
import ceri.common.tree.TreeNodeComparatorsTest;
import ceri.common.tree.TreeUtilTest;
import ceri.common.unit.InchUnitTest;
import ceri.common.unit.NormalizedValueBehavior;
import ceri.common.util.BasicUtilTest;
import ceri.common.util.EqualsUtilTest;
import ceri.common.util.HashCoderBehavior;
import ceri.common.util.KeyValueBehavior;
import ceri.common.util.LocaleUtilTest;
import ceri.common.util.MathUtilTest;
import ceri.common.util.MultiPatternBehavior;
import ceri.common.util.NameValueBehavior;
import ceri.common.util.OsUtilTest;
import ceri.common.util.PrimitiveUtilTest;
import ceri.common.zip.ZipUtilTest;
import ceri.common.zip.ZippingInputStreamBehavior;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// collection
	ArrayIteratorBehavior.class,
	ArrayUtilTest.class,
	CollectionUtilTest.class,
	DelegatingMapBehavior.class,
	FixedSizeCacheBehavior.class,
	ImmutableByteArrayBehavior.class,
	ImmutableUtilTest.class,
	MapBuilderBehavior.class,
	StreamUtilTest.class,
	// comparator
	ComparatorSequenceBehavior.class,
	ComparatorsTest.class,
	EnumComparatorsTest.class,
	// concurrent
	AsyncRunnerBehavior.class,
	BooleanConditionBehavior.class,
	ConcurrentUtilTest.class,
	RuntimeInterruptedExceptionBehavior.class,
	ValueConditionBehavior.class,
	// date
	CalendarFieldBehavior.class,
	ImmutableDateBehavior.class,
	ImmutableTimeZoneBehavior.class,
	MultiDateParserBehavior.class,
	TimeUnitBehavior.class,
	// email
	EmailUtilTest.class,
	// event
	EventTrackerBehavior.class,
	IntListenersBehavior.class,
	ListenersBehavior.class,
	//factory
	DateFactoriesTest.class,
	FactoriesTest.class,
	FactoryExceptionBehavior.class,
	NumberFactoriesTest.class,
	StringFactoriesTest.class,
	// filter
	CollectionFiltersTest.class,
	FiltersTest.class,
	// io
	BitIteratorBehavior.class,
	BlockingBufferStreamBehavior.class,
	ByteArrayDataInputBehavior.class,
	ByteArrayDataOutputBehavior.class,
	ByteBufferStreamBehavior.class,
	FileFiltersTest.class,
	FileIteratorBehavior.class,
	FilenameIteratorBehavior.class,
	FileTrackerBehavior.class,
	InputStreamIteratorBehavior.class,
	IoTimeoutExceptionBehavior.class,
	IoUtilTest.class,
	PollingInputStreamBehavior.class,
	RegexFilenameFilterBehavior.class,
	RuntimeIoExceptionBehavior.class,
	StringPrintStreamBehavior.class,
	// property
	BasePropertiesBehavior.class,
	KeyBehavior.class,
	PropertyUtilTest.class,
	// reflect
	CallerBehavior.class,
	CreateExceptionBehavior.class,
	ReflectUtilTest.class,
	// score
	CollectionScorersTest.class,
	ScoreLookupBehavior.class,
	ScorersTest.class,
	//test
	DebuggerBehavior.class,
	RegexMatcherBehavior.class,
	TestPrinterBehavior.class,
	TestRunAdapterBehavior.class,
	TestRunPrinterBehavior.class,
	TestStateBehavior.class,
	TestThreadBehavior.class,
	TestTimerBehavior.class,
	TestUtilTest.class,
	//text
	RegexUtilTest.class,
	StringUtilTest.class,
	TextUtilTest.class,
	ToStringHelperBehavior.class,
	Utf8UtilTest.class,
	// tree
	NodeTreeBehavior.class,
	TreeIteratorBehavior.class,
	TreeNodeBehavior.class,
	TreeNodeComparatorsTest.class,
	TreeUtilTest.class,
	// unit
	InchUnitTest.class,
	NormalizedValueBehavior.class,
	// util
	BasicUtilTest.class,
	EqualsUtilTest.class,
	HashCoderBehavior.class,
	KeyValueBehavior.class,
	LocaleUtilTest.class,
	MathUtilTest.class,
	MultiPatternBehavior.class,
	NameValueBehavior.class,
	OsUtilTest.class,
	PrimitiveUtilTest.class,
	// zip
	ZippingInputStreamBehavior.class,
	ZipUtilTest.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
