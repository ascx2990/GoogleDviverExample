package com.e.googledviverexample.ui.edit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.e.googledviverexample.Constant;
import com.e.googledviverexample.R;
import com.e.googledviverexample.model.DriveServiceHelper;
import com.e.googledviverexample.ui.drive.DriveActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;

public class EditActivity extends AppCompatActivity {
    private static final String[] SCOPES = {DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA};
    private final String TAG = "EditActivity";
    //Google Service
    private GoogleSignInClient mGoogleSignInClient;
    private DriveServiceHelper mDriveServiceHelper;
    private GoogleAccountCredential mCredential;

    //View
    private ProgressDialog mProgressDialog;
    private TextInputEditText tied_Title, tied_Content;
    private View rootView;
    private String mFileId, mFileUri, mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findView();
        initial();

    }

    private void findView() {
        tied_Title = findViewById(R.id.tied_title);
        tied_Content = findViewById(R.id.tied_content);
        rootView = findViewById(R.id.root_google_edit);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFileId == null) {
                    createFile();
                } else {
                    updateFile();
                }
            }
        });
    }

    private void initial() {
        mFileId = getIntent().getStringExtra(DriveActivity.BUNDLE_DOCUMENT_ID);
        mFileUri = getIntent().getStringExtra(DriveActivity.BUNDLE_DOCUMENT_WEB_CONTENT_LINK);
        mFileName = getIntent().getStringExtra(DriveActivity.BUNDLE_DOCUMENT_WEB_CONTENT_TITLE);
        if (mFileName != null && mFileName.trim() != "") {
            tied_Title.setText(mFileName);
        }
//        GoogleSignInOptions mSignInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestEmail()
//                        .requestScopes(new Scope(DriveScopes.DRIVE), new Scope(DriveScopes.DRIVE_APPDATA))
//
//                        .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, mSignInOptions);


    }

    @Override
    protected void onStart() {
        super.onStart();
        checkGoogleLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == Constant.REQUEST_CODE.REQUEST_CODE_READ_DOCUMENT && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                dumpImageMetaData(uri);
            }
        }
    }

    private void checkGoogleLogin() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(DriveScopes.DRIVE))) {
            setGoogleAccountCredential(account);
            mDriveServiceHelper = new DriveServiceHelper(getDriveService());
            updateUI(account);
        } else {
            // updateUI(null);
        }
    }

    private void setGoogleAccountCredential(GoogleSignInAccount account) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES));//設定想使用的權限
        //.setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccount(account.getAccount());
    }

    private Drive getDriveService() {
        Drive service = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                mCredential).build();
        return service;
    }

    private void updateFile() {
        Log.d(TAG, "updateFile");
        if (mDriveServiceHelper == null) {
            Log.e(TAG, "mDriveServiceHelper is null");
            return;
        }
        showProgressDialog();
        String title = tied_Title.getText().toString().trim();
        String content = tied_Content.getText().toString();
        if (title == null || title.equals("")) {
            Snackbar.make(rootView, R.string.errormessage_input_title, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            hideProgressDialog();
            return;
        }
        mDriveServiceHelper.saveFile(mFileId, title, content)
                .addOnSuccessListener(fileId -> {

                    hideProgressDialog();
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Can't create file. " + exception.toString());
                    hideProgressDialog();
                });
    }

    private void createFile() {
        Log.d(TAG, "createFile");
        if (mDriveServiceHelper == null) {
            Log.e(TAG, "mDriveServiceHelper is null");
            return;
        }
        showProgressDialog();
        String title = tied_Title.getText().toString().trim();
        String content = tied_Content.getText().toString();
        if (title == null || title.equals("")) {
            Snackbar.make(rootView, R.string.errormessage_input_title, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            hideProgressDialog();
            return;
        }
        mDriveServiceHelper.createDocumentFile(title, content)
                .addOnSuccessListener(fileId -> {
                    this.mFileId = fileId;
                    hideProgressDialog();
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Can't create file. " + exception.toString());
                    hideProgressDialog();
                });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void readFile(String fileId) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Reading file " + fileId);

            mDriveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;
                        //tied_Content.setText(name);
                        tied_Content.setText(content);

                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        Log.d(TAG, "updateUI");
        if (account != null && mFileId != null) {
            //loadData();

//                mDriveServiceHelper.readTextFromUrl(new URL("https://drive.google.com/a/g.ncu.edu.tw/file/d/1UFpgJa1ALnRHMEOSjIlXcGmOlvMIo19X/view?usp=drivesdk")).addOnSuccessListener(result -> {
//                    LogUnit.v(TAG, "content:" + result);
//
//                    hideProgressDialog();
//                });

            readFile(mFileId);

        } else {

        }
    }


    public void dumpImageMetaData(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = this.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size);
            }
        } finally {
            cursor.close();
        }
    }
}
