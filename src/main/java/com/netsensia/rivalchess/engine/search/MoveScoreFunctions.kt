package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.bitboards.bitFlippedHorizontalAxis
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.eval.*
import com.netsensia.rivalchess.model.Colour

fun scorePieceSquareValues(board: EngineBoard, fromSquare: Int, toSquare: Int): Int {
    val piece = board.getBitboardTypeOfPieceOnSquare(fromSquare, board.mover)
    val fromAdjusted = if (board.mover == Colour.WHITE) fromSquare else bitFlippedHorizontalAxis[fromSquare]
    val toAdjusted = if (board.mover == Colour.WHITE) toSquare else bitFlippedHorizontalAxis[toSquare]

    return when (piece) {
        BITBOARD_WP, BITBOARD_BP -> linearScale(
                if (board.mover == Colour.WHITE) board.blackPieceValues else board.whitePieceValues,
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                pawnEndGamePieceSquareTable[toAdjusted] - pawnEndGamePieceSquareTable[fromAdjusted],
                pawnPieceSquareTable[toAdjusted] - pawnPieceSquareTable[fromAdjusted])
        BITBOARD_WR, BITBOARD_BR -> rookPieceSquareTable[toAdjusted] - rookPieceSquareTable[fromAdjusted]
        BITBOARD_WN, BITBOARD_BN -> linearScale(
                if (board.mover == Colour.WHITE) board.blackPieceValues + board.blackPawnValues else board.whitePieceValues + board.whitePawnValues,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                knightEndGamePieceSquareTable[toAdjusted] - knightEndGamePieceSquareTable[fromAdjusted],
                knightPieceSquareTable[toAdjusted] - knightPieceSquareTable[fromAdjusted])
        BITBOARD_WB, BITBOARD_BB -> bishopPieceSquareTable[toAdjusted] - bishopPieceSquareTable[fromAdjusted]
        BITBOARD_WQ, BITBOARD_BQ -> queenPieceSquareTable[toAdjusted] - queenPieceSquareTable[fromAdjusted]
        BITBOARD_WK, BITBOARD_BK -> linearScale(
                if (board.mover == Colour.WHITE) board.blackPieceValues else board.whitePieceValues,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                kingEndGamePieceSquareTable[toAdjusted] - kingEndGamePieceSquareTable[fromAdjusted],
                kingPieceSquareTable[toAdjusted] - kingPieceSquareTable[fromAdjusted])
        else -> 0
    }
}

fun moveHasScore(move: Int) = move shr 24 != 127

fun getHighestScoringMoveFromArray(theseMoves: IntArray): Int {
    var bestIndex = -1
    var best = Int.MAX_VALUE
    var c = -1
    while (theseMoves[++c] != 0) {
        if (theseMoves[c] != -1 && theseMoves[c] < best && moveHasScore(theseMoves[c])) {
            // update best move found so far, but don't consider moves with no score
            best = theseMoves[c]
            bestIndex = c
        }
    }
    return if (best == Int.MAX_VALUE) {
        0
    } else {
        theseMoves[bestIndex] = -1
        moveNoScore(best)
    }
}
