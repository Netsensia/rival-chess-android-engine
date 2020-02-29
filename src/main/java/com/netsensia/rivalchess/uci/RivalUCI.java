package com.netsensia.rivalchess.uci;

import com.netsensia.rivalchess.engine.core.RivalSearch;

public final class RivalUCI {
    @SuppressWarnings("squid:S106")
    private static final RivalSearch rivalSearch = new RivalSearch(System.out);

    public static void main(String[] args) {

        int timeMultiple;
        if (args.length > 1 && args[0].equals("tm")) {
            timeMultiple = Integer.parseInt(args[1]);
        } else {
            timeMultiple = 1;
        }

        rivalSearch.startEngineTimer(true);
        rivalSearch.setHashSizeMB(32);

        new Thread(rivalSearch).start();

        @SuppressWarnings("squid:S106")
        UCIController uciController = new UCIController(
                rivalSearch,
                timeMultiple,
                System.out);

        new Thread(uciController).start();
    }
}
