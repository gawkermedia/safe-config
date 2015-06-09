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

        var thusFar: List[Tree] = List(root, q"""def nested(name : String) : com.kinja.config.BootupErrors[com.kinja.config.LiftedTypesafeConfig] = throw new Exception("")""")
        val configValues = impl.body.flatMap {
          case t @ ValDef(mods, name, tpt, rhs) if tpt.isEmpty && !mods.hasFlag(PRIVATE) ⇒
            val typ = c.typecheck(Block(thusFar, rhs)).tpe
            val Modifiers(flags, pw, ann) = mods
            thusFar = thusFar :+ t
            List(name → ValDef(Modifiers(PRIVATE | flags, pw, ann), freshTerm(), tq"$typ", rhs))
          case t @ ValDef(mods, name, tpt, rhs) if !mods.hasFlag(PRIVATE) ⇒
            thusFar = thusFar :+ t
            List(name → ValDef(mods, freshTerm(), tpt, rhs))
          case DefDef(_, name, _, _, _, _) if name.decodedName.toString == "<init>" ⇒ List.empty
          case t ⇒ 
            thusFar = thusFar :+ t
            List.empty
        }

        class IdentReplacer(from: TermName, to: TermName) extends Transformer {
          override def transform(tree : Tree) : Tree = tree match {
            case Ident(`from`) ⇒ Ident(to)
            case _ ⇒ super.transform(tree)
          }
        }

        val anonymizer = new Transformer {
          override def transform(tree : Tree) : Tree = tree match {
            case Function(ValDef(mods, name, tpt, rhs) :: Nil, body) if configValues.exists(_._1 == name) ⇒
              val anonName = freshTerm()
              Function(List(ValDef(mods, anonName, tpt, rhs)), new IdentReplacer(name, anonName).transform(body))
            case _ ⇒ super.transform(tree)
          }
        }

        val transformer = new Transformer {
          override def transform(tree : Tree) : Tree = tree match {
            case Ident(name) if configValues.exists(_._1 == name) ⇒
              val anonName = configValues.find(_._1 == name).get._2.name
              Ident(anonName)
            case ValDef(_, name, _, _) if configValues.exists(_._1 == name) ⇒
              super.transform(tree)
            case _ ⇒ super.transform(tree)
          }
        }

        val extractorName = freshType()
        val extractorClass = {
          val classMembers = configValues.map(_._2).flatMap {
            case ValDef(Modifiers(flags, pw, ann), name, AppliedTypeTree(tpt, args), rhs) ⇒
              List(ValDef(Modifiers(NoFlags, pw, ann), name, args.head, EmptyTree))
            case ValDef(Modifiers(flags, pw, ann), name, tpt, rhs) ⇒ tpt.tpe match {
              case TypeRef(_, _, typ :: Nil) ⇒
                List(ValDef(Modifiers(NoFlags, pw, ann), name, tq"$typ", EmptyTree))
              case _ ⇒ List.empty
            }
          }
          q"private final case class $extractorName(..$classMembers)"
        }
        val extractor = {
          val constructor =
            if (configValues.length > 1)
              q"${extractorName.toTermName}.apply _ curried"
          	else
              q"${extractorName.toTermName}.apply _"

          val applied = configValues.foldLeft(q"com.kinja.config.BootupErrors($constructor)") {
            case (acc, (_, valDef)) ⇒ q"$acc <*> ${valDef.name}"
          }

          q"""val ${extractorName.toTermName}(..${configValues.map(_._1)}) =
          ($applied)
            .fold(errs => throw new Exception("woah!"), a => a)""".children
        }

        val otherMembers = impl.body.filter {
          case ValDef(_, name, _, _) if configValues.exists(_._1 == name) ⇒ false
          case _          ⇒ true
        }
        val finalConfigValues = transformer.transformTrees(anonymizer.transformTrees(configValues.map(_._2)))

        val newBody = root :: extractorClass :: (otherMembers ++ finalConfigValues ++ extractor)

        ModuleDef(mods, name, Template(newParents, impl.self, newBody))
      case _ ⇒ c.abort(c.enclosingPosition, "Config must be an object.")
    }
    println(output)
    c.Expr[Any](Block(output :: Nil, Literal(Constant(()))))
  }
}
