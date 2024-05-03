package com.genir.aitweaks.features.shipai.loading

import org.objectweb.asm.*

class ObfTable {
    // Types
    var orderResponseModule = ""
    var flockingAI = ""
    var maneuver = ""
    var shipAI = ""
    var combatEntity = ""
    var combatEntityPackage = ""
    var approach = ""
    var move = ""

    // Methods
    var advance = ""
    var isDirectControl = ""
    var doManeuver = ""
    var getTarget = ""
    var getDesiredFacing = ""
    var getDesiredHeading = ""
    var getDesiredStrafeHeading = ""

    private val headingAndFacing = mutableMapOf<String, Int>()
    private val maneuvers = mutableSetOf<String>()

    init {
        analyzeBasicShipAI()
        analyzeManeuverInterface()
        analyzeStrafe()
        analyzeManeuvers()
    }

    private fun analyzeBasicShipAI() {
        val reader = getReader("com.fs.starfarer.combat.ai.BasicShipAI")

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
                val fullName = interfaces!!.first { it.startsWith("com/fs/starfarer/combat/ai/movement/maneuvers") }
                shipAI = getSimpleName(fullName)
                maneuver = getOuterName(fullName)
            }

            override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
                when (name) {
                    "orderResponseModule" -> orderResponseModule = getSimpleName(desc!!)
                    "flockingAI" -> flockingAI = getSimpleName(desc!!)
                }
                return null
            }

            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                return object : MethodVisitor(Opcodes.ASM4) {
                    override fun visitTypeInsn(opcode: Int, type: String?) {
                        if (type?.startsWith("com/fs/starfarer/combat/ai/movement/maneuvers/") == true) {
                            maneuvers.add(type)
                        }
                    }
                }
            }
        }, 0)
    }

    private fun analyzeManeuverInterface() {
        val reader = getReader("com.fs.starfarer.combat.ai.movement.maneuvers.${maneuver}")

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                when (desc) {
                    "(F)V" -> advance = name!!
                    "()Z" -> isDirectControl = name!!
                    "()V" -> doManeuver = name!!
                    "()F" -> headingAndFacing[name!!] = 0
                    else -> {
                        getTarget = name!!
                        val elems = desc!!.removeSuffix(";").split("/").reversed()
                        combatEntity = elems[0]
                        combatEntityPackage = elems[1]
                    }
                }

                return null
            }
        }, 0)
    }

    private fun analyzeStrafe() {
        val reader = getReader("com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2")

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                when {
                    desc == "(Z)F" && access.isPublic -> getDesiredStrafeHeading = name!!
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
            if (it.value == max) getDesiredFacing = it.key
            else getDesiredHeading = it.key
        }
    }

    private fun analyzeManeuvers() {
        val ship = "com/fs/starfarer/combat/entities/Ship"
        val vector = "org/lwjgl/util/vector/Vector2f"
        val flockingAI = "com/fs/starfarer/combat/ai/movement/${flockingAI}"
        val shipAI = "com/fs/starfarer/combat/ai/movement/maneuvers/${shipAI}"

        // Expected constructor types for overridden maneuvers.
        val approachInit = "(L$ship;L$ship;FL$flockingAI;L$shipAI;)V"
        val moveInit = "(L$ship;L$vector;L$shipAI;)V"

        val candidates = mutableMapOf<String, Pair<String, Int>>()

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

        this.move = getSimpleName(move)
        this.approach = getSimpleName(approach)
    }

    private fun getSimpleName(descriptor: String) = descriptor.split("/").last().removeSuffix(";")

    private fun getOuterName(descriptor: String) = getSimpleName(descriptor).split("$").first()

    private fun getReader(name: String): ClassReader {
        val classData = ClassConstantTransformer.readClassBuffer(this.javaClass.classLoader, name)
        return ClassReader(classData)
    }

    private val Int.isPublic: Boolean
        get() = (this and Opcodes.ACC_PUBLIC) != 0
}