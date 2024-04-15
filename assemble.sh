asm_root="./src_asm/com.genir.aitweaks.asm.combat.ai"

vanilla_path="com/fs/starfarer/combat/ai"
asm_path="com/genir/aitweaks/asm/combat/ai"
aitweaks_path="com/genir/aitweaks/features/maneuver"

find $asm_root -type f -exec sed -i "s^$vanilla_path/BasicShipAI^$asm_path/AssemblyShipAI^g" {} + # BasicShipAI
find $asm_root -type f -exec sed -i "s^$vanilla_path/I^$asm_path/OrderResponseModule^g" {} + # OrderResponseModule

# OverrideEngineAI
find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/BasicEngineAI^$aitweaks_path/OverrideEngineAI^g" {} +

# StrafeTargetManeuverV2
#find $asm_root -type f -exec sed -i "s^new $vanilla_path/movement/maneuvers/StrafeTargetManeuverV2^new $aitweaks_path/ManeuverV^g" {} +
#find $asm_root -type f -exec sed -i "s^Utf8 $vanilla_path/movement/maneuvers/StrafeTargetManeuverV2^Utf8 $aitweaks_path/ManeuverV^g" {} +
#find $asm_root -type f -exec sed -i "s^invokespecial Method $vanilla_path/movement/maneuvers/StrafeTargetManeuverV2^invokespecial Method $aitweaks_path/ManeuverV^g" {} +

# Strafe B
find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/StrafeTargetManeuverV2/B^$aitweaks_path/ManeuverV^g" {} +
find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/B^$aitweaks_path/ManeuverB^g" {} +


python ../../../Krakatau/assemble.py -out ./jars/AITweaks_asm.jar -r $asm_root



