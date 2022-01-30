package codes.som.anthony.koffee.sugar

import codes.som.anthony.koffee.BlockAssembly
import codes.som.anthony.koffee.ClassAssembly
import codes.som.anthony.koffee.MethodAssembly
import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.insns.jvm.aload_0
import codes.som.anthony.koffee.insns.jvm.invokespecial
import codes.som.anthony.koffee.insns.jvm.`return`
import codes.som.anthony.koffee.modifiers.Modifiers
import codes.som.anthony.koffee.types.TypeLike
import org.objectweb.asm.tree.MethodNode

object ClassAssemblyExtension {
    fun ClassAssembly.init(
        access: Modifiers,
        vararg parameterTypes: TypeLike,
        superClass: TypeLike = Object::class,
        routine: MethodAssembly.() -> Unit
    ): MethodNode {
        if (access.containsOther(public + private + protected + package_private)) {
            throw IllegalArgumentException(
                "Method 'init' has illegal modifiers! " +
                    "Legal modifiers include: public, private, protected, and package private."
            )
        }

        return method(access, "<init>", void, *parameterTypes) {
            aload_0 // load this
            invokespecial(superClass, "<init>", void)

            routine()
        }
    }

    fun ClassAssembly.clinit(routine: MethodAssembly.() -> Unit): MethodNode {
        return method(static, "<clinit>", void, routine = routine)
    }

    fun ClassAssembly.insertToInits(routine: BlockAssembly.() -> Unit): List<MethodAssembly> {
        val (instructions, _) = assembleBlock(routine)
        return findOrCreateInits().onEach { assembly ->
            assembly.instructions.insert(instructions)
        }
    }

    fun ClassAssembly.insertToClinit(routine: BlockAssembly.() -> Unit): MethodAssembly {
        val constructor = findOrCreateClinit()
        val (instructions, _) = assembleBlock(routine)
        constructor.instructions.insert(instructions)
        return constructor
    }

    private fun ClassAssembly.findOrCreateInits(): List<MethodAssembly> {
        val constructors = node.methods.filter { it.name == "<init>" }.toMutableList()
        if (constructors.isEmpty()) {
            val method = method(public, "<init>", void) {
                aload_0
                invokespecial("java/lang/Object", "<init>", "()V")
                `return`
            }
            constructors.add(method)
        }
        return constructors.map(::MethodAssembly)
    }

    private fun ClassAssembly.findOrCreateClinit(): MethodAssembly {
        val constructor = node.methods.find { it.name == "<clinit>" }
            ?: method(static, "<clinit>", void) {
                `return`
            }
        return MethodAssembly(constructor)
    }
}
