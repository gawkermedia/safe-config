# Safe Config

Safe Config provides a safe and convenient wrapper around Typesafe's Config library.

## Quick Start
Add the following to your `build.sbt` file:
```scala
libraryDependencies ++= Seq(
  "com.kinja" %% "safe-config" % "1.0.0",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full))
```
Create your first config object:
```scala
import com.kinja.config.safeConfig
import play.api.Play.configuration.{ underlying ⇒ playConf }

@safeConfig(playConf)
object Config {
   private val dbConfig = getConfig("db")
   
   val dbConfig = for {
      conf  ← dbConfig
      read  ← conf.getString("read")
      write ← conf.getString("write")
   } yield DbConfig(read, write)

   val languages = getStringList("application.languages")

   val secret = getString("application.secret")
}

final case class DbConfig(readConnection : String, writeConnection : String)
```
```scala
// In Global.scala
override def onStart(app: Application): Unit = {
  Config
  ...
}
```

## How to use
The `safeConfig` annotation marks a configuration object. Within the configuration object all errors are handled automatically and accessors are created exposing the pure values. Additionally, configuration objects expose the [`ConfigApi`](http://gawkermedia.github.io/safe-config/doc/#com.kinja.config.ConfigApi) interface (select "Visibility: All").

All config values in the configuration object are eagerly evaluated and if any are the wrong type or missing, an exception is thrown indicating the problems with reading the config file.
![](http://gawkermedia.github.io/safe-config/img/BootupErrorsException.png)

In order to catch these errors as soon as possible, you should reference your config objects during your application's startup.

## API Documentation

The full API documentation is available [here](http://gawkermedia.github.io/safe-config/doc/#package).

## How It Works

The example given above will expand to the following:
```scala
import com.kinja.config.safeConfig
import play.api.Play.configuration.{ underlying ⇒ playConf }

object Config extends com.kinja.config.ConfigApi {
   import com.kinja.config._
   val root = BootupErrors(LiftedTypesafeConfig(playConf))

   private final class $Extractor(a : DbConfig, b : List[String], c : String)
   private object $Extractor {
      def construct : DbConfig ⇒ List[String] ⇒ String ⇒ $Extractor =
         a ⇒ b ⇒ c ⇒ new $Extractor(a, b, c)
   }
   private val dbConfig = getConfig("db")

   private val $orig_dbConfig : BootupErrors[DbConfig] = for {
      conf  ← dbConfig
      read  ← conf.getString("read")
      write ← conf.getString("write")
   } yield DbConfig(read, write)
   private val $orig_languages : BootupErrors[List[String]] = getStringList("application.languages")
   private val $orig_secret : BootupErrors[String] = getString("application.secret")
   
   private val $Extractor_instance = (BootupErrors($Extractor.construct)
      <*> $orig_dbConfig
      <*> $orig_languages
      <*> $orig_secret
   ).fold(errs ⇒ throw new BootupErrorsException(errs), a ⇒ a)
   
   val dbConfig = $Extractor_instance.a
   val languages = $Extractor_instance.b
   val secret = $Extractor_instance.b
}

final case class DbConfig(readConnection : String, writeConnection : String)
```
