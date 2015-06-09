# Safe Config

Safe Config provides a safe and convenient wrapper around Typesafe's Config library.

## Usage
Although most people will use Safe Config in a Play project, Safe Config can be used anywhere that Typesafe's Config is used. The following example will assume Play.

To create your configuration object use the `safeConfig` macro.
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
To ensure your config is initialized as early as possible (and thus any errors thrown as early as possible) you should add a reference to your config object in Play's `onStart` function.

## How It Works

The example given above will expand to the following:
```scala
import com.kinja.config.safeConfig
import play.api.Play.configuration.{ underlying ⇒ playConf }

@safeConfig(playConf)
object Config {
   private final case class $Extractor(a : DbConfig, b : List[String], c : String)
   private val dbConfig = getConfig("db")

   private val $orig_dbConfig : BootupErrors[DbConfig] = for {
      conf  ← dbConfig
      read  ← conf.getString("read")
      write ← conf.getString("write")
   } yield DbConfig(read, write)
   private val $orig_languages : BootupErrors[List[String]] = getStringList("application.languages")
   private val $orig_secret : BootupErrors[String] = getString("application.secret")
   
   val $Extractor(dbConfig, languages, secret) = (BootupErrors($Extractor.apply _ curried)
      <*> $orig_dbConfig
      <*> $orig_languages
      <*> $orig_secret
   ).fold(errs ⇒ errs => throw new BootupErrorsException(errs), a => a)
}

final case class DbConfig(readConnection : String, writeConnection : String)
```
