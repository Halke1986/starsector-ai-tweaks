package com.genir.aitweaks.launcher.loading

import com.fs.starfarer.api.Global
import com.genir.aitweaks.launcher.loading.Bytecode.classPath
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.io.path.Path

class CoreLoader : URLClassLoader(arrayOf(latestCoreURL())) {
    private val cache: MutableMap<String, Class<*>> = mutableMapOf()

    private val obfuscator by lazy {
        val symbols: Symbols by lazy { Symbols() }
        val core = "com/genir/aitweaks/core/Obfuscated"

        Transformer(listOf(
            // Classes.
            Transformer.newTransform("$core\$Ship", symbols.ship.classPath),
            Transformer.newTransform("$core\$BasicShipAI", symbols.basicShipAI.classPath),
            Transformer.newTransform("$core\$AttackAIModule", symbols.attackAIModule.classPath),
            Transformer.newTransform("$core\$CombatEngine", symbols.combatEngine.classPath),
            Transformer.newTransform("$core\$FlockingAI", symbols.flockingAI.classPath),
            Transformer.newTransform("$core\$ApproachManeuver", symbols.approachManeuver.classPath),
            Transformer.newTransform("$core\$AutofireManager", symbols.autofireManager.classPath),
            Transformer.newTransform("$core\$Maneuver", symbols.maneuver.classPath),
            Transformer.newTransform("$core\$ShipCommandWrapper", symbols.shipCommandWrapper.classPath),
            Transformer.newTransform("$core\$ShipCommand", symbols.shipCommand.classPath),
            Transformer.newTransform("$core\$CombatEntity", symbols.combatEntity.classPath),
            Transformer.newTransform("$core\$Weapon", symbols.weapon.classPath),
            Transformer.newTransform("$core\$AimTracker", symbols.aimTracker.classPath),
            Transformer.newTransform("$core\$PlayerAction", symbols.playerAction.classPath),
            Transformer.newTransform("$core\$Keymap", symbols.keymap.classPath),
            Transformer.newTransform("$core\$FighterPullbackModule", symbols.fighterPullbackModule.classPath),
            Transformer.newTransform("$core\$SystemAI", symbols.systemAI.classPath),
            Transformer.newTransform("$core\$ShieldAI", symbols.shieldAI.classPath),
            Transformer.newTransform("$core\$VentModule", symbols.ventModule.classPath),
            Transformer.newTransform("$core\$ThreatEvaluator", symbols.threatEvaluator.classPath),
            Transformer.newTransform("$core\$ThreatResponseManeuver", symbols.threatResponseManeuver.classPath),
            Transformer.newTransform("$core\$CombatMap", symbols.combatMap.classPath),
            Transformer.newTransform("$core\$MissionDefinitionPluginContainer", symbols.missionDefinitionPluginContainer.classPath),
            Transformer.newTransform("$core\$BeamWeapon", symbols.beamWeapon.classPath),
            Transformer.newTransform("$core\$ProjectileWeapon", symbols.projectileWeapon.classPath),
            Transformer.newTransform("$core\$LoadingUtils", symbols.loadingUtils.classPath),
            Transformer.newTransform("$core\$FrontShieldAI", symbols.frontShieldAI.classPath),
            Transformer.newTransform("$core\$OmniShieldAI", symbols.omniShieldAI.classPath),
            Transformer.newTransform("$core\$Bounds", symbols.bounds.classPath),
            Transformer.newTransform("$core\$BoundsSegment", symbols.boundsSegment.classPath),

            // Fields and methods.
            Transformer.newTransform("autofireManager_advance", symbols.autofireManager_advance.name),
            Transformer.newTransform("shipCommandWrapper_getCommand", symbols.shipCommandWrapper_getCommand.name),
            Transformer.newTransform("maneuver_getTarget", symbols.maneuver_getTarget.name),
            Transformer.newTransform("aimTracker_setTargetOverride", symbols.aimTracker_setTargetOverride.name),
            Transformer.newTransform("keymap_isKeyDown", symbols.keymap_isKeyDown.name),
            Transformer.newTransform("attackAIModule_advance", symbols.attackAIModule_advance.name),
            Transformer.newTransform("fighterPullbackModule_advance", symbols.fighterPullbackModule_advance.name),
            Transformer.newTransform("systemAI_advance", symbols.systemAI_advance.name),
            Transformer.newTransform("shieldAI_advance", symbols.shieldAI_advance.name),
            Transformer.newTransform("ventModule_advance", symbols.ventModule_advance.name),
            Transformer.newTransform("threatEvaluator_advance", symbols.threatEvaluator_advance.name),
            Transformer.newTransform("flockingAI_setDesiredHeading", symbols.flockingAI_setDesiredHeading.name),
            Transformer.newTransform("flockingAI_setDesiredFacing", symbols.flockingAI_setDesiredFacing.name),
            Transformer.newTransform("flockingAI_setDesiredSpeed", symbols.flockingAI_setDesiredSpeed.name),
            Transformer.newTransform("flockingAI_advanceCollisionAnalysisModule", symbols.flockingAI_advanceCollisionAnalysisModule.name),
            Transformer.newTransform("flockingAI_getMissileDangerDir", symbols.flockingAI_getMissileDangerDir.name),
            Transformer.newTransform("flockingAI_getCollisionDangerDir", symbols.flockingAI_getCollisionDangerDir.name),
            Transformer.newTransform("combatMap_getPluginContainers", symbols.combatMap_getPluginContainers.name),
            Transformer.newTransform("missionDefinitionPluginContainer_getEveryFrameCombatPlugin", symbols.missionDefinitionPluginContainer_getEveryFrameCombatPlugin.name),
            Transformer.newTransform("loadingUtils_loadSpec", symbols.loadingUtils_loadSpec.name),
        ))
    }

    override fun loadClass(name: String): Class<*> {
        cache[name]?.let { return it }

        val c: Class<*> = when {
            name.startsWith("com.genir.aitweaks.core") -> {
                // Load and transform AI Tweaks core logic classes.
                val classBuffer = Bytecode.readClassBuffer(this, name)
                val obfuscated = obfuscator.apply(obfuscator.apply(classBuffer))
                defineClass(name, obfuscated, 0, obfuscated.size)
            }

            else -> try {
                // Load Java and Starsector classes, including the classes
                // restricted by Starsector reflection ban.
                ClassLoader.getSystemClassLoader().loadClass(name)
            } catch (_: ClassNotFoundException) {
                // Load mod classes using default Starsector script loader.
                // This ensures that AI Tweaks' core logic uses the same class
                // definitions as the rest of the application, including AI Tweaks
                // launcher. Using the same class definitions is important when
                // sharing state through static fields, as in the case of LunaLib
                // settings.
                Global.getSettings().scriptClassLoader.loadClass(name)
            }
        }

        cache[name] = c
        return c
    }

    companion object {
        /** Find the URL of latest aitweaks-core-dev-*.jar. This is useful for
         * replacing classes without restarting the game or reloading a save,
         * with more flexibility than the usual Java debugger hot reload. */
        private fun latestCoreURL(): URL {
            // Find all aitweaks-core dev jars.
            val jarsDir: File = Path(launcherURL().path.removePrefix("/")).parent.toFile()
            val devJars: List<String>? = jarsDir.list()?.filter { it.contains("aitweaks-core-dev") }

            // Find the latest dev aitweaks-core jar, if any.
            val coreJar: String = if (devJars?.isNotEmpty() != true) "aitweaks-core.jar"
            else devJars.fold(devJars[0]) { latest, it -> if (it > latest) it else latest }

            // Return the URL of AI Tweaks core jar.
            return URL(launcherURL().toString().replace("aitweaks-launcher.jar", coreJar))
        }

        /** AI Tweaks launcher jar URL. */
        private fun launcherURL(): URL {
            val urLs: Array<URL> = (this::class.java.classLoader as URLClassLoader).urLs
            return urLs.first { it.path.contains("aitweaks-launcher.jar") }
        }
    }
}
