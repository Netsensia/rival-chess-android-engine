package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.engine.core.EngineChessBoard
import com.netsensia.rivalchess.model.Piece

fun onlyKingRemains(board: EngineChessBoard) : Boolean =
        board.getWhitePieceValues() +
        board.getBlackPieceValues() +
        board.getWhitePawnValues() +
        board.getBlackPawnValues() == 0;

fun whitePawnPieceSquareEval(board: EngineChessBoard) : Int {
    var pieceSquareTemp = 0
    var pieceSquareTempEndGame = 0
    var bitboard: Long
    var sq: Int

    bitboard = board.whitePawnBitboard
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        pieceSquareTemp += PieceSquareTables.pawn[sq]
        pieceSquareTempEndGame += PieceSquareTables.pawnEndGame[sq]
    }

    return linearScale(
            board.blackPieceValues,
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pieceSquareTempEndGame,
            pieceSquareTemp
    )
}

fun blackPawnPieceSquareEval(board: EngineChessBoard) : Int {
    var pieceSquareTemp = 0
    var pieceSquareTempEndGame = 0
    var bitboard: Long
    var sq: Int

    bitboard = board.blackPawnBitboard
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        pieceSquareTemp += PieceSquareTables.pawn[Bitboards.bitFlippedHorizontalAxis[sq]]
        pieceSquareTempEndGame += PieceSquareTables.pawnEndGame[Bitboards.bitFlippedHorizontalAxis[sq]]
    }

    return linearScale(
            board.whitePieceValues,
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pieceSquareTempEndGame,
            pieceSquareTemp
    )
}

fun whiteKingSquareEval(board: EngineChessBoard) : Int =
        linearScale(
                board.getBlackPieceValues(),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                PieceSquareTables.kingEndGame.get(board.getWhiteKingSquare()),
                PieceSquareTables.king.get(board.getWhiteKingSquare())
        )

fun blackKingSquareEval(board: EngineChessBoard) : Int =
        linearScale(
                board.getWhitePieceValues(),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                PieceSquareTables.kingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(board.getBlackKingSquare())),
                PieceSquareTables.king.get(Bitboards.bitFlippedHorizontalAxis.get(board.getBlackKingSquare()))
        )

fun linearScale(situation: Int, ref1: Int, ref2: Int, score1: Int, score2: Int): Int {
    if (situation < ref1) return score1
    return if (situation > ref2) score2 else (situation - ref1) * (score2 - score1) / (ref2 - ref1) + score1
}