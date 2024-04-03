.version 49 0 
.class public super com/genir/aitweaks/asm/OrderResponseModule 
.super java/lang/Object 
.field public static final 'ÓO0000' F = +1750.0f 
.field public static final OO0000 F = +1000.0f 
.field private 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
.field private 'Ó00000' Lcom/fs/starfarer/combat/ai/movement/oOOO; 
.field private oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
.field private 'Ø00000' Z 
.field private while Z 
.field private float Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
.field private o00000 Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
.field private 'Ö00000' Lcom/fs/starfarer/combat/CombatFleetManager; 
.field private final null Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
.field private class Z 
.field private 'ö00000' Z 
.field private static synthetic new [I 
.field private static synthetic 'õ00000' [I 

.method public <init> : (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
    .code stack 3 locals 5 
L0:     aload_0 
L1:     invokespecial Method java/lang/Object <init> ()V 
L4:     aload_0 
L5:     iconst_0 
L6:     putfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ø00000' Z 
L9:     aload_0 
L10:    iconst_0 
L11:    putfield Field com/genir/aitweaks/asm/OrderResponseModule while Z 
L14:    aload_0 
L15:    iconst_0 
L16:    putfield Field com/genir/aitweaks/asm/OrderResponseModule class Z 
L19:    aload_0 
L20:    iconst_0 
L21:    putfield Field com/genir/aitweaks/asm/OrderResponseModule 'ö00000' Z 
L24:    aload_0 
L25:    aload_1 
L26:    putfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L29:    aload_0 
L30:    aload_2 
L31:    putfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ó00000' Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L34:    aload_0 
L35:    aload_3 
L36:    putfield Field com/genir/aitweaks/asm/OrderResponseModule null Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L39:    aload_0 
L40:    aload 4 
L42:    putfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L45:    aload_0 
L46:    invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L49:    aload_1 
L50:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L53:    invokevirtual Method com/fs/starfarer/combat/CombatEngine getFleetManager (I)Lcom/fs/starfarer/combat/CombatFleetManager; 
L56:    putfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ö00000' Lcom/fs/starfarer/combat/CombatFleetManager; 
L59:    aload_0 
L60:    aload_0 
L61:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ö00000' Lcom/fs/starfarer/combat/CombatFleetManager; 
L64:    aload_1 
L65:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isAlly ()Z 
L68:    invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getTaskManager (Z)Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L71:    putfield Field com/genir/aitweaks/asm/OrderResponseModule o00000 Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L74:    aload_0 
L75:    invokespecial Method com/genir/aitweaks/asm/OrderResponseModule 'Ø00000' ()V 
L78:    return 
L79:    
    .end code 
.end method 

.method private class : ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     invokespecial Method com/genir/aitweaks/asm/OrderResponseModule 'Ø00000' ()V 
L4:     aload_0 
L5:     getfield Field com/genir/aitweaks/asm/OrderResponseModule float Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L8:     areturn 
L9:     
    .end code 
.end method 

.method private 'Ø00000' : ()V 
    .code stack 3 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ö00000' Lcom/fs/starfarer/combat/CombatFleetManager; 
L4:     ifnull L50 
L7:     aload_0 
L8:     getfield Field com/genir/aitweaks/asm/OrderResponseModule float Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L11:    ifnonnull L50 
L14:    aload_0 
L15:    aload_0 
L16:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ö00000' Lcom/fs/starfarer/combat/CombatFleetManager; 
L19:    aload_0 
L20:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L23:    invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getDeployedFleetMember (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L26:    putfield Field com/genir/aitweaks/asm/OrderResponseModule float Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L29:    aload_0 
L30:    getfield Field com/genir/aitweaks/asm/OrderResponseModule float Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L33:    ifnull L50 
L36:    aload_0 
L37:    aload_0 
L38:    getfield Field com/genir/aitweaks/asm/OrderResponseModule float Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L41:    invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getMember ()Lcom/fs/starfarer/campaign/fleet/FleetMember; 
L44:    invokevirtual Method com/fs/starfarer/campaign/fleet/FleetMember isCarrier ()Z 
L47:    putfield Field com/genir/aitweaks/asm/OrderResponseModule while Z 
L50:    return 
L51:    
    .end code 
.end method 

.method public new : ()Z 
    .code stack 2 locals 2 
L0:     aload_0 
L1:     invokevirtual Method com/genir/aitweaks/asm/OrderResponseModule 'Ó00000' ()Lcom/fs/starfarer/combat/tasks/C; 
L4:     astore_1 
L5:     aload_1 
L6:     invokevirtual Method com/fs/starfarer/combat/tasks/C o00000 ()Lcom/fs/starfarer/combat/tasks/C$o; 
L9:     getstatic Field com/fs/starfarer/combat/tasks/C$o 'Ô00000' Lcom/fs/starfarer/combat/tasks/C$o; 
L12:    if_acmpne L17 
L15:    iconst_1 
L16:    ireturn 
L17:    iconst_0 
L18:    ireturn 
L19:    
    .end code 
.end method 

.method private new : (Lcom/fs/starfarer/combat/entities/Ship;)Lcom/fs/starfarer/combat/entities/Ship; 
    .code stack 2 locals 8 
L0:     aload_1 
L1:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L4:     ifnonnull L9 
L7:     aload_1 
L8:     areturn 
L9:     aload_1 
L10:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L13:    invokevirtual Method com/fs/starfarer/combat/ai/L getMembers ()Ljava/util/List; 
L16:    astore_2 
L17:    ldc +3.4028234663852886e+38f 
L19:    fstore_3 
L20:    aconst_null 
L21:    astore 4 
L23:    aload_2 
L24:    invokeinterface InterfaceMethod java/util/List iterator ()Ljava/util/Iterator; 1 
L29:    astore 6 
L31:    goto L85 
L34:    aload 6 
L36:    invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L41:    checkcast com/fs/starfarer/combat/entities/Ship 
L44:    astore 5 
L46:    aload 5 
L48:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L51:    ifeq L57 
L54:    goto L85 
L57:    aload_1 
L58:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L61:    aload 5 
L63:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L66:    invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L69:    fstore 7 
L71:    fload 7 
L73:    fload_3 
L74:    fcmpg 
L75:    ifge L85 
L78:    aload 5 
L80:    astore 4 
L82:    fload 7 
L84:    fstore_3 
L85:    aload 6 
L87:    invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L92:    ifne L34 
L95:    aload 4 
L97:    areturn 
L98:    
    .end code 
.end method 

.method public 'Ó00000' : ()Lcom/fs/starfarer/combat/tasks/C; 
    .code stack 2 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/OrderResponseModule o00000 Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L4:     aload_0 
L5:     invokespecial Method com/genir/aitweaks/asm/OrderResponseModule class ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L8:     invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getOrdersForFleetMember (Lcom/fs/starfarer/combat/CombatFleetManager$O0;)Lcom/fs/starfarer/combat/tasks/C; 
L11:    areturn 
L12:    
    .end code 
.end method 

.method public 'Ö00000' : ()Z 
    .code stack 2 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/OrderResponseModule o00000 Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L4:     aload_0 
L5:     invokespecial Method com/genir/aitweaks/asm/OrderResponseModule class ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L8:     invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getOrdersForFleetMember (Lcom/fs/starfarer/combat/CombatFleetManager$O0;)Lcom/fs/starfarer/combat/tasks/C; 
L11:    invokevirtual Method com/fs/starfarer/combat/tasks/C o00000 ()Lcom/fs/starfarer/combat/tasks/C$o; 
L14:    getstatic Field com/fs/starfarer/combat/tasks/C$o new Lcom/fs/starfarer/combat/tasks/C$o; 
L17:    if_acmpeq L22 
L20:    iconst_1 
L21:    ireturn 
L22:    iconst_0 
L23:    ireturn 
L24:    
    .end code 
.end method 

.method public 'ö00000' : ()Lcom/genir/aitweaks/asm/OrderResponseModule$o; 
    .code stack 7 locals 6 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L4:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L7:     getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags ESCORT_OTHER_SHIP Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L10:    invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L13:    ifeq L163 
L16:    aload_0 
L17:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L20:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isRetreating ()Z 
L23:    ifne L163 
L26:    aload_0 
L27:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L30:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L33:    getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags ESCORT_OTHER_SHIP Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L36:    invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags getCustom (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Ljava/lang/Object; 
L39:    astore_1 
L40:    aload_1 
L41:    instanceof com/fs/starfarer/combat/entities/Ship 
L44:    ifeq L163 
L47:    aload_1 
L48:    checkcast com/fs/starfarer/combat/entities/Ship 
L51:    astore_2 
L52:    aload_0 
L53:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L56:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L59:    aload_2 
L60:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L63:    invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L66:    fstore_3 
L67:    new com/genir/aitweaks/asm/OrderResponseModule$o 
L70:    dup 
L71:    aload_0 
L72:    invokespecial Method com/genir/aitweaks/asm/OrderResponseModule$o <init> (Lcom/genir/aitweaks/asm/OrderResponseModule;)V 
L75:    astore 4 
L77:    fload_3 
L78:    ldc +2000.0f 
L80:    fcmpg 
L81:    ifge L126 
L84:    new com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 
L87:    dup 
L88:    aload_0 
L89:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L92:    aload_2 
L93:    aload_0 
L94:    getfield Field com/genir/aitweaks/asm/OrderResponseModule null Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L97:    aload_0 
L98:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ó00000' Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L101:   aload_0 
L102:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L105:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L108:   astore 5 
L110:   aload 4 
L112:   aload 5 
L114:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L117:   aload 4 
L119:   fconst_2 
L120:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L123:   aload 4 
L125:   areturn 
L126:   new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L129:   dup 
L130:   aload_0 
L131:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L134:   aload_2 
L135:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L138:   aload_0 
L139:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L142:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L145:   astore 5 
L147:   aload 4 
L149:   aload 5 
L151:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L154:   aload 4 
L156:   fconst_2 
L157:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L160:   aload 4 
L162:   areturn 
L163:   aload_0 
L164:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L167:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L170:   getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags CAMP_LOCATION Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L173:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L176:   ifeq L256 
L179:   aload_0 
L180:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L183:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isRetreating ()Z 
L186:   ifne L256 
L189:   aload_0 
L190:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L193:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L196:   getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags CAMP_LOCATION Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L199:   invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags getCustom (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Ljava/lang/Object; 
L202:   astore_1 
L203:   aload_1 
L204:   instanceof org/lwjgl/util/vector/Vector2f 
L207:   ifeq L256 
L210:   aload_1 
L211:   checkcast org/lwjgl/util/vector/Vector2f 
L214:   astore_2 
L215:   new com/genir/aitweaks/asm/OrderResponseModule$o 
L218:   dup 
L219:   aload_0 
L220:   invokespecial Method com/genir/aitweaks/asm/OrderResponseModule$o <init> (Lcom/genir/aitweaks/asm/OrderResponseModule;)V 
L223:   astore_3 
L224:   new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L227:   dup 
L228:   aload_0 
L229:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L232:   aload_2 
L233:   aload_0 
L234:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L237:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L240:   astore 4 
L242:   aload_3 
L243:   aload 4 
L245:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L248:   aload_3 
L249:   ldc +3.0f 
L251:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L254:   aload_3 
L255:   areturn 
L256:   new com/genir/aitweaks/asm/OrderResponseModule$o 
L259:   dup 
L260:   aload_0 
L261:   invokespecial Method com/genir/aitweaks/asm/OrderResponseModule$o <init> (Lcom/genir/aitweaks/asm/OrderResponseModule;)V 
L264:   astore_1 
L265:   aload_0 
L266:   invokevirtual Method com/genir/aitweaks/asm/OrderResponseModule 'Ó00000' ()Lcom/fs/starfarer/combat/tasks/C; 
L269:   astore_2 
L270:   aload_0 
L271:   iconst_0 
L272:   putfield Field com/genir/aitweaks/asm/OrderResponseModule class Z 
L275:   aload_0 
L276:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L279:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L282:   ifnull L389 
L285:   aload_0 
L286:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L289:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L292:   invokevirtual Method com/fs/starfarer/combat/ai/L getSourceShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L295:   ifnull L389 
L298:   aload_0 
L299:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L302:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L305:   invokevirtual Method com/fs/starfarer/combat/ai/L isCarrierAlive ()Z 
L308:   ifeq L389 
L311:   aload_0 
L312:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L315:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L318:   invokevirtual Method com/fs/starfarer/combat/ai/L getSpec ()Lcom/fs/starfarer/loading/specs/FighterWingSpec; 
L321:   invokevirtual Method com/fs/starfarer/loading/specs/FighterWingSpec isAlwaysEscort ()Z 
L324:   ifne L343 
L327:   aload_0 
L328:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L331:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L334:   invokevirtual Method com/fs/starfarer/combat/ai/L getSourceShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L337:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isPullBackFighters ()Z 
L340:   ifeq L389 
L343:   new com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 
L346:   dup 
L347:   aload_0 
L348:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L351:   aload_0 
L352:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L355:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L358:   invokevirtual Method com/fs/starfarer/combat/ai/L getSourceShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L361:   aload_0 
L362:   getfield Field com/genir/aitweaks/asm/OrderResponseModule null Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L365:   aload_0 
L366:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ó00000' Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L369:   aload_0 
L370:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L373:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L376:   astore_3 
L377:   aload_1 
L378:   aload_3 
L379:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L382:   aload_1 
L383:   fconst_2 
L384:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L387:   aload_1 
L388:   areturn 
L389:   aload_0 
L390:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L393:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFighter ()Z 
L396:   ifeq L411 
L399:   aload_0 
L400:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L403:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L406:   ifnull L411 
L409:   aconst_null 
L410:   areturn 
L411:   invokestatic Method com/genir/aitweaks/asm/OrderResponseModule 'õ00000' ()[I 
L414:   aload_2 
L415:   invokevirtual Method com/fs/starfarer/combat/tasks/C o00000 ()Lcom/fs/starfarer/combat/tasks/C$o; 
L418:   invokevirtual Method com/fs/starfarer/combat/tasks/C$o ordinal ()I 
L421:   iaload 
L422:   tableswitch 1 
            L461 
            L467 
            L513 
            L456 
            L456 
            default : L513 

L456:   aload_0 
L457:   invokevirtual Method com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' ()Lcom/genir/aitweaks/asm/OrderResponseModule$o; 
L460:   areturn 
L461:   aload_0 
L462:   aload_2 
L463:   invokespecial Method com/genir/aitweaks/asm/OrderResponseModule o00000 (Lcom/fs/starfarer/combat/tasks/C;)Lcom/genir/aitweaks/asm/OrderResponseModule$o; 
L466:   areturn 
L467:   new com/fs/starfarer/combat/ai/movement/maneuvers/RetreatManeuver 
L470:   dup 
L471:   aload_0 
L472:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L475:   aload_0 
L476:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ó00000' Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L479:   aload_0 
L480:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L483:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/RetreatManeuver <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L486:   astore_3 
L487:   aload_1 
L488:   aload_3 
L489:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L492:   aload_1 
L493:   ldc +3.0f 
L495:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L498:   aload_0 
L499:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L502:   iconst_1 
L503:   aload_0 
L504:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L507:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isDirectRetreat ()Z 
L510:   invokevirtual Method com/fs/starfarer/combat/entities/Ship setRetreating (ZZ)V 
L513:   aload_1 
L514:   areturn 
L515:   
    .end code 
.end method 

.method protected 'Ô00000' : ()Lcom/genir/aitweaks/asm/OrderResponseModule$o; 
    .code stack 1 locals 1 
L0:     aconst_null 
L1:     areturn 
L2:     
    .end code 
.end method 

.method public o00000 : ()Z 
    .code stack 1 locals 1 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/OrderResponseModule class Z 
L4:     ireturn 
L5:     
    .end code 
.end method 

.method public o00000 : (F)V 
    .code stack 0 locals 2 
L0:     return 
L1:     
    .end code 
.end method 

.method private o00000 : (Lorg/lwjgl/util/vector/Vector2f;)V 
    .code stack 4 locals 2 
L0:     aload_1 
L1:     ifnull L23 
L4:     aload_0 
L5:     getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L8:     invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L11:    getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MOVEMENT_DEST Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L14:    ldc +3.0f 
L16:    aload_1 
L17:    invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;FLjava/lang/Object;)V 
L20:    goto L36 
L23:    aload_0 
L24:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L27:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L30:    getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MOVEMENT_DEST Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L33:    invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags unsetFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)V 
L36:    return 
L37:    
    .end code 
.end method 

.method private o00000 : (Lcom/fs/starfarer/combat/tasks/CombatTask;)V 
    .code stack 2 locals 4 
L0:     aload_1 
L1:     ifnonnull L5 
L4:     return 
L5:     aload_1 
L6:     invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L9:     getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType AVOID Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L12:    if_acmpeq L34 
L15:    aload_1 
L16:    invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L19:    ifnull L34 
L22:    aload_1 
L23:    invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L26:    invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L31:    ifnonnull L40 
L34:    aload_0 
L35:    aconst_null 
L36:    invokespecial Method com/genir/aitweaks/asm/OrderResponseModule o00000 (Lorg/lwjgl/util/vector/Vector2f;)V 
L39:    return 
L40:    aload_1 
L41:    invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L44:    invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L49:    astore_2 
L50:    aload_2 
L51:    aload_0 
L52:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L55:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L58:    invokestatic Method com/fs/starfarer/api/util/Misc getDistance (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L61:    aload_0 
L62:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L65:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L68:    fsub 
L69:    fstore_3 
L70:    fload_3 
L71:    ldc +2000.0f 
L73:    fcmpg 
L74:    ifge L83 
L77:    aload_0 
L78:    aconst_null 
L79:    invokespecial Method com/genir/aitweaks/asm/OrderResponseModule o00000 (Lorg/lwjgl/util/vector/Vector2f;)V 
L82:    return 
L83:    aload_0 
L84:    aload_2 
L85:    invokespecial Method com/genir/aitweaks/asm/OrderResponseModule o00000 (Lorg/lwjgl/util/vector/Vector2f;)V 
L88:    return 
L89:    
    .end code 
.end method 

.method private o00000 : (Lcom/fs/starfarer/combat/tasks/C;)Lcom/genir/aitweaks/asm/OrderResponseModule$o; 
    .code stack 7 locals 27 
L0:     aload_1 
L1:     ifnull L12 
L4:     aload_0 
L5:     aload_1 
L6:     invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L9:     invokespecial Method com/genir/aitweaks/asm/OrderResponseModule o00000 (Lcom/fs/starfarer/combat/tasks/CombatTask;)V 
L12:    new com/genir/aitweaks/asm/OrderResponseModule$o 
L15:    dup 
L16:    aload_0 
L17:    invokespecial Method com/genir/aitweaks/asm/OrderResponseModule$o <init> (Lcom/genir/aitweaks/asm/OrderResponseModule;)V 
L20:    astore_2 
L21:    aload_0 
L22:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L25:    invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L28:    aload_1 
L29:    invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L32:    invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L35:    invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L40:    invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L43:    fstore_3 
L44:    aload_1 
L45:    invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L48:    invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L51:    getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType LIGHT_ESCORT Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L54:    if_acmpeq L83 
L57:    aload_1 
L58:    invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L61:    invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L64:    getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType DESTROYER_ESCORT Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L67:    if_acmpeq L83 
L70:    aload_1 
L71:    invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L74:    invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L77:    getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType FULL_ESCORT Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L80:    if_acmpne L183 
L83:    fload_3 
L84:    ldc +2000.0f 
L86:    fcmpg 
L87:    ifge L141 
L90:    new com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 
L93:    dup 
L94:    aload_0 
L95:    getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L98:    aload_1 
L99:    invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L102:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L105:   checkcast com/fs/starfarer/combat/CombatFleetManager$O0 
L108:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L111:   aload_0 
L112:   getfield Field com/genir/aitweaks/asm/OrderResponseModule null Lcom/fs/starfarer/combat/ai/attack/AttackAIModule; 
L115:   aload_0 
L116:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ó00000' Lcom/fs/starfarer/combat/ai/movement/oOOO; 
L119:   aload_0 
L120:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L123:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/EscortTargetManeuverV3 <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/ai/attack/AttackAIModule;Lcom/fs/starfarer/combat/ai/movement/oOOO;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L126:   astore 4 
L128:   aload_2 
L129:   aload 4 
L131:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L134:   aload_2 
L135:   fconst_2 
L136:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L139:   aload_2 
L140:   areturn 
L141:   new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L144:   dup 
L145:   aload_0 
L146:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L149:   aload_1 
L150:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L153:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L156:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L161:   aload_0 
L162:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L165:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L168:   astore 4 
L170:   aload_2 
L171:   aload 4 
L173:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L176:   aload_2 
L177:   fconst_2 
L178:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L181:   aload_2 
L182:   areturn 
L183:   aload_1 
L184:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L187:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L190:   getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType INTERCEPT Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L193:   if_acmpne L246 
L196:   fload_3 
L197:   ldc_w +2700.0f 
L200:   fcmpl 
L201:   ifle L246 
L204:   new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L207:   dup 
L208:   aload_0 
L209:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L212:   aload_1 
L213:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L216:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L219:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L224:   aload_0 
L225:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
L228:   invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L231:   astore 4 
L233:   aload_2 
L234:   aload 4 
L236:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L239:   aload_2 
L240:   fconst_2 
L241:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L244:   aload_2 
L245:   areturn 
L246:   fload_3 
L247:   ldc_w +1500.0f 
L250:   fcmpl 
L251:   ifle L259 
L254:   aload_0 
L255:   iconst_0 
L256:   putfield Field com/genir/aitweaks/asm/OrderResponseModule 'ö00000' Z 
L259:   aload_1 
L260:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L263:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L266:   getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType CIVILIAN_CRAFT Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L269:   if_acmpne L276 
L272:   iconst_1 
L273:   goto L277 
L276:   iconst_0 
L277:   istore 4 
L279:   iconst_1 
L280:   istore 5 
L282:   aload_1 
L283:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L286:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'public' ()Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L289:   astore 6 
L291:   iconst_0 
L292:   istore 5 
L294:   aload_0 
L295:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L298:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSize ()Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L301:   astore 7 
L303:   aload_0 
L304:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L307:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isFrigate ()Z 
L310:   ifne L318 
L313:   getstatic Field com/fs/starfarer/api/combat/ShipAPI$HullSize DESTROYER Lcom/fs/starfarer/api/combat/ShipAPI$HullSize; 
L316:   astore 7 
L318:   ldc_w +210.0f 
L321:   aload_0 
L322:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L325:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getMaxSpeed ()F 
L328:   ldc_w +0.5f 
L331:   fmul 
L332:   ldc_w +100.0f 
L335:   invokestatic Method java/lang/Math min (FF)F 
L338:   fsub 
L339:   fstore 8 
L341:   aload_0 
L342:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L345:   aload_1 
L346:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L349:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L352:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L357:   fload 8 
L359:   ldc_w +1500.0f 
L362:   aload 7 
L364:   aconst_null 
L365:   invokestatic Method com/fs/starfarer/combat/ai/N 'super' [u125] 
L368:   istore 9 
L370:   aload_1 
L371:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L374:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L377:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L382:   astore 10 
L384:   aconst_null 
L385:   astore 11 
L387:   aload_0 
L388:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L391:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L394:   aload 10 
L396:   iconst_0 
L397:   invokestatic Method com/fs/starfarer/combat/ai/N 'super' (ILorg/lwjgl/util/vector/Vector2f;Z)Lcom/fs/starfarer/combat/entities/Ship; 
L400:   astore 11 
L402:   invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L405:   aload_0 
L406:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L409:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOriginalOwner ()I 
L412:   invokevirtual Method com/fs/starfarer/combat/CombatEngine getFleetManager (I)Lcom/fs/starfarer/combat/CombatFleetManager; 
L415:   aload 10 
L417:   iconst_0 
L418:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager findClosestToLocation (Lorg/lwjgl/util/vector/Vector2f;Z)Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L421:   astore 12 
L423:   ldc_w +10000000000.0f 
L426:   fstore 13 
L428:   aload 12 
L430:   ifnull L457 
L433:   aload 12 
L435:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L438:   aload_0 
L439:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L442:   if_acmpeq L457 
L445:   aload 12 
L447:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L450:   aload 10 
L452:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L455:   fstore 13 
L457:   ldc +3.4028234663852886e+38f 
L459:   fstore 14 
L461:   aload 11 
L463:   ifnull L488 
L466:   aload 11 
L468:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L471:   aload_1 
L472:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L475:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L478:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L483:   invokestatic Method com/fs/starfarer/prototype/Utils void (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L486:   fstore 14 
L488:   aload 6 
L490:   getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType DEFEND Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L493:   if_acmpeq L520 
L496:   aload 6 
L498:   getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType CONTROL Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L501:   if_acmpeq L520 
L504:   aload 6 
L506:   getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType HARASS Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L509:   if_acmpeq L520 
L512:   aload 6 
L514:   getstatic Field com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType ENGAGE Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L517:   if_acmpne L563 
L520:   aload_0 
L521:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L524:   ldc_w +3000.0f 
L527:   invokestatic Method com/fs/starfarer/combat/ai/N 'ø00000' (Lcom/fs/starfarer/combat/entities/Ship;F)Lcom/fs/starfarer/combat/entities/Ship; 
L530:   astore 15 
L532:   fload 14 
L534:   fload_3 
L535:   ldc_w +300.0f 
L538:   fadd 
L539:   fcmpl 
L540:   ifle L563 
L543:   fload_3 
L544:   ldc_w +3000.0f 
L547:   fcmpg 
L548:   ifge L563 
L551:   aload 15 
L553:   ifnull L563 
L556:   aload_0 
L557:   iconst_1 
L558:   putfield Field com/genir/aitweaks/asm/OrderResponseModule class Z 
L561:   aconst_null 
L562:   areturn 
L563:   aload_0 
L564:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L567:   invokevirtual Method com/fs/starfarer/combat/entities/Ship isCarrier ()Z 
L570:   istore 15 
L572:   iload 15 
L574:   aload_0 
L575:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L578:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSpec ()Lcom/fs/starfarer/loading/specs/privatesuper; 
L581:   invokevirtual Method com/fs/starfarer/loading/specs/privatesuper getHints ()Ljava/util/EnumSet; 
L584:   getstatic Field com/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints NO_AUTO_ESCORT Lcom/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints; 
L587:   invokevirtual Method java/util/EnumSet contains (Ljava/lang/Object;)Z 
L590:   ifeq L597 
L593:   iconst_0 
L594:   goto L598 
L597:   iconst_1 
L598:   iand 
L599:   istore 15 
L601:   iload 15 
L603:   aload_0 
L604:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L607:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getHullSpec ()Lcom/fs/starfarer/loading/specs/privatesuper; 
L610:   invokevirtual Method com/fs/starfarer/loading/specs/privatesuper getHints ()Ljava/util/EnumSet; 
L613:   getstatic Field com/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints COMBAT Lcom/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints; 
L616:   invokevirtual Method java/util/EnumSet contains (Ljava/lang/Object;)Z 
L619:   ifeq L626 
L622:   iconst_0 
L623:   goto L627 
L626:   iconst_1 
L627:   iand 
L628:   istore 15 
L630:   aload 6 
L632:   getstatic Field [c323] INTERCEPT [u327] 
L635:   if_acmpeq L659 
L638:   aload 6 
L640:   getstatic Field [c323] AVOID [u327] 
L643:   if_acmpeq L659 
L646:   aload 6 
L648:   getstatic Field [c323] STRIKE [u327] 
L651:   if_acmpne L703 
L654:   iload 15 
L656:   ifne L703 
L659:   fload_3 
L660:   ldc_w +2700.0f 
L663:   fcmpg 
L664:   ifge L703 
L667:   aload_1 
L668:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L671:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L674:   ifnull L703 
L677:   aload_2 
L678:   aload_1 
L679:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L682:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L685:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L688:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L691:   aload_2 
L692:   aconst_null 
L693:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L696:   aload_2 
L697:   fconst_0 
L698:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L701:   aload_2 
L702:   areturn 
L703:   iconst_0 
L704:   istore 16 
L706:   iconst_0 
L707:   istore 17 
L709:   aload_0 
L710:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L713:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L716:   ifnull L749 
L719:   aload_0 
L720:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L723:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L726:   invokevirtual Method com/fs/starfarer/combat/ai/L getSpec ()Lcom/fs/starfarer/loading/specs/FighterWingSpec; 
L729:   invokevirtual Method com/fs/starfarer/loading/specs/FighterWingSpec isBomber ()Z 
L732:   istore 16 
L734:   aload_0 
L735:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L738:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getWing ()Lcom/fs/starfarer/combat/ai/L; 
L741:   invokevirtual Method com/fs/starfarer/combat/ai/L getSpec ()Lcom/fs/starfarer/loading/specs/FighterWingSpec; 
L744:   invokevirtual Method com/fs/starfarer/loading/specs/FighterWingSpec isSupport ()Z 
L747:   istore 17 
L749:   iload 16 
L751:   ifne L759 
L754:   iload 17 
L756:   ifeq L803 
L759:   fload_3 
L760:   ldc_w +3000.0f 
L763:   fcmpg 
L764:   ifge L803 
L767:   aload_1 
L768:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L771:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L774:   ifnull L803 
L777:   aload_2 
L778:   aload_1 
L779:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L782:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L785:   invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L788:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L791:   aload_2 
L792:   aconst_null 
L793:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
L796:   aload_2 
L797:   fconst_0 
L798:   putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L801:   aload_2 
L802:   areturn 
L803:   iload 4 
L805:   ifne L1011 
L808:   aload 6 
L810:   getstatic Field [c323] CARRIER_GROUP [u327] 
L813:   if_acmpne L823 
L816:   fload_3 
L817:   ldc +1000.0f 
L819:   fcmpl 
L820:   ifgt L1011 
L823:   aload 6 
L825:   getstatic Field [c323] STRIKE [u327] 
L828:   if_acmpne L841 
L831:   iload 16 
L833:   ifne L1011 
L836:   iload 17 
L838:   ifne L1011 
L841:   aload 6 
L843:   getstatic Field [c323] ENGAGE [u327] 
L846:   if_acmpne L859 
L849:   iload 16 
L851:   ifne L1011 
L854:   iload 17 
L856:   ifne L1011 
L859:   aload 6 
L861:   getstatic Field [c323] AVOID [u327] 
L864:   if_acmpne L877 
L867:   iload 16 
L869:   ifne L1011 
L872:   iload 17 
L874:   ifne L1011 
L877:   aload 6 
L879:   getstatic Field [c323] CAPTURE [u327] 
L882:   if_acmpeq L893 
L885:   aload 6 
L887:   getstatic Field [c323] ASSAULT [u327] 
L890:   if_acmpne L948 
L893:   fload 13 
L895:   fload_3 
L896:   fcmpl 
L897:   iflt L948 
L900:   aload_1 
L901:   invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L904:   invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L907:   invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getOwner ()I 1 
L912:   aload_0 
L913:   getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L916:   invokevirtual Method com/fs/starfarer/combat/entities/Ship getOwner ()I 
L919:   if_icmpeq L948 
L922:   aload_0 
L923:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L926:   invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L931:   invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO Object ()Z 
L934:   ifne L948 
L937:   fload 14 
L939:   fload_3 
L940:   ldc_w +500.0f 
L943:   fadd 
L944:   fcmpl 
L945:   ifgt L1011 
L948:   aload 6 
L950:   getstatic Field [c323] STRIKE_FORCE [u327] 
L953:   if_acmpeq L964 
L956:   aload 6 
L958:   getstatic Field [c323] CIVILIAN_CRAFT [u327] 
L961:   if_acmpne L1027 
L964:   aload_0 
L965:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L968:   invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L973:   invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'ô00000' ()F 
L976:   fconst_0 
L977:   fcmpg 
L978:   ifle L1011 
L981:   aload_0 
L982:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L985:   invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L990:   invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO int ()Z 
L993:   ifeq L1027 
L996:   aload_0 
L997:   getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1000:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L1005:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ô00000' ()Z 
L1008:  ifne L1027 
L1011:  aload_0 
L1012:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1015:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L1018:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MOVEMENT_DEST_WHILE_SIDETRACKED Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L1021:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags unsetFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)V 
L1024:  goto L1334 
L1027:  aload_0 
L1028:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1031:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L1036:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ô00000' ()Z 
L1039:  ifne L1092 
L1042:  aload_0 
L1043:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1046:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L1051:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO return ()Z 
L1054:  ifne L1072 
L1057:  aload_0 
L1058:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1061:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L1066:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO 'Ò00000' ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo; 
L1069:  ifnonnull L1092 
L1072:  aload_0 
L1073:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1076:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getThreatEvaluator ()Lcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO; 1 
L1081:  invokevirtual Method com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO Object ()Z 
L1084:  ifeq L1278 
L1087:  iload 9 
L1089:  ifle L1278 
L1092:  ldc_w +90.0f 
L1095:  fconst_0 
L1096:  ldc_w +3000.0f 
L1099:  fload_3 
L1100:  fsub 
L1101:  invokestatic Method java/lang/Math max (FF)F 
L1104:  ldc_w +0.009999999776482582f 
L1107:  fmul 
L1108:  fadd 
L1109:  fstore 18 
L1111:  aload_0 
L1112:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1115:  aload 10 
L1117:  fload 18 
L1119:  fload_3 
L1120:  ldc +1000.0f 
L1122:  fadd 
L1123:  aload 7 
L1125:  aconst_null 
L1126:  invokestatic Method com/fs/starfarer/combat/ai/N 'super' [u125] 
L1129:  istore 19 
L1131:  aload 6 
L1133:  getstatic Field [c323] DEFEND [u327] 
L1136:  if_acmpeq L1163 
L1139:  aload 6 
L1141:  getstatic Field [c323] ASSAULT [u327] 
L1144:  if_acmpeq L1163 
L1147:  aload 6 
L1149:  getstatic Field [c323] CAPTURE [u327] 
L1152:  if_acmpeq L1163 
L1155:  aload 6 
L1157:  getstatic Field [c323] CONTROL [u327] 
L1160:  if_acmpne L1171 
L1163:  fload_3 
L1164:  ldc_w +4000.0f 
L1167:  fcmpl 
L1168:  ifle L1174 
L1171:  iconst_0 
L1172:  istore 19 
L1174:  fload_3 
L1175:  ldc_w +1500.0f 
L1178:  fcmpg 
L1179:  iflt L1187 
L1182:  iload 19 
L1184:  ifle L1222 
L1187:  aload_0 
L1188:  iconst_1 
L1189:  putfield Field com/genir/aitweaks/asm/OrderResponseModule class Z 
L1192:  aload_0 
L1193:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1196:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L1199:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MOVEMENT_DEST_WHILE_SIDETRACKED Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L1202:  ldc_w +5.0f 
L1205:  aload_1 
L1206:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1209:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1212:  invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L1217:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;FLjava/lang/Object;)V 
L1220:  aconst_null 
L1221:  areturn 
L1222:  aload_0 
L1223:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1226:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L1229:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MOVEMENT_DEST_WHILE_SIDETRACKED Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L1232:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags unsetFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)V 
L1235:  new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L1238:  dup 
L1239:  aload_0 
L1240:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1243:  aload_1 
L1244:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1247:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1250:  invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L1255:  aload_0 
L1256:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1259:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L1262:  astore 20 
L1264:  aload_2 
L1265:  aload 20 
L1267:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' [u29] 
L1270:  aload_2 
L1271:  ldc +3.0f 
L1273:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L1276:  aload_2 
L1277:  areturn 
L1278:  fload_3 
L1279:  ldc +2000.0f 
L1281:  fcmpg 
L1282:  ifge L1334 
L1285:  aload_0 
L1286:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1289:  ldc_w +3000.0f 
L1292:  invokestatic Method com/fs/starfarer/combat/ai/N 'ø00000' (Lcom/fs/starfarer/combat/entities/Ship;F)Lcom/fs/starfarer/combat/entities/Ship; 
L1295:  astore 18 
L1297:  aload_0 
L1298:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1301:  ldc_w +3000.0f 
L1304:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o setMaxTargetRange (F)V 2 
L1309:  aload 18 
L1311:  ifnull L1334 
L1314:  aload_0 
L1315:  iconst_1 
L1316:  putfield Field com/genir/aitweaks/asm/OrderResponseModule class Z 
L1319:  aload_0 
L1320:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1323:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L1326:  getstatic Field com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags MOVEMENT_DEST_WHILE_SIDETRACKED Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
L1329:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags unsetFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)V 
L1332:  aconst_null 
L1333:  areturn 
L1334:  aload_0 
L1335:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1338:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 
L1341:  getstatic Field [c197] MOVEMENT_DEST_WHILE_SIDETRACKED [u201] 
L1344:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags unsetFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)V 
L1347:  aconst_null 
L1348:  astore 18 
L1350:  aload 6 
L1352:  getstatic Field [c323] DEFEND [u327] 
L1355:  if_acmpne L1699 
L1358:  aload_1 
L1359:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1362:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1365:  instanceof com/fs/starfarer/combat/CombatFleetManager$O0 
L1368:  ifeq L1699 
L1371:  fload_3 
L1372:  ldc_w +3000.0f 
L1375:  fcmpg 
L1376:  ifge L1699 
L1379:  aload_1 
L1380:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1383:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1386:  checkcast com/fs/starfarer/combat/CombatFleetManager$O0 
L1389:  astore 19 
L1391:  aload 19 
L1393:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L1396:  ifnull L1699 
L1399:  new java/util/ArrayList 
L1402:  dup 
L1403:  invokespecial Method java/util/ArrayList <init> ()V 
L1406:  astore 20 
L1408:  aload_0 
L1409:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ö00000' Lcom/fs/starfarer/combat/CombatFleetManager; 
L1412:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager getDeployed ()Ljava/util/Set; 
L1415:  invokeinterface InterfaceMethod java/util/Set iterator ()Ljava/util/Iterator; 1 
L1420:  astore 22 
L1422:  goto L1494 
L1425:  aload 22 
L1427:  invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L1432:  checkcast com/fs/starfarer/combat/CombatFleetManager$O0 
L1435:  astore 21 
L1437:  aload 21 
L1439:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L1442:  ifnonnull L1448 
L1445:  goto L1494 
L1448:  aload_0 
L1449:  getfield Field com/genir/aitweaks/asm/OrderResponseModule o00000 Lcom/fs/starfarer/combat/tasks/CombatTaskManager; 
L1452:  aload 21 
L1454:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L1457:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTaskManager getAssignmentFor (Lcom/fs/starfarer/api/combat/ShipAPI;)Lcom/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo; 
L1460:  astore 23 
L1462:  aload 23 
L1464:  ifnull L1494 
L1467:  aload 23 
L1469:  invokeinterface InterfaceMethod com/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo getTarget ()Lcom/fs/starfarer/api/combat/AssignmentTargetAPI; 1 
L1474:  aload_1 
L1475:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1478:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1481:  if_acmpne L1494 
L1484:  aload 20 
L1486:  aload 21 
L1488:  invokeinterface InterfaceMethod java/util/List add (Ljava/lang/Object;)Z 2 
L1493:  pop 
L1494:  aload 22 
L1496:  invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L1501:  ifne L1425 
L1504:  new org/lwjgl/util/vector/Vector2f 
L1507:  dup 
L1508:  aload_1 
L1509:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1512:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1515:  invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L1520:  invokespecial Method org/lwjgl/util/vector/Vector2f <init> (Lorg/lwjgl/util/vector/ReadableVector2f;)V 
L1523:  astore 21 
L1525:  ldc_w +0.10000000149011612f 
L1528:  fstore 22 
L1530:  aload 21 
L1532:  fload 22 
L1534:  invokevirtual Method org/lwjgl/util/vector/Vector2f scale (F)Lorg/lwjgl/util/vector/Vector; 
L1537:  pop 
L1538:  aload 20 
L1540:  invokeinterface InterfaceMethod java/util/List iterator ()Ljava/util/Iterator; 1 
L1545:  astore 24 
L1547:  goto L1612 
L1550:  aload 24 
L1552:  invokeinterface InterfaceMethod java/util/Iterator next ()Ljava/lang/Object; 1 
L1557:  checkcast com/fs/starfarer/combat/CombatFleetManager$O0 
L1560:  astore 23 
L1562:  new org/lwjgl/util/vector/Vector2f 
L1565:  dup 
L1566:  aload 23 
L1568:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1571:  invokespecial Method org/lwjgl/util/vector/Vector2f <init> (Lorg/lwjgl/util/vector/ReadableVector2f;)V 
L1574:  astore 25 
L1576:  aload 23 
L1578:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L1581:  iconst_0 
L1582:  invokestatic Method com/fs/starfarer/combat/ai/N 'Ò00000' (Lcom/fs/starfarer/combat/entities/Ship;Z)F 
L1585:  fstore 26 
L1587:  aload 25 
L1589:  fload 26 
L1591:  invokevirtual Method org/lwjgl/util/vector/Vector2f scale (F)Lorg/lwjgl/util/vector/Vector; 
L1594:  pop 
L1595:  aload 21 
L1597:  aload 25 
L1599:  aload 21 
L1601:  invokestatic Method org/lwjgl/util/vector/Vector2f add (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L1604:  pop 
L1605:  fload 22 
L1607:  fload 26 
L1609:  fadd 
L1610:  fstore 22 
L1612:  aload 24 
L1614:  invokeinterface InterfaceMethod java/util/Iterator hasNext ()Z 1 
L1619:  ifne L1550 
L1622:  aload 21 
L1624:  fconst_1 
L1625:  fload 22 
L1627:  fdiv 
L1628:  invokevirtual Method org/lwjgl/util/vector/Vector2f scale (F)Lorg/lwjgl/util/vector/Vector; 
L1631:  pop 
L1632:  aload 21 
L1634:  aload_0 
L1635:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1638:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1641:  invokestatic Method com/fs/starfarer/api/util/Misc getAngleInDegrees (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L1644:  fstore 23 
L1646:  ldc +1000.0f 
L1648:  aload 19 
L1650:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L1653:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L1656:  fadd 
L1657:  aload_0 
L1658:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1661:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getCollisionRadius ()F 
L1664:  fadd 
L1665:  fstore 24 
L1667:  fload 23 
L1669:  invokestatic Method com/fs/starfarer/api/util/Misc getUnitVectorAtDegreeAngle (F)Lorg/lwjgl/util/vector/Vector2f; 
L1672:  astore 25 
L1674:  aload 25 
L1676:  fload 24 
L1678:  invokevirtual Method org/lwjgl/util/vector/Vector2f scale (F)Lorg/lwjgl/util/vector/Vector; 
L1681:  pop 
L1682:  aload 25 
L1684:  aload 19 
L1686:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L1689:  aload 25 
L1691:  invokestatic Method org/lwjgl/util/vector/Vector2f add (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L1694:  pop 
L1695:  aload 25 
L1697:  astore 18 
L1699:  invokestatic Method com/genir/aitweaks/asm/OrderResponseModule null ()[I 
L1702:  aload 6 
L1704:  invokevirtual Method [c323] ordinal ()I 
L1707:  iaload 
L1708:  tableswitch 1 
            L1800 
            L1800 
            L1939 
            L1800 
            L1800 
            L1800 
            L1800 
            L1800 
            L1800 
            L1859 
            L1800 
            L1859 
            L1800 
            L1800 
            L1859 
            L1800 
            L2099 
            L2099 
            L2099 
            default : L2099 

L1800:  aconst_null 
L1801:  astore 19 
L1803:  aload_1 
L1804:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1807:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1810:  invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L1815:  astore 20 
L1817:  aload 18 
L1819:  ifnull L1826 
L1822:  aload 18 
L1824:  astore 20 
L1826:  new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L1829:  dup 
L1830:  aload_0 
L1831:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1834:  aload 20 
L1836:  aload_0 
L1837:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1840:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L1843:  astore 19 
L1845:  aload_2 
L1846:  aload 19 
L1848:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' [u29] 
L1851:  aload_2 
L1852:  fconst_1 
L1853:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L1856:  goto L2099 
L1859:  iload 16 
L1861:  ifne L1869 
L1864:  iload 17 
L1866:  ifeq L1896 
L1869:  aload_1 
L1870:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1873:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L1876:  ifnull L1896 
L1879:  aload_2 
L1880:  aload_1 
L1881:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1884:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask int ()Lcom/fs/starfarer/combat/CombatFleetManager$O0; 
L1887:  invokevirtual Method com/fs/starfarer/combat/CombatFleetManager$O0 getShipIfShip ()Lcom/fs/starfarer/combat/entities/Ship; 
L1890:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L1893:  goto L2099 
L1896:  new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L1899:  dup 
L1900:  aload_0 
L1901:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1904:  aload_1 
L1905:  invokevirtual Method com/fs/starfarer/combat/tasks/C 'Õ00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask; 
L1908:  invokevirtual Method com/fs/starfarer/combat/tasks/CombatTask 'Ò00000' ()Lcom/fs/starfarer/combat/tasks/CombatTask$Oo; 
L1911:  invokeinterface InterfaceMethod com/fs/starfarer/combat/tasks/CombatTask$Oo getLocation ()Lorg/lwjgl/util/vector/Vector2f; 1 
L1916:  aload_0 
L1917:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1920:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L1923:  astore 19 
L1925:  aload_2 
L1926:  aload 19 
L1928:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' [u29] 
L1931:  aload_2 
L1932:  fconst_1 
L1933:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L1936:  goto L2099 
L1939:  iload 15 
L1941:  ifeq L2099 
L1944:  aload_0 
L1945:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L1948:  aconst_null 
L1949:  invokestatic Method com/fs/starfarer/combat/ai/N 'õ00000' (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/combat/entities/Ship;)F 
L1952:  fstore 21 
L1954:  ldc_w +200.0f 
L1957:  fstore 22 
L1959:  aload_0 
L1960:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L1963:  invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 1 
L1968:  getstatic Field [c197] MAINTAINING_STRIKE_RANGE [u201] 
L1971:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags hasFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;)Z 
L1974:  istore 23 
L1976:  iload 23 
L1978:  ifeq L1986 
L1981:  ldc_w +600.0f 
L1984:  fstore 22 
L1986:  fload_3 
L1987:  fload 21 
L1989:  fload 22 
L1991:  fsub 
L1992:  fcmpl 
L1993:  ifle L2099 
L1996:  iload 23 
L1998:  ifne L2028 
L2001:  aload_0 
L2002:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L2005:  invokeinterface InterfaceMethod [c485] getAIFlags ()Lcom/fs/starfarer/api/combat/ShipwideAIFlags; 1 
L2010:  getstatic Field [c197] MAINTAINING_STRIKE_RANGE [u201] 
L2013:  ldc_w +5.0f 
L2016:  invokestatic Method java/lang/Math random ()D 
L2019:  d2f 
L2020:  ldc_w +5.0f 
L2023:  fmul 
L2024:  fadd 
L2025:  invokevirtual Method com/fs/starfarer/api/combat/ShipwideAIFlags setFlag (Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags;F)V 
L2028:  aload 10 
L2030:  aload_0 
L2031:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L2034:  invokevirtual Method com/fs/starfarer/combat/entities/Ship getLocation ()Lorg/lwjgl/util/vector/Vector2f; 
L2037:  invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)F 
L2040:  invokestatic Method com/fs/starfarer/prototype/Utils 'Ó00000' (F)Lorg/lwjgl/util/vector/Vector2f; 
L2043:  astore 24 
L2045:  aload 24 
L2047:  fload 21 
L2049:  fload 22 
L2051:  fsub 
L2052:  invokevirtual Method org/lwjgl/util/vector/Vector2f scale (F)Lorg/lwjgl/util/vector/Vector; 
L2055:  pop 
L2056:  aload 24 
L2058:  aload 10 
L2060:  aload 24 
L2062:  invokestatic Method org/lwjgl/util/vector/Vector2f add (Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;)Lorg/lwjgl/util/vector/Vector2f; 
L2065:  pop 
L2066:  new com/fs/starfarer/combat/ai/movement/maneuvers/U 
L2069:  dup 
L2070:  aload_0 
L2071:  getfield Field com/genir/aitweaks/asm/OrderResponseModule 'Ô00000' Lcom/fs/starfarer/combat/entities/Ship; 
L2074:  aload 24 
L2076:  aload_0 
L2077:  getfield Field com/genir/aitweaks/asm/OrderResponseModule oO0000 [u176] 
L2080:  invokespecial Method com/fs/starfarer/combat/ai/movement/maneuvers/U <init> (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o;)V 
L2083:  astore 19 
L2085:  aload_2 
L2086:  aload 19 
L2088:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' [u29] 
L2091:  aload_2 
L2092:  fconst_1 
L2093:  putfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ô00000' F 
L2096:  goto L2099 
L2099:  aload_2 
L2100:  getfield Field com/genir/aitweaks/asm/OrderResponseModule$o Object Lcom/fs/starfarer/combat/entities/Ship; 
L2103:  ifnonnull L2115 
L2106:  aload_2 
L2107:  getfield Field com/genir/aitweaks/asm/OrderResponseModule$o 'Ò00000' [u29] 
L2110:  ifnonnull L2115 
L2113:  aconst_null 
L2114:  areturn 
L2115:  aload_2 
L2116:  areturn 
L2117:  
    .end code 
.end method 

.method private o00000 : (Lcom/fs/starfarer/combat/entities/Ship;)Z 
    .code stack 2 locals 3 
L0:     invokestatic Method com/fs/starfarer/combat/CombatEngine getInstance ()Lcom/fs/starfarer/combat/CombatEngine; 
L3:     invokevirtual Method com/fs/starfarer/combat/CombatEngine getObjects ()Lcom/fs/util/container/repo/ObjectRepository; 
L6:     ldc Class com/fs/starfarer/combat/entities/Ship 
L8:     invokevirtual Method com/fs/util/container/repo/ObjectRepository getList (Ljava/lang/Class;)Ljava/util/List; 
L11:    astore_2 
L12:    aload_1 
L13:    ifnull L35 
L16:    aload_1 
L17:    invokevirtual Method com/fs/starfarer/combat/entities/Ship isHulk ()Z 
L20:    ifne L35 
L23:    aload_2 
L24:    aload_1 
L25:    invokeinterface InterfaceMethod java/util/List contains (Ljava/lang/Object;)Z 2 
L30:    ifeq L35 
L33:    iconst_1 
L34:    ireturn 
L35:    iconst_0 
L36:    ireturn 
L37:    
    .end code 
.end method 

.method static synthetic 'õ00000' : ()[I 
    .code stack 3 locals 1 
L0:     getstatic Field com/genir/aitweaks/asm/OrderResponseModule new [I 
L3:     dup 
L4:     ifnull L8 
L7:     areturn 
L8:     pop 
L9:     invokestatic Method com/fs/starfarer/combat/tasks/C$o values ()[Lcom/fs/starfarer/combat/tasks/C$o; 
L12:    arraylength 
L13:    newarray int 
L15:    astore_0 
        .catch java/lang/NoSuchFieldError from L16 to L25 using L28 
L16:    aload_0 
L17:    getstatic Field com/fs/starfarer/combat/tasks/C$o 'Ö00000' Lcom/fs/starfarer/combat/tasks/C$o; 
L20:    invokevirtual Method com/fs/starfarer/combat/tasks/C$o ordinal ()I 
L23:    iconst_1 
L24:    iastore 
L25:    goto L29 
L28:    pop 
        .catch java/lang/NoSuchFieldError from L29 to L38 using L41 
L29:    aload_0 
L30:    getstatic Field com/fs/starfarer/combat/tasks/C$o new Lcom/fs/starfarer/combat/tasks/C$o; 
L33:    invokevirtual Method com/fs/starfarer/combat/tasks/C$o ordinal ()I 
L36:    iconst_5 
L37:    iastore 
L38:    goto L42 
L41:    pop 
        .catch java/lang/NoSuchFieldError from L42 to L51 using L54 
L42:    aload_0 
L43:    getstatic Field com/fs/starfarer/combat/tasks/C$o 'Ó00000' Lcom/fs/starfarer/combat/tasks/C$o; 
L46:    invokevirtual Method com/fs/starfarer/combat/tasks/C$o ordinal ()I 
L49:    iconst_3 
L50:    iastore 
L51:    goto L55 
L54:    pop 
        .catch java/lang/NoSuchFieldError from L55 to L64 using L67 
L55:    aload_0 
L56:    getstatic Field com/fs/starfarer/combat/tasks/C$o 'Ô00000' Lcom/fs/starfarer/combat/tasks/C$o; 
L59:    invokevirtual Method com/fs/starfarer/combat/tasks/C$o ordinal ()I 
L62:    iconst_2 
L63:    iastore 
L64:    goto L68 
L67:    pop 
        .catch java/lang/NoSuchFieldError from L68 to L77 using L80 
L68:    aload_0 
L69:    getstatic Field com/fs/starfarer/combat/tasks/C$o class Lcom/fs/starfarer/combat/tasks/C$o; 
L72:    invokevirtual Method com/fs/starfarer/combat/tasks/C$o ordinal ()I 
L75:    iconst_4 
L76:    iastore 
L77:    goto L81 
L80:    pop 
L81:    aload_0 
L82:    dup 
L83:    putstatic Field com/genir/aitweaks/asm/OrderResponseModule new [I 
L86:    areturn 
L87:    
    .end code 
.end method 

.method static synthetic null : ()[I 
    .code stack 3 locals 1 
L0:     getstatic Field com/genir/aitweaks/asm/OrderResponseModule 'õ00000' [I 
L3:     dup 
L4:     ifnull L8 
L7:     areturn 
L8:     pop 
L9:     invokestatic Method [c323] values ()[Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
L12:    arraylength 
L13:    newarray int 
L15:    astore_0 
        .catch java/lang/NoSuchFieldError from L16 to L26 using L29 
L16:    aload_0 
L17:    getstatic Field [c323] ASSAULT [u327] 
L20:    invokevirtual Method [c323] ordinal ()I 
L23:    bipush 9 
L25:    iastore 
L26:    goto L30 
L29:    pop 
        .catch java/lang/NoSuchFieldError from L30 to L40 using L43 
L30:    aload_0 
L31:    getstatic Field [c323] AVOID [u327] 
L34:    invokevirtual Method [c323] ordinal ()I 
L37:    bipush 12 
L39:    iastore 
L40:    goto L44 
L43:    pop 
        .catch java/lang/NoSuchFieldError from L44 to L54 using L57 
L44:    aload_0 
L45:    getstatic Field [c323] CAPTURE [u327] 
L48:    invokevirtual Method [c323] ordinal ()I 
L51:    bipush 6 
L53:    iastore 
L54:    goto L58 
L57:    pop 
        .catch java/lang/NoSuchFieldError from L58 to L67 using L70 
L58:    aload_0 
L59:    getstatic Field [c323] CARRIER_GROUP [u327] 
L62:    invokevirtual Method [c323] ordinal ()I 
L65:    iconst_5 
L66:    iastore 
L67:    goto L71 
L70:    pop 
        .catch java/lang/NoSuchFieldError from L71 to L81 using L84 
L71:    aload_0 
L72:    getstatic Field [c323] CIVILIAN_CRAFT [u327] 
L75:    invokevirtual Method [c323] ordinal ()I 
L78:    bipush 14 
L80:    iastore 
L81:    goto L85 
L84:    pop 
        .catch java/lang/NoSuchFieldError from L85 to L95 using L98 
L85:    aload_0 
L86:    getstatic Field [c323] CONTROL [u327] 
L89:    invokevirtual Method [c323] ordinal ()I 
L92:    bipush 7 
L94:    iastore 
L95:    goto L99 
L98:    pop 
        .catch java/lang/NoSuchFieldError from L99 to L109 using L112 
L99:    aload_0 
L100:   getstatic Field [c323] DEFEND [u327] 
L103:   invokevirtual Method [c323] ordinal ()I 
L106:   bipush 11 
L108:   iastore 
L109:   goto L113 
L112:   pop 
        .catch java/lang/NoSuchFieldError from L113 to L123 using L126 
L113:   aload_0 
L114:   getstatic Field [c323] DESTROYER_ESCORT [u327] 
L117:   invokevirtual Method [c323] ordinal ()I 
L120:   bipush 18 
L122:   iastore 
L123:   goto L127 
L126:   pop 
        .catch java/lang/NoSuchFieldError from L127 to L137 using L140 
L127:   aload_0 
L128:   getstatic Field [c323] ENGAGE [u327] 
L131:   invokevirtual Method [c323] ordinal ()I 
L134:   bipush 10 
L136:   iastore 
L137:   goto L141 
L140:   pop 
        .catch java/lang/NoSuchFieldError from L141 to L151 using L154 
L141:   aload_0 
L142:   getstatic Field [c323] FULL_ESCORT [u327] 
L145:   invokevirtual Method [c323] ordinal ()I 
L148:   bipush 19 
L150:   iastore 
L151:   goto L155 
L154:   pop 
        .catch java/lang/NoSuchFieldError from L155 to L165 using L168 
L155:   aload_0 
L156:   getstatic Field [c323] HARASS [u327] 
L159:   invokevirtual Method [c323] ordinal ()I 
L162:   bipush 16 
L164:   iastore 
L165:   goto L169 
L168:   pop 
        .catch java/lang/NoSuchFieldError from L169 to L179 using L182 
L169:   aload_0 
L170:   getstatic Field [c323] INTERCEPT [u327] 
L173:   invokevirtual Method [c323] ordinal ()I 
L176:   bipush 15 
L178:   iastore 
L179:   goto L183 
L182:   pop 
        .catch java/lang/NoSuchFieldError from L183 to L193 using L196 
L183:   aload_0 
L184:   getstatic Field [c323] LIGHT_ESCORT [u327] 
L187:   invokevirtual Method [c323] ordinal ()I 
L190:   bipush 17 
L192:   iastore 
L193:   goto L197 
L196:   pop 
        .catch java/lang/NoSuchFieldError from L197 to L207 using L210 
L197:   aload_0 
L198:   getstatic Field [c323] PATROL [u327] 
L201:   invokevirtual Method [c323] ordinal ()I 
L204:   bipush 8 
L206:   iastore 
L207:   goto L211 
L210:   pop 
        .catch java/lang/NoSuchFieldError from L211 to L220 using L223 
L211:   aload_0 
L212:   getstatic Field [c323] RALLY_FIGHTERS [u327] 
L215:   invokevirtual Method [c323] ordinal ()I 
L218:   iconst_4 
L219:   iastore 
L220:   goto L224 
L223:   pop 
        .catch java/lang/NoSuchFieldError from L224 to L234 using L237 
L224:   aload_0 
L225:   getstatic Field [c323] RALLY_TASK_FORCE [u327] 
L228:   invokevirtual Method [c323] ordinal ()I 
L231:   bipush 13 
L233:   iastore 
L234:   goto L238 
L237:   pop 
        .catch java/lang/NoSuchFieldError from L238 to L247 using L250 
L238:   aload_0 
L239:   getstatic Field [c323] RECON [u327] 
L242:   invokevirtual Method [c323] ordinal ()I 
L245:   iconst_1 
L246:   iastore 
L247:   goto L251 
L250:   pop 
        .catch java/lang/NoSuchFieldError from L251 to L260 using L263 
L251:   aload_0 
L252:   getstatic Field [c323] STRIKE [u327] 
L255:   invokevirtual Method [c323] ordinal ()I 
L258:   iconst_3 
L259:   iastore 
L260:   goto L264 
L263:   pop 
        .catch java/lang/NoSuchFieldError from L264 to L273 using L276 
L264:   aload_0 
L265:   getstatic Field [c323] STRIKE_FORCE [u327] 
L268:   invokevirtual Method [c323] ordinal ()I 
L271:   iconst_2 
L272:   iastore 
L273:   goto L277 
L276:   pop 
L277:   aload_0 
L278:   dup 
L279:   putstatic Field com/genir/aitweaks/asm/OrderResponseModule 'õ00000' [I 
L282:   areturn 
L283:   
    .end code 
.end method 
.innerclasses 
    com/fs/starfarer/api/combat/CombatFleetManagerAPI$AssignmentInfo com/fs/starfarer/api/combat/CombatFleetManagerAPI AssignmentInfo public static interface abstract 
    com/fs/starfarer/api/combat/ShipAPI$HullSize com/fs/starfarer/api/combat/ShipAPI HullSize public static final enum 
    com/fs/starfarer/api/combat/ShipHullSpecAPI$ShipTypeHints com/fs/starfarer/api/combat/ShipHullSpecAPI ShipTypeHints public static final enum 
    [c197] com/fs/starfarer/api/combat/ShipwideAIFlags AIFlags public static final enum 
    com/fs/starfarer/combat/CombatFleetManager$O0 com/fs/starfarer/combat/CombatFleetManager O0 public static 
    com/genir/aitweaks/asm/OrderResponseModule$o com/genir/aitweaks/asm/OrderResponseModule o public 
    com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO$Oo com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO Oo public static 
    [c485] com/fs/starfarer/combat/ai/movement/maneuvers/oO0O o public static interface abstract 
    com/fs/starfarer/combat/tasks/CombatTask$Oo com/fs/starfarer/combat/tasks/CombatTask Oo public static interface abstract 
    [c323] com/fs/starfarer/combat/tasks/CombatTask CombatTaskType public static final enum 
    com/fs/starfarer/combat/tasks/C$o com/fs/starfarer/combat/tasks/C o public static final enum 
.end innerclasses 
.const [u29] = Utf8 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O; 
.const [u125] = Utf8 (Lcom/fs/starfarer/combat/entities/Ship;Lorg/lwjgl/util/vector/Vector2f;FFLcom/fs/starfarer/api/combat/ShipAPI$HullSize;Lcom/fs/starfarer/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B;)I 
.const [u176] = Utf8 Lcom/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o; 
.const [c197] = Class [u163] 
.const [u201] = Utf8 Lcom/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags; 
.const [c323] = Class [u279] 
.const [u327] = Utf8 Lcom/fs/starfarer/combat/tasks/CombatTask$CombatTaskType; 
.const [c485] = Class [u334] 
.const [u163] = Utf8 com/fs/starfarer/api/combat/ShipwideAIFlags$AIFlags 
.const [u279] = Utf8 com/fs/starfarer/combat/tasks/CombatTask$CombatTaskType 
.const [u334] = Utf8 com/fs/starfarer/combat/ai/movement/maneuvers/oO0O$o 
.end class 
