package com.kinja.config

import scalaz._, Scalaz._

/**
 * A Bootup Errors accumulator. It accumulates errors when applied (<*>).
 * It is also a monad (but does not accumulate errors in that case).
 */
final case class BootupErrors[A] private[BootupErrors] (run : Either[Seq[String], A]) extends AnyVal {
  def map[B](f : A ⇒ B) : BootupErrors[B] = BootupErrors(this.run.map(f))

  def <*>[B, C](that : BootupErrors[B])(implicit ev : <:<[A, B ⇒ C]) : BootupErrors[C] = BootupErrors {
    (this.run, that.run) match {
      case (Right(g), Right(k)) ⇒ Right(g(k))
      case (Left(e), Right(k))  ⇒ Left(e)
      case (Right(e), Left(k))  ⇒ Left(k)
      case (Left(e), Left(k))   ⇒ Left(e ++ k)
    }
  }

  def flatten[B](implicit ev : A <:< BootupErrors[B]) : BootupErrors[B] = this.flatMap(a ⇒ a)

  def flatMap[B](f : A ⇒ BootupErrors[B]) : BootupErrors[B] = BootupErrors(this.run.flatMap(f(_).run))

  def fold[B](err : Seq[String] ⇒ B, succ : A ⇒ B) : B = run.fold(err, succ)
  def getOrElse(e : Seq[String] ⇒ A) : A = this.fold(e, a ⇒ a)

  /**
   * Return all accumulated errors, if any
   */
  def errors : Seq[String] = run.fold(a ⇒ a, _ ⇒ Nil)

  /**
   * Ignore any errors in this BootupErrors and return None if there are any.
   * Useful for optionally selecting things from config files.
   */
  def optional : BootupErrors[Option[A]] = BootupErrors(this.fold(_ ⇒ None, Some(_)))
}

object BootupErrors {
  def apply[A](a : A) : BootupErrors[A] = BootupErrors(Right(a))
  def failed[A](err : String) : BootupErrors[A] = BootupErrors(Left(err :: Nil))

  implicit val monad : Monad[BootupErrors] = new Monad[BootupErrors] {
    def point[A](a : ⇒ A) = BootupErrors(a)
    def bind[A, B](v : BootupErrors[A])(f : A ⇒ BootupErrors[B]) = v.flatMap(f)
  }
}
