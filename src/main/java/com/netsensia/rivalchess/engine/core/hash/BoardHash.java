package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.eval.PawnHashEntry;

public class BoardHash {

    private long lastPawnHashValue = -1;

    private PawnHashEntry lastPawnHashEntry = new PawnHashEntry();

    private int hashTableVersion;
    private int[] hashTableUseHeight;
    private int[] hashTableIgnoreHeight;
    private long[] pawnHashTable;
    public int maxHashEntries;
    private int maxPawnHashEntries;
    private int lastHashSizeCreated;

    public int getHashTableUseHeight(int index) {
        return hashTableUseHeight[index];
    }

    public int getHashTableIgnoreHeight(int index) {
        return hashTableIgnoreHeight[index];
    }

    public void setHashTableUseHeight(int index, int value) {
        hashTableUseHeight[index] = value;
    }

    public void setHashTableIgnoreHeight(int index, int value) {
        hashTableIgnoreHeight[index] = value;
    }

    public long getPawnHashTable(int index) {
        return pawnHashTable[index];
    }

    public void setPawnHashTable(int index, long value) {
        pawnHashTable[index] = value;
    }

    public synchronized void clearHash() {
        for (int i = 0; i < maxHashEntries; i++) {
            this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
            this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
            this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
            this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
            if (RivalConstants.USE_PAWN_HASH) {
                this.pawnHashTable[i * RivalConstants.NUM_PAWNHASH_FIELDS + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
            }
        }
    }

    public synchronized void setHashTable() {
        if (maxHashEntries != lastHashSizeCreated) {
            this.hashTableUseHeight = new int[maxHashEntries * RivalConstants.NUM_HASH_FIELDS];
            this.hashTableIgnoreHeight = new int[maxHashEntries * RivalConstants.NUM_HASH_FIELDS];
            if (RivalConstants.USE_PAWN_HASH) {
                this.pawnHashTable = new long[maxHashEntries * RivalConstants.NUM_PAWNHASH_FIELDS];
            }
            lastHashSizeCreated = maxHashEntries;
            for (int i = 0; i < maxHashEntries; i++) {
                this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
                this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_VERSION] = 1;
                this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
                this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_VERSION] = 1;
                if (RivalConstants.USE_PAWN_HASH) {
                    this.pawnHashTable[i * RivalConstants.NUM_PAWNHASH_FIELDS + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
                }
            }
        }
    }

    public void storeHashMove(int move, EngineChessBoard board, int score, byte flag, int height) {
        int hashIndex = (int) (BoardHashHelper.getHash(board) % maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;

        if (height >= this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] || hashTableVersion > this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
            if (hashTableVersion == this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
                // move this entry to the always replace table, but not if it is an entry from a previous search
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_MOVE] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_MOVE];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_SCORE];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_FLAG];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION];
            }

            if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
                for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
                    if (hashTableVersion == this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
                        this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i];
                        this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12];
                    }
                    this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = (int) (board.getBitboardByIndex(i) >>> 32);
                    this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = (int) (board.getBitboardByIndex(i) & Bitboards.LOW32);
                }
            }

            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_MOVE] = move;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] = score;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = flag;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] = (int) (BoardHashHelper.getHash(board) >>> 32);
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] = (int) (BoardHashHelper.getHash(board) & Bitboards.LOW32);
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = height;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = hashTableVersion;
        } else {
            if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
                for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
                    this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = (int) (board.getBitboardByIndex(i) >>> 32);
                    this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = (int) (board.getBitboardByIndex(i) & Bitboards.LOW32);
                }
            }
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_MOVE] = move;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] = score;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = flag;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] = (int) (BoardHashHelper.getHash(board) >>> 32);
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] = (int) (BoardHashHelper.getHash(board) & Bitboards.LOW32);
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = height;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = hashTableVersion;
        }
    }

    public int getPawnHashIndex(EngineChessBoard engineChessBoard) {
        return (int) (BoardHashHelper.getPawnHash(engineChessBoard) % this.maxPawnHashEntries) * RivalConstants.NUM_PAWNHASH_FIELDS;
    }

    public int getHashIndex(EngineChessBoard engineChessBoard) {
        return (int) (BoardHashHelper.getHash(engineChessBoard) % maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;
    }

    public long getHash(EngineChessBoard engineChessBoard) {
        return BoardHashHelper.getHash(engineChessBoard);
    }

    public PawnHashEntry getPawnHashEntry(EngineChessBoard board) {
        PawnHashEntry pawnHashEntry;

        final long pawnHashValue = BoardHashHelper.getPawnHash(board);
        final int pawnHashIndex = getPawnHashIndex(board);

        if (RivalConstants.USE_PAWN_HASH) {
            if (RivalConstants.USE_QUICK_PAWN_HASH_RETURN && lastPawnHashValue == pawnHashValue) {
                pawnHashEntry = new PawnHashEntry(lastPawnHashEntry);
            } else {
                pawnHashEntry = getPawnStatsFromHash(pawnHashValue, pawnHashIndex);
            }
        } else {
            pawnHashEntry = new PawnHashEntry();
        }

        if (!pawnHashEntry.isPopulated()) {
            final long whitePawnAttacks = Bitboards.getWhitePawnAttacks(board.getWhitePawnBitboard());
            final long blackPawnAttacks = Bitboards.getBlackPawnAttacks(board.getBlackPawnBitboard());
            final long whitePawnFiles = Bitboards.getPawnFiles(board.getWhitePawnBitboard());
            final long blackPawnFiles = Bitboards.getPawnFiles(board.getBlackPawnBitboard());

            pawnHashEntry.setPawnScore(0);

            pawnHashEntry.setWhitePassedPawnsBitboard(Bitboards.getWhitePassedPawns(board.getWhitePawnBitboard(), board.getBlackPawnBitboard()));

            final long whiteGuardedPassedPawns = pawnHashEntry.getWhitePassedPawnsBitboard() & (Bitboards.getWhitePawnAttacks(board.getWhitePawnBitboard()));

            pawnHashEntry.setBlackPassedPawnsBitboard(Bitboards.getBlackPassedPawns(board.getWhitePawnBitboard(), board.getBlackPawnBitboard()));

            long blackGuardedPassedPawns = pawnHashEntry.getBlackPassedPawnsBitboard() & (Bitboards.getBlackPawnAttacks(board.getBlackPawnBitboard()));

            pawnHashEntry.setWhitePassedPawnScore(Long.bitCount(whiteGuardedPassedPawns) * RivalConstants.VALUE_GUARDED_PASSED_PAWN);
            pawnHashEntry.setBlackPassedPawnScore(Long.bitCount(blackGuardedPassedPawns) * RivalConstants.VALUE_GUARDED_PASSED_PAWN);

            final long whiteIsolatedPawns = whitePawnFiles & ~(whitePawnFiles << 1) & ~(whitePawnFiles >>> 1);
            final long blackIsolatedPawns = blackPawnFiles & ~(blackPawnFiles << 1) & ~(blackPawnFiles >>> 1);
            pawnHashEntry.decPawnScore(Long.bitCount(whiteIsolatedPawns) * RivalConstants.VALUE_ISOLATED_PAWN_PENALTY);
            pawnHashEntry.incPawnScore(Long.bitCount(blackIsolatedPawns) * RivalConstants.VALUE_ISOLATED_PAWN_PENALTY);

            if ((whiteIsolatedPawns & Bitboards.FILE_D) != 0) {
                pawnHashEntry.decPawnScore(RivalConstants.VALUE_ISOLATED_DPAWN_PENALTY);
            }
            if ((blackIsolatedPawns & Bitboards.FILE_D) != 0) {
                pawnHashEntry.incPawnScore(RivalConstants.VALUE_ISOLATED_DPAWN_PENALTY);
            }

            pawnHashEntry.decPawnScore(
                    Long.bitCount(
                            board.getWhitePawnBitboard() &
                                    ~((board.getWhitePawnBitboard() | board.getBlackPawnBitboard()) >>> 8) &
                                    (blackPawnAttacks >>> 8) &
                                    ~Bitboards.northFill(whitePawnAttacks) &
                                    (Bitboards.getBlackPawnAttacks(board.getWhitePawnBitboard())) &
                                    ~Bitboards.northFill(blackPawnFiles)
                    ) * RivalConstants.VALUE_BACKWARD_PAWN_PENALTY);

            pawnHashEntry.incPawnScore(Long.bitCount(
                    board.getBlackPawnBitboard() &
                            ~((board.getBlackPawnBitboard() | board.getWhitePawnBitboard()) << 8) &
                            (whitePawnAttacks << 8) &
                            ~Bitboards.southFill(blackPawnAttacks) &
                            (Bitboards.getWhitePawnAttacks(board.getBlackPawnBitboard())) &
                            ~Bitboards.northFill(whitePawnFiles)
            ) * RivalConstants.VALUE_BACKWARD_PAWN_PENALTY);

            int sq;
            long bitboard = pawnHashEntry.getWhitePassedPawnsBitboard();
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                pawnHashEntry.addWhitePassedPawnScore(RivalConstants.VALUE_PASSED_PAWN_BONUS[sq / 8]);
            }

            bitboard = pawnHashEntry.getBlackPassedPawnsBitboard();
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                pawnHashEntry.addBlackPassedPawnScore(RivalConstants.VALUE_PASSED_PAWN_BONUS[7 - (sq / 8)]);
            }

            pawnHashEntry.decPawnScore(
                    (Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_H))
                            * RivalConstants.VALUE_SIDE_PAWN_PENALTY);

            pawnHashEntry.incPawnScore(
                    (Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_H))
                            * RivalConstants.VALUE_SIDE_PAWN_PENALTY);

            long occupiedFileMask = Bitboards.southFill(board.getWhitePawnBitboard()) & Bitboards.RANK_1;
            pawnHashEntry.decPawnScore(RivalConstants.VALUE_DOUBLED_PAWN_PENALTY * ((board.getWhitePawnValues() / 100) - Long.bitCount(occupiedFileMask)));
            pawnHashEntry.decPawnScore(Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * RivalConstants.VALUE_PAWN_ISLAND_PENALTY);

            occupiedFileMask = Bitboards.southFill(board.getBlackPawnBitboard()) & Bitboards.RANK_1;
            pawnHashEntry.incPawnScore(RivalConstants.VALUE_DOUBLED_PAWN_PENALTY * ((board.getBlackPawnValues() / 100) - Long.bitCount(occupiedFileMask)));
            pawnHashEntry.incPawnScore(Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * RivalConstants.VALUE_PAWN_ISLAND_PENALTY);

            if (RivalConstants.USE_PAWN_HASH) {
                setPawnHashTable(pawnHashIndex + RivalConstants.PAWNHASHENTRY_MAIN_SCORE, pawnHashEntry.getPawnScore());
                setPawnHashTable(pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE, pawnHashEntry.getWhitePassedPawnScore());
                setPawnHashTable(pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE, pawnHashEntry.getBlackPassedPawnScore());
                setPawnHashTable(pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWNS, pawnHashEntry.getWhitePassedPawnsBitboard());
                setPawnHashTable(pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWNS, pawnHashEntry.getBlackPassedPawnsBitboard());
                setPawnHashTable(pawnHashIndex + RivalConstants.PAWNHASHENTRY_LOCK, BoardHashHelper.getPawnHash(board));
            }
        }

        setLastPawnHashValueAndEntry(board, pawnHashEntry);

        return pawnHashEntry;
    }

    private PawnHashEntry getPawnStatsFromHash(long pawnHashValue, int pawnHashIndex) {
        PawnHashEntry pawnHashEntry = new PawnHashEntry();

        if (this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_LOCK] == pawnHashValue) {
            pawnHashEntry.setPawnScore((int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_MAIN_SCORE]);
            if (pawnHashEntry.getPawnScore() != RivalConstants.PAWNHASH_DEFAULT_SCORE) {
                pawnHashEntry.setWhitePassedPawnScore((int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE]);
                pawnHashEntry.setBlackPassedPawnScore((int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE]);
                pawnHashEntry.setWhitePassedPawnsBitboard(this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWNS]);
                pawnHashEntry.setBlackPassedPawnsBitboard(this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWNS]);
            }
        }

        return pawnHashEntry;
    }

    public void setLastPawnHashValueAndEntry(EngineChessBoard board, PawnHashEntry pawnHashEntry) {
        if (RivalConstants.USE_QUICK_PAWN_HASH_RETURN) {
            setLastPawnHashValue(BoardHashHelper.getPawnHash(board));
            setLastPawnHashEntry(new PawnHashEntry(pawnHashEntry));
        }
    }

    public synchronized void setHashSizeMB(int hashSizeMB) {
        if (hashSizeMB < 1) {
            setMaxHashEntries(1);
            setMaxPawnHashEntries(1);
        } else {
            int mainHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 6; // two of these
            int pawnHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 2; // one of these
            setMaxHashEntries(mainHashTableSize / RivalConstants.HASHPOSITION_SIZE_BYTES);
            setMaxPawnHashEntries(pawnHashTableSize / RivalConstants.PAWNHASHENTRY_SIZE_BYTES);
        }

        setHashTable();
    }

    public int getHashTableVersion() {
        return hashTableVersion;
    }

    public void setHashTableVersion(int hashTableVersion) {
        this.hashTableVersion = hashTableVersion;
    }

    public int getMaxHashEntries() {
        return maxHashEntries;
    }

    public void setMaxHashEntries(int maxHashEntries) {
        this.maxHashEntries = maxHashEntries;
    }

    public int getMaxPawnHashEntries() {
        return maxPawnHashEntries;
    }

    public void setMaxPawnHashEntries(int maxPawnHashEntries) {
        this.maxPawnHashEntries = maxPawnHashEntries;
    }

    public int getLastHashSizeCreated() {
        return lastHashSizeCreated;
    }

    public void setLastHashSizeCreated(int lastHashSizeCreated) {
        this.lastHashSizeCreated = lastHashSizeCreated;
    }

    public long getLastPawnHashValue() {
        return lastPawnHashValue;
    }

    public void setLastPawnHashValue(long lastPawnHashValue) {
        this.lastPawnHashValue = lastPawnHashValue;
    }

    public PawnHashEntry getLastPawnHashEntry() {
        return lastPawnHashEntry;
    }

    public void setLastPawnHashEntry(PawnHashEntry lastPawnHashEntry) {
        this.lastPawnHashEntry = lastPawnHashEntry;
    }

    public void incVersion() {
        this.hashTableVersion++;
    }
}