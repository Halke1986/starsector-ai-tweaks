## COPY SOURCE

asm_root="./src_asm"

vanilla_path="com/fs/starfarer/combat/ai"
asm_path="com/genir/aitweaks/asm/combat/ai"
aitweaks_path="com/genir/aitweaks/features/maneuver"

rm -rf   $asm_root

mkdir -p $asm_root/$asm_path

cp ./disassembly/$vanilla_path/'BasicShipAI.j'   $asm_root/$asm_path/'AssemblyShipAI.j'
cp ./disassembly/$vanilla_path/'BasicShipAI$1.j' $asm_root/$asm_path/'AssemblyShipAI$1.j'
cp ./disassembly/$vanilla_path/'BasicShipAI$o.j' $asm_root/$asm_path/'AssemblyShipAI$o.j'

cp ./disassembly/$vanilla_path/'I.j'   $asm_root/$asm_path/'OrderResponseModule.j'
cp ./disassembly/$vanilla_path/'I$o.j' $asm_root/$asm_path/'OrderResponseModule$o.j'

## REPLACE CALLS

# Update AI paths
find $asm_root -type f -exec sed -i "s^$vanilla_path/BasicShipAI^$asm_path/AssemblyShipAI^g" {} +
find $asm_root -type f -exec sed -i "s^$vanilla_path/I^$asm_path/OrderResponseModule^g" {} +

# Override Engine Controller
find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/BasicEngineAI^$aitweaks_path/OverrideEngineAI^g" {} +

# Override Maneuvers
find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/StrafeTargetManeuverV2^$aitweaks_path/V^g" {} +
find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/B^$aitweaks_path/B^g" {} +
find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/U^$aitweaks_path/U^g" {} +

## ASSEMBLE

python ../../../Krakatau/assemble.py -out ./jars/AITweaks_asm.jar -r $asm_root
