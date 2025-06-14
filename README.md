AI Tweaks
=========

AI Tweaks provides various improvements focused on ship AI and AI-assisted player support.

The key features of AI Tweaks are **autofire AI written from scratch**, **custom ship AI created mostly from scratch**,
**automatic omni shields and aim assist for the player’s ship**, and various upgrades to vanilla AI. AI Tweaks also
allows players to configure the personality of automated ships in their fleet. A full description of all features is
available at the bottom of the post.

### Balance notes ###

The AI changes may disrupt the game balance to some extent, as the ship defense AI is not adjusted to counter improved
weapon usage. There's a [forum thread](https://fractalsoftworks.com/forum/index.php?topic=28364.0) featuring insights
from Starsector developer Alex on certain aspects of AI modding and their effects on game balance.

Additionally, Vanilla AI is designed so its actions resemble those of a human. Certain AI limitations, such as reaction
delays, are intentionally implemented. You can read more about this in
this [forum thread](https://fractalsoftworks.com/forum/index.php?topic=11437.0). AI Tweaks does not share this design
goal, so you may notice some uncanny, machine-like behavior.

### Installation ###

Requires [LazyLib](https://fractalsoftworks.com/forum/index.php?topic=5444.0)
and [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658.0). The mod can be safely added to existing
saves.

To install AI Tweaks, just unzip the downloaded archive into Starsector mods folder.

### Uninstallation ###

AI Tweaks can be safely uninstalled without affecting existing save games.

**NOTE:** Uninstalling AI Tweaks from a save that also
uses [Starship Legends](https://fractalsoftworks.com/forum/index.php?topic=15321.0) may result in save game corruption.
Otherwise, AI Tweaks and Starship Legends are fully compatible.

### Credits ###

I took a lot of inspiration on how to code AI Tweaks from
DesperatePeter's [AdvancedGunneryControl](https://fractalsoftworks.com/forum/index.php?topic=21280.0) mod. Especially
the method of attaching AutofireAIPlugins to existing ships and preserving the underlying vanilla logic. If you want
even more customizable weapon behavior, AdvancedGunneryControl has it!

AI Improvements
---------------

### 1. Custom ship AI ###

A reworked ship AI, replacing a significant part of vanilla logic with custom implementation. It's still work in
progress, and will probably remain so for a long time. The custom AI can be activated via a hullmod on selected ship
types: non-phase, non-carrier destroyers, cruisers and capital ships.

![custom_ship_ai](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/custom_ship_ai.png)

### 2. Ships rotate to aim hardpoints and correctly handle broadside builds ###

With AI Tweaks, ships can finally aim hardpoints. Just imagine how powerful an LP Brawler becomes when it can
consistently hit its target! This change is enabled for all ships, whether or not they are equipped with the Custom AI
hullmod.

![Starsector frigates correctly aiming hardpoint weapons](https://vimeo.com/1030111629)

The upgrade also allows the AI to handle all types of broadside builds, such as the symmetrical Conquest or broadsiding
Onslaught:

![Broadside Onslaught](https://vimeo.com/1050582068)

In addition, this improvement fixes the bug mentioned in
this [forum thread](https://fractalsoftworks.com/forum/index.php?topic=28473.0), where broadside ships revert to
front-facing behavior when issued an escort order.

The fix applies to ballistic and beam weapons, but not missiles.

### 3. Autofire AI ###

AI Tweaks provides weapon AI implemented from scratch. It aims at fixing various vanilla AI deficiencies. The most
noticeable differences from vanilla autofire AI are:

#### Improved target leading algorithm ####

Vulcan cannons are finally able to reliably shoot down Salamanders!

Vanilla target leading algorithm calculates only approximate intercept point, even for ships that are supposed to have
excellent autofire accuracy. AI Tweaks replaces the vanilla algorithm with an improved one. The improved algorithm
calculates exact intercept point by solving quadratic equations. Accuracy bonus mechanism is respected, so ships with
low combat readiness will still have difficulties with target tracking. The difference is most noticeable for ship PD
weapons tracking missiles aimed at different allied ships.

Additionally, weapon fade range is taken into account when attacking shieldless targets. When a projectile passes its
maximum range, it begins to fade. Fading projectiles deal only soft flux damage to shields and its attack strength
rapidly diminishes. But they are still effective against hulls, and especially missiles. Accounting for fade increases
effective range of some PD weapons by up to 50%, and anti armor range by about 10% to 15%.

#### Staggered firing mode ####

Ships will use autofire weapons in staggered firing mode. In this mode, all weapons of the same type fire at a constant
interval.

Staggered fire mode does not apply to phase ships, allowing them to fire all weapons immediately after exiting P-space.

The feature can be configured via LunaLib settings.

![staggered_firing_mode](https://vimeo.com/1037950693)

#### Aggressive friendly fire behavior ####

Ships are finally not paralyzed by the slightest possibility of inflicting friendly fire damage. Quite the opposite,
weapons will fire shots skimming the very surfaces of allied shields! AI can afford very tight ff tolerances thanks to
improved math, which allows to reliably account for allies movement. But be warned, the math doesn't predict course
changes. So if you have frigates that like to dance in front of big guns, they will get hit. Overall, the ratio of ff
incidents will increase, but the ratio of enemy hits will increase more substantially.

#### Focusing fire on a single target ####

Vanilla AI likes to assign ship weapons to different targets, leading to situations where a ship fights all nearby
enemies, but destroys none. Starsector 0.96 contained a feature-bug that allowed to fix the
behavior ([forum thread](https://fractalsoftworks.com/forum/index.php?topic=28093.0)), but it was removed in 0.97. AI
Tweaks improve the target selection algorithm, so that weapons strongly prefer to attack the target faced by the ship.
Note that for autopilot controlled ships, true target is not the same as R-selected target.

At the same time, the weapons are not completely glued to ships target, as is the case with the bug referenced above. PD
weapons will still prioritize missiles and normal weapons will consider other targets if ships target is outside firing
arc or range. Overall, the result is much more focused fire, leading to noticeably faster kill times.

#### Specialized target leading algorithm for hardpoints ####

It is not uncommon for vanilla Autofire AI to target hardpoint weapons away from an enemy in front of the ship, wasting
opportunity to inflict damage. There are two reasons for the incorrect behavior. The vanilla AI may simply chose to
target an enemy off-axis, or it may target the correct enemy, but order the weapon to rotate too soon. In the latter
case, when the ship rotates towards the enemy, the weapon ends up over-rotated.

Modified AI predicts the enemy location and pre-aims hardpoint weapons at the correct angle, even before the ship
rotates towards the enemy. All front-facing hardpoints are affected by the change, but only on AI-piloted ships.

Example of the incorrect behavior, fixed by AI Tweaks. One of the Guardians High Intensity Lasers is aiming at the void:

![hardpoint miss](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/hardpoint_miss.jpg)

#### Improved beam weapon target switching ####

In Starsector, beam weapons have a finite travel speed, which can cause delays in reaching their targets. When a beam
weapon switches targets, rotating the existing beam to the new target may be faster and more efficient than stopping and
re-firing once aligned. This is particularly useful for point-defense (PD) beams, which often need to rapidly engage
multiple incoming missiles.

The improved AI applies only to normal beams, not burst or ammo based beams.

![beam weapon ai](https://player.vimeo.com/video/1026930513?h=9c69150945)

#### Modified anti-shield burst weapon AI ####

A simple modification preventing autofire AI from firing ammo-based kinetic weapons, as well as the Light Needler, Heavy
Needler and Storm Needler on shieldless targets. This change greatly improves needlers anti shield capability by
preventing them from wasting bursts on exposed hulls. All ships are subject to the change, no hullmod is required.

The feature can be disabled via LunaLib settings.

#### Additional changes ####

AI Tweaks autofire introduces a number of smaller changes in comparison to vanilla AI, some of which may not be
intentional. In no specific order:

* USE_LESS_VS_SHIELDS weapons (Mining Blaster and IR Autolance in vanilla) do not fire on shields when their magazines
  is above 80; vanilla AI does that to avoid "wasting" recharges
* phased ships are targeted only by beams and PD weapons, but only if the weapon is not ammo- or burst-based
* non-PD weapons attack fighters only when there are no bigger hulls in range
* shield hits are correctly predicted even for modular ships like stations, improving behavior of weapons that are
  supposed to attack shields only, or avoid shields

### 4. Fleet Cohesion AI ###

Fleet Cohesion AI fixes one of the more frustrating aspects of vanilla AI: cruisers and capital ships leaving the main
battle line and chasing lone frigates and destroyers to the edge of the map. With the fixed AI the fleet stays together
and maintains high combat effectiveness.

The feature can be disabled via LunaLib settings.

Additional details:

* Fleet Cohesion AI applies only to player fleet. Enemy fleet keeps the vanilla Admiral AI.
* Fleet Cohesion AI is disabled during full assault and when at least one AVOID order is issued.

### 5. Fixed Invictus and Lidar Array AI ###

"Cuz my problem with ai invictus is it just wastes it’s system 9/10 times" - niceman121454 on Discord.

With AI Tweaks, not anymore.

Vanilla Lidar AI has several deficiencies. Most notably, switching target in the middle of Lidar burst, described
in [Uniquifying the Factions blog post](https://fractalsoftworks.com/2022/03/18/uniquifying-the-factions-part-1/): "For
example, the lidar array AI might decide to activate it, while the “main” AI decides that it’s a good time to turn the
ship away its current target and attack a different ship." The blog post describes a solution employed in vanilla, which
doesn't seem to work.

Other deficiencies are backing off too far from the attacked target and loosing it from weapons range and poor flux
management.

AI Tweaks fixes all of the above and turns Invictus into a truly overpowered - unbalanced even - brick of a ship.

Invictus aiming hardpoint weapons with the entire ship, note the aim is not centered at the target:

![target lead](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/target_lead.png)

### 6. Improved High Energy Focus ship system AI ###

This mod changes the way AI controlled ships use their High Energy Focus system. AI will no longer be tempted to
activate HEF just because there's a fighter or missile in range of PD beams or because an enemy ship can barely be
reached by a Graviton Beam. The precious HEF charges will be preserved for big guns instead. Best use case is of course
the mighty Executor. With two linked Gigacannons and officer with System Expertise almost every salvo will be spiced up
by High Energy Focus!

Player Assist
-------------

### 1. Shield assist for player ship ###

Vanilla AI is notoriously good at controlling omni shields, often better than the player himself.

Now, with AI Tweaks it's possible to pilot ship manually while leaving shield in AI control. Be warned though.
According to playtesters the feature may be a bit too powerful. Still, "it's not so OP that it's unusable" as playtester
snark said. The assist works for both omni and front shields.

Shield assist can be toggled with `]` (right bracket) key by default. Keybinding can be configured via LunaLib
settings. When AI is in control, a circular indicator is displayed around the ship. The player can override AI and force
the shields to drop with right mouse button.

![auto omni shields](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/autoomni.jpg)

### 2. Aim Assist ###

Aim Assist helps the player by automating target leading. With Aim Assist, you can simply point the mouse at an enemy
ship and fire without worrying about projectile travel time. The AI automatically offsets manually controlled weapons to
account for both target and projectile velocity, significantly improving accuracy.

Aim Assist also enhances hardpoint aiming by adjusting the player ship’s rotation in "auto-turn to cursor", or STRAFE
LOCK mode. This works with all non-guided weapons, even those not facing forward. And yes, Aim Assist can make the
Venture Mk. II fly backwards. You can disable this ship-rotation feature in LunaLib settings.

Manually piloted Nova in STRAFE LOCK mode rotating to aim selected weapons at the enemy ship:

![aim_assist](https://vimeo.com/1041836748)

Aim Assist is toggled using the `[` (left bracket) key by default.

When active, an AIM ASSIST status icon is displayed:

![aim assist_ui](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/aim_assist.png)

AI Configuration
----------------

### 1. Changing the personality of automated ships ###

Vanilla AI forces reckless behavior on all automated ships in the player's fleet, with AI core captains and without,
presumably to maintain game balance. This results in automated ships rushing headlong into enemy deathball and getting
themselves killed in the most frustrating manner.

AI Tweaks allows to configure the personality of automated ships. The configuration is made via the LunaLib settings and
is global, affecting all automated ship in the player's fleet. If no specific personality is configured, the automated
ships will default to aggressive behavior, instead of the vanilla reckless.

### 2. Finisher Beam Protocol hullmod ###

A hullmod that changes how ship AI uses several beam weapons:

![finisher beam protocol](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/finisher_beam_protocol.png)

"A set of software modifications and officer training routines designed to modify the firing behavior of certain beam
weapons. Under the Protocol, the modified weapons are inhibited from firing at shields. This allows to reliably hit
exposed hull surfaces the moment the shields drop and to conserve flux capacity. Affected weapons are burst beams like
Tachyon Lance and Phase Lance, as well as High Explosive beams like High Intensity Laser. The Protocol is active only
for weapons in autofire mode and does not affect point defense beams."

Details

The hullmod prevents selected beam weapons from firing at shields by adding USE_LESS_VS_SHIELDS tag. Additionally, the
weapons are prevented from shooting at fighters.

For a weapon to be subject to Finisher Beam Protocol, it needs to have `FINISHER_BEAM` aiTweaksTag in
its `/data/weapons/weaponId.ait` file. And, of course, it needs to be a beam weapon:

```
{
  "aiTag": [
    "FINISHER_BEAM"
  ]
}
```

### 3. System Shunt hullmod ###

System Shunt prevents AI from using the ship system:

![system shunt](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/system_shunt.png)

"A set of officer instructions and software modifications that prevent the use of the ship's system unless directly
ordered by the fleet's first-in-command. Note: This protocol may be activated if the system's usage is deemed
detrimental to the ship's performance in combat."

### 4. Search and Destroy hullmod ###

"The ship defaults to Search and Destroy order. The ship will not be automatically assigned to Assault, Eliminate or
any other tasks. Player can manually assign the ship to any tasks.
The hullmod is suppressed during initial deployment, to allow for easy objective capping.""

![search and destroy](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/search_and_destroy.png)

### 5. Skirmisher hullmod ###

"This ship will operate on the outskirts of the battlefield, engaging targets of opportunity. While it may still join the 
main battle line, it will not prioritize doing so. On ships without Custom AI, this hullmod disables the Fleet Cohesion AI, 
allowing them to revert to vanilla behavior of chasing enemy frigates to the edge of the map. This hullmod cannot be installed 
on frigates as they are inherently considered skirmishers."

![skiremisher](https://raw.githubusercontent.com/Halke1986/starsector-ai-tweaks/master/images/skirmisher.png)
