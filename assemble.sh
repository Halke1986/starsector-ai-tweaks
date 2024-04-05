asm_root="./src_asm/com.genir.aitweaks.asm.combat.ai"

find $asm_root -type f -exec sed -i 's^com/fs/starfarer/combat/ai/BasicShipAI^com/genir/aitweaks/asm/combat/ai/AssemblyShipAI^g' {} +
find $asm_root -type f -exec sed -i 's^com/fs/starfarer/combat/ai/I^com/genir/aitweaks/asm/combat/ai/OrderResponseModule^g' {} +

python ../../../Krakatau/assemble.py -out ./jars/AITweaks_asm.jar -r $asm_root