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
package org.scalatest.funspec

import org.scalatest._
import SharedHelpers._
import org.scalactic.Prettifier
import events.TestFailed
import java.awt.AWTError
import java.lang.annotation.AnnotationFormatError
import java.nio.charset.CoderMalfunctionError
import javax.xml.parsers.FactoryConfigurationError
import javax.xml.transform.TransformerFactoryConfigurationError
import org.scalactic.exceptions.NullArgumentException
import org.scalatest.events.InfoProvided
import org.scalatest.exceptions.DuplicateTestNameException
import org.scalatest.exceptions.NotAllowedException
import org.scalatest.exceptions.TestCanceledException
import org.scalatest.exceptions.TestFailedException
import org.scalatest.exceptions.TestRegistrationClosedException
import org.scalatest
import org.scalatest.funspec

class FixtureAnyFunSpecSpec extends scalatest.freespec.AnyFreeSpec {

  private val prettifier = Prettifier.default

  "A fixture.FunSpec" - {

    "should return the test names in order of registration from testNames" in {
      val a = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = Succeeded
        it("should do that") { fixture =>
          /* ASSERTION_SUCCEED */
        }
        it("should do this") { fixture =>
          /* ASSERTION_SUCCEED */
        }
      }

      assertResult(List("should do that", "should do this")) {
        a.testNames.iterator.toList
      }

      val b = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = Succeeded
      }

      assertResult(List[String]()) {
        b.testNames.iterator.toList
      }

      val c = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = Succeeded
        it("should do this") { fixture =>
          /* ASSERTION_SUCCEED */
        }
        it("should do that") { fixture =>
          /* ASSERTION_SUCCEED */
        }
      }

      assertResult(List("should do this", "should do that")) {
        c.testNames.iterator.toList
      }
    }

    "should throw DuplicateTestNameException if a duplicate test name registration is attempted" in {

      intercept[DuplicateTestNameException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          it("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
          it("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
        }
      }
      intercept[DuplicateTestNameException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          it("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
          ignore("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
        }
      }
      intercept[DuplicateTestNameException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          ignore("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
          ignore("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
        }
      }
      intercept[DuplicateTestNameException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          ignore("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
          it("test this") { fixture =>
            /* ASSERTION_SUCCEED */
          }
        }
      }
    }

    "should pass in the fixture to every test method" in {
      val a = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        val hello = "Hello, world!"
        def withFixture(test: OneArgTest): Outcome = {
          test(hello)
        }
        it("should do this") { fixture =>
          assert(fixture === hello)
        }
        it("should do that") { fixture =>
          assert(fixture === hello)
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      assert(!rep.eventsReceived.exists(_.isInstanceOf[TestFailed]))
    }
    "should throw NullArgumentException if a null test tag is provided" in {
      // it
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          it("hi", null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      val caught = intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          it("hi", mytags.SlowAsMolasses, null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          it("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }

      // ignore
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          ignore("hi", null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      val caught2 = intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          ignore("hi", mytags.SlowAsMolasses, null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          ignore("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }

      // registerTest
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          registerTest("hi", null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      val caught3 = intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          registerTest("hi", mytags.SlowAsMolasses, null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      assert(caught3.getMessage === "a test tag was null")
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          registerTest("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }

      // registerIgnoredTest
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          registerIgnoredTest("hi", null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      val caught4 = intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          registerIgnoredTest("hi", mytags.SlowAsMolasses, null) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
      assert(caught4.getMessage === "a test tag was null")
      intercept[NullArgumentException] {
        new funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = Succeeded
          registerIgnoredTest("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) { fixture => /* ASSERTION_SUCCEED */ }
        }
      }
    }

    class TestWasCalledSuite extends funspec.FixtureAnyFunSpec {
      type FixtureParam = String
      def withFixture(test: OneArgTest): Outcome = { test("hi") }
      var theTestThisCalled = false
      var theTestThatCalled = false
      it("should run this") { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
      it("should run that, maybe") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
    }

    "should execute all tests when run is called with testName None" in {

      val b = new TestWasCalledSuite
      b.run(None, Args(SilentReporter))
      assert(b.theTestThisCalled)
      assert(b.theTestThatCalled)
    }

    "should execute one test when run is called with a defined testName" in {

      val a = new TestWasCalledSuite
      a.run(Some("should run this"), Args(SilentReporter))
      assert(a.theTestThisCalled)
      assert(!a.theTestThatCalled)
    }

    "should report as ignored, and not run, tests marked ignored" in {

      class SpecA extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        it("test this") { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      class SpecB extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        ignore("test this") { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val b = new SpecB

      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB))
      assert(repB.testIgnoredReceived)
      assert(repB.lastEvent.isDefined)
      assert(repB.lastEvent.get.testName endsWith "test this")
      assert(!b.theTestThisCalled)
      assert(b.theTestThatCalled)

      class SpecC extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        it("test this") { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        ignore("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
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
      class SpecD extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        ignore("test this") { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        ignore("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
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

    "should ignore a test marked as ignored if run is invoked with that testName" in {
      // If I provide a specific testName to run, then it should ignore an Ignore on that test
      // method and actually invoke it.
      class SpecE extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        ignore("test this") { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val e = new SpecE

      import scala.language.reflectiveCalls

      val repE = new TestIgnoredTrackingReporter
      e.run(Some("test this"), Args(repE))
      assert(repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(!e.theTestThatCalled)
    }

    "should run only those tests selected by the tags to include and exclude sets" in {

      // Nothing is excluded
      class SpecA extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        it("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      // SlowAsMolasses is included, one test should be excluded
      class SpecB extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        it("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val b = new SpecB
      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repB.testIgnoredReceived)
      assert(b.theTestThisCalled)
      assert(!b.theTestThatCalled)

      // SlowAsMolasses is included, and both tests should be included
      class SpecC extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        it("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val c = new SpecC
      val repC = new TestIgnoredTrackingReporter
      c.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repC.testIgnoredReceived)
      assert(c.theTestThisCalled)
      assert(c.theTestThatCalled)

      // SlowAsMolasses is included. both tests should be included but one ignored
      class SpecD extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        ignore("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val d = new SpecD
      val repD = new TestIgnoredTrackingReporter
      d.run(None, Args(repD, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repD.testIgnoredReceived)
      assert(!d.theTestThisCalled)
      assert(d.theTestThatCalled)

      // SlowAsMolasses included, FastAsLight excluded
      class SpecE extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        it("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
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
      class SpecF extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        it("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
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
      class SpecG extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        ignore("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
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
      class SpecH extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        it("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val h = new SpecH
      val repH = new TestIgnoredTrackingReporter
      h.run(None, Args(repH, Stopper.default, Filter(None, Set("org.scalatest.FastAsLight")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repH.testIgnoredReceived)
      assert(!h.theTestThisCalled)
      assert(h.theTestThatCalled)
      assert(h.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded
      class SpecI extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        it("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val i = new SpecI
      val repI = new TestIgnoredTrackingReporter
      i.run(None, Args(repI, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!i.theTestThisCalled)
      assert(!i.theTestThatCalled)
      assert(i.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded, TestIgnored should not be received on excluded ones
      class SpecJ extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        ignore("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        it("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val j = new SpecJ
      val repJ = new TestIgnoredTrackingReporter
      j.run(None, Args(repJ, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!j.theTestThisCalled)
      assert(!j.theTestThatCalled)
      assert(j.theTestTheOtherCalled)

      // Same as previous, except Ignore specifically mentioned in excludes set
      class SpecK extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        ignore("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        ignore("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val k = new SpecK
      val repK = new TestIgnoredTrackingReporter
      k.run(None, Args(repK, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repK.testIgnoredReceived)
      assert(!k.theTestThisCalled)
      assert(!k.theTestThatCalled)
      assert(!k.theTestTheOtherCalled)
    }

    "should run only those registered tests selected by the tags to include and exclude sets" in {

      // Nothing is excluded
      class SpecA extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerTest("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      // SlowAsMolasses is included, one test should be excluded
      class SpecB extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerTest("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that") { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val b = new SpecB
      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repB.testIgnoredReceived)
      assert(b.theTestThisCalled)
      assert(!b.theTestThatCalled)

      // SlowAsMolasses is included, and both tests should be included
      class SpecC extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerTest("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val c = new SpecC
      val repC = new TestIgnoredTrackingReporter
      c.run(None, Args(repB, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repC.testIgnoredReceived)
      assert(c.theTestThisCalled)
      assert(c.theTestThatCalled)

      // SlowAsMolasses is included. both tests should be included but one ignored
      class SpecD extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val d = new SpecD
      val repD = new TestIgnoredTrackingReporter
      d.run(None, Args(repD, Stopper.default, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repD.testIgnoredReceived)
      assert(!d.theTestThisCalled)
      assert(d.theTestThatCalled)

      // SlowAsMolasses included, FastAsLight excluded
      class SpecE extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
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
      class SpecF extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
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
      class SpecG extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
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
      class SpecH extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val h = new SpecH
      val repH = new TestIgnoredTrackingReporter
      h.run(None, Args(repH, Stopper.default, Filter(None, Set("org.scalatest.FastAsLight")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repH.testIgnoredReceived)
      assert(!h.theTestThisCalled)
      assert(h.theTestThatCalled)
      assert(h.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded
      class SpecI extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val i = new SpecI
      val repI = new TestIgnoredTrackingReporter
      i.run(None, Args(repI, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!i.theTestThisCalled)
      assert(!i.theTestThatCalled)
      assert(i.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded, TestIgnored should not be received on excluded ones
      class SpecJ extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerTest("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val j = new SpecJ
      val repJ = new TestIgnoredTrackingReporter
      j.run(None, Args(repJ, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!j.theTestThisCalled)
      assert(!j.theTestThatCalled)
      assert(j.theTestTheOtherCalled)

      // Same as previous, except Ignore specifically mentioned in excludes set
      class SpecK extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        registerIgnoredTest("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { fixture => theTestThisCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test that", mytags.SlowAsMolasses) { fixture => theTestThatCalled = true; /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test the other") { fixture => theTestTheOtherCalled = true; /* ASSERTION_SUCCEED */ }
      }
      val k = new SpecK
      val repK = new TestIgnoredTrackingReporter
      k.run(None, Args(repK, Stopper.default, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), ConfigMap.empty, None, new Tracker, Set.empty))
      assert(repK.testIgnoredReceived)
      assert(!k.theTestThisCalled)
      assert(!k.theTestThatCalled)
      assert(!k.theTestTheOtherCalled)
    }

    "should return the correct test count from its expectedTestCount method" in {

      val a = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        it("test this") { fixture => /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(a.expectedTestCount(Filter()) === 2)

      val b = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        ignore("test this") { fixture => /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(b.expectedTestCount(Filter()) === 1)

      val c = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        it("test this", mytags.FastAsLight) { fixture => /* ASSERTION_SUCCEED */ }
        it("test that") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) === 1)

      val d = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        it("test this", mytags.FastAsLight, mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        it("test the other thing") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 1)
      assert(d.expectedTestCount(Filter()) === 3)

      val e = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        it("test this", mytags.FastAsLight, mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        it("test that", mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        ignore("test the other thing") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 0)
      assert(e.expectedTestCount(Filter()) === 2)

      val f = new Suites(a, b, c, d, e)
      assert(f.expectedTestCount(Filter()) === 10)
    }

    "should return the correct test count from its expectedTestCount method when uses registerTest and registerIgnoredTest to register tests" in {

      val a = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        registerTest("test this") { fixture => /* ASSERTION_SUCCEED */ }
        registerTest("test that") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(a.expectedTestCount(Filter()) === 2)

      val b = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        registerIgnoredTest("test this") { fixture => /* ASSERTION_SUCCEED */ }
        registerTest("test that") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(b.expectedTestCount(Filter()) === 1)

      val c = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        registerTest("test this", mytags.FastAsLight) { fixture => /* ASSERTION_SUCCEED */ }
        registerTest("test that") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) === 1)

      val d = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        registerTest("test this", mytags.FastAsLight, mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        registerTest("test the other thing") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 1)
      assert(d.expectedTestCount(Filter()) === 3)

      val e = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        registerTest("test this", mytags.FastAsLight, mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        registerTest("test that", mytags.SlowAsMolasses) { fixture => /* ASSERTION_SUCCEED */ }
        registerIgnoredTest("test the other thing") { fixture => /* ASSERTION_SUCCEED */ }
      }
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 0)
      assert(e.expectedTestCount(Filter()) === 2)

      val f = new Suites(a, b, c, d, e)
      assert(f.expectedTestCount(Filter()) === 10)
    }

    "should generate a TestPending message when the test body is (pending)" in {
      val a = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        val hello = "Hello, world!"
        def withFixture(test: OneArgTest): Outcome = {
          test(hello)
        }

        it("should do this") (pending)

        it("should do that") { fixture =>
          assert(fixture === hello)
        }
        it("should do something else") { fixture =>
          assert(fixture === hello)
          pending
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val tp = rep.testPendingEventsReceived
      assert(tp.size === 2)
    }
    "should generate a test failure if a Throwable, or an Error other than direct Error subtypes " +
            "known in JDK 1.5, excluding AssertionError" in {
      val a = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        val hello = "Hello, world!"
        def withFixture(test: OneArgTest): Outcome = {
          test(hello)
        }
        it("throws AssertionError") { s => throw new AssertionError }
        it("throws plain old Error") { s => throw new Error }
        it("throws Throwable") { s => throw new Throwable }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep))
      val tf = rep.testFailedEventsReceived
      assert(tf.size === 3)
    }
    // SKIP-SCALATESTJS,NATIVE-START
    "should propagate out Errors that are direct subtypes of Error in JDK 1.5, other than " +
            "AssertionError, causing Suites and Runs to abort." in {
      val a = new funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        val hello = "Hello, world!"
        def withFixture(test: OneArgTest): Outcome = {
          test(hello)
        }
        it("throws AssertionError") { s => throw new OutOfMemoryError }
      }
      intercept[OutOfMemoryError] {
        a.run(None, Args(SilentReporter))
      }
    }
    // SKIP-SCALATESTJS,NATIVE-END
/*
    it("should send InfoProvided events with aboutAPendingTest set to true for info " +
            "calls made from a test that is pending") {
      val a = new fixture.FunSpec with GivenWhenThen {
        type FixtureParam = String
        val hello = "Hello, world!"
        def withFixture(test: OneArgTest): Outcome = {
          test(hello)
        }
        it("should do something else") { s =>
          given("two integers")
          when("one is subracted from the other")
          then("the result is the difference between the two numbers")
          pending
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
    }
    it("should send InfoProvided events with aboutAPendingTest set to false for info " +
            "calls made from a test that is not pending") {
      val a = new fixture.FunSpec with GivenWhenThen {
        type FixtureParam = String
        val hello = "Hello, world!"
        def withFixture(test: OneArgTest): Outcome = {
          test(hello)
        }
        it("should do something else") { s =>
          given("two integers")
          when("one is subracted from the other")
          then("the result is the difference between the two numbers")
          assert(1 + 1 === 2)
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
    }
*/
    "should allow both tests that take fixtures and tests that don't" in {
      class SpecA extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = {
          test("Hello, world!")
        }

        var takesNoArgsInvoked = false
        it("take no args") { () => takesNoArgsInvoked = true; /* ASSERTION_SUCCEED */ }

        var takesAFixtureInvoked = false
        it("takes a fixture") { s => takesAFixtureInvoked = true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      a.run(None, Args(SilentReporter))
      assert(a.testNames.size === 2, a.testNames)
      assert(a.takesNoArgsInvoked)
      assert(a.takesAFixtureInvoked)
    }
    "should work with test functions whose inferred result type is not Unit" in {
      class SpecA extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = {
          test("Hello, world!")
        }

        var takesNoArgsInvoked = false
        it("should take no args") { () => takesNoArgsInvoked = true; true; /* ASSERTION_SUCCEED */ }

        var takesAFixtureInvoked = false
        it("should take a fixture") { s => takesAFixtureInvoked = true; true; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      assert(!a.takesNoArgsInvoked)
      assert(!a.takesAFixtureInvoked)
      a.run(None, Args(SilentReporter))
      assert(a.testNames.size === 2, a.testNames)
      assert(a.takesNoArgsInvoked)
      assert(a.takesAFixtureInvoked)
    }
    "should work with ignored tests whose inferred result type is not Unit" in {
      class SpecA extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        ignore("should test this") { () => theTestThisCalled = true; "hi"; /* ASSERTION_SUCCEED */ }
        ignore("should test that") { fixture => theTestThatCalled = true; 42; /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      assert(!a.theTestThisCalled)
      assert(!a.theTestThatCalled)
      val reporter = new EventRecordingReporter
      a.run(None, Args(reporter))
      assert(reporter.testIgnoredEventsReceived.size === 2)
      assert(!a.theTestThisCalled)
      assert(!a.theTestThatCalled)
    }
    "should pass a NoArgTest to withFixture for tests that take no fixture" in {
      class MySpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var aNoArgTestWasPassed = false
        var aOneArgTestWasPassed = false
        override def withFixture(test: NoArgTest): Outcome = {
          aNoArgTestWasPassed = true
          Succeeded
        }
        def withFixture(test: OneArgTest): Outcome = {
          aOneArgTestWasPassed = true
          Succeeded
        }
        it("something") { () =>
          assert(1 + 1 === 2)
        }
      }

      val s = new MySpec
      s.run(None, Args(SilentReporter))
      assert(s.aNoArgTestWasPassed)
      assert(!s.aOneArgTestWasPassed)
    }
    "should not pass a NoArgTest to withFixture for tests that take a Fixture" in {
      class MySpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var aNoArgTestWasPassed = false
        var aOneArgTestWasPassed = false
        override def withFixture(test: NoArgTest): Outcome = {
          aNoArgTestWasPassed = true
          Succeeded
        }
        def withFixture(test: OneArgTest): Outcome = {
          aOneArgTestWasPassed = true
          Succeeded
        }
        it("something") { fixture =>
          assert(1 + 1 === 2)
        }
      }

      val s = new MySpec
      s.run(None, Args(SilentReporter))
      assert(!s.aNoArgTestWasPassed)
      assert(s.aOneArgTestWasPassed)
    }
    "should pass a NoArgTest that invokes the no-arg test when the " +
            "NoArgTest's no-arg apply method is invoked" in {

      class MySpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var theNoArgTestWasInvoked = false
        def withFixture(test: OneArgTest): Outcome = {
          // Shouldn't be called, but just in case don't invoke a OneArgTest
          Succeeded
        }
        it("something") { () =>
          theNoArgTestWasInvoked = true
          /* ASSERTION_SUCCEED */
        }
      }

      val s = new MySpec
      s.run(None, Args(SilentReporter))
      assert(s.theNoArgTestWasInvoked)
    }
    "should pass the correct test name in the OneArgTest passed to withFixture" in {
      class SpecA extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var correctTestNameWasPassed = false
        def withFixture(test: OneArgTest): Outcome = {
          correctTestNameWasPassed = test.name == "should do something"
          test("hi")
        }
        it("should do something") { fixture => /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      a.run(None, Args(SilentReporter))
      assert(a.correctTestNameWasPassed)
    }
    "should pass the correct config map in the OneArgTest passed to withFixture" in {
      class SpecA extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var correctConfigMapWasPassed = false
        def withFixture(test: OneArgTest): Outcome = {
          correctConfigMapWasPassed = (test.configMap == ConfigMap("hi" -> 7))
          test("hi")
        }
        it("should do something") { fixture => /* ASSERTION_SUCCEED */ }
      }
      val a = new SpecA

      import scala.language.reflectiveCalls

      a.run(None, Args(SilentReporter, Stopper.default, Filter(), ConfigMap("hi" -> 7), None, new Tracker(), Set.empty))
      assert(a.correctConfigMapWasPassed)
    }
    "(when a nesting rule has been violated)" - {

      "should, if they call a describe from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = { test("hi") }
          it("should blow up") { fixture =>
            describe("in the wrong place, at the wrong time") {
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a describe with a nested it from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = { test("hi") }
          it("should blow up") { fixture =>
            describe("in the wrong place, at the wrong time") {
              it("should never run") { fixture =>
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested it from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = { test("hi") }
          it("should blow up") { fixture =>
            it("should never run") { fixture =>
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested it with tags from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = { test("hi") }
          it("should blow up") { fixture =>
            it("should never run", mytags.SlowAsMolasses) { fixture =>
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a describe with a nested ignore from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = { test("hi") }
          it("should blow up") { fixture =>
            describe("in the wrong place, at the wrong time") {
              ignore("should never run") { fixture =>
                assert(1 === 1)
              }
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested ignore from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = { test("hi") }
          it("should blow up") { fixture =>
            ignore("should never run") { fixture =>
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested ignore with tags from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends funspec.FixtureAnyFunSpec {
          type FixtureParam = String
          def withFixture(test: OneArgTest): Outcome = { test("hi") }
          it("should blow up") { fixture =>
            ignore("should never run", mytags.SlowAsMolasses) { fixture =>
              assert(1 === 1)
            }
            /* ASSERTION_SUCCEED */
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
    }
    "should support expectations" ignore { // Unignore after we uncomment the expectation implicits in RegistrationPolicy
      class TestSpec extends funspec.FixtureAnyFunSpec with expectations.Expectations {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        it("fail scenario") { () =>
          expect(1 === 2); /* ASSERTION_SUCCEED */
        }
        describe("a feature") {
          it("nested fail scenario") { fixture =>
            expect(1 === 2); /* ASSERTION_SUCCEED */
          }
        }
      }
      val rep = new EventRecordingReporter
      val s1 = new TestSpec
      s1.run(None, Args(rep))
      assert(rep.testFailedEventsReceived.size === 2)
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "FixtureAnyFunSpecSpec.scala")
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 13)
      assert(rep.testFailedEventsReceived(1).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "FixtureAnyFunSpecSpec.scala")
      assert(rep.testFailedEventsReceived(1).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 11)
    }
  }
  
  "when failure happens" - {
    
    "should fire TestFailed event with correct stack depth info when test failed" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        def withFixture(test: OneArgTest): Outcome = { test("hi") }
        it("fail scenario") { fixture =>
          assert(1 === 2)
        }
        describe("a feature") {
          it("nested fail scenario") { fixture =>
            assert(1 === 2)
          }
        }
      }
      val rep = new EventRecordingReporter
      val s1 = new TestSpec
      s1.run(None, Args(rep))
      assert(rep.testFailedEventsReceived.size === 2)
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "FixtureAnyFunSpecSpec.scala")
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 13)
      assert(rep.testFailedEventsReceived(1).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "FixtureAnyFunSpecSpec.scala")
      assert(rep.testFailedEventsReceived(1).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 11)
    }
    
    "should generate TestRegistrationClosedException with correct stack depth info when has a it nested inside a it" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var registrationClosedThrown = false
        describe("a feature") {
          it("a scenario") { fixture =>
            it("nested scenario") { fixture =>
              assert(1 === 2)
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: OneArgTest): Outcome = {
          val outcome = test.apply("hi")
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
      assert("FixtureAnyFunSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("An it clause may not appear inside another it or they clause."))
    }

    "should generate TestRegistrationClosedException with correct stack depth info when has a ignore nested inside a it" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var registrationClosedThrown = false
        describe("a feature") {
          it("a scenario") { fixture =>
            ignore("nested scenario") { fixture =>
              assert(1 === 2)
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: OneArgTest): Outcome = {
          val outcome = test.apply("hi")
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
      assert("FixtureAnyFunSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("An ignore clause may not appear inside an it or a they clause."))
    }

    "should generate TestRegistrationClosedException with correct stack depth info when has a they nested inside a they" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var registrationClosedThrown = false
        describe("a feature") {
          they("a scenario") { fixture =>
            they("nested scenario") { fixture =>
              assert(1 === 2)
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: OneArgTest): Outcome = {
          val outcome = test.apply("hi")
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
      assert("FixtureAnyFunSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("A they clause may not appear inside another it or they clause."))
    }

    "should generate TestRegistrationClosedException with correct stack depth info when has a ignore nested inside a they" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var registrationClosedThrown = false
        describe("a feature") {
          they("a scenario") { fixture =>
            ignore("nested scenario") { fixture =>
              assert(1 === 2)
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: OneArgTest): Outcome = {
          val outcome = test.apply("hi")
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
      assert("FixtureAnyFunSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("An ignore clause may not appear inside an it or a they clause."))
    }

    "should allow test registration with registerTest and registerIgnoredTest" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        val a = 1
        registerTest("test 1") { fixture =>
          val e = intercept[TestFailedException] {
            assert(a == 2)
          }
          assert(e.message == Some("1 did not equal 2"))
          assert(e.failedCodeFileName == Some("FixtureAnyFunSpecSpec.scala"))
          assert(e.failedCodeLineNumber == Some(thisLineNumber - 4))
        }
        registerTest("test 2") { fixture =>
          assert(a == 2)
        }
        registerTest("test 3") { fixture =>
          pending
        }
        registerTest("test 4") { fixture =>
          cancel()
        }
        registerIgnoredTest("test 5") { fixture =>
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

    "should generate TestRegistrationClosedException with correct stack depth info when has a registerTest nested inside a registerTest" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var registrationClosedThrown = false
        describe("a feature") {
          registerTest("a scenario") { fixture =>
            registerTest("nested scenario") { fixture =>
              assert(1 === 2)
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: OneArgTest): Outcome = {
          val outcome = test.apply("hi")
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
      assert("FixtureAnyFunSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("Test cannot be nested inside another test."))
    }

    "should generate TestRegistrationClosedException with correct stack depth info when has a registerIgnoredTest nested inside a registerTest" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        var registrationClosedThrown = false
        describe("a feature") {
          registerTest("a scenario") { fixture =>
            registerIgnoredTest("nested scenario") { fixture =>
              assert(1 === 2)
            }; /* ASSERTION_SUCCEED */
          }
        }
        override def withFixture(test: OneArgTest): Outcome = {
          val outcome = test.apply("hi")
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
      assert("FixtureAnyFunSpecSpec.scala" === trce.failedCodeFileName.get)
      assert(trce.failedCodeLineNumber.get === thisLineNumber - 24)
      assert(trce.message == Some("Test cannot be nested inside another test."))
    }

    "should generate NotAllowedException wrapping a TestFailedException when assert fails in scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          val a = 1
          assert(a == 2)
        }
      }
      val e = intercept[NotAllowedException] {
        new TestSpec
      }
      assert("FixtureAnyFunSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotDescribeClause))

      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(causeThrowable.isInstanceOf[TestFailedException])
      val cause = causeThrowable.asInstanceOf[TestFailedException]
      assert("FixtureAnyFunSpecSpec.scala" == cause.failedCodeFileName.get)
      assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
      assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
    }

    "should generate NotAllowedException wrapping a TestCanceledException when assume fails in scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          val a = 1
          assume(a == 2)
        }
      }
      val e = intercept[NotAllowedException] {
        new TestSpec
      }
      assert("FixtureAnyFunSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.message == Some(FailureMessages.assertionShouldBePutInsideItOrTheyClauseNotDescribeClause))

      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(causeThrowable.isInstanceOf[TestCanceledException])
      val cause = causeThrowable.asInstanceOf[TestCanceledException]
      assert("FixtureAnyFunSpecSpec.scala" == cause.failedCodeFileName.get)
      assert(cause.failedCodeLineNumber.get == thisLineNumber - 15)
      assert(cause.message == Some(FailureMessages.didNotEqual(prettifier, 1, 2)))
    }

    "should generate NotAllowedException wrapping a non-fatal RuntimeException is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new RuntimeException("on purpose")
        }
      }
      val e = intercept[NotAllowedException] {
        new TestSpec
      }
      assert("FixtureAnyFunSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 8)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInDescribeClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", "on purpose")))

      assert(causeThrowable.isInstanceOf[RuntimeException])
      val cause = causeThrowable.asInstanceOf[RuntimeException]
      assert(cause.getMessage == "on purpose")
    }

    "should generate NotAllowedException wrapping a DuplicateTestNameException is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {
        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")
        describe("a feature") {
          it("test 1") { fixture => /* ASSERTION_SUCCEED */}
          it("test 1") { fixture => /* ASSERTION_SUCCEED */}
        }
      }
      val e = intercept[NotAllowedException] {
        new TestSpec
      }
      assert("FixtureAnyFunSpecSpec.scala" == e.failedCodeFileName.get)
      assert(e.failedCodeLineNumber.get == thisLineNumber - 7)
      assert(e.cause.isDefined)
      val causeThrowable = e.cause.get
      assert(e.message == Some(FailureMessages.exceptionWasThrownInDescribeClause(prettifier, UnquotedString(causeThrowable.getClass.getName), "a feature", FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature test 1")))))

      assert(causeThrowable.isInstanceOf[DuplicateTestNameException])
      val cause = causeThrowable.asInstanceOf[DuplicateTestNameException]
      assert(cause.getMessage == FailureMessages.duplicateTestName(prettifier, UnquotedString("a feature test 1")))
    }

    // SKIP-SCALATESTJS,NATIVE-START
    "should propagate AnnotationFormatError when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new AnnotationFormatError("on purpose")
        }
      }
      val e = intercept[AnnotationFormatError] {
        new TestSpec
      }
      assert(e.getMessage == "on purpose")
    }

    "should propagate AWTError when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new AWTError("on purpose")
        }
      }
      val e = intercept[AWTError] {
        new TestSpec
      }
      assert(e.getMessage == "on purpose")
    }

    "should propagate CoderMalfunctionError when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new CoderMalfunctionError(new RuntimeException("on purpose"))
        }
      }
      val e = intercept[CoderMalfunctionError] {
        new TestSpec
      }
      assert(e.getMessage == "java.lang.RuntimeException: on purpose")
    }

    "should propagate FactoryConfigurationError when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new FactoryConfigurationError("on purpose")
        }
      }
      val e = intercept[FactoryConfigurationError] {
        new TestSpec
      }
      assert(e.getMessage == "on purpose")
    }

    "should propagate LinkageError when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new LinkageError("on purpose")
        }
      }
      val e = intercept[LinkageError] {
        new TestSpec
      }
      assert(e.getMessage == "on purpose")
    }

    "should propagate ThreadDeath when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new ThreadDeath
        }
      }
      val e = intercept[ThreadDeath] {
        new TestSpec
      }
      assert(e.getMessage == null)
    }

    "should propagate TransformerFactoryConfigurationError when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new TransformerFactoryConfigurationError("on purpose")
        }
      }
      val e = intercept[TransformerFactoryConfigurationError] {
        new TestSpec
      }
      assert(e.getMessage == "on purpose")
    }

    "should propagate VirtualMachineError when it is thrown inside scope" in {
      class TestSpec extends funspec.FixtureAnyFunSpec {

        type FixtureParam = String
        override def withFixture(test: OneArgTest): Outcome = test("test")

        describe("a feature") {
          throw new VirtualMachineError("on purpose") {}
        }
      }
      val e = intercept[VirtualMachineError] {
        new TestSpec
      }
      assert(e.getMessage == "on purpose")
    }
    // SKIP-SCALATESTJS,NATIVE-END
  }
}
