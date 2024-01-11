//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package reference;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;

public class VanillaAutofireAI implements AutofireAIPlugin {
    private static final float Ò00000 = 0.05F;
    private static final float ø00000 = 0.5F;
    private static final float ÓO0000 = 0.5F;
    private WeaponAPI weapon;
    private ShipAPI ship;

    private float timeSincePDWaponWasAllowedToAttackNonMissile;
    private float randValue0;
    private float floatField1;

    private float isFiringSince;
    private float isNotFiringSince;
    private autofireStates autofireState;
    private CombatEntityAPI target;
    private CombatEntityAPI previousTarget;
    private IntervalUtil intervalTracker;
    private boolean impossibleToHitTarget0;
    private boolean impossibleToHitTarget1;
    private boolean isAbleToAttack;

    public VanillaAutofireAI(WeaponAPI weapon) {
        this.autofireState = autofireStates.IDLE;
        this.floatField1 = 1.0F;
        this.intervalTracker = new IntervalUtil(0.25F, 0.5F);
        this.impossibleToHitTarget0 = false;
        this.impossibleToHitTarget1 = false;
        this.isAbleToAttack = true;
        this.isFiringSince = 0.0F;
        this.isNotFiringSince = 0.0F;
        this.previousTarget = null;
        this.weapon = weapon;
        this.ship = weapon.getShip();
        this.randValue0 = (float) Math.random() * 0.05F;
        if (weapon.getSize() != WeaponSize.SMALL && !weapon.hasAIHint(AIHints.PD)) {
            if (weapon.getSize() == WeaponSize.MEDIUM) {
                this.floatField1 = 0.5F;
            } else if (weapon.getSize() == WeaponSize.LARGE) {
                this.floatField1 = 1.0F;
            }
        } else {
            this.floatField1 = 0.25F;
        }
    }

    public void forceOff() {
        this.setTarget((CombatEntityAPI) null);
        this.autofireState = autofireStates.IDLE;
        this.timeSincePDWaponWasAllowedToAttackNonMissile = 0.0F;
        this.randValue0 = (float) Math.random() * 0.05F;
    }

    public void advance(float var1) {
        this.timeSincePDWaponWasAllowedToAttackNonMissile += var1;
        if (this.target != this.previousTarget) {
            this.previousTarget = this.target;
            this.isFiringSince = 0.0F;
            this.isNotFiringSince = 0.0F;
        }

        if (this.weapon.isFiring()) {
            this.isFiringSince += var1;
            this.isNotFiringSince = 0.0F;
        } else {
            this.isNotFiringSince += var1;
        }

        if (this.isNotFiringSince > 3.0F) {
            this.isFiringSince = 0.0F;
            this.isNotFiringSince = 0.0F;
        }

        if (this.weapon.isDisabled() || this.weapon.usesAmmo() && this.weapon.getAmmo() == 0) {
            this.autofireState = autofireStates.IDLE;
        } else {
            this.intervalTracker.advance(var1);
            if (this.intervalTracker.intervalElapsed()) {
                this.refreshSomeBools();
            }

            if (this.autofireState == autofireStates.HAS_TARGET && (!this.weapon.hasAIHint(AIHints.PD) || this.target instanceof MissileAPI) && this.targetIsValid() && this.isTargetWithinFiringArc(0.0F)) {
                this.ifShouldAttackCurrentTarget();
                if (this.isAbleToAttack) {
                    this.autofireState = autofireStates.ATTACK;
                }
            }

            // Probability based decision if PD weapon should attack non-missile target.
            if (this.timeSincePDWaponWasAllowedToAttackNonMissile >= this.randValue0) {
                this.findNewTarget();
                if (this.autofireState == autofireStates.HAS_TARGET) {
                    this.randValue0 = (float) Math.random() * 0.5F + 0.5F;
                    this.randValue0 *= this.floatField1;
                } else {
                    this.randValue0 = (float) Math.random() * 0.05F + 0.05F;
                }

                if (this.autofireState == autofireStates.HAS_TARGET && this.targetIsValid() && this.isTargetWithinFiringArc(0.0F)) {
                    this.ifShouldAttackCurrentTarget();
                    if (this.isAbleToAttack) {
                        this.autofireState = autofireStates.ATTACK;
                    }
                }

                this.timeSincePDWaponWasAllowedToAttackNonMissile = 0.0F;
            }

        }
    }

    private void ifShouldAttackCurrentTarget() {
        if (this.weapon.hasAIHint(AIHints.USE_LESS_VS_SHIELDS) && this.target != null && this.target instanceof ShipAPI && !((ShipAPI) this.target).isFighter()) {
            if (this.target.getShield() != null && this.target.getShield().isOn()) {
                ShipAPI ship = this.weapon.getShip();
                WeaponSlotAPI slot = this.weapon.getSlot();
                Vector2f slotPosition = slot.computePosition(ship);
                boolean willHitShields = UtilsAlias.willHitShield(this.target.getShield().getFacing(), this.target.getShield().getActiveArc(), ((ShipAPI) this.target).getShieldCenterEvenIfNoShield(), slotPosition);
                if (willHitShields) {
                    if (this.weapon.getAmmo() <= 1 || (float) this.weapon.getAmmo() <= (float) this.weapon.getMaxAmmo() * 0.8F) {
                        this.isAbleToAttack = false;
                    }

                    if (!this.isAbleToAttack && (float) this.weapon.getAmmo() >= (float) this.weapon.getMaxAmmo() * 1.0F) {
                        this.isAbleToAttack = true;
                    }

                    if (!this.weapon.usesAmmo() || this.weapon.getAmmoPerSecond() <= 0.0F) {
                        this.isAbleToAttack = false;
                    }
                } else {
                    this.isAbleToAttack = true;
                }
            } else {
                this.isAbleToAttack = true;
            }

        } else if (!this.weapon.hasAIHint(AIHints.PD) && this.weapon.usesAmmo() && !(this.weapon.getSpec().getAmmoPerSecond() <= 0.0F) && !this.weapon.hasAIHint(AIHints.DO_NOT_CONSERVE)) {
            if (this.weapon.getAmmo() <= 1) {
                this.isAbleToAttack = false;
            }

            if (!this.isAbleToAttack && ((float) this.weapon.getAmmo() >= (float) this.weapon.getMaxAmmo() * 0.34F || this.weapon.getAmmo() >= 5)) {
                this.isAbleToAttack = true;
            }

        }
    }

    private void setTarget(CombatEntityAPI target) {
        this.target = target;
        this.refreshSomeBools();
    }

    private void findNewTarget() {
        if (!this.targetIsValid() || !this.isTargetWithinFiringArc(30.0F) || this.weapon.hasAIHint(AIHints.PD) && !(this.target instanceof MissileAPI)) {
            this.setTarget((CombatEntityAPI) null);
            this.findEntityToTarget();
        } else if (this.ship.getShipTarget() != null && this.ship.getShipTarget() != this.target && (!this.weapon.hasAIHint(AIHints.PD) || !(this.target instanceof MissileAPI)) && this.ship.getShipTarget().getOriginalOwner() != this.ship.getOwner() && this.willHitTarget((Vector2f) null, this.ship.getShipTarget())) {
            this.setTarget((CombatEntityAPI) null);
            if (this.weapon.hasAIHint(AIHints.PD)) {
                this.findEntityToTarget();
            } else {
                this.setTarget(this.ship.getShipTarget());
                if (!this.targetIsValid()) {
                    this.setTarget((CombatEntityAPI) null);
                    this.findEntityToTarget();
                }
            }
        }

        if (this.target != null) {
            this.autofireState = autofireStates.HAS_TARGET;
        } else {
            this.autofireState = autofireStates.IDLE;
        }

    }

    private boolean unusedMethod() {
        return false;
    }

    private float getAutofireAccBonus() {
        return this.weapon.getSpec().getAutofireAccBonus();
    }

    private float getAutofireAcc() {
        return (this.weapon.hasAIHint(AIHints.PD) || this.weapon.hasAIHint(AIHints.PD_ONLY)) && !this.weapon.hasAIHint(AIHints.STRIKE) && this.ship != null && this.ship.getMutableStats().getDynamic().getValue("pd_best_target_leading", 0.0F) >= 1.0F ? 10000.0F : this.isFiringSince;
    }

    private void findEntityToTarget() {
        if (!this.targetIsValid() || this.weapon.hasAIHint(AIHints.PD) && !(this.target instanceof MissileAPI)) {
            boolean missileOnlyPD = this.weapon.hasAIHint(AIHints.PD_ONLY) && !this.weapon.hasAIHint(AIHints.ANTI_FTR);
            boolean generalPD = this.weapon.hasAIHint(AIHints.PD_ONLY) && this.weapon.hasAIHint(AIHints.ANTI_FTR);

            // Try to find a missile first.
            if (!this.weapon.hasAIHint(AIHints.PD_ALSO) && (this.weapon.hasAIHint(AIHints.PD) || this.weapon.hasAIHint(AIHints.PD_ONLY)) && !this.weapon.hasAIHint(AIHints.STRIKE)) {
                this.findMissileToTarget();
                if (this.targetIsValid()) {
                    return;
                }
            }

            // Try to find a ship.
            if (!missileOnlyPD) {
                if (this.weapon.hasAIHint(AIHints.PD) && this.weapon.getAmmoTracker() != null && this.weapon.getAmmoTracker().getAmmoPerSecond() > 0.0F && this.weapon.getAmmo() <= 10 && this.weapon.getSpec().getReloadSize() <= 1.0F && this.weapon.getAmmo() < this.weapon.getMaxAmmo()) {
                    if (!this.weapon.hasAIHint(AIHints.ANTI_FTR)) {
                        return;
                    }

                    generalPD = true;
                }

                ShipAPI ship = this.weapon.getShip();
                WeaponSlotAPI slot = this.weapon.getSlot();
                Vector2f slotLocation = slot.computePosition(ship);
                FogOfWarAPI fogOfWar = Global.getCombatEngine().getFogOfWar(ship.getOwner());
                CombatEngineAPI engine = Global.getCombatEngine();
                float maxFloat = Float.MAX_VALUE;
                ArrayList var9 = new ArrayList();
                float maxRange = this.weapon.getRange() * 2.0F + 50.0F;
                Iterator enemyShips = engine.getAiGridShips().getCheckIterator(slotLocation, maxRange, maxRange);

                while (enemyShips.hasNext()) {
                    Object nextShip = enemyShips.next();
                    if (nextShip instanceof ShipAPI) {
                        ShipAPI currentShip = (ShipAPI) nextShip;
                        if (ship.getOwner() != currentShip.getOwner() && currentShip.isTargetable() && (fogOfWar == null || fogOfWar.isVisible(currentShip) || ship.isDrone()) && !currentShip.isShuttlePod() && (!generalPD || currentShip.isFighter() || currentShip.isDrone()) && (!currentShip.isFighter() || !this.weapon.hasAIHint(AIHints.STRIKE) || this.weapon.hasAIHint(AIHints.PD) || this.weapon.hasAIHint(AIHints.PD_ONLY) || this.weapon.hasAIHint(AIHints.ANTI_FTR)) && (!currentShip.isFighter() || this.weapon.getType() != WeaponType.MISSILE || !this.weapon.usesAmmo() || this.weapon.hasAIHint(AIHints.DO_NOT_AIM) || this.weapon.hasAIHint(AIHints.PD) || this.weapon.hasAIHint(AIHints.PD_ONLY) || this.weapon.hasAIHint(AIHints.ANTI_FTR)) && (!currentShip.isFrigate() || currentShip.getStationSlot() != null || !this.weapon.hasAIHint(AIHints.STRIKE) || this.weapon.hasAIHint(AIHints.USE_VS_FRIGATES)) && ship.getOwner() != currentShip.getOwner() && !currentShip.isHulk() && currentShip.getOwner() != 100) {
                            float var14 = N.super0(slotLocation, currentShip, true);
                            float distance = UtilsAlias.distance(slotLocation, currentShip.getLocation());
                            boolean enemyInRange = distance <= this.weapon.getRange() + var14;
                            if (enemyInRange) {
                                Vector2f interceptLocation = N.calculateTargetLead0(ship, currentShip, this.weapon.getProjectileSpeed(), false, this.getAutofireAcc(), this.getAutofireAccBonus());
                                float var18 = 1.75F * var14 / (6.2831855F * distance) * 360.0F;
                                boolean var19 = this.weapon.hasAIHint(AIHints.DO_NOT_AIM);
                                float var20 = this.weapon.getRange();
                                boolean var21 = Utils.o00000(slot.computeMidArcAngle(ship), slot.getArc() + var18, slotLocation, interceptLocation);
                                if (var19 && !var21) {
                                    var20 *= 0.75F;
                                    var21 = true;
                                }

                                if (enemyInRange && var21) {
                                    if (ship.getShipTarget() != null && currentShip == ship.getShipTarget()) {
                                        this.setTarget(currentShip);
                                        break;
                                    }

                                    float var22 = UtilsAlias.shortestRotationToTarget(this.weapon.getCurrAngle(), slotLocation, interceptLocation);
                                    if (var22 < maxFloat) {
                                        maxFloat = var22;
                                        this.setTarget(currentShip);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!this.targetIsValid() && this.weapon.hasAIHint(AIHints.PD) && this.weapon.hasAIHint(AIHints.PD_ALSO)) {
                    this.findMissileToTarget();
                }

                if (!var9.isEmpty()) {
                    int var23 = (int) (Math.random() * (double) var9.size());
                    this.setTarget((CombatEntityAPI) var9.get(var23));
                }
            }
        }
    }

    private boolean willHitTarget(Vector2f var1, ShipAPI target) {
        WeaponSlotAPI var3 = this.weapon.getSlot();
        if (var1 == null) {
            var1 = var3.computePosition(this.ship);
        }

        float var4 = N.super0(var1, target, true);
        Vector2f var5 = N.calculateTargetLead0(this.ship, target, this.weapon.getProjectileSpeed(), false, this.getAutofireAcc(), this.getAutofireAccBonus());
        float var6 = Utils.Ø00000(var1, var5);
        float var7 = 1.75F * var4 / (6.2831855F * var6) * 360.0F;
        boolean var8 = this.weapon.hasAIHint(AIHints.DO_NOT_AIM);
        float var9 = this.weapon.getRange();
        boolean var10 = Utils.o00000(var3.computeMidArcAngle(this.ship), var3.getArc() + var7, this.ship.getLocation(), var5);
        if (var8 && !var10) {
            var9 *= 0.8F;
            var10 = true;
        }

        boolean var11 = var6 <= this.weapon.getRange() + var4;
        return var11 && var10;
    }

    private void findMissileToTarget() {
        ShipAPI ship = this.weapon.getShip();
        WeaponSlotAPI weaponSlot = this.weapon.getSlot();
        Vector2f slotLocation = weaponSlot.computePosition(ship);
        CombatEngineAPI engine = Global.getCombatEngine();
        float maxFloat = Float.MAX_VALUE;
        Object var6 = null;
        boolean var7 = false;
        float maxRange = this.weapon.getRange() * 2.0F + 100.0F;
        ArrayList var9 = new ArrayList();
        if (var6 == null) {
            Iterator missilesWithinRange = engine.getAiGridMissiles().getCheckIterator(slotLocation, maxRange, maxRange);

            label79:
            while (true) {
                MissileAPI currentMissile;
                boolean ignoreFlares;
                do {
                    do {
                        do {
                            Object nextMissile;
                            do {
                                if (!missilesWithinRange.hasNext()) {
                                    break label79;
                                }

                                nextMissile = missilesWithinRange.next();
                            } while (!(nextMissile instanceof MissileAPI));

                            currentMissile = (MissileAPI) nextMissile;
                        } while (currentMissile.getCollisionClass() == CollisionClass.NONE);
                    } while (currentMissile.getOwner() == ship.getOwner());

                    if (!currentMissile.isFlare()) {
                        break;
                    }

                    ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue("pd_ignores_flares", 0.0F) >= 1.0F;
                    ignoreFlares |= this.weapon.hasAIHint(AIHints.IGNORES_FLARES);
                } while (ignoreFlares);

                float var21 = currentMissile.getCollisionRadius();
                Vector2f var14 = N.calculateTargetLead0(ship, currentMissile, this.weapon.getProjectileSpeed(), false, this.getAutofireAcc(), this.getAutofireAccBonus());
                float var15 = Utils.Ø00000(slotLocation, var14);
                boolean var16 = var15 <= this.weapon.getRange() + var21;
                if (var16) {
                    boolean var17 = Utils.o00000(weaponSlot.computeMidArcAngle(ship), weaponSlot.getArc() + 15.0F, ship.getLocation(), var14);
                    boolean var18 = this.weapon.hasAIHint(AIHints.DO_NOT_AIM);
                    float var19 = this.weapon.getRange();
                    if (var18 && !var17) {
                        var19 *= 0.75F;
                        var17 = true;
                    }

                    if (var16 && var17) {
                        var9.add(currentMissile);
                    }
                }
            }
        }

        if (!var9.isEmpty() && (var6 == null || !var7)) {
            int var20 = (int) (Math.random() * (double) var9.size());
            this.setTarget((CombatEntityAPI) var9.get(var20));
        } else {
            this.setTarget((CombatEntityAPI) var6);
        }

    }

    private boolean targetIsValid() {
        if (this.target == null) {
            return false;
        } else if (this.target instanceof ShipAPI && ((ShipAPI) this.target).isHulk()) {
            return false;
        } else if (!Global.getCombatEngine().isInPlay(this.target)) {
            return false;
        } else if (this.target.getOwner() != 100 && this.target.getOwner() != this.weapon.getShip().getOwner()) {
            FogOfWarAPI var1 = Global.getCombatEngine().getFogOfWar(this.ship.getOwner());
            if (var1 != null && !var1.isVisible(this.target) && !this.ship.isDrone()) {
                return false;
            } else {
                boolean var2 = this.target != null && !this.target.isExpired();
                if (!var2) {
                    this.setTarget((CombatEntityAPI) null);
                }

                return var2;
            }
        } else {
            return false;
        }
    }

    private void refreshSomeBools() {
        this.impossibleToHitTarget0 = false;
        this.impossibleToHitTarget1 = false;
        if (this.target != null) {
            Vector2f slotLocation = this.weapon.getSlot().computePosition(this.ship);
            Vector2f weaponDirVector = UtilsAlias.directionalVector(this.weapon.getCurrAngle());
            weaponDirVector.scale(this.weapon.getRange());
            Vector2f.add(slotLocation, weaponDirVector, weaponDirVector);
            if (!this.isTargetWithinFiringArc(0.0F)) {
                Vector2f targetLocation = this.getTarget();
                if (targetLocation != null) {
                    weaponDirVector = targetLocation;
                }
            }

            if (N.super0(this.ship, this.target, slotLocation, weaponDirVector)) {
                this.impossibleToHitTarget0 = true;
            }

            CollisionClass collisionClass = this.weapon.getProjectileCollisionClass();
            boolean var4 = false;
            if (collisionClass != CollisionClass.RAY_FIGHTER && collisionClass != CollisionClass.PROJECTILE_FIGHTER && collisionClass != CollisionClass.HITS_SHIPS_ONLY_NO_FF && collisionClass != CollisionClass.NONE && collisionClass != CollisionClass.MISSILE_NO_FF && collisionClass != CollisionClass.PROJECTILE_NO_FF) {
                var4 = true;
            }

            if (var4) {
                float var5 = this.weapon.getRange();
                if (!this.weapon.isBeam()) {
                    var5 += 150.0F;
                }

                if (N.super0(this.ship, this.target, slotLocation, weaponDirVector, this.weapon.getProjectileSpeed(), this.weapon.getSpec().getDerivedStats().getBurstFireDuration(), var5) > 0.1F) {
                    this.impossibleToHitTarget1 = true;
                }
            }
        }

    }

    public boolean shouldFire() {
        if (this.autofireState != autofireStates.ATTACK) {
            return false;
        } else if (this.target == null) {
            return false;
        } else if (!this.impossibleToHitTarget0 && !this.impossibleToHitTarget1) {
            if (this.weapon.getFluxCostToFire() > 0) { //this.ship.getFluxAvailable()) {
                return false;
            } else {
                if (this.target instanceof ShipAPI) {
                    ShipAPI var1 = (ShipAPI) this.target;
                    if (var1.isPhased()) {
                        if (this.weapon.usesAmmo()) {
                            return false;
                        }

                        if (this.weapon.getCooldown() >= 5.0F) {
                            return false;
                        }

                        ShipwideAIFlags var2 = this.ship.getAIFlags();
                        if (var2 != null) {
                            float var8 = this.weapon.getFluxCostToFire() / this.ship.getMaxFlux();
                            float var9 = this.ship.getFluxLevel() - this.ship.getHardFluxLevel();
                            float var10 = 1.0F - this.ship.getFluxLevel();
                            boolean var6 = var9 <= 0.1F && var10 >= var8 * 4.0F;
                            return var6;
                        }

                        if (var1.isPhased() && (/*!var1.isUnphasing() ||*/ (float) Math.random() < 0.95F)) {
                            return false;
                        }
                    }

                    if (this.weapon.hasAIHint(AIHints.USE_LESS_VS_SHIELDS) && this.target != null && this.target instanceof ShipAPI && !((ShipAPI) this.target).isFighter() && this.target.getShield() != null && this.target.getShield().isOn()) {
                        ShipAPI var7 = this.weapon.getShip();
                        WeaponSlotAPI var3 = this.weapon.getSlot();
                        Vector2f var4 = var3.computePosition(var7);
                        boolean var5 = Utils.o00000(this.target.getShield().getFacing(), this.target.getShield().getActiveArc(), ((ShipAPI) this.target).getShieldCenterEvenIfNoShield(), var4);
                        if (var5 && (this.weapon.getAmmo() <= 1 || (float) this.weapon.getAmmo() <= (float) this.weapon.getMaxAmmo() * 0.75F)) {
                            return false;
                        }
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public Vector2f getTarget() {
        if (this.target == null) {
            return null;
        } else {
            Vector2f var1 = N.calculateTargetLead0(this.weapon.getShip(), this.target, this.weapon.getProjectileSpeed(), false, this.getAutofireAcc(), this.getAutofireAccBonus());
            return var1;
        }
    }

    public ShipAPI getTargetShip() {
        return this.target instanceof ShipAPI ? (ShipAPI) this.target : null;
    }

    public MissileAPI getTargetMissile() {
        return this.target instanceof MissileAPI ? (MissileAPI) this.target : null;
    }

    public WeaponAPI getWeapon() {
        return this.weapon;
    }

    private boolean isTargetWithinFiringArc(float unused) {
        if (this.target == null) {
            return false;
        } else if (this.weapon.getAmmo() <= 0) {
            return false;
        } else {
            ShipAPI ship = this.weapon.getShip();
            Vector2f targetLeadLocation = N.calculateTargetLead0(ship, this.target, this.weapon.getProjectileSpeed(), true, this.getAutofireAcc(), this.getAutofireAccBonus());
            if (targetLeadLocation == null) {
                return false;
            } else {
                Vector2f weaponLocation = this.weapon.getSlot().computePosition(ship);
                float var5 = N.super0(weaponLocation, this.target, true);
                float var6 = Utils.Ø00000(weaponLocation, targetLeadLocation);
                float var7 = 1.5F * var5 * 0.5F / (6.2831855F * var6) * 360.0F;
                var7 += this.weapon.getSpec().getExtraArcForAI() / 2.0F;
                if ((this.weapon.hasAIHint(AIHints.DO_NOT_AIM) || Utils.o00000(this.weapon.getSlot().computeMidArcAngle(ship), this.weapon.getSlot().getArc() + var7 * 2.0F, weaponLocation, targetLeadLocation)) && var6 <= this.weapon.getRange() + var5) {
                    float angleTowardsTarget = UtilsAlias.angleTowards(weaponLocation, targetLeadLocation);
                    float var9 = UtilsAlias.shortestRotation(this.weapon.getCurrAngle(), angleTowardsTarget);
                    var9 -= var7;
                    if (var9 < 0.0F || this.weapon.hasAIHint(AIHints.DO_NOT_AIM)) {
                        var9 = 0.0F;
                    }

                    return !(var9 > 0.0F);
                } else {
                    return false;
                }
            }
        }
    }

    private static enum autofireStates {
        IDLE, HAS_TARGET, ATTACK;

        private autofireStates() {
        }
    }
}
