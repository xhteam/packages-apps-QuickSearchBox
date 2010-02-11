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

package com.android.quicksearchbox.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.quicksearchbox.R;
import com.android.quicksearchbox.SuggestionCursor;

/**
 * Inflates suggestion views.
 */
public class SuggestionViewInflater implements SuggestionViewFactory {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.SuggestionViewInflater";

    // The suggestion view classes that may be returned by this factory.
    private static final Class<?>[] SUGGESTION_VIEW_CLASSES = {
            DefaultSuggestionView.class,
            ContactSuggestionView.class,
    };

    // The layout ids associated with each of the above classes.
    private static final int[] SUGGESTION_VIEW_LAYOUTS = {
            R.layout.suggestion,
            R.layout.contact_suggestion,
    };

    private static final String CONTACT_LOOKUP_URI
            = ContactsContract.Contacts.CONTENT_LOOKUP_URI.toString();

    private final Context mContext;

    public SuggestionViewInflater(Context context) {
        mContext = context;
    }

    protected LayoutInflater getInflater() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getSuggestionViewTypeCount() {
        return SUGGESTION_VIEW_CLASSES.length;
    }

    public int getSuggestionViewType(SuggestionCursor suggestion) {
        return isContactSuggestion(suggestion) ? 1 : 0;
    }

    public SuggestionView getSuggestionView(int viewType, View convertView,
            ViewGroup parentViewType) {
        if (convertView == null || !convertView.getClass().equals(
                SUGGESTION_VIEW_CLASSES[viewType])) {
            int layoutId = SUGGESTION_VIEW_LAYOUTS[viewType];
            convertView = getInflater().inflate(layoutId, parentViewType, false);
        }
        return (SuggestionView) convertView;
    }

    public CorpusView createSourceView(ViewGroup parentViewType) {
        if (DBG) Log.d(TAG, "createSourceView()");
        CorpusView view = (CorpusView)
                getInflater().inflate(R.layout.corpus_grid_item, parentViewType, false);
        return view;
    }

    public String getGlobalSearchLabel() {
        return mContext.getString(R.string.corpus_label_global);
    }

    public Drawable getGlobalSearchIcon() {
        return mContext.getResources().getDrawable(R.drawable.corpus_icon_global);
    }

    public Uri getGlobalSearchIconUri() {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(mContext.getPackageName())
                .appendEncodedPath(String.valueOf(R.drawable.corpus_icon_global))
                .build();
    }

    private boolean isContactSuggestion(SuggestionCursor suggestion) {
        String intentData = suggestion.getSuggestionIntentDataString();
        return intentData != null && intentData.startsWith(CONTACT_LOOKUP_URI);
    }
}
