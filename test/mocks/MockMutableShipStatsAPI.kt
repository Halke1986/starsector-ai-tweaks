package mocks

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.util.DynamicStatsAPI

class MockMutableShipStatsAPI(vararg values: Pair<String, Any?>) : MutableShipStatsAPI, Mock(*values) {
    override fun getEntity(): CombatEntityAPI = getMockValue(object {})!!

    override fun getFleetMember(): FleetMemberAPI = getMockValue(object {})!!

    override fun getMaxSpeed(): MutableStat = getMockValue(object {})!!

    override fun getAcceleration(): MutableStat = getMockValue(object {})!!

    override fun getDeceleration(): MutableStat = getMockValue(object {})!!

    override fun getMaxTurnRate(): MutableStat = getMockValue(object {})!!

    override fun getTurnAcceleration(): MutableStat = getMockValue(object {})!!

    override fun getFluxCapacity(): MutableStat = getMockValue(object {})!!

    override fun getFluxDissipation(): MutableStat = getMockValue(object {})!!

    override fun getWeaponMalfunctionChance(): MutableStat = getMockValue(object {})!!

    override fun getEngineMalfunctionChance(): MutableStat = getMockValue(object {})!!

    override fun getCriticalMalfunctionChance(): MutableStat = getMockValue(object {})!!

    override fun getShieldMalfunctionChance(): MutableStat = getMockValue(object {})!!

    override fun getShieldMalfunctionFluxLevel(): MutableStat = getMockValue(object {})!!

    override fun getMaxCombatReadiness(): MutableStat = getMockValue(object {})!!

    override fun getCRPerDeploymentPercent(): StatBonus = getMockValue(object {})!!

    override fun getPeakCRDuration(): StatBonus = getMockValue(object {})!!

    override fun getCRLossPerSecondPercent(): StatBonus = getMockValue(object {})!!

    @Deprecated("Deprecated in Java")
    override fun getFluxDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getEmpDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getHullDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getArmorDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getEngineDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getWeaponDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getBeamDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getMissileDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getProjectileDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getEnergyDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getKineticDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getHighExplosiveDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getFragmentationDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getBeamShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getMissileShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getProjectileShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getEnergyShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getKineticShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getHighExplosiveShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getFragmentationShieldDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getBeamWeaponDamageMult(): MutableStat = getMockValue(object {})!!

    override fun getEnergyWeaponDamageMult(): MutableStat = getMockValue(object {})!!

    override fun getBallisticWeaponDamageMult(): MutableStat = getMockValue(object {})!!

    override fun getMissileWeaponDamageMult(): MutableStat = getMockValue(object {})!!

    override fun getEnergyWeaponFluxCostMod(): StatBonus = getMockValue(object {})!!

    override fun getBallisticWeaponFluxCostMod(): StatBonus = getMockValue(object {})!!

    override fun getMissileWeaponFluxCostMod(): StatBonus = getMockValue(object {})!!

    override fun getBeamWeaponFluxCostMult(): MutableStat = getMockValue(object {})!!

    override fun getShieldUpkeepMult(): MutableStat = getMockValue(object {})!!

    override fun getShieldAbsorptionMult(): MutableStat = getMockValue(object {})!!

    override fun getShieldTurnRateMult(): MutableStat = getMockValue(object {})!!

    override fun getShieldUnfoldRateMult(): MutableStat = getMockValue(object {})!!

    override fun getMissileRoFMult(): MutableStat = getMockValue(object {})!!

    override fun getBallisticRoFMult(): MutableStat = getMockValue(object {})!!

    override fun getEnergyRoFMult(): MutableStat = getMockValue(object {})!!

    override fun getPhaseCloakActivationCostBonus(): StatBonus = getMockValue(object {})!!

    override fun getPhaseCloakUpkeepCostBonus(): StatBonus = getMockValue(object {})!!

    override fun getEnergyWeaponRangeBonus(): StatBonus = getMockValue(object {})!!

    override fun getBallisticWeaponRangeBonus(): StatBonus = getMockValue(object {})!!

    override fun getMissileWeaponRangeBonus(): StatBonus = getMockValue(object {})!!

    override fun getBeamWeaponRangeBonus(): StatBonus = getMockValue(object {})!!

    override fun getWeaponTurnRateBonus(): StatBonus = getMockValue(object {})!!

    override fun getBeamWeaponTurnRateBonus(): StatBonus = getMockValue(object {})!!

    override fun getCombatEngineRepairTimeMult(): MutableStat = getMockValue(object {})!!

    override fun getCombatWeaponRepairTimeMult(): MutableStat = getMockValue(object {})!!

    override fun getWeaponHealthBonus(): StatBonus = getMockValue(object {})!!

    override fun getEngineHealthBonus(): StatBonus = getMockValue(object {})!!

    override fun getArmorBonus(): StatBonus = getMockValue(object {})!!

    override fun getHullBonus(): StatBonus = getMockValue(object {})!!

    override fun getShieldArcBonus(): StatBonus = getMockValue(object {})!!

    override fun getBallisticAmmoBonus(): StatBonus = getMockValue(object {})!!

    override fun getEnergyAmmoBonus(): StatBonus = getMockValue(object {})!!

    override fun getMissileAmmoBonus(): StatBonus = getMockValue(object {})!!

    override fun getEccmChance(): MutableStat = getMockValue(object {})!!

    override fun getMissileGuidance(): MutableStat = getMockValue(object {})!!

    override fun getSightRadiusMod(): StatBonus = getMockValue(object {})!!

    override fun getHullCombatRepairRatePercentPerSecond(): MutableStat = getMockValue(object {})!!

    override fun getMaxCombatHullRepairFraction(): MutableStat = getMockValue(object {})!!

    @Deprecated("Deprecated in Java")
    override fun getHullRepairRatePercentPerSecond(): MutableStat = getMockValue(object {})!!

    @Deprecated("Deprecated in Java")
    override fun getMaxHullRepairFraction(): MutableStat = getMockValue(object {})!!

    override fun getEffectiveArmorBonus(): StatBonus = getMockValue(object {})!!

    override fun getHitStrengthBonus(): StatBonus = getMockValue(object {})!!

    override fun getDamageToTargetEnginesMult(): MutableStat = getMockValue(object {})!!

    override fun getDamageToTargetWeaponsMult(): MutableStat = getMockValue(object {})!!

    override fun getDamageToTargetShieldsMult(): MutableStat = getMockValue(object {})!!

    override fun getDamageToTargetHullMult(): MutableStat = getMockValue(object {})!!

    override fun getAutofireAimAccuracy(): MutableStat = getMockValue(object {})!!

    override fun getMaxRecoilMult(): MutableStat = getMockValue(object {})!!

    override fun getRecoilPerShotMult(): MutableStat = getMockValue(object {})!!

    override fun getRecoilDecayMult(): MutableStat = getMockValue(object {})!!

    override fun getOverloadTimeMod(): StatBonus = getMockValue(object {})!!

    override fun getZeroFluxSpeedBoost(): MutableStat = getMockValue(object {})!!

    override fun getZeroFluxMinimumFluxLevel(): MutableStat = getMockValue(object {})!!

    override fun getCrewLossMult(): MutableStat = getMockValue(object {})!!

    override fun getHardFluxDissipationFraction(): MutableStat = getMockValue(object {})!!

    override fun getFuelMod(): StatBonus = getMockValue(object {})!!

    override fun getFuelUseMod(): StatBonus = getMockValue(object {})!!

    override fun getMinCrewMod(): StatBonus = getMockValue(object {})!!

    override fun getMaxCrewMod(): StatBonus = getMockValue(object {})!!

    override fun getCargoMod(): StatBonus = getMockValue(object {})!!

    override fun getHangarSpaceMod(): StatBonus = getMockValue(object {})!!

    override fun getMissileMaxSpeedBonus(): StatBonus = getMockValue(object {})!!

    override fun getMissileAccelerationBonus(): StatBonus = getMockValue(object {})!!

    override fun getMissileMaxTurnRateBonus(): StatBonus = getMockValue(object {})!!

    override fun getMissileTurnAccelerationBonus(): StatBonus = getMockValue(object {})!!

    override fun getProjectileSpeedMult(): MutableStat = getMockValue(object {})!!

    override fun getVentRateMult(): MutableStat = getMockValue(object {})!!

    override fun getBaseCRRecoveryRatePercentPerDay(): MutableStat = getMockValue(object {})!!

    override fun getMaxBurnLevel(): MutableStat = getMockValue(object {})!!

    override fun getFighterRefitTimeMult(): MutableStat = getMockValue(object {})!!

    override fun getRepairRatePercentPerDay(): MutableStat = getMockValue(object {})!!

    override fun getSensorProfile(): MutableStat = getMockValue(object {})!!

    override fun getSensorStrength(): MutableStat = getMockValue(object {})!!

    override fun getDynamic(): DynamicStatsAPI = getMockValue(object {})!!

    override fun getSuppliesToRecover(): MutableStat = getMockValue(object {})!!

    override fun getSuppliesPerMonth(): MutableStat = getMockValue(object {})!!

    override fun getWeaponRangeThreshold(): MutableStat = getMockValue(object {})!!

    override fun getWeaponRangeMultPastThreshold(): MutableStat = getMockValue(object {})!!

    override fun getTimeMult(): MutableStat = getMockValue(object {})!!

    override fun getBeamPDWeaponRangeBonus(): StatBonus = getMockValue(object {})!!

    override fun getNonBeamPDWeaponRangeBonus(): StatBonus = getMockValue(object {})!!

    override fun getMinArmorFraction(): MutableStat = getMockValue(object {})!!

    override fun getMaxArmorDamageReduction(): MutableStat = getMockValue(object {})!!

    override fun getNumFighterBays(): MutableStat = getMockValue(object {})!!

    override fun getMissileHealthBonus(): StatBonus = getMockValue(object {})!!

    override fun getPhaseCloakCooldownBonus(): StatBonus = getMockValue(object {})!!

    override fun getSystemCooldownBonus(): StatBonus = getMockValue(object {})!!

    override fun getSystemRegenBonus(): StatBonus = getMockValue(object {})!!

    override fun getSystemUsesBonus(): StatBonus = getMockValue(object {})!!

    override fun getSystemRangeBonus(): StatBonus = getMockValue(object {})!!

    override fun getKineticArmorDamageTakenMult(): MutableStat = getMockValue(object {})!!

    override fun getDamageToFighters(): MutableStat = getMockValue(object {})!!

    override fun getDamageToMissiles(): MutableStat = getMockValue(object {})!!

    override fun getDamageToFrigates(): MutableStat = getMockValue(object {})!!

    override fun getDamageToDestroyers(): MutableStat = getMockValue(object {})!!

    override fun getDamageToCruisers(): MutableStat = getMockValue(object {})!!

    override fun getDamageToCapital(): MutableStat = getMockValue(object {})!!

    override fun getCriticalMalfunctionDamageMod(): StatBonus = getMockValue(object {})!!

    override fun getBreakProb(): MutableStat = getMockValue(object {})!!

    override fun getFighterWingRange(): StatBonus = getMockValue(object {})!!

    override fun getVariant(): ShipVariantAPI = getMockValue(object {})!!

    override fun getRecoilPerShotMultSmallWeaponsOnly(): MutableStat = getMockValue(object {})!!

    override fun getEnergyWeaponFluxBasedBonusDamageMagnitude(): MutableStat = getMockValue(object {})!!

    override fun getEnergyWeaponFluxBasedBonusDamageMinLevel(): MutableStat = getMockValue(object {})!!

    override fun getAllowZeroFluxAtAnyLevel(): MutableStat = getMockValue(object {})!!

    override fun getListenerManager(): CombatListenerManagerAPI = getMockValue(object {})!!

    override fun addListener(listener: Any?) = Unit

    override fun removeListener(listener: Any?) = Unit

    override fun removeListenerOfClass(c: Class<*>?) = Unit

    override fun hasListener(listener: Any?): Boolean = getMockValue(object {})!!

    override fun hasListenerOfClass(c: Class<*>?): Boolean = getMockValue(object {})!!

    override fun <T : Any?> getListeners(c: Class<T>?): MutableList<T> = getMockValue(object {})!!

    override fun getBallisticProjectileSpeedMult(): MutableStat = getMockValue(object {})!!

    override fun getEnergyProjectileSpeedMult(): MutableStat = getMockValue(object {})!!

    override fun getMissileAmmoRegenMult(): MutableStat = getMockValue(object {})!!

    override fun getEnergyAmmoRegenMult(): MutableStat = getMockValue(object {})!!

    override fun getBallisticAmmoRegenMult(): MutableStat = getMockValue(object {})!!

    override fun getShieldSoftFluxConversion(): MutableStat = getMockValue(object {})!!
}