v1.2.0:
- Added automatic omni shields for player controlled ship (left CTRL to deploy).
- Fixed a bug where autofire weapons attacked R-selected friendly ships.

v1.1.3:
- Fixed weapon AI behavior on ships with Escort assignment; it's still slightly worse than default AITweaks behavior, due to limitations of vanilla API.
- Non-PD weapons now attack fighters, but only when there are no bigger ships in range.
- Modified Storm Needler to attack only shields. Now the enemy ships can forget about having shields!

v1.1.2:
- Improved target leading for weapons with chargeup time (delay between pressing trigger and actual attack), like Railgun or Gauss Cannon

v1.1.1:
- PD weapons now prioritize fighters over ships.
- Added a little easter egg feature.
- Updated Phase Lance definition to reflect vanilla balance changes (thanks Vesperrr for reminding me!)

v1.0.0:
- Updated mod to work with Starsector 0.97a-RC6.
- Fixed bug that caused accuracy penalty to be applied incorrectly.
- Accuracy penalty is now taken into account in all calculations, not just target leading.

v0.4.6:
- Projectile fade range is taken into account when attacking shieldless targets.
- Improved accuracy of first volley after weapon rotates to a new target.
- Fixed bug that prevented weapons on certain modular ships from shooting.
- Performance improvements.

v0.4.3:
- High explosive weapons are less likely to miss exposed hull; especially noticeable for slow projectiles like Hellbore.
- Fixed -hopefully- null pointer exception reported by albinobigfoot.

v0.4.2:
- Completely reimplemented autofire AI, not relying on vanilla AI in any regard.

v0.3.0:
- Added specialized target leading algorithm for hardpoints.

v0.2.0:
- Added improved target leading algorithm for autofire weapons.