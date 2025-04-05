SUGGESTED: ships avoid collisions with very large modded shields

----------------------------------------------------------------

- attack enemy ships that are inflicting damage

- sometimes guided finisher missiles are ignored when venting, especially on phase ships

- ships with assignment should have priority, at least for player fleet.

- area targeting for devastator

- do not track targets with angular velocity higher than weapon rotation

- avoid blocking burn vector via collision avoidance

- revisit hiding behind allies to vent

- extract vanilla missile decisioning

- take damage type into account when calculating history of calculated damage, to not ignore hellbores when venting

- aim slow turrets
- DEPRECATED: use front facing turrets with rotation speed lower than ship turn rate as hardpoints
- unless flamed out
- not for player

- try to attack same enemy battle group during initial split

- notify systems of overridden facing in extended AI

check Lidar on faster ships - destroyer : ass pinetree, frigate DEX Catapult

- why SO ships backing off so far away
- nova backing off too far to vent
    - probably missileDangerDir

- tahlan shipworks hel hound module has speed 0

- why manticore targets fighters

- weapons on invictus not shooting sometimes, when one is disabled?

SUGGESTED by Hyperkayak:
Preface: haven't updated to .98 yet.
I know this is mainly for maneuvering and guns,
BUT
Another 0pt hullmod suggestion.
[name pending, because "just *** GO already" doesn't fit with the others' theme]
What it does: Ship with this hullmod (and/or the strikecraft wings themselves, idk how the AI works) is/are made completely incapable of targeting friendly units with strikecraft actions.
Context: I'm sick and tired of carriers (even reckless ones) sending their swarms of bees to buzz around some expendable friggie that's off to the side capturing a POI and not even in any danger while there are valid enemy targets in strike range. Unless it's all bombers. Bombers seem to work okay-ish.
I'm slightly-less-but-still-substantially sick and tired of ppl going "bro just order strikes on every single enemy bro".

REPORTED by Malice:
Hey!

    Tested a little AI Tweaks with AGC about auto shield rotation deployment bug:
    Played some battles in campaign and some missions in the main menu to confirm relation.


    Missions in main menu: Nothing Personal, Hornet's Nest, The Last Hurrah. Auto-shield's not working properly in missions, possible relation explained further.


In campaign tested on Aurora, every fights (approximately 5 battles with different factions) played fine without a bug, until I thought about it: "what if this somehow related to a number of ships?" I spawned 10 Atlases in my fleet and fought remnants, with whom I fought without bug with one ship, Aurora.
Result: battles with only Aurora are fine; battles with Aurora and 10 Atlases (or in other words, battles with anything more, than Aurora) aren't, see related screenshots (first - remnants, second - independent, I didn't deployed Atlases btw).
Next, I scuttled Atlases to check if I'm right, and bug's didn't occurred, auto-shield worked fine, but If I let even only one Atlas stay in my fleet, auto-rotation will not work. This bug happened not only with Atlases, butwith others too: checked with Aurora+Enforcer, regrettably, bug occurred.

    Testing *AI Tweaks without AGC. Same test with Aurora.


*Missions played fine, without a bug.

*Campaign tested likewise, bug didn't occurred, despite presence of more than one ship in the fleet with Aurora.

    Conclusion, there's actually an auto-shield bug with AGC, that happens when there's more than one ship in the fleet, otherwise one ship will be fine.


REPOTRED by Lprsti99:
Having an issue with the shield assist, where if it's toggled on when a battle ends, it starts enabled in the next battles, but it's nonfunctional - doesn't raise the shield, and if I raise it manually it doesn't control it at all.  toggling it off and on doesn't solve this, the only fix I've found is to make sure it's toggled off at the end of *that* battle, in which case it's functional again next time.  Possibly a mod incompatibility - if so, my immediate suspect is Puretilt's QOL pack, due to the shield facing settings in there.  I mainly use the qol pack for automatically toggling the transponder in hyperspace anyway, but also possible that something in the 0.98 update caused it maybe? Keep forgetting to try disabling the qol mod and I can't try now unfortunately, irl stuff going on (I only just happened to think to make a comment).

----------------------------------------------------------------


PLAYER ASSIST EXECUTE COMMANDS !!!!!!!!!!!!!!!!!!
and extended ship AI