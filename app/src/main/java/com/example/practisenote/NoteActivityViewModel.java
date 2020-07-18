package com.example.practisenote;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    public static final String ORIGINAL_NOTECOURSE_ID = "com.example.practisenote.ORIGINAL_NOTECOURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE_ID = "com.example.practisenote.OORIGINAL_NOTE_TITLE_ID";
    public static final String ORIGINAL_NOTE_TEXT_ID = "com.example.practisenote. ORIGINAL_NOTE_TEXT_ID ";

    public  String mOriginalNoteCourseId;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;
    boolean mIsNewlyCreated = true;

    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTECOURSE_ID,mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE_ID,mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT_ID,mOriginalNoteText);
    }
    public void restoreState(Bundle instate){
        mOriginalNoteCourseId = instate.getString(ORIGINAL_NOTECOURSE_ID);
        mOriginalNoteTitle = instate.getString(ORIGINAL_NOTE_TITLE_ID);
        mOriginalNoteText = instate.getString(ORIGINAL_NOTE_TEXT_ID);

    }
}