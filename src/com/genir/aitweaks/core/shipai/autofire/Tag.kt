package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.loading.LoadingUtils
import org.json.JSONArray

enum class Tag {
    FINISHER_BEAM,      // Weapon is affected by the Finisher Beam Protocol hullmod.
    APPROACH_CLOSER,    // Ships with Custom AI approach to 80% of the weapon maximum range.
    ANTI_SHIELD,        // Weapon will attack enemy ships only when their shields are raised
    TRIGGER_HAPPY,      // Weapon will use a specialized AI, which ignores some of the hold-fire rules.
    NO_MODDED_AI,       // Weapon is not subject to AI Tweaks modded behavior.
    ANTI_FIGHTER,       // Same as vanilla ANTO_FTR AI hint.
    NO_STAGGERED_FIRE,  // Weapon will not use staggered firing mode.
    USE_LESS_VS_SHIELDS // Same as vanilla USE_LESS_VS_SHIELDS; weapon will attack enemy ships only when their shields are down.
}

private val weaponTags: MutableMap<String, Set<Tag>> = mutableMapOf()

/** Check if the weapon has an AI tag appended to /data/weapons/weaponId.ait file. */
fun WeaponAPI.hasAITag(tag: Tag): Boolean {
    return tag in AITags
}

val WeaponAPI.AITags: Set<Tag>
    get() {
        val id: String = spec?.weaponId ?: return setOf()
        weaponTags[id]?.let { return it }

        val newTags = try {
            val path = "data/weapons/${id}.ait"
            val json = LoadingUtils.loadingUtils_loadSpec(path, null)

            val jsonTags: JSONArray? = json.optJSONArray("aiTag")
            if (jsonTags == null) setOf()
            else {
                val idx = (0 until jsonTags.length())
                idx.map { enumValueOf<Tag>(jsonTags.getString(it)) }.toSet()
            }
        } catch (_: Exception) {
            setOf()
        }

        weaponTags[id] = newTags
        return newTags
    }