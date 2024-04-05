.version 49 0 
.class public super com/genir/aitweaks/asm/combat/ai/AssemblyShipAI 
.super java/lang/Object 
.implements com/fs/starfarer/combat/ai/AI 
.implements com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o 
.implements com/fs/starfarer/combat/ai/Orderable 
.implements com/fs/starfarer/api/combat/ShipAIPlugin 
.field public static ALLOW_OLD_STRAFE Z 
.field public static MIN_ATTACK_MODE_TIME F 
.field public static ATTACK_MODE_TIME_RANGE F 
.field public engine Lcom/fs/starfarer/combat/CombatEngine; 
.field public ship Lcom/fs/starfarer/combat/entities/Ship; 
.field public engineAI Lcom/fs/starfarer/combat/ai/movement/EngineAI; 
.field public flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
.field public attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
.field public threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
.field public shieldAI Lcom/fs/starfarer/combat/ai/G; 
.field public attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
.field public timeInMode F 
.field public maxTimeInMode F 
.field public sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
.field public map Lcom/fs/starfarer/combat/A/new; 
.field public orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
.field public aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
.field public ventModule Lcom/fs/starfarer/combat/ai/oOOO; 
.field public fighterPullbackModule Lcom/fs/starfarer/combat/ai/E; 
.field public behaviorModule Lcom/fs/starfarer/combat/ai/oO0O; 
.field public systemAI Lcom/fs/starfarer/combat/ai/system/M; 
.field public collisionCheckTracker Lcom/fs/starfarer/util/IntervalTracker; 
.field public eval Lcom/fs/starfarer/combat/ai/C; 
.field public config Lcom/fs/starfarer/api/combat/ShipAIConfig; 
.field public prevCollisionDir Lorg/lwjgl/util/vector/Vector2f; 
.field public lastThreatResponse Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
.field public nearbyFriendlyShipCount I 
.field public target Lcom/fs/starfarer/combat/entities/Ship; 
.field public maxAttackRange F 
.field public avoidingCollision Z 
.field public forceTarget Z 
.field public badTarget Z 
.field protected targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
.field public static synthetic $SWITCH_TABLE$com$fs$starfarer$api$combat$ShipAPI$HullSize [I 

.method static <clinit> : ()V 
    .code stack 1 locals 0 
L0:     iconst_0 
L1:     putstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ALLOW_OLD_STRAFE Z 
L4:     ldc +10.0f 
L6:     putstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI MIN_ATTACK_MODE_TIME F 
L9:     ldc +10.0f 
L11:    putstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ATTACK_MODE_TIME_RANGE F 
L14:    return 
L15:    
    .end code 
.end method 

.method public forceCircumstanceEvaluation : ()V 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI collisionCheckTracker Lcom/fs/starfarer/util/IntervalTracker; 
L4:     invokevirtual Method com/fs/starfarer/util/IntervalTracker forceIntervalElapsed ()V 
L7:     aload_0 
L8:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L11:    invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'ö00000' ()Lcom/fs/starfarer/util/IntervalTracker; 
L14:    invokevirtual Method com/fs/starfarer/util/IntervalTracker forceIntervalElapsed ()V 
L17:    return 
L18:    
    .end code 
.end method 

.method public getConfig : ()Lcom/fs/starfarer/api/combat/ShipAIConfig; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI config Lcom/fs/starfarer/api/combat/ShipAIConfig; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public <init> : (Lcom/fs/starfarer/combat/entities/Ship;)V 
    .code stack 4 locals 2 
L0:     aload_0 
L1:     aload_1 
L2:     new com/fs/starfarer/api/combat/ShipAIConfig 
L5:     dup 
L6:     invokespecial Method com/fs/starfarer/api/combat/ShipAIConfig <init> ()V 
L9:     invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipAIConfig;)V 
L12:    return 
L13:    
    .end code 
.end method 

.method public <init> : (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipAIConfig;)V 
    .code stack 8 locals 4 
L0:     aload_0 
L1:     invokespecial Method java/lang/Object <init> ()V 
L4:     aload_0 
L5:     aconst_null 
L6:     putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L9:     aload_0 
L10:    ldc +10.0f 
L12:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI timeInMode F 
L15:    aload_0 
L16:    fconst_0 
L17:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxTimeInMode F 
L20:    aload_0 
L21:    new com/fs/starfarer/api/combat/ShipwideAIFlags 
L24:    dup 
L25:    invokespecial Method com/fs/starfarer/api/combat/ShipwideAIFlags <init> ()V 
L28:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L31:    aload_0 
L32:    new com/fs/starfarer/util/IntervalTracker 
L35:    dup 
L36:    ldc +0.05009999871253967f 
L38:    ldc +0.08349999785423279f 
L40:    invokespecial Method com/fs/starfarer/util/IntervalTracker <init> (FF)V 
L43:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI collisionCheckTracker Lcom/fs/starfarer/util/IntervalTracker; 
L46:    aload_0 
L47:    new org/lwjgl/util/vector/Vector2f 
L50:    dup 
L51:    invokespecial Method org/lwjgl/util/vector/Vector2f <init> ()V 
L54:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI prevCollisionDir Lorg/lwjgl/util/vector/Vector2f; 
L57:    aload_0 
L58:    aconst_null 
L59:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI lastThreatResponse Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L62:    aload_0 
L63:    iconst_0 
L64:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI nearbyFriendlyShipCount I 
L67:    aload_0 
L68:    aconst_null 
L69:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L72:    aload_0 
L73:    ldc +3.4028234663852886e+38f 
L75:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L78:    aload_0 
L79:    iconst_0 
L80:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L83:    aload_0 
L84:    iconst_0 
L85:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L88:    aload_0 
L89:    aconst_null 
L90:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L93:    aload_0 
L94:    aload_2 
L95:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI config Lcom/fs/starfarer/api/combat/ShipAIConfig; 
L98:    aload_0 
L99:    new com/fs/starfarer/combat/ai/movement/maneuvers/oooO 
L102:   dup 
L103:   aload_1 
L104:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO <init> (Lcom/fs/starfarer/combat/entities/Ship;)V 
L107:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L110:   aload_0 
L111:   new com/fs/starfarer/combat/ai/C 
L114:   dup 
L115:   aload_1 
L116:   aload_0 
L117:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L120:   invokespecial Method com/fs/starfarer/combat/ai/C <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipwideAIFlags;)V 
L123:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI eval Lcom/fs/starfarer/combat/ai/C; 
L126:   aload_0 
L127:   aload_1 
L128:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L131:   aload_0 
L132:   invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L135:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L138:   aload_0 
L139:   new com/fs/starfarer/combat/ai/movement/BasicEngineAI 
L142:   dup 
L143:   aload_1 
L144:   invokespecial Method com/fs/starfarer/combat/ai/movement/BasicEngineAI <init> (Lcom/fs/starfarer/combat/entities/Ship;)V 
L147:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engineAI Lcom/fs/starfarer/combat/ai/movement/EngineAI; 
L150:   aload_0 
L151:   new com/fs/starfarer/combat/ai/movement/oOOO 
L154:   dup 
L155:   aload_1 
L156:   aload_0 
L157:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engineAI Lcom/fs/starfarer/combat/ai/movement/EngineAI; 
L160:   aload_0 
L161:   invokespecial Method com/fs/starfarer/combat/ai/movement/oOOO <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/EngineAI;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L164:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L167:   aload_0 
L168:   new com/fs/starfarer/combat/ai/attack/AttackAIModule 
L171:   dup 
L172:   aload_1 
L173:   aload_0 
L174:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L177:   invokespecial Method com/fs/starfarer/combat/ai/attack/AttackAIModule <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipwideAIFlags;)V 
L180:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L183:   aload_0 
L184:   new com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 
L187:   dup 
L188:   aload_1 
L189:   aload_0 
L190:   aload_0 
L191:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L194:   invokespecial Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Lcom/fs/starfarer/combat/ai/movement/oOOO;)V 
L197:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L200:   aload_1 
L201:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getSpec ()Lcom/fs/starfarer/loading/specs/HullVariantSpec; 
L204:   invokevirtual Method com/fs/starfarer/loading/specs/HullVariantSpec getHullSpec ()Lcom/fs/starfarer/loading/specs/privatesuper; 
L207:   invokevirtual Method com/fs/starfarer/loading/specs/privatesuper getShieldSpec ()Lcom/fs/starfarer/loading/specs/OOOo; 
L210:   invokevirtual Method com/fs/starfarer/loading/specs/OOOo getType ()Lcom/fs/starfarer/api/combat/ShieldAPI$ShieldType; 
L213:   getstatic Field com/fs/starfarer/api/combat/ShieldAPI$ShieldType PHASE Lcom/fs/starfarer/api/combat/ShieldAPI$ShieldType; 
L216:   if_acmpne L282 
L219:   aload_1 
L220:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getPhaseCloak ()Lcom/fs/starfarer/combat/systems/OOoO; 
L223:   invokevirtual Method com/fs/starfarer/combat/systems/OOoO getSpec ()Lcom/fs/starfarer/loading/specs/M; 
L226:   aload_1 
L227:   aload_0 
L228:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L231:   aload_0 
L232:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L235:   aload_0 
L236:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L239:   aload_0 
L240:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L243:   aload_0 
L244:   invokevirtual Method com/fs/starfarer/loading/specs/M createSystemAI [u227] 
L247:   astore_3 
L248:   aload_3 
L249:   instanceof com/fs/starfarer/combat/ai/system/V 
L252:   ifeq L266 
L255:   aload_3 
L256:   checkcast com/fs/starfarer/combat/ai/system/V 
L259:   invokevirtual Method com/fs/starfarer/combat/ai/system/V 'ôo0000' ()Lcom/fs/starfarer/combat/ai/null; 
L262:   iconst_1 
L263:   invokevirtual Method com/fs/starfarer/combat/ai/null o00000 (Z)V 
L266:   aload_0 
L267:   new com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 
L270:   dup 
L271:   aload_0 
L272:   aload_3 
L273:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 <init> (Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI;Lcom/fs/starfarer/combat/ai/system/M;)V 
L276:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L279:   goto L367 
L282:   aload_1 
L283:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getShield ()Lcom/fs/starfarer/combat/systems/H; 
L286:   ifnull L367 
L289:   aload_1 
L290:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getShield ()Lcom/fs/starfarer/combat/systems/H; 
L293:   invokevirtual Method com/fs/starfarer/combat/systems/H getType ()Lcom/fs/starfarer/api/combat/ShieldAPI$ShieldType; 
L296:   getstatic Field com/fs/starfarer/api/combat/ShieldAPI$ShieldType OMNI Lcom/fs/starfarer/api/combat/ShieldAPI$ShieldType; 
L299:   if_acmpne L338 
L302:   aload_0 
L303:   new com/fs/starfarer/combat/ai/O0oO 
L306:   dup 
L307:   aload_1 
L308:   aload_0 
L309:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L312:   invokespecial Method com/fs/starfarer/combat/ai/O0oO <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipwideAIFlags;)V 
L315:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L318:   aload_0 
L319:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L322:   checkcast com/fs/starfarer/combat/ai/O0oO 
L325:   aload_0 
L326:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L329:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Ó00000' ()Lcom/fs/starfarer/combat/ai/collisions/CollisionAnalysisModule; 
L332:   invokevirtual Method com/fs/starfarer/combat/ai/O0oO o00000 (Lcom/fs/starfarer/combat/ai/collisions/CollisionAnalysisModule;)V 
L335:   goto L367 
L338:   aload_1 
L339:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getShield ()Lcom/fs/starfarer/combat/systems/H; 
L342:   invokevirtual Method com/fs/starfarer/combat/systems/H getType ()Lcom/fs/starfarer/api/combat/ShieldAPI$ShieldType; 
L345:   getstatic Field com/fs/starfarer/api/combat/ShieldAPI$ShieldType FRONT Lcom/fs/starfarer/api/combat/ShieldAPI$ShieldType; 
L348:   if_acmpne L367 
L351:   aload_0 
L352:   new com/fs/starfarer/combat/ai/Q 
L355:   dup 
L356:   aload_1 
L357:   aload_0 
L358:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L361:   invokespecial Method com/fs/starfarer/combat/ai/Q <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipwideAIFlags;)V 
L364:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L367:   aload_0 
L368:   fconst_0 
L369:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI pickAttackModeIfNeeded (F)V 
L372:   aload_0 
L373:   new com/genir/aitweaks/asm/combat/ai/OrderResponseModule 
L376:   dup 
L377:   aload_1 
L378:   aload_0 
L379:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L382:   aload_0 
L383:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L386:   aload_0 
L387:   invokespecial Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L390:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L393:   aload_0 
L394:   new com/fs/starfarer/combat/ai/oOOO 
L397:   dup 
L398:   aload_1 
L399:   aload_0 
L400:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L403:   aload_0 
L404:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L407:   aload_0 
L408:   aload_0 
L409:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L412:   invokespecial Method com/fs/starfarer/combat/ai/oOOO <init> [u41] 
L415:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ventModule Lcom/fs/starfarer/combat/ai/oOOO; 
L418:   aload_1 
L419:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L422:   ifne L450 
L425:   aload_0 
L426:   new com/fs/starfarer/combat/ai/E 
L429:   dup 
L430:   aload_1 
L431:   aload_0 
L432:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L435:   aload_0 
L436:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L439:   aload_0 
L440:   aload_0 
L441:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L444:   invokespecial Method com/fs/starfarer/combat/ai/E <init> [u41] 
L447:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI fighterPullbackModule Lcom/fs/starfarer/combat/ai/E; 
L450:   aload_0 
L451:   new com/fs/starfarer/combat/ai/oO0O 
L454:   dup 
L455:   aload_1 
L456:   aload_0 
L457:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L460:   aload_0 
L461:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L464:   aload_0 
L465:   aload_0 
L466:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L469:   invokespecial Method com/fs/starfarer/combat/ai/oO0O <init> [u41] 
L472:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI behaviorModule Lcom/fs/starfarer/combat/ai/oO0O; 
L475:   aload_1 
L476:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getSystem ()Lcom/fs/starfarer/combat/systems/OOoO; 
L479:   ifnull L514 
L482:   aload_0 
L483:   aload_1 
L484:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getSystem ()Lcom/fs/starfarer/combat/systems/OOoO; 
L487:   invokevirtual Method com/fs/starfarer/combat/systems/OOoO getSpec ()Lcom/fs/starfarer/loading/specs/M; 
L490:   aload_1 
L491:   aload_0 
L492:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L495:   aload_0 
L496:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L499:   aload_0 
L500:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L503:   aload_0 
L504:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L507:   aload_0 
L508:   invokevirtual Method com/fs/starfarer/loading/specs/M createSystemAI [u227] 
L511:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI systemAI Lcom/fs/starfarer/combat/ai/system/M; 
L514:   return 
L515:   
    .end code 
.end method 

.method public getAIFlags : ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public getEngineAI : ()Lcom/fs/starfarer/combat/ai/movement/EngineAI; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engineAI Lcom/fs/starfarer/combat/ai/movement/EngineAI; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public getMaxFiringRange : ()F 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L4:     invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule do ()F 
L7:     freturn 
L8:     
    .end code 
.end method 

.method public getMaxNonMissileRange : ()F 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L4:     invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'øO0000' ()F 
L7:     freturn 
L8:     
    .end code 
.end method 

.method public getCurrentManeuver : ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L4:     invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L7:     areturn 
L8:     
    .end code 
.end method 

.method private pickAttackModeIfNeeded : (F)V 
    .code stack 4 locals 4 
L0:     aload_0 
L1:     dup 
L2:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI timeInMode F 
L5:     fload_1 
L6:     fadd 
L7:     putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI timeInMode F 
L10:    aload_0 
L11:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI timeInMode F 
L14:    aload_0 
L15:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxTimeInMode F 
L18:    fcmpl 
L19:    iflt L293 
L22:    aload_0 
L23:    fconst_0 
L24:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI timeInMode F 
L27:    aload_0 
L28:    invokestatic Method java/lang/Math random ()D 
L31:    d2f 
L32:    getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ATTACK_MODE_TIME_RANGE F 
L35:    fmul 
L36:    getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI MIN_ATTACK_MODE_TIME F 
L39:    fadd 
L40:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxTimeInMode F 
L43:    aload_0 
L44:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L47:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getMaxSpeed ()F 
L50:    ldc_w +100.0f 
L53:    invokestatic Method java/lang/Math random ()D 
L56:    d2f 
L57:    ldc_w +50.0f 
L60:    fmul 
L61:    fadd 
L62:    fcmpl 
L63:    ifle L70 
L66:    iconst_1 
L67:    goto L71 
L70:    iconst_0 
L71:    istore_2 
L72:    iload_2 
L73:    ifeq L95 
L76:    invokestatic Method java/lang/Math random ()D 
L79:    d2f 
L80:    ldc_w +0.25f 
L83:    fcmpl 
L84:    ifle L95 
L87:    aload_0 
L88:    getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L91:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L94:    return 
L95:    aload_0 
L96:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L99:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L102:   astore_3 
L103:   invokestatic Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI $SWITCH_TABLE$com$fs$starfarer$api$combat$ShipAPI$HullSize ()[I 
L106:   aload_3 
L107:   invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L110:   iaload 
L111:   tableswitch 2 
            L144 
            L154 
            L192 
            L244 
            L244 
            default : L293 

L144:   aload_0 
L145:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L148:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L151:   goto L293 
L154:   invokestatic Method java/lang/Math random ()D 
L157:   d2f 
L158:   ldc_w +0.5f 
L161:   fcmpl 
L162:   ifle L175 
L165:   aload_0 
L166:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L169:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L172:   goto L182 
L175:   aload_0 
L176:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o 'Ó00000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L179:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L182:   aload_0 
L183:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L186:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L189:   goto L293 
L192:   invokestatic Method java/lang/Math random ()D 
L195:   d2f 
L196:   ldc_w +0.6000000238418579f 
L199:   fcmpl 
L200:   ifle L213 
L203:   aload_0 
L204:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L207:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L210:   goto L293 
L213:   invokestatic Method java/lang/Math random ()D 
L216:   d2f 
L217:   ldc_w +0.5f 
L220:   fcmpl 
L221:   ifle L234 
L224:   aload_0 
L225:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o 'Ò00000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L228:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L231:   goto L293 
L234:   aload_0 
L235:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L238:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L241:   goto L293 
L244:   invokestatic Method java/lang/Math random ()D 
L247:   d2f 
L248:   ldc_w +0.800000011920929f 
L251:   fcmpl 
L252:   ifle L265 
L255:   aload_0 
L256:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L259:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L262:   goto L293 
L265:   invokestatic Method java/lang/Math random ()D 
L268:   d2f 
L269:   ldc_w +0.5f 
L272:   fcmpl 
L273:   ifle L286 
L276:   aload_0 
L277:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o 'Ò00000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L280:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L283:   goto L293 
L286:   aload_0 
L287:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L290:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L293:   aload_0 
L294:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L297:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o 'Ò00000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L300:   if_acmpeq L313 
L303:   aload_0 
L304:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L307:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o 'Ó00000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L310:   if_acmpne L320 
L313:   aload_0 
L314:   getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L317:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L320:   return 
L321:   
    .end code 
.end method 

.method public hasOrders : ()Z 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L4:     invokevirtual Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule 'Ö00000' ()Z 
L7:     ireturn 
L8:     
    .end code 
.end method 

.method public getOrders : ()Lcom/fs/starfarer/combat/tasks/C; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L4:     invokevirtual Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule 'Ó00000' ()Lcom/fs/starfarer/combat/tasks/C; 
L7:     areturn 
L8:     
    .end code 
.end method 

.method public advance : (F)V 
    .code stack 7 locals 13 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getAI ()Lcom/fs/starfarer/combat/ai/AI; 
L7:     aload_0 
L8:     if_acmpeq L82 
L11:    aload_0 
L12:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L15:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getAI ()Lcom/fs/starfarer/combat/ai/AI; 
L18:    instanceof com/fs/starfarer/combat/entities/Ship$ShipAIWrapper 
L21:    ifeq L41 
L24:    aload_0 
L25:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L28:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getAI ()Lcom/fs/starfarer/combat/ai/AI; 
L31:    checkcast com/fs/starfarer/combat/entities/Ship$ShipAIWrapper 
L34:    invokevirtual Method com/fs/starfarer/combat/entities/Ship$ShipAIWrapper getAI ()Lcom/fs/starfarer/api/combat/ShipAIPlugin; 
L37:    aload_0 
L38:    if_acmpeq L82 
L41:    new java/lang/RuntimeException 
L44:    dup 
L45:    ldc_w 'Ship [%s] - [%s] is being controlled by an AI that is assigned to something else' 
L48:    iconst_2 
L49:    anewarray java/lang/Object 
L52:    dup 
L53:    iconst_0 
L54:    aload_0 
L55:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L58:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getName ()Ljava/lang/String; 
L61:    aastore 
L62:    dup 
L63:    iconst_1 
L64:    aload_0 
L65:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L68:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getSpec ()Lcom/fs/starfarer/loading/specs/HullVariantSpec; 
L71:    invokevirtual Method com/fs/starfarer/loading/specs/HullVariantSpec getFullDesignationForShip ()Ljava/lang/String; 
L74:    aastore 
L75:    invokestatic Method java/lang/String format (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String; 
L78:    invokespecial Method java/lang/RuntimeException <init> (Ljava/lang/String;)V 
L81:    athrow 
L82:    aload_0 
L83:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L86:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L89:    ifeq L93 
L92:    return 
L93:    ldc_w 'Ship AI' 
L96:    invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L99:    aload_0 
L100:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L103:   invokevirtual Method com/fs/starfarer/combat/entities/Ship controlsLocked ()Z 
L106:   istore_2 
L107:   iload_2 
L108:   ifeq L187 
L111:   aload_0 
L112:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L115:   getstatic Field com/fs/starfarer/combat/entities/Ship$oo class Lcom/fs/starfarer/combat/entities/Ship$oo; 
L118:   invokevirtual Method com/fs/starfarer/combat/entities/Ship blockCommandForOneFrame (Lcom/fs/starfarer/combat/entities/Ship$oo;)V 
L121:   aload_0 
L122:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L125:   getstatic Field com/fs/starfarer/combat/entities/Ship$oo 'ØO0000' Lcom/fs/starfarer/combat/entities/Ship$oo; 
L128:   invokevirtual Method com/fs/starfarer/combat/entities/Ship blockCommandForOneFrame (Lcom/fs/starfarer/combat/entities/Ship$oo;)V 
L131:   aload_0 
L132:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L135:   getstatic Field com/fs/starfarer/combat/entities/Ship$oo 'Ø00000' Lcom/fs/starfarer/combat/entities/Ship$oo; 
L138:   invokevirtual Method com/fs/starfarer/combat/entities/Ship blockCommandForOneFrame (Lcom/fs/starfarer/combat/entities/Ship$oo;)V 
L141:   aload_0 
L142:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L145:   getstatic Field com/fs/starfarer/combat/entities/Ship$oo oO0000 Lcom/fs/starfarer/combat/entities/Ship$oo; 
L148:   invokevirtual Method com/fs/starfarer/combat/entities/Ship blockCommandForOneFrame (Lcom/fs/starfarer/combat/entities/Ship$oo;)V 
L151:   aload_0 
L152:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L155:   getstatic Field com/fs/starfarer/combat/entities/Ship$oo 'õ00000' Lcom/fs/starfarer/combat/entities/Ship$oo; 
L158:   invokevirtual Method com/fs/starfarer/combat/entities/Ship blockCommandForOneFrame (Lcom/fs/starfarer/combat/entities/Ship$oo;)V 
L161:   aload_0 
L162:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L165:   iconst_1 
L166:   invokevirtual Method com/fs/starfarer/combat/entities/Ship setHoldFireOneFrame (Z)V 
L169:   aload_0 
L170:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L173:   new com/fs/starfarer/combat/entities/Ship$Oo 
L176:   dup 
L177:   getstatic Field com/fs/starfarer/combat/entities/Ship$oo 'ÖO0000' Lcom/fs/starfarer/combat/entities/Ship$oo; 
L180:   aconst_null 
L181:   invokespecial Method com/fs/starfarer/combat/entities/Ship$Oo <init> (Lcom/fs/starfarer/combat/entities/Ship$oo;Ljava/lang/Object;)V 
L184:   invokevirtual Method com/fs/starfarer/combat/entities/Ship giveCommand (Lcom/fs/starfarer/combat/entities/Ship$Oo;)V 
        .catch [0] from L187 to L1328 using L1328 
L187:   aload_0 
L188:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L191:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getShipTarget ()Lcom/fs/starfarer/combat/entities/Ship; 
L194:   ifnull L231 
L197:   aload_0 
L198:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L201:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getShipTarget ()Lcom/fs/starfarer/combat/entities/Ship; 
L204:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L207:   ifne L223 
L210:   aload_0 
L211:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L214:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getShipTarget ()Lcom/fs/starfarer/combat/entities/Ship; 
L217:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isExpired ()Z 
L220:   ifeq L231 
L223:   aload_0 
L224:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L227:   aconst_null 
L228:   invokevirtual Method com/fs/starfarer/combat/entities/Ship setShipTarget (Lcom/fs/starfarer/api/combat/ShipAPI;)V 
L231:   aload_0 
L232:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L235:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L238:   ifnull L351 
L241:   aload_0 
L242:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L245:   ifnull L351 
L248:   aload_0 
L249:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L252:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L255:   getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MANEUVER_TARGET Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L258:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags getCustom (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Ljava/lang/Object; 
L261:   astore_3 
L262:   aload_3 
L263:   instanceof com/fs/starfarer/api/combat/ShipAPI 
L266:   ifeq L351 
L269:   aload_3 
L270:   checkcast com/fs/starfarer/api/combat/ShipAPI 
L273:   astore 4 
L275:   aload 4 
L277:   invokeinterface InterfaceMethod com/fs/starfarer/api/combat/ShipAPI isHulk ()Z 1 
L282:   ifne L305 
L285:   aload 4 
L287:   invokeinterface InterfaceMethod com/fs/starfarer/api/combat/ShipAPI isExpired ()Z 1 
L292:   ifne L305 
L295:   aload 4 
L297:   invokeinterface InterfaceMethod com/fs/starfarer/api/combat/ShipAPI isPiece ()Z 1 
L302:   ifeq L351 
L305:   aload_0 
L306:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L309:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L312:   instanceof com/fs/starfarer/combat/ai/movement/maneuvers/B 
L315:   ifne L344 
L318:   aload_0 
L319:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L322:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L325:   instanceof com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2 
L328:   ifne L344 
L331:   aload_0 
L332:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L335:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L338:   instanceof com/fs/starfarer/combat/ai/movement/maneuvers/return 
L341:   ifeq L351 
L344:   aload_0 
L345:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L348:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'Õ00000' ()V 
L351:   aload_0 
L352:   aload_0 
L353:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L356:   invokevirtual Method com/fs/starfarer/combat/CombatEngine getCombatMap ()Lcom/fs/starfarer/combat/A/new; 
L359:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI map Lcom/fs/starfarer/combat/A/new; 
L362:   aload_0 
L363:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L366:   fload_1 
L367:   invokevirtual Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule o00000 (F)V 
L370:   aload_0 
L371:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L374:   fload_1 
L375:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags advance (F)V 
L378:   aload_0 
L379:   fload_1 
L380:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI pickAttackModeIfNeeded (F)V 
L383:   aload_0 
L384:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L387:   ifnull L407 
L390:   aload_0 
L391:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L394:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L397:   invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO isOverloadedOrVenting ()Z 
L400:   ifeq L407 
L403:   iconst_1 
L404:   goto L408 
L407:   iconst_0 
L408:   istore_3 
L409:   aload_0 
L410:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L413:   fload_1 
L414:   invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO Object (F)Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$o; 
L417:   astore 4 
L419:   aload_0 
L420:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L423:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getFixedLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L426:   ifnonnull L439 
L429:   aload_0 
L430:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L433:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getStationSlot ()Lcom/fs/starfarer/loading/specs/Y; 
L436:   ifnull L442 
L439:   aconst_null 
L440:   astore 4 
L442:   aload_0 
L443:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
L446:   invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ò00000' ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo; 
L449:   astore 5 
L451:   aload 5 
L453:   ifnull L479 
L456:   aload 5 
L458:   invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo 'Ó00000' ()Lcom/fs/starfarer/combat/entities/Ship; 
L461:   ifnull L479 
L464:   aload_0 
L465:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L468:   getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags BIGGEST_THREAT Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L471:   ldc_w +0.10000000149011612f 
L474:   aload 5 
L476:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;FLjava/lang/Object;)V 
L479:   aload_0 
L480:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L483:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isRetreating ()Z 
L486:   ifne L596 
L489:   aload 4 
L491:   ifnull L596 
L494:   aload_0 
L495:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L498:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L501:   astore 6 
L503:   aload 4 
L505:   getfield Field com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$o 'super' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L508:   ifnonnull L559 
L511:   aload 6 
L513:   instanceof com/fs/starfarer/combat/ai/movement/maneuvers/Y 
L516:   ifeq L596 
L519:   iconst_1 
L520:   istore 7 
L522:   aload 6 
L524:   checkcast com/fs/starfarer/combat/ai/movement/maneuvers/Y 
L527:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/Y 'public.new' ()F 
L530:   fstore 8 
L532:   fload 8 
L534:   ldc_w +90.0f 
L537:   fcmpl 
L538:   ifle L544 
L541:   iconst_0 
L542:   istore 7 
L544:   iload 7 
L546:   ifeq L596 
L549:   aload_0 
L550:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
L553:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'Õ00000' ()V 
L556:   goto L596 
L559:   aload 6 
L561:   ifnull L573 
L564:   aload 6 
L566:   aload_0 
L567:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI lastThreatResponse Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L570:   if_acmpeq L596 
L573:   aload_0 
L574:   aload 4 
L576:   getfield Field com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$o 'super' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L579:   aload 4 
L581:   getfield Field com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$o 'Ò00000' F 
L584:   invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L587:   aload_0 
L588:   aload 4 
L590:   getfield Field com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$o 'super' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L593:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI lastThreatResponse Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L596:   aload_0 
L597:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L600:   fload_1 
L601:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/oooO 'super' (F)V 
L604:   aload_0 
L605:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI collisionCheckTracker Lcom/fs/starfarer/util/IntervalTracker; 
L608:   fload_1 
L609:   invokevirtual Method com/fs/starfarer/util/IntervalTracker advance (F)V 
L612:   fconst_0 
L613:   fstore 6 
L615:   aload_0 
L616:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI collisionCheckTracker Lcom/fs/starfarer/util/IntervalTracker; 
L619:   invokevirtual Method com/fs/starfarer/util/IntervalTracker intervalElapsed ()Z 
L622:   istore 7 
L624:   iload 7 
L626:   ifne L636 
L629:   aload_0 
L630:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L633:   ifeq L705 
L636:   aload_0 
L637:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L640:   ifeq L649 
L643:   fload_1 
L644:   fstore 6 
L646:   goto L658 
L649:   aload_0 
L650:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI collisionCheckTracker Lcom/fs/starfarer/util/IntervalTracker; 
L653:   invokevirtual Method com/fs/starfarer/util/IntervalTracker getIntervalDuration ()F 
L656:   fstore 6 
L658:   ldc_w 'Checking for potential collisions to avoid' 
L661:   invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L664:   aload_0 
L665:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI checkForPotentialMineTriggers ()V 
L668:   aload_0 
L669:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidShipCollisions ()V 
L672:   aload_0 
L673:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidMissiles ()V 
L676:   aload_0 
L677:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L680:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Õ00000' ()Lorg/lwjgl/util/vector/Vector2f; 
L683:   ifnull L690 
L686:   iconst_1 
L687:   goto L691 
L690:   iconst_0 
L691:   istore 8 
L693:   iload 8 
L695:   ifne L702 
L698:   aload_0 
L699:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidAsteroidCollisions ()V 
L702:   invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L705:   aload_0 
L706:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ventModule Lcom/fs/starfarer/combat/ai/oOOO; 
L709:   fload_1 
L710:   aload_0 
L711:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L714:   invokevirtual Method com/fs/starfarer/combat/ai/oOOO o00000 (FLcom/fs/starfarer/combat/entities/Ship;)V 
L717:   aload_0 
L718:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI fighterPullbackModule Lcom/fs/starfarer/combat/ai/E; 
L721:   ifnull L736 
L724:   aload_0 
L725:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI fighterPullbackModule Lcom/fs/starfarer/combat/ai/E; 
L728:   fload_1 
L729:   aload_0 
L730:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L733:   invokevirtual Method com/fs/starfarer/combat/ai/E o00000 (FLcom/fs/starfarer/combat/entities/Ship;)V 
L736:   aload_0 
L737:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI behaviorModule Lcom/fs/starfarer/combat/ai/oO0O; 
L740:   fload_1 
L741:   invokevirtual Method com/fs/starfarer/combat/ai/oO0O 'super' (F)V 
L744:   aload_0 
L745:   aload_0 
L746:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L749:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO String ()Z 
L752:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L755:   aload_0 
L756:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L759:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getFixedLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L762:   ifnonnull L775 
L765:   aload_0 
L766:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L769:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getStationSlot ()Lcom/fs/starfarer/loading/specs/Y; 
L772:   ifnull L780 
L775:   aload_0 
L776:   iconst_0 
L777:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L780:   aload_0 
L781:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L784:   ifne L797 
L787:   aload_0 
L788:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L791:   invokevirtual Method [c152] void ()Z 
L794:   ifne L886 
L797:   aload_0 
L798:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L801:   ifeq L815 
L804:   aload_0 
L805:   aload_0 
L806:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L809:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Õ00000' ()Lorg/lwjgl/util/vector/Vector2f; 
L812:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI prevCollisionDir Lorg/lwjgl/util/vector/Vector2f; 
L815:   aload_0 
L816:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engineAI Lcom/fs/starfarer/combat/ai/movement/EngineAI; 
L819:   aload_0 
L820:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L823:   invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/EngineAI setAvoidingCollision (Z)V 2 
L828:   aload_0 
L829:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L832:   aload_0 
L833:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L836:   invokevirtual Method [c152] 'Ó00000' ()F 
L839:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Ò00000' (F)V 
L842:   aload_0 
L843:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L846:   aload_0 
L847:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L850:   invokevirtual Method [c152] 'ô00000' ()F 
L853:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'super' (F)V 
L856:   ldc_w 'Directing engine AI (incl. collision avoidance computation)' 
L859:   invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L862:   aload_0 
L863:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L866:   fload_1 
L867:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Ö00000' (F)V 
L870:   invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L873:   aload_0 
L874:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engineAI Lcom/fs/starfarer/combat/ai/movement/EngineAI; 
L877:   fload_1 
L878:   invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/EngineAI advance (F)V 2 
L883:   goto L1018 
L886:   ldc_w 'Maneuver direct control' 
L889:   invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L892:   aload_0 
L893:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L896:   invokevirtual Method [c152] 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L899:   ifnonnull L908 
L902:   ldc_w 'null maneuver' 
L905:   goto L921 
L908:   aload_0 
L909:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L912:   invokevirtual Method [c152] 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L915:   invokevirtual Method java/lang/Object getClass ()Ljava/lang/Class; 
L918:   invokevirtual Method java/lang/Class getSimpleName ()Ljava/lang/String; 
L921:   invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L924:   aload_0 
L925:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L928:   invokevirtual Method [c152] 'super' ()V 
L931:   invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L934:   aload_0 
L935:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L938:   invokevirtual Method [c152] 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L941:   instanceof com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2 
L944:   ifeq L987 
L947:   aload_0 
L948:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L951:   invokevirtual Method [c152] 'Ö00000' ()Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L954:   checkcast com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2 
L957:   astore 8 
L959:   aload_0 
L960:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L963:   aload 8 
L965:   iconst_1 
L966:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2 Object (Z)F 
L969:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Ò00000' (F)V 
L972:   aload_0 
L973:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L976:   aload 8 
L978:   invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2 'Ò00000' ()F 
L981:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'super' (F)V 
L984:   goto L1015 
L987:   aload_0 
L988:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L991:   aload_0 
L992:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L995:   invokevirtual Method [c152] 'Ó00000' ()F 
L998:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Ò00000' (F)V 
L1001:  aload_0 
L1002:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1005:  aload_0 
L1006:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L1009:  invokevirtual Method [c152] 'ô00000' ()F 
L1012:  invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'super' (F)V 
L1015:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1018:  fload 6 
L1020:  fconst_0 
L1021:  fcmpl 
L1022:  ifle L1034 
L1025:  aload_0 
L1026:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1029:  fload 6 
L1031:  invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO String (F)V 
L1034:  aload_0 
L1035:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L1038:  ifne L1046 
L1041:  aload_0 
L1042:  aconst_null 
L1043:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI prevCollisionDir Lorg/lwjgl/util/vector/Vector2f; 
L1046:  aload_0 
L1047:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1050:  invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Õ00000' ()Lorg/lwjgl/util/vector/Vector2f; 
L1053:  astore 8 
L1055:  aload_0 
L1056:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L1059:  fload_1 
L1060:  aload_0 
L1061:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L1064:  aload 8 
L1066:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'super' [u518] 
L1069:  aload_0 
L1070:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1073:  astore 9 
L1075:  aload 9 
L1077:  ifnonnull L1089 
L1080:  aload_0 
L1081:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1084:  invokestatic Method com/fs/starfarer/combat/ai/N 'ÖO0000' (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
L1087:  astore 9 
L1089:  aload 9 
L1091:  ifnonnull L1127 
L1094:  aload_0 
L1095:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1098:  invokestatic Method com/fs/starfarer/combat/ai/N Oo0000 (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
L1101:  astore 10 
L1103:  aload 10 
L1105:  ifnull L1127 
L1108:  aload 10 
L1110:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getShipTarget ()Lcom/fs/starfarer/combat/entities/Ship; 
L1113:  astore 9 
L1115:  aload 9 
L1117:  ifnonnull L1127 
L1120:  aload 10 
L1122:  invokestatic Method com/fs/starfarer/combat/ai/N 'ÖO0000' (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
L1125:  astore 9 
L1127:  aload_0 
L1128:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L1131:  ifnull L1159 
L1134:  aload_0 
L1135:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L1138:  fload_1 
L1139:  aload_0 
L1140:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L1143:  aload 8 
L1145:  aload_0 
L1146:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1149:  invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO void ()Lorg/lwjgl/util/vector/Vector2f; 
L1152:  aload 9 
L1154:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/G o00000 [u527] 6 
L1159:  aload_0 
L1160:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI eval Lcom/fs/starfarer/combat/ai/C; 
L1163:  fload_1 
L1164:  aload_0 
L1165:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L1168:  aload 8 
L1170:  aload_0 
L1171:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1174:  invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO void ()Lorg/lwjgl/util/vector/Vector2f; 
L1177:  iconst_1 
L1178:  invokevirtual Method com/fs/starfarer/combat/ai/C 'super' [u528] 
L1181:  aload_0 
L1182:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI systemAI Lcom/fs/starfarer/combat/ai/system/M; 
L1185:  ifnull L1311 
L1188:  ldc_w 'Ship system AI' 
L1191:  invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L1194:  aload_0 
L1195:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1198:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getSystem ()Lcom/fs/starfarer/combat/systems/OOoO; 
L1201:  invokevirtual Method com/fs/starfarer/combat/systems/OOoO getDisplayName ()Ljava/lang/String; 
L1204:  invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L1207:  aload_0 
L1208:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L1211:  aload_0 
L1212:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1215:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L1218:  invokevirtual Method com/fs/starfarer/combat/CombatEngine getFogOfWar (I)Lcom/fs/starfarer/combat/B/OoOo; 
L1221:  astore 10 
L1223:  aload 9 
L1225:  ifnull L1247 
L1228:  aload_0 
L1229:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1232:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L1235:  aload 9 
L1237:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (ILcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)Z 
L1240:  ifne L1247 
L1243:  iconst_1 
L1244:  goto L1248 
L1247:  iconst_0 
L1248:  istore 11 
L1250:  aload_0 
L1251:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1254:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L1257:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags TARGET_FOR_SHIP_SYSTEM Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L1260:  fconst_1 
L1261:  iload 11 
L1263:  ifeq L1270 
L1266:  aconst_null 
L1267:  goto L1272 
L1270:  aload 9 
L1272:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;FLjava/lang/Object;)V 
L1275:  aload_0 
L1276:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI systemAI Lcom/fs/starfarer/combat/ai/system/M; 
L1279:  fload_1 
L1280:  aload 8 
L1282:  aload_0 
L1283:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1286:  invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO void ()Lorg/lwjgl/util/vector/Vector2f; 
L1289:  iload 11 
L1291:  ifeq L1298 
L1294:  aconst_null 
L1295:  goto L1300 
L1298:  aload 9 
L1300:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/system/M 'super' (FLorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/entities/Ship;)V 5 
L1305:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1308:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1311:  aload_0 
L1312:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L1315:  invokevirtual Method [c152] String ()Z 
L1318:  ifeq L1336 
L1321:  aload_0 
L1322:  invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI pickManeuver ()V 
L1325:  goto L1336 
L1328:  astore 12 
L1330:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1333:  aload 12 
L1335:  athrow 
L1336:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1339:  return 
L1340:  
    .end code 
.end method 

.method private checkForPotentialMineTriggers : ()V 
    .code stack 4 locals 7 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L7:     ldc_w +350.0f 
L10:    fadd 
L11:    fstore_1 
L12:    fload_1 
L13:    fconst_2 
L14:    fmul 
L15:    fstore_2 
L16:    invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L19:    invokevirtual Method com/fs/starfarer/combat/CombatEngine getShipGrid ()Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G; 
L22:    aload_0 
L23:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L26:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L29:    fload_2 
L30:    fload_2 
L31:    invokevirtual Method com/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G getCheckIterator (Lorg/lwjgl/util/vector/Vector2f;FF)Ljava/util/Iterator; 
L34:    astore_3 
L35:    iconst_0 
L36:    istore 4 
L38:    goto L117 
L41:    aload_3 
L42:    invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L47:    checkcast com/fs/starfarer/combat/entities/Ship 
L50:    astore 5 
L52:    aload 5 
L54:    aload_0 
L55:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L58:    if_acmpne L64 
L61:    goto L117 
L64:    aload 5 
L66:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L69:    ifeq L87 
L72:    aload 5 
L74:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L77:    aload_0 
L78:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L81:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L84:    if_icmpeq L117 
L87:    aload_0 
L88:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L91:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L94:    aload 5 
L96:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L99:    invokestatic Method com/fs/starfarer/api/util/Misc getDistance (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L102:   fstore 6 
L104:   fload 6 
L106:   fload_1 
L107:   fcmpg 
L108:   ifge L117 
L111:   iconst_1 
L112:   istore 4 
L114:   goto L126 
L117:   aload_3 
L118:   invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L123:   ifne L41 
L126:   iload 4 
L128:   ifne L203 
L131:   invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L134:   invokevirtual Method com/fs/starfarer/combat/CombatEngine getAsteroidGrid ()Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G; 
L137:   aload_0 
L138:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L141:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L144:   fload_2 
L145:   fload_2 
L146:   invokevirtual Method com/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G getCheckIterator (Lorg/lwjgl/util/vector/Vector2f;FF)Ljava/util/Iterator; 
L149:   astore_3 
L150:   goto L194 
L153:   aload_3 
L154:   invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L159:   checkcast com/fs/starfarer/combat/entities/terrain/Asteroid 
L162:   astore 5 
L164:   aload_0 
L165:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L168:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L171:   aload 5 
L173:   invokevirtual Method com/fs/starfarer/combat/entities/terrain/Asteroid getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L176:   invokestatic Method com/fs/starfarer/api/util/Misc getDistance (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L179:   fstore 6 
L181:   fload 6 
L183:   fload_1 
L184:   fcmpg 
L185:   ifge L194 
L188:   iconst_1 
L189:   istore 4 
L191:   goto L203 
L194:   aload_3 
L195:   invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L200:   ifne L153 
L203:   iload 4 
L205:   ifeq L227 
L208:   aload_0 
L209:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L212:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L215:   getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags HAS_POTENTIAL_MINE_TRIGGER_NEARBY Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L218:   ldc_w +0.5f 
L221:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;F)V 
L224:   goto L240 
L227:   aload_0 
L228:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L231:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L234:   getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags HAS_POTENTIAL_MINE_TRIGGER_NEARBY Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L237:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags unsetFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)V 
L240:   return 
L241:   
    .end code 
.end method 

.method public isAvoidingCollision : ()Z 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI avoidingCollision Z 
L4:     ireturn 
L5:     
    .end code 
.end method 

.method private avoidShipCollisions : ()V 
    .code stack 4 locals 15 
L0:     aload_0 
L1:     iconst_0 
L2:     putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI nearbyFriendlyShipCount I 
L5:     aload_0 
L6:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L9:     invokestatic Method com/fs/starfarer/combat/ai/N 'String.super' (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
L12:    astore_1 
L13:    aload_1 
L14:    ifnull L21 
L17:    iconst_1 
L18:    goto L22 
L21:    iconst_0 
L22:    istore_2 
L23:    iconst_0 
L24:    istore_3 
L25:    fconst_0 
L26:    fstore 4 
L28:    iload_2 
L29:    ifeq L48 
L32:    aload_0 
L33:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L36:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L39:    aload_1 
L40:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L43:    invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L46:    fstore 4 
L48:    aload_0 
L49:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L52:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L55:    fconst_2 
L56:    fmul 
L57:    ldc_w +2000.0f 
L60:    fadd 
L61:    fstore 5 
L63:    aload_0 
L64:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L67:    invokevirtual Method com/fs/starfarer/combat/CombatEngine getAiGridShips ()Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G; 
L70:    aload_0 
L71:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L74:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L77:    fload 5 
L79:    fload 5 
L81:    invokevirtual Method com/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G getCheckIterator (Lorg/lwjgl/util/vector/Vector2f;FF)Ljava/util/Iterator; 
L84:    astore 6 
L86:    goto L468 
L89:    aload 6 
L91:    invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L96:    astore 7 
L98:    aload 7 
L100:   instanceof com/fs/starfarer/combat/entities/Ship 
L103:   ifne L109 
L106:   goto L468 
L109:   aload 7 
L111:   checkcast com/fs/starfarer/combat/entities/Ship 
L114:   astore 8 
L116:   aload_0 
L117:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L120:   aload 8 
L122:   if_acmpne L128 
L125:   goto L468 
L128:   aload 8 
L130:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L133:   ifeq L139 
L136:   goto L468 
L139:   aload 8 
L141:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isShuttlePod ()Z 
L144:   ifeq L150 
L147:   goto L468 
L150:   aload_0 
L151:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L154:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L157:   aload 8 
L159:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L162:   if_icmpne L177 
L165:   aload 8 
L167:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isEngineDisabled ()Z 
L170:   ifeq L177 
L173:   iconst_1 
L174:   goto L178 
L177:   iconst_0 
L178:   istore 9 
L180:   aload 8 
L182:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isDrone ()Z 
L185:   ifeq L203 
L188:   aload 8 
L190:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionClass ()Lcom/fs/starfarer/api/combat/CollisionClass; 
L193:   getstatic Field com/fs/starfarer/api/combat/CollisionClass SHIP Lcom/fs/starfarer/api/combat/CollisionClass; 
L196:   if_acmpne L203 
L199:   iconst_1 
L200:   goto L204 
L203:   iconst_0 
L204:   istore 10 
L206:   iload 9 
L208:   iload 10 
L210:   ior 
L211:   istore 9 
L213:   aload 8 
L215:   aload_0 
L216:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L219:   invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)Z 
L222:   ifeq L255 
L225:   aload_0 
L226:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L229:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L232:   invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L235:   aload 8 
L237:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L240:   invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L243:   if_icmplt L255 
L246:   aload_0 
L247:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L250:   aload 8 
L252:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Ó00000' (Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)V 
L255:   aload 8 
L257:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L260:   invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L263:   aload_0 
L264:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L267:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L270:   invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L273:   if_icmpge L292 
L276:   iload 9 
L278:   ifne L292 
L281:   aload 8 
L283:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L286:   ifne L292 
L289:   goto L468 
L292:   aload 8 
L294:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L297:   ifeq L354 
L300:   aload_0 
L301:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L304:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L307:   ifeq L354 
L310:   aload 8 
L312:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getMass ()F 
L315:   aload_0 
L316:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L319:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getMass ()F 
L322:   fcmpg 
L323:   ifge L354 
L326:   iload 9 
L328:   ifne L354 
L331:   aload 8 
L333:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L336:   ifne L354 
L339:   aload 8 
L341:   aload_0 
L342:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L345:   invokevirtual Method com/fs/starfarer/combat/CombatEngine getPlayerShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L348:   if_acmpeq L354 
L351:   goto L468 
L354:   iconst_0 
L355:   istore 11 
L357:   aload_0 
L358:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L361:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L364:   aload 8 
L366:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L369:   if_icmpne L376 
L372:   iconst_1 
L373:   goto L377 
L376:   iconst_0 
L377:   istore 12 
L379:   aload_0 
L380:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L383:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L386:   aload 8 
L388:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L391:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L394:   fstore 13 
L396:   aload_0 
L397:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L400:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L403:   aload 8 
L405:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L408:   fadd 
L409:   fstore 14 
L411:   iload 12 
L413:   ifeq L438 
L416:   fload 13 
L418:   fload 14 
L420:   ldc_w +3.0f 
L423:   fmul 
L424:   fcmpg 
L425:   ifge L438 
L428:   aload_0 
L429:   dup 
L430:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI nearbyFriendlyShipCount I 
L433:   iconst_1 
L434:   iadd 
L435:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI nearbyFriendlyShipCount I 
L438:   iload 10 
L440:   ifeq L459 
L443:   aload_0 
L444:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L447:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'Ó00000' ()Lcom/fs/starfarer/combat/ai/collisions/CollisionAnalysisModule; 
L450:   aload 8 
L452:   iconst_1 
L453:   invokevirtual Method com/fs/starfarer/combat/ai/collisions/CollisionAnalysisModule o00000 (Lcom/fs/starfarer/combat/entities/Ship;Z)V 
L456:   goto L468 
L459:   aload_0 
L460:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L463:   aload 8 
L465:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'super' (Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)V 
L468:   aload 6 
L470:   invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L475:   ifne L89 
L478:   return 
L479:   
    .end code 
.end method 

.method private avoidAsteroidCollisions : ()V 
    .code stack 4 locals 6 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L7:     astore_1 
L8:     aload_0 
L9:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L12:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L15:    fconst_2 
L16:    fmul 
L17:    ldc_w +500.0f 
L20:    fadd 
L21:    fstore_2 
L22:    aload_0 
L23:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L26:    invokevirtual Method com/fs/starfarer/combat/CombatEngine getAiGridAsteroids ()Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G; 
L29:    aload_1 
L30:    fload_2 
L31:    fload_2 
L32:    invokevirtual Method com/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G getCheckIterator (Lorg/lwjgl/util/vector/Vector2f;FF)Ljava/util/Iterator; 
L35:    astore_3 
L36:    goto L108 
L39:    aload_3 
L40:    invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L45:    astore 4 
L47:    aload 4 
L49:    instanceof com/fs/starfarer/combat/entities/terrain/Asteroid 
L52:    ifne L58 
L55:    goto L108 
L58:    aload 4 
L60:    checkcast com/fs/starfarer/combat/entities/terrain/Asteroid 
L63:    astore 5 
L65:    aload 5 
L67:    invokevirtual Method com/fs/starfarer/combat/entities/terrain/Asteroid getMass ()F 
L70:    ldc_w +1.5f 
L73:    fmul 
L74:    aload_0 
L75:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L78:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getMass ()F 
L81:    fcmpl 
L82:    ifge L99 
L85:    aload_0 
L86:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L89:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getHitpoints ()F 
L92:    ldc_w +100.0f 
L95:    fcmpg 
L96:    ifge L108 
L99:    aload_0 
L100:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L103:   aload 5 
L105:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO 'super' (Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)V 
L108:   aload_3 
L109:   invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L114:   ifne L39 
L117:   return 
L118:   
    .end code 
.end method 

.method private avoidMissiles : ()V 
    .code stack 6 locals 8 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L7:     istore_1 
L8:     invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L11:    invokevirtual Method com/fs/starfarer/combat/CombatEngine getAiGridMissiles ()Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G; 
L14:    astore_2 
L15:    aload_0 
L16:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L19:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L22:    fstore_3 
L23:    aload_2 
L24:    aload_0 
L25:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L28:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L31:    ldc_w +5000.0f 
L34:    fload_3 
L35:    fconst_2 
L36:    fmul 
L37:    fadd 
L38:    ldc_w +5000.0f 
L41:    fload_3 
L42:    fconst_2 
L43:    fmul 
L44:    fadd 
L45:    invokevirtual Method com/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/G getCheckIterator (Lorg/lwjgl/util/vector/Vector2f;FF)Ljava/util/Iterator; 
L48:    astore 4 
L50:    goto L196 
L53:    aload 4 
L55:    invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L60:    astore 5 
L62:    aload 5 
L64:    instanceof com/fs/starfarer/combat/entities/Missile 
L67:    ifeq L196 
L70:    aload 5 
L72:    checkcast com/fs/starfarer/combat/entities/Missile 
L75:    astore 6 
L77:    aload 6 
L79:    invokevirtual Method com/fs/starfarer/combat/entities/Missile getOwner ()I 
L82:    iload_1 
L83:    if_icmpne L136 
L86:    aload 6 
L88:    invokevirtual Method com/fs/starfarer/combat/entities/Missile isFizzling ()Z 
L91:    ifne L136 
L94:    aload 6 
L96:    invokevirtual Method com/fs/starfarer/combat/entities/Missile getCollisionClass ()Lcom/fs/starfarer/api/combat/CollisionClass; 
L99:    getstatic Field com/fs/starfarer/api/combat/CollisionClass MISSILE_NO_FF Lcom/fs/starfarer/api/combat/CollisionClass; 
L102:   if_acmpne L136 
L105:   aload 6 
L107:   invokevirtual Method com/fs/starfarer/combat/entities/Missile isMine ()Z 
L110:   ifeq L125 
L113:   aload 6 
L115:   invokevirtual Method com/fs/starfarer/combat/entities/Missile isNoMineFFConcerns ()Z 
L118:   ifne L125 
L121:   iconst_1 
L122:   goto L126 
L125:   iconst_0 
L126:   istore 7 
L128:   iload 7 
L130:   ifne L136 
L133:   goto L196 
L136:   aload 6 
L138:   invokevirtual Method com/fs/starfarer/combat/entities/Missile getSource ()Lcom/fs/starfarer/combat/entities/Ship; 
L141:   aload_0 
L142:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L145:   if_acmpne L187 
L148:   aload 6 
L150:   invokevirtual Method com/fs/starfarer/combat/entities/Missile isFizzling ()Z 
L153:   ifne L187 
L156:   aload 6 
L158:   invokevirtual Method com/fs/starfarer/combat/entities/Missile isMine ()Z 
L161:   ifeq L176 
L164:   aload 6 
L166:   invokevirtual Method com/fs/starfarer/combat/entities/Missile isNoMineFFConcerns ()Z 
L169:   ifne L176 
L172:   iconst_1 
L173:   goto L177 
L176:   iconst_0 
L177:   istore 7 
L179:   iload 7 
L181:   ifne L187 
L184:   goto L196 
L187:   aload_0 
L188:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L191:   aload 6 
L193:   invokevirtual Method com/fs/starfarer/combat/ai/movement/oOOO String (Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)V 
L196:   aload 4 
L198:   invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L203:   ifne L53 
L206:   return 
L207:   
    .end code 
.end method 

.method private avoidMapEdges : ()V 
    .code stack 0 locals 1 
L0:     return 
L1:     
    .end code 
.end method 

.method private isGoodBackAwayTarget : (Lcom/fs/starfarer/combat/entities/Ship;)Z 
    .code stack 3 locals 4 
L0:     aload_1 
L1:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L4:     aload_0 
L5:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L8:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L11:    if_icmpeq L21 
L14:    aload_1 
L15:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L18:    ifeq L23 
L21:    iconst_0 
L22:    ireturn 
L23:    aload_0 
L24:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L27:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L30:    aload_1 
L31:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L34:    invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L37:    fstore_2 
L38:    aload_0 
L39:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L42:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getFacing ()F 
L45:    aload_0 
L46:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L49:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L52:    aload_1 
L53:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L56:    invokestatic Method com/fs/starfarer/prototype/Utils 'Ò00000' (FLorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L59:    fstore_3 
L60:    fload_2 
L61:    ldc_w +800.0f 
L64:    fcmpg 
L65:    ifge L78 
L68:    fload_3 
L69:    ldc_w +30.0f 
L72:    fcmpg 
L73:    ifge L78 
L76:    iconst_1 
L77:    ireturn 
L78:    iconst_0 
L79:    ireturn 
L80:    
    .end code 
.end method 

.method private isValidOrderTarget : (Lcom/fs/starfarer/combat/entities/Ship;)Z 
    .code stack 2 locals 3 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L4:     invokevirtual Method com/fs/starfarer/combat/CombatEngine getObjects ()Lcom/fs/util/container/repo/ObjectRepository; 
L7:     ldc Class com/fs/starfarer/combat/entities/Ship 
L9:     invokevirtual Method com/fs/util/container/repo/ObjectRepository getList (Ljava/lang/Class;)Ljava/util/List; 
L12:    astore_2 
L13:    aload_1 
L14:    ifnull L36 
L17:    aload_1 
L18:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L21:    ifne L36 
L24:    aload_2 
L25:    aload_1 
L26:    invokeinterface InterfaceMethod java/util/List contains (Ljava/lang/Object;)Z 2 
L31:    ifeq L36 
L34:    iconst_1 
L35:    ireturn 
L36:    iconst_0 
L37:    ireturn 
L38:    
    .end code 
.end method 

.method public render : ()V 
    .code stack 0 locals 1 
L0:     return 
L1:     
    .end code 
.end method 

.method public cancelCurrentManeuver : ()V 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L4:     invokevirtual Method [c152] 'Õ00000' ()V 
L7:     aload_0 
L8:     invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI pickManeuver ()V 
L11:    return 
L12:    
    .end code 
.end method 

.method public getFlockingAI : ()Lcom/fs/starfarer/combat/ai/movement/oOOO; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public setManeuver : (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
    .code stack 4 locals 3 
L0:     fload_2 
L1:     ldc_w -0.5f 
L4:     invokestatic Method java/lang/Math random ()D 
L7:     d2f 
L8:     fconst_1 
L9:     fmul 
L10:    fadd 
L11:    fadd 
L12:    fstore_2 
L13:    fload_2 
L14:    ldc_w +0.5f 
L17:    fcmpg 
L18:    ifge L25 
L21:    ldc_w +0.5f 
L24:    fstore_2 
L25:    aload_0 
L26:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L29:    invokevirtual Method [c152] 'Õ00000' ()V 
L32:    aload_0 
L33:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L36:    aload_1 
L37:    fload_2 
L38:    invokevirtual Method [c152] 'super' (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L41:    return 
L42:    
    .end code 
.end method 

.method public isEngaged : ()Z 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L4:     invokevirtual Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule o00000 ()Z 
L7:     ireturn 
L8:     
    .end code 
.end method 

.method private pickManeuver : ()V 
    .code stack 11 locals 84 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI sequence [u722] 
L4:     invokevirtual Method [c152] String ()Z 
L7:     ifne L11 
L10:    return 
L11:    aload_0 
L12:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L15:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getFixedLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L18:    ifnull L45 
L21:    aload_0 
L22:    new com/fs/starfarer/combat/ai/movement/maneuvers/private 
L25:    dup 
L26:    aload_0 
L27:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L30:    aload_0 
L31:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L34:    aload_0 
L35:    invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/private <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L38:    ldc_w +3.0f 
L41:    invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L44:    return 
L45:    aload_0 
L46:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L49:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getStationSlot ()Lcom/fs/starfarer/loading/specs/Y; 
L52:    ifnull L468 
L55:    aload_0 
L56:    new com/fs/starfarer/combat/ai/movement/maneuvers/R 
L59:    dup 
L60:    aload_0 
L61:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L64:    aload_0 
L65:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L68:    aload_0 
L69:    invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/R <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L72:    ldc_w +3.0f 
L75:    invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L78:    aload_0 
L79:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L82:    invokestatic Method com/fs/starfarer/combat/ai/N do (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/ai/N$O0; 
L85:    astore_1 
L86:    aload_1 
L87:    ifnull L109 
L90:    aload_0 
L91:    aload_1 
L92:    invokevirtual Method com/fs/starfarer/combat/ai/N$O0 'Ô00000' ()Lcom/fs/starfarer/combat/entities/Ship; 
L95:    putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L98:    aload_0 
L99:    aload_1 
L100:   invokevirtual Method com/fs/starfarer/combat/ai/N$O0 'Ó00000' ()Z 
L103:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L106:   goto L449 
L109:   aload_0 
L110:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L113:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isCruiser ()Z 
L116:   ifne L129 
L119:   aload_0 
L120:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L123:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L126:   ifeq L183 
L129:   aload_0 
L130:   aload_0 
L131:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L134:   invokestatic Method com/fs/starfarer/combat/ai/N 'ÒO0000' (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
L137:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L140:   aload_0 
L141:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L144:   ifnull L183 
L147:   aload_0 
L148:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L151:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L154:   aload_0 
L155:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L158:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L161:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L164:   fstore_2 
L165:   fload_2 
L166:   aload_0 
L167:   invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI getMaxFiringRange ()F 
L170:   ldc_w +500.0f 
L173:   fadd 
L174:   fcmpl 
L175:   ifle L183 
L178:   aload_0 
L179:   aconst_null 
L180:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L183:   aload_0 
L184:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L187:   ifnonnull L426 
L190:   aload_0 
L191:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L194:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L197:   ifeq L308 
L200:   aload_0 
L201:   aload_0 
L202:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L205:   aload_0 
L206:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L209:   iconst_1 
L210:   invokestatic Method com/fs/starfarer/combat/ai/N 'Ó00000' (Lcom/fs/starfarer/combat/entities/Ship;FZ)Lcom/fs/starfarer/combat/entities/Ship; 
L213:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L216:   aload_0 
L217:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L220:   ifnull L320 
L223:   aload_0 
L224:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L227:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L230:   ifeq L320 
L233:   aload_0 
L234:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L237:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L240:   ifnull L320 
L243:   aload_0 
L244:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L247:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L250:   invokevirtual Method com/fs/starfarer/combat/ai/L getSourceShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L253:   astore_2 
L254:   aload_2 
L255:   ifnull L320 
L258:   aload_0 
L259:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L262:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L265:   aload_2 
L266:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L269:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L272:   fstore_3 
L273:   fload_3 
L274:   aload_2 
L275:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L278:   ldc_w +700.0f 
L281:   fadd 
L282:   fcmpg 
L283:   ifge L320 
L286:   aload_2 
L287:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isPullBackFighters ()Z 
L290:   ifeq L320 
L293:   aload_0 
L294:   aload_0 
L295:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L298:   iconst_1 
L299:   invokestatic Method com/fs/starfarer/combat/ai/N String (Lcom/fs/starfarer/combat/entities/Ship;Z)Lcom/fs/starfarer/combat/entities/Ship; 
L302:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L305:   goto L320 
L308:   aload_0 
L309:   aload_0 
L310:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L313:   iconst_1 
L314:   invokestatic Method com/fs/starfarer/combat/ai/N String (Lcom/fs/starfarer/combat/entities/Ship;Z)Lcom/fs/starfarer/combat/entities/Ship; 
L317:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L320:   aload_0 
L321:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L324:   ifnull L359 
L327:   aload_0 
L328:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L331:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L334:   aload_0 
L335:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L338:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L341:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L344:   fstore_2 
L345:   fload_2 
L346:   aload_0 
L347:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L350:   fcmpl 
L351:   ifle L359 
L354:   aload_0 
L355:   aconst_null 
L356:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L359:   aload_0 
L360:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L363:   ifnonnull L426 
L366:   aload_0 
L367:   aload_0 
L368:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L371:   aload_0 
L372:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L375:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L378:   ldc +3.4028234663852886e+38f 
L380:   iconst_1 
L381:   invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;FZ)Lcom/fs/starfarer/combat/entities/Ship; 
L384:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L387:   aload_0 
L388:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L391:   ifnull L426 
L394:   aload_0 
L395:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L398:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L401:   aload_0 
L402:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L405:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L408:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L411:   fstore_2 
L412:   fload_2 
L413:   aload_0 
L414:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L417:   fcmpl 
L418:   ifle L426 
L421:   aload_0 
L422:   aconst_null 
L423:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L426:   aload_0 
L427:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L430:   ifnonnull L449 
L433:   aload_0 
L434:   aload_0 
L435:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L438:   aload_0 
L439:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L442:   iconst_0 
L443:   invokestatic Method com/fs/starfarer/combat/ai/N 'Ó00000' (Lcom/fs/starfarer/combat/entities/Ship;FZ)Lcom/fs/starfarer/combat/entities/Ship; 
L446:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L449:   aload_0 
L450:   aload_0 
L451:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L454:   invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI isValidOrderTarget (Lcom/fs/starfarer/combat/entities/Ship;)Z 
L457:   ifne L467 
L460:   aload_0 
L461:   aconst_null 
L462:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L465:   aconst_null 
L466:   astore_1 
L467:   return 
L468:   aload_0 
L469:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI map Lcom/fs/starfarer/combat/A/new; 
L472:   ifnonnull L485 
L475:   aload_0 
L476:   invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L479:   invokevirtual Method com/fs/starfarer/combat/CombatEngine getCombatMap ()Lcom/fs/starfarer/combat/A/new; 
L482:   putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI map Lcom/fs/starfarer/combat/A/new; 
L485:   ldc_w 'Picking maneuver' 
L488:   invokestatic Method com/fs/profiler/Profiler 'Ò00000' (Ljava/lang/String;)V 
L491:   iconst_1 
L492:   istore_1 
        .catch [0] from L493 to L947 using L7714 
L493:   iconst_0 
L494:   istore_2 
L495:   aconst_null 
L496:   astore_3 
L497:   aconst_null 
L498:   astore 4 
L500:   invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L503:   aload_0 
L504:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L507:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L510:   aload_0 
L511:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L514:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isAlly ()Z 
L517:   invokevirtual Method com/fs/starfarer/combat/CombatEngine getTaskManager (IZ)Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L520:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getAllTasks ()Ljava/util/List; 
L523:   invokeinterface InterfaceMethod java/util/List iterator ()Ljava/util/Iterator; 1 
L528:   astore 6 
L530:   goto L695 
L533:   aload 6 
L535:   invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L540:   checkcast com/fs/starfarer/combat/tasks/CombatTask 
L543:   astore 5 
L545:   aload 5 
L547:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L550:   getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType AVOID Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L553:   if_acmpne L695 
L556:   aload 5 
L558:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L561:   ifnull L597 
L564:   aload 5 
L566:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L569:   astore 7 
L571:   aload 7 
L573:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L576:   ifnull L597 
L579:   aload_0 
L580:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L583:   aload 7 
L585:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L588:   invokestatic Method com/fs/starfarer/combat/ai/N 'Ó00000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)Z 
L591:   ifne L597 
L594:   goto L695 
L597:   aload 5 
L599:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L602:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L607:   astore 4 
L609:   aload_0 
L610:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L613:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L616:   aload 4 
L618:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L621:   fstore 7 
L623:   fload 7 
L625:   ldc_w +3500.0f 
L628:   fcmpg 
L629:   ifge L695 
L632:   iconst_1 
L633:   istore_2 
L634:   aload 4 
L636:   aload_0 
L637:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L640:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L643:   invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' [u767] 
L646:   fstore 8 
L648:   fload 8 
L650:   invokestatic Method java/lang/Math random ()D 
L653:   d2f 
L654:   ldc_w +90.0f 
L657:   fmul 
L658:   ldc_w +45.0f 
L661:   fsub 
L662:   fadd 
L663:   fstore 8 
L665:   fload 8 
L667:   invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' (F)Lorg/lwjgl/util/vector/Vector2f; 
L670:   astore_3 
L671:   aload_3 
L672:   ldc_w +2000.0f 
L675:   invokevirtual Method org/lwjgl/util/vector/Vector2f scale (F)Lorg/lwjgl/util/vector/Vector; 
L678:   pop 
L679:   aload_3 
L680:   aload_0 
L681:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L684:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L687:   aload_3 
L688:   invokestatic Method org/lwjgl/util/vector/Vector2f add (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L691:   pop 
L692:   goto L705 
L695:   aload 6 
L697:   invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L702:   ifne L533 
L705:   iload_2 
L706:   ifeq L951 
L709:   aload_0 
L710:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L713:   ldc +3.4028234663852886e+38f 
L715:   iconst_1 
L716:   invokestatic Method com/fs/starfarer/combat/ai/N 'Ó00000' (Lcom/fs/starfarer/combat/entities/Ship;FZ)Lcom/fs/starfarer/combat/entities/Ship; 
L719:   astore 5 
L721:   aload 5 
L723:   ifnull L789 
L726:   aload_0 
L727:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L730:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L733:   aload 5 
L735:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L738:   invokestatic Method com/fs/starfarer/prototype/Utils void [u767] 
L741:   fstore 6 
L743:   fload 6 
L745:   ldc_w +2500.0f 
L748:   fcmpl 
L749:   ifle L789 
L752:   aload 4 
L754:   aload 5 
L756:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L759:   invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' [u767] 
L762:   fstore 7 
L764:   fload 7 
L766:   invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' (F)Lorg/lwjgl/util/vector/Vector2f; 
L769:   astore_3 
L770:   aload_3 
L771:   ldc_w +2000.0f 
L774:   invokevirtual Method org/lwjgl/util/vector/Vector2f scale (F)Lorg/lwjgl/util/vector/Vector; 
L777:   pop 
L778:   aload_3 
L779:   aload 5 
L781:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L784:   aload_3 
L785:   invokestatic Method org/lwjgl/util/vector/Vector2f add (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L788:   pop 
L789:   invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L792:   aload_0 
L793:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L796:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L799:   invokevirtual Method com/fs/starfarer/combat/CombatEngine getFleetManager (I)Lcom/fs/starfarer/combat/CombatFleetManager; 
L802:   astore 6 
L804:   aload 6 
L806:   aload_0 
L807:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L810:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isAlly ()Z 
L813:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getTaskManager (Z)Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L816:   astore 7 
L818:   aload 7 
L820:   aload 6 
L822:   aload_0 
L823:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L826:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getDeployedFleetMember (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L829:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getOrdersForFleetMember (Lcom/fs/starfarer/combat/CombatFleetManager$O0;)Lcom/fs/starfarer/combat/tasks/C; 
L832:   astore 8 
L834:   aload 8 
L836:   ifnull L861 
L839:   aload 8 
L841:   invokevirtual Method com/fs/starfarer/combat/tasks/C o00000 ()Lcom/fs/starfarer/combat/tasks/C$o; 
L844:   getstatic Field com/fs/starfarer/combat/tasks/C$o 'Ô00000' Lcom/fs/starfarer/combat/tasks/C$o; 
L847:   if_acmpeq L951 
L850:   aload 8 
L852:   invokevirtual Method com/fs/starfarer/combat/tasks/C o00000 ()Lcom/fs/starfarer/combat/tasks/C$o; 
L855:   getstatic Field com/fs/starfarer/combat/tasks/C$o 'Ó00000' Lcom/fs/starfarer/combat/tasks/C$o; 
L858:   if_acmpeq L951 
L861:   aconst_null 
L862:   astore 9 
L864:   aload 8 
L866:   ifnull L914 
L869:   aload 8 
L871:   invokevirtual Method com/fs/starfarer/combat/tasks/C o00000 ()Lcom/fs/starfarer/combat/tasks/C$o; 
L874:   getstatic Field com/fs/starfarer/combat/tasks/C$o 'Ö00000' Lcom/fs/starfarer/combat/tasks/C$o; 
L877:   if_acmpne L914 
L880:   aload 8 
L882:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L885:   ifnull L914 
L888:   aload 8 
L890:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L893:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L896:   ifnull L914 
L899:   aload 8 
L901:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L904:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L907:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L912:   astore 9 
L914:   aload 9 
L916:   ifnonnull L922 
L919:   aload_3 
L920:   astore 9 
L922:   new com/fs/starfarer/combat/ai/movement/maneuvers/C 
L925:   dup 
L926:   aload_0 
L927:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L930:   aload 9 
L932:   aload_0 
L933:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/C <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L936:   astore 10 
L938:   aload_0 
L939:   aload 10 
L941:   ldc_w +3.0f 
L944:   invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L947:   invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L950:   return 
        .catch [0] from L951 to L1249 using L7714 
L951:   aload_0 
L952:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L955:   invokevirtual Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule 'ö00000' ()Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule$o; 
L958:   astore 5 
L960:   aload_0 
L961:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L964:   invokevirtual Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule new ()Z 
L967:   ifne L1253 
L970:   aload_0 
L971:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L974:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L977:   getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags DO_NOT_AVOID_BORDER Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L980:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L983:   ifne L1253 
L986:   aload_0 
L987:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L990:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L993:   astore 6 
L995:   aload_0 
L996:   getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L999:   invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'øO0000' ()F 
L1002:  fconst_2 
L1003:  fdiv 
L1004:  ldc_w +300.0f 
L1007:  fadd 
L1008:  fstore 7 
L1010:  fload 7 
L1012:  ldc_w +500.0f 
L1015:  fcmpg 
L1016:  ifge L1024 
L1019:  ldc_w +500.0f 
L1022:  fstore 7 
L1024:  ldc_w +5.0f 
L1027:  fstore 8 
L1029:  aload 5 
L1031:  ifnull L1053 
L1034:  aload 5 
L1036:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L1039:  instanceof com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 
L1042:  ifeq L1053 
L1045:  ldc_w +100.0f 
L1048:  fstore 7 
L1050:  fconst_2 
L1051:  fstore 8 
L1053:  aload 6 
L1055:  getfield Field org/lwjgl/util/vector/Vector2f x F 
L1058:  aload_0 
L1059:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI map Lcom/fs/starfarer/combat/A/new; 
L1062:  invokevirtual Method com/fs/starfarer/combat/A/new 'Ö00000' ()F 
L1065:  fload 7 
L1067:  fsub 
L1068:  fcmpl 
L1069:  ifgt L1129 
L1072:  aload 6 
L1074:  getfield Field org/lwjgl/util/vector/Vector2f x F 
L1077:  aload_0 
L1078:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI map Lcom/fs/starfarer/combat/A/new; 
L1081:  invokevirtual Method com/fs/starfarer/combat/A/new 'Ò00000' ()F 
L1084:  fload 7 
L1086:  fadd 
L1087:  fcmpg 
L1088:  iflt L1129 
L1091:  aload 6 
L1093:  getfield Field org/lwjgl/util/vector/Vector2f y F 
L1096:  aload_0 
L1097:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI map Lcom/fs/starfarer/combat/A/new; 
L1100:  invokevirtual Method com/fs/starfarer/combat/A/new void ()F 
L1103:  fload 7 
L1105:  fsub 
L1106:  fcmpl 
L1107:  ifgt L1129 
L1110:  aload 6 
L1112:  getfield Field org/lwjgl/util/vector/Vector2f y F 
L1115:  aload_0 
L1116:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI map Lcom/fs/starfarer/combat/A/new; 
L1119:  invokevirtual Method com/fs/starfarer/combat/A/new 'Õ00000' ()F 
L1122:  fload 7 
L1124:  fadd 
L1125:  fcmpg 
L1126:  ifge L1253 
L1129:  aload_0 
L1130:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1133:  ifnull L1151 
L1136:  aload_0 
L1137:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1140:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isAlive ()Z 
L1143:  ifne L1151 
L1146:  aload_0 
L1147:  aconst_null 
L1148:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1151:  aload_0 
L1152:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1155:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L1158:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags AVOIDING_BORDER Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L1161:  fload 8 
L1163:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;F)V 
L1166:  aload_0 
L1167:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1170:  ifnull L1218 
L1173:  ldc_w +5.0f 
L1176:  fstore 8 
L1178:  new com/genir/aitweaks/features/maneuver/ManeuverObf 
L1181:  dup 
L1182:  aload_0 
L1183:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1186:  aload_0 
L1187:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1190:  iconst_0 
L1191:  fconst_0 
L1192:  ldc_w +1200.0f 
L1195:  fconst_0 
L1196:  aload_0 
L1197:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1200:  aload_0 
L1201:  iconst_0 
L1202:  invokespecial Method com/genir/aitweaks/features/maneuver/ManeuverObf <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L1205:  astore 9 
L1207:  aload_0 
L1208:  aload 9 
L1210:  fload 8 
L1212:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L1215:  goto L1249 
L1218:  new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L1221:  dup 
L1222:  aload_0 
L1223:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1226:  invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L1229:  invokevirtual Method com/fs/starfarer/combat/CombatEngine getCombatMap ()Lcom/fs/starfarer/combat/A/new; 
L1232:  invokevirtual Method com/fs/starfarer/combat/A/new 'ô00000' ()Lorg/lwjgl/util/vector/Vector2f; 
L1235:  aload_0 
L1236:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L1239:  astore 9 
L1241:  aload_0 
L1242:  aload 9 
L1244:  fload 8 
L1246:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L1249:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1252:  return 
        .catch [0] from L1253 to L1681 using L7714 
L1253:  aconst_null 
L1254:  astore 6 
L1256:  aload_0 
L1257:  iconst_0 
L1258:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L1261:  aload_0 
L1262:  iconst_0 
L1263:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L1266:  iconst_0 
L1267:  istore 7 
L1269:  aload 5 
L1271:  ifnull L1711 
L1274:  aload_0 
L1275:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L1278:  ifnull L1308 
L1281:  aload_0 
L1282:  aload_0 
L1283:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L1286:  invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI isValidOrderTarget (Lcom/fs/starfarer/combat/entities/Ship;)Z 
L1289:  ifeq L1308 
L1292:  aload_0 
L1293:  iconst_1 
L1294:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L1297:  aload_0 
L1298:  aload_0 
L1299:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L1302:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1305:  goto L1427 
L1308:  aload 5 
L1310:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L1313:  ifnull L1378 
L1316:  aload 5 
L1318:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L1321:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getChildModules ()Ljava/util/List; 
L1324:  invokeinterface InterfaceMethod java/util/List isEmpty ()Z 1 
L1329:  ifne L1378 
L1332:  aload_0 
L1333:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1336:  getstatic Field com/fs/starfarer/combat/ai/N$oo new Lcom/fs/starfarer/combat/ai/N$oo; 
L1339:  iconst_0 
L1340:  aload 5 
L1342:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L1345:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/N$oo;ZLcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/ai/N$O0; 
L1348:  astore 6 
L1350:  aload 6 
L1352:  ifnull L1372 
L1355:  aload_0 
L1356:  aload 6 
L1358:  invokevirtual Method com/fs/starfarer/combat/ai/N$O0 'Ô00000' ()Lcom/fs/starfarer/combat/entities/Ship; 
L1361:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1364:  aload_0 
L1365:  iconst_1 
L1366:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L1369:  goto L1427 
L1372:  iconst_1 
L1373:  istore 7 
L1375:  goto L1427 
L1378:  aload 5 
L1380:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L1383:  ifnull L1414 
L1386:  aload 5 
L1388:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L1391:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isTargetable ()Z 
L1394:  ifeq L1414 
L1397:  aload_0 
L1398:  aload 5 
L1400:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L1403:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1406:  aload_0 
L1407:  iconst_1 
L1408:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L1411:  goto L1427 
L1414:  aload_0 
L1415:  aconst_null 
L1416:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1419:  aload_0 
L1420:  iconst_0 
L1421:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L1424:  iconst_1 
L1425:  istore 7 
L1427:  aload_0 
L1428:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1431:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getVariant ()Lcom/fs/starfarer/loading/specs/HullVariantSpec; 
L1434:  invokevirtual Method com/fs/starfarer/loading/specs/HullVariantSpec isCarrier ()Z 
L1437:  ifeq L1497 
L1440:  aload_0 
L1441:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1444:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSpec ()Lcom/fs/starfarer/loading/specs/privatesuper; 
L1447:  invokevirtual Method com/fs/starfarer/loading/specs/privatesuper getHints ()Ljava/util/EnumSet; 
L1450:  getstatic Field com/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints NO_AUTO_ESCORT Lcom/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints; 
L1453:  invokevirtual Method java/util/EnumSet contains (Ljava/lang/Object;)Z 
L1456:  ifne L1497 
L1459:  aload_0 
L1460:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1463:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLaunchBays ()Ljava/util/List; 
L1466:  invokeinterface InterfaceMethod java/util/List size ()I 1 
L1471:  i2f 
L1472:  aload_0 
L1473:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1476:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getStats ()Lcom/fs/starfarer/combat/entities/ship/class; 
L1479:  invokevirtual Method com/fs/starfarer/combat/entities/ship/class getNumFighterBays ()Lcom/fs/starfarer/api/combat/MutableStat; 
L1482:  invokevirtual Method com/fs/starfarer/api/combat/MutableStat getModifiedValue ()F 
L1485:  ldc_w +0.6600000262260437f 
L1488:  fmul 
L1489:  fcmpl 
L1490:  iflt L1497 
L1493:  iconst_1 
L1494:  goto L1498 
L1497:  iconst_0 
L1498:  istore 8 
L1500:  iload 8 
L1502:  ifeq L1685 
L1505:  aload_0 
L1506:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1509:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isRetreating ()Z 
L1512:  ifne L1685 
L1515:  invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L1518:  aload_0 
L1519:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1522:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L1525:  aload_0 
L1526:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1529:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isAlly ()Z 
L1532:  invokevirtual Method com/fs/starfarer/combat/CombatEngine getTaskManager (IZ)Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L1535:  astore 9 
L1537:  aload 9 
L1539:  ifnull L1685 
L1542:  aload 9 
L1544:  aload_0 
L1545:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1548:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getAssignmentFor (Lcom/fs/starfarer/api/combat/ShipAPI;)Lcom/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo; 
L1551:  astore 10 
L1553:  aload 10 
L1555:  ifnull L1685 
L1558:  aload_0 
L1559:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1562:  invokestatic Method com/fs/starfarer/combat/ai/N 'ÓO0000' (Lcom/fs/starfarer/combat/entities/Ship;)Ljava/lang/String; 
L1565:  astore 11 
L1567:  ldc_w 'reckless' 
L1570:  aload 11 
L1572:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L1575:  istore 12 
L1577:  ldc_w 'aggressive' 
L1580:  aload 11 
L1582:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L1585:  istore 13 
L1587:  aload 10 
L1589:  invokeinterface InterfaceMethod com/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo getType ()Lcom/fs/starfarer/api/combat/CombatAssignmentType; 1 
L1594:  getstatic Field com/fs/starfarer/api/combat/CombatAssignmentType INTERCEPT Lcom/fs/starfarer/api/combat/CombatAssignmentType; 
L1597:  if_acmpne L1614 
L1600:  iload 12 
L1602:  ifne L1610 
L1605:  iload 13 
L1607:  ifeq L1614 
L1610:  iconst_1 
L1611:  goto L1615 
L1614:  iconst_0 
L1615:  istore 14 
L1617:  iload 14 
L1619:  ifne L1685 
L1622:  aload_0 
L1623:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1626:  iconst_0 
L1627:  aload 10 
L1629:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;ZLcom/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo;)Lcom/fs/starfarer/combat/entities/Ship; 
L1632:  astore 15 
L1634:  aload 15 
L1636:  ifnull L1685 
L1639:  new com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 
L1642:  dup 
L1643:  aload_0 
L1644:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1647:  aload 15 
L1649:  aload_0 
L1650:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L1653:  aload_0 
L1654:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L1657:  aload_0 
L1658:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L1661:  astore 16 
L1663:  aload_0 
L1664:  aload 16 
L1666:  fconst_2 
L1667:  iload 8 
L1669:  ifeq L1676 
L1672:  fconst_1 
L1673:  goto L1677 
L1676:  fconst_0 
L1677:  fadd 
L1678:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L1681:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1684:  return 
        .catch [0] from L1685 to L1707 using L7714 
L1685:  aload 5 
L1687:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L1690:  ifnull L1711 
L1693:  aload_0 
L1694:  aload 5 
L1696:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L1699:  aload 5 
L1701:  getfield Field com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o 'Ô00000' F 
L1704:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
L1707:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L1710:  return 
        .catch [0] from L1711 to L2241 using L7714 
L1711:  iload 7 
L1713:  ifne L1721 
L1716:  aload 5 
L1718:  ifnonnull L2164 
L1721:  aload_0 
L1722:  aconst_null 
L1723:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1726:  aload_0 
L1727:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1730:  invokestatic Method com/fs/starfarer/combat/ai/N do (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/ai/N$O0; 
L1733:  astore 6 
L1735:  aload 6 
L1737:  ifnull L1761 
L1740:  aload_0 
L1741:  aload 6 
L1743:  invokevirtual Method com/fs/starfarer/combat/ai/N$O0 'Ô00000' ()Lcom/fs/starfarer/combat/entities/Ship; 
L1746:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1749:  aload_0 
L1750:  aload 6 
L1752:  invokevirtual Method com/fs/starfarer/combat/ai/N$O0 'Ó00000' ()Z 
L1755:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L1758:  goto L2145 
L1761:  aload_0 
L1762:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1765:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCruiser ()Z 
L1768:  ifne L1781 
L1771:  aload_0 
L1772:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1775:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L1778:  ifeq L1837 
L1781:  aload_0 
L1782:  aload_0 
L1783:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1786:  invokestatic Method com/fs/starfarer/combat/ai/N 'ÒO0000' (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
L1789:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1792:  aload_0 
L1793:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1796:  ifnull L1837 
L1799:  aload_0 
L1800:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1803:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1806:  aload_0 
L1807:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1810:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1813:  invokestatic Method com/fs/starfarer/prototype/Utils void [u767] 
L1816:  fstore 8 
L1818:  fload 8 
L1820:  aload_0 
L1821:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI getMaxFiringRange ()F 
L1824:  ldc_w +500.0f 
L1827:  fadd 
L1828:  fcmpl 
L1829:  ifle L1837 
L1832:  aload_0 
L1833:  aconst_null 
L1834:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1837:  aload_0 
L1838:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1841:  ifnonnull L2091 
L1844:  aload_0 
L1845:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1848:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L1851:  ifeq L1969 
L1854:  aload_0 
L1855:  aload_0 
L1856:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1859:  aload_0 
L1860:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L1863:  iconst_1 
L1864:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ó00000' (Lcom/fs/starfarer/combat/entities/Ship;FZ)Lcom/fs/starfarer/combat/entities/Ship; 
L1867:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1870:  aload_0 
L1871:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1874:  ifnull L1981 
L1877:  aload_0 
L1878:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1881:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L1884:  ifeq L1981 
L1887:  aload_0 
L1888:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1891:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L1894:  ifnull L1981 
L1897:  aload_0 
L1898:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1901:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L1904:  invokevirtual Method com/fs/starfarer/combat/ai/L getSourceShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L1907:  astore 8 
L1909:  aload 8 
L1911:  ifnull L1981 
L1914:  aload_0 
L1915:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1918:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1921:  aload 8 
L1923:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1926:  invokestatic Method com/fs/starfarer/prototype/Utils void [u767] 
L1929:  fstore 9 
L1931:  fload 9 
L1933:  aload 8 
L1935:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L1938:  ldc_w +700.0f 
L1941:  fadd 
L1942:  fcmpg 
L1943:  ifge L1981 
L1946:  aload 8 
L1948:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isPullBackFighters ()Z 
L1951:  ifeq L1981 
L1954:  aload_0 
L1955:  aload_0 
L1956:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1959:  iconst_1 
L1960:  invokestatic Method com/fs/starfarer/combat/ai/N String (Lcom/fs/starfarer/combat/entities/Ship;Z)Lcom/fs/starfarer/combat/entities/Ship; 
L1963:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1966:  goto L1981 
L1969:  aload_0 
L1970:  aload_0 
L1971:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1974:  iconst_1 
L1975:  invokestatic Method com/fs/starfarer/combat/ai/N String (Lcom/fs/starfarer/combat/entities/Ship;Z)Lcom/fs/starfarer/combat/entities/Ship; 
L1978:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1981:  aload_0 
L1982:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1985:  ifnull L2022 
L1988:  aload_0 
L1989:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L1992:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1995:  aload_0 
L1996:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L1999:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L2002:  invokestatic Method com/fs/starfarer/prototype/Utils void [u767] 
L2005:  fstore 8 
L2007:  fload 8 
L2009:  aload_0 
L2010:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L2013:  fcmpl 
L2014:  ifle L2022 
L2017:  aload_0 
L2018:  aconst_null 
L2019:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2022:  aload_0 
L2023:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2026:  ifnonnull L2091 
L2029:  aload_0 
L2030:  aload_0 
L2031:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2034:  aload_0 
L2035:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2038:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L2041:  ldc +3.4028234663852886e+38f 
L2043:  iconst_1 
L2044:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;FZ)Lcom/fs/starfarer/combat/entities/Ship; 
L2047:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2050:  aload_0 
L2051:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2054:  ifnull L2091 
L2057:  aload_0 
L2058:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2061:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L2064:  aload_0 
L2065:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2068:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L2071:  invokestatic Method com/fs/starfarer/prototype/Utils void [u767] 
L2074:  fstore 8 
L2076:  fload 8 
L2078:  aload_0 
L2079:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L2082:  fcmpl 
L2083:  ifle L2091 
L2086:  aload_0 
L2087:  aconst_null 
L2088:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2091:  aload_0 
L2092:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2095:  ifnonnull L2114 
L2098:  aload_0 
L2099:  aload_0 
L2100:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2103:  aload_0 
L2104:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L2107:  iconst_0 
L2108:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ó00000' (Lcom/fs/starfarer/combat/entities/Ship;FZ)Lcom/fs/starfarer/combat/entities/Ship; 
L2111:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2114:  aload_0 
L2115:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L2118:  ifnull L2145 
L2121:  aload_0 
L2122:  aload_0 
L2123:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L2126:  invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI isValidOrderTarget (Lcom/fs/starfarer/combat/entities/Ship;)Z 
L2129:  ifeq L2145 
L2132:  aload_0 
L2133:  iconst_1 
L2134:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L2137:  aload_0 
L2138:  aload_0 
L2139:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L2142:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2145:  aload_0 
L2146:  aload_0 
L2147:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2150:  invokespecial Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI isValidOrderTarget (Lcom/fs/starfarer/combat/entities/Ship;)Z 
L2153:  ifne L2164 
L2156:  aload_0 
L2157:  aconst_null 
L2158:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2161:  aconst_null 
L2162:  astore 6 
L2164:  aload_0 
L2165:  aconst_null 
L2166:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L2169:  aload_0 
L2170:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI engine Lcom/fs/starfarer/combat/CombatEngine; 
L2173:  aload_0 
L2174:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2177:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L2180:  invokevirtual Method com/fs/starfarer/combat/CombatEngine getFogOfWar (I)Lcom/fs/starfarer/combat/B/OoOo; 
L2183:  astore 8 
L2185:  aload_0 
L2186:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2189:  ifnull L2245 
L2192:  aload_0 
L2193:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2196:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L2199:  aload_0 
L2200:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2203:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (ILcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)Z 
L2206:  ifne L2245 
L2209:  new com/fs/starfarer/combat/ai/movement/maneuvers/ExploreManeuver 
L2212:  dup 
L2213:  aload_0 
L2214:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2217:  aload_0 
L2218:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L2221:  aload_0 
L2222:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/ExploreManeuver <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L2225:  astore 9 
L2227:  aload_0 
L2228:  aload 9 
L2230:  fconst_2 
L2231:  invokestatic Method java/lang/Math random ()D 
L2234:  d2f 
L2235:  fconst_2 
L2236:  fmul 
L2237:  fadd 
L2238:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L2241:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L2244:  return 
        .catch [0] from L2245 to L3237 using L7714 
L2245:  aload_0 
L2246:  ldc +3.4028234663852886e+38f 
L2248:  putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L2251:  aload_0 
L2252:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2255:  ifnull L7722 
L2258:  aload_0 
L2259:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2262:  iconst_1 
L2263:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isNonCombat (Z)Z 
L2266:  istore 9 
L2268:  aload_0 
L2269:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2272:  iconst_0 
L2273:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isNonCombat (Z)Z 
L2276:  istore 10 
L2278:  aload_0 
L2279:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2282:  iconst_0 
L2283:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isNonCombat (Z)Z 
L2286:  istore 11 
L2288:  iconst_0 
L2289:  istore 12 
L2291:  iconst_0 
L2292:  istore 13 
L2294:  iconst_0 
L2295:  istore 14 
L2297:  invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L2300:  aload_0 
L2301:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2304:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L2307:  invokevirtual Method com/fs/starfarer/combat/CombatEngine getFleetManager (I)Lcom/fs/starfarer/combat/CombatFleetManager; 
L2310:  astore 15 
L2312:  aload 15 
L2314:  ifnull L2430 
L2317:  aload 15 
L2319:  aload_0 
L2320:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2323:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isAlly ()Z 
L2326:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getTaskManager (Z)Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L2329:  astore 16 
L2331:  aload 16 
L2333:  aload 15 
L2335:  aload_0 
L2336:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2339:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getDeployedFleetMember (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L2342:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getDirectOrders (Lcom/fs/starfarer/combat/CombatFleetManager$O0;)Lcom/fs/starfarer/combat/tasks/C; 
L2345:  astore 17 
L2347:  aload 17 
L2349:  ifnull L2398 
L2352:  aload 17 
L2354:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L2357:  ifnull L2398 
L2360:  aload 17 
L2362:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L2365:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L2368:  astore 18 
L2370:  aload 18 
L2372:  getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType HARASS Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L2375:  if_acmpne L2384 
L2378:  iconst_1 
L2379:  istore 12 
L2381:  goto L2430 
L2384:  aload 18 
L2386:  getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType RECON Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L2389:  if_acmpne L2430 
L2392:  iconst_1 
L2393:  istore 14 
L2395:  goto L2430 
L2398:  aload 16 
L2400:  aload_0 
L2401:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2404:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getAssignmentFor (Lcom/fs/starfarer/api/combat/ShipAPI;)Lcom/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo; 
L2407:  astore 18 
L2409:  aload 18 
L2411:  ifnull L2430 
L2414:  aload 18 
L2416:  invokeinterface InterfaceMethod com/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo getType ()Lcom/fs/starfarer/api/combat/CombatAssignmentType; 1 
L2421:  getstatic Field com/fs/starfarer/api/combat/CombatAssignmentType HARASS Lcom/fs/starfarer/api/combat/CombatAssignmentType; 
L2424:  if_acmpne L2430 
L2427:  iconst_1 
L2428:  istore 12 
L2430:  iload 12 
L2432:  ifeq L2437 
L2435:  iconst_0 
L2436:  istore_1 
L2437:  aload_0 
L2438:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2441:  invokestatic Method com/fs/starfarer/combat/ai/N 'ÓO0000' (Lcom/fs/starfarer/combat/entities/Ship;)Ljava/lang/String; 
L2444:  astore 16 
L2446:  ldc_w 'reckless' 
L2449:  aload 16 
L2451:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L2454:  istore 17 
L2456:  ldc_w 'aggressive' 
L2459:  aload 16 
L2461:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L2464:  istore 18 
L2466:  aload_0 
L2467:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2470:  invokestatic Method com/fs/starfarer/combat/ai/N 'ø00000' (Lcom/fs/starfarer/combat/entities/Ship;)Z 
L2473:  istore 19 
L2475:  aload_0 
L2476:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2479:  invokestatic Method com/fs/starfarer/combat/ai/N void (Lcom/fs/starfarer/combat/entities/Ship;)Z 
L2482:  ifeq L2518 
L2485:  aload_0 
L2486:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2489:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCarrier ()Z 
L2492:  ifeq L2514 
L2495:  aload_0 
L2496:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2499:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSpec ()Lcom/fs/starfarer/loading/specs/privatesuper; 
L2502:  invokevirtual Method com/fs/starfarer/loading/specs/privatesuper getHints ()Ljava/util/EnumSet; 
L2505:  getstatic Field com/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints COMBAT Lcom/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints; 
L2508:  invokevirtual Method java/util/EnumSet contains (Ljava/lang/Object;)Z 
L2511:  ifeq L2518 
L2514:  iconst_1 
L2515:  goto L2519 
L2518:  iconst_0 
L2519:  istore 20 
L2521:  aload_0 
L2522:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2525:  invokestatic Method com/fs/starfarer/combat/ai/N 'private' (Lcom/fs/starfarer/combat/entities/Ship;)Z 
L2528:  istore 21 
L2530:  invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L2533:  aload_0 
L2534:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2537:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L2540:  aload_0 
L2541:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2544:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isAlly ()Z 
L2547:  invokevirtual Method com/fs/starfarer/combat/CombatEngine getTaskManager (IZ)Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L2550:  astore 22 
L2552:  aload 16 
L2554:  ldc_w 'cautious' 
L2557:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L2560:  ifne L2579 
L2563:  aload 16 
L2565:  ldc_w 'timid' 
L2568:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L2571:  ifne L2579 
L2574:  iload 10 
L2576:  ifeq L2630 
L2579:  iload 10 
L2581:  ifne L2589 
L2584:  iload 11 
L2586:  ifne L2630 
L2589:  iload 18 
L2591:  ifne L2630 
L2594:  iload 17 
L2596:  ifne L2630 
L2599:  iload 21 
L2601:  ifne L2630 
L2604:  aload_0 
L2605:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2608:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L2611:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L2614:  aload_0 
L2615:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2618:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L2621:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L2624:  if_icmpgt L2630 
L2627:  iconst_1 
L2628:  istore 12 
L2630:  iload 18 
L2632:  ifne L2645 
L2635:  iload 17 
L2637:  ifne L2645 
L2640:  iload 21 
L2642:  ifeq L2656 
L2645:  iload 10 
L2647:  ifne L2656 
L2650:  iconst_1 
L2651:  istore 13 
L2653:  iconst_0 
L2654:  istore 12 
L2656:  aload_0 
L2657:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI config Lcom/fs/starfarer/api/combat/ShipAIConfig; 
L2660:  getfield Field com/fs/starfarer/api/combat/ShipAIConfig backingOffWhileNotVentingAllowed Z 
L2663:  ifne L2669 
L2666:  iconst_0 
L2667:  istore 12 
L2669:  aload_0 
L2670:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2673:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getSpec ()Lcom/fs/starfarer/loading/specs/HullVariantSpec; 
L2676:  invokevirtual Method com/fs/starfarer/loading/specs/HullVariantSpec getHullSpec ()Lcom/fs/starfarer/loading/specs/privatesuper; 
L2679:  invokevirtual Method com/fs/starfarer/loading/specs/privatesuper getHullId ()Ljava/lang/String; 
L2682:  astore 23 
L2684:  aload_0 
L2685:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2688:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L2691:  aload_0 
L2692:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2695:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L2698:  invokestatic Method com/fs/starfarer/prototype/Utils void [u767] 
L2701:  fstore 24 
L2703:  aload_0 
L2704:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2707:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L2710:  invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO isOverloadedOrVenting ()Z 
L2713:  ifeq L2754 
L2716:  aload_0 
L2717:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2720:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L2723:  invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO getOverloadTimeRemaining ()F 
L2726:  ldc_w +5.0f 
L2729:  fcmpl 
L2730:  ifgt L2750 
L2733:  aload_0 
L2734:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2737:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L2740:  invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO getTimeToVent ()F 
L2743:  ldc_w +5.0f 
L2746:  fcmpl 
L2747:  ifle L2754 
L2750:  iconst_1 
L2751:  goto L2755 
L2754:  iconst_0 
L2755:  istore 26 
L2757:  aload_0 
L2758:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2761:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L2764:  invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO isOverloadedOrVenting ()Z 
L2767:  ifeq L2804 
L2770:  aload_0 
L2771:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2774:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L2777:  invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO getOverloadTimeRemaining ()F 
L2780:  fconst_0 
L2781:  fcmpl 
L2782:  ifgt L2800 
L2785:  aload_0 
L2786:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2789:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L2792:  invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO getTimeToVent ()F 
L2795:  fconst_0 
L2796:  fcmpl 
L2797:  ifle L2804 
L2800:  iconst_1 
L2801:  goto L2805 
L2804:  iconst_0 
L2805:  istore 27 
L2807:  aload_0 
L2808:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2811:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxLevel ()F 
L2814:  ldc_w +0.699999988079071f 
L2817:  fcmpl 
L2818:  ifle L2825 
L2821:  iconst_1 
L2822:  goto L2826 
L2825:  iconst_0 
L2826:  istore 28 
L2828:  aload_0 
L2829:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2832:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxAvailable ()F 
L2835:  aload_0 
L2836:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2839:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxAvailable ()F 
L2842:  fcmpl 
L2843:  iflt L2850 
L2846:  iconst_1 
L2847:  goto L2851 
L2850:  iconst_0 
L2851:  istore 29 
L2853:  aload_0 
L2854:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2857:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxLevel ()F 
L2860:  ldc_w +0.800000011920929f 
L2863:  fcmpl 
L2864:  ifle L2871 
L2867:  iconst_1 
L2868:  goto L2872 
L2871:  iconst_0 
L2872:  istore 30 
L2874:  aload_0 
L2875:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2878:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L2881:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L2884:  aload_0 
L2885:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2888:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L2891:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L2894:  if_icmpge L2901 
L2897:  iconst_1 
L2898:  goto L2902 
L2901:  iconst_0 
L2902:  istore 31 
L2904:  aload_0 
L2905:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2908:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullLevel ()F 
L2911:  ldc_w +0.25f 
L2914:  fcmpg 
L2915:  ifge L2922 
L2918:  iconst_1 
L2919:  goto L2923 
L2922:  iconst_0 
L2923:  istore 32 
L2925:  aload_0 
L2926:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2929:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L2932:  aload_0 
L2933:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2936:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L2939:  aload_0 
L2940:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2943:  aload_0 
L2944:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L2947:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' [u908] 
L2950:  fmul 
L2951:  fadd 
L2952:  fstore 33 
L2954:  aload_0 
L2955:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2958:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getVariant ()Lcom/fs/starfarer/loading/specs/HullVariantSpec; 
L2961:  invokevirtual Method com/fs/starfarer/loading/specs/HullVariantSpec isCarrier ()Z 
L2964:  ifeq L3024 
L2967:  aload_0 
L2968:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2971:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSpec ()Lcom/fs/starfarer/loading/specs/privatesuper; 
L2974:  invokevirtual Method com/fs/starfarer/loading/specs/privatesuper getHints ()Ljava/util/EnumSet; 
L2977:  getstatic Field com/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints NO_AUTO_ESCORT Lcom/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints; 
L2980:  invokevirtual Method java/util/EnumSet contains (Ljava/lang/Object;)Z 
L2983:  ifne L3024 
L2986:  aload_0 
L2987:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L2990:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLaunchBays ()Ljava/util/List; 
L2993:  invokeinterface InterfaceMethod java/util/List size ()I 1 
L2998:  i2f 
L2999:  aload_0 
L3000:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3003:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getStats ()Lcom/fs/starfarer/combat/entities/ship/class; 
L3006:  invokevirtual Method com/fs/starfarer/combat/entities/ship/class getNumFighterBays ()Lcom/fs/starfarer/api/combat/MutableStat; 
L3009:  invokevirtual Method com/fs/starfarer/api/combat/MutableStat getModifiedValue ()F 
L3012:  ldc_w +0.6600000262260437f 
L3015:  fmul 
L3016:  fcmpl 
L3017:  iflt L3024 
L3020:  iconst_1 
L3021:  goto L3025 
L3024:  iconst_0 
L3025:  istore 34 
L3027:  aload 22 
L3029:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager isFullAssault ()Z 
L3032:  ifne L3241 
L3035:  iload 19 
L3037:  ifne L3241 
L3040:  iload 20 
L3042:  ifne L3241 
L3045:  aload_0 
L3046:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3049:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isRetreating ()Z 
L3052:  ifne L3241 
L3055:  iload 9 
L3057:  ifne L3086 
L3060:  iload 17 
L3062:  ifne L3086 
L3065:  iload 21 
L3067:  ifne L3086 
L3070:  iload 18 
L3072:  ifne L3086 
L3075:  aload_0 
L3076:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3079:  aconst_null 
L3080:  invokestatic Method com/fs/starfarer/combat/ai/N 'ÓO0000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)Z 
L3083:  ifeq L3091 
L3086:  iload 34 
L3088:  ifeq L3241 
L3091:  iconst_1 
L3092:  istore 35 
L3094:  iload 34 
L3096:  ifeq L3131 
L3099:  aload 22 
L3101:  aload_0 
L3102:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3105:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getAssignmentFor (Lcom/fs/starfarer/api/combat/ShipAPI;)Lcom/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo; 
L3108:  astore 36 
L3110:  aload 36 
L3112:  ifnull L3131 
L3115:  aload 36 
L3117:  invokeinterface InterfaceMethod com/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo getType ()Lcom/fs/starfarer/api/combat/CombatAssignmentType; 1 
L3122:  getstatic Field com/fs/starfarer/api/combat/CombatAssignmentType SEARCH_AND_DESTROY Lcom/fs/starfarer/api/combat/CombatAssignmentType; 
L3125:  if_acmpne L3131 
L3128:  iconst_0 
L3129:  istore 35 
L3131:  aload_0 
L3132:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3135:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3138:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MOVEMENT_DEST Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L3141:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L3144:  ifeq L3150 
L3147:  iconst_0 
L3148:  istore 35 
L3150:  iload 35 
L3152:  ifeq L3241 
L3155:  aload_0 
L3156:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3159:  invokestatic Method com/fs/starfarer/combat/ai/N 'Òo0000' (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
L3162:  astore 36 
L3164:  aload 36 
L3166:  ifnull L3241 
L3169:  aload 36 
L3171:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L3174:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L3177:  aload_0 
L3178:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3181:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L3184:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L3187:  if_icmpgt L3195 
L3190:  iload 34 
L3192:  ifeq L3241 
L3195:  new com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 
L3198:  dup 
L3199:  aload_0 
L3200:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3203:  aload 36 
L3205:  aload_0 
L3206:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3209:  aload_0 
L3210:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L3213:  aload_0 
L3214:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L3217:  astore 37 
L3219:  aload_0 
L3220:  aload 37 
L3222:  fconst_2 
L3223:  iload 34 
L3225:  ifeq L3232 
L3228:  fconst_1 
L3229:  goto L3233 
L3232:  fconst_0 
L3233:  fadd 
L3234:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L3237:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L3240:  return 
        .catch [0] from L3241 to L4469 using L7714 
L3241:  aload_0 
L3242:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3245:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L3248:  aload_0 
L3249:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L3252:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L3255:  invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' [u767] 
L3258:  fstore 35 
L3260:  aload_0 
L3261:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3264:  fload 35 
L3266:  ldc_w +90.0f 
L3269:  fload 24 
L3271:  iconst_0 
L3272:  iconst_1 
L3273:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;FFFZZ)I 
L3276:  istore 36 
L3278:  aload_0 
L3279:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3282:  aload_0 
L3283:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L3286:  ldc_w +3000.0f 
L3289:  iconst_1 
L3290:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FZ)I 
L3293:  istore 37 
L3295:  aload_0 
L3296:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3299:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L3302:  aload_0 
L3303:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L3306:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L3309:  new org/lwjgl/util/vector/Vector2f 
L3312:  dup 
L3313:  invokespecial Method org/lwjgl/util/vector/Vector2f <init> ()V 
L3316:  invokestatic Method org/lwjgl/util/vector/Vector2f sub (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L3319:  astore 38 
L3321:  aload 38 
L3323:  invokestatic Method com/fs/starfarer/prototype/Utils 'super' (Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L3326:  pop 
L3327:  aload_0 
L3328:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L3331:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFacing ()F 
L3334:  invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' (F)Lorg/lwjgl/util/vector/Vector2f; 
L3337:  astore 39 
L3339:  new org/lwjgl/util/vector/Vector2f 
L3342:  dup 
L3343:  aload_0 
L3344:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L3347:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getVelocity ()Lorg/lwjgl/util/vector/Vector2f; 
L3350:  invokespecial Method org/lwjgl/util/vector/Vector2f <init> (Lorg/lwjgl/util/vector/ReadableVector2f;)V 
L3353:  invokestatic Method com/fs/starfarer/prototype/Utils 'super' (Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L3356:  astore 40 
L3358:  aload 38 
L3360:  aload 39 
L3362:  invokestatic Method org/lwjgl/util/vector/Vector2f dot [u767] 
L3365:  fstore 41 
L3367:  iconst_0 
L3368:  istore 42 
L3370:  fload 41 
L3372:  ldc_w -0.25f 
L3375:  fcmpg 
L3376:  ifge L3382 
L3379:  iconst_1 
L3380:  istore 42 
L3382:  fload 41 
L3384:  ldc_w +0.7099999785423279f 
L3387:  fcmpg 
L3388:  ifge L3395 
L3391:  iconst_1 
L3392:  goto L3396 
L3395:  iconst_0 
L3396:  istore 43 
L3398:  aload_0 
L3399:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3402:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule Oo0000 ()Lcom/fs/starfarer/combat/ai/attack/super; 
L3405:  ifnull L3423 
L3408:  aload_0 
L3409:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3412:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule Oo0000 ()Lcom/fs/starfarer/combat/ai/attack/super; 
L3415:  invokevirtual Method com/fs/starfarer/combat/ai/attack/super 'ÒO0000' ()F 
L3418:  fstore 25 
L3420:  goto L3432 
L3423:  aload_0 
L3424:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3427:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'Ó00000' ()F 
L3430:  fstore 25 
L3432:  iload 12 
L3434:  istore 44 
L3436:  aload 16 
L3438:  ldc_w 'steady' 
L3441:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L3444:  ifne L3474 
L3447:  aload 16 
L3449:  ldc_w 'cautious' 
L3452:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L3455:  ifne L3474 
L3458:  aload 16 
L3460:  ldc_w 'timid' 
L3463:  invokevirtual Method java/lang/String equals (Ljava/lang/Object;)Z 
L3466:  ifne L3474 
L3469:  iload 10 
L3471:  ifeq L3507 
L3474:  iload 10 
L3476:  ifne L3484 
L3479:  iload 11 
L3481:  ifne L3507 
L3484:  iload 18 
L3486:  ifne L3507 
L3489:  iload 17 
L3491:  ifne L3507 
L3494:  iload 21 
L3496:  ifne L3507 
L3499:  iload 19 
L3501:  ifne L3507 
L3504:  iconst_1 
L3505:  istore 44 
L3507:  aload_0 
L3508:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3511:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'private' ()Z 
L3514:  ifne L3539 
L3517:  aload_0 
L3518:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3521:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule String ()F 
L3524:  aload_0 
L3525:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3528:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'ÓO0000' ()F 
L3531:  fcmpl 
L3532:  ifle L3539 
L3535:  iconst_1 
L3536:  goto L3540 
L3539:  iconst_0 
L3540:  istore 45 
L3542:  iload 45 
L3544:  iload 44 
L3546:  iand 
L3547:  istore 45 
L3549:  fload 25 
L3551:  aload_0 
L3552:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3555:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'õ00000' ()F 
L3558:  fcmpl 
L3559:  ifle L3576 
L3562:  iload 45 
L3564:  ifne L3576 
L3567:  aload_0 
L3568:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3571:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'õ00000' ()F 
L3574:  fstore 25 
L3576:  iload 45 
L3578:  ifeq L3686 
L3581:  aload_0 
L3582:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3585:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getSystem ()Lcom/fs/starfarer/combat/systems/OOoO; 
L3588:  ifnull L3686 
L3591:  aload_0 
L3592:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3595:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getStats ()Lcom/fs/starfarer/combat/entities/ship/class; 
L3598:  ifnull L3686 
L3601:  aload_0 
L3602:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3605:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getSystem ()Lcom/fs/starfarer/combat/systems/OOoO; 
L3608:  invokevirtual Method com/fs/starfarer/combat/systems/OOoO getSpec ()Lcom/fs/starfarer/loading/specs/M; 
L3611:  astore 46 
L3613:  aload 46 
L3615:  invokevirtual Method com/fs/starfarer/loading/specs/M getThreatAmount ()F 
L3618:  fconst_0 
L3619:  fcmpl 
L3620:  ifle L3686 
L3623:  aload 46 
L3625:  invokevirtual Method com/fs/starfarer/loading/specs/M getThreatAngle ()F 
L3628:  aload 46 
L3630:  invokevirtual Method com/fs/starfarer/loading/specs/M getThreatArc ()F 
L3633:  fconst_0 
L3634:  invokestatic Method com/fs/starfarer/prototype/Utils String (FFF)Z 
L3637:  ifeq L3686 
L3640:  aload 46 
L3642:  aload_0 
L3643:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3646:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getStats ()Lcom/fs/starfarer/combat/entities/ship/class; 
L3649:  invokevirtual Method com/fs/starfarer/loading/specs/M getThreatRange (Lcom/fs/starfarer/api/combat/MutableShipStatsAPI;)F 
L3652:  aload_0 
L3653:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3656:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L3659:  ldc_w +0.75f 
L3662:  fmul 
L3663:  fsub 
L3664:  fstore 47 
L3666:  fload 47 
L3668:  ldc_w +0.75f 
L3671:  fmul 
L3672:  fstore 47 
L3674:  fload 47 
L3676:  fload 25 
L3678:  fcmpg 
L3679:  ifge L3686 
L3682:  fload 47 
L3684:  fstore 25 
L3686:  iload 27 
L3688:  ifne L3720 
L3691:  iload 26 
L3693:  ifne L3711 
L3696:  iload 28 
L3698:  ifne L3720 
L3701:  iload 29 
L3703:  ifne L3711 
L3706:  iload 30 
L3708:  ifeq L3720 
L3711:  aload_0 
L3712:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3715:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'ÓO0000' ()F 
L3718:  fstore 25 
L3720:  aload_0 
L3721:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L3724:  aload_0 
L3725:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L3728:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Õ00000' [u921] 
L3731:  astore 46 
L3733:  fload 25 
L3735:  aload 46 
L3737:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L3740:  ldc_w +200.0f 
L3743:  fadd 
L3744:  invokestatic Method java/lang/Math min (FF)F 
L3747:  fstore 25 
L3749:  iload 12 
L3751:  ifeq L4017 
L3754:  iload 9 
L3756:  ifne L4017 
L3759:  aload_0 
L3760:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3763:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3766:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags HARASS_MOVE_IN Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L3769:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L3772:  ifne L3832 
L3775:  aload_0 
L3776:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3779:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3782:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags HARASS_MOVE_IN_COOLDOWN Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L3785:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L3788:  ifne L3832 
L3791:  iload 42 
L3793:  ifeq L3899 
L3796:  iload 37 
L3798:  ifle L3899 
L3801:  aload_0 
L3802:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3805:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3808:  getstatic Field [c482] HARASS_MOVE_IN [u486] 
L3811:  ldc_w +3.0f 
L3814:  aload_0 
L3815:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3818:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L3821:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L3824:  i2f 
L3825:  fadd 
L3826:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;F)V 
L3829:  goto L3899 
L3832:  iload 42 
L3834:  ifeq L3899 
L3837:  iload 37 
L3839:  ifle L3899 
L3842:  aload_0 
L3843:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3846:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3849:  getstatic Field [c482] HARASS_MOVE_IN [u486] 
L3852:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L3855:  ifne L3899 
L3858:  aload_0 
L3859:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3862:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3865:  getstatic Field [c482] HARASS_MOVE_IN [u486] 
L3868:  ldc_w +3.0f 
L3871:  aload_0 
L3872:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3875:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L3878:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L3881:  i2f 
L3882:  fadd 
L3883:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;F)V 
L3886:  aload_0 
L3887:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3890:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3893:  getstatic Field [c482] HARASS_MOVE_IN_COOLDOWN [u486] 
L3896:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags unsetFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)V 
L3899:  aload_0 
L3900:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L3903:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L3906:  getstatic Field [c482] HARASS_MOVE_IN [u486] 
L3909:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L3912:  ifeq L3935 
L3915:  aload_0 
L3916:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3919:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'øO0000' ()F 
L3922:  fload 25 
L3924:  invokestatic Method java/lang/Math max (FF)F 
L3927:  fstore 25 
L3929:  iconst_0 
L3930:  istore 12 
L3932:  goto L4017 
L3935:  ldc_w +200.0f 
L3938:  fstore 47 
L3940:  aload_0 
L3941:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3944:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'øO0000' ()F 
L3947:  aload 46 
L3949:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L3952:  fcmpl 
L3953:  ifle L4007 
L3956:  aload_0 
L3957:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L3960:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'øO0000' ()F 
L3963:  aload 46 
L3965:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L3968:  fsub 
L3969:  fstore 47 
L3971:  fload 47 
L3973:  ldc_w +0.699999988079071f 
L3976:  fmul 
L3977:  fstore 47 
L3979:  fload 47 
L3981:  ldc_w +50.0f 
L3984:  fcmpg 
L3985:  ifge L3993 
L3988:  ldc_w +50.0f 
L3991:  fstore 47 
L3993:  fload 47 
L3995:  ldc_w +200.0f 
L3998:  fcmpl 
L3999:  ifle L4007 
L4002:  ldc_w +200.0f 
L4005:  fstore 47 
L4007:  aload 46 
L4009:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L4012:  fload 47 
L4014:  fadd 
L4015:  fstore 25 
L4017:  iload 13 
L4019:  ifeq L4068 
L4022:  aload_0 
L4023:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4026:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'ÖO0000' ()F 
L4029:  ldc_w +0.699999988079071f 
L4032:  fmul 
L4033:  aload_0 
L4034:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4037:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'ÖO0000' ()F 
L4040:  ldc_w +200.0f 
L4043:  fsub 
L4044:  invokestatic Method java/lang/Math max (FF)F 
L4047:  fload 25 
L4049:  invokestatic Method java/lang/Math min (FF)F 
L4052:  fstore 25 
L4054:  fload 25 
L4056:  ldc_w +100.0f 
L4059:  fcmpg 
L4060:  ifge L4068 
L4063:  ldc_w +100.0f 
L4066:  fstore 25 
L4068:  aload_0 
L4069:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4072:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCarrier ()Z 
L4075:  ifeq L4177 
L4078:  aload_0 
L4079:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4082:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'private' ()Z 
L4085:  ifne L4177 
L4088:  aload_0 
L4089:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4092:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule String ()F 
L4095:  fstore 47 
L4097:  aload_0 
L4098:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4101:  ifnull L4122 
L4104:  fload 47 
L4106:  aload_0 
L4107:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4110:  aload_0 
L4111:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4114:  invokestatic Method com/fs/starfarer/combat/ai/N 'õ00000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)F 
L4117:  invokestatic Method java/lang/Math min (FF)F 
L4120:  fstore 47 
L4122:  aload_0 
L4123:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4126:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'õ00000' ()F 
L4129:  fstore 48 
L4131:  fload 47 
L4133:  fload 48 
L4135:  fcmpl 
L4136:  ifle L4172 
L4139:  iload 44 
L4141:  ifeq L4161 
L4144:  ldc_w +1500.0f 
L4147:  fload 47 
L4149:  ldc_w +0.800000011920929f 
L4152:  fmul 
L4153:  invokestatic Method java/lang/Math max (FF)F 
L4156:  fstore 25 
L4158:  goto L4177 
L4161:  fload 47 
L4163:  ldc_w +0.800000011920929f 
L4166:  fmul 
L4167:  fstore 25 
L4169:  goto L4177 
L4172:  ldc_w +2000.0f 
L4175:  fstore 25 
L4177:  iload 13 
L4179:  ifne L4195 
L4182:  iload 12 
L4184:  ifne L4195 
L4187:  fload 25 
L4189:  ldc_w +100.0f 
L4192:  fsub 
L4193:  fstore 25 
L4195:  fconst_0 
L4196:  fstore 47 
L4198:  aload 6 
L4200:  ifnull L4210 
L4203:  aload 6 
L4205:  invokevirtual Method com/fs/starfarer/combat/ai/N$O0 class ()F 
L4208:  fstore 47 
L4210:  aload_0 
L4211:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4214:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getShield ()Lcom/fs/starfarer/combat/systems/H; 
L4217:  ifnull L4237 
L4220:  aload_0 
L4221:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4224:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getShield ()Lcom/fs/starfarer/combat/systems/H; 
L4227:  invokevirtual Method com/fs/starfarer/combat/systems/H isFront ()Z 
L4230:  ifeq L4237 
L4233:  iconst_1 
L4234:  goto L4238 
L4237:  iconst_0 
L4238:  istore 48 
L4240:  aload_0 
L4241:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4244:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L4247:  fstore 49 
L4249:  fload 49 
L4251:  fconst_2 
L4252:  fmul 
L4253:  ldc_w +500.0f 
L4256:  fadd 
L4257:  fstore 50 
L4259:  aload_0 
L4260:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4263:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCruiser ()Z 
L4266:  ifeq L4277 
L4269:  fload 50 
L4271:  ldc_w +300.0f 
L4274:  fadd 
L4275:  fstore 50 
L4277:  aload_0 
L4278:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4281:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L4284:  ifeq L4295 
L4287:  fload 50 
L4289:  ldc_w +700.0f 
L4292:  fadd 
L4293:  fstore 50 
L4295:  iload 36 
L4297:  ifle L4526 
L4300:  aload_0 
L4301:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4304:  fload 35 
L4306:  ldc_w +90.0f 
L4309:  fadd 
L4310:  ldc_w +179.0f 
L4313:  fload 50 
L4315:  iconst_1 
L4316:  iconst_1 
L4317:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ò00000' (Lcom/fs/starfarer/combat/entities/Ship;FFFZZ)F 
L4320:  fstore 51 
L4322:  aload_0 
L4323:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4326:  fload 35 
L4328:  ldc_w +90.0f 
L4331:  fsub 
L4332:  ldc_w +179.0f 
L4335:  fload 50 
L4337:  iconst_1 
L4338:  iconst_1 
L4339:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ò00000' (Lcom/fs/starfarer/combat/entities/Ship;FFFZZ)F 
L4342:  fstore 52 
L4344:  ldc_w -1.0f 
L4347:  fstore 53 
L4349:  fload 51 
L4351:  fload 52 
L4353:  fcmpl 
L4354:  ifle L4361 
L4357:  fconst_1 
L4358:  goto L4364 
L4361:  ldc_w -1.0f 
L4364:  fstore 54 
L4366:  fload 51 
L4368:  fload 52 
L4370:  fcmpl 
L4371:  ifne L4377 
L4374:  fconst_0 
L4375:  fstore 54 
L4377:  iload 9 
L4379:  ifeq L4423 
L4382:  aload 46 
L4384:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L4387:  fload 33 
L4389:  fadd 
L4390:  ldc_w +1200.0f 
L4393:  fadd 
L4394:  fstore 25 
L4396:  aload_0 
L4397:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4400:  aload_0 
L4401:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4404:  invokestatic Method com/fs/starfarer/combat/ai/N 'õ00000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)F 
L4407:  fstore 55 
L4409:  fload 25 
L4411:  fload 55 
L4413:  fcmpl 
L4414:  ifle L4421 
L4417:  fload 55 
L4419:  fstore 25 
L4421:  iconst_0 
L4422:  istore_1 
L4423:  fload 54 
L4425:  fconst_0 
L4426:  fcmpl 
L4427:  ifeq L4473 
L4430:  new com/genir/aitweaks/features/maneuver/ManeuverObf 
L4433:  dup 
L4434:  aload_0 
L4435:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4438:  aload_0 
L4439:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4442:  aload_0 
L4443:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L4446:  fconst_0 
L4447:  fload 25 
L4449:  fload 54 
L4451:  aload_0 
L4452:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L4455:  aload_0 
L4456:  iload_1 
L4457:  invokespecial Method com/genir/aitweaks/features/maneuver/ManeuverObf <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L4460:  astore 55 
L4462:  aload_0 
L4463:  aload 55 
L4465:  fconst_2 
L4466:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L4469:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L4472:  return 
        .catch [0] from L4473 to L4522 using L7714 
L4473:  fload 24 
L4475:  fload 25 
L4477:  ldc_w +1000.0f 
L4480:  fadd 
L4481:  fcmpg 
L4482:  ifge L4526 
L4485:  new com/genir/aitweaks/features/maneuver/ManeuverObf 
L4488:  dup 
L4489:  aload_0 
L4490:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4493:  aload_0 
L4494:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4497:  aload_0 
L4498:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L4501:  fconst_0 
L4502:  fload 25 
L4504:  aload_0 
L4505:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L4508:  aload_0 
L4509:  iload_1 
L4510:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L4513:  astore 55 
L4515:  aload_0 
L4516:  aload 55 
L4518:  fconst_2 
L4519:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L4522:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L4525:  return 
        .catch [0] from L4526 to L4676 using L7714 
L4526:  ldc_w +500.0f 
L4529:  fstore 51 
L4531:  iload 13 
L4533:  ifeq L4541 
L4536:  ldc_w +300.0f 
L4539:  fstore 51 
L4541:  iload 9 
L4543:  ifeq L4592 
L4546:  iload 11 
L4548:  ifne L4592 
L4551:  aload 46 
L4553:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L4556:  fload 33 
L4558:  fadd 
L4559:  ldc_w +1200.0f 
L4562:  fadd 
L4563:  fstore 25 
L4565:  aload_0 
L4566:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4569:  aload_0 
L4570:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4573:  invokestatic Method com/fs/starfarer/combat/ai/N 'õ00000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)F 
L4576:  fstore 52 
L4578:  fload 25 
L4580:  fload 52 
L4582:  fcmpl 
L4583:  ifle L4590 
L4586:  fload 52 
L4588:  fstore 25 
L4590:  iconst_0 
L4591:  istore_1 
L4592:  aload_0 
L4593:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4596:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L4599:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L4602:  getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize FRIGATE Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L4605:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L4608:  if_icmplt L4680 
L4611:  iload 29 
L4613:  ifeq L4621 
L4616:  iload 31 
L4618:  ifeq L4680 
L4621:  iload 28 
L4623:  ifeq L4680 
L4626:  iload 26 
L4628:  ifne L4680 
L4631:  aload_0 
L4632:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4635:  astore 52 
L4637:  new [c500] 
L4640:  dup 
L4641:  aload_0 
L4642:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4645:  aload_0 
L4646:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4649:  aload_0 
L4650:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L4653:  fconst_0 
L4654:  fload 25 
L4656:  aload_0 
L4657:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L4660:  aload_0 
L4661:  iload_1 
L4662:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L4665:  astore 53 
L4667:  aload_0 
L4668:  aload 53 
L4670:  ldc_w +3.0f 
L4673:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L4676:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L4679:  return 
        .catch [0] from L4680 to L4736 using L7714 
L4680:  iload 27 
L4682:  ifeq L4740 
L4685:  new [c500] 
L4688:  dup 
L4689:  aload_0 
L4690:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4693:  aload_0 
L4694:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4697:  aload_0 
L4698:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L4701:  fconst_0 
L4702:  fload 25 
L4704:  fload 24 
L4706:  ldc_w +200.0f 
L4709:  fsub 
L4710:  fload 33 
L4712:  fsub 
L4713:  invokestatic Method java/lang/Math max (FF)F 
L4716:  aload_0 
L4717:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L4720:  aload_0 
L4721:  iload_1 
L4722:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L4725:  astore 52 
L4727:  aload_0 
L4728:  aload 52 
L4730:  ldc_w +3.0f 
L4733:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L4736:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L4739:  return 
        .catch [0] from L4740 to L5329 using L7714 
L4740:  aload_0 
L4741:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4744:  ldc_w +3000.0f 
L4747:  iconst_1 
L4748:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;FZ)I 
L4751:  istore 52 
L4753:  fload 25 
L4755:  aload 46 
L4757:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L4760:  fcmpl 
L4761:  ifle L4778 
L4764:  aload 46 
L4766:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L4769:  fconst_0 
L4770:  fcmpl 
L4771:  ifle L4778 
L4774:  iconst_1 
L4775:  goto L4779 
L4778:  iconst_0 
L4779:  istore 53 
L4781:  aload_0 
L4782:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4785:  aload_0 
L4786:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4789:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L4792:  aload_0 
L4793:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4796:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getVelocity ()Lorg/lwjgl/util/vector/Vector2f; 
L4799:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' [u944] 
L4802:  fstore 54 
L4804:  fload 54 
L4806:  fconst_0 
L4807:  fcmpg 
L4808:  ifge L4815 
L4811:  iconst_1 
L4812:  goto L4816 
L4815:  iconst_0 
L4816:  istore 55 
L4818:  aload_0 
L4819:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4822:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getMaxSpeed ()F 
L4825:  ldc_w +100.0f 
L4828:  fcmpl 
L4829:  ifle L4836 
L4832:  iconst_1 
L4833:  goto L4837 
L4836:  iconst_0 
L4837:  istore 56 
L4839:  aload_0 
L4840:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4843:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L4846:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L4849:  aload_0 
L4850:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4853:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L4856:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L4859:  if_icmpgt L4866 
L4862:  iconst_1 
L4863:  goto L4867 
L4866:  iconst_0 
L4867:  istore 57 
L4869:  iload 26 
L4871:  ifeq L4877 
L4874:  iconst_0 
L4875:  istore 53 
L4877:  iload 9 
L4879:  ifeq L4957 
L4882:  iload 11 
L4884:  ifeq L4913 
L4887:  aload_0 
L4888:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4891:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getVariant ()Lcom/fs/starfarer/loading/specs/HullVariantSpec; 
L4894:  invokevirtual Method com/fs/starfarer/loading/specs/HullVariantSpec isCarrier ()Z 
L4897:  ifne L4957 
L4900:  aload_0 
L4901:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4904:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getVariant ()Lcom/fs/starfarer/loading/specs/HullVariantSpec; 
L4907:  invokevirtual Method com/fs/starfarer/loading/specs/HullVariantSpec isCarrier ()Z 
L4910:  ifeq L4957 
L4913:  iconst_1 
L4914:  istore 53 
L4916:  aload 46 
L4918:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo class ()F 
L4921:  fload 33 
L4923:  fadd 
L4924:  ldc_w +1200.0f 
L4927:  fadd 
L4928:  fstore 25 
L4930:  aload_0 
L4931:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4934:  aload_0 
L4935:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4938:  invokestatic Method com/fs/starfarer/combat/ai/N 'õ00000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)F 
L4941:  fstore 58 
L4943:  fload 25 
L4945:  fload 58 
L4947:  fcmpl 
L4948:  ifle L4955 
L4951:  fload 58 
L4953:  fstore 25 
L4955:  iconst_0 
L4956:  istore_1 
L4957:  iload 9 
L4959:  ifne L4980 
L4962:  iload 11 
L4964:  ifne L4977 
L4967:  aload_0 
L4968:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L4971:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isRetreating ()Z 
L4974:  ifeq L4980 
L4977:  iconst_0 
L4978:  istore 53 
L4980:  iload 12 
L4982:  ifeq L4988 
L4985:  iconst_1 
L4986:  istore 53 
L4988:  iload 21 
L4990:  ifeq L4996 
L4993:  iconst_0 
L4994:  istore 53 
L4996:  aload_0 
L4997:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L5000:  getstatic Field [c482] BACK_OFF [u486] 
L5003:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L5006:  ifeq L5012 
L5009:  iconst_1 
L5010:  istore 53 
L5012:  aload_0 
L5013:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5016:  fload 35 
L5018:  ldc_w +120.0f 
L5021:  fload 24 
L5023:  ldc_w +1000.0f 
L5026:  fadd 
L5027:  iconst_1 
L5028:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;FFFZ)F 
L5031:  fstore 58 
L5033:  aload_0 
L5034:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5037:  fload 35 
L5039:  ldc_w +150.0f 
L5042:  fload 24 
L5044:  ldc_w +1500.0f 
L5047:  fadd 
L5048:  iconst_1 
L5049:  iconst_1 
L5050:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ò00000' (Lcom/fs/starfarer/combat/entities/Ship;FFFZZ)F 
L5053:  fstore 59 
L5055:  aload_0 
L5056:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5059:  aload_0 
L5060:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5063:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFacing ()F 
L5066:  ldc_w +180.0f 
L5069:  fadd 
L5070:  ldc_w +170.0f 
L5073:  ldc_w +1500.0f 
L5076:  fload 24 
L5078:  ldc_w +500.0f 
L5081:  fadd 
L5082:  invokestatic Method java/lang/Math max (FF)F 
L5085:  iconst_1 
L5086:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;FFFZ)F 
L5089:  fstore 60 
L5091:  fload 59 
L5093:  fconst_0 
L5094:  fcmpg 
L5095:  ifgt L5101 
L5098:  iconst_0 
L5099:  istore 42 
L5101:  fload 58 
L5103:  fload 59 
L5105:  getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize CAPITAL_SHIP Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L5108:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L5111:  i2f 
L5112:  fadd 
L5113:  fcmpl 
L5114:  ifle L5121 
L5117:  iconst_1 
L5118:  goto L5122 
L5121:  iconst_0 
L5122:  istore 61 
L5124:  iload 61 
L5126:  fload 24 
L5128:  aload 46 
L5130:  invokevirtual Method [c542] class ()F 
L5133:  fload 25 
L5135:  invokestatic Method java/lang/Math max (FF)F 
L5138:  fload 33 
L5140:  fadd 
L5141:  ldc_w +750.0f 
L5144:  fadd 
L5145:  fcmpg 
L5146:  ifge L5153 
L5149:  iconst_1 
L5150:  goto L5154 
L5153:  iconst_0 
L5154:  iand 
L5155:  istore 61 
L5157:  fload 60 
L5159:  fconst_0 
L5160:  fcmpl 
L5161:  ifle L5167 
L5164:  iconst_1 
L5165:  istore 61 
L5167:  aload_0 
L5168:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5171:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L5174:  aload_0 
L5175:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5178:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L5181:  invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' [u767] 
L5184:  fstore 62 
L5186:  aload_0 
L5187:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5190:  aload_0 
L5191:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5194:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L5197:  fload 62 
L5199:  ldc_w +190.0f 
L5202:  ldc_w +1500.0f 
L5205:  fload 24 
L5207:  ldc_w +500.0f 
L5210:  fadd 
L5211:  invokestatic Method java/lang/Math max (FF)F 
L5214:  aload_0 
L5215:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5218:  iconst_1 
L5219:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;FFFLcom/fs/starfarer/combat/entities/Ship;Z)F 
L5222:  fstore 63 
L5224:  fload 63 
L5226:  fconst_0 
L5227:  fcmpl 
L5228:  ifle L5250 
L5231:  fload 24 
L5233:  ldc_w +2000.0f 
L5236:  fload 25 
L5238:  fconst_2 
L5239:  fmul 
L5240:  invokestatic Method java/lang/Math max (FF)F 
L5243:  fcmpg 
L5244:  ifge L5250 
L5247:  iconst_1 
L5248:  istore 61 
L5250:  aload_0 
L5251:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI getEval ()Lcom/fs/starfarer/combat/ai/C; 
L5254:  invokevirtual Method com/fs/starfarer/combat/ai/C 'Õ00000' ()Z 
L5257:  ifeq L5263 
L5260:  iconst_1 
L5261:  istore 61 
L5263:  iload 21 
L5265:  ifeq L5271 
L5268:  iconst_0 
L5269:  istore 61 
L5271:  iload 48 
L5273:  istore 64 
L5275:  iload 61 
L5277:  ifeq L5333 
L5280:  new [c500] 
L5283:  dup 
L5284:  aload_0 
L5285:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5288:  aload_0 
L5289:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5292:  aload_0 
L5293:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L5296:  fconst_0 
L5297:  fload 25 
L5299:  fload 24 
L5301:  ldc_w +200.0f 
L5304:  fsub 
L5305:  fload 33 
L5307:  fsub 
L5308:  invokestatic Method java/lang/Math max (FF)F 
L5311:  aload_0 
L5312:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L5315:  aload_0 
L5316:  iload_1 
L5317:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L5320:  astore 65 
L5322:  aload_0 
L5323:  aload 65 
L5325:  fconst_2 
L5326:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L5329:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L5332:  return 
        .catch [0] from L5333 to L5568 using L7714 
L5333:  iload 21 
L5335:  ifeq L5341 
L5338:  iconst_1 
L5339:  istore 19 
L5341:  new com/fs/starfarer/combat/ai/movement/maneuvers/Stringsuper 
L5344:  dup 
L5345:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/Stringsuper <init> ()V 
L5348:  astore 65 
L5350:  aload 65 
L5352:  aload_0 
L5353:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5356:  aload_0 
L5357:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5360:  aload_0 
L5361:  invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/Stringsuper o00000 (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L5364:  aload 65 
L5366:  invokevirtual Method com/fs/starfarer/combat/ai/movement/maneuvers/Stringsuper class ()Z 
L5369:  ifeq L5376 
L5372:  iconst_0 
L5373:  goto L5377 
L5376:  iconst_1 
L5377:  istore 66 
L5379:  aload_0 
L5380:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L5383:  getstatic Field [c482] DO_NOT_PURSUE [u486] 
L5386:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L5389:  istore 67 
L5391:  iload 67 
L5393:  aload_0 
L5394:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI aiFlags Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L5397:  getstatic Field [c482] BACK_OFF [u486] 
L5400:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L5403:  ior 
L5404:  istore 67 
L5406:  getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ALLOW_OLD_STRAFE Z 
L5409:  ifeq L5572 
L5412:  iload 19 
L5414:  ifne L5572 
L5417:  iload 20 
L5419:  ifne L5572 
L5422:  iload 67 
L5424:  ifne L5572 
L5427:  iload 37 
L5429:  ifne L5572 
L5432:  iload 52 
L5434:  ifle L5572 
L5437:  iload 36 
L5439:  iconst_1 
L5440:  if_icmpeq L5572 
L5443:  iload 42 
L5445:  ifne L5572 
L5448:  fload 24 
L5450:  fload 25 
L5452:  fload 33 
L5454:  fadd 
L5455:  ldc_w +100.0f 
L5458:  fadd 
L5459:  fcmpl 
L5460:  ifle L5572 
L5463:  fload 24 
L5465:  aload 46 
L5467:  invokevirtual Method [c542] class ()F 
L5470:  fload 33 
L5472:  fadd 
L5473:  ldc_w +100.0f 
L5476:  fsub 
L5477:  fcmpl 
L5478:  ifle L5572 
L5481:  aload_0 
L5482:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5485:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L5488:  ifne L5572 
L5491:  iload 64 
L5493:  ifne L5572 
L5496:  iload 57 
L5498:  ifeq L5572 
L5501:  ldc_w +2000.0f 
L5504:  aload 46 
L5506:  invokevirtual Method [c542] class ()F 
L5509:  ldc_w +750.0f 
L5512:  fload 41 
L5514:  fmul 
L5515:  fadd 
L5516:  invokestatic Method java/lang/Math min (FF)F 
L5519:  fstore 68 
L5521:  aload_0 
L5522:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5525:  aload_0 
L5526:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5529:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ò00000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)F 
L5532:  fstore 69 
L5534:  new com/fs/starfarer/combat/ai/movement/maneuvers/return 
L5537:  dup 
L5538:  aload_0 
L5539:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5542:  aload_0 
L5543:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5546:  fconst_0 
L5547:  fload 68 
L5549:  fload 69 
L5551:  aload_0 
L5552:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L5555:  aload_0 
L5556:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/return <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L5559:  astore 70 
L5561:  aload_0 
L5562:  aload 70 
L5564:  fconst_1 
L5565:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L5568:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L5571:  return 
        .catch [0] from L5572 to L5779 using L7714 
L5572:  aload_0 
L5573:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5576:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L5579:  aload_0 
L5580:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5583:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L5586:  invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' [u767] 
L5589:  aload_0 
L5590:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5593:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFacing ()F 
L5596:  invokestatic Method com/fs/starfarer/prototype/Utils 'super' (FF)F 
L5599:  ldc_w +60.0f 
L5602:  fcmpg 
L5603:  ifge L5610 
L5606:  iconst_1 
L5607:  goto L5611 
L5610:  iconst_0 
L5611:  istore 68 
L5613:  iload 68 
L5615:  ifeq L5783 
L5618:  fload 24 
L5620:  aload 46 
L5622:  invokevirtual Method [c542] class ()F 
L5625:  fload 25 
L5627:  invokestatic Method java/lang/Math max (FF)F 
L5630:  fload 33 
L5632:  fadd 
L5633:  ldc_w +1500.0f 
L5636:  fadd 
L5637:  fcmpg 
L5638:  ifge L5783 
L5641:  aload_0 
L5642:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5645:  fload 35 
L5647:  ldc_w +90.0f 
L5650:  fadd 
L5651:  ldc_w +179.0f 
L5654:  fload 50 
L5656:  iconst_1 
L5657:  iconst_1 
L5658:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ò00000' (Lcom/fs/starfarer/combat/entities/Ship;FFFZZ)F 
L5661:  fstore 69 
L5663:  aload_0 
L5664:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5667:  fload 35 
L5669:  ldc_w +90.0f 
L5672:  fsub 
L5673:  ldc_w +179.0f 
L5676:  fload 50 
L5678:  iconst_1 
L5679:  iconst_1 
L5680:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ò00000' (Lcom/fs/starfarer/combat/entities/Ship;FFFZZ)F 
L5683:  fstore 70 
L5685:  ldc_w -1.0f 
L5688:  fstore 71 
L5690:  fload 69 
L5692:  fload 70 
L5694:  fcmpl 
L5695:  ifle L5702 
L5698:  fconst_1 
L5699:  goto L5705 
L5702:  ldc_w -1.0f 
L5705:  fstore 72 
L5707:  fload 69 
L5709:  fload 70 
L5711:  fcmpl 
L5712:  ifne L5718 
L5715:  fconst_0 
L5716:  fstore 72 
L5718:  fload 72 
L5720:  fconst_0 
L5721:  fcmpl 
L5722:  ifeq L5783 
L5725:  new [c500] 
L5728:  dup 
L5729:  aload_0 
L5730:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5733:  aload_0 
L5734:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5737:  aload_0 
L5738:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L5741:  fconst_0 
L5742:  fload 25 
L5744:  fload 24 
L5746:  ldc_w +200.0f 
L5749:  fload 71 
L5751:  fmul 
L5752:  fadd 
L5753:  fload 33 
L5755:  fsub 
L5756:  invokestatic Method java/lang/Math max (FF)F 
L5759:  fload 72 
L5761:  aload_0 
L5762:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L5765:  aload_0 
L5766:  iload_1 
L5767:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L5770:  astore 73 
L5772:  aload_0 
L5773:  aload 73 
L5775:  fconst_2 
L5776:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L5779:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L5782:  return 
        .catch [0] from L5783 to L5868 using L7714 
L5783:  iload 26 
L5785:  ifeq L5961 
L5788:  iload 53 
L5790:  ifeq L5872 
L5793:  new com/fs/starfarer/combat/ai/movement/maneuvers/S 
L5796:  dup 
L5797:  aload_0 
L5798:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5801:  aload_0 
L5802:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5805:  fload 25 
L5807:  ldc_w +0.5f 
L5810:  fmul 
L5811:  aload_0 
L5812:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L5815:  aload_0 
L5816:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/S <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L5819:  astore 69 
L5821:  new [c500] 
L5824:  dup 
L5825:  aload_0 
L5826:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5829:  aload_0 
L5830:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5833:  aload_0 
L5834:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L5837:  fconst_0 
L5838:  ldc_w +200.0f 
L5841:  fload 25 
L5843:  ldc_w +0.5f 
L5846:  fmul 
L5847:  invokestatic Method java/lang/Math max (FF)F 
L5850:  aload_0 
L5851:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L5854:  aload_0 
L5855:  iload_1 
L5856:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L5859:  astore 69 
L5861:  aload_0 
L5862:  aload 69 
L5864:  fconst_2 
L5865:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L5868:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L5871:  return 
        .catch [0] from L5872 to L5957 using L7714 
L5872:  iload 66 
L5874:  ifeq L5927 
L5877:  new [c500] 
L5880:  dup 
L5881:  aload_0 
L5882:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5885:  aload_0 
L5886:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5889:  aload_0 
L5890:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L5893:  fconst_0 
L5894:  ldc_w +200.0f 
L5897:  fload 25 
L5899:  ldc_w +0.5f 
L5902:  fmul 
L5903:  invokestatic Method java/lang/Math max (FF)F 
L5906:  aload_0 
L5907:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L5910:  aload_0 
L5911:  iload_1 
L5912:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L5915:  astore 69 
L5917:  aload_0 
L5918:  aload 69 
L5920:  fconst_2 
L5921:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L5924:  goto L5957 
L5927:  new com/fs/starfarer/combat/ai/movement/maneuvers/B 
L5930:  dup 
L5931:  aload_0 
L5932:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L5935:  aload_0 
L5936:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L5939:  fconst_1 
L5940:  aload_0 
L5941:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L5944:  aload_0 
L5945:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/B <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L5948:  astore 69 
L5950:  aload_0 
L5951:  aload 69 
L5953:  fconst_2 
L5954:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L5957:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L5960:  return 
        .catch [0] from L5961 to L6472 using L7714 
L5961:  aload 40 
L5963:  aload 39 
L5965:  invokestatic Method org/lwjgl/util/vector/Vector2f dot [u767] 
L5968:  fstore 69 
L5970:  fload 69 
L5972:  ldc_w -0.10000000149011612f 
L5975:  fcmpg 
L5976:  ifge L5983 
L5979:  iconst_1 
L5980:  goto L5984 
L5983:  iconst_0 
L5984:  istore 70 
L5986:  fload 24 
L5988:  fload 25 
L5990:  fload 33 
L5992:  fadd 
L5993:  fload 25 
L5995:  ldc_w +1000.0f 
L5998:  invokestatic Method java/lang/Math min (FF)F 
L6001:  fadd 
L6002:  fcmpl 
L6003:  ifle L6476 
L6006:  fconst_1 
L6007:  fstore 71 
L6009:  iload 53 
L6011:  ifeq L6034 
L6014:  fload 24 
L6016:  fload 25 
L6018:  fload 33 
L6020:  fadd 
L6021:  ldc_w +500.0f 
L6024:  fadd 
L6025:  fcmpg 
L6026:  ifge L6034 
L6029:  ldc_w +0.25f 
L6032:  fstore 71 
L6034:  iconst_0 
L6035:  istore 72 
L6037:  aload_0 
L6038:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6041:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L6044:  ifne L6050 
L6047:  iconst_1 
L6048:  istore 72 
L6050:  invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L6053:  iload 72 
L6055:  invokevirtual Method com/fs/starfarer/combat/CombatEngine getFleetManager (I)Lcom/fs/starfarer/combat/CombatFleetManager; 
L6058:  astore 73 
L6060:  aload 73 
L6062:  ifnull L6080 
L6065:  aload 73 
L6067:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getGoal ()Lcom/fs/starfarer/api/fleet/FleetGoal; 
L6070:  getstatic Field com/fs/starfarer/api/fleet/FleetGoal ESCAPE Lcom/fs/starfarer/api/fleet/FleetGoal; 
L6073:  if_acmpne L6080 
L6076:  iconst_1 
L6077:  goto L6081 
L6080:  iconst_0 
L6081:  istore 74 
L6083:  iload 67 
L6085:  ifne L6262 
L6088:  iload 42 
L6090:  ifne L6154 
L6093:  aload_0 
L6094:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6097:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L6100:  ifeq L6124 
L6103:  iload 53 
L6105:  ifne L6124 
L6108:  invokestatic Method java/lang/Math random ()D 
L6111:  d2f 
L6112:  ldc_w +0.5f 
L6115:  fcmpl 
L6116:  ifgt L6154 
L6119:  iload 74 
L6121:  ifne L6154 
L6124:  aload_0 
L6125:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L6128:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ô00000' ()Z 
L6131:  ifne L6262 
L6134:  iload 55 
L6136:  ifne L6154 
L6139:  aload_0 
L6140:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6143:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L6146:  ifeq L6154 
L6149:  iload 74 
L6151:  ifeq L6262 
L6154:  new com/fs/starfarer/combat/ai/movement/maneuvers/B 
L6157:  dup 
L6158:  aload_0 
L6159:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6162:  aload_0 
L6163:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6166:  fload 71 
L6168:  aload_0 
L6169:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L6172:  aload_0 
L6173:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/B <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L6176:  astore 75 
L6178:  iload 66 
L6180:  ifeq L6223 
L6183:  new [c500] 
L6186:  dup 
L6187:  aload_0 
L6188:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6191:  aload_0 
L6192:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6195:  aload_0 
L6196:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L6199:  fconst_0 
L6200:  ldc_w +200.0f 
L6203:  fload 25 
L6205:  ldc_w +0.5f 
L6208:  fmul 
L6209:  invokestatic Method java/lang/Math max (FF)F 
L6212:  aload_0 
L6213:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L6216:  aload_0 
L6217:  iload_1 
L6218:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L6221:  astore 75 
L6223:  aload_0 
L6224:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6227:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L6230:  ifeq L6250 
L6233:  iload 53 
L6235:  ifne L6250 
L6238:  aload_0 
L6239:  aload 75 
L6241:  ldc_w +3.0f 
L6244:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L6247:  goto L7722 
L6250:  aload_0 
L6251:  aload 75 
L6253:  ldc_w +1.5f 
L6256:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L6259:  goto L7722 
L6262:  iload 53 
L6264:  ifeq L6290 
L6267:  fload 24 
L6269:  fload 25 
L6271:  fload 33 
L6273:  fadd 
L6274:  ldc_w +100.0f 
L6277:  fsub 
L6278:  fcmpg 
L6279:  ifge L6290 
L6282:  fload 25 
L6284:  ldc_w +1000.0f 
L6287:  fadd 
L6288:  fstore 25 
L6290:  aload 46 
L6292:  invokevirtual Method [c542] class ()F 
L6295:  ldc_w +500.0f 
L6298:  fadd 
L6299:  fload 24 
L6301:  ldc_w +300.0f 
L6304:  fsub 
L6305:  fload 33 
L6307:  fsub 
L6308:  invokestatic Method java/lang/Math min (FF)F 
L6311:  fstore 75 
L6313:  fload 25 
L6315:  fload 75 
L6317:  invokestatic Method java/lang/Math max (FF)F 
L6320:  fstore 75 
L6322:  iload 48 
L6324:  ifne L6337 
L6327:  aload_0 
L6328:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6331:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L6334:  ifeq L6377 
L6337:  new [c500] 
L6340:  dup 
L6341:  aload_0 
L6342:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6345:  aload_0 
L6346:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6349:  aload_0 
L6350:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L6353:  fconst_0 
L6354:  fload 75 
L6356:  aload_0 
L6357:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L6360:  aload_0 
L6361:  iload_1 
L6362:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L6365:  astore 76 
L6367:  aload_0 
L6368:  aload 76 
L6370:  fconst_2 
L6371:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L6374:  goto L6472 
L6377:  getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ALLOW_OLD_STRAFE Z 
L6380:  ifeq L6435 
L6383:  fload 75 
L6385:  fload 25 
L6387:  ldc_w +500.0f 
L6390:  fadd 
L6391:  fcmpl 
L6392:  ifle L6435 
L6395:  iload 67 
L6397:  ifne L6435 
L6400:  new com/fs/starfarer/combat/ai/movement/maneuvers/return 
L6403:  dup 
L6404:  aload_0 
L6405:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6408:  aload_0 
L6409:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6412:  fconst_0 
L6413:  fload 75 
L6415:  aload_0 
L6416:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L6419:  aload_0 
L6420:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/return <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L6423:  astore 76 
L6425:  aload_0 
L6426:  aload 76 
L6428:  fconst_2 
L6429:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L6432:  goto L6472 
L6435:  new [c500] 
L6438:  dup 
L6439:  aload_0 
L6440:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6443:  aload_0 
L6444:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6447:  aload_0 
L6448:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L6451:  fconst_0 
L6452:  fload 75 
L6454:  aload_0 
L6455:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L6458:  aload_0 
L6459:  iload_1 
L6460:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L6463:  astore 76 
L6465:  aload_0 
L6466:  aload 76 
L6468:  fconst_2 
L6469:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L6472:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L6475:  return 
        .catch [0] from L6476 to L6671 using L7714 
L6476:  fload 24 
L6478:  ldc_w +1000.0f 
L6481:  fload 25 
L6483:  fload 33 
L6485:  fadd 
L6486:  fconst_2 
L6487:  fdiv 
L6488:  invokestatic Method java/lang/Math min (FF)F 
L6491:  fcmpg 
L6492:  ifge L6499 
L6495:  iconst_1 
L6496:  goto L6500 
L6499:  iconst_0 
L6500:  istore 71 
L6502:  iconst_0 
L6503:  istore 71 
L6505:  aload_0 
L6506:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6509:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L6512:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L6515:  aload_0 
L6516:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6519:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L6522:  invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L6525:  if_icmplt L6617 
L6528:  iload 70 
L6530:  ifeq L6617 
L6533:  aload_0 
L6534:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6537:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getMaxSpeed ()F 
L6540:  ldc_w +50.0f 
L6543:  fadd 
L6544:  aload_0 
L6545:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6548:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getMaxSpeed ()F 
L6551:  fcmpl 
L6552:  ifle L6617 
L6555:  fload 58 
L6557:  aload_0 
L6558:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6561:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;)F 
L6564:  fcmpg 
L6565:  ifgt L6617 
L6568:  fload 24 
L6570:  fload 25 
L6572:  fload 33 
L6574:  fadd 
L6575:  fcmpl 
L6576:  ifle L6617 
L6579:  aload_0 
L6580:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6583:  aload_0 
L6584:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6587:  getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize FRIGATE Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L6590:  fload 24 
L6592:  ldc_w +200.0f 
L6595:  fadd 
L6596:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipAPI$HullSize;F)Lcom/fs/starfarer/combat/entities/Ship; 
L6599:  if_acmpne L6617 
L6602:  invokestatic Method java/lang/Math random ()D 
L6605:  d2f 
L6606:  ldc_w +0.25f 
L6609:  fcmpl 
L6610:  ifle L6617 
L6613:  iconst_1 
L6614:  goto L6618 
L6617:  iconst_0 
L6618:  istore 72 
L6620:  iload 72 
L6622:  ifeq L6675 
L6625:  iload 67 
L6627:  ifne L6675 
L6630:  invokestatic Method java/lang/Math random ()D 
L6633:  pop2 
L6634:  new com/fs/starfarer/combat/ai/movement/maneuvers/B 
L6637:  dup 
L6638:  aload_0 
L6639:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6642:  aload_0 
L6643:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6646:  fconst_1 
L6647:  aload_0 
L6648:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L6651:  aload_0 
L6652:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/B <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L6655:  astore 73 
L6657:  aload_0 
L6658:  aload 73 
L6660:  fconst_1 
L6661:  invokestatic Method java/lang/Math random ()D 
L6664:  d2f 
L6665:  fconst_1 
L6666:  fmul 
L6667:  fadd 
L6668:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L6671:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L6674:  return 
        .catch [0] from L6675 to L7002 using L7714 
L6675:  aload_0 
L6676:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6679:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L6682:  ifne L6695 
L6685:  aload_0 
L6686:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6689:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L6692:  ifeq L6709 
L6695:  aload_0 
L6696:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6699:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L6702:  ifeq L6709 
L6705:  iconst_1 
L6706:  goto L6710 
L6709:  iconst_0 
L6710:  istore 73 
L6712:  aload_0 
L6713:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6716:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getMaxSpeed ()F 
L6719:  ldc_w +100.0f 
L6722:  invokestatic Method java/lang/Math random ()D 
L6725:  d2f 
L6726:  ldc_w +50.0f 
L6729:  fmul 
L6730:  fadd 
L6731:  fcmpl 
L6732:  ifle L6739 
L6735:  iconst_1 
L6736:  goto L6740 
L6739:  iconst_0 
L6740:  istore 74 
L6742:  aload_0 
L6743:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L6746:  invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule if ()Z 
L6749:  ifeq L6756 
L6752:  iconst_0 
L6753:  goto L6757 
L6756:  iconst_1 
L6757:  istore 75 
L6759:  iload 74 
L6761:  istore 76 
L6763:  aload_0 
L6764:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L6767:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ô00000' ()Z 
L6770:  ifeq L6782 
L6773:  iload 76 
L6775:  ifeq L6782 
L6778:  iconst_1 
L6779:  goto L6783 
L6782:  iconst_0 
L6783:  istore 77 
L6785:  iload 74 
L6787:  ifne L6843 
L6790:  aload_0 
L6791:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L6794:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ô00000' ()Z 
L6797:  ifne L6843 
L6800:  aload_0 
L6801:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L6804:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ò00000' ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo; 
L6807:  ifnull L6827 
L6810:  aload_0 
L6811:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6814:  aload_0 
L6815:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L6818:  invokevirtual Method [c95] 'Ò00000' ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo; 
L6821:  invokevirtual Method [c542] 'Ó00000' ()Lcom/fs/starfarer/combat/entities/Ship; 
L6824:  if_acmpne L6843 
L6827:  fload 24 
L6829:  fload 33 
L6831:  ldc_w +300.0f 
L6834:  fadd 
L6835:  fcmpl 
L6836:  ifle L6843 
L6839:  iconst_1 
L6840:  goto L6844 
L6843:  iconst_0 
L6844:  istore 78 
L6846:  iload 78 
L6848:  iload 53 
L6850:  ifeq L6857 
L6853:  iconst_0 
L6854:  goto L6858 
L6857:  iconst_1 
L6858:  iand 
L6859:  istore 78 
L6861:  aload_0 
L6862:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI getConfig ()Lcom/fs/starfarer/api/combat/ShipAIConfig; 
L6865:  getfield Field com/fs/starfarer/api/combat/ShipAIConfig backingOffWhileNotVentingAllowed Z 
L6868:  ifne L6887 
L6871:  aload_0 
L6872:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6875:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getFluxTracker ()Lcom/fs/starfarer/combat/entities/ship/O0OO; 
L6878:  invokevirtual Method com/fs/starfarer/combat/entities/ship/O0OO isOverloadedOrVenting ()Z 
L6881:  ifne L6887 
L6884:  iconst_1 
L6885:  istore 73 
L6887:  iconst_0 
L6888:  istore 77 
L6890:  iconst_0 
L6891:  istore 75 
L6893:  fconst_0 
L6894:  fstore 79 
L6896:  iload 75 
L6898:  ifeq L6936 
L6901:  invokestatic Method java/lang/Math random ()D 
L6904:  d2f 
L6905:  ldc_w +0.75f 
L6908:  fcmpl 
L6909:  ifle L6936 
L6912:  invokestatic Method java/lang/Math random ()D 
L6915:  d2f 
L6916:  ldc_w +0.5f 
L6919:  fcmpl 
L6920:  ifle L6931 
L6923:  ldc_w +30.0f 
L6926:  fstore 79 
L6928:  goto L6936 
L6931:  ldc_w -30.0f 
L6934:  fstore 79 
L6936:  aload_0 
L6937:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L6940:  getstatic Field [c482] PHASE_ATTACK_RUN [u486] 
L6943:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L6946:  ifne L6962 
L6949:  aload_0 
L6950:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L6953:  getstatic Field [c482] PHASE_ATTACK_RUN_IN_GOOD_SPOT [u486] 
L6956:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag [u1127] 
L6959:  ifeq L7006 
L6962:  new [c500] 
L6965:  dup 
L6966:  aload_0 
L6967:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L6970:  aload_0 
L6971:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L6974:  aload_0 
L6975:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L6978:  fload 79 
L6980:  fload 25 
L6982:  aload_0 
L6983:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L6986:  aload_0 
L6987:  iload_1 
L6988:  invokespecial Method [c500] <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
L6991:  astore 80 
L6993:  aload_0 
L6994:  aload 80 
L6996:  ldc_w +3.0f 
L6999:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7002:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L7005:  return 
        .catch [0] from L7006 to L7714 using L7714 
L7006:  iload 75 
L7008:  ifeq L7061 
L7011:  iload 77 
L7013:  ifeq L7061 
L7016:  invokestatic Method java/lang/Math random ()D 
L7019:  d2f 
L7020:  ldc_w +0.5f 
L7023:  fcmpl 
L7024:  ifle L7061 
L7027:  new com/fs/starfarer/combat/ai/movement/maneuvers/H 
L7030:  dup 
L7031:  aload_0 
L7032:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7035:  aload_0 
L7036:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7039:  fload 25 
L7041:  fload 47 
L7043:  aload_0 
L7044:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/H <init> [u968] 
L7047:  astore 80 
L7049:  aload_0 
L7050:  aload 80 
L7052:  ldc_w +5.0f 
L7055:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7058:  goto L7722 
L7061:  iload 78 
L7063:  ifeq L7070 
L7066:  invokestatic Method java/lang/Math random ()D 
L7069:  pop2 
L7070:  iload 53 
L7072:  ifeq L7183 
L7075:  aload_0 
L7076:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7079:  getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7082:  if_acmpeq L7183 
L7085:  iload 73 
L7087:  ifne L7140 
L7090:  iload 71 
L7092:  ifne L7106 
L7095:  invokestatic Method java/lang/Math random ()D 
L7098:  d2f 
L7099:  ldc_w +0.5f 
L7102:  fcmpl 
L7103:  ifle L7140 
L7106:  new com/fs/starfarer/combat/ai/movement/maneuvers/J 
L7109:  dup 
L7110:  aload_0 
L7111:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7114:  aload_0 
L7115:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7118:  aload_0 
L7119:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7122:  aload_0 
L7123:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/J <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L7126:  astore 80 
L7128:  aload_0 
L7129:  aload 80 
L7131:  ldc_w +3.0f 
L7134:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7137:  goto L7722 
L7140:  new [c500] 
L7143:  dup 
L7144:  aload_0 
L7145:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7148:  aload_0 
L7149:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7152:  aload_0 
L7153:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L7156:  fload 79 
L7158:  fload 25 
L7160:  aload_0 
L7161:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7164:  aload_0 
L7165:  iload_1 
L7166:  invokespecial Method [c500] <init> [u942] 
L7169:  astore 80 
L7171:  aload_0 
L7172:  aload 80 
L7174:  ldc_w +5.0f 
L7177:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7180:  goto L7722 
L7183:  aload_0 
L7184:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7187:  getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o 'Ò00000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7190:  if_acmpeq L7198 
L7193:  iload 71 
L7195:  ifeq L7392 
L7198:  iload 73 
L7200:  ifne L7349 
L7203:  invokestatic Method java/lang/Math random ()D 
L7206:  d2f 
L7207:  ldc_w +0.5f 
L7210:  fcmpl 
L7211:  ifle L7349 
L7214:  aload_0 
L7215:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7218:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L7221:  ifeq L7240 
L7224:  invokestatic Method java/lang/Math random ()D 
L7227:  d2f 
L7228:  ldc_w +0.5f 
L7231:  fcmpl 
L7232:  ifgt L7240 
L7235:  iload 67 
L7237:  ifeq L7274 
L7240:  new com/fs/starfarer/combat/ai/movement/maneuvers/J 
L7243:  dup 
L7244:  aload_0 
L7245:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7248:  aload_0 
L7249:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7252:  aload_0 
L7253:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7256:  aload_0 
L7257:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/J <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L7260:  astore 80 
L7262:  aload_0 
L7263:  aload 80 
L7265:  ldc_w +3.0f 
L7268:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7271:  goto L7722 
L7274:  new com/fs/starfarer/combat/ai/movement/maneuvers/B 
L7277:  dup 
L7278:  aload_0 
L7279:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7282:  aload_0 
L7283:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7286:  fconst_1 
L7287:  aload_0 
L7288:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7291:  aload_0 
L7292:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/B <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L7295:  astore 80 
L7297:  iload 66 
L7299:  ifeq L7337 
L7302:  new [c500] 
L7305:  dup 
L7306:  aload_0 
L7307:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7310:  aload_0 
L7311:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7314:  aload_0 
L7315:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L7318:  fload 79 
L7320:  fload 25 
L7322:  ldc_w +0.15000000596046448f 
L7325:  fmul 
L7326:  aload_0 
L7327:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7330:  aload_0 
L7331:  iload_1 
L7332:  invokespecial Method [c500] <init> [u942] 
L7335:  astore 80 
L7337:  aload_0 
L7338:  aload 80 
L7340:  ldc_w +3.0f 
L7343:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7346:  goto L7722 
L7349:  new [c500] 
L7352:  dup 
L7353:  aload_0 
L7354:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7357:  aload_0 
L7358:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7361:  aload_0 
L7362:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L7365:  fload 79 
L7367:  fload 25 
L7369:  aload_0 
L7370:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7373:  aload_0 
L7374:  iload_1 
L7375:  invokespecial Method [c500] <init> [u942] 
L7378:  astore 80 
L7380:  aload_0 
L7381:  aload 80 
L7383:  ldc_w +5.0f 
L7386:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7389:  goto L7722 
L7392:  aload_0 
L7393:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7396:  getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7399:  if_acmpne L7668 
L7402:  fload 24 
L7404:  fload 33 
L7406:  fsub 
L7407:  ldc_w +1250.0f 
L7410:  fload 25 
L7412:  fadd 
L7413:  fcmpl 
L7414:  ifle L7421 
L7417:  iconst_1 
L7418:  goto L7422 
L7421:  iconst_0 
L7422:  istore 80 
L7424:  fload 24 
L7426:  fload 33 
L7428:  fsub 
L7429:  ldc_w +250.0f 
L7432:  fcmpg 
L7433:  ifge L7440 
L7436:  iconst_1 
L7437:  goto L7441 
L7440:  iconst_0 
L7441:  istore 81 
L7443:  iload 67 
L7445:  ifne L7489 
L7448:  invokestatic Method java/lang/Math random ()D 
L7451:  d2f 
L7452:  ldc_w +0.33000001311302185f 
L7455:  fcmpl 
L7456:  ifgt L7484 
L7459:  iload 74 
L7461:  ifeq L7484 
L7464:  iload 81 
L7466:  ifne L7484 
L7469:  iload 48 
L7471:  ifne L7484 
L7474:  aload_0 
L7475:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7478:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L7481:  ifeq L7532 
L7484:  iload 80 
L7486:  ifne L7532 
L7489:  new [c500] 
L7492:  dup 
L7493:  aload_0 
L7494:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7497:  aload_0 
L7498:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7501:  aload_0 
L7502:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L7505:  fload 79 
L7507:  fload 25 
L7509:  aload_0 
L7510:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7513:  aload_0 
L7514:  iload_1 
L7515:  invokespecial Method [c500] <init> [u942] 
L7518:  astore 82 
L7520:  aload_0 
L7521:  aload 82 
L7523:  ldc_w +8.0f 
L7526:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7529:  goto L7722 
L7532:  iload 80 
L7534:  ifeq L7625 
L7537:  aload_0 
L7538:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7541:  invokevirtual Method com/fs/starfarer/combat/entities/Ship isCapital ()Z 
L7544:  ifeq L7625 
L7547:  iload 9 
L7549:  ifne L7625 
L7552:  new com/fs/starfarer/combat/ai/movement/maneuvers/B 
L7555:  dup 
L7556:  aload_0 
L7557:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7560:  aload_0 
L7561:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7564:  fconst_1 
L7565:  aload_0 
L7566:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7569:  aload_0 
L7570:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/B <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L7573:  astore 82 
L7575:  iload 66 
L7577:  ifeq L7615 
L7580:  new [c500] 
L7583:  dup 
L7584:  aload_0 
L7585:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7588:  aload_0 
L7589:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7592:  aload_0 
L7593:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L7596:  fload 79 
L7598:  fload 25 
L7600:  ldc_w +0.15000000596046448f 
L7603:  fmul 
L7604:  aload_0 
L7605:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7608:  aload_0 
L7609:  iload_1 
L7610:  invokespecial Method [c500] <init> [u942] 
L7613:  astore 82 
L7615:  aload_0 
L7616:  aload 82 
L7618:  fconst_2 
L7619:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7622:  goto L7722 
L7625:  new [c500] 
L7628:  dup 
L7629:  aload_0 
L7630:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7633:  aload_0 
L7634:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7637:  aload_0 
L7638:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI badTarget Z 
L7641:  fload 79 
L7643:  fload 25 
L7645:  aload_0 
L7646:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7649:  aload_0 
L7650:  iload_1 
L7651:  invokespecial Method [c500] <init> [u942] 
L7654:  astore 82 
L7656:  aload_0 
L7657:  aload 82 
L7659:  ldc_w +5.0f 
L7662:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7665:  goto L7722 
L7668:  aload_0 
L7669:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackMode Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7672:  getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o 'Ó00000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o; 
L7675:  if_acmpne L7722 
L7678:  new com/fs/starfarer/combat/ai/movement/maneuvers/S 
L7681:  dup 
L7682:  aload_0 
L7683:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L7686:  aload_0 
L7687:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI target Lcom/fs/starfarer/combat/entities/Ship; 
L7690:  fload 25 
L7692:  aload_0 
L7693:  getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI flockingAI Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L7696:  aload_0 
L7697:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/S <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;FLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L7700:  astore 80 
L7702:  aload_0 
L7703:  aload 80 
L7705:  ldc_w +5.0f 
L7708:  invokevirtual Method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI setManeuver [u790] 
L7711:  goto L7722 
L7714:  astore 83 
L7716:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L7719:  aload 83 
L7721:  athrow 
L7722:  invokestatic Method com/fs/profiler/Profiler o00000 ()V 
L7725:  return 
L7726:  
    .end code 
.end method 

.method public getAttackingGroup : ()Lcom/fs/starfarer/combat/ai/attack/super; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4:     invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule Oo0000 ()Lcom/fs/starfarer/combat/ai/attack/super; 
L7:     areturn 
L8:     
    .end code 
.end method 

.method public getEvaluationFor : (Lcom/fs/starfarer/combat/systems/G;)Lcom/fs/starfarer/combat/ai/attack/OoOO; 
    .code stack 2 locals 2 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4:     aload_1 
L5:     invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'super' (Lcom/fs/starfarer/combat/systems/G;)Lcom/fs/starfarer/combat/ai/attack/OoOO; 
L8:     areturn 
L9:     
    .end code 
.end method 

.method public doesShipVelocityMatterForAim : ()Z 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4:     invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'super' ()Z 
L7:     ireturn 
L8:     
    .end code 
.end method 

.method public getCollisionRadius : ()F 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L7:     freturn 
L8:     
    .end code 
.end method 

.method public getLocation : ()Lorg/lwjgl/util/vector/Vector2f; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI ship Lcom/fs/starfarer/combat/entities/Ship; 
L4:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L7:     areturn 
L8:     
    .end code 
.end method 

.method public isFighter : ()Z 
    .code stack 1 locals 1 
L0:     iconst_0 
L1:     ireturn 
L2:     
    .end code 
.end method 

.method public setMaxTargetRange : (F)V 
    .code stack 2 locals 2 
L0:     aload_0 
L1:     fload_1 
L2:     putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI maxAttackRange F 
L5:     return 
L6:     
    .end code 
.end method 

.method public canObeyOrders : ()Z 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L4:     invokevirtual Method com/genir/aitweaks/asm/combat/ai/OrderResponseModule new ()Z 
L7:     ifeq L14 
L10:    iconst_0 
L11:    goto L15 
L14:    iconst_1 
L15:    ireturn 
L16:    
    .end code 
.end method 

.method public getThreatEvaluator : ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI threatEvalAI [u719] 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public notifyNewOrderGivenToBattleGroup : (Lcom/fs/starfarer/combat/P;)V 
    .code stack 0 locals 2 
L0:     return 
L1:     
    .end code 
.end method 

.method public getOptimalNonMissileRange : ()F 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4:     invokevirtual Method com/fs/starfarer/combat/ai/attack/AttackAIModule 'õ00000' ()F 
L7:     freturn 
L8:     
    .end code 
.end method 

.method public getAttackAI : ()Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI attackAI [u27] 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public keepTarget : ()Z 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI forceTarget Z 
L4:     ireturn 
L5:     
    .end code 
.end method 

.method public getTargetOverride : ()Lcom/fs/starfarer/combat/entities/Ship; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public setTargetOverride : (Lcom/fs/starfarer/combat/entities/Ship;)V 
    .code stack 2 locals 2 
L0:     aload_0 
L1:     aload_1 
L2:     putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI targetOverride Lcom/fs/starfarer/combat/entities/Ship; 
L5:     return 
L6:     
    .end code 
.end method 

.method public getShieldAI : ()Lcom/fs/starfarer/combat/ai/G; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI shieldAI Lcom/fs/starfarer/combat/ai/G; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public getEval : ()Lcom/fs/starfarer/combat/ai/C; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI eval Lcom/fs/starfarer/combat/ai/C; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method public setDoNotFireDelay : (F)V 
    .code stack 0 locals 2 
L0:     return 
L1:     
    .end code 
.end method 

.method public needsRefit : ()Z 
    .code stack 1 locals 1 
L0:     iconst_0 
L1:     ireturn 
L2:     
    .end code 
.end method 

.method public getOrderResponseModule : ()Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI orderResponseModule Lcom/genir/aitweaks/asm/combat/ai/OrderResponseModule; 
L4:     areturn 
L5:     
    .end code 
.end method 

.method static synthetic $SWITCH_TABLE$com$fs$starfarer$api$combat$ShipAPI$HullSize : ()[I 
    .code stack 3 locals 1 
L0:     getstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI $SWITCH_TABLE$com$fs$starfarer$api$combat$ShipAPI$HullSize [I 
L3:     dup 
L4:     ifnull L8 
L7:     areturn 
L8:     pop 
L9:     invokestatic Method com/fs/starfarer/api/combat/ShipAPI$HullSize values ()[Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L12:    arraylength 
L13:    newarray int 
L15:    astore_0 
        .catch java/lang/NoSuchFieldError from L16 to L26 using L29 
L16:    aload_0 
L17:    getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize CAPITAL_SHIP Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L20:    invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L23:    bipush 6 
L25:    iastore 
L26:    goto L30 
L29:    pop 
        .catch java/lang/NoSuchFieldError from L30 to L39 using L42 
L30:    aload_0 
L31:    getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize CRUISER Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L34:    invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L37:    iconst_5 
L38:    iastore 
L39:    goto L43 
L42:    pop 
        .catch java/lang/NoSuchFieldError from L43 to L52 using L55 
L43:    aload_0 
L44:    getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize DEFAULT Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L47:    invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L50:    iconst_1 
L51:    iastore 
L52:    goto L56 
L55:    pop 
        .catch java/lang/NoSuchFieldError from L56 to L65 using L68 
L56:    aload_0 
L57:    getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize DESTROYER Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L60:    invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L63:    iconst_4 
L64:    iastore 
L65:    goto L69 
L68:    pop 
        .catch java/lang/NoSuchFieldError from L69 to L78 using L81 
L69:    aload_0 
L70:    getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize FIGHTER Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L73:    invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L76:    iconst_2 
L77:    iastore 
L78:    goto L82 
L81:    pop 
        .catch java/lang/NoSuchFieldError from L82 to L91 using L94 
L82:    aload_0 
L83:    getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize FRIGATE Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L86:    invokevirtual Method com/fs/starfarer/api/combat/ShipAPI$HullSize ordinal ()I 
L89:    iconst_3 
L90:    iastore 
L91:    goto L95 
L94:    pop 
L95:    aload_0 
L96:    dup 
L97:    putstatic Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI $SWITCH_TABLE$com$fs$starfarer$api$combat$ShipAPI$HullSize [I 
L100:   areturn 
L101:   
    .end code 
.end method 
.innerclasses 
    com/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo com/fs/starfarer/api/combat/CombatFleetManagerAPI AssignmentInfo public static interface abstract 
    com/fs/starfarer/api/combat/ShieldAPI$ShieldType com/fs/starfarer/api/combat/ShieldAPI ShieldType public static final enum 
    com/fs/starfarer/api/combat/ShipAPI$HullSize com/fs/starfarer/api/combat/ShipAPI HullSize public static final enum 
    com/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints com/fs/starfarer/api/combat/ShipHullSpecAPI ShipTypeHints public static final enum 
    [c482] com/fs/starfarer/api/combat/ShipwideAIFlags AIFlags public static final enum 
    com/fs/starfarer/combat/CombatFleetManager$O0 com/fs/starfarer/combat/CombatFleetManager O0 public static 
    com/fs/starfarer/combat/ai/N$oo com/fs/starfarer/combat/ai/N oo public static final enum 
    com/fs/starfarer/combat/ai/N$O0 com/fs/starfarer/combat/ai/N O0 public static 
    com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 [0] [0] 
    com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$o com/genir/aitweaks/asm/combat/ai/AssemblyShipAI o private static final enum 
    com/genir/aitweaks/asm/combat/ai/OrderResponseModule$o com/genir/aitweaks/asm/combat/ai/OrderResponseModule o public 
    [c542] [c95] Oo public static 
    com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$o [c95] o public static 
    com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o com/fs/starfarer/combat/ai/movement/maneuvers/oO0O o public static interface abstract 
    com/fs/starfarer/combat/entities/Ship$Oo com/fs/starfarer/combat/entities/Ship Oo public static 
    com/fs/starfarer/combat/entities/Ship$oo com/fs/starfarer/combat/entities/Ship oo public static final enum 
    com/fs/starfarer/combat/entities/Ship$ShipAIWrapper com/fs/starfarer/combat/entities/Ship ShipAIWrapper public static 
    com/fs/starfarer/combat/tasks/CombatTask$Oo com/fs/starfarer/combat/tasks/CombatTask Oo public static interface abstract 
    com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType com/fs/starfarer/combat/tasks/CombatTask CombatTaskType public static final enum 
    com/fs/starfarer/combat/tasks/C$o com/fs/starfarer/combat/tasks/C o public static final enum 
.end innerclasses 
.const [u27] = Utf8 Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
.const [u41] = Utf8 (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Lcom/fs/starfarer/api/combat/ShipwideAIFlags;)V 
.const [c95] = Class [u96] 
.const [c152] = Class [u153] 
.const [u227] = Utf8 (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipwideAIFlags;Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)Lcom/fs/starfarer/combat/ai/system/M; 
.const [c482] = Class [u483] 
.const [u486] = Utf8 Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
.const [c500] = Class [u501] 
.const [u518] = Utf8 (FLcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;Lorg/lwjgl/util/vector/Vector2f;)V 
.const [u527] = Utf8 (FLcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/entities/Ship;)V 
.const [u528] = Utf8 (FLcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Z)V 
.const [c542] = Class [u543] 
.const [u719] = Utf8 Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 
.const [u722] = Utf8 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oooO; 
.const [u767] = Utf8 (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
.const [u790] = Utf8 (Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O;F)V 
.const [u908] = Utf8 (Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)F 
.const [u921] = Utf8 (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo; 
.const [u942] = Utf8 (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;ZFFLcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;Z)V 
.const [u944] = Utf8 (Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
.const [u968] = Utf8 (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;FFLcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
.const [u1127] = Utf8 (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
.const [u96] = Utf8 com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 
.const [u153] = Utf8 com/fs/starfarer/combat/ai/movement/maneuvers/oooO 
.const [u483] = Utf8 com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags 
.const [u501] = Utf8 com/genir/aitweaks/features/maneuver/ManeuverObf 
.const [u543] = Utf8 com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo 
.end class 
