package com.example.practisenote;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.example.practisenote.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNoteInfo;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        View Model provider instance to help manage state activity of the app
//        This view model provider helps persist the state of the app
//        which is useful in situations where the app's activity is destroyed an recreated from scratch
//        i.e during the configuration change of the mobile device
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

//        This helps us save our Activity state when the View model and the Activity were destroyed
//        and are being recreated
        if (mViewModel.mIsNewlyCreated && savedInstanceState != null)
            mViewModel.restoreState(savedInstanceState);
        mViewModel.mIsNewlyCreated = false;


        mSpinnerCourses = findViewById(R.id.spinner_courses);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<CourseInfo>(this,android.R.layout.simple_spinner_item,courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        //This method gets the objects passed from the NoteListActivity
        // and display's them on the screen
        readDisplayStateValues();
        saveOriginalNoteValues();

        //Reference the Edit texts used in our layouts to display the notes passed to this Activity
        // using the parcelable implementation
        mTextNoteTitle = findViewById(R.id.text_title);
        mTextNoteText = findViewById(R.id.text_note);

        if (!mIsNewNote)
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
    }

    private void saveOriginalNoteValues() {
//        Here the original note values are stored to be used later
        if (mIsNewNote)
            return;
        mViewModel.mOriginalNoteCourseId = mNoteInfo.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNoteInfo.getTitle();
        mViewModel.mOriginalNoteText = mNoteInfo.getText();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling){
            if (mIsNewNote){
                DataManager.getInstance().removeNote(mNotePosition);
            }
            else {
                storePreviousNoteValues();
            }
        }
        else {
            saveNote();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
        mViewModel.saveState(outState);
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNoteInfo.setCourse(course);
        mNoteInfo.setTitle(mViewModel.mOriginalNoteTitle);
        mNoteInfo.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        mNoteInfo.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNoteInfo.setTitle(mTextNoteTitle.getText().toString());
        mNoteInfo.setText(mTextNoteText.getText().toString());
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNoteInfo.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNoteInfo.getTitle());
        textNoteText.setText(mNoteInfo.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = position == POSITION_NOT_SET;
        if (mIsNewNote){
            createNewNote();
        }else {
            mNoteInfo = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNoteInfo = dm.getNotes().get(mNotePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();

            return true;
        }else if (id == R.id.action_cancel){
            mIsCancelling = true;
            finish();
        }else if (id == R.id.action_next){
//            This method call gives the user the option
//            to move to the next note in the NoteActivity
            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }
// This method checks for the position of the notes.
//  The next menu item continues to be enabled if the note is not in the last position,
//    And gets disabled if the note is in the last position.
//
//    In every instance the user next's the notes, the moveNext() method is called,
//    the invalidateIOptionsMenu() method
//    also gets called which triggers the onPrepareOptionsMenu() to check the position of the note item
//    Until the last note is reached and the next menu item gets disabled.
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndent = DataManager.getInstance().getNotes().size() -1;
        item.setEnabled(mNotePosition < lastNoteIndent);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
//        Save the original note and the changes made to it before moving to the next note
        saveNote();
//        Increase the position of the note list by one to move to the next note
        ++mNotePosition;
//        Get the note in the new position and store the value in the mNote field
        mNoteInfo = DataManager.getInstance().getNotes().get(mNotePosition);
        saveOriginalNoteValues();
        displayNote(mSpinnerCourses, mTextNoteTitle,mTextNoteText);
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out what I learned in the PluralSight course \"" + course.getTitle() +
        "\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(intent);
    }
}