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
package org.scalatest.wordspec

// elements
import org.scalatest._
import org.scalatest.SharedHelpers._
import org.scalatest.events._
import org.scalactic.Prettifier
import java.awt.AWTError
import java.lang.annotation.AnnotationFormatError
import java.nio.charset.CoderMalfunctionError
import javax.xml.parsers.FactoryConfigurationError
import javax.xml.transform.TransformerFactoryConfigurationError
import org.scalactic.exceptions.NullArgumentException
import org.scalatest.exceptions.DuplicateTestNameException
import org.scalatest.exceptions.NotAllowedException
import org.scalatest.exceptions.TestCanceledException
import org.scalatest.exceptions.TestFailedException
import org.scalatest.exceptions.TestRegistrationClosedException
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.wordspec.AnyWordSpec

class AnyWordSpecSpec extends AnyFunSpec with GivenWhenThen {

  private val prettifier = Prettifier.default

  describe("A WordSpec") {

    it("should invoke withFixture from runTest") {
      class SpecA extends AnyWordSpec {
        var withFixtureWasInvoked = false
        var testWasInvoked = false
        override def withFixture(test: NoArgTest): Outcome = {
          withFixtureWasInvoked = true
          super.withFixture(test)
        }
        "do something" in {
          testWasInvoked = true
          /* ASSERTION_SUCCEED */
        }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      a.run(None, Args(SilentReporter))
      assert(a.withFixtureWasInvoked)
      assert(a.testWasInvoked)
    }
    it("should pass the correct test name in the NoArgTest passed to withFixture") {
      class SpecA extends AnyWordSpec {
        var correctTestNameWasPassed = false
        override def withFixture(test: NoArgTest): Outcome = {
          correctTestNameWasPassed = test.name == "do something"
          super.withFixture(test)
        }
        "do something" in {/* ASSERTION_SUCCEED */}
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      a.run(None, Args(SilentReporter))
      assert(a.correctTestNameWasPassed)
    }
    it("should pass the correct config map in the NoArgTest passed to withFixture") {
      class SpecA extends AnyWordSpec {
        var correctConfigMapWasPassed = false
        override def withFixture(test: NoArgTest): Outcome = {
          correctConfigMapWasPassed = (test.configMap == ConfigMap("hi" -> 7))
          super.withFixture(test)
        }
        "do something" in {/* ASSERTION_SUCCEED */}
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      a.run(None, Args(SilentReporter, Stopper.default, Filter(), ConfigMap("hi" -> 7), None, new Tracker(), Set.empty))
      assert(a.correctConfigMapWasPassed)
    }

    describe("(when a nesting rule has been violated)") {

      it("should, if they call a should from within an in clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" should {
              //DOTTY-ONLY ()
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"should\" clause may not appear inside an \"in\" clause")
      }
      it("should, if they call a should with a nested in from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" should {
              "should never run" in {
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"should\" clause may not appear inside an \"in\" clause")
      }

      it("should, if they call a when from within an in clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" when {
              //DOTTY-ONLY ()
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"when\" clause may not appear inside an \"in\" clause")
      }
      it("should, if they call a when with a nested in from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" when {
              "should never run" in {
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"when\" clause may not appear inside an \"in\" clause")
      }

      it("should, if they call a that from within an in clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" that {
              //DOTTY-ONLY ()
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"that\" clause may not appear inside an \"in\" clause")
      }
      it("should, if they call a that with a nested in from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" that {
              "should never run" in {
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"that\" clause may not appear inside an \"in\" clause")
      }

      it("should, if they call a which from within an in clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" which {
              //DOTTY-ONLY ()
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"which\" clause may not appear inside an \"in\" clause")
      }
      it("should, if they call a which with a nested in from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" which {
              "should never run" in {
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"which\" clause may not appear inside an \"in\" clause")
      }

      it("should, if they call a can from within an in clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" can {
              //DOTTY-ONLY ()
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"can\" clause may not appear inside an \"in\" clause")
      }
      it("should, if they call a can with a nested in from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" can {
              "should never run" in {
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceivedWithCorrectMessage(spec, "should blow up", "a \"can\" clause may not appear inside an \"in\" clause")
      }

      it("should, if they call a nested it from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "should never run" in {
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested it with tags from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "should never run" taggedAs(mytags.SlowAsMolasses) in {
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested registerTest with tags from within a registerTest clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          registerTest("should blow up") {
            registerTest("should never run", mytags.SlowAsMolasses) {
              assert(1 == 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a describe with a nested ignore from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" should {
              "should never run" ignore {
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested ignore from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "should never run" ignore {
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested ignore with tags from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          "should blow up" in {
            "should never run" taggedAs(mytags.SlowAsMolasses) ignore {
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested registerIgnoredTest with tags from within a registerTest clause, result in a TestFailedException when running the test") {

        class MySpec extends AnyWordSpec {
          registerTest("should blow up") {
            registerIgnoredTest("should never run", mytags.SlowAsMolasses) {
              assert(1 == 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
    }

    it("should return the test names in registration order from testNames") {

      val a = new AnyWordSpec {
        "it should test this" in {/* ASSERTION_SUCCEED */}
        "it should test that" in {/* ASSERTION_SUCCEED */}
      }

      assertResult(List("it should test this", "it should test that")) {
        a.testNames.iterator.toList
      }

      val b = new AnyWordSpec {}

      assertResult(List[String]()) {
        b.testNames.iterator.toList
      }

      val c = new AnyWordSpec {
        "it should test that" in {/* ASSERTION_SUCCEED */}
        "it should test this" in {/* ASSERTION_SUCCEED */}
      }

      assertResult(List("it should test that", "it should test this")) {
        c.testNames.iterator.toList
      }

      val d = new AnyWordSpec {
        "A Tester" should {
          "test that" in {/* ASSERTION_SUCCEED */}
          "test this" in {/* ASSERTION_SUCCEED */}
        }
      }

      assertResult(List("A Tester should test that", "A Tester should test this")) {
        d.testNames.iterator.toList
      }

      val e = new AnyWordSpec {
        "A Tester" should {
          "test this" in {/* ASSERTION_SUCCEED */}
          "test that" in {/* ASSERTION_SUCCEED */}
        }
      }

      assertResult(List("A Tester should test this", "A Tester should test that")) {
        e.testNames.iterator.toList
      }
    }

    it("should throw DuplicateTestNameException if a duplicate test name registration is attempted") {
      
      intercept[DuplicateTestNameException] {
        new AnyWordSpec {
          "should test this" in {/* ASSERTION_SUCCEED */}
          "should test this" in {/* ASSERTION_SUCCEED */}
        }
      }
      intercept[DuplicateTestNameException] {
        new AnyWordSpec {
          "should test this" in {/* ASSERTION_SUCCEED */}
          "should test this" ignore {/* ASSERTION_SUCCEED */}
        }
      }
      intercept[DuplicateTestNameException] {
        new AnyWordSpec {
          "should test this" ignore {/* ASSERTION_SUCCEED */}
          "should test this" ignore {/* ASSERTION_SUCCEED */}
        }
      }
      intercept[DuplicateTestNameException] {
        new AnyWordSpec {
          "should test this" ignore {/* ASSERTION_SUCCEED */}
          "should test this" in {/* ASSERTION_SUCCEED */}
        }
      }
    }

    describe("(with info calls)") {
      class InfoInsideTestSpec extends AnyWordSpec {
        val msg = "hi there, dude"
        val testName = "test name"
        testName in {
          info(msg)
          /* ASSERTION_SUCCEED */
        }
      }
      // In a Spec, any InfoProvided's fired during the test should be cached and sent out after the test has
      // suceeded or failed. This makes the report look nicer, because the info is tucked under the "specifier'
      // text for that test.
      it("should, when the info appears in the code of a successful test, report the info in the TestSucceeded") {
        val spec = new InfoInsideTestSpec
        val (testStartingIndex, testSucceededIndex) =
          getIndexesForTestInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(testStartingIndex < testSucceededIndex)
      }
      class InfoBeforeTestSpec extends AnyWordSpec {
        val msg = "hi there, dude"
        val testName = "test name"
        info(msg)
        testName in {/* ASSERTION_SUCCEED */}
      }
      it("should, when the info appears in the body before a test, report the info before the test") {
        val spec = new InfoBeforeTestSpec
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(infoProvidedIndex < testStartingIndex)
        assert(testStartingIndex < testSucceededIndex)
      }
      it("should, when the info appears in the body after a test, report the info after the test runs") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySpec extends AnyWordSpec {
          testName in {/* ASSERTION_SUCCEED */}
          info(msg)
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(new MySpec, testName, msg)
        assert(testStartingIndex < testSucceededIndex)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      it("should print to stdout when info is called by a method invoked after the suite has been executed") {
        class MySpec extends AnyWordSpec {
          callInfo() // This should work fine
          def callInfo(): Unit = {
            info("howdy")
          }
          "howdy also" in {
            callInfo() // This should work fine
            /* ASSERTION_SUCCEED */
          }
        }
        val spec = new MySpec
        val myRep = new EventRecordingReporter
        spec.run(None, Args(myRep))
        spec.callInfo() // TODO: Actually test that This prints to stdout
      }
      it("should send an InfoProvided with an IndentedText formatter with level 1 when called outside a test") {
        val spec = new InfoBeforeTestSpec
        val indentedText = getIndentedTextFromInfoProvided(spec)
        assert(indentedText === IndentedText("+ " + spec.msg, spec.msg, 0))
      }
      it("should send an InfoProvided with an IndentedText formatter with level 2 when called within a test") {
        val spec = new InfoInsideTestSpec
        val indentedText = getIndentedTextFromTestInfoProvided(spec)
        assert(indentedText === IndentedText("  + " + spec.msg, spec.msg, 1))
      }
    }
    it("should throw NullArgumentException if a null test tag is provided") {
      // it
      intercept[NullArgumentException] {
        new AnyWordSpec {
          "hi" taggedAs(null) in {/* ASSERTION_SUCCEED */}
        }
      }
      val caught = intercept[NullArgumentException] {
        new AnyWordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null) in {/* ASSERTION_SUCCEED */}
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullArgumentException] {
        new AnyWordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) in {/* ASSERTION_SUCCEED */}
        }
      }

      // ignore
      intercept[NullArgumentException] {
        new AnyWordSpec {
          "hi" taggedAs(null) ignore {/* ASSERTION_SUCCEED */}
        }
      }
      val caught2 = intercept[NullArgumentException] {
        new AnyWordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null) ignore {/* ASSERTION_SUCCEED */}
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullArgumentException] {
        new AnyWordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) ignore {/* ASSERTION_SUCCEED */}
        }
      }

      // registerTest
      intercept[NullArgumentException] {
        new AnyWordSpec {
          registerTest("hi", null) {/* ASSERTION_SUCCEED */}
        }
      }
      val caught3 = intercept[NullArgumentException] {
        new AnyWordSpec {
          registerTest("hi", mytags.SlowAsMolasses, null) {/* ASSERTION_SUCCEED */}
        }
      }
      assert(caught3.getMessage == "a test tag was null")
      intercept[NullArgumentException] {
        new AnyWordSpec {
          registerTest("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {/* ASSERTION_SUCCEED */}
        }
      }

      // registerIgnoredTest
      intercept[NullArgumentException] {
        new AnyWordSpec {
          registerIgnoredTest("hi", null) {/* ASSERTION_SUCCEED */}
        }
      }
      val caught4 = intercept[NullArgumentException] {
        new AnyWordSpec {
          registerIgnoredTest("hi", mytags.SlowAsMolasses, null) {/* ASSERTION_SUCCEED */}
        }
      }
      assert(caught4.getMessage == "a test tag was null")
      intercept[NullArgumentException] {
        new AnyWordSpec {
          registerIgnoredTest("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {/* ASSERTION_SUCCEED */}
        }
      }
    }
    it("should return a correct tags map from the tags method using is (pending)") {

      val a = new AnyWordSpec {
        "test this" ignore {/* ASSERTION_SUCCEED */}
        "test that" is (pending)
      }
      assertResult(Map("test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new AnyWordSpec {
        "test this" is (pending)
        "test that" ignore {/* ASSERTION_SUCCEED */}
      }
      assertResult(Map("test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new AnyWordSpec {
        "test this" ignore {/* ASSERTION_SUCCEED */}
        "test that" ignore {/* ASSERTION_SUCCEED */}
      }
      assertResult(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new AnyWordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
        "test that" taggedAs(mytags.SlowAsMolasses) ignore {/* ASSERTION_SUCCEED */}
      }
      assertResult(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new AnyWordSpec {
        "test this" is (pending)
        "test that" is (pending)
      }
      assertResult(Map()) {
        e.tags
      }

      val f = new AnyWordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        "test that" taggedAs(mytags.SlowAsMolasses) in  {/* ASSERTION_SUCCEED */}
      }
      assertResult(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new AnyWordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        "test that" taggedAs(mytags.SlowAsMolasses) in  {/* ASSERTION_SUCCEED */}
      }
      assertResult(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }

    class TestWasCalledSuite extends AnyWordSpec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      "run this" in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
      "run that, maybe" in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
    }

    it("should execute all tests when run is called with testName None") {

      val b = new TestWasCalledSuite
      b.run(None, Args(SilentReporter))
      assert(b.theTestThisCalled)
      assert(b.theTestThatCalled)
    }

    it("should execute one test when run is called with a defined testName") {

      val a = new TestWasCalledSuite
      a.run(Some("run this"), Args(SilentReporter))
      assert(a.theTestThisCalled)
      assert(!a.theTestThatCalled)
    }

    it("should report as ignored, and not run, tests marked ignored") {

      class SpecA extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      class SpecB extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" ignore { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val b = new SpecB

      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB))
      assert(repB.testIgnoredReceived)
      assert(repB.lastEvent.isDefined)
      assert(repB.lastEvent.get.testName endsWith "test this")
      assert(!b.theTestThisCalled)
      assert(b.theTestThatCalled)

      class SpecC extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" ignore { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val c = new SpecC

      val repC = new TestIgnoredTrackingReporter
      c.run(None, Args(repC))
      assert(repC.testIgnoredReceived)
      assert(repC.lastEvent.isDefined)
      assert(repC.lastEvent.get.testName endsWith "test that", repC.lastEvent.get.testName)
      assert(c.theTestThisCalled)
      assert(!c.theTestThatCalled)

      // The order I want is order of appearance in the file.
      // Will try and implement that tomorrow. Subtypes will be able to change the order.
      class SpecD extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" ignore { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" ignore { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val d = new SpecD

      val repD = new TestIgnoredTrackingReporter
      d.run(None, Args(repD))
      assert(repD.testIgnoredReceived)
      assert(repD.lastEvent.isDefined)
      assert(repD.lastEvent.get.testName endsWith "test that") // last because should be in order of appearance
      assert(!d.theTestThisCalled)
      assert(!d.theTestThatCalled)
    }

    it("should ignore a test marked as ignored if run is invoked with that testName") {
      // If I provide a specific testName to run, then it should ignore an Ignore on that test
      // method and actually invoke it.
      class SpecE extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" ignore { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val e = new SpecE

      import scala.language.reflectiveCalls

      val repE = new TestIgnoredTrackingReporter
      e.run(Some("test this"), Args(repE))
      assert(repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(!e.theTestThatCalled)
    }

    it("should run only those tests selected by the tags to include and exclude sets") {

      // Nothing is excluded
      class SpecA extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses) in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      // SlowAsMolasses is included, one test should be excluded
      class SpecB extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses) in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val b = new SpecB
      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repB.testIgnoredReceived)
      assert(b.theTestThisCalled)
      assert(!b.theTestThatCalled)

      // SlowAsMolasses is included, and both tests should be included
      class SpecC extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses) in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val c = new SpecC
      val repC = new TestIgnoredTrackingReporter
      c.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repC.testIgnoredReceived)
      assert(c.theTestThisCalled)
      assert(c.theTestThatCalled)

      // SlowAsMolasses is included. both tests should be included but one ignored
      class SpecD extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses) ignore { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val d = new SpecD
      val repD = new TestIgnoredTrackingReporter
      d.run(None, Args(repD, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repD.testIgnoredReceived)
      assert(!d.theTestThisCalled)
      assert(d.theTestThatCalled)

      // SlowAsMolasses included, FastAsLight excluded
      class SpecE extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.FastAsLight) in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        "test the other" in { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val e = new SpecE
      val repE = new TestIgnoredTrackingReporter
      e.run(None, Args(repE, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(e.theTestThatCalled)
      assert(!e.theTestTheOtherCalled)

      // An Ignored test that was both included and excluded should not generate a TestIgnored event
      class SpecF extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.FastAsLight) ignore { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        "test the other" in { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val f = new SpecF
      val repF = new TestIgnoredTrackingReporter
      f.run(None, Args(repF, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repF.testIgnoredReceived)
      assert(!f.theTestThisCalled)
      assert(f.theTestThatCalled)
      assert(!f.theTestTheOtherCalled)

      // An Ignored test that was not included should not generate a TestIgnored event
      class SpecG extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.FastAsLight) in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        "test the other" ignore { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val g = new SpecG
      val repG = new TestIgnoredTrackingReporter
      g.run(None, Args(repG, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repG.testIgnoredReceived)
      assert(!g.theTestThisCalled)
      assert(g.theTestThatCalled)
      assert(!g.theTestTheOtherCalled)

      // No tagsToInclude set, FastAsLight excluded
      class SpecH extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.FastAsLight) in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        "test the other" in { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val h = new SpecH
      val repH = new TestIgnoredTrackingReporter
      h.run(None, Args(repH, Stopper.default, Filter(None, Set("org.scalatest.FastAsLight")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repH.testIgnoredReceived)
      assert(!h.theTestThisCalled)
      assert(h.theTestThatCalled)
      assert(h.theTestTheOtherCalled)

      // No tagsToInclude set, mytags.SlowAsMolasses excluded
      class SpecI extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.FastAsLight) in { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) in { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        "test the other" in { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val i = new SpecI
      val repI = new TestIgnoredTrackingReporter
      i.run(None, Args(repI, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!i.theTestThisCalled)
      assert(!i.theTestThatCalled)
      assert(i.theTestTheOtherCalled)

      // No tagsToInclude set, mytags.SlowAsMolasses excluded, TestIgnored should not be received on excluded ones
      class SpecJ extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.FastAsLight) ignore { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) ignore { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        "test the other" in { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val j = new SpecJ
      val repJ = new TestIgnoredTrackingReporter
      j.run(None, Args(repJ, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!j.theTestThisCalled)
      assert(!j.theTestThatCalled)
      assert(j.theTestTheOtherCalled)

      // Same as previous, except Ignore specifically mentioned in excludes set
      class SpecK extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.FastAsLight) ignore { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        "test that" taggedAs(mytags.SlowAsMolasses) ignore { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        "test the other" ignore { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val k = new SpecK
      val repK = new TestIgnoredTrackingReporter
      k.run(None, Args(repK, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repK.testIgnoredReceived)
      assert(!k.theTestThisCalled)
      assert(!k.theTestThatCalled)
      assert(!k.theTestTheOtherCalled)
    }

    it("should run only those registered tests selected by the tags to include and exclude sets") {

      // Nothing is excluded
      class SpecA extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerTest("test this", mytags.SlowAsMolasses) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that") { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      // SlowAsMolasses is included, one test should be excluded
      class SpecB extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerTest("test this", mytags.SlowAsMolasses) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that") { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val b = new SpecB
      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repB.testIgnoredReceived)
      assert(b.theTestThisCalled)
      assert(!b.theTestThatCalled)

      // SlowAsMolasses is included, and both tests should be included
      class SpecC extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerTest("test this", mytags.SlowAsMolasses) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val c = new SpecC
      val repC = new TestIgnoredTrackingReporter
      c.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repC.testIgnoredReceived)
      assert(c.theTestThisCalled)
      assert(c.theTestThatCalled)

      // SlowAsMolasses is included. both tests should be included but one ignored
      class SpecD extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val d = new SpecD
      val repD = new TestIgnoredTrackingReporter
      d.run(None, Args(repD, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repD.testIgnoredReceived)
      assert(!d.theTestThisCalled)
      assert(d.theTestThatCalled)

      // SlowAsMolasses included, FastAsLight excluded
      class SpecE extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val e = new SpecE
      val repE = new TestIgnoredTrackingReporter
      e.run(None, Args(repE, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
        ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(e.theTestThatCalled)
      assert(!e.theTestTheOtherCalled)

      // An Ignored test that was both included and excluded should not generate a TestIgnored event
      class SpecF extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val f = new SpecF
      val repF = new TestIgnoredTrackingReporter
      f.run(None, Args(repF, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
        ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repF.testIgnoredReceived)
      assert(!f.theTestThisCalled)
      assert(f.theTestThatCalled)
      assert(!f.theTestTheOtherCalled)

      // An Ignored test that was not included should not generate a TestIgnored event
      class SpecG extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test the other") { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val g = new SpecG
      val repG = new TestIgnoredTrackingReporter
      g.run(None, Args(repG, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
        ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repG.testIgnoredReceived)
      assert(!g.theTestThisCalled)
      assert(g.theTestThatCalled)
      assert(!g.theTestTheOtherCalled)

      // No tagsToInclude set, FastAsLight excluded
      class SpecH extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val h = new SpecH
      val repH = new TestIgnoredTrackingReporter
      h.run(None, Args(repH, Stopper.default, Filter(None, Set("org.scalatest.FastAsLight")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repH.testIgnoredReceived)
      assert(!h.theTestThisCalled)
      assert(h.theTestThatCalled)
      assert(h.theTestTheOtherCalled)

      // No tagsToInclude set, mytags.SlowAsMolasses excluded
      class SpecI extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val i = new SpecI
      val repI = new TestIgnoredTrackingReporter
      i.run(None, Args(repI, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!i.theTestThisCalled)
      assert(!i.theTestThatCalled)
      assert(i.theTestTheOtherCalled)

      // No tagsToInclude set, mytags.SlowAsMolasses excluded, TestIgnored should not be received on excluded ones
      class SpecJ extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val j = new SpecJ
      val repJ = new TestIgnoredTrackingReporter
      j.run(None, Args(repJ, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!j.theTestThisCalled)
      assert(!j.theTestThatCalled)
      assert(j.theTestTheOtherCalled)

      // Same as previous, except Ignore specifically mentioned in excludes set
      class SpecK extends AnyWordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test that", mytags.SlowAsMolasses) { theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test the other") { theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val k = new SpecK
      val repK = new TestIgnoredTrackingReporter
      k.run(None, Args(repK, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repK.testIgnoredReceived)
      assert(!k.theTestThisCalled)
      assert(!k.theTestThatCalled)
      assert(!k.theTestTheOtherCalled)
    }

    it("should return the correct test count from its expectedTestCount method") {

      val a = new AnyWordSpec {
        "test this" in {/* ASSERTION_SUCCEED */}
        "test that" in {/* ASSERTION_SUCCEED */}
      }
      assert(a.expectedTestCount(Filter()) === 2)

      val b = new AnyWordSpec {
        "test this" ignore {/* ASSERTION_SUCCEED */}
        "test that" in {/* ASSERTION_SUCCEED */}
      }
      assert(b.expectedTestCount(Filter()) === 1)

      val c = new AnyWordSpec {
        "test this" taggedAs(mytags.FastAsLight) in {/* ASSERTION_SUCCEED */}
        "test that" in {/* ASSERTION_SUCCEED */}
      }
      assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) === 1)

      val d = new AnyWordSpec {
        "test this" taggedAs(mytags.FastAsLight, mytags.SlowAsMolasses) in {/* ASSERTION_SUCCEED */}
        "test that" taggedAs(mytags.SlowAsMolasses) in {/* ASSERTION_SUCCEED */}
        "test the other thing" in {/* ASSERTION_SUCCEED */}
      }
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 1)
      assert(d.expectedTestCount(Filter()) === 3)

      val e = new AnyWordSpec {
        "test this" taggedAs(mytags.FastAsLight, mytags.SlowAsMolasses) in {/* ASSERTION_SUCCEED */}
        "test that" taggedAs(mytags.SlowAsMolasses) in {/* ASSERTION_SUCCEED */}
        "test the other thing" ignore {/* ASSERTION_SUCCEED */}
      }
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 0)
      assert(e.expectedTestCount(Filter()) === 2)

      val f = new Suites(a, b, c, d, e)
      assert(f.expectedTestCount(Filter()) === 10)
    }
    it("should return the correct test count from its expectedTestCount method when uses registerTest and registerIgnoredTest to register tests") {

      val a = new AnyWordSpec {
        registerTest("test this") {/* ASSERTION_SUCCEED */}
        registerTest("test that") {/* ASSERTION_SUCCEED */}
      }
      assert(a.expectedTestCount(Filter()) == 2)

      val b = new AnyWordSpec {
        registerIgnoredTest("test this") {/* ASSERTION_SUCCEED */}
        registerTest("test that") {/* ASSERTION_SUCCEED */}
      }
      assert(b.expectedTestCount(Filter()) == 1)

      val c = new AnyWordSpec {
        registerTest("test this", mytags.FastAsLight) {/* ASSERTION_SUCCEED */}
        registerTest("test that") {/* ASSERTION_SUCCEED */}
      }
      assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) == 1)
      assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) == 1)

      val d = new AnyWordSpec {
        registerTest("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {/* ASSERTION_SUCCEED */}
        registerTest("test that", mytags.SlowAsMolasses) {/* ASSERTION_SUCCEED */}
        registerTest("test the other thing") {/* ASSERTION_SUCCEED */}
      }
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) == 1)
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) == 1)
      assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) == 1)
      assert(d.expectedTestCount(Filter()) == 3)

      val e = new AnyWordSpec {
        registerTest("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {/* ASSERTION_SUCCEED */}
        registerTest("test that", mytags.SlowAsMolasses) {/* ASSERTION_SUCCEED */}
        registerIgnoredTest("test the other thing") {/* ASSERTION_SUCCEED */}
      }
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) == 1)
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) == 1)
      assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) == 0)
      assert(e.expectedTestCount(Filter()) == 2)

      val f = new Suites(a, b, c, d, e)
      assert(f.expectedTestCount(Filter()) == 10)
    }
    it("should generate a TestPending message when the test body is (pending)") {
      val a = new AnyWordSpec {

        "should do this" is (pending)

        "should do that" in {
          assert(2 + 2 === 4)
        }
        "should do something else" in {
          assert(2 + 2 === 4)
          pending
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val tp = rep.testPendingEventsReceived
      assert(tp.size === 2)
    }
    it("should allow is pendingUntilFixed to be used after is") {
      val a = new AnyWordSpec {

        "should do this" is pendingUntilFixed {
          fail("i meant to do that")
        }

        "should do that" in {
          assert(2 + 2 === 4)
        }
        "should do something else" in {
          assert(2 + 2 === 4)
          pending
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val tp = rep.testPendingEventsReceived
      assert(tp.size === 2)
    }
    it("should generate a test failure if a Throwable, or an Error other than direct Error subtypes " +
            "known in JDK 1.5, excluding AssertionError") {
      val a = new AnyWordSpec {
        "This WordSpec" should {
          "throw AssertionError" in { throw new AssertionError }
          "throw plain old Error" in { throw new Error }
          "throw Throwable" in { throw new Throwable }
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val tf = rep.testFailedEventsReceived
      assert(tf.size === 3)
    }
    // SKIP-SCALATESTJS,NATIVE-START
    it("should propagate out Errors that are direct subtypes of Error in JDK 1.5, other than " +
            "AssertionError, causing Suites and Runs to abort.") {
      val a = new AnyWordSpec {
        "This WordSpec" should {
          "throw AssertionError" in { throw new OutOfMemoryError }
        }
      }
      intercept[OutOfMemoryError] {
        a.run(None, Args(SilentReporter))
      }
    }
    // SKIP-SCALATESTJS,NATIVE-END
/*
    it("should send InfoProvided events with aboutAPendingTest set to true for info " +
            "calls made from a test that is pending") {
      val a = new WordSpec with GivenWhenThen {
        "A WordSpec" should {
          "do something" in {
            given("two integers")
            when("one is subracted from the other")
            then("the result is the difference between the two numbers")
            pending
          }
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val testPending = rep.testPendingEventsReceived
      assert(testPending.size === 1)
      val recordedEvents = testPending(0).recordedEvents
      assert(recordedEvents.size === 3)
      for (event <- recordedEvents) {
        val ip = event.asInstanceOf[InfoProvided]
        assert(ip.aboutAPendingTest.isDefined && ip.aboutAPendingTest.get)
      }
      val so = rep.scopeOpenedEventsReceived
      assert(so.size === 1)
      for (event <- so) {
        assert(event.message == "A WordSpec")
      }
      val sc = rep.scopeClosedEventsReceived
      assert(so.size === 1)
      for (event <- sc) {
        assert(event.message == "A WordSpec")
      }
    }
    it("should send InfoProvided events with aboutAPendingTest set to false for info " +
            "calls made from a test that is not pending") {
      val a = new WordSpec with GivenWhenThen {
        "A WordSpec" should {
          "do something" in {
            given("two integers")
            when("one is subracted from the other")
            then("the result is the difference between the two numbers")
            assert(1 + 1 === 2)
          }
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val testSucceeded = rep.testSucceededEventsReceived
      assert(testSucceeded.size === 1)
      val recordedEvents = testSucceeded(0).recordedEvents
      assert(recordedEvents.size === 3)
      for (event <- recordedEvents) {
        val ip = event.asInstanceOf[InfoProvided]
        assert(ip.aboutAPendingTest.isDefined && !ip.aboutAPendingTest.get)
      }
      val so = rep.scopeOpenedEventsReceived
      assert(so.size === 1)
      for (event <- so) {
        assert(event.message == "A WordSpec")
      }
      val sc = rep.scopeClosedEventsReceived
      assert(so.size === 1)
      for (event <- sc) {
        assert(event.message == "A WordSpec")
      }
    }
*/
    it("should not put parentheses around should clauses that follow when") {
      val a = new AnyWordSpec {
        "A Stack" when {
          "empty" should {
            "chill out" in {
              assert(1 + 1 === 2)
            }
          }
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val ts = rep.testSucceededEventsReceived
      assert(ts.size === 1)
      assert(ts.head.testName === "A Stack when empty should chill out")
    }
    it("should not put parentheses around should clauses that don't follow when") {
      val a = new AnyWordSpec {
        "A Stack" should {
          "chill out" in {
            assert(1 + 1 === 2)
          }
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val ts = rep.testSucceededEventsReceived
      assert(ts.size === 1)
      assert(ts.head.testName === "A Stack should chill out")
    }
    
    it("should contains correct formatter for TestStarting, TestSucceeded, TestFailed, TestPending, TestCanceled and TestIgnored") {
      class TestSpec extends AnyWordSpec {
        "a feature" should {
          "succeeded here" in {/* ASSERTION_SUCCEED */}
          "failed here" in { fail() }
          "pending here" in { pending }
          "cancel here" in { cancel() }
          "ignore here" ignore {/* ASSERTION_SUCCEED */}
        }
      }
      val rep = new EventRecordingReporter
      val s = new TestSpec
      s.run(None, Args(rep))
      val testStartingList = rep.testStartingEventsReceived
      assert(testStartingList.size === 4)
      assert(testStartingList(0).formatter === Some(MotionToSuppress), "Expected testStartingList(0).formatter to be Some(MotionToSuppress), but got: " + testStartingList(0).formatter.getClass.getName)
      assert(testStartingList(1).formatter === Some(MotionToSuppress), "Expected testStartingList(1).formatter to be Some(MotionToSuppress), but got: " + testStartingList(1).formatter.getClass.getName)
      assert(testStartingList(2).formatter === Some(MotionToSuppress), "Expected testStartingList(2).formatter to be Some(MotionToSuppress), but got: " + testStartingList(2).formatter.getClass.getName)
      assert(testStartingList(3).formatter === Some(MotionToSuppress), "Expected testStartingList(3).formatter to be Some(MotionToSuppress), but got: " + testStartingList(3).formatter.getClass.getName)
      
      val testSucceededList = rep.testSucceededEventsReceived
      assert(testSucceededList.size === 1)
      assert(testSucceededList(0).formatter.isDefined, "Expected testSucceededList(0).formatter to be defined, but it is not.")
      assert(testSucceededList(0).formatter.get.isInstanceOf[IndentedText], "Expected testSucceededList(0).formatter to be Some(IndentedText), but got: " + testSucceededList(0).formatter)
      val testSucceededFormatter = testSucceededList(0).formatter.get.asInstanceOf[IndentedText]
      assert(testSucceededFormatter.formattedText === "- should succeeded here")
      assert(testSucceededFormatter.rawText === "should succeeded here")
      
      val testFailedList = rep.testFailedEventsReceived
      assert(testFailedList.size === 1)
      assert(testFailedList(0).formatter.isDefined, "Expected testFailedList(0).formatter to be defined, but it is not.")
      assert(testFailedList(0).formatter.get.isInstanceOf[IndentedText], "Expected testFailedList(0).formatter to be Some(IndentedText), but got: " + testSucceededList(0).formatter)
      val testFailedFormatter = testFailedList(0).formatter.get.asInstanceOf[IndentedText]
      assert(testFailedFormatter.formattedText === "- should failed here")
      assert(testFailedFormatter.rawText === "should failed here")
      
      val testPendingList = rep.testPendingEventsReceived
      assert(testPendingList.size === 1)
      assert(testPendingList(0).formatter.isDefined, "Expected testPendingList(0).formatter to be defined, but it is not.")
      assert(testPendingList(0).formatter.get.isInstanceOf[IndentedText], "Expected testPendingList(0).formatter to be Some(IndentedText), but got: " + testSucceededList(0).formatter)
      val testPendingFormatter = testPendingList(0).formatter.get.asInstanceOf[IndentedText]
      assert(testPendingFormatter.formattedText === "- should pending here")
      assert(testPendingFormatter.rawText === "should pending here")
      
      val testCanceledList = rep.testCanceledEventsReceived
      assert(testCanceledList.size === 1)
      assert(testCanceledList(0).formatter.isDefined, "Expected testCanceledList(0).formatter to be defined, but it is not.")
      assert(testCanceledList(0).formatter.get.isInstanceOf[IndentedText], "Expected testCanceledList(0).formatter to be Some(IndentedText), but got: " + testSucceededList(0).formatter)
      val testCanceledFormatter = testCanceledList(0).formatter.get.asInstanceOf[IndentedText]
      assert(testCanceledFormatter.formattedText === "- should cancel here")
      assert(testCanceledFormatter.rawText === "should cancel here")
      
      val testIgnoredList = rep.testIgnoredEventsReceived
      assert(testIgnoredList.size === 1)
      assert(testIgnoredList(0).formatter.isDefined, "Expected testIgnoredList(0).formatter to be defined, but it is not.")
      assert(testIgnoredList(0).formatter.get.isInstanceOf[IndentedText], "Expected testIgnoredList(0).formatter to be Some(IndentedText), but got: " + testSucceededList(0).formatter)
      val testIgnoredFormatter = testIgnoredList(0).formatter.get.asInstanceOf[IndentedText]
      assert(testIgnoredFormatter.formattedText === "- should ignore here")
      assert(testIgnoredFormatter.rawText === "should ignore here")
    }

    describe("registerTest and registerIgnoredTest method") {
      it("should allow test registration and ignored test registration") {
        class TestSpec extends AnyWordSpec {
          val a = 1
          registerTest("test 1") {
            val e = intercept[TestFailedException] {
              assert(a == 2)
            }
            assert(e.message == Some("1 did not equal 2"))
            assert(e.failedCodeFileName == Some("AnyWordSpecSpec.scala"))
            assert(e.failedCodeLineNumber == Some(thisLineNumber - 4))
          }
          registerTest("test 2") {
            assert(a == 2)
          }
          registerTest("test 3") {
            pending
          }
          registerTest("test 4") {
            cancel()
          }
          registerIgnoredTest("test 5") {
            assert(a == 2)
          }
        }

        val rep = new EventRecordingReporter
        val s = new TestSpec
        s.run(None, Args(rep))

        assert(rep.testStartingEventsReceived.length == 4)
        assert(rep.testSucceededEventsReceived.length == 1)
        assert(rep.testSucceededEventsReceived(0).testName == "test 1")
        assert(rep.testFailedEventsReceived.length == 1)
        assert(rep.testFailedEventsReceived(0).testName == "test 2")
        assert(rep.testPendingEventsReceived.length == 1)
        assert(rep.testPendingEventsReceived(0).testName == "test 3")
        assert(rep.testCanceledEventsReceived.length == 1)
        assert(rep.testCanceledEventsReceived(0).testName == "test 4")
        assert(rep.testIgnoredEventsReceived.length == 1)
        assert(rep.testIgnoredEventsReceived(0).testName == "test 5")
      }

      it("should generate TestRegistrationClosedException with correct stack depth info when has a registerTest nested inside a registerTest") {
        class TestSpec extends AnyWordSpec {
          var registrationClosedThrown = false
          "a feature" should {
            registerTest("a scenario") {
              registerTest("nested scenario") {
                /* ASSERTION_SUCCEED */
              }; /* ASSERTION_SUCCEED */
            }
          }
          override def withFixture(test: NoArgTest): Outcome = {
            val outcome = test.apply()
            outcome match {
              case Exceptional(ex: TestRegistrationClosedException) =>
                registrationClosedThrown = true
              case _ =>
            }
            outcome
          }
        }
        val rep = new EventRecordingReporter
        val s = new TestSpec
        s.run(None, Args(rep))
        assert(s.registrationClosedThrown == true)
        val testFailedEvents = rep.testFailedEventsReceived
        assert(testFailedEvents.size === 1)
        assert(testFailedEvents(0).throwable.get.getClass() === classOf[TestRegistrationClosedException])
        val trce = testFailedEvents(0).throwable.get.asInstanceOf[TestRegistrationClosedException]
        assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
        assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
        assert(trce.message == Some("Test cannot be nested inside another test."))
      }

      it("should generate TestRegistrationClosedException with correct stack depth info when has a registerIgnoredTest nested inside a registerTest") {
        class TestSpec extends AnyWordSpec {
          var registrationClosedThrown = false
          "a feature" should {
            registerTest("a scenario") {
              registerIgnoredTest("nested scenario") {
                /* ASSERTION_SUCCEED */
              }; /* ASSERTION_SUCCEED */
            }
          }
          override def withFixture(test: NoArgTest): Outcome = {
            val outcome = test.apply()
            outcome match {
              case Exceptional(ex: TestRegistrationClosedException) =>
                registrationClosedThrown = true
              case _ =>
            }
            outcome
          }
        }
        val rep = new EventRecordingReporter
        val s = new TestSpec
        s.run(None, Args(rep))
        assert(s.registrationClosedThrown == true)
        val testFailedEvents = rep.testFailedEventsReceived
        assert(testFailedEvents.size === 1)
        assert(testFailedEvents(0).throwable.get.getClass() === classOf[TestRegistrationClosedException])
        val trce = testFailedEvents(0).throwable.get.asInstanceOf[TestRegistrationClosedException]
        assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
        assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
        assert(trce.message == Some("Test cannot be nested inside another test."))
      }

    }
    ignore("should support expectations") { // Unignore after we uncomment the expectation implicits in RegistrationPolicy
      class TestSpec extends AnyWordSpec with expectations.Expectations {
        "it should do something" in {
          expect(1 === 2); /* ASSERTION_SUCCEED */
        }
        "a widget" should {
          "do something" in {
            expect(1 === 2); /* ASSERTION_SUCCEED */
          }
        }
      }
      val rep = new EventRecordingReporter
      val s1 = new TestSpec
      s1.run(None, Args(rep))
      assert(rep.testFailedEventsReceived.size === 2)
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "AnyWordSpecSpec.scala")
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 13)
      assert(rep.testFailedEventsReceived(1).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "AnyWordSpecSpec.scala")
      assert(rep.testFailedEventsReceived(1).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 11)
    }
  }
  
  describe("when failure happens") {
    
    it("should fire TestFailed event with correct stack depth info when test failed") {
      class TestSpec extends AnyWordSpec {
        "A Stack" should {
          "chill out" in {
            assert(1 === 2)
          }
        }
      }
      val rep = new EventRecordingReporter
      val s1 = new TestSpec
      s1.run(None, Args(rep))
      assert(rep.testFailedEventsReceived.size === 1)
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "AnyWordSpecSpec.scala")
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 9)
    }
    
    it("should generate TestRegistrationClosedException with correct stack depth info when has an in nested inside an in") {
      class TestSpec extends AnyWordSpec {
        var registrationClosedThrown = false
        "a feature" should {
          "a scenario" in {
            "nested scenario" in {
              /* ASSERTION_SUCCEED */
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: NoArgTest): Outcome = {
          val outcome = test.apply()
          outcome match {
            case Exceptional(ex: TestRegistrationClosedException) => 
              registrationClosedThrown = true
            case _ =>
          }
          outcome
        }
      }
      val rep = new EventRecordingReporter
      val s = new TestSpec
      s.run(None, Args(rep))
      assert(s.registrationClosedThrown == true)
      val testFailedEvents = rep.testFailedEventsReceived
      assert(testFailedEvents.size === 1)
      assert(testFailedEvents(0).throwable.get.getClass() === classOf[TestRegistrationClosedException])
      val trce = testFailedEvents(0).throwable.get.asInstanceOf[TestRegistrationClosedException]
      assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("An in clause may not appear inside another in clause."))
    }

    it("should generate TestRegistrationClosedException with correct stack depth info when has an ignore nested inside an in") {
      class TestSpec extends AnyWordSpec {
        var registrationClosedThrown = false
        "a feature" should {
          "a scenario" in {
            "nested scenario" ignore {
              /* ASSERTION_SUCCEED */
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: NoArgTest): Outcome = {
          val outcome = test.apply()
          outcome match {
            case Exceptional(ex: TestRegistrationClosedException) =>
              registrationClosedThrown = true
            case _ =>
          }
          outcome
        }
      }
      val rep = new EventRecordingReporter
      val s = new TestSpec
      s.run(None, Args(rep))
      assert(s.registrationClosedThrown == true)
      val testFailedEvents = rep.testFailedEventsReceived
      assert(testFailedEvents.size === 1)
      assert(testFailedEvents(0).throwable.get.getClass() === classOf[TestRegistrationClosedException])
      val trce = testFailedEvents(0).throwable.get.asInstanceOf[TestRegistrationClosedException]
      assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("An ignore clause may not appear inside an in clause."))
    }
  }
  
  describe("shorthand syntax") {
    
    describe("'it'") {
      
      describe("under top level") {
        
        it("should work with subject") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            } 

            it should { 
              "do something interesting 1" in {/* ASSERTION_SUCCEED */}
            }
        
            it can {
              "do something interesting 2" in {/* ASSERTION_SUCCEED */}
            }
        
            it must {
              "do something interesting 3" in {/* ASSERTION_SUCCEED */}
            }
        
            it when {
              "do something interesting 4" in {/* ASSERTION_SUCCEED */}
            }
          }
      
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
      
          val testStartingList = rep.testStartingEventsReceived
          assert(testStartingList.size === 5)
          assert(testStartingList(0).testName === "A Stack when empty should be empty")
          assert(testStartingList(1).testName === "A Stack should do something interesting 1")
          assert(testStartingList(2).testName === "A Stack can do something interesting 2")
          assert(testStartingList(3).testName === "A Stack must do something interesting 3")
          assert(testStartingList(4).testName === "A Stack when do something interesting 4")
      
          val testSucceededList = rep.testSucceededEventsReceived
          assert(testSucceededList.size === 5)
          assert(testSucceededList(0).testName === "A Stack when empty should be empty")
          assert(testSucceededList(1).testName === "A Stack should do something interesting 1")
          assert(testSucceededList(2).testName === "A Stack can do something interesting 2")
          assert(testSucceededList(3).testName === "A Stack must do something interesting 3")
          assert(testSucceededList(4).testName === "A Stack when do something interesting 4")
        }
    
        it("should throw NotAllowedException with correct stack depth and message when 'it should' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            it should {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it can' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            it can {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it must' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            it must {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it when' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            it when {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it should' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            it should {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it can' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            it can {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it must' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            it must {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it when' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            it when {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
      }
      
      describe("under inner level") {
        
        it("should throw NotAllowedException with correct stack depth and message when 'it should' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              it should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it can' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              it can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it must' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              it must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it when' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              it when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it should' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              it should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it can' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              it can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it must' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              it must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it when' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              it when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it should' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              it should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it can' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              it can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it must' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              it must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it when' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              it when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "An it clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
      }
      
      describe("under 'in' clause") {
        
        it("should throw NotAllowedException with correct stack depth and message when 'it should' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              it should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "An it clause must only appear after a top level subject clause.")
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it can' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              it can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "An it clause must only appear after a top level subject clause.")
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it must' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              it must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "An it clause must only appear after a top level subject clause.")
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'it when' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              it when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "An it clause must only appear after a top level subject clause.")
        }
        
      }
    }
    
    describe("'they'") {
      
      describe("under top level") {
        
        it("should work with subject") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            } 

            they should { 
              "do something interesting 1" in {/* ASSERTION_SUCCEED */}
            }
        
            they can {
              "do something interesting 2" in {/* ASSERTION_SUCCEED */}
            }
        
            they must {
              "do something interesting 3" in {/* ASSERTION_SUCCEED */}
            }
        
            they when {
              "do something interesting 4" in {/* ASSERTION_SUCCEED */}
            }
          }
      
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
      
          val testStartingList = rep.testStartingEventsReceived
          assert(testStartingList.size === 5)
          assert(testStartingList(0).testName === "A Stack when empty should be empty")
          assert(testStartingList(1).testName === "A Stack should do something interesting 1")
          assert(testStartingList(2).testName === "A Stack can do something interesting 2")
          assert(testStartingList(3).testName === "A Stack must do something interesting 3")
          assert(testStartingList(4).testName === "A Stack when do something interesting 4")
      
          val testSucceededList = rep.testSucceededEventsReceived
          assert(testSucceededList.size === 5)
          assert(testSucceededList(0).testName === "A Stack when empty should be empty")
          assert(testSucceededList(1).testName === "A Stack should do something interesting 1")
          assert(testSucceededList(2).testName === "A Stack can do something interesting 2")
          assert(testSucceededList(3).testName === "A Stack must do something interesting 3")
          assert(testSucceededList(4).testName === "A Stack when do something interesting 4")
        }
    
        it("should throw NotAllowedException with correct stack depth and message when 'they should' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            they should {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they can' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            they can {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they must' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            they must {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they when' is called without subject") {
          class TestSpec extends AnyWordSpec { 
            they when {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they should' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            they should {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they can' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            they can {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they must' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            they must {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they when' is called after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 

            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              } 
            }
            
            "Other do something special" in {/* ASSERTION_SUCCEED */}
            
            they when {
              "do something interesting" in {/* ASSERTION_SUCCEED */}
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 9))
        }
      }
      
      describe("under inner level") {
        
        it("should throw NotAllowedException with correct stack depth and message when 'they should' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              they should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they can' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              they can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they must' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              they must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they when' is called with inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              they when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they should' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              they should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they can' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              they can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they must' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              they must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they when' is called without inner branch") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              they when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they should' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              they should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they can' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              they can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they must' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              they must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they when' is called with inner branch but after an 'in' clause") {
          class TestSpec extends AnyWordSpec { 
            "A Stack" when { 
              "empty" should { 
                "be empty" in {/* ASSERTION_SUCCEED */}
              }
              "do something" in {/* ASSERTION_SUCCEED */}
              they when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }
            }
          }
          val e = intercept[exceptions.NotAllowedException] {
            new TestSpec
          }
          assert(e.getMessage === "A they clause must only appear after a top level subject clause.")
          assert(e.failedCodeFileName === Some("AnyWordSpecSpec.scala"))
          assert(e.failedCodeLineNumber === Some(thisLineNumber - 10))
        }
      }
      
      describe("under 'in' clause") {
        
        it("should throw NotAllowedException with correct stack depth and message when 'they should' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              they should {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "A they clause must only appear after a top level subject clause.")
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they can' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              they can {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "A they clause must only appear after a top level subject clause.")
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they must' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              they must {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "A they clause must only appear after a top level subject clause.")
        }
        
        it("should throw NotAllowedException with correct stack depth and message when 'they when' is called") {
          class TestSpec extends AnyWordSpec { 
            var notAllowedThrown = false
            "Something special" in {
              they when {
                "do something interesting" in {/* ASSERTION_SUCCEED */}
              }; /* ASSERTION_SUCCEED */
            }
            override def withFixture(test: NoArgTest): Outcome = {
              val outcome = test.apply()
              outcome match {
                case Exceptional(ex: exceptions.NotAllowedException) => 
                  notAllowedThrown = true
                case _ =>
              }
              outcome
            }
          }
          val rep = new EventRecordingReporter
          val s = new TestSpec
          s.run(None, Args(rep))
          assert(s.notAllowedThrown == true)
          val testFailedEvents = rep.testFailedEventsReceived
          assert(testFailedEvents.size === 1)
          assert(testFailedEvents(0).throwable.get.getClass() === classOf[exceptions.NotAllowedException])
          val trce = testFailedEvents(0).throwable.get.asInstanceOf[exceptions.NotAllowedException]
          assert("AnyWordSpecSpec.scala" === trce.failedCodeFileName.get)
          assert(trce.failedCodeLineNumber.get === thisLineNumber - 23)
          assert(trce.getMessage === "A they clause must only appear after a top level subject clause.")
        }

        /*it("should generate NotAllowedException wrapping a TestFailedException when assert fails in should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              assert(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestFailedException])
          val cause = causeThrowable.asInstanceOf[TestFailedException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestFailedException when assert fails in must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              assert(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestFailedException])
          val cause = causeThrowable.asInstanceOf[TestFailedException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestFailedException when assert fails in when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              assert(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestFailedException])
          val cause = causeThrowable.asInstanceOf[TestFailedException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestFailedException when assert fails in that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              assert(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestFailedException])
          val cause = causeThrowable.asInstanceOf[TestFailedException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestFailedException when assert fails in which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              assert(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestFailedException])
          val cause = causeThrowable.asInstanceOf[TestFailedException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestFailedException when assert fails in can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              assert(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestFailedException])
          val cause = causeThrowable.asInstanceOf[TestFailedException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestCanceledException when assume fails in should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              assume(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestCanceledException])
          val cause = causeThrowable.asInstanceOf[TestCanceledException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestCanceledException when assume fails in must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              assume(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestCanceledException])
          val cause = causeThrowable.asInstanceOf[TestCanceledException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestCanceledException when assume fails in when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              assume(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestCanceledException])
          val cause = causeThrowable.asInstanceOf[TestCanceledException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestCanceledException when assume fails in that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              assume(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestCanceledException])
          val cause = causeThrowable.asInstanceOf[TestCanceledException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestCanceledException when assume fails in which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              assume(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestCanceledException])
          val cause = causeThrowable.asInstanceOf[TestCanceledException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a TestCanceledException when assume fails in can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              assume(a == 2)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotShouldMustWhenThatWhichOrCanClause))

          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(causeThrowable.isInstanceOf[TestCanceledException])
          val cause = causeThrowable.asInstanceOf[TestCanceledException]
          assert("AnyWordSpecSpec.scala" == cause.failedCodeFileName.get)
          assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
          assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
        }

        it("should generate NotAllowedException wrapping a non-fatal RuntimeException is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new RuntimeException("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(e.message == Some(FailureMessages.exceptionWasThrownInShouldClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature")))

          assert(causeThrowable.isInstanceOf[RuntimeException])
          val cause = causeThrowable.asInstanceOf[RuntimeException]
          assert(cause.getMessage == "on purpose")
        }

        it("should generate NotAllowedException wrapping a non-fatal RuntimeException is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new RuntimeException("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(e.message == Some(FailureMessages.exceptionWasThrownInMustClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature")))

          assert(causeThrowable.isInstanceOf[RuntimeException])
          val cause = causeThrowable.asInstanceOf[RuntimeException]
          assert(cause.getMessage == "on purpose")
        }

        it("should generate NotAllowedException wrapping a non-fatal RuntimeException is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new RuntimeException("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(e.message == Some(FailureMessages.exceptionWasThrownInWhenClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature")))

          assert(causeThrowable.isInstanceOf[RuntimeException])
          val cause = causeThrowable.asInstanceOf[RuntimeException]
          assert(cause.getMessage == "on purpose")
        }

        it("should generate NotAllowedException wrapping a non-fatal RuntimeException is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new RuntimeException("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(e.message == Some(FailureMessages.exceptionWasThrownInThatClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature")))

          assert(causeThrowable.isInstanceOf[RuntimeException])
          val cause = causeThrowable.asInstanceOf[RuntimeException]
          assert(cause.getMessage == "on purpose")
        }

        it("should generate NotAllowedException wrapping a non-fatal RuntimeException is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new RuntimeException("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(e.message == Some(FailureMessages.exceptionWasThrownInWhichClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature")))

          assert(causeThrowable.isInstanceOf[RuntimeException])
          val cause = causeThrowable.asInstanceOf[RuntimeException]
          assert(cause.getMessage == "on purpose")
        }

        it("should generate NotAllowedException wrapping a non-fatal RuntimeException is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new RuntimeException("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[NotAllowedException] {
            new TestSpec
          }
          assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
          assert(e.failedCodeLineNumber.get == thisLineNumber - 3)
          assert(e.cause.isDefined)
          val causeThrowable = e.cause.get
          assert(e.message == Some(FailureMessages.exceptionWasThrownInCanClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature")))

          assert(causeThrowable.isInstanceOf[RuntimeException])
          val cause = causeThrowable.asInstanceOf[RuntimeException]
          assert(cause.getMessage == "on purpose")
        }
*/

        // SKIP-SCALATESTJS,NATIVE-START
/*
        it("should propagate AnnotationFormatError when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new AnnotationFormatError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AnnotationFormatError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AnnotationFormatError when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new AnnotationFormatError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AnnotationFormatError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AnnotationFormatError when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new AnnotationFormatError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AnnotationFormatError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AnnotationFormatError when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new AnnotationFormatError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AnnotationFormatError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AnnotationFormatError when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new AnnotationFormatError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AnnotationFormatError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AnnotationFormatError when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new AnnotationFormatError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AnnotationFormatError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AWTError when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new AWTError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AWTError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AWTError when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new AWTError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AWTError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AWTError when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new AWTError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AWTError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AWTError when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new AWTError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AWTError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AWTError when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new AWTError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AWTError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate AWTError when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new AWTError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[AWTError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate CoderMalfunctionError when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new CoderMalfunctionError(new RuntimeException("on purpose"))
              assert(a == 1)
            }
          }
          val e = intercept[CoderMalfunctionError] {
            new TestSpec
          }
          assert(e.getMessage == "java.lang.RuntimeException: on purpose")
        }

        it("should propagate CoderMalfunctionError when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new CoderMalfunctionError(new RuntimeException("on purpose"))
              assert(a == 1)
            }
          }
          val e = intercept[CoderMalfunctionError] {
            new TestSpec
          }
          assert(e.getMessage == "java.lang.RuntimeException: on purpose")
        }

        it("should propagate CoderMalfunctionError when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new CoderMalfunctionError(new RuntimeException("on purpose"))
              assert(a == 1)
            }
          }
          val e = intercept[CoderMalfunctionError] {
            new TestSpec
          }
          assert(e.getMessage == "java.lang.RuntimeException: on purpose")
        }

        it("should propagate CoderMalfunctionError when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new CoderMalfunctionError(new RuntimeException("on purpose"))
              assert(a == 1)
            }
          }
          val e = intercept[CoderMalfunctionError] {
            new TestSpec
          }
          assert(e.getMessage == "java.lang.RuntimeException: on purpose")
        }

        it("should propagate CoderMalfunctionError when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new CoderMalfunctionError(new RuntimeException("on purpose"))
              assert(a == 1)
            }
          }
          val e = intercept[CoderMalfunctionError] {
            new TestSpec
          }
          assert(e.getMessage == "java.lang.RuntimeException: on purpose")
        }

        it("should propagate CoderMalfunctionError when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new CoderMalfunctionError(new RuntimeException("on purpose"))
              assert(a == 1)
            }
          }
          val e = intercept[CoderMalfunctionError] {
            new TestSpec
          }
          assert(e.getMessage == "java.lang.RuntimeException: on purpose")
        }

        it("should propagate FactoryConfigurationError when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new FactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[FactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate FactoryConfigurationError when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new FactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[FactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate FactoryConfigurationError when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new FactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[FactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate FactoryConfigurationError when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new FactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[FactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate FactoryConfigurationError when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new FactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[FactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate FactoryConfigurationError when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new FactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[FactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate LinkageError when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new LinkageError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[LinkageError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate LinkageError when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new LinkageError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[LinkageError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate LinkageError when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new LinkageError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[LinkageError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate LinkageError when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new LinkageError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[LinkageError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate LinkageError when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new LinkageError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[LinkageError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate LinkageError when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new LinkageError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[LinkageError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate ThreadDeath when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new ThreadDeath
              assert(a == 1)
            }
          }
          val e = intercept[ThreadDeath] {
            new TestSpec
          }
          assert(e.getMessage == null)
        }

        it("should propagate ThreadDeath when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new ThreadDeath
              assert(a == 1)
            }
          }
          val e = intercept[ThreadDeath] {
            new TestSpec
          }
          assert(e.getMessage == null)
        }

        it("should propagate ThreadDeath when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new ThreadDeath
              assert(a == 1)
            }
          }
          val e = intercept[ThreadDeath] {
            new TestSpec
          }
          assert(e.getMessage == null)
        }

        it("should propagate ThreadDeath when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new ThreadDeath
              assert(a == 1)
            }
          }
          val e = intercept[ThreadDeath] {
            new TestSpec
          }
          assert(e.getMessage == null)
        }

        it("should propagate ThreadDeath when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new ThreadDeath
              assert(a == 1)
            }
          }
          val e = intercept[ThreadDeath] {
            new TestSpec
          }
          assert(e.getMessage == null)
        }

        it("should propagate ThreadDeath when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new ThreadDeath
              assert(a == 1)
            }
          }
          val e = intercept[ThreadDeath] {
            new TestSpec
          }
          assert(e.getMessage == null)
        }

        it("should propagate TransformerFactoryConfigurationError when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new TransformerFactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[TransformerFactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate TransformerFactoryConfigurationError when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new TransformerFactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[TransformerFactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate TransformerFactoryConfigurationError when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new TransformerFactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[TransformerFactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate TransformerFactoryConfigurationError when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new TransformerFactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[TransformerFactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate TransformerFactoryConfigurationError when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new TransformerFactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[TransformerFactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate TransformerFactoryConfigurationError when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new TransformerFactoryConfigurationError("on purpose")
              assert(a == 1)
            }
          }
          val e = intercept[TransformerFactoryConfigurationError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate VirtualMachineError when it is thrown inside should scope") {
          class TestSpec extends WordSpec {
            "a feature" should {
              val a = 1
              throw new VirtualMachineError("on purpose") {}
              assert(a == 1)
            }
          }
          val e = intercept[VirtualMachineError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate VirtualMachineError when it is thrown inside must scope") {
          class TestSpec extends WordSpec {
            "a feature" must {
              val a = 1
              throw new VirtualMachineError("on purpose") {}
              assert(a == 1)
            }
          }
          val e = intercept[VirtualMachineError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate VirtualMachineError when it is thrown inside when scope") {
          class TestSpec extends WordSpec {
            "a feature" when {
              val a = 1
              throw new VirtualMachineError("on purpose") {}
              assert(a == 1)
            }
          }
          val e = intercept[VirtualMachineError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate VirtualMachineError when it is thrown inside that scope") {
          class TestSpec extends WordSpec {
            "a feature" that {
              val a = 1
              throw new VirtualMachineError("on purpose") {}
              assert(a == 1)
            }
          }
          val e = intercept[VirtualMachineError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate VirtualMachineError when it is thrown inside which scope") {
          class TestSpec extends WordSpec {
            "a feature" which {
              val a = 1
              throw new VirtualMachineError("on purpose") {}
              assert(a == 1)
            }
          }
          val e = intercept[VirtualMachineError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }

        it("should propagate VirtualMachineError when it is thrown inside can scope") {
          class TestSpec extends WordSpec {
            "a feature" can {
              val a = 1
              throw new VirtualMachineError("on purpose") {}
              assert(a == 1)
            }
          }
          val e = intercept[VirtualMachineError] {
            new TestSpec
          }
          assert(e.getMessage == "on purpose")
        }*/
        // SKIP-SCALATESTJS,NATIVE-END
      }
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside when") {
      class TestSpec extends AnyWordSpec {
        "a feature" when {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInWhenClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature when test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature when test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside shorthand when") {
      class TestSpec extends AnyWordSpec {
        "a feature" when {
          //DOTTY-ONLY ()
        }
        it when {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInWhenClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature when test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature when test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside should") {
      class TestSpec extends AnyWordSpec {
        "a feature" should {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInShouldClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature should test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature should test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside shorthand should") {
      class TestSpec extends AnyWordSpec {
        "a feature" should {
          //DOTTY-ONLY ()
        }
        it should {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
        new TestSpec
      }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInShouldClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature should test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature should test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside must") {
      class TestSpec extends AnyWordSpec {
        "a feature" must {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInMustClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature must test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature must test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside shorthand must") {
      class TestSpec extends AnyWordSpec {
        "a feature" must {
          //DOTTY-ONLY ()
        }
        it must {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInMustClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature must test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature must test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside that") {
      class TestSpec extends AnyWordSpec {
        "a feature" that {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInThatClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature that", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature that test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature that test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside which") {
      class TestSpec extends AnyWordSpec {
        "a feature" which {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInWhichClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature which", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature which test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature which test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside can") {
      class TestSpec extends AnyWordSpec {
        "a feature" can {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInCanClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature can test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature can test 1")))
    }

    it("should throw NotAllowedException wrapping a DuplicateTestNameException when duplicate test name is detected inside shorthand can") {
      class TestSpec extends AnyWordSpec {
        "a feature" can {
          //DOTTY-ONLY ()
        }
        it can {
          "test 1" in {/* ASSERTION_SUCCEED */}
          "test 1" in {/* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
            new TestSpec
          }
      assert("AnyWordSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInCanClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature can test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature can test 1")))
    }

  }
}
