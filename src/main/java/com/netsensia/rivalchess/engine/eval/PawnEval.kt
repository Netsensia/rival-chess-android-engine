package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.*
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.BITBOARD_BP
import com.netsensia.rivalchess.consts.BITBOARD_WP
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour
import kotlin.math.abs

private fun pawnDistanceFromPromotion(colour: Colour, square: Int) = if (colour == Colour.WHITE) yCoordOfSquare(square) else 7 - yCoordOfSquare(square)

inline fun pawnShieldEval(friendlyPawns: Long, enemyPawns: Long, friendlyPawnShield: Long, shifter: Long.(Int) -> Long) =
        (KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * popCount(friendlyPawns and friendlyPawnShield)
                - KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * popCount(enemyPawns and (friendlyPawnShield or shifter(friendlyPawnShield,8)))
                + KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * popCount(friendlyPawns and shifter(friendlyPawnShield,8))
                - KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * popCount(enemyPawns and shifter(friendlyPawnShield,16)))

fun whitePawnsEval(board: EngineBoard): Int {
    var acc = 0
    val blackPieceValues = board.blackPieceValues
    applyToSquares(board.getBitboard(BITBOARD_WP)) {
        acc += linearScale(
                blackPieceValues,
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                pawnEndGamePieceSquareTable[it],
                pawnPieceSquareTable[it]
        )
    }
    return acc
}

fun blackPawnsEval(board: EngineBoard): Int {
    var acc = 0
    val whitePieceValues = board.whitePieceValues
    applyToSquares(board.getBitboard(BITBOARD_BP)) {
        acc += linearScale(
                whitePieceValues,
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                pawnEndGamePieceSquareTable[bitFlippedHorizontalAxis[it]],
                pawnPieceSquareTable[bitFlippedHorizontalAxis[it]]
        )
    }
    return acc
}

fun pawnScore(attacks: Attacks, board: EngineBoard): Int {

    val whitePawnBitboard = board.getBitboard(BITBOARD_WP)
    val blackPawnBitboard = board.getBitboard(BITBOARD_BP)

    val whitePawnFiles = getPawnFiles(whitePawnBitboard)
    val blackPawnFiles = getPawnFiles(blackPawnBitboard)

    val whitePassedPawnsBitboard = getWhitePassedPawns(whitePawnBitboard, blackPawnBitboard)
    val blackPassedPawnsBitboard = getBlackPassedPawns(whitePawnBitboard, blackPawnBitboard)
    val whiteGuardedPassedPawns = whitePassedPawnsBitboard and attacks.whitePawns
    val blackGuardedPassedPawns = blackPassedPawnsBitboard and attacks.blackPawns
    val whiteBackwardPawns = getWhiteBackwardPawns(whitePawnBitboard, blackPawnBitboard, attacks.whitePawns, attacks.blackPawns, blackPawnFiles)
    val blackBackwardPawns = getBlackBackwardPawns(blackPawnBitboard, whitePawnBitboard, attacks.blackPawns, attacks.whitePawns, whitePawnFiles)
    val whiteIsolatedPawns = whitePawnFiles and (whitePawnFiles shl 1).inv() and (whitePawnFiles ushr 1).inv()
    val blackIsolatedPawns = blackPawnFiles and (blackPawnFiles shl 1).inv() and (blackPawnFiles ushr 1).inv()

    val whiteOccupiedFileMask = southFill(whitePawnBitboard) and RANK_1
    val blackOccupiedFileMask = southFill(blackPawnBitboard) and RANK_1

    fun whitePassedPawnScore(): Int {
        var acc = 0
        applyToSquares(whitePassedPawnsBitboard) { acc += VALUE_PASSED_PAWN_BONUS[yCoordOfSquare(it)] }
        return popCount(whiteGuardedPassedPawns) * VALUE_GUARDED_PASSED_PAWN + acc
    }

    fun blackPassedPawnScore(): Int {
        var acc = 0
        applyToSquares(blackPassedPawnsBitboard) { acc += VALUE_PASSED_PAWN_BONUS[7 - yCoordOfSquare(it)] }
        return popCount(blackGuardedPassedPawns) * VALUE_GUARDED_PASSED_PAWN + acc
    }

    return  popCount(blackIsolatedPawns) * VALUE_ISOLATED_PAWN_PENALTY -
            popCount(whiteIsolatedPawns) * VALUE_ISOLATED_PAWN_PENALTY -
            (if (whiteIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) +
            (if (blackIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) -
            popCount(whiteBackwardPawns) * VALUE_BACKWARD_PAWN_PENALTY +
            popCount(blackBackwardPawns) * VALUE_BACKWARD_PAWN_PENALTY -
            ((popCount(whitePawnBitboard and FILE_A) + popCount(whitePawnBitboard and FILE_H)) * VALUE_SIDE_PAWN_PENALTY) +
            ((popCount(blackPawnBitboard and FILE_A) + popCount(blackPawnBitboard and FILE_H)) * VALUE_SIDE_PAWN_PENALTY) -
            VALUE_DOUBLED_PAWN_PENALTY * (board.whitePawnValues / 100 - popCount(whiteOccupiedFileMask)) -
            popCount(whiteOccupiedFileMask.inv() ushr 1 and whiteOccupiedFileMask) * VALUE_PAWN_ISLAND_PENALTY +
            VALUE_DOUBLED_PAWN_PENALTY * (board.blackPawnValues / 100 - popCount(blackOccupiedFileMask)) +
            popCount(blackOccupiedFileMask.inv() ushr 1 and blackOccupiedFileMask) * VALUE_PAWN_ISLAND_PENALTY +
            (linearScale(board.blackPieceValues, 0, PAWN_ADJUST_MAX_MATERIAL, whitePassedPawnScore() * 2, whitePassedPawnScore())) -
            (linearScale(board.whitePieceValues, 0, PAWN_ADJUST_MAX_MATERIAL, blackPassedPawnScore() * 2, blackPassedPawnScore())) +
            (if (board.blackPieceValues < PAWN_ADJUST_MAX_MATERIAL)
                calculateLowMaterialPawnBonus(
                        Colour.BLACK,
                        board.whiteKingSquare,
                        board.blackKingSquare,
                        board,
                        whitePassedPawnsBitboard,
                        blackPassedPawnsBitboard,
                        board.mover)
            else 0) +
            (if (board.whitePieceValues < PAWN_ADJUST_MAX_MATERIAL)
                calculateLowMaterialPawnBonus(
                        Colour.WHITE,
                        board.whiteKingSquare,
                        board.blackKingSquare,
                        board,
                        whitePassedPawnsBitboard,
                        blackPassedPawnsBitboard,
                        board.mover)
            else 0)
}

fun calculateLowMaterialPawnBonus(
        lowMaterialColour: Colour,
        whiteKingSquare: Int,
        blackKingSquare: Int,
        board: EngineBoard,
        whitePassedPawnsBitboard: Long,
        blackPassedPawnsBitboard: Long,
        mover: Colour
): Int {

    val kingSquare = if (lowMaterialColour == Colour.WHITE) whiteKingSquare else blackKingSquare
    val kingX = xCoordOfSquare(kingSquare)
    val kingY = yCoordOfSquare(kingSquare)
    val lowMaterialSidePieceValues = if (lowMaterialColour == Colour.WHITE) board.whitePieceValues else board.blackPieceValues

    var acc = 0
    applyToSquares (if (lowMaterialColour == Colour.WHITE) blackPassedPawnsBitboard else whitePassedPawnsBitboard) {
        val pawnDistance = pawnDistanceFromPromotion(lowMaterialColour, it).coerceAtMost(5)
        val kingXDistanceFromPawn = difference(kingX, it)
        val kingYDistanceFromPawn = difference(colourAdjustedYRank(lowMaterialColour, kingY), it)
        val kingDistanceFromPawn = kingXDistanceFromPawn.coerceAtLeast(kingYDistanceFromPawn)

        val moverAdjustment = if (lowMaterialColour == mover) 1 else 0

        val scoreAdjustment = linearScale(
                lowMaterialSidePieceValues,
                0,
                PAWN_ADJUST_MAX_MATERIAL,
                kingDistanceFromPawn * 4,
                0) +
                if (pawnDistance < kingDistanceFromPawn - moverAdjustment && lowMaterialSidePieceValues == 0) {
                    VALUE_KING_CANNOT_CATCH_PAWN
                } else 0

        acc += if (lowMaterialColour == Colour.WHITE) -scoreAdjustment else scoreAdjustment
    }

    return acc

}

fun colourAdjustedYRank(colour: Colour, yRank: Int) = if (colour == Colour.WHITE) yRank else abs(yRank - 7)

fun difference(kingX: Int, it: Int) = abs(kingX - xCoordOfSquare(it))

fun xCoordOfSquare(square: Int) = square % 8

fun yCoordOfSquare(square: Int) = square / 8

fun getPawnFiles(pawns: Long) = southFill(pawns) and RANK_1

fun getBlackPassedPawns(whitePawns: Long, blackPawns: Long) =
        blackPawns and northFill(whitePawns or whitePawnAttacks(whitePawns) or (blackPawns shl 8)).inv()

fun getWhitePassedPawns(whitePawns: Long, blackPawns: Long) =
        whitePawns and southFill(blackPawns or blackPawnAttacks(blackPawns) or (whitePawns ushr 8)).inv()

fun getWhiteBackwardPawns(whitePawnBitboard: Long, blackPawnBitboard: Long, whitePawnAttacks: Long, blackPawnAttacks: Long, blackPawnFiles: Long) =
        whitePawnBitboard and
                (whitePawnBitboard or blackPawnBitboard ushr 8).inv() and
                (blackPawnAttacks ushr 8) and
                northFill(whitePawnAttacks).inv() and
                blackPawnAttacks(whitePawnBitboard) and
                northFill(blackPawnFiles).inv()

fun getBlackBackwardPawns(blackPawnBitboard: Long, whitePawnBitboard: Long, blackPawnAttacks: Long, whitePawnAttacks: Long, whitePawnFiles: Long) =
        blackPawnBitboard and
                (blackPawnBitboard or whitePawnBitboard shl 8).inv() and
                (whitePawnAttacks shl 8) and
                southFill(blackPawnAttacks).inv() and
                whitePawnAttacks(blackPawnBitboard) and
                northFill(whitePawnFiles).inv()