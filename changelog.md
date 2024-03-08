- AI Tweaks can now be removed from the game, without breaking saves. 

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