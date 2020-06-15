package com.netsensia.rivalchess.bitboards;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u0016\n\u0002\b\u0005\n\u0002\u0010\u0015\n\u0002\b\r\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J.\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u001f0!2\u0006\u0010\"\u001a\u00020\u001fH\u0002J\u0018\u0010#\u001a\u00020\u001f2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001fH\u0002J\u0018\u0010%\u001a\u00020\u001f2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001fH\u0002J\u0010\u0010&\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J \u0010\'\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u001fH\u0002J \u0010(\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020*H\u0002J \u0010+\u001a\u00020*2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020*H\u0002J \u0010,\u001a\u00020*2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020*H\u0002J \u0010-\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020*H\u0002J \u0010.\u001a\u00020*2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020*H\u0002J \u0010/\u001a\u00020*2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020*H\u0002R\u0010\u0010\u0003\u001a\u00020\u00048\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u0018\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00068\u0006X\u0087\u0004\u00a2\u0006\u0004\n\u0002\u0010\bR\u0018\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00070\u00068\u0006X\u0087\u0004\u00a2\u0006\u0004\n\u0002\u0010\bR\u0010\u0010\n\u001a\u00020\u00078\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u00020\u00078\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u00020\r8\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u00020\r8\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u000f\u001a\u0004\u0018\u00010\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u0010\u0010\u0014\u001a\u00020\u00078\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u00020\u00078\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0016\u001a\u0004\u0018\u00010\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0011\"\u0004\b\u0018\u0010\u0013R\u0010\u0010\u0019\u001a\u00020\u00048\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00060"}, d2 = {"Lcom/netsensia/rivalchess/bitboards/MagicBitboards;", "", "()V", "bishopVars", "Lcom/netsensia/rivalchess/bitboards/MagicVars;", "magicMovesBishop", "", "", "[[J", "magicMovesRook", "magicNumberBishop", "magicNumberRook", "magicNumberShiftsBishop", "", "magicNumberShiftsRook", "occupancyAttackSet", "getOccupancyAttackSet", "()[J", "setOccupancyAttackSet", "([J)V", "occupancyMaskBishop", "occupancyMaskRook", "occupancyVariation", "getOccupancyVariation", "setOccupancyVariation", "rookVars", "calculateOccupancyAttackSets", "", "isRook", "", "bitRef", "", "setBitsInMask", "", "bitCount", "calculateOccupancyAttackSetsBishop", "i", "calculateOccupancyAttackSetsRook", "generateOccupancyVariationsAndDatabase", "setMagicMoves", "setMagicMovesForBishop", "validMoves", "", "setMagicMovesForNorthEastDiagonal", "setMagicMovesForNorthWestDiagonal", "setMagicMovesForRooks", "setMagicMovesForSouthEastDiagonal", "setMagicMovesForSouthWestDiagonal", "rivalchess-engine"})
public final class MagicBitboards {
    @org.jetbrains.annotations.NotNull()
    public static final long[][] magicMovesRook = null;
    @org.jetbrains.annotations.NotNull()
    public static final long[][] magicMovesBishop = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.netsensia.rivalchess.bitboards.MagicVars bishopVars = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.netsensia.rivalchess.bitboards.MagicVars rookVars = null;
    @org.jetbrains.annotations.Nullable()
    private static long[] occupancyVariation;
    @org.jetbrains.annotations.Nullable()
    private static long[] occupancyAttackSet;
    @org.jetbrains.annotations.NotNull()
    public static final long[] occupancyMaskRook = {282578800148862L, 565157600297596L, 1130315200595066L, 2260630401190006L, 4521260802379886L, 9042521604759646L, 18085043209519166L, 36170086419038334L, 282578800180736L, 565157600328704L, 1130315200625152L, 2260630401218048L, 4521260802403840L, 9042521604775424L, 18085043209518592L, 36170086419037696L, 282578808340736L, 565157608292864L, 1130315208328192L, 2260630408398848L, 4521260808540160L, 9042521608822784L, 18085043209388032L, 36170086418907136L, 282580897300736L, 565159647117824L, 1130317180306432L, 2260632246683648L, 4521262379438080L, 9042522644946944L, 18085043175964672L, 36170086385483776L, 283115671060736L, 565681586307584L, 1130822006735872L, 2261102847592448L, 4521664529305600L, 9042787892731904L, 18085034619584512L, 36170077829103616L, 420017753620736L, 699298018886144L, 1260057572672512L, 2381576680245248L, 4624614895390720L, 9110691325681664L, 18082844186263552L, 36167887395782656L, 35466950888980736L, 34905104758997504L, 34344362452452352L, 33222877839362048L, 30979908613181440L, 26493970160820224L, 17522093256097792L, 35607136465616896L, 9079539427579068672L, 8935706818303361536L, 8792156787827803136L, 8505056726876686336L, 7930856604974452736L, 6782456361169985536L, 4485655873561051136L, 9115426935197958144L};
    @org.jetbrains.annotations.NotNull()
    public static final long[] occupancyMaskBishop = {18049651735527936L, 70506452091904L, 275415828992L, 1075975168L, 38021120L, 8657588224L, 2216338399232L, 567382630219776L, 9024825867763712L, 18049651735527424L, 70506452221952L, 275449643008L, 9733406720L, 2216342585344L, 567382630203392L, 1134765260406784L, 4512412933816832L, 9024825867633664L, 18049651768822272L, 70515108615168L, 2491752130560L, 567383701868544L, 1134765256220672L, 2269530512441344L, 2256206450263040L, 4512412900526080L, 9024834391117824L, 18051867805491712L, 637888545440768L, 1135039602493440L, 2269529440784384L, 4539058881568768L, 1128098963916800L, 2256197927833600L, 4514594912477184L, 9592139778506752L, 19184279556981248L, 2339762086609920L, 4538784537380864L, 9077569074761728L, 562958610993152L, 1125917221986304L, 2814792987328512L, 5629586008178688L, 11259172008099840L, 22518341868716544L, 9007336962655232L, 18014673925310464L, 2216338399232L, 4432676798464L, 11064376819712L, 22137335185408L, 44272556441600L, 87995357200384L, 35253226045952L, 70506452091904L, 567382630219776L, 1134765260406784L, 2832480465846272L, 5667157807464448L, 11333774449049600L, 22526811443298304L, 9024825867763712L, 18049651735527936L};
    @org.jetbrains.annotations.NotNull()
    public static final long[] magicNumberRook = {-6809440297970302416L, 18031991769407488L, 36038143404675074L, 36037603858059264L, 4755805742261731336L, 324270168337547392L, 288249070030688264L, 36029485304252544L, 1158410273640464400L, 141012502578304L, 281548010029248L, 9570429053182464L, 562984615223308L, 2317665028167565316L, 36310276294967300L, 4611826770948146432L, 35734170910721L, 40532672598138888L, 146508275402153985L, 2306408158734600512L, -9203104738884648955L, 7503138266510533120L, 756609135579115537L, 49898036700188740L, 4756997477301784224L, -5763974197892087420L, 2314885394989056896L, 4512397868925056L, 2451092895445287936L, 180322057977984L, 145247702234960049L, 39443356148842756L, -9115285370915774464L, 5227623773657632769L, 4618617341884764164L, 1310547633382625288L, -8573587018750098432L, 18577401109821456L, 37163566100426770L, 6764614326944769L, 2467981942197485568L, 4504836850630660L, 721139440361275424L, 4503805936861280L, 1126449796907028L, 36030996109361280L, 180709134088306816L, 5764609177138364420L, 72625084065399296L, 4611721271523148224L, 7074258976014848L, 144167968935291136L, 2253449752477824L, 38320179284836480L, 4556479306859520L, 14401680344089088L, 36169538976354881L, 282025005154337L, 1134833455624769L, 148900881174103305L, -9217460993586358271L, 576742261706982915L, 867224549276820481L, 281483572970051L};
    @org.jetbrains.annotations.NotNull()
    public static final long[] magicNumberBishop = {2958870736342630660L, 148625593733349760L, 6350640762115325952L, 3217827023294562336L, 145245632058099968L, 2306425819699871746L, -9205286152370650904L, -9139490286097856448L, -9221938198462595000L, 1153416293563564289L, 1153211784333631744L, 1733894672109148160L, 27588963480963584L, 5775871126377857056L, -4609422931399782398L, 621507748057711104L, 666532762307662354L, 6764205368148496L, 291612620082513304L, 4767062406775980112L, 144255942758760704L, 147211490934526016L, 2544278522825728L, 20618119521894664L, 9042934735577472L, 4508548690608640L, 2326111407164358672L, 72831925168964096L, 145250022789750800L, 162429753293308097L, 1164744005599371520L, 666817518372952134L, 163273113042158080L, 4611844417090225814L, 43997713661984L, 576465287925465344L, 153122954266837024L, 288794983963100361L, 9852740876502528L, 18300272070689538L, 293314586705266712L, -9213793048448712184L, 1441469649287399424L, 9610015228887168L, 1226390875936915984L, 2323866787944595585L, -9114017898925390848L, 4612918572052652544L, 288795131136444432L, 5048817791164088328L, -9223359925029960702L, 9007509054949888L, -9079256710765084416L, 2607045165514856L, 1171149242833600576L, 577745067834485312L, 2594108732980871168L, 146457156588405252L, 291608076442742816L, 2305860876281972224L, 288230386353579024L, 576569827428140288L, 39599733347344L, 87855381971216640L};
    @org.jetbrains.annotations.NotNull()
    public static final int[] magicNumberShiftsRook = {52, 53, 53, 53, 53, 53, 53, 52, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 52, 53, 53, 53, 53, 53, 53, 52};
    @org.jetbrains.annotations.NotNull()
    public static final int[] magicNumberShiftsBishop = {58, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59, 59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 58};
    public static final com.netsensia.rivalchess.bitboards.MagicBitboards INSTANCE = null;
    
    @org.jetbrains.annotations.Nullable()
    public final long[] getOccupancyVariation() {
        return null;
    }
    
    public final void setOccupancyVariation(@org.jetbrains.annotations.Nullable()
    long[] p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final long[] getOccupancyAttackSet() {
        return null;
    }
    
    public final void setOccupancyAttackSet(@org.jetbrains.annotations.Nullable()
    long[] p0) {
    }
    
    private final void generateOccupancyVariationsAndDatabase(boolean isRook) {
    }
    
    private final void setMagicMoves(boolean isRook, int bitRef, int bitCount) {
    }
    
    private final void setMagicMovesForBishop(int bitRef, int i, long validMoves) {
    }
    
    private final long setMagicMovesForSouthWestDiagonal(int bitRef, int i, long validMoves) {
        return 0L;
    }
    
    private final long setMagicMovesForNorthEastDiagonal(int bitRef, int i, long validMoves) {
        return 0L;
    }
    
    private final long setMagicMovesForSouthEastDiagonal(int bitRef, int i, long validMoves) {
        return 0L;
    }
    
    private final long setMagicMovesForNorthWestDiagonal(int bitRef, int i, long validMoves) {
        return 0L;
    }
    
    private final void setMagicMovesForRooks(int bitRef, int i, long validMoves) {
    }
    
    private final void calculateOccupancyAttackSets(boolean isRook, int bitRef, java.util.List<java.lang.Integer> setBitsInMask, int bitCount) {
    }
    
    private final int calculateOccupancyAttackSetsBishop(int bitRef, int i) {
        return 0;
    }
    
    private final int calculateOccupancyAttackSetsRook(int bitRef, int i) {
        return 0;
    }
    
    private MagicBitboards() {
        super();
    }
}