SUGGESTED: ships avoid collisions with very large modded shields

----------------------------------------------------------------

- attack enemy ships that are inflicting damage

- area targeting for devastator

- avoid blocking burn vector via collision avoidance

- revisit hiding behind allies to vent

- extract vanilla missile decisioning

- try to attack same enemy battle group during initial split

- notify systems of overridden facing in extended AI

check Lidar on faster ships - destroyer : ass pinetree, frigate DEX Catapult

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

HAS_INCOMING_DAMAGE flag, when adding custom threat evaluation

redo integration with leading pip (or maybe just remove it?)

take a look at progressive staggered fire

approach into missile range

shieldless ships should ignore all non-missile weapons when deciding if to vent 

vent logic:
  - calculate proper damage for dangerous weapons, remove finisherMissile flag
  - properly handle non-guided missiles

----------------------------------------------------------------

https://fractalsoftworks.com/forum/index.php?topic=28428.msg507813#msg507813

Fossic:
Can the monitor be kept on F?
The IQ of most gunboats has increased, but it will affect the AI of the monitor and prevent the monitor from developing skills. 

Take DEMs into account.
Lelboihi
 — 
18/01/2026, 16:49
@Genir 
this isnt the ai tweaks prerelease channel but uhhh
local odyssey (IX) decides to drop shields and vent infront of 10 dragonfires fired by strike pegasus, fucking dies

----------------------------------------------------------------
