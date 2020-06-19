package com.netsensia.rivalchess.engine.type

import com.netsensia.rivalchess.consts.BITBOARD_NONE

class MoveDetail {
	@JvmField
	var movePiece = BITBOARD_NONE

	@JvmField
	var capturePiece = BITBOARD_NONE

	@JvmField
	var move = 0

	@JvmField
	var halfMoveCount: Byte = 0

	@JvmField
	var castlePrivileges: Byte = 0

	@JvmField
	var enPassantBitboard: Long = 0

	@JvmField
	var hashValue: Long = 0

	@JvmField
	var isOnNullMove = false
}