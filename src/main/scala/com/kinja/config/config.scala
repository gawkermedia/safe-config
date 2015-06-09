package com.kinja.config

import scala.annotation.{ compileTimeOnly, StaticAnnotation }
import scala.language.experimental.macros
import scala.reflect.macros.{ blackbox, whitebox, TypecheckException }

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

        // Previous definitions. Used for type checking.
        // TODO: Maybe just typecheck the whole block at once?
        var thusFar: List[Tree] = List(root, q"""def nested(name : String) : com.kinja.config.BootupErrors[com.kinja.config.LiftedTypesafeConfig] = throw new Exception("")""")
        val configValues = impl.body.flatMap {
          case t @ ValDef(mods, name, tpt, rhs) if tpt.isEmpty && !mods.hasFlag(PRIVATE) ⇒
            val typ = try {
              c.typecheck(Block(thusFar, rhs)).tpe
            } catch {
              case e : TypecheckException ⇒ c.abort(e.pos.asInstanceOf[c.Position], e.getMessage)
            }
            val Modifiers(flags, pw, ann) = mods
            thusFar = thusFar :+ ValDef(mods, name, tq"$typ", rhs)

            // Ignore pure values.
            if (typ <:< typeOf[BootupErrors[_]])
              List(name → ValDef(Modifiers(PRIVATE | flags, pw, ann), freshTerm(), tq"$typ", rhs))
            else
              List.empty
          case t @ ValDef(mods, name, tpt @ AppliedTypeTree(Ident(TypeName("BootupErrors")), args), rhs)
              if !mods.hasFlag(PRIVATE) ⇒

            thusFar = thusFar :+ t
            List(name → ValDef(mods, freshTerm(), tpt, rhs))
          case DefDef(_, name, _, _, _, _) if name.decodedName.toString == "<init>" ⇒ List.empty
          case t ⇒ 
            thusFar = thusFar :+ t
            List.empty
        }

        val transformer = new Transformer {
          import collection.mutable.Stack
          val stack : Stack[Map[TermName, TermName]] = Stack(configValues.map {
            case (key, tree) ⇒ key → tree.name
          } toMap)

          override def transform(tree : Tree) : Tree = tree match {
            case Block(stmnts, last) ⇒
              val (args, trees) = stmnts.foldLeft(stack.head -> List.empty[Tree]) {
                case ((idents, block), t @ ValDef(_, name, _, _)) ⇒
                  val args = idents - name
                  stack.push(args)
                  try (
                    args → (super.transform(t) :: block)
                  ) finally { stack.pop(); () }
                case ((idents, block), t) ⇒
                  idents → (super.transform(t) :: block)
              }
              stack.push(args)
              try super.transform(tree) finally { stack.pop(); () }
            case Function(valDefs, _) ⇒
              val args = stack.head -- valDefs.map(_.name)
              stack.push(args)
              try super.transform(tree) finally { stack.pop(); () }
            case Ident(TermName(name)) if stack.head.contains(TermName(name)) ⇒
              val anonName = stack.head.get(TermName(name)).get
              Ident(anonName)
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
        val finalConfigValues = transformer.transformTrees(configValues.map(_._2))

        val newBody = root :: extractorClass :: (otherMembers ++ finalConfigValues ++ extractor)

        ModuleDef(mods, name, Template(newParents, impl.self, newBody))
      case _ ⇒ c.abort(c.enclosingPosition, "Config must be an object.")
    }
    println(output)
    c.Expr[Any](Block(output :: Nil, Literal(Constant(()))))
  }
}
