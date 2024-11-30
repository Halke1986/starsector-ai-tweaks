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