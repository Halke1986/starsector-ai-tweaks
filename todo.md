SUGGESTED: ships avoid collisions with very large modded shields

----------------------------------------------------------------

- attack enemy ships that are inflicting damage

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

vent logic:
  - calculate proper damage for dangerous weapons, remove finisherMissile flag
  - properly handle non-guided missiles


Wish Dragon
 — 
00:14
ah see, I play with a relatively even or slightly advantaged field, because the normal game is too easy. Ships have a random chance to have a mod that gives them significantly more power, so some ships are dramatically more dangerous than others, which I think is why it doesn't work
they require more focused fire
and can't be hard rushed

----------------------------------------------------------------

Take DEMs into account.
Lelboihi
 — 
18/01/2026, 16:49
@Genir 
this isnt the ai tweaks prerelease channel but uhhh
local odyssey (IX) decides to drop shields and vent infront of 10 dragonfires fired by strike pegasus, fucking dies


coordination between cruisers and frigates

Cherman0 
Also, unsure if the scope of the mod extends to specific weapons but two things I have noticed:
The Rift Lance could probably get the Finisher Beam Protocol treatment given that it's basically just a Phase Lance in a small slot.
The Antimatter SRM Launcher should probably have its DO_NOT_CONSERVE tag removed since the AI tends to waste them all against shield immediately, which is both harmful to its own flux grid at 1000 flux per missile and leaves it rather unimpressive at a 1000 damage missile every 20 seconds. It gets even worse if EMR and/or Missile Specialization are involved as the AI will just immediately flux itself out opening by spending 9000 flux at long range. Their primary role is also mysteriously listed as "Anti Small Craft" when they are more like an Atropos or Harpoon in their effective use-case.

----------------------------------------------------------------
 