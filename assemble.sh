asm_root="./src_asm/com.genir.aitweaks.asm.combat.ai"

# BasicShipAI
find $asm_root -type f -exec sed -i 's^com/fs/starfarer/combat/ai/BasicShipAI^com/genir/aitweaks/asm/combat/ai/AssemblyShipAI^g' {} +

# OrderResponseModule
find $asm_root -type f -exec sed -i 's^com/fs/starfarer/combat/ai/I^com/genir/aitweaks/asm/combat/ai/OrderResponseModule^g' {} +

# StrafeTargetManeuverV2
find $asm_root -type f -exec sed -i 's^new com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2^new com/genir/aitweaks/features/maneuver/ManeuverObf^g' {} +
find $asm_root -type f -exec sed -i 's^Utf8 com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2^Utf8 com/genir/aitweaks/features/maneuver/ManeuverObf^g' {} +
find $asm_root -type f -exec sed -i 's^invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2^invokespecial Method com/genir/aitweaks/features/maneuver/ManeuverObf^g' {} +

python ../../../Krakatau/assemble.py -out ./jars/AITweaks_asm.jar -r $asm_root
