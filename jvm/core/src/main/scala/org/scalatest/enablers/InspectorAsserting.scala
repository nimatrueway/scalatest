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
package org.scalatest.enablers

import org.scalatest._
import org.scalatest.exceptions._
import org.scalactic.{source, Prettifier}
import scala.annotation.tailrec
import scala.collection.GenTraversable
import StackDepthExceptionHelper.getStackDepth
import Suite.indentLines
import org.scalatest.FailureMessages.decorateToStringValue
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Try, Success, Failure}

/**
 * Supertrait for <code>InspectorAsserting</code> typeclasses, which are used to implement and determine the result
 * type of <a href="../Inspectors.html"><code>Inspectors</code></a> methods such as <code>forAll</code>, <code>forBetween</code>, <em>etc</em>.
 *
 * <p>
 * Currently, an inspector expression will have result type <code>Assertion</code>, if the function passed has result type <code>Assertion</code>,
 * else it will have result type <code>Unit</code>.
 * </p>
 */
trait InspectorAsserting[T] {
  type Result

  /**
   * Implementation method for <code>Inspectors</code> <code>forAll</code> syntax.
   */
  def forAll[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result

  /**
   * Implementation method for <code>Inspectors</code> <code>forAtLeast</code> syntax.
   */
  def forAtLeast[E](min: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result

  /**
   * Implementation method for <code>Inspectors</code> <code>forAtMost</code> syntax.
   */
  def forAtMost[E](max: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result

  /**
   * Implementation method for <code>Inspectors</code> <code>forExactly</code> syntax.
   */
  def forExactly[E](succeededCount: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result

  /**
   * Implementation method for <code>Inspectors</code> <code>forNo</code> syntax.
   */
  def forNo[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result

  /**
   * Implementation method for <code>Inspectors</code> <code>forBetween</code> syntax.
   */
  def forBetween[E](from: Int, upTo: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result

  /**
   * Implementation method for <code>Inspectors</code> <code>forEvery</code> syntax.
   */
  def forEvery[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result
}

/**
 * Class holding lowest priority <code>InspectorAsserting</code> implicit, which enables inspector expressions that have result type <code>Unit</code>.
 */
abstract class UnitInspectorAsserting {

  /**
   * Abstract subclass of <code>InspectorAsserting</code> that provides the bulk of the implementations of <code>InspectorAsserting</code>
   * methods.
   */
  abstract class InspectorAssertingImpl[T] extends InspectorAsserting[T] {

    import InspectorAsserting._

    // Inherit Scaladoc for now. See later if can just make this implementation class private[scalatest].
    def forAll[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result = {
      val xsIsMap = isMap(original)
      val result =
        runFor(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.failedElements.length > 0)
      if (result.failedElements.length > 0)
        indicateFailure(
          if (shorthand)
            Resources.allShorthandFailed(indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
          else
            Resources.forAllFailed(indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original)),
          Some(result.failedElements(0)._3),
          pos,
          result.failedElements.flatMap(_._4)
        )
      else indicateSuccess("forAll succeeded")
    }

    def forAtLeast[E](min: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result = {
      @tailrec
      def forAtLeastAcc(itr: Iterator[E], includeIndex: Boolean, index: Int, passedCount: Int, messageAcc: IndexedSeq[String]): (Int, IndexedSeq[String]) = {
        if (itr.hasNext) {
          val head = itr.next
          val (newPassedCount, newMessageAcc) =
            try {
              fun(head)
              (passedCount + 1, messageAcc)
            }
            catch {
              case e if !shouldPropagate(e) =>
                val xsIsMap = isMap(original)
                val messageKey = head match {
                  case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
                  case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
                  case _ => index.toString
                }
                (passedCount, messageAcc :+ createMessage(messageKey, e, xsIsMap))
            }
          if (newPassedCount < min)
            forAtLeastAcc(itr, includeIndex, index + 1, newPassedCount, newMessageAcc)
          else
            (newPassedCount, newMessageAcc)
        }
        else
          (passedCount, messageAcc)
      }

      if (min <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'min'"))

      val (passedCount, messageAcc) = forAtLeastAcc(xs.toIterator, xs.isInstanceOf[Seq[E]], 0, 0, IndexedSeq.empty)
      if (passedCount < min)
        indicateFailure(
          if (shorthand)
            if (passedCount > 0)
              Resources.atLeastShorthandFailed(min.toString, elementLabel(passedCount), indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else
              Resources.atLeastShorthandFailedNoElement(min.toString, indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
          else
            if (passedCount > 0)
              Resources.forAtLeastFailed(min.toString, elementLabel(passedCount), indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else
              Resources.forAtLeastFailedNoElement(min.toString, indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original)),
          None,
          pos
        )
      else indicateSuccess("forAtLeast succeeded")
    }

    def forAtMost[E](max: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result = {
      if (max <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'max'"))

      val xsIsMap = isMap(original)
      val result =
        runFor(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount > max)
      if (result.passedCount > max)
        indicateFailure(
          if (shorthand)
            Resources.atMostShorthandFailed(max.toString, result.passedCount.toString, keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
          else
            Resources.forAtMostFailed(max.toString, result.passedCount.toString, keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original)),
          None,
          pos
        )
      else indicateSuccess("forAtMost succeeded")
    }

    def forExactly[E](succeededCount: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result = {
      if (succeededCount <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'succeededCount'"))

      val xsIsMap = isMap(original)
      val result =
        runFor(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount > succeededCount)
      if (result.passedCount != succeededCount)
        indicateFailure(
          if (shorthand)
            if (result.passedCount == 0)
              Resources.exactlyShorthandFailedNoElement(succeededCount.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else {
              if (result.passedCount < succeededCount)
                Resources.exactlyShorthandFailedLess(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else
                Resources.exactlyShorthandFailedMore(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            }
          else
            if (result.passedCount == 0)
              Resources.forExactlyFailedNoElement(succeededCount.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else {
              if (result.passedCount < succeededCount)
                Resources.forExactlyFailedLess(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else
                Resources.forExactlyFailedMore(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            },
          None,
          pos
        )
      else indicateSuccess("forExactly succeeded")
    }

    def forNo[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result = {
      val xsIsMap = isMap(original)
      val result =
        runFor(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount != 0)
      if (result.passedCount != 0)
        indicateFailure(
          if (shorthand)
            Resources.noShorthandFailed(keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
          else
            Resources.forNoFailed(keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original)),
          None,
          pos
        )
      else indicateSuccess("forNo succeeded")
    }

    def forBetween[E](from: Int, upTo: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result = {
      if (from < 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanEqualZero("'from'"))
      if (upTo <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'upTo'"))
      if (upTo <= from)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThan("'upTo'", "'from'"))

      val xsIsMap = isMap(original)
      val result =
        runFor(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount > upTo)
      if (result.passedCount < from || result.passedCount > upTo)
        indicateFailure(
          if (shorthand)
            if (result.passedCount == 0)
              Resources.betweenShorthandFailedNoElement(from.toString, upTo.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else {
              if (result.passedCount < from)
                Resources.betweenShorthandFailedLess(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else
                Resources.betweenShorthandFailedMore(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            }
          else
            if (result.passedCount == 0)
              Resources.forBetweenFailedNoElement(from.toString, upTo.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else {
              if (result.passedCount < from)
                Resources.forBetweenFailedLess(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else
                Resources.forBetweenFailedMore(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            },
          None,
          pos
        )
      else indicateSuccess("forBetween succeeded")
    }

    def forEvery[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => T): Result = {
      @tailrec
      def runAndCollectErrorMessage[E](itr: Iterator[E], messageList: IndexedSeq[String], index: Int)(fun: E => T): IndexedSeq[String] = {
        if (itr.hasNext) {
          val head = itr.next
          val newMessageList =
            try {
              fun(head)
              messageList
            }
            catch {
              case e if !shouldPropagate(e) =>
                val xsIsMap = isMap(original)
                val messageKey = head match {
                  case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
                  case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
                  case _ => index.toString
                }
                messageList :+ createMessage(messageKey, e, xsIsMap)
            }

          runAndCollectErrorMessage(itr, newMessageList, index + 1)(fun)
        }
        else
          messageList
      }
      val messageList = runAndCollectErrorMessage(xs.toIterator, IndexedSeq.empty, 0)(fun)
      if (messageList.size > 0)
        indicateFailure(
          if (shorthand)
            Resources.everyShorthandFailed(indentErrorMessages(messageList).mkString(", \n"), decorateToStringValue(prettifier, original))
          else
            Resources.forEveryFailed(indentErrorMessages(messageList).mkString(", \n"), decorateToStringValue(prettifier, original)),
          None,
          pos
        )
      else indicateSuccess("forEvery succeeded")
    }

    // TODO: Why is this a by-name? Well, I made it a by-name because it was one in MatchersHelper.
    // Why is it a by-name there?
    // CS: because we want to construct the message lazily.
    private[scalatest] def indicateSuccess(message: => String): Result

    private[scalatest] def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: source.Position): Result

    private[scalatest] def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: source.Position, analysis: scala.collection.immutable.IndexedSeq[String]): Result
  }

  /**
   * Provides an implicit <code>InspectorAsserting</code> instance for any type that did not match a
   * higher priority implicit provider, enabling inspector syntax that has result type <code>Unit</code>.
   */
  implicit def assertingNatureOfT[T]: InspectorAsserting[T] { type Result = Unit } =
    new InspectorAssertingImpl[T] {
      type Result = Unit
      def indicateSuccess(message: => String): Unit = ()
      def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: source.Position): Unit = {
        val msg: String = message
        throw new TestFailedException(
          (_: StackDepthException) => Some(msg),
          optionalCause,
          pos
        )
      }
      def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: source.Position, analysis: scala.collection.immutable.IndexedSeq[String]): Unit = {
        val msg: String = message
        throw new TestFailedException(
          (_: StackDepthException) => Some(msg),
          optionalCause,
          Left(pos),
          None,
          analysis
        )
      }
    }
}

/**
 * Abstract class that in the future will hold an intermediate priority <code>InspectorAsserting</code> implicit, which will enable inspector expressions
 * that have result type <code>Expectation</code>, a more composable form of assertion that returns a result instead of throwing an exception when it fails.
 */
abstract class ExpectationInspectorAsserting extends UnitInspectorAsserting {

  private[scalatest] implicit def assertingNatureOfExpectation(implicit prettifier: Prettifier): InspectorAsserting[Expectation] { type Result = Expectation } = {
    new InspectorAssertingImpl[Expectation] {
      type Result = Expectation
      def indicateSuccess(message: => String): Expectation = Fact.Yes(message)(prettifier)
      def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: org.scalactic.source.Position): Expectation = Fact.No(message)(prettifier)
      def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: source.Position, analysis: scala.collection.immutable.IndexedSeq[String]): Expectation = Fact.No(message)(prettifier)
    }
  }
}

/**
 * Companion object to <code>InspectorAsserting</code> that provides two implicit providers, a higher priority one for passed functions that have result
 * type <code>Assertion</code>, which also yields result type <code>Assertion</code>, and one for any other type, which yields result type <code>Unit</code>.
 */
object InspectorAsserting extends ExpectationInspectorAsserting {

  /**
   * Provides an implicit <code>InspectorAsserting</code> instance for type <code>Assertion</code>,
   * enabling inspector syntax that has result type <code>Assertion</code>.
   */
  implicit def assertingNatureOfAssertion: InspectorAsserting[Assertion] { type Result = Assertion } =
    new InspectorAssertingImpl[Assertion] {
      type Result = Assertion
      def indicateSuccess(message: => String): Assertion = Succeeded
      def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: source.Position): Assertion = {
        val msg: String = message
        throw new TestFailedException(
          (_: StackDepthException) => Some(msg),
          optionalCause,
          pos
        )
      }
      def indicateFailure(message: => String, optionalCause: Option[Throwable], pos: source.Position, analysis: scala.collection.immutable.IndexedSeq[String]): Assertion = {
        val msg: String = message
        throw new TestFailedException(
          (_: StackDepthException) => Some(msg),
          optionalCause,
          Left(pos),
          None,
          analysis
        )
      }
    }

  /**
    * Abstract subclass of <code>InspectorAsserting</code> that provides the bulk of the implementations of <code>InspectorAsserting</code>
    * methods.
    */
  private[scalatest] abstract class FutureInspectorAssertingImpl[T] extends InspectorAsserting[Future[T]] {

    type Result = Future[T]

    implicit def executionContext: ExecutionContext

    // Inherit Scaladoc for now. See later if can just make this implementation class private[scalatest].
    def forAll[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => Future[T]): Result = {
      val xsIsMap = isMap(original)
      val future = runAsyncSerial(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.failedElements.length > 0)
      future.map { result =>
        if (result.failedElements.length > 0)
          indicateFailureFuture(
            if (shorthand)
              Resources.allShorthandFailed(indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else
              Resources.forAllFailed(indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original)),
            Some(result.failedElements(0)._3),
            pos
          )
        else indicateSuccessFuture("forAll succeeded")
      }
    }

    def forAtLeast[E](min: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => Future[T]): Result = {
      def forAtLeastAcc(itr: Iterator[E], includeIndex: Boolean, index: Int, passedCount: Int, messageAcc: IndexedSeq[String]): Future[(Int, IndexedSeq[String])] = {
        if (itr.hasNext) {
          val head = itr.next
          try {
            val future = fun(head)
            future.map { r =>
              (passedCount + 1, messageAcc)
            } recover {
              case execEx: java.util.concurrent.ExecutionException if shouldPropagate(execEx.getCause) => throw execEx.getCause
              case e if !shouldPropagate(e) =>
                val xsIsMap = isMap(original)
                val messageKey = head match {
                  case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
                  case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
                  case _ => index.toString
                }
                (passedCount, messageAcc :+ createMessage(messageKey, e, xsIsMap))
              case other => throw other
            } flatMap { result =>
              val (newPassedCount, newMessageAcc) = result
              if (newPassedCount < min)
                forAtLeastAcc(itr, includeIndex, index + 1, newPassedCount, newMessageAcc)
              else
                Future.successful((newPassedCount, newMessageAcc))
            }
          }
          catch {
            case e if !shouldPropagate(e) =>
              val xsIsMap = isMap(original)
              val messageKey = head match {
                case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
                case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
                case _ => index.toString
              }
              val (newPassedCount, newMessageAcc) = (passedCount, messageAcc :+ createMessage(messageKey, e, xsIsMap))
              if (newPassedCount < min)
                forAtLeastAcc(itr, includeIndex, index + 1, newPassedCount, newMessageAcc)
              else
                Future.successful((newPassedCount, newMessageAcc))

            case other => Future { throw other}
          }
        }
        else
          Future.successful((passedCount, messageAcc))
      }

      if (min <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'min'"))

      val resultFuture = forAtLeastAcc(xs.toIterator, xs.isInstanceOf[Seq[E]], 0, 0, IndexedSeq.empty)
      resultFuture.map { result =>
        val (passedCount, messageAcc) = result
        if (passedCount < min)
          indicateFailureFuture(
            if (shorthand)
              if (passedCount > 0)
                Resources.atLeastShorthandFailed(min.toString, elementLabel(passedCount), indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else
                Resources.atLeastShorthandFailedNoElement(min.toString, indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else
            if (passedCount > 0)
              Resources.forAtLeastFailed(min.toString, elementLabel(passedCount), indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else
              Resources.forAtLeastFailedNoElement(min.toString, indentErrorMessages(messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original)),
            None,
            pos
          )
        else indicateSuccessFuture("forAtLeast succeeded")
      }
    }

    def forAtMost[E](max: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => Future[T]): Result = {
      if (max <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'max'"))

      val xsIsMap = isMap(original)
      val future = runAsyncSerial(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount > max)
      future.map { result =>
        if (result.passedCount > max)
          indicateFailureFuture(
            if (shorthand)
              Resources.atMostShorthandFailed(max.toString, result.passedCount.toString, keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            else
              Resources.forAtMostFailed(max.toString, result.passedCount.toString, keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original)),
            None,
            pos
          )
        else indicateSuccessFuture("forAtMost succeeded")
      }
    }

    def forExactly[E](succeededCount: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => Future[T]): Result = {
      if (succeededCount <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'succeededCount'"))

      val xsIsMap = isMap(original)
      val future = runAsyncSerial(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount > succeededCount)
      future.map { result =>
        if (result.passedCount != succeededCount) {
          indicateFailureFuture(
            if (shorthand)
              if (result.passedCount == 0)
                Resources.exactlyShorthandFailedNoElement(succeededCount.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else {
                if (result.passedCount < succeededCount)
                  Resources.exactlyShorthandFailedLess(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
                else
                  Resources.exactlyShorthandFailedMore(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
              }
            else if (result.passedCount == 0)
              Resources.forExactlyFailedNoElement(succeededCount.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else {
              if (result.passedCount < succeededCount)
                Resources.forExactlyFailedLess(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else
                Resources.forExactlyFailedMore(succeededCount.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            },
            None,
            pos
          )
        }
        else indicateSuccessFuture("forExactly succeeded")
      }
    }

    def forNo[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => Future[T]): Result = {
      val xsIsMap = isMap(original)
      val future = runAsyncSerial(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount != 0)
      future.map { result =>
        if (result.passedCount != 0)
          indicateFailureFuture(
            if (shorthand)
              Resources.noShorthandFailed(keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            else
              Resources.forNoFailed(keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original)),
            None,
            pos
          )
        else indicateSuccessFuture("forNo succeeded")
      }
    }

    def forBetween[E](from: Int, upTo: Int, xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => Future[T]): Result = {
      if (from < 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanEqualZero("'from'"))
      if (upTo <= 0)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThanZero("'upTo'"))
      if (upTo <= from)
        throw new IllegalArgumentException(Resources.forAssertionsMoreThan("'upTo'", "'from'"))

      val xsIsMap = isMap(original)
      val future = runAsyncSerial(xs.toIterator, xsIsMap, 0, new ForResult[E], fun, _.passedCount > upTo)

      future.map { result =>
        if (result.passedCount < from || result.passedCount > upTo)
          indicateFailureFuture(
            if (shorthand)
              if (result.passedCount == 0)
                Resources.betweenShorthandFailedNoElement(from.toString, upTo.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else {
                if (result.passedCount < from)
                  Resources.betweenShorthandFailedLess(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
                else
                  Resources.betweenShorthandFailedMore(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
              }
            else if (result.passedCount == 0)
              Resources.forBetweenFailedNoElement(from.toString, upTo.toString, indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else {
              if (result.passedCount < from)
                Resources.forBetweenFailedLess(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
              else
                Resources.forBetweenFailedMore(from.toString, upTo.toString, elementLabel(result.passedCount), keyOrIndexLabel(original, result.passedElements), decorateToStringValue(prettifier, original))
            },
            None,
            pos
          )
        else indicateSuccessFuture("forBetween succeeded")
      }
    }

    def forEvery[E](xs: GenTraversable[E], original: Any, shorthand: Boolean, prettifier: Prettifier, pos: source.Position)(fun: E => Future[T]): Result = {
      val xsIsMap = isMap(original)
      val future = runAsyncParallel(xs, xsIsMap, fun)(executionContext)
      future.map { result =>
        if (result.failedElements.length > 0)
          indicateFailureFuture(
            if (shorthand)
              Resources.everyShorthandFailed(indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original))
            else
              Resources.forEveryFailed(indentErrorMessages(result.messageAcc).mkString(", \n"), decorateToStringValue(prettifier, original)),
            Some(result.failedElements(0)._3),
            pos
          )
        else indicateSuccessFuture("forAll succeeded")
      }
    }

    // TODO: Why is this a by-name? Well, I made it a by-name because it was one in MatchersHelper.
    // Why is it a by-name there?
    // CS: because we want to construct the message lazily.
    private[scalatest] def indicateSuccessFuture(message: => String): T

    private[scalatest] def indicateFailureFuture(message: => String, optionalCause: Option[Throwable], pos: source.Position): T
  }

  implicit def assertingNatureOfFutureAssertion(implicit execCtx: ExecutionContext): InspectorAsserting[Future[Assertion]] { type Result = Future[Assertion] } =
    new FutureInspectorAssertingImpl[Assertion] {
      val executionContext = execCtx
      def indicateSuccessFuture(message: => String): Assertion = Succeeded
      def indicateFailureFuture(message: => String, optionalCause: Option[Throwable], pos: source.Position): Assertion = {
        val msg: String = message
        optionalCause match {
          case Some(ex: java.util.concurrent.ExecutionException) if shouldPropagate(ex.getCause) =>
            throw ex.getCause

          case other =>
            other match {
              case Some(ex) if shouldPropagate(ex) => throw ex
              case _ =>
                throw new TestFailedException(
                  (_: StackDepthException) => Some(msg),
                  optionalCause,
                  pos
                )
            }
        }
      }
    }

  private[scalatest] final def indentErrorMessages(messages: IndexedSeq[String]) = indentLines(1, messages)

  private[scalatest] final def isMap(xs: Any): Boolean =
    xs match {
      case _: collection.GenMap[_, _] => true
      // SKIP-SCALATESTJS,NATIVE-START
      case _: java.util.Map[_, _] => true
      // SKIP-SCALATESTJS,NATIVE-END
      case _ => false
    }

  private[scalatest] final def shouldPropagate(throwable: Throwable): Boolean =
    throwable match {
      case _: NotAllowedException |
           _: TestPendingException |
           _: TestCanceledException => true
      case _ if Suite.anExceptionThatShouldCauseAnAbort(throwable) => true
      case _ => false
    }

  private[scalatest] final def createMessage(messageKey: String, t: Throwable, xsIsMap: Boolean): String =
    t match {
      case sde: StackDepthException =>
        sde.failedCodeFileNameAndLineNumberString match {
          case Some(failedCodeFileNameAndLineNumber) =>
            if (xsIsMap)
              Resources.forAssertionsGenMapMessageWithStackDepth(messageKey, sde.getMessage, failedCodeFileNameAndLineNumber)
            else
              Resources.forAssertionsGenTraversableMessageWithStackDepth(messageKey, sde.getMessage, failedCodeFileNameAndLineNumber)
          case None =>
            if (xsIsMap)
              Resources.forAssertionsGenMapMessageWithoutStackDepth(messageKey, sde.getMessage)
            else
              Resources.forAssertionsGenTraversableMessageWithoutStackDepth(messageKey, sde.getMessage)
        }
      case _ =>
        if (xsIsMap)
          Resources.forAssertionsGenMapMessageWithoutStackDepth(messageKey, if (t.getMessage != null) t.getMessage else "null")
        else
          Resources.forAssertionsGenTraversableMessageWithoutStackDepth(messageKey, if (t.getMessage != null) t.getMessage else "null")
    }

  private[scalatest] final def elementLabel(count: Int): String =
    if (count > 1) Resources.forAssertionsElements(count.toString) else Resources.forAssertionsElement(count.toString)

  private[scalatest] final case class ForResult[T](passedCount: Int = 0, messageAcc: IndexedSeq[String] = IndexedSeq.empty,
                          passedElements: IndexedSeq[(Int, T)] = IndexedSeq.empty, failedElements: scala.collection.immutable.IndexedSeq[(Int, T, Throwable, scala.collection.immutable.IndexedSeq[String])] = Vector.empty)

  @tailrec
  private[scalatest] final def runFor[T, ASSERTION](itr: Iterator[T], xsIsMap: Boolean, index:Int, result: ForResult[T], fun: T => ASSERTION, stopFun: ForResult[_] => Boolean): ForResult[T] = {
    if (itr.hasNext) {
      val head = itr.next
      val newResult =
        try {
          fun(head)
          result.copy(passedCount = result.passedCount + 1, passedElements = result.passedElements :+ (index, head))
        }
        catch {
          case tfe: TestFailedException =>
            val messageKey = head match {
              case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
              case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
              case _ => index.toString
            }
            result.copy(messageAcc = result.messageAcc :+ createMessage(messageKey, tfe, xsIsMap), failedElements = result.failedElements :+ (index, head, tfe, tfe.analysis))

          case e if !shouldPropagate(e) =>
            val messageKey = head match {
              case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
              case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
              case _ => index.toString
            }
            result.copy(messageAcc = result.messageAcc :+ createMessage(messageKey, e, xsIsMap), failedElements = result.failedElements :+ (index, head, e, Vector.empty))
        }
      if (stopFun(newResult))
        newResult
      else
        runFor(itr, xsIsMap, index + 1, newResult, fun, stopFun)
    }
    else
      result
  }

  private[scalatest] final def runAsyncParallel[T, ASSERTION](col: scala.collection.GenTraversable[T], xsIsMap: Boolean, fun: T => Future[ASSERTION])(implicit ctx: ExecutionContext): Future[ForResult[T]] = {
    val futCol: IndexedSeq[Future[(T, Int, Try[ASSERTION])]] =
      col.toIndexedSeq.zipWithIndex map { case (next, idx) =>
        try {
          fun(next) map { r =>
            (next, idx, Success(r))
          } recover {
            case e: Throwable => (next, idx, Failure(e))
          }
        }
        catch {
          case e if !shouldPropagate(e) =>
            Future.successful((next, idx, Failure(e)))
          case other =>
            Future { throw other }
        }
      }
    Future.sequence(futCol).map { col =>
      val (passedCol, failedCol) = col.partition { case (e, i, r) =>
        r match {
          case Success(_) => true
          case Failure(_) => false
        }
      }
      val messages =
        failedCol.map { case (e, i, f) =>
          val messageKey = e match {
            case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
            case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
            case _ => i.toString
          }
          createMessage(messageKey, f.asInstanceOf[Failure[_]].exception, xsIsMap)
        }

      ForResult(passedCol.length, messages,
                passedCol.map(e => (e._2, e._1)), failedCol.map{ e =>
        e._3.asInstanceOf[Failure[_]].exception match {
          case tfe: TestFailedException => (e._2, e._1, tfe, tfe.analysis)
          case other => (e._2, e._1, other, Vector.empty)
        }
      }.toVector)
    }
  }

  private[scalatest] final def runAsyncSerial[T, ASSERTION](itr: Iterator[T], xsIsMap: Boolean, index:Int, result: ForResult[T], fun: T => Future[ASSERTION], stopFun: ForResult[_] => Boolean)(implicit ctx: ExecutionContext): Future[ForResult[T]] = {
    if (itr.hasNext) {
      val head = itr.next
      try {
        val future = fun(head)
        future map { r =>
          result.copy(passedCount = result.passedCount + 1, passedElements = result.passedElements :+ (index, head))
        } recover {
          case execEx: java.util.concurrent.ExecutionException if shouldPropagate(execEx.getCause) =>
            throw execEx.getCause

          case e if !shouldPropagate(e) =>
            val messageKey = head match {
              case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
              case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
              case _ => index.toString
            }
            e match {
              case tfe: TestFailedException =>
                result.copy(messageAcc = result.messageAcc :+ createMessage(messageKey, tfe, xsIsMap), failedElements = result.failedElements :+ (index, head, tfe, tfe.analysis))

              case _ =>
                result.copy(messageAcc = result.messageAcc :+ createMessage(messageKey, e, xsIsMap), failedElements = result.failedElements :+ (index, head, e, Vector.empty))
            }

          case other =>
            throw other
        } flatMap { newResult =>
          if (stopFun(newResult))
            Future.successful(newResult)
          else
            runAsyncSerial(itr, xsIsMap, index + 1, newResult, fun, stopFun)
        }
      }
      catch {
        case e if !shouldPropagate(e) =>
          val messageKey = head match {
            case tuple: Tuple2[_, _] if xsIsMap => tuple._1.toString
            case entry: Entry[_, _] if xsIsMap => entry.getKey.toString
            case _ => index.toString
          }
          val newResult =
            e match {
              case tfe: TestFailedException =>
                result.copy(messageAcc = result.messageAcc :+ createMessage(messageKey, tfe, xsIsMap), failedElements = result.failedElements :+ (index, head, tfe, tfe.analysis))
              case _ =>
                result.copy(messageAcc = result.messageAcc :+ createMessage(messageKey, e, xsIsMap), failedElements = result.failedElements :+ (index, head, e, Vector.empty))
            }

          if (stopFun(newResult))
            Future.successful(newResult)
          else
            runAsyncSerial(itr, xsIsMap, index + 1, newResult, fun, stopFun)

        case other => Future { throw other }
      }
    }
    else {
      Future.successful(result)
    }
  }

  private[scalatest] final def keyOrIndexLabel(xs: Any, passedElements: IndexedSeq[(Int, _)]): String = {
    def makeAndLabel(indexes: IndexedSeq[Int]): String =
      if (indexes.length > 1)
        indexes.dropRight(1).mkString(", ") + " and " + indexes.last
      else
        indexes.mkString(", ")

    val (xsIsMap, elements) = xs match {
      // SKIP-SCALATESTJS,NATIVE-START
      case _: collection.GenMap[_, _] | _: java.util.Map[_, _] =>
        // SKIP-SCALATESTJS,NATIVE-END
        //SCALATESTJS,NATIVE-ONLY case _: collection.GenMap[_, _] =>
        val elements = passedElements.map{ case (index, e) =>
          e match {
            case tuple2: Tuple2[_, _] => tuple2._1
            // SKIP-SCALATESTJS,NATIVE-START
            case entry: java.util.Map.Entry[_, _] => entry.getKey
            // SKIP-SCALATESTJS,NATIVE-END
            case _ => index
          }
        }
        (true, elements)
      case _ =>
        (false, passedElements.map(_._1))
    }

    if (elements.length > 1)
      if (xsIsMap)
        Resources.forAssertionsKeyAndLabel(elements.dropRight(1).mkString(", "), elements.last.toString)
      else
        Resources.forAssertionsIndexAndLabel(elements.dropRight(1).mkString(", "), elements.last.toString)
    else
    if (xsIsMap)
      Resources.forAssertionsKeyLabel(elements.mkString(", "))
    else
      Resources.forAssertionsIndexLabel(elements.mkString(", "))
  }
}

