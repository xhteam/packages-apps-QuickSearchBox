/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.quicksearchbox;

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.util.Log;

import java.util.HashSet;

/**
 * Provides values for configurable parameters in all of QSB.
 *
 * All the methods in this class return fixed default values. Subclasses may
 * make these values server-side settable.
 *
 */
public class Config {

    private static final String TAG = "QSB.Config";

    private static final long DAY_MILLIS = 86400000L;

    private static final int NUM_PROMOTED_SOURCES = 3;
    private static final int MAX_PROMOTED_SUGGESTIONS = 8;
    private static final int MAX_RESULTS_PER_SOURCE = 50;
    private static final long SOURCE_TIMEOUT_MILLIS = 10000;

    private static final int QUERY_THREAD_MAX_POOL_SIZE = 4;
    private static final long QUERY_THREAD_KEEPALIVE_MILLIS = 30000;
    private static final int QUERY_THREAD_PRIORITY =
            Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE;

    private static final long MAX_STAT_AGE_MILLIS = 7 * DAY_MILLIS;
    private static final long MAX_SOURCE_EVENT_AGE_MILLIS = 30 * DAY_MILLIS;
    private static final int MIN_IMPRESSIONS_FOR_SOURCE_RANKING = 5;
    private static final int MIN_CLICKS_FOR_SOURCE_RANKING = 3;
    private static final int MAX_SHORTCUTS_RETURNED = 12;

    private static final long THREAD_START_DELAY_MILLIS = 100;

    private final Context mContext;
    private HashSet<String> mDefaultCorpora;

    /**
     * Creates a new config that uses hard-coded default values.
     */
    public Config(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * Releases any resources used by the configuration object.
     *
     * Default implementation does nothing.
     */
    public void close() {
    }

    private HashSet<String> loadDefaultCorpora() {
        HashSet<String> defaultCorpora = new HashSet<String>();
        try {
            // Get the list of default corpora from a resource, which allows vendor overlays.
            String[] corpora = mContext.getResources().getStringArray(R.array.default_corpora);
            for (String corpus : corpora) {
                defaultCorpora.add(corpus);
            }
            return defaultCorpora;
        } catch (Resources.NotFoundException ex) {
            Log.e(TAG, "Could not load default corpora", ex);
            return defaultCorpora;
        }
    }

    /**
     * Checks if we trust the given source not to be spammy.
     */
    public synchronized boolean isCorpusEnabledByDefault(String corpusName) {
        if (mDefaultCorpora == null) {
            mDefaultCorpora = loadDefaultCorpora();
        }
        return mDefaultCorpora.contains(corpusName);
    }

    /**
     * The number of promoted sources.
     */
    public int getNumPromotedSources() {
        return NUM_PROMOTED_SOURCES;
    }

    /**
     * The maximum number of suggestions to promote.
     */
    public int getMaxPromotedSuggestions() {
        return MAX_PROMOTED_SUGGESTIONS;
    }

    /**
     * The number of results to ask each source for.
     */
    public int getMaxResultsPerSource() {
        return MAX_RESULTS_PER_SOURCE;
    }

    /**
     * The timeout for querying each source, in milliseconds.
     */
    public long getSourceTimeoutMillis() {
        return SOURCE_TIMEOUT_MILLIS;
    }

    /**
     * The maximum thread pool size for the query thread pool.
     */
    public int getQueryThreadMaxPoolSize(){
        return QUERY_THREAD_MAX_POOL_SIZE;
    }

    /**
     * The keep-alive time for the query thread pool, in millisseconds.
     */
    public long getQueryThreadKeepAliveMillis() {
        return QUERY_THREAD_KEEPALIVE_MILLIS;
    }

    /**
     * The priority of query threads.
     *
     * @return A thread priority, as defined in {@link Process}.
     */
    public int getQueryThreadPriority() {
        return QUERY_THREAD_PRIORITY;
    }

    /**
     * The maximum age of log data used for shortcuts.
     */
    public long getMaxStatAgeMillis(){
        return MAX_STAT_AGE_MILLIS;
    }

    /**
     * The maximum age of log data used for source ranking.
     */
    public long getMaxSourceEventAgeMillis(){
        return MAX_SOURCE_EVENT_AGE_MILLIS;
    }

    /**
     * The minimum number of impressions needed to rank a source.
     */
    public int getMinImpressionsForSourceRanking(){
        return MIN_IMPRESSIONS_FOR_SOURCE_RANKING;
    }

    /**
     * The minimum number of clicks needed to rank a source.
     */
    public int getMinClicksForSourceRanking(){
        return MIN_CLICKS_FOR_SOURCE_RANKING;
    }

    /**
     * The maximum number of shortcuts shown.
     */
    public int getMaxShortcutsReturned(){
        return MAX_SHORTCUTS_RETURNED;
    }

    /**
     * The maximum time to delay starting a search query thread after the user types a character.
     */
    public long getThreadStartDelayMillis() {
        return THREAD_START_DELAY_MILLIS;
    }

}
