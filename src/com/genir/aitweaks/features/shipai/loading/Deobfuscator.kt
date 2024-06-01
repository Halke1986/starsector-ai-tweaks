package com.genir.aitweaks.features.shipai.loading

import org.objectweb.asm.*

/**
 * Deobfuscator analyzes vanilla classes and identifies obfuscated
 * names of packages, methods and fields that will to be replaced
 * when building AI Tweaks ship AI.
 */
class Deobfuscator(private val scriptLoader: ClassLoader) {
    class Symbols {
        // Packages
        var combatEntityPackage = ""

        // Types
        var basicShipAIInnerClasses: MutableList<String> = mutableListOf()
        var shipAIInterface = ""
        var maneuverInterface = ""
        var combatEntityInterface = ""
        var orderResponseModule = ""
        var orderResponseModuleInner = ""
        var flockingAIModule = ""
        var approachManeuver = ""
        var moveManeuver = ""

        // Methods
        var advance = ""
        var isDirectControl = ""
        var doManeuver = ""
        var getTarget = ""
        var getDesiredFacing = ""
        var getDesiredHeading = ""
        var getDesiredStrafeHeading = ""
    }

    private val symbols = Symbols()
    private val headingAndFacing = mutableMapOf<String, Int>()
    private val maneuvers = mutableSetOf<String>()

    fun getDeobfuscatedSymbols(): Symbols {
        analyzeBasicShipAI()
        analyzeOrderResponseModule()
        analyzeManeuverInterface()
        analyzeStrafe()
        analyzeManeuvers()

        return symbols
    }

    private fun analyzeBasicShipAI() {
        val reader = getReader("com.fs.starfarer.combat.ai.BasicShipAI")

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            // Find interfaces implemented by BasicShipAI.
            override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
                val fullName = interfaces!!.first { it.startsWith("com/fs/starfarer/combat/ai/movement/maneuvers") }
                symbols.shipAIInterface = getSimpleName(fullName)
                symbols.maneuverInterface = getOuterName(fullName)
            }

            // Find ship AI modules.
            override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
                when (name) {
                    "orderResponseModule" -> symbols.orderResponseModule = getSimpleName(desc!!)
                    "flockingAI" -> symbols.flockingAIModule = getSimpleName(desc!!)
                }
                return null
            }

            // Find all maneuver classes used by ship AI.
            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                return object : MethodVisitor(Opcodes.ASM4) {
                    override fun visitTypeInsn(opcode: Int, type: String?) {
                        if (type?.startsWith("com/fs/starfarer/combat/ai/movement/maneuvers/") == true) {
                            maneuvers.add(type)
                        }
                    }
                }
            }

            // Find BasicShipAI inner classes.
            override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
                if (name?.startsWith("com/fs/starfarer/combat/ai/BasicShipAI") == true) {
                    symbols.basicShipAIInnerClasses.add(getInnerName(name))
                }
            }
        }, 0)
    }

    private fun analyzeOrderResponseModule() {
        val reader = getReader("com.fs.starfarer.combat.ai.${symbols.orderResponseModule}")

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            // Find OrderResponseModule inner classe.
            override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
                if (name?.startsWith("com/fs/starfarer/combat/ai/${symbols.orderResponseModule}") == true) {
                    symbols.orderResponseModuleInner = getInnerName(name)
                }
            }
        }, 0)
    }

    private fun analyzeManeuverInterface() {
        val reader = getReader("com.fs.starfarer.combat.ai.movement.maneuvers.${symbols.maneuverInterface}")

        // Find maneuver interface method names and CombatEntity interface name.
        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                when (desc) {
                    "(F)V" -> symbols.advance = name!!
                    "()Z" -> symbols.isDirectControl = name!!
                    "()V" -> symbols.doManeuver = name!!
                    "()F" -> headingAndFacing[name!!] = 0
                    else -> {
                        symbols.getTarget = name!!
                        val elems = desc!!.removeSuffix(";").split("/").reversed()
                        symbols.combatEntityInterface = elems[0]
                        symbols.combatEntityPackage = elems[1]
                    }
                }

                return null
            }
        }, 0)
    }

    private fun analyzeStrafe() {
        val reader = getReader("com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2")

        // Find StrafeTargetManeuverV2 method names.
        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                when {
                    desc == "(Z)F" && access.isPublic -> symbols.getDesiredStrafeHeading = name!!
                    desc == "()F" && access.isPublic && headingAndFacing.contains(name) -> {
                        return object : MethodVisitor(Opcodes.ASM4) {
                            override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                                headingAndFacing[name!!] = maxLocals
                            }
                        }
                    }
                }

                return null
            }
        }, 0)

        // Distinguish between getFacing and getHeading methods
        // based on number of local fields. getFacing has more.
        val max: Int = headingAndFacing.values.maxOrNull()!!.toInt()
        headingAndFacing.forEach {
            if (it.value == max) symbols.getDesiredFacing = it.key
            else symbols.getDesiredHeading = it.key
        }
    }

    private fun analyzeManeuvers() {
        val ship = "com/fs/starfarer/combat/entities/Ship"
        val vector = "org/lwjgl/util/vector/Vector2f"
        val flockingAI = "com/fs/starfarer/combat/ai/movement/${symbols.flockingAIModule}"
        val shipAI = "com/fs/starfarer/combat/ai/movement/maneuvers/${symbols.shipAIInterface}"

        // Expected constructor types for overridden maneuvers.
        val approachInit = "(L$ship;L$ship;FL$flockingAI;L$shipAI;)V"
        val moveInit = "(L$ship;L$vector;L$shipAI;)V"

        val candidates = mutableMapOf<String, Pair<String, Int>>()

        // Gather all possible candidate classes for move and approach maneuvers.
        maneuvers.forEach { className ->
            val reader = getReader(className.replace("/", "."))
            candidates[className] = Pair("", 0)

            reader.accept(object : ClassVisitor(Opcodes.ASM4) {
                override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                    candidates[className] = candidates[className]!!.let { Pair(it.first, it.second + 1) }
                    if (name == "<init>") {
                        when (desc) {
                            approachInit -> candidates[className] = Pair("approach", candidates[className]!!.second)
                            moveInit -> candidates[className] = Pair("move", candidates[className]!!.second)
                        }
                    }

                    return null
                }
            }, 0)
        }

        // Identify expected maneuvers based on constructor type and number of methods.
        // In both cases the expected maneuvers have higher number of methods.
        val move = candidates.filter { it.value.first == "move" }.maxWithOrNull { a, b -> a.value.second - b.value.second }!!.key
        val approach = candidates.filter { it.value.first == "approach" }.maxWithOrNull { a, b -> a.value.second - b.value.second }!!.key

        symbols.moveManeuver = getSimpleName(move)
        symbols.approachManeuver = getSimpleName(approach)
    }

    private fun getSimpleName(descriptor: String) = descriptor.split("/").last().removeSuffix(";")

    private fun getOuterName(descriptor: String) = getSimpleName(descriptor).split("$").first()

    private fun getInnerName(descriptor: String) = getSimpleName(descriptor).split("$").last()

    private fun getReader(name: String): ClassReader = ClassReader(Transformer.readClassBuffer(scriptLoader, name))

    private val Int.isPublic: Boolean
        get() = (this and Opcodes.ACC_PUBLIC) != 0
}