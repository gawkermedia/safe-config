package com.kinja.config

import scala.annotation.{ compileTimeOnly, StaticAnnotation }
import scala.language.experimental.macros
import scala.reflect.macros.{ whitebox, TypecheckException }

/**
 * Adding this annotation to a class or object enables the configuration DSL within.
 *
 * @param underlying
 *   Either the underlying Config object or a string indicating the identifier that the underlying Config object can be
 *   referenced by.
 */
@compileTimeOnly("Macro paradise must be enabled to expand macro annotations.")
class safeConfig(underlying: Any) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro safeConfig.impl
}

object safeConfig {

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.Any",
      "org.wartremover.warts.NonUnitStatements",
      "org.wartremover.warts.Nothing",
      "org.wartremover.warts.SizeIs",
      "org.wartremover.warts.ToString",
      "org.wartremover.warts.Var"
    )
  )
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import c.universe.Flag._

    def freshTerm(): TermName = TermName(c.freshName("safe_config"))
    def freshType(): TypeName = TypeName(c.freshName("safe_config"))

    // Get the argument passed to safeConfig.
    @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
    val underlying: Tree = c.prefix.tree match {
      case q"new $_(..$params)" =>
        params match {
          case Literal(Constant(i: String)) :: Nil => Ident(TermName(i))
          case head :: Nil => head
          case _ =>
            c.abort(c.enclosingPosition, "No underlying configuration given.")
        }
      case _ => c.abort(c.enclosingPosition, "Encountered unexpected tree.")
    }

    // The root element.
    val root =
      q"""lazy val root: com.kinja.config.BootupErrors[com.kinja.config.LiftedTypesafeConfig] = com.kinja.config.BootupErrors(com.kinja.config.LiftedTypesafeConfig(($underlying, "root")))"""

    def modBody(impl: Template): Template = {
      val configApi = tq"com.kinja.config.ConfigApi"
      val newParents = impl.parents.filter {
        case Select(Ident(TermName("scala")), TypeName("AnyRef")) => false
        case _ => true
      } :+ configApi

      // Put members of mixins like ConfigApi into scope.
      val context = c.typecheck(
        q"""(throw new java.lang.Exception("")) : Object with ..$newParents"""
      )
      val dummyMembers = context.tpe.members
        .filter(member => member.name.decodedName.toString.trim != "wait")
        .map {
          case m: MethodSymbol =>
            val params = m.paramLists.headOption.toList.flatMap(
              _.map(p => q"""val ${p.name.toTermName} : ${p.typeSignature}""")
            )
            q"""def ${m.name.toTermName}(..$params) : ${m.returnType} = (throw new java.lang.Exception("")) : ${m.returnType}"""
          case t: TermSymbol =>
            q"""var ${t.name.toTermName} : ${t.typeSignature} = (throw new java.lang.Exception("")) : ${t.typeSignature}"""
        }

      // Previous definitions. Used for type checking.
      // TODO: Typecheck the whole block at once to allow for forward references.
      //       This will require some serious reworking of the implementation.
      var thusFar: List[Tree] = dummyMembers.toList
      val configValues = impl.body.flatMap {
        case t @ ValDef(mods, name, tpt, rhs) if tpt.isEmpty && !mods.hasFlag(PRIVATE) =>
          val typ =
            try {
              c.typecheck(Block(thusFar, rhs)).tpe
            } catch {
              case e: TypecheckException =>
                c.abort(e.pos.asInstanceOf[c.Position], e.getMessage)
            }
          val Modifiers(flags, pw, ann) = mods
          thusFar = thusFar :+ ValDef(mods, name, tq"$typ", rhs)

          // Ignore pure values.
          if (typ <:< typeOf[BootupErrors[_]])
            List(
              name -> ValDef(
                Modifiers(PRIVATE | flags, pw, ann),
                freshTerm(),
                tq"$typ",
                rhs
              )
            )
          else
            List.empty[(TermName, ValDef)]
        case t @ ValDef(
              mods,
              name,
              tpt @ AppliedTypeTree(Ident(TypeName("BootupErrors")), args),
              rhs
            ) if !mods.hasFlag(PRIVATE) =>
          val Modifiers(flags, pw, ann) = mods
          thusFar = thusFar :+ t
          List(
            name -> ValDef(
              Modifiers(PRIVATE | flags, pw, ann),
              freshTerm(),
              tpt,
              rhs
            )
          )
        case DefDef(_, name, _, _, _, _) if name.decodedName.toString == "<init>" =>
          List.empty[(TermName, ValDef)]
        case t @ ValDef(mods, name, tpt, rhs) if mods.hasFlag(Flag.PARAMACCESSOR) =>
          val Modifiers(_, pw, ann) = mods
          val flags =
            if (mods.hasFlag(Flag.IMPLICIT)) Flag.IMPLICIT else NoFlags

          thusFar = thusFar :+ ValDef(Modifiers(flags, pw, ann), name, tpt, rhs)
          List.empty[(TermName, ValDef)]
        case t =>
          thusFar = thusFar :+ t
          List.empty[(TermName, ValDef)]
      }

      // Replaces references between config values with the private name.
      val transformer = new Transformer {
        var stack: List[Map[TermName, TermName]] = List(configValues.map { case (key, tree) =>
          key -> tree.name
        }.toMap)

        @SuppressWarnings(
          Array(
            "org.wartremover.warts.OptionPartial",
            "org.wartremover.warts.TraversableOps"
          )
        )
        override def transform(tree: Tree): Tree = tree match {
          case Block(stmnts, last) =>
            val (args, trees) =
              stmnts.foldLeft(stack.head -> List.empty[Tree]) {
                case ((idents, block), t @ ValDef(_, name, _, _)) =>
                  val args = idents - name
                  stack = args +: stack
                  try {
                    args -> (super.transform(t) :: block)
                  } finally { stack = stack.tail }
                case ((idents, block), t) =>
                  idents -> (super.transform(t) :: block)
              }
            stack = args +: stack
            try super.transform(tree)
            finally { stack = stack.tail }
          case Function(valDefs, _) =>
            val args = stack.head -- valDefs.map(_.name)
            stack = args +: stack
            try super.transform(tree)
            finally { stack = stack.tail }
          case Ident(TermName(name)) if stack.head.contains(TermName(name)) =>
            val anonName = stack.head.get(TermName(name)).get
            Ident(anonName)
          case _ => super.transform(tree)
        }
      }

      // The maximum number of arguments to a class in Java is 255.
      val extractors = configValues.grouped(255).flatMap { configValues =>
        val extractorName = freshType()
        val extractorClass = {
          val classMembers = configValues.map(_._2).flatMap {
            case ValDef(
                  Modifiers(flags, pw, ann),
                  name,
                  AppliedTypeTree(tpt, arg :: _),
                  rhs
                ) =>
              List(ValDef(Modifiers(NoFlags, pw, ann), name, arg, EmptyTree))
            case ValDef(Modifiers(flags, pw, ann), name, tpt, rhs) =>
              tpt.tpe match {
                case TypeRef(_, _, typ :: Nil) =>
                  List(
                    ValDef(
                      Modifiers(NoFlags, pw, ann),
                      name,
                      tq"$typ",
                      EmptyTree
                    )
                  )
                case _ => List.empty[ValDef]
              }
          }
          val construct = classMembers.foldRight(
            q"new $extractorName(..${classMembers.map(_.name)})"
          ) { case (valDef, acc) =>
            q"($valDef => $acc)"
          }
          q"""private final class $extractorName(..$classMembers)
              private object ${extractorName.toTermName} {
                def construct = $construct
              }""".children
        }
        val extractor = {
          val constructor =
            if (configValues.length > 1)
              q"${extractorName.toTermName}.construct"
            else
              q"${extractorName.toTermName}.construct"

          val applied = configValues.foldLeft(
            q"com.kinja.config.BootupErrors($constructor)"
          ) { case (acc, (_, valDef)) =>
            q"$acc <*> ${valDef.name}"
          }

          val extractorInstance = freshTerm()

          val bundled = q"""private val $extractorInstance = ($applied)
            .fold(errs => throw new com.kinja.config.BootupConfigurationException(errs), a => a)"""

          val accessors = configValues.map { case (name, valDef) =>
            val tpeOpt: Option[Tree] = valDef.tpt match {
              case AppliedTypeTree(tpt, args) => args.headOption
              case tree =>
                tree.tpe match {
                  case TypeRef(_, _, typ :: Nil) => Some(tq"$typ")
                  case _ => None
                }
            }
            tpeOpt match {
              case Some(tpe) =>
                q"val $name: $tpe = $extractorInstance.${valDef.name}"
              case None => q"val $name = $extractorInstance.${valDef.name}"
            }
          }

          (extractorClass :+ bundled) ++ accessors
        }
        extractor
      }

      val otherMembers = impl.body.filter {
        case ValDef(_, name, _, _) if configValues.exists(_._1 == name) => false
        case _ => true
      }
      val finalConfigValues = transformer.transformTrees(configValues.map(_._2))

      val newBody = root :: (otherMembers ++ finalConfigValues ++ extractors)

      Template(newParents, impl.self, newBody)
    }

    val output = annottees.headOption.map(_.tree) match {
      case Some(ModuleDef(mods, name, impl)) =>
        ModuleDef(mods, name, modBody(impl))
      case Some(ClassDef(mods, name, tparams, impl)) =>
        ClassDef(mods, name, tparams, modBody(impl))
      case _ => c.abort(c.enclosingPosition, "Config must be an object.")
    }
    c.Expr[Any](Block(output :: Nil, Literal(Constant(()))))
  }
}
