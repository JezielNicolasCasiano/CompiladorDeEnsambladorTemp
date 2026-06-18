
Data Segment
.datasegmente
.data segment
var1 DB “Hola”
var1 dw 045
variable_uno DB 0
variablese dw 0ef45h
dw Data Segment 45
34 db var2
var2 dw 789H
var2 dw 0789H
varbin db 00811101b
varbin1 db 00011101b
vb dw 00000000111111b
vbb dw 1000000011110011b
const1 dbw 50 dup(0)
cons db 50 dop(0)
cons db 50 dup(0)
cons equ 10
con1 equ 10
ends
 
.stack Segment
       dup(0) 100 dw
       100 dup(0) dw
       dw 100 dup(“?”)
ends
 
.code segment
et2:
       add cl,dl
       int 021H
       jns etdes
            jns et2
           loopne et2
 
;equipo 
       mov al, bx
       nop
       popf
       popa
       aad
et5: 
       inc bx
       dec con1
       mov var1, cl
       neg dx,var2
       std
       neg ax
    lea vardes
     and ax
       and al,bl
    mult ax
       CLD
            CLk
et2:
 
et6:
       adc cl,dl
       int 021H
             jns etdes
      jnae et2
       jnae e22
              jge et5
 
 
 
fin10:
;INGRESA ENL CODE SEGMENT DEL PROGRAMA CON LAS
;INSTRUCCCIONES DE TU EQUIPO AQUI
mov ax, @data
mov ds, ax
cbw ;correcto
cbw ax;incorrecto
clc;correcto
clc 1;incorrecto
lodsb;correcto
lodsb al;incorrecto
stosw;correcto
stosw es:di;incorrecto
div bl;correcto
div 10;incorrecto
imul bl;correcto
imul 5;incorrecto
inc ax; correcto
inc 5; incorrecto
neg ax;correcto
neg 20;incorrecto
add ax, bx;correcto
add [variable1], [variable1];incorrecto
lds si, [puntero];correcto
lds si, ax ;incorrecto
mov ax, 10;correcto
mov ds, 10;incorrecto
ror al, 1;correcto
ror al, 10;incorrecto
jns etiqueta_1;correcto
jns 100;incorrecto
js etiqueta_1;correcto
js ax;incorrecto
loopne etiqueta_1;correcto
loopne 5;incorrecto
jg etiqueta_1;correcto
jg ah, al;incorrecto
jmp etiqueta_1;correcto
jmp ax, bx;incorrecto
etiqueta_1:
mov ah, 4ch
int 21h

;equipo 
       mov al, bx
       nop
       aaa
et50: 
       inc bx
       hlt
       imul con1
       mov var1, cl
       idiv dx,var2
    lea vardes
     sar ax
     jc ax
       cwd
            cwd1
       movsw
       movsw ax
      
 
et60:   aad cl,dl
       adc cl,dl
       into 021H
             jl etdes
            jc et50
              jc e2
              loope et50
              jno et60
             jo et22
              jo et5
       xor al,cx
       xor al,cl
       xor al
 
 
ends
