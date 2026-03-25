package com.genir.aitweaks.launcher.loading

import com.fs.starfarer.api.Global
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.io.path.Path

class CoreLoader : URLClassLoader(arrayOf(latestCoreURL())) {
    private val cache: MutableMap<String, Class<*>> = mutableMapOf()

    private val obfuscator by lazy {
        val symbols: Symbols by lazy { Symbols() }

        Transformer(listOf(
            Transformer.newTransform("com.genir.starfarer", "com.fs.starfarer"),
            Transformer.newTransform("com.genir.graphics", "com.fs.graphics"),

            // Classes.
            Transformer.newTransform("com.genir.starfarer.combat.ai.movement.FlockingAI", symbols.flockingAI.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.movement.maneuvers.ApproachManeuver", symbols.approachManeuver.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.attack.AutofireManager", symbols.autofireManager.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.movement.maneuvers.Maneuver", symbols.maneuver.name),
            Transformer.newTransform("com.genir.starfarer.combat.entities.Ship\$CommandWrapper", symbols.shipCommandWrapper.name),
            Transformer.newTransform("com.genir.starfarer.combat.entities.Ship\$Command", symbols.shipCommand.name),
            Transformer.newTransform("com.genir.starfarer.combat.collision.CombatEntity", symbols.combatEntity.name),
            Transformer.newTransform("com.genir.starfarer.combat.systems.Weapon", symbols.weapon.name),
            Transformer.newTransform("com.genir.starfarer.combat.entities.ship.trackers.AimTracker", symbols.aimTracker.name),
            Transformer.newTransform("com.genir.starfarer.title.input.Keymap\$PlayerAction", symbols.playerAction.name),
            Transformer.newTransform("com.genir.starfarer.title.input.Keymap", symbols.keymap.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.FighterPullbackModule", symbols.fighterPullbackModule.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.system.SystemAI", symbols.systemAI.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.ShieldAI", symbols.shieldAI.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.ThreatEvaluator", symbols.threatEvaluator.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.ThreatEvaluator\$ThreatResponseManeuver", symbols.threatResponseManeuver.name),
            Transformer.newTransform("com.genir.starfarer.combat.map.CombatMap", symbols.combatMap.name),
            Transformer.newTransform("com.genir.starfarer.title.mission.MissionDefinition\$PluginContainer", symbols.missionDefinitionPluginContainer.name),
            Transformer.newTransform("com.genir.starfarer.combat.entities.ship.weapons.BeamWeapon", symbols.beamWeapon.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.FrontShieldAI", symbols.frontShieldAI.name),
            Transformer.newTransform("com.genir.starfarer.combat.ai.OmniShieldAI", symbols.omniShieldAI.name),
            Transformer.newTransform("com.genir.starfarer.combat.collision.Bounds", symbols.bounds.name),
            Transformer.newTransform("com.genir.starfarer.combat.collision.Bounds\$Segment", symbols.boundsSegment.name),
            Transformer.newTransform("com.genir.starfarer.combat.tasks.CombatTaskManager\$DeployedFleetMember", symbols.deployedFleetMember.name),
            Transformer.newTransform("com.genir.starfarer.combat.entities.ship.trackers.BeamChargeTracker", symbols.beamChargeTracker.name),
            Transformer.newTransform("com.genir.starfarer.combat.entities.ship.trackers.BeamWeaponState", symbols.beamWeaponState.name),

            // Fields and methods.
            Transformer.newTransform("autofireManager_advance", symbols.autofireManager_advance.name),
            Transformer.newTransform("shipCommandWrapper_command", symbols.shipCommandWrapper_command.name),
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
            Transformer.newTransform("beamChargeTracker_getState", symbols.beamChargeTracker_getState.name),
        ))
    }

    override fun loadClass(name: String): Class<*> {
        cache[name]?.let { return it }

        val c: Class<*> = when {
            name.startsWith("com.genir.aitweaks.core") -> {
                // Load and transform AI Tweaks core logic classes.
                val classBuffer = Transformer.readClassBuffer(this, name)
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
