package com.e.googledviverexample.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.e.googledviverexample.Constant;
import com.e.googledviverexample.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;

/**
 * 要申請
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    //Google Service
    private GoogleSignInClient mGoogleSignInClient;
    // private DriveServiceHelper mDriveServiceHelper;
    private GoogleAccountCredential mCredential;
    //View
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private LinearLayout ln_drive_option, ln_sign_out_and_disconnect;
    private Button bt_sign_out, bt_disconnect;// bt_create;
    private SignInButton bt_sign_in;
    private static final String[] SCOPES = {DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findView();
        initial();

    }

    private void findView() {
        // Views
        mStatusTextView = findViewById(R.id.status);
        bt_sign_in = findViewById(R.id.sign_in_button);
        bt_sign_out = findViewById(R.id.sign_out_button);
        bt_disconnect = findViewById(R.id.disconnect_button);
        //bt_create = findViewById(R.id.create_btn);
        ln_drive_option = findViewById(R.id.ln_drive_option);
        ln_sign_out_and_disconnect = findViewById(R.id.sign_out_and_disconnect);
        // Button listeners
        bt_sign_in.setOnClickListener(view -> signInGoogle());
        bt_sign_out.setOnClickListener(view -> signOutGoogle());
        bt_disconnect.setOnClickListener(view -> revokeAccess());
//        bt_create.setOnClickListener(view -> {
//            createFile();
//        });

    }

    private void initial() {
        Log.v(TAG, "initial");

        GoogleSignInOptions mSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE), new Scope(DriveScopes.DRIVE_APPDATA),
                                new Scope("https://www.googleapis.com/auth/photoslibrary"),
                                new Scope("https://www.googleapis.com/auth/photoslibrary.appendonly"),
                                new Scope("https://www.googleapis.com/auth/photoslibrary.sharing"))
                        .requestIdToken("")
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mSignInOptions);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case Constant.REQUEST_CODE.REQUEST_CODE_SIGN_IN:
                Log.v(TAG, "REQUEST_CODE_SIGN_IN");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(resultData);
                hideProgressDialog();
                handleSignInResult(task);
                break;

//            case REQUEST_CODE_OPEN_DOCUMENT:
//                LogUnit.v(TAG, "REQUEST_CODE_OPEN_DOCUMENT");
////                if (resultCode == Activity.RESULT_OK && resultData != null) {
////                    Uri uri = resultData.getData();
////                    if (uri != null) {
////                        openFileFromFilePicker(uri);
////                    }
////                }
//                break;
            default:
                Log.d(TAG, "REQUEST_CODE_NONE");
                break;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.v(TAG, "idToken:" + idToken);
            setGoogleAccountCredential(account);
            // mDriveServiceHelper = new DriveServiceHelper(getDriveService());
            // TODO(developer): send ID Token to server and validate
            updateUI(account);
        } catch (ApiException e) {
            Log.e(TAG, "handleSignInResult:" + e.toString());
            Toast.makeText(this, "Login fail", Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void setGoogleAccountCredential(GoogleSignInAccount account) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES));//設定想使用的權限
        //.setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccount(account.getAccount());
        String idToken = account.getIdToken();
        Log.v(TAG, "idToken:" + idToken);
    }

    private void signInGoogle() {
        //LogUnit.v(TAG, "signInGoogle");
        showProgressDialog();
        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), Constant.REQUEST_CODE.REQUEST_CODE_SIGN_IN);
    }

    private void signOutGoogle() {
        //LogUnit.v(TAG, "signOutGoogle");
        showProgressDialog();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // ...

                updateUI(null);
                hideProgressDialog();
            }
        });
    }

    //取消授權
    private void revokeAccess() {
        Log.v(TAG, "revokeAccess");
        showProgressDialog();
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // ...
                updateUI(null);
                hideProgressDialog();
            }
        });
    }

    private void updateUI(GoogleSignInAccount account) {
        Log.v(TAG, "updateUI");
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));
            bt_sign_in.setVisibility(View.GONE);
            ln_sign_out_and_disconnect.setVisibility(View.VISIBLE);
            ln_drive_option.setVisibility(View.VISIBLE);
            finish();
        } else {
            mStatusTextView.setText(R.string.signed_out);
            bt_sign_in.setVisibility(View.VISIBLE);
            ln_sign_out_and_disconnect.setVisibility(View.GONE);
            ln_drive_option.setVisibility(View.GONE);
        }
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
}
