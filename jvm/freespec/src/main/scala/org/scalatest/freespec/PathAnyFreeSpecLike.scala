/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest.freespec

import org.scalatest._
import org.scalatest.exceptions._
import org.scalactic.{source, Prettifier}
import org.scalatest.verbs.BehaveWord
import Suite.autoTagClassAnnotations


/**
 * Implementation trait for class <code>PathAnyFreeSpec</code>, which is
 * a sister class to <code>org.scalatest.freespec.AnyFreeSpec</code> that isolates
 * tests by running each test in its own instance of the test class, and
 * for each test, only executing the <em>path</em> leading to that test.
 * 
 * <p>
 * <a href="PathAnyFreeSpec.html"><code>PathAnyFreeSpec</code></a> is a class, not a trait,
 * to minimize compile time given there is a slight compiler overhead to
 * mixing in traits compared to extending classes. If you need to mix the
 * behavior of <code>PathAnyFreeSpec</code> into some other class, you can use this
 * trait instead, because class <code>PathAnyFreeSpec</code> does nothing more than
 * extend this trait and add a nice <code>toString</code> implementation.
 * </p>
 *
 * <p>
 * See the documentation of the class for a <a href="PathAnyFreeSpec.html">detailed
 * overview of <code>PathAnyFreeSpec</code></a>.
 * </p>
 *
 * @author Bill Venners
 */
@Finders(Array("org.scalatest.finders.FreeSpecFinder"))
//SCALATESTJS-ONLY @scala.scalajs.reflect.annotation.EnableReflectiveInstantiation
//SCALATESTNATIVE-ONLY @scala.scalanative.reflect.annotation.EnableReflectiveInstantiation
trait PathAnyFreeSpecLike extends org.scalatest.Suite with OneInstancePerTest with Informing with Notifying with Alerting with Documenting { thisSuite =>
  
  private final val engine = PathEngine.getEngine()
  import engine._

  // SKIP-SCALATESTJS,NATIVE-START
  override def newInstance: org.scalatest.freespec.PathAnyFreeSpecLike = this.getClass.newInstance.asInstanceOf[PathAnyFreeSpecLike]
  // SKIP-SCALATESTJS,NATIVE-END
  //SCALATESTJS,NATIVE-ONLY override def newInstance: org.scalatest.freespec.PathAnyFreeSpecLike

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor (including within a test, since
   * those are invoked during construction in a <code>PathAnyFreeSpec</code>, it
   * will register the passed string for forwarding later when <code>run</code> is invoked. If invoked from inside a test function,
   * it will record the information and forward it to the current reporter only after the test completed, as <code>recordedEvents</code>
   * of the test completed event, such as <code>TestSucceeded</code>.  If invoked at any other time, it will print to the standard output.
   * This method can be called safely by any thread.
   */
  protected def info: Informer = atomicInformer.get

  /**
   * Returns a <code>Notifier</code> that during test execution will forward strings passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>Path.FreeSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * print to the standard output. This method can be called safely by any thread.
   */
  protected def note: Notifier = atomicNotifier.get

  /**
   * Returns an <code>Alerter</code> that during test execution will forward strings passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>PathAnyFreeSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * print to the standard output. This method can be called safely by any thread.
   */
  protected def alert: Alerter = atomicAlerter.get

  /**
   * Returns a <code>Documenter</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor (including within a test, since
   * those are invoked during construction in a <code>PathAnyFreeSpec</code>, it
   * will register the passed string for forwarding later when <code>run</code> is invoked. If invoked from inside a test function,
   * it will record the information and forward it to the current reporter only after the test completed, as <code>recordedEvents</code>
   * of the test completed event, such as <code>TestSucceeded</code>.  If invoked at any other time, it will print to the standard output.
   * This method can be called safely by any thread.
   */
  protected def markup: Documenter = atomicDocumenter.get

    /**
   * Register a test with the given spec text, optional tags, and test function value that takes no arguments.
   * An invocation of this method is called an &ldquo;example.&rdquo;
   *
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FreeSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param methodName caller's method name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullArgumentException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  private def registerTestToRun(specText: String, testTags: List[Tag], methodName: String, testFun: () => Unit /* Assertion */, pos: source.Position): Unit = {
    // SKIP-SCALATESTJS,NATIVE-START
    val stackDepth = 4
    val stackDepthAdjustment = -3
    // SKIP-SCALATESTJS,NATIVE-END
    //SCALATESTJS,NATIVE-ONLY val stackDepth = 6
    //SCALATESTJS,NATIVE-ONLY val stackDepthAdjustment = -5
    handleTest(thisSuite, specText, Transformer(testFun), Resources.itCannotAppearInsideAnotherIt, "FreeSpecLike.scala", methodName, stackDepth, stackDepthAdjustment, None, Some(pos), testTags: _*)
  }

  /**
   * Register a test to ignore, which has the given spec text, optional tags, and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documentation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FreeSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param methodName caller's method name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullArgumentException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  private def registerTestToIgnore(specText: String, testTags: List[Tag], methodName: String, testFun: () => Unit /* Assertion */, pos: source.Position): Unit = {
    // SKIP-SCALATESTJS,NATIVE-START
    val stackDepth = 4
    val stackDepthAdjustment = -3
    // SKIP-SCALATESTJS,NATIVE-END
    //SCALATESTJS,NATIVE-ONLY val stackDepth = 6
    //SCALATESTJS,NATIVE-ONLY val stackDepthAdjustment = -5
    handleIgnoredTest(specText, Transformer(testFun), Resources.ignoreCannotAppearInsideAnIt, "FreeSpecLike.scala", methodName, stackDepth, stackDepthAdjustment, None, Some(pos), testTags: _*)
  }

  /**
   * Class that supports the registration of tagged tests.
   *
   * <p>
   * Instances of this class are returned by the <code>taggedAs</code> method of 
   * class <code>FreeSpecStringWrapper</code>.
   * </p>
   *
   * @author Bill Venners
   */
  protected final class ResultOfTaggedAsInvocationOnString(specText: String, tags: List[Tag], pos: source.Position) {

    /**
     * Supports tagged test registration.
     *
     * <p>
     * For example, this method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "complain on peek" taggedAs(SlowTest) in { ... }
     *                                       ^
     * </pre>
     *
     * <p>
     * This trait's implementation of this method will decide whether to register the text (passed to the constructor
     * of <code>ResultOfTaggedAsInvocationOnString</code>) and invoke the passed function
     * based on whether or not this is part of the current "test path." For the details on this process, see
     * the <a href="#howItExecutes">How it executes</a> section of the main documentation for
     * trait <code>org.scalatest.path.FreeSpec</code>.
     * </p>
     */
    def in(testFun: => Unit /* Assertion */): Unit = {
      registerTestToRun(specText, tags, "in", () => testFun, pos)
    }

    /**
     * Supports registration of tagged, pending tests.
     *
     * <p>
     * For example, this method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "complain on peek" taggedAs(SlowTest) is (pending)
     *                                       ^
     * </pre>
     *
     * <p>
     * For more information and examples of this method's use, see the
     * <a href="AnyFreeSpec.html#pendingTests">Pending tests</a> section in the main documentation for
     * sister trait <code>org.scalatest.freespec.AnyFreeSpec</code>.
     * Note that this trait's implementation of this method will decide whether to register the text (passed to the constructor
     * of <code>ResultOfTaggedAsInvocationOnString</code>) and invoke the passed function
     * based on whether or not this is part of the current "test path." For the details on this process, see
     * the <a href="#howItExecutes">How it executes</a> section of the main documentation for
     * trait <code>org.scalatest.freespec.PathAnyFreeSpec</code>.
     * </p>
     */
    def is(testFun: => PendingStatement): Unit = {
      registerTestToRun(specText, tags, "is", () => testFun, pos)
    }

    /**
     * Supports registration of tagged, ignored tests.
     *
     * <p>
     * For example, this method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "complain on peek" taggedAs(SlowTest) ignore { ... }
     *                                       ^
     * </pre>
     *
     * <p>
     * For more information and examples of this method's use, see the
     * <a href="AnyFreeSpec.html#ignoredTests">Ignored tests</a> section in the main documentation for sister
     * trait <code>org.scalatest.freespec.AnyFreeSpec</code>. Note that a separate instance will be created for an ignored test,
     * and the path to the ignored test will be executed in that instance, but the test function itself will not
     * be executed. Instead, a <code>TestIgnored</code> event will be fired.
     * </p>
     */
    def ignore(testFun: => Unit /* Assertion */): Unit = {
      registerTestToIgnore(specText, tags, "ignore", () => testFun, pos)
    }
  }       

  /**
   * A class that via an implicit conversion (named <code>convertToFreeSpecStringWrapper</code>) enables
   * methods <code>in</code>, <code>is</code>, <code>taggedAs</code> and <code>ignore</code>,
   * as well as the dash operator (<code>-</code>), to be invoked on <code>String</code>s.
   *
   * @author Bill Venners
   */
  protected final class FreeSpecStringWrapper(string: String, pos: source.Position) {

    /**
     * Register some text that may surround one or more tests. The passed
     * passed function value may contain surrounding text registrations (defined with dash (<code>-</code>)) and/or tests
     * (defined with <code>in</code>). This class's implementation of this method will decide whether to
     * register the text (passed to the constructor of <code>FreeSpecStringWrapper</code>) and invoke the passed function
     * based on whether or not this is part of the current "test path." For the details on this process, see
     * the <a href="#howItExecutes">How it executes</a> section of the main documentation for trait
     * <code>org.scalatest.freespec.PathAnyFreeSpec</code>.
     */
    def -(fun: => Unit): Unit = {

      // SKIP-SCALATESTJS,NATIVE-START
      val stackDepth = 3
      // SKIP-SCALATESTJS,NATIVE-END
      //SCALATESTJS,NATIVE-ONLY val stackDepth = 5

      try {
        handleNestedBranch(string, None, fun, Resources.dashCannotAppearInsideAnIn, "FreeSpecLike.scala", "-", stackDepth, -2, None, Some(pos))
      }
      catch {
        case e: TestFailedException => throw new NotAllowedException(FailureMessages.assertionShouldBePutInsideInClauseNotDashClause, Some(e), e.position.getOrElse(pos))
        case e: TestCanceledException => throw new NotAllowedException(FailureMessages.assertionShouldBePutInsideInClauseNotDashClause, Some(e), e.position.getOrElse(pos))
        case tgce: TestRegistrationClosedException => throw tgce
        case e: DuplicateTestNameException => throw new NotAllowedException(FailureMessages.exceptionWasThrownInDashClause(Prettifier.default, UnquotedString(e.getClass.getName), string, e.getMessage), Some(e), e.position.getOrElse(pos))
        case other: Throwable if (!Suite.anExceptionThatShouldCauseAnAbort(other)) => throw new NotAllowedException(FailureMessages.exceptionWasThrownInDashClause(Prettifier.default, UnquotedString(other.getClass.getName), string, other.getMessage), Some(other), pos)
        case other: Throwable => throw other
      }
    }

    /**
     * Supports test registration.
     *
     * <p>
     * For example, this method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "complain on peek" in { ... }
     *                    ^
     * </pre>
     *
     * <p>
     * This trait's implementation of this method will decide whether to register the text (passed to the constructor
     * of <code>FreeSpecStringWrapper</code>) and invoke the passed function
     * based on whether or not this is part of the current "test path." For the details on this process, see
     * the <a href="#howItExecutes">How it executes</a> section of the main documentation for
     * trait <code>org.scalatest.freespec.PathAnyFreeSpec</code>.
     * </p>
     */
    def in(f: => Unit /* Assertion */): Unit = {
      registerTestToRun(string, List(), "in", () => f, pos)
    }

    /**
     * Supports ignored test registration.
     *
     * <p>
     * For example, this method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "complain on peek" ignore { ... }
     *                    ^
     * </pre>
     *
     * <p>
     * For more information and examples of this method's use, see the
     * <a href="AnyFreeSpec.html#ignoredTests">Ignored tests</a> section in the main documentation for sister
     * trait <code>org.scalatest.freespec.AnyFreeSpec</code>. Note that a separate instance will be created for an ignored test,
     * and the path to the ignored test will be executed in that instance, but the test function itself will not
     * be executed. Instead, a <code>TestIgnored</code> event will be fired.
     * </p>
     */
    def ignore(f: => Unit /* Assertion */): Unit = {
      registerTestToIgnore(string, List(), "ignore", () => f, pos)
    }

    /**
     * Supports pending test registration.
     *
     * <p>
     * For example, this method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "complain on peek" is (pending)
     *                    ^
     * </pre>
     *
     * <p>
     * For more information and examples of this method's use, see the
     * <a href="AnyFreeSpec.html#pendingTests">Pending tests</a> section in the main documentation for
     * sister trait <code>org.scalatest.freespec.AnyFreeSpec</code>.
     * Note that this trait's implementation of this method will decide whether to register the text (passed to the constructor
     * of <code>FreeSpecStringWrapper</code>) and invoke the passed function
     * based on whether or not this is part of the current "test path." For the details on this process, see
     * the <a href="#howItExecutes">How it executes</a> section of the main documentation for
     * trait <code>org.scalatest.freespec.PathAnyFreeSpec</code>.
     * </p>
     */
    def is(f: => PendingStatement): Unit = {
      registerTestToRun(string, List(), "is", () => f, pos)
    }

    /**
     * Supports tagged test registration.
     *
     * <p>
     * For example, this method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "complain on peek" taggedAs(SlowTest) in { ... }
     *                    ^
     * </pre>
     *
     * <p>
     * For more information and examples of this method's use, see the
     * <a href="AnyFreeSpec.html#taggingTests">Tagging tests</a> section in the main documentation for sister
     * trait <code>org.scalatest.freespec.AnyFreeSpec</code>.
     * </p>
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*): ResultOfTaggedAsInvocationOnString = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ResultOfTaggedAsInvocationOnString(string, tagList, pos)
    }
  }

  import scala.language.implicitConversions

  /**
   * Implicitly converts <code>String</code>s to <code>FreeSpecStringWrapper</code>, which enables
   * methods <code>in</code>, <code>is</code>, <code>taggedAs</code> and <code>ignore</code>,
   * as well as the dash operator (<code>-</code>), to be invoked on <code>String</code>s.
   */
  // SKIP-DOTTY-START
  protected implicit def convertToFreeSpecStringWrapper(s: String)(implicit pos: source.Position): FreeSpecStringWrapper = new FreeSpecStringWrapper(s, pos)
  // SKIP-DOTTY-END
  //DOTTY-ONLY inline implicit def convertToFreeSpecStringWrapper(s: String): FreeSpecStringWrapper = {
  //DOTTY-ONLY   ${ source.Position.withPosition[FreeSpecStringWrapper]('{(pos: source.Position) => new FreeSpecStringWrapper(s, pos) }) } 
  //DOTTY-ONLY }

  /**
   * Supports shared test registration in <code>PathAnyFreeSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * behave like nonFullStack(stackWithOneItem)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of <cod>behave</code>, see the
   * <a href="PathAnyFreeSpec.html#SharedTests">Shared tests section</a> in the main documentation for sister
   * trait <code>org.scalatest.freespec.PathAnyFreeSpec</code>.
   * </p>
   */
  protected val behave = new BehaveWord

  /**
   * An immutable <code>Set</code> of test names. If this <code>PathAnyFreeSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space. For example, consider this <code>PathAnyFreeSpec</code>:
   * </p>
   *
   * <pre class="stHighlight">
   * import org.scalatest.freespec
   *
   * class StackSpec extends freespec.PathAnyFreeSpec {
   *   "A Stack" - {
   *     "when not empty" - {
   *       "must allow me to pop" in {}
   *     }
   *     "when not full" - {
   *       "must allow me to push" in {}
   *     }
   *   }
   * }
   * </pre>
   *
   * <p>
   * Invoking <code>testNames</code> on this <code>FreeSpec</code> will yield a set that contains the following
   * two test name strings:
   * </p>
   *
   * <pre>
   * "A Stack when not empty must allow me to pop"
   * "A Stack when not full must allow me to push"
   * </pre>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final override def testNames: Set[String] = {
    ensureTestResultsRegistered(thisSuite)
    InsertionOrderSet(atomic.get.testNamesList)
  }

  /**
   * The total number of tests that are expected to run when this <code>PathAnyFreeSpec</code>'s <code>run</code> method
   * is invoked.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation of this method returns the size of the <code>testNames</code> <code>List</code>, minus
   * the number of tests marked as ignored as well as any tests excluded by the passed <code>Filter</code>.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   *
   * @param filter a <code>Filter</code> with which to filter tests to count based on their tags
   */
  final override def expectedTestCount(filter: Filter): Int = {
    ensureTestResultsRegistered(thisSuite)
    super.expectedTestCount(filter)
  }

  /**
   * Runs a test.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation reports the test results registered with the name specified by
   * <code>testName</code>. Each test's name is a concatenation of the text of all describers surrounding a test,
   * from outside in, and the test's  spec text, with one space placed between each item. (See the documentation
   * for <code>testNames</code> for an example.)
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   *
   * @param testName the name of one test to execute.
   * @param args the <code>Args</code> for this run
   *
   * @throws NullArgumentException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>configMap</code>
   *     is <code>null</code>.
   */
  final protected override def runTest(testName: String, args: Args): Status = {

    ensureTestResultsRegistered(thisSuite)
    
    def dontInvokeWithFixture(theTest: TestLeaf): Outcome = {
      theTest.testFun()
    }

    runTestImpl(thisSuite, testName, args, true, dontInvokeWithFixture)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>PathAnyFreeSpec</code>
   * belong, and values the <code>Set</code> of test names that belong to each tag. If this <code>PathAnyFreeSpec</code>
   * contains no tags, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed
   * to methods <code>test</code> and <code>ignore</code>.
   * </p>
   * 
   * <p>
   * In addition, this trait's implementation will also auto-tag tests with class level annotations.  
   * For example, if you annotate @Ignore at the class level, all test methods in the class will be auto-annotated with @Ignore.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final override def tags: Map[String, Set[String]] = {
    ensureTestResultsRegistered(thisSuite)
    autoTagClassAnnotations(atomic.get.tagsMap, this)
  }

  /**
   * Runs this <code>path.FreeSpec</code>, reporting test results that were registered when the tests
   * were run, each during the construction of its own instance.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * will report the registered results for all tests except any excluded by the passed <code>Filter</code>.
   * If <code>testName</code> is defined, it will report the results of only that named test. Because a
   * <code>path.FreeSpec</code> is not allowed to contain nested suites, this trait's implementation of
   * this method does not call <code>runNestedSuites</code>.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   *
   * @param testName an optional name of one test to run. If <code>None</code>, all relevant tests should be run.
   *                 I.e., <code>None</code> acts like a wildcard that means run all relevant tests in this <code>Suite</code>.
   * @param args the <code>Args</code> for this run
   *
   * @throws NullArgumentException if any passed parameter is <code>null</code>.
   * @throws IllegalArgumentException if <code>testName</code> is defined, but no test with the specified test name
   *     exists in this <code>Suite</code>
   */
  final override def run(testName: Option[String], args: Args): Status = {
    // TODO enforce those throws specs

    ensureTestResultsRegistered(thisSuite)
    runPathTestsImpl(thisSuite, testName, args, info, true, runTest)
  }

  /**
   * This lifecycle method is unused by this trait, and will complete abruptly with
   * <code>UnsupportedOperationException</code> if invoked.
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final protected override def runTests(testName: Option[String], args: Args): Status = {
    throw new UnsupportedOperationException
  }

  /**
   * This lifecycle method is unused by this trait, and is implemented to do nothing. If invoked, it will
   * just return immediately.
   *
   * <p>
   * Nested suites are not allowed in a <code>PathAnyFreeSpec</code>. Because
   * a <code>PathAnyFreeSpec</code> executes tests eagerly at construction time, registering the results of
   * those test runs and reporting them later, the order of nested suites versus test runs would be different
   * in a <code>org.scalatest.freespec.PathAnyFreeSpec</code> than in an <code>org.scalatest.freespec.AnyFreeSpec</code>. In an
   * <code>org.scalatest.freespec.AnyFreeSpec</code>, nested suites are executed then tests are executed. In an
   * <code>org.scalatest.freespec.PathAnyFreeSpec</code> it would be the opposite. To make the code easy to reason about,
   * therefore, this is just not allowed. If you want to add nested suites to a <code>path.FreeSpec</code>, you can
   * instead wrap them all in a <a href="../Suites.html"><code>Suites</code></a> 
   * object and put them in whatever order you wish.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final protected override def runNestedSuites(args: Args): Status = SucceededStatus

  /**
   * Returns an empty list.
   *
   * <p>
   * This lifecycle method is unused by this trait. If invoked, it will return an empty list, because
   * nested suites are not allowed in a <code>PathAnyFreeSpec</code>. Because
   * a <code>PathAnyFreeSpec</code> executes tests eagerly at construction time, registering the results of
   * those test runs and reporting them later, the order of nested suites versus test runs would be different
   * in a <code>org.scalatest.freespec.PathAnyFreeSpec</code> than in an <code>org.scalatest.freespec.AnyFreeSpec</code>. In an
   * <code>org.scalatest.freespec.AnyFreeSpec</code>, nested suites are executed then tests are executed. In an
   * <code>org.scalatest.freespec.PathAnyFreeSpec</code> it would be the opposite. To make the code easy to reason about,
   * therefore, this is just not allowed. If you want to add nested suites to a <code>PathAnyFreeSpec</code>, you can
   * instead wrap them all in a <a href="../Suites.html"><code>Suites</code></a> 
   * object and put them in whatever order you wish.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final override def nestedSuites: collection.immutable.IndexedSeq[Suite] = Vector.empty
  
  /**
   * <strong>The <code>styleName</code> lifecycle method has been deprecated and will be removed in a future version of ScalaTest.</strong>
   *
   * <p>This method was used to support the chosen styles feature, which was deactivated in 3.1.0. The internal modularization of ScalaTest in 3.2.0
   * will replace chosen styles as the tool to encourage consistency across a project. We do not plan a replacement for <code>styleName</code>.</p>
   */
  @deprecated("The styleName lifecycle method has been deprecated and will be removed in a future version of ScalaTest with no replacement.", "3.1.0")
  final override val styleName: String = "org.scalatest.path.FreeSpec"
    
  override def testDataFor(testName: String, theConfigMap: ConfigMap = ConfigMap.empty): TestData = {
    ensureTestResultsRegistered(thisSuite)
    createTestDataFor(testName, theConfigMap, this)
  }
}

