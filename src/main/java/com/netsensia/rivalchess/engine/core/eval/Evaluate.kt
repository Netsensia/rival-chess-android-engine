package com.netsensia.rivalchess.engine.core.eval

import arrow.syntax.function.memoize
import com.netsensia.rivalchess.bitboards.BitboardType.*
import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.bitboards.util.*
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.engine.core.EngineChessBoard
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant
import java.lang.Long.bitCount
import java.lang.Long.numberOfTrailingZeros

data class BitboardData(
        val whitePawns: Long = 0L,
        val whiteBishops: Long = 0L,
        val whiteKnights: Long = 0L,
        val whiteKing: Long = 0L,
        val whiteQueens: Long = 0L,
        val whiteRooks: Long = 0L,
        val blackPawns: Long = 0L,
        val blackBishops: Long = 0L,
        val blackKnights: Long = 0L,
        val blackKing: Long = 0L,
        val blackQueens: Long = 0L,
        val blackRooks: Long = 0L,
        val enemy: Long = 0L,
        val friendly: Long = 0L,
        val all: Long = 0L,
        val enPassantSquare: Long = 0L
)

data class PieceSquareLists(
        val whitePawns: List<Int>,
        val whiteRooks: List<Int>,
        val whiteBishops: List<Int>,
        val whiteKnights: List<Int>,
        val whiteQueens: List<Int>,
        val blackPawns: List<Int>,
        val blackRooks: List<Int>,
        val blackBishops: List<Int>,
        val blackKnights: List<Int>,
        val blackQueens: List<Int>
)

data class AttackLists(
        val whitePawns: Long,
        val blackPawns: Long,
        val whiteRooks: List<Long>,
        val blackRooks: List<Long>,
        val whiteQueens: List<Long>,
        val whiteKnights: List<Long>,
        val blackQueens: List<Long>,
        val whiteBishops: List<Long>,
        val blackBishops: List<Long>,
        val blackKnights: List<Long>
)

data class MaterialValues(
        val whitePieces: Int,
        val blackPieces: Int,
        val whitePawns: Int,
        val blackPawns: Int
)

fun initBitboardData(board: EngineChessBoard) =
    BitboardData(
            whitePawns = board.getBitboard(WP),
            whiteBishops = board.getBitboard(WB),
            whiteKnights = board.getBitboard(WN),
            whiteKing = board.getBitboard(WK),
            whiteQueens = board.getBitboard(WQ),
            whiteRooks = board.getBitboard(WR),
            blackPawns = board.getBitboard(BP),
            blackBishops = board.getBitboard(BB),
            blackKnights = board.getBitboard(BN),
            blackKing = board.getBitboard(BK),
            blackQueens = board.getBitboard(BQ),
            blackRooks = board.getBitboard(BR),
            enPassantSquare = board.getBitboard(ENPASSANTSQUARE),
            enemy = board.getBitboard(ENEMY),
            all = board.getBitboard(ALL),
            friendly = board.getBitboard(FRIENDLY)
    )


fun initPieceSquareLists(bitboards: BitboardData) =
    PieceSquareLists(
            whitePawns = squareList(bitboards.whitePawns),
            whiteRooks = squareList(bitboards.whiteRooks),
            whiteBishops = squareList(bitboards.whiteBishops),
            whiteKnights = squareList(bitboards.whiteKnights),
            whiteQueens = squareList(bitboards.whiteQueens),
            blackPawns = squareList(bitboards.blackPawns),
            blackRooks = squareList(bitboards.blackRooks),
            blackBishops = squareList(bitboards.blackBishops),
            blackKnights = squareList(bitboards.blackKnights),
            blackQueens = squareList(bitboards.blackQueens)
    )

fun initAttackLists(bitboards: BitboardData, pieceSquareLists: PieceSquareLists) =
        AttackLists(
            whitePawns = whitePawnAttacks(bitboards.whitePawns),
            blackPawns = blackPawnAttacks(bitboards.blackPawns),
            whiteRooks = rookAttackList(bitboards, pieceSquareLists.whiteRooks),
            whiteBishops = bishopAttackList(bitboards, pieceSquareLists.whiteBishops),
            whiteQueens = queenAttackList(bitboards, pieceSquareLists.whiteQueens),
            whiteKnights = knightAttackList(pieceSquareLists.whiteKnights),
            blackRooks = rookAttackList(bitboards, pieceSquareLists.blackRooks),
            blackBishops = bishopAttackList(bitboards, pieceSquareLists.blackBishops),
            blackQueens = queenAttackList(bitboards, pieceSquareLists.blackQueens),
            blackKnights = knightAttackList(pieceSquareLists.blackKnights)
        )

fun initMaterialValues(bitboards: BitboardData) =
        MaterialValues(
                whitePieces = whitePieceValues(bitboards),
                blackPieces = blackPieceValues(bitboards),
                whitePawns = whitePawnValues(bitboards),
                blackPawns = blackPawnValues(bitboards)
        )

fun materialDifferenceEval(materialValues: MaterialValues) =
        materialValues.whitePieces - materialValues.blackPieces +
                materialValues.whitePawns - materialValues.blackPawns

fun onlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L

fun onlyKingsRemain(bitboards: BitboardData) = onlyOneBitSet(bitboards.enemy) and onlyOneBitSet(bitboards.friendly)

fun whiteKingSquareEval(bitboards: BitboardData) =
        linearScale(
                blackPieceValues(bitboards),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.value,
                PieceSquareTables.kingEndGame[whiteKingSquare(bitboards)],
                PieceSquareTables.king[whiteKingSquare(bitboards)]
        )

fun blackKingSquareEval(bitboards: BitboardData) =
        linearScale(
                whitePieceValues(bitboards),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.value,
                PieceSquareTables.kingEndGame[Bitboards.bitFlippedHorizontalAxis[blackKingSquare(bitboards)]],
                PieceSquareTables.king[Bitboards.bitFlippedHorizontalAxis[blackKingSquare(bitboards)]]
        )

fun linearScale(situation: Int, ref1: Int, ref2: Int, score1: Int, score2: Int) =
        when {
            situation < ref1 -> score1
            situation > ref2 -> score2
            else -> (situation - ref1) * (score2 - score1) / (ref2 - ref1) + score1
        }

fun twoWhiteRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.whiteRooks and Bitboards.RANK_7) > 1
                && bitboards.blackKing and Bitboards.RANK_8 != 0L)
            Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.value else 0

fun twoBlackRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.blackRooks and Bitboards.RANK_2) > 1
                && bitboards.whiteKing and Bitboards.RANK_1 != 0L)
            Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.value else 0

fun whiteRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if (Bitboards.FILES[file] and bitboards.whitePawns == 0L)
            if (Bitboards.FILES[file] and bitboards.blackPawns == 0L)
                Evaluation.VALUE_ROOK_ON_OPEN_FILE.value
            else
                Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.value
        else 0

fun blackRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if ((Bitboards.FILES[file] and bitboards.blackPawns) == 0L)
            if ((Bitboards.FILES[file] and bitboards.whitePawns) == 0L)
                Evaluation.VALUE_ROOK_ON_OPEN_FILE.value
            else
                Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.value
        else 0

fun rookEnemyPawnMultiplier(enemyPawnValues: Int) =
        (enemyPawnValues / PieceValue.getValue(Piece.PAWN)).coerceAtMost(6)

fun knightAttackList(squares: List<Int>) : List<Long> =
        squares.asSequence().map { Bitboards.knightMoves[it] }.toList()

fun rookAttacks(bitboards: BitboardData, sq: Int) : Long =
        Bitboards.magicBitboards.magicMovesRook[sq][
                ((bitboards.all and MagicBitboards.occupancyMaskRook[sq])
                        * MagicBitboards.magicNumberRook[sq] ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun rookAttackList(bitboards: BitboardData, rookSquares: List<Int>) : List<Long> =
        rookSquares.asSequence().map { rookAttacks(bitboards, it) }.toList()

fun bishopAttacks(bitboards: BitboardData, sq: Int) =
        Bitboards.magicBitboards.magicMovesBishop[sq][
                ((bitboards.all and MagicBitboards.occupancyMaskBishop[sq])
                        * MagicBitboards.magicNumberBishop[sq]
                        ushr MagicBitboards.magicNumberShiftsBishop[sq]).toInt()]

fun bishopAttackList(bitboards: BitboardData, whiteBishopSquares: List<Int>) : List<Long> =
        whiteBishopSquares.asSequence().map { s -> bishopAttacks(bitboards, s)}.toList()

fun queenAttacks(bitboards: BitboardData, sq: Int) = rookAttacks(bitboards, sq) or bishopAttacks(bitboards, sq)

fun queenAttackList(bitboards: BitboardData, whiteQueenSquares: List<Int>) : List<Long> =
        whiteQueenSquares.asSequence().map { s -> queenAttacks(bitboards, s)}.toList()

fun sameFile(square1: Int, square2: Int) = square1 % 8 == square2 % 8

fun doubledRooksEval(squares: List<Int>) =
        if (squares.size > 1 && sameFile(squares[0], squares[1]))
            Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else
            if (squares.size > 2 && (sameFile(squares[0], squares[2]) || sameFile(squares[1], squares[2])))
                Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else 0

fun flippedSquareTableScore(table: List<Int>, bit: Int) = table[Bitboards.bitFlippedHorizontalAxis[bit]]

fun kingAttackCount(dangerZone: Long, attacks: List<Long>): Int {
    return attacks.asSequence()
            .map { bitCount(it and dangerZone) }
            .fold(0) { acc, i -> acc + i }
}

fun tradePieceBonusWhenMoreMaterial(bitboards: BitboardData, materialDifference: Int): Int {
    return linearScale(
            if (materialDifference > 0)
                blackPieceValues(bitboards) +
                        blackPawnValues(bitboards) else
                whitePieceValues(bitboards) +
                        whitePawnValues(bitboards),
            0,
            Evaluation.TOTAL_PIECE_VALUE_PER_SIDE_AT_START.value,
            30 * materialDifference / 100,
            0)
}

fun tradePawnBonusWhenMoreMaterial(bitboards: BitboardData, materialDifference: Int): Int {
    return linearScale(
            if (materialDifference > 0) whitePawnValues(bitboards) else blackPawnValues(bitboards),
            0,
            Evaluation.TRADE_BONUS_UPPER_PAWNS.value,
            -30 * materialDifference / 100,
            0)
}

fun bishopScore(bitboards: BitboardData, materialDifference: Int) =
        bishopPairEval(bitboards) + oppositeColourBishopsEval(bitboards, materialDifference) + trappedBishopEval(bitboards)

fun whiteLightBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and Bitboards.LIGHT_SQUARES != 0L

fun whiteDarkBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and Bitboards.DARK_SQUARES != 0L

fun blackLightBishopExists(bitboards: BitboardData) = bitboards.blackBishops and Bitboards.LIGHT_SQUARES != 0L

fun blackDarkBishopExists(bitboards: BitboardData) = bitboards.blackBishops and Bitboards.DARK_SQUARES != 0L

fun whiteBishopColourCount(bitboards: BitboardData) =
        (if (whiteLightBishopExists(bitboards)) 1 else 0) + if (whiteDarkBishopExists(bitboards)) 1 else 0

fun blackBishopColourCount(bitboards: BitboardData) =
        (if (blackLightBishopExists(bitboards)) 1 else 0) + if (blackDarkBishopExists(bitboards)) 1 else 0

fun oppositeColourBishopsEval(bitboards: BitboardData, materialDifference: Int): Int {

    if (whiteBishopColourCount(bitboards) == 1 && blackBishopColourCount(bitboards) == 1 &&
            whiteLightBishopExists(bitboards) != blackLightBishopExists(bitboards) &&
            whitePieceValues(bitboards) == blackPieceValues(bitboards)) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty = materialDifference / Evaluation.WRONG_COLOUR_BISHOP_PENALTY_DIVISOR.value // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        return -linearScale(
                whitePieceValues(bitboards) + blackPieceValues(bitboards),
                Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_LOW.value,
                Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_HIGH.value,
                maxPenalty,
                0)
    }
    return 0
}

fun bishopPairEval(bitboards: BitboardData) = (if (whiteBishopColourCount(bitboards) == 2)
    Evaluation.VALUE_BISHOP_PAIR.value +
            (8 - whitePawnValues(bitboards) / PieceValue.getValue(Piece.PAWN)) *
            Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
else 0) -
        if (blackBishopColourCount(bitboards) == 2)
            Evaluation.VALUE_BISHOP_PAIR.value +
                    (8 - blackPawnValues(bitboards) / PieceValue.getValue(Piece.PAWN)) *
                    Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
        else 0

fun trappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops or bitboards.blackBishops and Bitboards.A2A7H2H7 != 0L)
            blackA2TrappedBishopEval(bitboards) +
                    blackH2TrappedBishopEval(bitboards) -
                    whiteA7TrappedBishopEval(bitboards) -
                    whiteH7TrappedBishopEval(bitboards)
        else 0

fun blackH2TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.blackBishops and (1L shl Square.H2.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.G3.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.F2.bitRef) != 0L)
            if (bitboards.blackQueens == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
            else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value
        else 0

fun blackA2TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.blackBishops and (1L shl Square.A2.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.B3.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.C2.bitRef) != 0L)
            Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
        else 0

fun whiteH7TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops and (1L shl Square.H7.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.G6.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.F7.bitRef) != 0L)
            if (bitboards.whiteQueens == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
            else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value
        else 0

fun whiteA7TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops and (1L shl Square.A7.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.B6.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.C7.bitRef) != 0L)
            Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
        else 0

fun blackPieceBitboard(bitboards: BitboardData) =
        (bitboards.blackKnights or bitboards.blackRooks or bitboards.blackQueens or bitboards.blackBishops)

fun whitePieceBitboard(bitboards: BitboardData) =
        (bitboards.whiteKnights or bitboards.whiteRooks or bitboards.whiteQueens or bitboards.whiteBishops)

fun whitePieceAttackList(attackLists: AttackLists): List<Long> {
    val result = ArrayList<Long>()

    result.addAll(attackLists.whiteRooks)
    result.addAll(attackLists.whiteQueens)
    result.addAll(attackLists.whiteBishops)
    result.addAll(attackLists.whiteKnights)

    return result
}

fun blackPieceAttackList(attackLists: AttackLists): List<Long> {

    val result = ArrayList<Long>()
    result.addAll(attackLists.blackRooks)
    result.addAll(attackLists.blackQueens)
    result.addAll(attackLists.blackBishops)
    result.addAll(attackLists.blackKnights)

    return result
}

fun whiteAttacksBitboard(bitboards: BitboardData, attackLists: AttackLists) =
        (orList(whitePieceAttackList(attackLists)) or attackLists.whitePawns) and
                blackPieceBitboard(bitboards)

fun blackAttacksBitboard(bitboards: BitboardData, attackLists: AttackLists) =
        (orList(blackPieceAttackList(attackLists)) or attackLists.blackPawns) and
                whitePieceBitboard(bitboards)

fun threatEval(bitboards: BitboardData, attackLists: AttackLists, squareOccupants: List<SquareOccupant>): Int {
    return (adjustedAttackScore(whiteAttackScore(bitboards, attackLists, squareOccupants)) -
            adjustedAttackScore(blackAttackScore(bitboards, attackLists, squareOccupants))) /
            Evaluation.THREAT_SCORE_DIVISOR.value
}

fun adjustedAttackScore(attackScore: Int) =
        attackScore + attackScore * (attackScore / PieceValue.getValue(Piece.QUEEN))

fun whiteAttackScore(bitboards: BitboardData, attackLists: AttackLists, squareOccupants: List<SquareOccupant>): Int {
    return squareList(whiteAttacksBitboard(bitboards, attackLists))
            .asSequence()
            .map { PieceValue.getValue(squareOccupants[it].piece) }
            .fold(0) { acc, i -> acc + i }
}

fun blackAttackScore(bitboards: BitboardData, attackLists: AttackLists, squareOccupants: List<SquareOccupant>): Int {
    return squareList(blackAttacksBitboard(bitboards, attackLists))
            .asSequence()
            .map { PieceValue.getValue(squareOccupants[it].piece) }
            .fold(0) { acc, i -> acc + i }
}

fun isEndGame(bitboards: BitboardData) =
        (whitePieceValues(bitboards) +
                whitePawnValues(bitboards) +
                blackPieceValues(bitboards) +
                blackPawnValues(bitboards)) <= Evaluation.EVAL_ENDGAME_TOTAL_PIECES.value

fun kingSafetyEval(bitboards: BitboardData, attackLists: AttackLists, board: EngineChessBoard): Int {

    val whiteKingDangerZone = whiteKingDangerZone(bitboards)

    val blackKingDangerZone = blackKingDangerZone(bitboards)

    val blackKingAttackedCount = kingAttackCount(blackKingDangerZone, attackLists.whiteRooks) +
            kingAttackCount(blackKingDangerZone, attackLists.whiteQueens) * 2 +
            kingAttackCount(blackKingDangerZone, attackLists.whiteBishops)

    val whiteKingAttackedCount = kingAttackCount(whiteKingDangerZone, attackLists.blackRooks) +
            kingAttackCount(whiteKingDangerZone, attackLists.blackQueens) * 2 +
            kingAttackCount(whiteKingDangerZone, attackLists.blackBishops)

    val averagePiecesPerSide = (whitePieceValues(bitboards) + blackPieceValues(bitboards)) / 2

    if (averagePiecesPerSide <= Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.value) {
        return 0
    }

    val whiteKingSafety: Int = getWhiteKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + whiteKingShieldEval(bitboards)

    val blackKingSafety: Int = getBlackKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + blackKingShieldEval(bitboards)

    return linearScale(
            averagePiecesPerSide,
            Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.value,
            Evaluation.KINGSAFETY_MAX_PIECE_BALANCE.value,
            0,
            whiteKingSafety -
                    blackKingSafety +
                    (blackKingAttackedCount - whiteKingAttackedCount) *
                    Evaluation.KINGSAFETY_ATTACK_MULTIPLIER.value)

}

private fun blackKingDangerZone(bitboards: BitboardData) =
        Bitboards.kingMoves[blackKingSquare(bitboards)] or
                (Bitboards.kingMoves[blackKingSquare(bitboards)] ushr 8)

private fun whiteKingDangerZone(bitboards: BitboardData) =
        Bitboards.kingMoves[whiteKingSquare(bitboards)] or
                (Bitboards.kingMoves[whiteKingSquare(bitboards)] shl 8)

fun uncastledTrappedWhiteRookEval(bitboards: BitboardData) =
        if (bitboards.whiteKing and Bitboards.F1G1 != 0L &&
                bitboards.whiteRooks and Bitboards.G1H1 != 0L &&
                bitboards.whitePawns and Bitboards.FILE_G != 0L &&
                bitboards.whitePawns and Bitboards.FILE_H != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else (if (bitboards.whiteKing and Bitboards.B1C1 != 0L &&
                bitboards.whiteRooks and Bitboards.A1B1 != 0L &&
                bitboards.whitePawns and Bitboards.FILE_A != 0L &&
                bitboards.whitePawns and Bitboards.FILE_B != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else 0)

fun pawnShieldEval(friendlyPawns: Long, enemyPawns: Long, friendlyPawnShield: Long, shifter: Long.(Int) -> Long) =
        (Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.value * bitCount(friendlyPawns and friendlyPawnShield)
                - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.value * bitCount(enemyPawns and (friendlyPawnShield or shifter(friendlyPawnShield,8)))
                + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.value * bitCount(friendlyPawns and shifter(friendlyPawnShield,8))
                - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.value * bitCount(enemyPawns and shifter(friendlyPawnShield,16)))

fun uncastledTrappedBlackRookEval(bitboards: BitboardData) =
        if (bitboards.blackKing and Bitboards.F8G8 != 0L &&
                bitboards.blackRooks and Bitboards.G8H8 != 0L &&
                bitboards.blackPawns and Bitboards.FILE_G != 0L &&
                bitboards.blackPawns and Bitboards.FILE_H != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else (if (bitboards.blackKing and Bitboards.B8C8 != 0L &&
                bitboards.blackRooks and Bitboards.A8B8 != 0L &&
                bitboards.blackPawns and Bitboards.FILE_A != 0L &&
                bitboards.blackPawns and Bitboards.FILE_B != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else 0)

fun openFiles(kingShield: Long, pawnBitboard: Long) =
        southFill(kingShield, 8) and southFill(pawnBitboard, 8).inv() and Bitboards.RANK_1

fun whiteKingShieldEval(bitboards: BitboardData) =
        if (whiteKingOnFirstTwoRanks(bitboards)) {
            combineWhiteKingShieldEval(bitboards, whiteKingShield(bitboards))
        } else 0

fun combineWhiteKingShieldEval(bitboards: BitboardData, kingShield: Long) =
        pawnShieldEval(bitboards.whitePawns, bitboards.blackPawns, kingShield, Long::shl)
                .coerceAtMost(Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value) -
                uncastledTrappedWhiteRookEval(bitboards) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.whitePawns)) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.blackPawns))

fun openFilesKingShieldEval(openFiles: Long) =
        if (openFiles != 0L) {
            Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(openFiles and Bitboards.MIDDLE_FILES_8_BIT) +
                    Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(openFiles and Bitboards.NONMID_FILES_8_BIT)
        } else 0

fun blackKingShieldEval(bitboards: BitboardData) =
        if (blackKingOnFirstTwoRanks(bitboards)) {
            combineBlackKingShieldEval(bitboards, blackKingShield(bitboards))
        } else 0

fun combineBlackKingShieldEval(bitboards: BitboardData, kingShield: Long) =
        pawnShieldEval(bitboards.blackPawns, bitboards.whitePawns, kingShield, Long::ushr)
                .coerceAtMost(Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value) -
                uncastledTrappedBlackRookEval(bitboards) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.whitePawns)) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.blackPawns))

fun whiteKingOnFirstTwoRanks(bitboards: BitboardData) =
        yCoordOfSquare(whiteKingSquare(bitboards)) < 2

fun blackKingOnFirstTwoRanks(bitboards: BitboardData) =
        yCoordOfSquare(blackKingSquare(bitboards)) >= 6

fun blackKingShield(bitboards: BitboardData) =
        Bitboards.whiteKingShieldMask[blackKingSquare(bitboards) % 8] shl 40

fun whiteKingShield(bitboards: BitboardData): Long =
        Bitboards.whiteKingShieldMask[whiteKingSquare(bitboards) % 8]

fun whiteCastlingEval(bitboards: BitboardData, castlePrivileges: Int) : Int {

    val whiteCastleValue = maxCastleValue(blackPieceValues(bitboards))

    return if (whiteCastleValue > 0) {
        whiteCastleValue / whiteTimeToCastleKingSide(castlePrivileges, bitboards)
                .coerceAtMost(whiteTimeToCastleQueenSide(castlePrivileges, bitboards))
    } else 0
}

fun whiteTimeToCastleQueenSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 6) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 5) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 4) != 0L) 1 else 0)
        } else 100


fun whiteTimeToCastleKingSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 1) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 2) != 0L) 1 else 0)
        } else 100

fun blackTimeToCastleQueenSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 60) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 61) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 62) != 0L) 1 else 0)
        } else 100


fun blackTimeToCastleKingSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 57) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 58) != 0L) 1 else 0)
        } else 100

fun blackCastlingEval(bitboards: BitboardData, castlePrivileges: Int) : Int {
    // Value of moving King to its queenside castle destination in the middle game
    val blackCastleValue = maxCastleValue(whitePieceValues(bitboards))

    return if (blackCastleValue > 0) {
        blackCastleValue / blackTimeToCastleKingSide(castlePrivileges, bitboards)
                .coerceAtMost(blackTimeToCastleQueenSide(castlePrivileges, bitboards))
    } else 0
}

fun rookSquareBonus() = PieceSquareTables.rook[3] - PieceSquareTables.rook[0]

fun kingSquareBonusEndGame() = PieceSquareTables.kingEndGame[1] - PieceSquareTables.kingEndGame[3]

fun kingSquareBonusMiddleGame() = PieceSquareTables.king[1] - PieceSquareTables.king[3]

fun castlingEval(bitboards: BitboardData, castlePrivileges: Int) =
        if (isAnyCastleAvailable(castlePrivileges)) {
            whiteCastlingEval(bitboards, castlePrivileges) - blackCastlingEval(bitboards, castlePrivileges)
        } else 0

// don't want to exceed this value because otherwise castling would be discouraged due to the bonuses
// given by still having castling rights.
fun maxCastleValue(pieceValues: Int) =
        kingSquareBonusScaled(pieceValues, kingSquareBonusEndGame(), kingSquareBonusMiddleGame()) + rookSquareBonus()

fun kingSquareBonusScaled(pieceValues: Int, kingSquareBonusEndGame: Int, kingSquareBonusMiddleGame: Int) =
        linearScale(
                pieceValues,
                Evaluation.CASTLE_BONUS_LOW_MATERIAL.value,
                Evaluation.CASTLE_BONUS_HIGH_MATERIAL.value,
                kingSquareBonusEndGame,
                kingSquareBonusMiddleGame)

fun isAnyCastleAvailable(castlePrivileges: Int) = castlePrivileges != 0

fun endGameAdjustment(bitboards: BitboardData, currentScore: Int) =
        if (bothSidesHaveOnlyOneKnightOrBishopEach(bitboards)) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
        else when (currentScore) {
            0 -> 0
            in 0..Int.MAX_VALUE -> whiteWinningEndGameAdjustment(bitboards, currentScore)
            else -> blackWinningEndGameAdjustment(bitboards, currentScore)
        }

fun blackWinningEndGameAdjustment(bitboards: BitboardData, currentScore: Int) =
        if (blackHasInsufficientMaterial(bitboards)) currentScore + (blackPieceValues(bitboards) * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt()
        else if (probableDrawWhenBlackIsWinning(bitboards)) currentScore / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value
        else if (noBlackRooksQueensOrBishops(bitboards) && (blackBishopDrawOnFileA(bitboards) || blackBishopDrawOnFileH(bitboards))) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
        else if (whitePawnValues(bitboards) == 0) blackWinningNoWhitePawnsEndGameAdjustment(bitboards, currentScore)
        else currentScore

fun blackWinningNoWhitePawnsEndGameAdjustment(bitboards: BitboardData, currentScore: Int) =
        if (blackMoreThanABishopUpInNonPawns(bitboards)) {
            if (blackHasOnlyTwoKnights(bitboards) && whitePieceValues(bitboards) == 0) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
            else if (blackHasOnlyAKnightAndBishop(bitboards) && whitePieceValues(bitboards) == 0) {
                blackKnightAndBishopVKingEval(currentScore, bitboards)
            } else currentScore - Evaluation.VALUE_SHOULD_WIN.value
        } else currentScore

fun blackKnightAndBishopVKingEval(currentScore: Int, bitboards: BitboardData): Int {
    blackShouldWinWithKnightAndBishopValue(currentScore)
    return -if (blackDarkBishopExists(bitboards)) enemyKingCloseToDarkCornerMateSquareValue(whiteKingSquare(bitboards))
    else enemyKingCloseToLightCornerMateSquareValue(whiteKingSquare(bitboards))
}

fun whiteWinningEndGameAdjustment(bitboards: BitboardData, currentScore: Int) =
        if (whiteHasInsufficientMaterial(bitboards)) currentScore - (whitePieceValues(bitboards) * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt()
        else if (probablyDrawWhenWhiteIsWinning(bitboards)) currentScore / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value
        else if (noWhiteRooksQueensOrKnights(bitboards) && (whiteBishopDrawOnFileA(bitboards) || whiteBishopDrawOnFileH(bitboards))) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
        else if (blackPawnValues(bitboards) == 0) whiteWinningNoBlackPawnsEndGameAdjustment(bitboards, currentScore)
        else currentScore

fun whiteWinningNoBlackPawnsEndGameAdjustment(bitboards: BitboardData, currentScore: Int) =
        if (whiteMoreThanABishopUpInNonPawns(bitboards)) {
            if (whiteHasOnlyTwoKnights(bitboards) && blackPieceValues(bitboards) == 0) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
            else if (whiteHasOnlyAKnightAndBishop(bitboards) && blackPieceValues(bitboards) == 0) whiteKnightAndBishopVKingEval(currentScore, bitboards)
            else currentScore + Evaluation.VALUE_SHOULD_WIN.value
        } else currentScore

fun whiteKnightAndBishopVKingEval(currentScore: Int, bitboards: BitboardData): Int {
    whiteShouldWinWithKnightAndBishopValue(currentScore)
    return +if (whiteDarkBishopExists(bitboards)) enemyKingCloseToDarkCornerMateSquareValue(blackKingSquare(bitboards))
    else enemyKingCloseToLightCornerMateSquareValue(blackKingSquare(bitboards))
}

fun enemyKingCloseToDarkCornerMateSquareValue(kingSquare: Int) =
        enemyKingCloseToLightCornerMateSquareValue(Bitboards.bitFlippedHorizontalAxis[kingSquare])

fun enemyKingCloseToLightCornerMateSquareValue(kingSquare: Int) =
        (7 - Bitboards.distanceToH1OrA8[kingSquare]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value

fun blackShouldWinWithKnightAndBishopValue(eval: Int) =
        -(PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value) +
                eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value

fun whiteShouldWinWithKnightAndBishopValue(eval: Int) =
        PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value + eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value

fun whiteHasOnlyAKnightAndBishop(bitboards: BitboardData) =
        bitCount(bitboards.whiteKnights) == 1 && (whitePieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP))

fun blackHasOnlyAKnightAndBishop(bitboards: BitboardData) =
        bitCount(bitboards.blackKnights) == 1 && (blackPieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP))

fun whiteHasOnlyTwoKnights(bitboards: BitboardData) =
        bitCount(bitboards.whiteKnights) == 2 && (whitePieceValues(bitboards) == 2 * PieceValue.getValue(Piece.KNIGHT))

fun blackHasOnlyTwoKnights(bitboards: BitboardData) =
        bitCount(bitboards.blackKnights) == 2 && (blackPieceValues(bitboards) == 2 * PieceValue.getValue(Piece.KNIGHT))

fun blackMoreThanABishopUpInNonPawns(bitboards: BitboardData) =
        blackPieceValues(bitboards) - whitePieceValues(bitboards) > PieceValue.getValue(Piece.BISHOP)

fun whiteMoreThanABishopUpInNonPawns(bitboards: BitboardData) =
        whitePieceValues(bitboards) - blackPieceValues(bitboards) > PieceValue.getValue(Piece.BISHOP)

fun noBlackRooksQueensOrBishops(bitboards: BitboardData) =
        bitboards.blackRooks or bitboards.blackKnights or bitboards.blackQueens == 0L

fun bothSidesHaveOnlyOneKnightOrBishopEach(bitboards: BitboardData) =
        noPawnsRemain(bitboards) && whitePieceValues(bitboards) < PieceValue.getValue(Piece.ROOK) &&
                blackPieceValues(bitboards) < PieceValue.getValue(Piece.ROOK)

fun noPawnsRemain(bitboards: BitboardData) =
        whitePawnValues(bitboards) + blackPawnValues(bitboards) == 0

fun noWhiteRooksQueensOrKnights(bitboards: BitboardData) =
        bitboards.whiteRooks or bitboards.whiteKnights or bitboards.whiteQueens == 0L

fun blackBishopDrawOnFileH(bitboards: BitboardData): Boolean {
    return bitboards.blackPawns and Bitboards.FILE_H.inv() == 0L &&
            bitboards.blackBishops and Bitboards.LIGHT_SQUARES == 0L &&
            bitboards.whiteKing and Bitboards.H1H2G1G2 != 0L
}

fun blackBishopDrawOnFileA(bitboards: BitboardData): Boolean {
    return bitboards.blackPawns and Bitboards.FILE_A.inv() == 0L &&
            bitboards.blackBishops and Bitboards.DARK_SQUARES == 0L &&
            bitboards.whiteKing and Bitboards.A1A2B1B2 != 0L
}

fun whiteBishopDrawOnFileA(bitboards: BitboardData): Boolean {
    return bitboards.whitePawns and Bitboards.FILE_A.inv() == 0L &&
            bitboards.whiteBishops and Bitboards.LIGHT_SQUARES == 0L &&
            bitboards.blackKing and Bitboards.A8A7B8B7 != 0L
}

fun whiteBishopDrawOnFileH(bitboards: BitboardData): Boolean {
    return bitboards.whitePawns and Bitboards.FILE_H.inv() == 0L &&
            bitboards.whiteBishops and Bitboards.DARK_SQUARES == 0L &&
            bitboards.blackKing and Bitboards.H8H7G8G7 != 0L
}

fun probableDrawWhenBlackIsWinning(bitboards: BitboardData) =
        blackPawnValues(bitboards) == 0 && blackPieceValues(bitboards) - PieceValue.getValue(Piece.BISHOP) <= whitePieceValues(bitboards)

fun probablyDrawWhenWhiteIsWinning(bitboards: BitboardData) =
        whitePawnValues(bitboards) == 0 && whitePieceValues(bitboards) - PieceValue.getValue(Piece.BISHOP) <= blackPieceValues(bitboards)

fun blackHasInsufficientMaterial(bitboards: BitboardData) =
        blackPawnValues(bitboards) == 0 && (blackPieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) ||
                blackPieceValues(bitboards) == PieceValue.getValue(Piece.BISHOP))

fun whiteHasInsufficientMaterial(bitboards: BitboardData) =
        whitePawnValues(bitboards) == 0 && (whitePieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) ||
                whitePieceValues(bitboards) == PieceValue.getValue(Piece.BISHOP))

fun whitePieceValues(bitboards: BitboardData) =
        bitCount(bitboards.whiteKnights) * PieceValue.getValue(Piece.KNIGHT) +
                bitCount(bitboards.whiteRooks) * PieceValue.getValue(Piece.ROOK) +
                bitCount(bitboards.whiteBishops) * PieceValue.getValue(Piece.BISHOP) +
                bitCount(bitboards.whiteQueens) * PieceValue.getValue(Piece.QUEEN)

fun blackPieceValues(bitboards: BitboardData) =
        bitCount(bitboards.blackKnights) * PieceValue.getValue(Piece.KNIGHT) +
                bitCount(bitboards.blackRooks) * PieceValue.getValue(Piece.ROOK) +
                bitCount(bitboards.blackBishops) * PieceValue.getValue(Piece.BISHOP) +
                bitCount(bitboards.blackQueens) * PieceValue.getValue(Piece.QUEEN)

fun whitePawnValues(bitboards: BitboardData): Int {
    return bitCount(bitboards.whitePawns) * PieceValue.getValue(Piece.PAWN)
}

fun blackPawnValues(bitboards: BitboardData): Int {
    return bitCount(bitboards.blackPawns) * PieceValue.getValue(Piece.PAWN)
}

fun whiteKingSquare(bitboards: BitboardData) = numberOfTrailingZeros(bitboards.whiteKing)

fun blackKingSquare(bitboards: BitboardData) = numberOfTrailingZeros(bitboards.blackKing)

fun blockedKnightPenaltyEval(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        bitCount(blockedKnightLandingSquares(square, enemyPawnAttacks, friendlyPawns)) * Evaluation.KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY.value

fun blockedKnightLandingSquares(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        Bitboards.knightMoves[square] and (enemyPawnAttacks or friendlyPawns)

fun whitePawnsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData): Int {
    return pieceSquareLists.whitePawns.asSequence().map {
        linearScale(
                blackPieceValues(bitboards),
                Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
                Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.pawnEndGame[it],
                PieceSquareTables.pawn[it]
        )
    }.fold(0) { acc, i -> acc + i }
}

fun blackPawnsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData): Int {
    return pieceSquareLists.blackPawns.asSequence().map {
        linearScale(
                whitePieceValues(bitboards),
                Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
                Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.pawnEndGame[Bitboards.bitFlippedHorizontalAxis[it]],
                PieceSquareTables.pawn[Bitboards.bitFlippedHorizontalAxis[it]]
        )
    }.fold(0) { acc, i -> acc + i }
}

fun blackKnightsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData): Int {
    return pieceSquareLists.blackKnights.asSequence().map {
        linearScale(
                whitePieceValues(bitboards) + whitePawnValues(bitboards),
                Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
                Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.knightEndGame[Bitboards.bitFlippedHorizontalAxis[it]],
                PieceSquareTables.knight[Bitboards.bitFlippedHorizontalAxis[it]]
        ) -
        blockedKnightPenaltyEval(it, whitePawnAttacks(bitboards.whitePawns), bitboards.blackPawns)
    }.fold(0) { acc, i -> acc + i }
}

fun whiteKnightsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData): Int {
    return pieceSquareLists.whiteKnights.asSequence().map {
        linearScale(blackPieceValues(bitboards) + blackPawnValues(bitboards),
                Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
                Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.knightEndGame[it],
                PieceSquareTables.knight[it]
        ) -
        blockedKnightPenaltyEval(it, blackPawnAttacks(bitboards.blackPawns), bitboards.whitePawns)
    }.fold(0) { acc, i -> acc + i }
}

fun blackBishopsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, blackPieces: Long): Int {
    return pieceSquareLists.blackBishops.asSequence().map {
        Evaluation.getBishopMobilityValue(bitCount(bishopAttacks(bitboards, it) and blackPieces.inv())) +
                flippedSquareTableScore(PieceSquareTables.bishop, it)
    }.fold(0) { acc, i -> acc + i }
}

fun whiteBishopEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, whitePieces: Long): Int {
    return pieceSquareLists.whiteBishops.asSequence().map {
        Evaluation.getBishopMobilityValue(bitCount(bishopAttacks(bitboards, it) and whitePieces.inv())) +
                PieceSquareTables.bishop[it]
    }.fold(0) { acc, i -> acc + i }
}

private fun blackQueensEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, blackPieces: Long): Int {
    return pieceSquareLists.blackQueens.asSequence().map {
        Evaluation.getQueenMobilityValue(bitCount(queenAttacks(bitboards, it) and blackPieces.inv())) +
                flippedSquareTableScore(PieceSquareTables.queen, it)
    }.fold(0) { acc, i -> acc + i }
}

private fun whiteQueensEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, whitePieces: Long): Int {
    return pieceSquareLists.whiteQueens.asSequence().map {
        Evaluation.getQueenMobilityValue(bitCount(queenAttacks(bitboards, it) and whitePieces.inv())) +
                PieceSquareTables.queen[it]
    }.fold(0) { acc, i -> acc + i }
}

private fun blackRooksEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, blackPieces: Long): Int {
    return pieceSquareLists.blackRooks.asSequence().map {
        blackRookOpenFilesEval(bitboards, it % 8) +
                Evaluation.getRookMobilityValue(bitCount(rookAttacks(bitboards, it) and blackPieces.inv())) +
                flippedSquareTableScore(PieceSquareTables.rook, it) * rookEnemyPawnMultiplier(whitePawnValues(bitboards)) / 6
    }.fold(0) { acc, i -> acc + i }
}

fun whiteRooksEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, whitePieces: Long): Int {
    return pieceSquareLists.whiteRooks.asSequence().map {
        whiteRookOpenFilesEval(bitboards, it % 8) +
                Evaluation.getRookMobilityValue(bitCount(rookAttacks(bitboards, it) and whitePieces.inv())) +
                PieceSquareTables.rook[it] * rookEnemyPawnMultiplier(blackPawnValues(bitboards)) / 6
    }.fold(0) { acc, i -> acc + i }
}

fun pawnScore(whitePawnBitboard: Long,
                     blackPawnBitboard: Long,
                     attackLists: AttackLists,
                     materialValues: MaterialValues,
                     whiteKingSquare: Int,
                     blackKingSquare: Int,
                     mover: Colour): Int {

    val whitePawnAttacks = attackLists.whitePawns
    val blackPawnAttacks = attackLists.blackPawns

    val whitePawnFiles = getPawnFiles(whitePawnBitboard)
    val blackPawnFiles = getPawnFiles(blackPawnBitboard)

    val whitePassedPawnsBitboard = getWhitePassedPawns(whitePawnBitboard, blackPawnBitboard)
    val whiteGuardedPassedPawns = whitePassedPawnsBitboard and whitePawnAttacks
    val blackPassedPawnsBitboard = getBlackPassedPawns(whitePawnBitboard, blackPawnBitboard)
    val blackGuardedPassedPawns = blackPassedPawnsBitboard and blackPawnAttacks

    val whiteIsolatedPawns = whitePawnFiles and (whitePawnFiles shl 1).inv() and (whitePawnFiles ushr 1).inv()
    val blackIsolatedPawns = blackPawnFiles and (blackPawnFiles shl 1).inv() and (blackPawnFiles ushr 1).inv()
    val whiteOccupiedFileMask = southFill(whitePawnBitboard, 8) and Bitboards.RANK_1
    val blackOccupiedFileMask = southFill(blackPawnBitboard, 8) and Bitboards.RANK_1

    val whitePassedPawnScore = bitCount(whiteGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.value +
            squareList(whitePassedPawnsBitboard).asSequence()
                    .map { Evaluation.getPassedPawnBonus(yCoordOfSquare(it)) }.fold(0) { acc, i -> acc + i }

    val blackPassedPawnScore = bitCount(blackGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.value +
            squareList(blackPassedPawnsBitboard).asSequence()
                    .map { Evaluation.getPassedPawnBonus(7 - yCoordOfSquare(it)) }.fold(0) { acc, i -> acc + i }

    return bitCount(blackIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.value -
            bitCount(whiteIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.value -
            (if (whiteIsolatedPawns and Bitboards.FILE_D != 0L) Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.value else 0) +
            (if (blackIsolatedPawns and Bitboards.FILE_D != 0L) Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.value else 0) -
            (bitCount(
                    whitePawnBitboard and
                            (whitePawnBitboard or blackPawnBitboard ushr 8).inv() and
                            (blackPawnAttacks ushr 8) and
                            northFill(whitePawnAttacks, 8).inv() and
                            blackPawnAttacks(whitePawnBitboard) and
                            northFill(blackPawnFiles, 8).inv()
            ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.value) +
            (bitCount(
                    blackPawnBitboard and
                            (blackPawnBitboard or whitePawnBitboard shl 8).inv() and
                            (whitePawnAttacks shl 8) and
                            southFill(blackPawnAttacks, 8).inv() and
                            whitePawnAttacks(blackPawnBitboard) and
                            northFill(whitePawnFiles, 8).inv()
            ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.value) -
            ((bitCount(whitePawnBitboard and Bitboards.FILE_A) + bitCount(whitePawnBitboard and Bitboards.FILE_H))
                    * Evaluation.VALUE_SIDE_PAWN_PENALTY.value) +
            ((bitCount(blackPawnBitboard and Bitboards.FILE_A) + bitCount(blackPawnBitboard and Bitboards.FILE_H))
                    * Evaluation.VALUE_SIDE_PAWN_PENALTY.value) -
            Evaluation.VALUE_DOUBLED_PAWN_PENALTY.value * (materialValues.whitePawns / 100 - bitCount(whiteOccupiedFileMask)) -
            bitCount(whiteOccupiedFileMask.inv() ushr 1 and whiteOccupiedFileMask) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.value +
            Evaluation.VALUE_DOUBLED_PAWN_PENALTY.value * (materialValues.blackPawns / 100 - bitCount(blackOccupiedFileMask)) +
            bitCount(blackOccupiedFileMask.inv() ushr 1 and blackOccupiedFileMask) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.value +
            (linearScale(materialValues.blackPieces, 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.value, whitePassedPawnScore * 2, whitePassedPawnScore)) -
            (linearScale(materialValues.whitePieces, 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.value, blackPassedPawnScore * 2, blackPassedPawnScore)) +
            (if (materialValues.blackPieces < Evaluation.PAWN_ADJUST_MAX_MATERIAL.value)
                calculateLowMaterialPawnBonus(
                        Colour.BLACK,
                        whiteKingSquare,
                        blackKingSquare,
                        materialValues,
                        whitePassedPawnsBitboard,
                        blackPassedPawnsBitboard,
                        mover)
            else 0) +
            (if (materialValues.whitePieces < Evaluation.PAWN_ADJUST_MAX_MATERIAL.value)
                calculateLowMaterialPawnBonus(
                        Colour.WHITE,
                        whiteKingSquare,
                        blackKingSquare,
                        materialValues,
                        whitePassedPawnsBitboard,
                        blackPassedPawnsBitboard,
                        mover)
            else 0)
}

fun calculateLowMaterialPawnBonus(
        lowMaterialColour: Colour,
        whiteKingSquare: Int,
        blackKingSquare: Int,
        materialValues: MaterialValues,
        whitePassedPawnsBitboard: Long,
        blackPassedPawnsBitboard: Long,
        mover: Colour
): Int {

    val kingSquare = if (lowMaterialColour == Colour.WHITE) whiteKingSquare else blackKingSquare
    val kingX = xCoordOfSquare(kingSquare)
    val kingY = yCoordOfSquare(kingSquare)
    val lowMaterialSidePieceValues = if (lowMaterialColour == Colour.WHITE) materialValues.whitePieces else materialValues.blackPieces

    return squareList(if (lowMaterialColour == Colour.WHITE) blackPassedPawnsBitboard else whitePassedPawnsBitboard)
            .asSequence().map {
                val pawnDistance = Math.min(5, pawnDistanceFromPromotion(lowMaterialColour, it))
                val kingXDistanceFromPawn = difference(kingX, it)
                val kingYDistanceFromPawn = difference(colourAdjustedYRank(lowMaterialColour, kingY), it)
                val kingDistanceFromPawn = Math.max(kingXDistanceFromPawn, kingYDistanceFromPawn)

                val moverAdjustment = if (lowMaterialColour == mover) 1 else 0

                val scoreAdjustment = linearScale(
                        lowMaterialSidePieceValues,
                        0,
                        Evaluation.PAWN_ADJUST_MAX_MATERIAL.value,
                        kingDistanceFromPawn * 4,
                        0) +
                        if (pawnDistance < kingDistanceFromPawn - moverAdjustment && lowMaterialSidePieceValues == 0) {
                            Evaluation.VALUE_KING_CANNOT_CATCH_PAWN.value
                        } else 0

                if (lowMaterialColour == Colour.WHITE) -scoreAdjustment else scoreAdjustment
            }.fold(0) { acc, i -> acc + i }

}

private fun colourAdjustedYRank(colour: Colour, yRank: Int) =
        if (colour == Colour.WHITE) yRank else Math.abs(yRank - 7)

private fun difference(kingX: Int, it: Int) = Math.abs(kingX - xCoordOfSquare(it))

private fun xCoordOfSquare(it: Int) = it % 8

private fun pawnDistanceFromPromotion(colour: Colour, square: Int) =
        if (colour == Colour.WHITE) yCoordOfSquare(square) else 7 - yCoordOfSquare(square)

private fun yCoordOfSquare(kingSquare: Int) = kingSquare / 8

fun evaluate(board: EngineChessBoard) : Int {

    val bitboards = initBitboardData(board)
    val pieceSquareLists = initPieceSquareLists(bitboards)
    val attackLists = initAttackLists(bitboards, pieceSquareLists)
    val materialValues = initMaterialValues(bitboards)

    if (onlyKingsRemain(bitboards)) {
        return 0
    } else {
        val whitePieces = if (board.mover == Colour.WHITE) bitboards.friendly else bitboards.enemy
        val blackPieces = if (board.mover == Colour.WHITE) bitboards.enemy else bitboards.friendly

        val materialDifference = materialDifferenceEval(materialValues)

        val eval =  materialDifference +
                    (twoWhiteRooksTrappingKingEval(bitboards) - twoBlackRooksTrappingKingEval(bitboards)) +
                    (doubledRooksEval(pieceSquareLists.whiteRooks) - doubledRooksEval(pieceSquareLists.blackRooks)) +
                    pawnScore(bitboards.whitePawns,
                            bitboards.blackPawns,
                            attackLists, materialValues,
                            board.whiteKingSquare,
                            board.blackKingSquare,
                            board.mover) +
                    (whiteBishopEval(pieceSquareLists, bitboards, whitePieces) - blackBishopsEval(pieceSquareLists, bitboards, blackPieces)) +
                    (whiteKnightsEval(pieceSquareLists, bitboards) - blackKnightsEval(pieceSquareLists, bitboards)) +
                    tradePawnBonusWhenMoreMaterial(bitboards, materialDifference) +
                    tradePieceBonusWhenMoreMaterial(bitboards, materialDifference) +
                    (whiteKingSquareEval(bitboards) - blackKingSquareEval(bitboards) ) +
                    (whitePawnsEval(pieceSquareLists, bitboards) - blackPawnsEval(pieceSquareLists, bitboards)) +
                    (whiteRooksEval(pieceSquareLists, bitboards, whitePieces) - blackRooksEval(pieceSquareLists, bitboards, blackPieces)) +
                    (whiteQueensEval(pieceSquareLists, bitboards, whitePieces) - blackQueensEval(pieceSquareLists, bitboards, blackPieces)) +
                    castlingEval(bitboards, board.castlePrivileges) +
                    bishopScore(bitboards, materialDifference) +
                    threatEval(bitboards, attackLists, board.squareOccupants) +
                    kingSafetyEval(bitboards, attackLists, board)

        val endGameAdjustedScore = if (isEndGame(bitboards)) endGameAdjustment(bitboards, eval) else eval

        return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore
    }
}

fun scoreRightWayPositions(board: EngineChessBoard, h1: Int, h2: Int, h3: Int, g2: Int, g3: Int, f1: Int, f2: Int, f3: Int, f4: Int, isWhite: Boolean, cornerColour: Int): Int {
    var safety = 0
    val offset = if (isWhite) 0 else 6
    if (board.allPiecesBitboard and (1L shl h1) != 0L ||
            board.getBitboardByIndex(SquareOccupant.WR.index + offset) and (1L shl f1) == 0L) {
        return 0
    }
    safety = if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl f2) != 0L) {
        if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl g2) != 0L) {
            checkForPositionsAOrD(board, h2, h3, f3, isWhite, cornerColour, safety)
        } else {
            checkForPositionsBOrC(board, h2, h3, g2, g3, isWhite, safety)
        }
    } else {
        if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl f4) != 0L) {
            checkForPositionE(board, h2, g2, f3, isWhite, safety)
        } else {
            checkForPositionFOrH(board, h2, h3, g2, f3, isWhite, safety)
        }
    }
    return safety / 4
}

fun checkForPositionFOrH(board: EngineChessBoard, h2: Int, h3: Int, g2: Int, f3: Int, isWhite: Boolean, safety: Int): Int {
    var safety = safety
    val offset = if (isWhite) 0 else 6
    if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl f3) != 0L
            && board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl g2) != 0L) {
        if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h2) != 0L) {
            // (F)
            safety -= 10
        } else {
            if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h3) != 0L) {
                // (H)
                safety -= 30
            }
        }
    }
    return safety
}

fun checkForPositionE(board: EngineChessBoard, h2: Int, g2: Int, f3: Int, isWhite: Boolean, safety: Int): Int {
    var safety = safety
    val offset = if (isWhite) 0 else 6
    if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl g2) != 0L
            && board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h2) != 0L) {
        // (E)
        safety += 80
        if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h2) != 0L
                && board.getBitboardByIndex(SquareOccupant.WN.index + offset) and (1L shl f3) != 0L) {
            safety += 40
        }
    }
    return safety
}

fun checkForPositionsBOrC(board: EngineChessBoard, h2: Int, h3: Int, g2: Int, g3: Int, isWhite: Boolean, safety: Int): Int {
    var safety = safety
    val offset = if (isWhite) 0 else 6
    if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl g3) != 0L) {
        if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h2) != 0L) {
            if (board.getBitboardByIndex(SquareOccupant.WB.index + offset) and (1L shl g2) != 0L) {
                // (B)
                safety += 100
            }
        } else {
            if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h3) != 0L
                    && board.getBitboardByIndex(SquareOccupant.WB.index + offset) and (1L shl g2) != 0L) {
                // (C)
                safety += 70
            }
        }
    }
    return safety
}

fun checkForPositionsAOrD(board: EngineChessBoard, h2: Int, h3: Int, f3: Int, isWhite: Boolean, cornerColour: Int, safety: Int): Int {
    var safety = safety
    val offset = if (isWhite) 0 else 6
    if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h2) != 0L) {
        // (A)
        safety += 120
    } else {
        safety = checkForPositionD(board, h3, f3, isWhite, cornerColour, safety)
    }
    return safety
}

fun checkForPositionD(board: EngineChessBoard, h3: Int, f3: Int, isWhite: Boolean, cornerColour: Int, safety: Int): Int {
    var safety = safety
    val offset = if (isWhite) 0 else 6
    if (board.getBitboardByIndex(SquareOccupant.WP.index + offset) and (1L shl h3) != 0L
            && board.getBitboardByIndex(SquareOccupant.WN.index + offset) and (1L shl f3) != 0L) {
        // (D)
        safety += 70
        // check for bishop of same colour as h3
        val bits = if (cornerColour == Colour.WHITE.value.toInt()) Bitboards.LIGHT_SQUARES else Bitboards.DARK_SQUARES
        if (bits and board.getBitboardByIndex(SquareOccupant.WB.index + offset) != 0L) {
            safety -= 30
        }
    }
    return safety
}

fun getWhiteKingRightWayScore(engineChessBoard: EngineChessBoard): Int {
    return if (engineChessBoard.whiteKingSquare == 1 || engineChessBoard.whiteKingSquare == 8) {
        scoreRightWayPositions(engineChessBoard,
                0, 8, 16, 9, 17, 2, 10, 18, 26, true,
                Colour.WHITE.value.toInt())
    } else 0
}

fun getBlackKingRightWayScore(engineChessBoard: EngineChessBoard): Int {
    return if (engineChessBoard.blackKingSquare == 57 || engineChessBoard.blackKingSquare == 48) {
        scoreRightWayPositions(engineChessBoard,
                56, 48, 40, 49, 41, 58, 50, 42, 34, false,
                Colour.BLACK.value.toInt())
    } else 0
}