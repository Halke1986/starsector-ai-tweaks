SUGGESTED: ships avoid collisions with very large modded shields

----------------------------------------------------------------

- attack enemy ships that are inflicting damage

- sometimes guided finisher missiles are ignored when venting, especially on phase ships

- area targeting for devastator

- do not track targets with angular velocity higher than weapon rotation

- avoid blocking burn vector via collision avoidance

- revisit hiding behind allies to vent

- extract vanilla missile decisioning

- take damage type into account when calculating history of calculated damage, to not ignore hellbores when venting

- try to attack same enemy battle group during initial split

- notify systems of overridden facing in extended AI

check Lidar on faster ships - destroyer : ass pinetree, frigate DEX Catapult

- why SO ships backing off so far away
- nova backing off too far to vent
    - probably missileDangerDir

- tahlan shipworks hel hound module has speed 0

SUGGESTED by Hyperkayak:
Preface: haven't updated to .98 yet.
I know this is mainly for maneuvering and guns,
BUT
Another 0pt hullmod suggestion.
[name pending, because "just *** GO already" doesn't fit with the others' theme]
What it does: Ship with this hullmod (and/or the strikecraft wings themselves, idk how the AI works) is/are made completely incapable of targeting friendly units with strikecraft actions.
Context: I'm sick and tired of carriers (even reckless ones) sending their swarms of bees to buzz around some expendable friggie that's off to the side capturing a POI and not even in any danger while there are valid enemy targets in strike range. Unless it's all bombers. Bombers seem to work okay-ish.
I'm slightly-less-but-still-substantially sick and tired of ppl going "bro just order strikes on every single enemy bro".

----------------------------------------------------------------

REPOTRED by Lprsti99:
Having an issue with the shield assist, where if it's toggled on when a battle ends, it starts enabled in the next battles, but it's nonfunctional - doesn't raise the shield, and if I raise it manually it doesn't control it at all.  toggling it off and on doesn't solve this, the only fix I've found is to make sure it's toggled off at the end of *that* battle, in which case it's functional again next time.  Possibly a mod incompatibility - if so, my immediate suspect is Puretilt's QOL pack, due to the shield facing settings in there.  I mainly use the qol pack for automatically toggling the transponder in hyperspace anyway, but also possible that something in the 0.98 update caused it maybe? Keep forgetting to try disabling the qol mod and I can't try now unfortunately, irl stuff going on (I only just happened to think to make a comment).
fast time aim assist

Temporal shell anubis vibrates.

Custom LPC, issue with custom AI on Pankrator from Symmetrical Ships

check finish burst target, AI seems to break burst with tach lance

DO_NOT_BACK_OFF flag
HAS_INCOMING_DAMAGE flag, when adding custom threat evaluation

ships that use the custom AI hullmod do not target and hunt down enemy fighters if they are the last enemy remaining

----------------------------------------------------------------
