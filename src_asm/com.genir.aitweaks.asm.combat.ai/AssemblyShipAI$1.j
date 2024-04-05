.version 49 0 
.class super com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 
.super java/lang/Object 
.implements com/fs/starfarer/combat/ai/G 
.field final synthetic 'ÔÒ0000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI; 
.field public final synthetic 'ÕÒ0000' Lcom/fs/starfarer/combat/ai/system/M; 

.method <init> : (Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI;Lcom/fs/starfarer/combat/ai/system/M;)V 
    .code stack 2 locals 3 
L0:     aload_0 
L1:     aload_1 
L2:     putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 'ÔÒ0000' Lcom/genir/aitweaks/asm/combat/ai/AssemblyShipAI; 
L5:     aload_0 
L6:     aload_2 
L7:     putfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 'ÕÒ0000' Lcom/fs/starfarer/combat/ai/system/M; 
L10:    aload_0 
L11:    invokespecial Method java/lang/Object <init> ()V 
L14:    return 
L15:    
    .end code 
.end method 

.method public o00000 : ()Z 
    .code stack 1 locals 1 
L0:     iconst_0 
L1:     ireturn 
L2:     
    .end code 
.end method 

.method public Object : ()Z 
    .code stack 1 locals 2 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 'ÕÒ0000' Lcom/fs/starfarer/combat/ai/system/M; 
L4:     instanceof com/fs/starfarer/combat/ai/system/V 
L7:     ifeq L29 
L10:    aload_0 
L11:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 'ÕÒ0000' Lcom/fs/starfarer/combat/ai/system/M; 
L14:    checkcast com/fs/starfarer/combat/ai/system/V 
L17:    astore_1 
L18:    aload_1 
L19:    invokevirtual Method com/fs/starfarer/combat/ai/system/V 'ôo0000' ()Lcom/fs/starfarer/combat/ai/null; 
L22:    invokevirtual Method com/fs/starfarer/combat/ai/null 'Ò00000' ()Lcom/fs/starfarer/combat/ai/C; 
L25:    invokevirtual Method com/fs/starfarer/combat/ai/C 'Õ00000' ()Z 
L28:    ireturn 
L29:    iconst_0 
L30:    ireturn 
L31:    
    .end code 
.end method 

.method public 'Ò00000' : ()Lcom/fs/starfarer/combat/ai/C; 
    .code stack 1 locals 2 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 'ÕÒ0000' Lcom/fs/starfarer/combat/ai/system/M; 
L4:     instanceof com/fs/starfarer/combat/ai/system/V 
L7:     ifeq L26 
L10:    aload_0 
L11:    getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 'ÕÒ0000' Lcom/fs/starfarer/combat/ai/system/M; 
L14:    checkcast com/fs/starfarer/combat/ai/system/V 
L17:    astore_1 
L18:    aload_1 
L19:    invokevirtual Method com/fs/starfarer/combat/ai/system/V 'ôo0000' ()Lcom/fs/starfarer/combat/ai/null; 
L22:    invokevirtual Method com/fs/starfarer/combat/ai/null 'Ò00000' ()Lcom/fs/starfarer/combat/ai/C; 
L25:    areturn 
L26:    aconst_null 
L27:    areturn 
L28:    
    .end code 
.end method 

.method public o00000 : [u46] 
    .code stack 5 locals 6 
L0:     aload_0 
L1:     getfield Field com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 'ÕÒ0000' Lcom/fs/starfarer/combat/ai/system/M; 
L4:     fload_1 
L5:     aload_3 
L6:     aload 4 
L8:     aload 5 
L10:    invokeinterface InterfaceMethod com/fs/starfarer/combat/ai/system/M 'super' (FLorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/entities/Ship;)V 5 
L15:    return 
L16:    
    .end code 
.end method 
.enclosing method com/genir/aitweaks/asm/combat/ai/AssemblyShipAI <init> (Lcom/fs/starfarer/combat/entities/Ship;Lcom/fs/starfarer/api/combat/ShipAIConfig;)V 
.innerclasses 
    com/genir/aitweaks/asm/combat/ai/AssemblyShipAI$1 [0] [0] 
.end innerclasses 
.const [u46] = Utf8 (FLcom/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;Lorg/lwjgl/util/vector/Vector2f;Lorg/lwjgl/util/vector/Vector2f;Lcom/fs/starfarer/combat/entities/Ship;)V 
.end class 
