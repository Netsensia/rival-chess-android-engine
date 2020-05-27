package com.netsensia.rivalchess.config

enum class FeatureFlag(val isActive: Boolean) {
    USE_HASH_TABLES(true),
    USE_HEIGHT_REPLACE_HASH(true),
    USE_ALWAYS_REPLACE_HASH(true),
    USE_ASPIRATION_WINDOW(true), // +3 fails when turned off
    USE_NULL_MOVE_PRUNING(true), // +17 fails when turned off
    USE_DELTA_PRUNING(false), // (1) 20 - best so far - turned off
    USE_FUTILITY_PRUNING(true), // +4 fails when turned off
    USE_HISTORY_HEURISTIC(true), // +5 fails when turned off
    USE_MATE_HISTORY_KILLERS(true), // +3 fails when turned off
    USE_INTERNAL_OPENING_BOOK(true),
    USE_PIECE_SQUARES_IN_MOVE_ORDERING(false), // +154 when turned off!!!!
    USE_LATE_MOVE_REDUCTIONS(true),
    LMR_RESEARCH_ON_FAIL_HIGH(true),
    USE_PV_SEARCH(true),
    USE_INTERNAL_ITERATIVE_DEEPENING(true),
    USE_SUPER_VERIFY_ON_HASH(false);
}