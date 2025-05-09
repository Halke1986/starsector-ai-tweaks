- Custom AI: Several minor improvement to ship movement.
- Autofire AI: Projectile PD weapons now utilize their full range more effectively when intercepting missiles.
- Custom AI: AI now targets strikecraft once all enemy ships have been destroyed. Suggested by Ruin.
- Autofire AI: Rift Beam no longer fires at targets well beyond its effective range. Reported by Ultrarattecoon and CrashToDesktop.

Version 2.1.0
 
- Player Assist: Fixed an issue where the omni shield AI kept toggling the shield on and off when the player ship wasn't in danger.
- Added Search and Destroy hullmod. It makes ships default to Search and Destroy order.
- Custom AI: Fixed a bug where ships ignored certain enemies when deciding if to vent. The bug was most apparent when fighting Shrouded Eyes.

Version 2.0.3

- Custom AI: Full assault order increases ship aggression.
- Autofire AI: Fixed an issue where the Voltaic Discharge was not discharging. Reported by CrashToDesktop. 
- Autofire AI: Voidblaster does not attack enemy shields, same as Mining Blaster.
- Autofire AI: Added LunaLib setting to enable/disable PD beam sweep (also known as "constant laser lightshow"). Suggested by Alkkaid.
- Autofire AI: Added LunaLib setting to enable/disable AI behavior where weapons tagged USE_LESS_VS_SHIELDS (such as the Mining Blaster and IR Autolance) will not fire at shields, ever. Suggested by DR, undead Changed Bean of f-mes.
- Debug: Added LunaLib setting to enable Custom AI in all eligible ships on both sides, without the hullmod. Suggested by syndrome.
- Autofire AI: Fixed a bug that caused weapons positioned inside a target’s collision radius to aim away from that target. Reported by Norath.
- Autofire AI: Staggered fire mode no longer applies to phase ships, allowing them to fire all weapons immediately after exiting P-space. Suggested by ymfah.

Version 2.0.2

- Custom AI: Fixed an issue where weapon logic delegated to vanilla AI overrode the ship target selection. The issue affected especially Invictus Lidar burst, causing weapons to hold fire.
- Custom AI: Further improved collision avoidance logic: Ships will move pass each other more effectively and not bump into each other.
- Heavy Adjudicators no longer conserve ammo for point‑defense; with the right build, the Onslaught Mk I will now broadside while Heavy Adjudicators are reloading.

Version 2.0.1

- Autofire AI: Improved aiming logic for low turn rate turrets. Now it's significantly less likely a ship will interrupt a burst when turning towards the target.
- Autofire AI: Improved aiming logic for PD weapons, Paladin in particular. Now they defend against swarms of low-HP threats more effectively. As if Anubis didn't invalidate missiles and strikecraft hard enough.
- Custom AI: Subsystems delegated to vanilla AI no longer exhibit fearless behavior on automated ships.

Version 2.0.0

- Compatibility update with Starsector 0.98a-RC5.
- Autofire AI: Fixed a bug where weapons would attack a target other than the ship’s designated attack target, even when the intended target was already within firing range. 

Version 1.12.8

- Custom AI: Fixed a bug where ships were not attempting to intercept and block the escape route of retreating enemies.
- Custom AI: Improved collision avoidance logic: Ships will now yield to allied vessels that are falling back to vent flux behind the line of battle.

Version 1.12.7

- Fixed a NullPointerException related to empty weapon groups in CustomShipAI and ExtendedShipAI. Reported by multiple users.

Version 1.12.6

- It is now possible to disable staggered fire mode for selected weapon types via the /data/weapons/*.ait configuration file. Suggested by Ioulaum. 
- Ships are less likely to select unusual attack angles, especially when equipped with front shields. This fixes the issue of Gryphons flying backwards. Reported by 颯爽的夏亞.
- Autofire AI: Fixed an edge case where weapons would not fire on some shielded modular ships, e.g. 幽灵部队. Reported by 无烟.

Version 1.12.5

- Aim Assist now correctly handles attack and movement when the player ship in fast-time advance.
- Fixed a bug where low rate of fire weapons, like the Hellbore, had their DPS estimated incorrectly, which led to improper aiming behavior.
- Fixed a null pointer exception caused by incorrect use of Java WeakReference. Reported by prom.

Version 1.12.4

- Fixed a null pointer exception in ship maneuver logic. Reported by TimeDiver0.

Version 1.12.3
 
- Shield Assist now works with front shields.
- Aim Assist now has a Perfect Targeting mode, where it disregards the mouse input and aims precisely at the target center. Perfect Targeting can be enabled in LunaLib settings.
- Fixed a special case in ship facing calculation when using unusual weapon groups, such as both back-facing weapon slots on a Harbinger.
- Pressing right ALT + K speeds up the combat by a factor of 5.

Version 1.12.2

- Custom AI: If the enemy is in full retreat, ships will attempt to intercept and block its escape route. This is the initial implementation of the feature, improvements are on the todo list. Suggested by Reshy.
- Custom AI: Several improvements and regression fixes in ship movement coordination.
- All ammo-based kinetic weapons attack enemy ships only when their shields are raised. Previously the behavior applied only to vanilla Needlers. This feature can be disabled in LunaLib settings. Suggested by vinh.
- Aim Assist can be configured not to target hulks and asteroids. Suggested by Roxorium.
- Potentially fixed an incompatibility with Luddic Enhancement: IED Edition, where "the funny Luddic explosive Tankers loses its suicidal behavior". Reported by TheShear.

Version 1.12.1

- Custom AI: Fixed another case of interrupting bursts by venting flux, this time affecting burst beams.
- Fixed a memory leak which could trigger the Starsector memory leak warning.
- Fixed an issue where certain ships would "wiggle all over the place" when aiming weapons. Reported by Ioulaum.
- Refactored direction calculations to use a dedicated modulo arithmetic class, likely fixing unnoticed bugs.

Version 1.12.0

- Fixed several regressions in weapon aiming, particularly affecting PD beams.
- Improved the algorithm for calculating ship rotation to better aim hardpoints and broadside weapons.
- All ships, regardless of whether they are equipped with the Custom AI hullmod, now aim hardpoints and handle broadside builds correctly. Apexes will kill all your frigates.
- Vanilla AI is no longer affected by the bug where broadside ships revert to front-facing behavior when issued an escort order.

Version 1.11.6

- Fixed a bug where some automated frigates had an aggressive personality instead of the intended fearless personality.
- Added an option to disable automated ship personality override, reverting to vanilla settings. Suggested by shmone-else.
- Made several improvements to how AI changes are applied, reducing the risk of interfering with custom AI or personality overrides from other mods. Suggested by shmone-else.
- Fixed a bug that prevented installing Custom AI hullmod on ships with no weapons. Reported by vinh and Rayden_Solo.
- Custom AI: Fixed an issue where ships would start venting flux and interrupt weapon bursts. Reported by Reshy.

Version 1.11.5

- Custom AI: Several bugfixes and improvements to ship maneuvering logic, especially when attacking stations. Now the ships are less likely to block allies and their own line of fire.
- Custom AI: AI now estimates weapon attack range for broadside builds with additional precision. Back slot will not be left out of action, even on the weirdest broadside ships.
- AI Tweaks-specific weapon tags are now added via a custom /data/weapons/weaponId.ait file. This improves mod interoperability and eliminates the need to override definitions in weapon_data.csv.
- Fixed a bug that prevented High Energy Focus from triggering on ships with a lot of energy PD weapons. Reported by Marq.
- Autofire AI now correctly handles out-of-range targets. It accounts for projectile and target velocity, aiming and initiating attacks in advance, before the target enters maximum firing range. This increases the window of opportunity to deal damage, strongly benefiting projectile PD weapons, among others. 

Version 1.11.4

- Fixed a bug that caused ships to incorrectly estimate range to their targets.
- Fixed a bug where staggered fire logic could prevent weapons from firing on ships equipped with Lidar Arrays, such as the KoL Lunaria.
- Fixed "parentStation must not be null" crash caused by detached ship modules. Reported on Fossic.
- Fixed NoSuchElementException crash caused by ships with negative deployment points. Reported on Fossic.

Version 1.11.3

- Fixed a major bug where staggered fire logic could prevent weapons from firing under specific conditions.
- Custom AI: Ship will not vent if it has an opportunity to finish a heavily damaged target.
- Custom AI: Improved ship movement algorithm. Now the AI will not approach larger targets closer than necessary.
- Custom AI: Ships equipped with Burn Drive now coordinate their movement more effectively and block each other’s paths less often.

Version 1.11.2

- Custom AI: Improvements to burn drive AI. Reduced the number of occasions the ship will overexpose itself when using the burn drive.
- Custom AI: Ships no longer ignore the fog of war during the early stages of a battle, before opposing fleets have made contact.
- Custom AI: Ships with temporal shell will use the system when backing off.
- Eligible enemy ships in the simulator are controlled by AI Tweaks custom AI. This feature can be disabled in LunaLib settings.
- Number of minor fixes and improvements.

Version 1.11.1
 
- Custom AI: Many improvements to vent AI. Now it doesn't like to vent in front of Hammer Barrage.
- Custom AI: Issuing a retreat order to a ship no longer removes its Custom AI, replacing it with vanilla AI.

Version 1.11.0

- Custom Ship AI can be enabled for ships in player fleet via a hullmod.
- Added LunaLib setting to enable/disable needler AI attacking enemy ships only when their shields are raised.
- Added integration with Leading PIP. When AI Tweaks Aim Assist is enabled, the targeting lead indicator centers on the selected target.
- Added an option to remove the square grid on the combat map. This option is available in the LunaLib config under the Debug tab.

Version 1.10.12

- Fixed a bug where AI would not fire no-friendly-fire weapons (e.g., fighter weapons, Paladin) over allies. Reported by ºOnLooker.

Version 1.10.11

- Fixed a bug in the automatic omni shield AI that caused interference with manual weapon aiming. Reported by Android9k.

Version 1.10.10

- The Weapon AI has been improved to reduce the number of friendly fire incidents, though they still do happen.
- Autofire weapons no longer attack missiles with NONE collision class, like prv Starworks Frasare bombs. Reported by swwu.

Version 1.10.9

- Aim-assisted turret weapons will not attack when wildly off target.
- Aim assist no longer interferes with movement systems. Reported by Rubenlee123.
- Invictus and other ships controller by custom ship AI aggressively follow the eliminate order. Issue reported by mora.

Version 1.10.8

- Aim Assist correctly handles "auto-turn to cursor" inverted mode.
- Improved the UI for the player's automatic omni shield. It is now impossible to force it into a broken state through incorrect input combinations.
- The automatic omni shield now draws an indication circle around the player's ship at all times when the AI is active. When the automatic omni shield is enabled but manually deactivated by pressing the right mouse button, the indication circle becomes fainter.
- The automatic omni shield UI is now aware of vanilla keymap.

Version 1.10.7

- Aim Assist correctly handles alternating weapon groups.
- Ship under Aim Assist no longer decelerates when issued no commands.

Version 1.10.6

- Aim assist now rotates the player ship to correctly aim the selected weapon group when using "auto-turn to cursor" mode. This works with all non-guided weapons, even ones not facing the front. And yes, aim assist can make Venture Mk. II fly backwards.
- Aim assist ship rotation can be disabled in LunaLib settings.
- Aim assist will target fighters. Suggested by TANK6110.
- Performance improvements.

Version 1.10.5

- When a beam weapon switches targets, it may rotate the existing beam to the new target if it's faster than stopping and re-firing once aligned.
- AI Tweaks hullmods have a manufacturer attribute set and can be filtered from Common hullmods. Suggested by MegaPenguin.
- Weapons with firing cycle longer than 8 seconds are not eligible for staggered firing mode. This solves the issue with AI Tweaks not firing the Iron Shell Twin-Linked Railgun reported by StrikeEcho
- Improved High Energy Focus ship system. Now it's more likely to activate when weapons are firing at a shielded target. Suggested by MegaPenguin.
- Underlying math improvements for even more precision.

Version 1.10.4

- Frigate hardpoint aiming now works correctly with Advanced Gunnery Control.
- Frigates will rotate to properly aim all front-facing projectile and beam weapons, not just hardpoints.
- Frigate hardpoint aiming can be disabled with "Enable Custom Ship AI" LunaLib setting.
- Rift Beam has a specialized, trigger-happy AI that will fire even when the weapon is switching targets. Suggested by dry_dock. 

Version 1.10.3

- AI-controlled frigates no longer briefly freeze after destroying their current target; now they immediately proceed to attack a new target.
- Target leading calculations account for weapon barrel offset, ship rotation and combat engine order of operation, leading to slightly improved accuracy.
- More accurate arctan(x) approximation and engine controller AI improvements, again leading to slightly improved accuracy, especially for hardpoints.

Version 1.10.2

- Fixed a bug where AI Tweaks did not register LunaLib settings changes until Starsector was restarted.
- Fixed a crash that happened when personality of an automated ship without a captain was overridden. Reported by Kalos

Version 1.10.1

- Frigates will now rotate to properly aim hardpoint weapons. Currently, works only for projectile weapons, not missiles.
- Pest Cutter from Torchships has a specialized, very trigger-happy AI for operating the Catalyzed Chemical Torch.
- Fixed another case of "Fatal: Illegal character in path at index" crash during game startup.

Version 1.10.0

- Vanilla ship AI no longer takes "manual" control of weapon groups, except groups with missiles. This ensures AI Tweaks controls almost all weapons.
- Staggered firing mode is now enabled for ships with vanilla AI.
- Custom ship AI no longer autofires missiles; instead, it now uses vanilla logic to manage them.

Version 1.9.6

- Staggered firing mode bugfixes.
- Staggered firing mode can be disabled in LunaLib settings.

Version 1.9.5

- Ships with Custom AI now fire projectile weapons in staggered firing mode. In this mode, all weapons of the same type fire at a constant interval. This feature is not yet available for vanilla ships.
- Fixed "Fatal: Illegal character in path at index" crash during game startup. Reported by LuckyBitch and 暮淅

Version 1.9.4

- Fixed regression in autofire accuracy, especially when attacking frigates.
- Various improvements in Custom ship AI.

Version 1.9.3

- Fixed an incompatibility with Advanced Gunnery Control that caused ships controlled by AI Tweaks' custom AI to tilt 90 degrees while attacking. Reported by Kothyxaan and albinobigfoot.

Version 1.9.2

- Automatic omni shield and aim assist on/off settings are now saved and persist between game sessions. Suggested by @Carkidd

Version 1.9.1

- Fixed crash caused by aim assist trying to operate weapons on destroyed player ship. Reported by @Abyssal Phoenix 

Version 1.9.0

- Added aim assist for manually controlled weapons. Aim assist automatically adjusts weapon position to account for target leading. Use "[" key to activate.
- Fast-time ships (phase or temporal shell) speed increase is taken into account when calculating firing solution.
- Finisher Beam Protocol hullmod has a new icon.

Version 1.8.0

- Added System Shunt hullmod. System Shunt prevents AI from using the ship system.

Version 1.7.6

- Fixed crash caused by overriding automated fighter AI personality. Reported by @Hyperkayak

Version 1.7.5

- Autofire weapons with long bursts like Tachyon Lance or Plasma Cannon are even less likely to change targets mid-burst.
- Fixed issue where autofire weapons in some cases were too accurate on ships with low Combat Readiness.
- Fixed targeting priority for PD weapons with no ANTI_FTR hint. Now they prioritize missiles over fighters.
- Fixed issue with PD weapons targeting friendly VIC Hungruf missile engine module. Reported by @alexgu812
- Invictus and other ships using lidararray are controlled by the custom ship AI all the time, not only when lidar array is active.

Version 1.7.4

- Updated LunaLib settings description. Reported by @soundso.

Version 1.7.3

- Custom ship AI avoids ship collisions.
- Custom ship AI avoids blocking line of fire.
- Removed a lot of class loader jank used in earlier version of custom ship AI.
- Custom ship AI is enabled for Guardians in bar bounty. 
- Added improved build for Guardians in bar bounty.
- Removed option to draw weapon debug lines.
- Added AI Tweaks devmode setting to LunaLib. Devmode shouldn't be used outside development, as it reduces stability.
- Added debug option to highlight ships controlled by AI Tweaks custom AI. 
- Added the possibility to replace the entire logic jar file without reloading the game. This will speed up development, but should have no impact on gameplay. Enabled only in devmode.
- Added custom variant for low tech star fortress: station3_Bulwark. The variant is not currently used.

Version 1.7.2

- Fixed compatibility with Starsector 0.96. All features should work correctly. mod_info.json gameVersion needs to be downgraded manually. 

Version 1.7.1

- Custom ship AI no longer requires -Xverify:none argument.

Version 1.7.0

- Added custom ship AI. Currently, enabled only for Guardian in cryosleeper encounter.
- Custom ship AI requires adding -Xverify:none argument to Starsector vmparams file. Alternatively, the AI can be disabled in LunaLib settings.
- Guardian in cryosleeper encounter has a better build.
- Various minor tweaks to weapon AI.

Version 1.6.3

- Fixed null pointer exception in autofire logic. Reported by @Ganegrei.

Version 1.6.2

- Fixed null pointer exception in Fleet Cohesion AI caused by incorrect handling of modular ships. Reported by Celepito.

Version 1.6.1

- Fixed bug that caused Fleet Cohesion AI to take over control of player ship. Reported by @WilliamDraco and CV514.

Version 1.6.0

- Implemented Fleet Cohesion AI. The AI will attempt to maintain fleet cohesion by auto-assigning move commands to cruisers and capitals that stray off from the rest of the fleet. Configurable via LunaLib settings.

Version 1.5.3

- Fixed another null pointer exception in Lidar Array AI.
- Previous fix to Proximity Mine was insufficient. Now Proximity Mine is on autofire blacklist and is controlled by vanilla AI. 

Version 1.5.2

- Fixed issue with Wasps being hesitant to fire Proximity Mines.
- PD weapons with ANTI_FTR AI tag will now prioritize fighters over missiles.
- Minor improvements to Lidar Array AI.
- Improved the logic for recognizing indestructible station modules.

Version 1.5.1

- Fixed null pointer exception in Lidar Array AI. 
- Improved Lidar Array AI; now the entire ship rotates to properly lead target with hardpoint weapons.

Version 1.5.0

- Added custom Lidar Array AI. The AI should make Invictus much more focused on killing enemies instead of wobbling around.

Version 1.4.7

- Fixed regression in hardpoint weapon aiming.
- Fixed unhandled case of cleanup when AI Tweaks is removed from the game: station modules with Finisher Beam Protocol hullmod.
- Added debug option to draw lines between autofire weapons and their targets. Configurable via LunaLib settings.

Version 1.4.6

- Fixed bug that prevented weapons from shooting over armor modules from the Knights of Ludd mod. 

Version 1.4.5

- AI Tweaks can be removed from the game without breaking saves.
- Changed the way weapon behavior is configured, to not interfere with game localisation. 

Version 1.4.4

- Fixed null pointer exception in autofire AI logic.

Version 1.4.3

- Changed the default keybind for automatic omni shield toggle to "]" to avoid conflicts with vanilla functionality.
- Added setting to enable/disable title screen fire.
- Added alpha version of custom Lidar Array AI. The AI should make Invictus much more focused on killing enemies instead of wobbling around. Can be enabled by uncommenting lines in AITweaks/data/config/shipsystems/lidararray.system. 

Version 1.4.2

- Automated ships personality setting does not try to take over the control when a player is piloting the ship manually.

Version 1.4.1

- Improved the personality picker for automated ships in player's fleet. Now it works reliably in all cases.
- Personality setting for automated ships now applies also to automated ships without AI cores assigned. In vanilla, they are always reckless. 

Version 1.4.0

- Added the ability to change personality of AI core captains in player fleet. The personality can be changed via LunaLib settings.
- Default personality of AI cores in player fleet is now aggressive instead of reckless (aka fearless). 
- Fixed a bug that caused weapons to try to shoot through indestructible parts of space stations.

Version 1.3.0

- Integrated AITweaks with LunaLib. Now LunaLib is a required dependency.
- Automatic omni shield toggle keybind is now configurable via LunaLib settings.
- Ammo based PD weapons like Burst Laser conserve ammo when attacking non PD targets, similar as vanilla.
- Ammo based non-PD weapons like Antimatter Blaster conserve ammo when attacking PD targets.
- Fixed bug where automatic omni shield indicator was displayed for ships with front shields.

Version 1.2.0

- Added automatic omni shields for player controlled ship (left CTRL to deploy).
- Fixed a bug where autofire weapons attacked R-selected friendly ships.

Version 1.1.3

- Fixed weapon AI behavior on ships with Escort assignment; it's still slightly worse than default AITweaks behavior, due to limitations of vanilla API.
- Non-PD weapons now attack fighters, but only when there are no bigger ships in range.
- Modified Storm Needler to attack only shields. Now the enemy ships can forget about having shields!

Version 1.1.2

- Improved target leading for weapons with chargeup time (delay between pressing trigger and actual attack), like Railgun or Gauss Cannon

Version 1.1.1

- PD weapons now prioritize fighters over ships.
- Added a little easter egg feature.
- Updated Phase Lance definition to reflect vanilla balance changes (thanks Vesperrr for reminding me!)

Version 1.0.0

- Updated mod to work with Starsector 0.97a-RC6.
- Fixed bug that caused accuracy penalty to be applied incorrectly.
- Accuracy penalty is now taken into account in all calculations, not just target leading.

Version 0.4.6

- Projectile fade range is taken into account when attacking shieldless targets.
- Improved accuracy of first volley after weapon rotates to a new target.
- Fixed bug that prevented weapons on certain modular ships from shooting.
- Performance improvements.

Version 0.4.3

- High explosive weapons are less likely to miss exposed hull; especially noticeable for slow projectiles like Hellbore.
- Fixed -hopefully- null pointer exception reported by albinobigfoot.

Version 0.4.2

- Completely reimplemented autofire AI, not relying on vanilla AI in any regard.

Version 0.3.0

- Added specialized target leading algorithm for hardpoints.

Version 0.2.0

- Added improved target leading algorithm for autofire weapons.