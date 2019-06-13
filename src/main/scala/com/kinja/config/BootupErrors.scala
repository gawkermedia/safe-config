package com.kinja.config

/**
 * A Bootup Errors accumulator. It accumulates errors when applied (<*>).
 * It is also a monad (but does not accumulate errors in that case).
 */
final case class BootupErrors[A] private[BootupErrors] (run : Either[Seq[ConfigError], A]) extends AnyVal {
  def map[B](f : A => B) : BootupErrors[B] = BootupErrors(this.run.map(f))

  /** Sequentially apply another BootupErrors to this one, accumulating any errors therein. */
  def <*>[B, C](that : BootupErrors[B])(implicit ev : <:<[A, B => C]) : BootupErrors[C] = BootupErrors {
    (this.run, that.run) match {
      case (Right(g), Right(k)) => Right[Seq[ConfigError], C](g(k))
      case (Left(e), Right(k))  => Left[Seq[ConfigError], C](e)
      case (Right(e), Left(k))  => Left[Seq[ConfigError], C](k)
      case (Left(e), Left(k))   => Left[Seq[ConfigError], C](e ++ k)
    }
  }

  def flatten[B](implicit ev : A <:< BootupErrors[B]) : BootupErrors[B] = this.flatMap(a => a)

  /** Bind on another BootupErrors. Unlike `<*>` this does not accumulate errors. */
  def flatMap[B](f : A => BootupErrors[B]) : BootupErrors[B] = BootupErrors(this.run.flatMap(f(_).run))

  def fold[B](err : Seq[ConfigError] => B, succ : A => B) : B = run.fold(err, succ)
  def getOrElse(e : Seq[ConfigError] => A) : A = this.fold(e, a => a)

  /**
   * Return all accumulated errors, if any
   */
  def errors : Seq[ConfigError] = run.fold(a => a, _ => Nil)

  // TODO this should return None when the value is missing, Some when successful,
  // and a failed BootupErrors in all other situations.
  // def optional : BootupErrors[Option[A]]

  /**
   * Returns None if this BootupErrors contains any errors, otherwise Some.
   */
  def toOption : Option[A] = this.fold(_ => None, Some(_))
}

object BootupErrors {
  def apply[A](a : A) : BootupErrors[A] = BootupErrors(Right[Seq[ConfigError], A](a))
  def failed[A](err : ConfigError) : BootupErrors[A] = BootupErrors(Left[Seq[ConfigError], A](err :: Nil))

  def sequence[A](as : List[BootupErrors[A]]) : BootupErrors[List[A]] =
    as.foldRight(BootupErrors(List.empty[A])) {
      case (a, acc) => for {
        a_ <- a
        acc_ <- acc
      } yield a_ :: acc_
    }
}
