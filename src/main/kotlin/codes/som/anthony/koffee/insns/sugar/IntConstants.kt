package codes.som.anthony.koffee.insns.sugar

import codes.som.anthony.koffee.insns.InstructionAssembly
import codes.som.anthony.koffee.insns.jvm.*

fun InstructionAssembly.push_int(i: Int) {
    when (i) {
        -1 -> iconst_m1
        0 -> iconst_0
        1 -> iconst_1
        2 -> iconst_2
        3 -> iconst_3
        4 -> iconst_4
        5 -> iconst_5

        in Byte.MIN_VALUE..Byte.MAX_VALUE -> bipush(i)
        in Short.MIN_VALUE..Short.MAX_VALUE -> sipush(i)

        else -> ldc(i)
    }
}
