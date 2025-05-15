package com.genir.starfarer.combat.tasks;

import com.fs.starfarer.api.combat.*;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

// UNOBFUSCATED
public class CombatTaskManager implements CombatTaskManagerAPI {
    // UNOBFUSCATED
    public boolean hasDirectOrders(DeployedFleetMember fleetMember) {
        return false;
    }

    // UNOBFUSCATED
    public boolean isCommChannelOpen() {
        return false;
    }

    // UNOBFUSCATED
    public void closeCommChannel() {
    }

    @Override
    public CombatFleetManagerAPI.AssignmentInfo getAssignmentFor(ShipAPI ship) {
        return null;
    }

    @Override
    public List<CombatFleetManagerAPI.AssignmentInfo> getAllAssignments() {
        return null;
    }

    @Override
    public CombatFleetManagerAPI.AssignmentInfo createAssignment(CombatAssignmentType type, AssignmentTargetAPI target, boolean useCommandPoint) {
        return null;
    }

    @Override
    public void giveAssignment(DeployedFleetMemberAPI member, CombatFleetManagerAPI.AssignmentInfo assignment, boolean useCommandPointIfNeeded) {

    }

    @Override
    public void orderRetreat(DeployedFleetMemberAPI member, boolean useCommandPointIfNeeded, boolean direct) {

    }

    @Override
    public void orderSearchAndDestroy(DeployedFleetMemberAPI member, boolean useCommandPointIfNeeded) {

    }

    @Override
    public void orderSearchAndDestroy() {

    }

    @Override
    public void orderFullRetreat() {

    }

    @Override
    public boolean isInFullRetreat() {
        return false;
    }

    @Override
    public MutableStat getCommandPointsStat() {
        return null;
    }

    @Override
    public int getCommandPointsLeft() {
        return 0;
    }

    @Override
    public boolean isPreventFullRetreat() {
        return false;
    }

    @Override
    public void setPreventFullRetreat(boolean preventFullRetreat) {

    }

    @Override
    public boolean isFullAssault() {
        return false;
    }

    @Override
    public void setFullAssault(boolean explicitSearchAndDestroy) {

    }

    @Override
    public float getSecondsUntilNextPoint() {
        return 0;
    }

    @Override
    public float getCPRateMult() {
        return 0;
    }

    @Override
    public float getCPInterval() {
        return 0;
    }

    @Override
    public MutableStat getCPRateModifier() {
        return null;
    }

    @Override
    public void removeAssignment(CombatFleetManagerAPI.AssignmentInfo info) {

    }

    @Override
    public void clearEmptyWaypoints() {

    }

    @Override
    public AssignmentTargetAPI createWaypoint2(Vector2f loc, boolean ally) {
        return null;
    }

    @Override
    public void setAssignmentWeight(CombatFleetManagerAPI.AssignmentInfo info, float weight) {

    }

    @Override
    public void reassign() {

    }

    @Override
    public AssignmentTargetAPI getAssignmentTargetFor(ShipAPI ship) {
        return null;
    }

    @Override
    public void clearTasks() {

    }

    @Override
    public CombatFleetManagerAPI.AssignmentInfo getAssignmentInfoForTarget(AssignmentTargetAPI target) {
        return null;
    }

    static public class DeployedFleetMember {
    }
}
