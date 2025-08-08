rm -rf                "$SS_PATH/mods/AITweaks"
mkdir                 "$SS_PATH/mods/AITweaks"

cp -r ./data          "$SS_PATH/mods/AITweaks"
cp -r ./graphics      "$SS_PATH/mods/AITweaks"
cp -r ./jars          "$SS_PATH/mods/AITweaks"
cp ./mod_info.json    "$SS_PATH/mods/AITweaks/mod_info.json"
cp ./aitweaks.version "$SS_PATH/mods/AITweaks/aitweaks.version"
cp ./LICENSE          "$SS_PATH/mods/AITweaks/LICENSE"