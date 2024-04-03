find ./src/com/genir/aitweaks/asm -type f -exec sed -i 's^com/fs/starfarer/combat/ai/BasicShipAI^com/genir/aitweaks/asm/AssemblyShipAI^g' {} +
find ./src/com/genir/aitweaks/asm -type f -exec sed -i 's^com/fs/starfarer/combat/ai/I^com/genir/aitweaks/asm/OrderResponseModule^g' {} +

python ../../../Krakatau/assemble.py -out ./jars/AITweaks_asm.jar -r ./src/com/genir/aitweaks/asm