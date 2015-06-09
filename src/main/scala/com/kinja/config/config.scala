package com.kinja.config

import scala.annotation.{ compileTimeOnly, StaticAnnotation }
import scala.language.experimental.macros
import scala.reflect.macros.{ blackbox, whitebox }

import com.typesafe.config.{ Config ⇒ TypesafeConfig }

@compileTimeOnly("Macro paradise must be enabled to expand macro annotations.")
class config(underlying : TypesafeConfig) extends StaticAnnotation {
  def macroTransform(annottees : Any*) : Any = macro config.impl
}

object config {
  def impl(c : whitebox.Context)(annottees : c.Expr[Any]*) : c.Expr[Any] = {
    import c.universe._
    import c.universe.Flag._

    def freshTerm(): TermName = c.freshName(TermName(""))
    def freshType(): TypeName = c.freshName(TypeName(""))

    val underlying : TermName = c.prefix.tree match {
      case q"new $_(..$params)" ⇒ params match {
        case Ident(name) :: tail ⇒ name.toTermName
        case _                   ⇒ c.abort(c.enclosingPosition, "No underlying template given.")
      }
      case _ ⇒ c.abort(c.enclosingPosition, "Encountered unexpected tree.")
    }
    val output = annottees.head.tree match {
      case ModuleDef(mods, name, impl) ⇒
        val root = q"""val root = com.kinja.config.BootupErrors(com.kinja.config.LiftedTypesafeConfig($underlying, "root"))"""
        val configApi = tq"com.kinja.config.ConfigApi"
        val newParents = configApi :: (impl.parents.filter(_ == tq"scala.AnyRef"))

        val configValues = impl.body.collect {
          case t @ ValDef(mods, name, tpt, rhs) if tpt.isEmpty ⇒
            val typ = c.typecheck(Block(List(root), rhs)).tpe
            val Modifiers(flags, pw, ann) = mods
            name → ValDef(Modifiers(PRIVATE | flags, pw, ann), freshTerm(), tq"$typ", rhs)
          case ValDef(mods, name, tpt, rhs) ⇒ name → ValDef(mods, freshTerm(), tpt, rhs)
        }

        val extractorName = freshType()
        val extractorClass = {
          val classMembers = configValues.map(_._2).map {
            case ValDef(Modifiers(flags, pw, ann), name, tpt, rhs) ⇒
              val TypeRef(_, _, typ :: Nil) = tpt.tpe

              ValDef(Modifiers(NoFlags, pw, ann), name, tq"$typ", EmptyTree)
          }
          q"private final case class $extractorName(..$classMembers)"
        }
        val extractor = {
          val constructor =
            if (configValues.length > 1)
              q"${extractorName.toTermName}.apply _ curried"
          	else
              q"${extractorName.toTermName}.apply _"

          q"""val ${extractorName.toTermName}(..${configValues.map(_._1)}) =
          (com.kinja.config.BootupErrors($constructor) <*> ${configValues.head._2.name} <*> ${configValues.tail.head._2.name})
            .fold(errs => throw new Exception("woah!"), a => a)""".children
        }

        val otherMembers = impl.body.filter {
          case _ : ValDef ⇒ false
          case _          ⇒ true
        }

        val newBody = root :: extractorClass :: (otherMembers ++ configValues.map(_._2) ++ extractor)

        ModuleDef(mods, name, Template(newParents, impl.self, newBody))
      case _ ⇒ c.abort(c.enclosingPosition, "Config must be an object.")
    }
    println(output)
    c.Expr[Any](Block(output :: Nil, Literal(Constant(()))))
  }
}
