package com.keshavg.reddit;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by keshavgupta on 9/28/16.
 */

public class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = SearchSuggestionsProvider.class.getName();
    public static final int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
