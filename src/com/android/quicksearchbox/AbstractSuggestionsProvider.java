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

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

/**
 * Common suggestions provider base class.
 *
 * The provider will only handle a single query at a time. If a new query comes
 * in, the old one is canceled.
 */
public abstract class AbstractSuggestionsProvider implements SuggestionsProvider {

    private static final boolean DBG = true;
    private static final String TAG = "QSB.AbstractSuggestionsProvider";

    private final Config mConfig;

    private final SourceTaskExecutor mQueryExecutor;

    private final Handler mPublishThread;

    private final Promoter mPromoter;

    private final ShouldQueryStrategy mShouldQueryStrategy = new ShouldQueryStrategy();

    private BatchingSourceTaskExecutor mBatchingExecutor;

    public AbstractSuggestionsProvider(Config config,
            SourceTaskExecutor queryExecutor,
            Handler publishThread,
            Promoter promoter) {
        mConfig = config;
        mQueryExecutor = queryExecutor;
        mPublishThread = publishThread;
        mPromoter = promoter;
    }

    public void close() {
        cancelPendingTasks();
    }

    /**
     * Cancels all pending query tasks.
     */
    private void cancelPendingTasks() {
        if (mBatchingExecutor != null) {
            mBatchingExecutor.cancelPendingTasks();
            mBatchingExecutor = null;
        }
    }

    public abstract ArrayList<Corpus> getOrderedCorpora();

    protected abstract SuggestionCursor getShortcutsForQuery(String query);

    /**
     * Gets the sources that should be queried for the given query.
     *
     */
    private ArrayList<Corpus> getCorporaToQuery(String query) {
        if (query.length() == 0) {
            return new ArrayList<Corpus>(0);
        }
        ArrayList<Corpus> orderedCorpora = getOrderedCorpora();
        ArrayList<Corpus> corporaToQuery = new ArrayList<Corpus>(orderedCorpora.size());
        for (Corpus corpus : orderedCorpora) {
            if (shouldQueryCorpus(corpus, query)) {
                corporaToQuery.add(corpus);
            }
        }
        return corporaToQuery;
    }

    protected boolean shouldQueryCorpus(Corpus corpus, String query) {
        return mShouldQueryStrategy.shouldQueryCorpus(corpus, query);
    }

    private void updateShouldQueryStrategy(CorpusResult cursor) {
        if (cursor.getCount() == 0) {
            mShouldQueryStrategy.onZeroResults(cursor.getCorpus(),
                    cursor.getUserQuery());
        }
    }

    public Suggestions getSuggestions(String query) {
        if (DBG) Log.d(TAG, "getSuggestions(" + query + ")");
        cancelPendingTasks();
        ArrayList<Corpus> corporaToQuery = getCorporaToQuery(query);
        final Suggestions suggestions = new Suggestions(mPromoter,
                mConfig.getMaxPromotedSuggestions(),
                query,
                corporaToQuery.size());
        SuggestionCursor shortcuts = getShortcutsForQuery(query);
        if (shortcuts != null) {
            suggestions.setShortcuts(shortcuts);
        }

        // Fast path for the zero sources case
        if (corporaToQuery.size() == 0) {
            return suggestions;
        }

        mBatchingExecutor = new BatchingSourceTaskExecutor(mQueryExecutor,
                mConfig.getNumPromotedSources());

        SuggestionCursorReceiver receiver = new SuggestionCursorReceiver(
                mBatchingExecutor, suggestions);

        int maxResultsPerSource = mConfig.getMaxResultsPerSource();
        for (Corpus corpus : corporaToQuery) {
            QueryTask task = new QueryTask(query, corpus, maxResultsPerSource, receiver);
            mBatchingExecutor.execute(task);
        }

        return suggestions;
    }

    private class SuggestionCursorReceiver {
        private final BatchingSourceTaskExecutor mExecutor;
        private final Suggestions mSuggestions;

        public SuggestionCursorReceiver(BatchingSourceTaskExecutor executor,
                Suggestions suggestions) {
            mExecutor = executor;
            mSuggestions = suggestions;
        }

        public void receiveSuggestionCursor(final CorpusResult cursor) {
            updateShouldQueryStrategy(cursor);
            mPublishThread.post(new Runnable() {
                public void run() {
                    mSuggestions.addCorpusResult(cursor);
                    if (!mSuggestions.isClosed()) {
                        executeNextBatchIfNeeded();
                    }
                }
            });
        }

        private void executeNextBatchIfNeeded() {
            if (mSuggestions.getSourceCount() % mConfig.getNumPromotedSources() == 0) {
                // We've just finished one batch
                if (mSuggestions.getPromoted().getCount() < mConfig.getMaxPromotedSuggestions()) {
                    // But we still don't have enough results, ask for more
                    mExecutor.executeNextBatch();
                }
            }
        }
    }

    /**
     * Gets suggestions from a given source.
     */
    private static class QueryTask implements SourceTask {
        private final String mQuery;
        private final Corpus mCorpus;
        private final int mQueryLimit;
        private final SuggestionCursorReceiver mReceiver;

        public QueryTask(String query, Corpus corpus, int queryLimit,
                SuggestionCursorReceiver receiver) {
            mQuery = query;
            mCorpus = corpus;
            mQueryLimit = queryLimit;
            mReceiver = receiver;
        }

        public void run() {
            CorpusResult cursor = mCorpus.getSuggestions(mQuery, mQueryLimit);
            mReceiver.receiveSuggestionCursor(cursor);
        }

        @Override
        public String toString() {
            return mCorpus + "[" + mQuery + "]";
        }
    }
}
