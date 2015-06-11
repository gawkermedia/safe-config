package com.kinja.config

/**
 * A Bootup Errors accumulator. It accumulates errors when applied (<*>).
 * It is also a monad (but does not accumulate errors in that case).
 */
final case class BootupErrors[A] private[BootupErrors] (run : Either[Seq[String], A]) extends AnyVal {
  def map[B](f : A ⇒ B) : BootupErrors[B] = BootupErrors(this.run.right.map(f))

  /** Sequentially apply another BootupErrors to this one, accumulating any errors therein. */
  def <*>[B, C](that : BootupErrors[B])(implicit ev : <:<[A, B ⇒ C]) : BootupErrors[C] = BootupErrors {
    (this.run, that.run) match {
      case (Right(g), Right(k)) ⇒ Right(g(k))
      case (Left(e), Right(k))  ⇒ Left(e)
      case (Right(e), Left(k))  ⇒ Left(k)
      case (Left(e), Left(k))   ⇒ Left(e ++ k)
    }
  }

  def flatten[B](implicit ev : A <:< BootupErrors[B]) : BootupErrors[B] = this.flatMap(a ⇒ a)

  /** Bind on another BootupErrors. Unlike `<*>` this does not accumulate errors. */
  def flatMap[B](f : A ⇒ BootupErrors[B]) : BootupErrors[B] = BootupErrors(this.run.right.flatMap(f(_).run))

  def fold[B](err : Seq[String] ⇒ B, succ : A ⇒ B) : B = run.fold(err, succ)
  def getOrElse(e : Seq[String] ⇒ A) : A = this.fold(e, a ⇒ a)

  /**
   * Return all accumulated errors, if any
   */
  def errors : Seq[String] = run.fold(a ⇒ a, _ ⇒ Nil)

  // TODO this should return None when the value is missing, Some when successful,
  // and a failed BootupErrors in all other situations.
  // def optional : BootupErrors[Option[A]]

  /**
   * Returns None if this BootupErrors contains any errors, otherwise Some.
   */
  def toOption : Option[A] = this.fold(_ ⇒ None, Some(_))
}

object BootupErrors {
  def apply[A](a : A) : BootupErrors[A] = BootupErrors(Right(a))
  def failed[A](err : String) : BootupErrors[A] = BootupErrors(Left(err :: Nil))
}
