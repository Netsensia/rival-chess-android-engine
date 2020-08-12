package com.netsensia.rivalchess.config

import com.netsensia.rivalchess.consts.BITBOARD_WP
import com.netsensia.rivalchess.engine.eval.pieceValue

val FUTILITY_MARGIN_BASE: Int = (pieceValue(BITBOARD_WP) * 2)
val FUTILITY_MARGIN = intArrayOf(FUTILITY_MARGIN_BASE, FUTILITY_MARGIN_BASE * 2, FUTILITY_MARGIN_BASE * 3)