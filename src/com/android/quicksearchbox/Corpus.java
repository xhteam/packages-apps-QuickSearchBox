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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

/**
 * A corpus is a user-visible set of suggestions. A corpus gets suggestions from one
 * or more sources.
 *
 * Objects that implement this interface should override {@link Object#equals(Object)}
 * and {@link Object#hashCode()} so that they can be used as keys in hash maps.
 */
public interface Corpus {

    /**
     * Gets the localized, human-readable label for this corpus.
     */
    CharSequence getLabel();

    /**
     * Gets the icon for this corpus.
     */
    Drawable getCorpusIcon();

    /**
     * Gets the icon URI for this corpus.
     */
    Uri getCorpusIconUri();

    /**
     * Gets the description to use for this corpus in system search settings.
     */
    CharSequence getSettingsDescription();

    /**
     * Gets suggestions from this corpus.
     *
     * @param query The user query.
     * @param queryLimit An advisory maximum number of results that the source should return.
     * @return The suggestion results.
     */
    CorpusResult getSuggestions(String query, int queryLimit);

    /**
     * Gets the unique name for this corpus.
     */
    String getName();

    int getQueryThreshold();

    boolean queryAfterZeroResults();

    boolean voiceSearchEnabled();

    Intent createSearchIntent(String query, Bundle appData);

    Intent createVoiceSearchIntent(Bundle appData);

    boolean isWebCorpus();
}
