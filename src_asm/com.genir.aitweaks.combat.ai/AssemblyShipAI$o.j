.version 49 0 
.class final super enum com/genir/aitweaks/asm/AssemblyShipAI$o 
.super java/lang/Enum 
.field public static final enum String Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
.field public static final enum 'Ò00000' Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
.field public static final enum 'Ó00000' Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
.field public static final synthetic 'super' [Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 

.method static <clinit> : ()V 
    .code stack 4 locals 0 
L0:     new com/genir/aitweaks/asm/AssemblyShipAI$o 
L3:     dup 
L4:     ldc 'STRAFE' 
L6:     iconst_0 
L7:     invokespecial Method com/genir/aitweaks/asm/AssemblyShipAI$o <init> (Ljava/lang/String;I)V 
L10:    putstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L13:    new com/genir/aitweaks/asm/AssemblyShipAI$o 
L16:    dup 
L17:    ldc 'BACK_AWAY' 
L19:    iconst_1 
L20:    invokespecial Method com/genir/aitweaks/asm/AssemblyShipAI$o <init> (Ljava/lang/String;I)V 
L23:    putstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o 'Ò00000' Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L26:    new com/genir/aitweaks/asm/AssemblyShipAI$o 
L29:    dup 
L30:    ldc 'HOVER' 
L32:    iconst_2 
L33:    invokespecial Method com/genir/aitweaks/asm/AssemblyShipAI$o <init> (Ljava/lang/String;I)V 
L36:    putstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o 'Ó00000' Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L39:    iconst_3 
L40:    anewarray com/genir/aitweaks/asm/AssemblyShipAI$o 
L43:    dup 
L44:    iconst_0 
L45:    getstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o String Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L48:    aastore 
L49:    dup 
L50:    iconst_1 
L51:    getstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o 'Ò00000' Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L54:    aastore 
L55:    dup 
L56:    iconst_2 
L57:    getstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o 'Ó00000' Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L60:    aastore 
L61:    putstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o 'super' [Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L64:    return 
L65:    
    .end code 
.end method 

.method private <init> : (Ljava/lang/String;I)V 
    .code stack 3 locals 3 
L0:     aload_0 
L1:     aload_1 
L2:     iload_2 
L3:     invokespecial Method java/lang/Enum <init> (Ljava/lang/String;I)V 
L6:     return 
L7:     
    .end code 
.end method 

.method public static values : ()[Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
    .code stack 5 locals 3 
L0:     getstatic Field com/genir/aitweaks/asm/AssemblyShipAI$o 'super' [Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
L3:     dup 
L4:     astore_0 
L5:     iconst_0 
L6:     aload_0 
L7:     arraylength 
L8:     dup 
L9:     istore_1 
L10:    anewarray com/genir/aitweaks/asm/AssemblyShipAI$o 
L13:    dup 
L14:    astore_2 
L15:    iconst_0 
L16:    iload_1 
L17:    invokestatic Method java/lang/System arraycopy (Ljava/lang/Object;ILjava/lang/Object;II)V 
L20:    aload_2 
L21:    areturn 
L22:    
    .end code 
.end method 

.method public static valueOf : (Ljava/lang/String;)Lcom/genir/aitweaks/asm/AssemblyShipAI$o; 
    .code stack 2 locals 1 
L0:     ldc Class com/genir/aitweaks/asm/AssemblyShipAI$o 
L2:     aload_0 
L3:     invokestatic Method java/lang/Enum valueOf (Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum; 
L6:     checkcast com/genir/aitweaks/asm/AssemblyShipAI$o 
L9:     areturn 
L10:    
    .end code 
.end method 
.signature Ljava/lang/Enum<Lcom/genir/aitweaks/asm/AssemblyShipAI$o;>; 
.innerclasses 
    com/genir/aitweaks/asm/AssemblyShipAI$o com/genir/aitweaks/asm/AssemblyShipAI o private static final enum 
.end innerclasses 
.end class 
