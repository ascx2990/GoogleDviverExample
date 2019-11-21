package com.e.googledviverexample.ui.drive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.e.googledviverexample.R;
import com.e.googledviverexample.model.DriveServiceHelper;
import com.e.googledviverexample.ui.edit.EditActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.Arrays;
import java.util.List;

public class DriveActivity extends AppCompatActivity {
    private final String TAG = DriveActivity.class.getSimpleName();
    private static final String[] SCOPES = {DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA};
    //
    private GoogleAccountCredential mCredential;
    private DriveServiceHelper mDriveServiceHelper;
    //
    public static final String PARENT_FILE_NAME = "CloudDriveTest";
    public static String BUNDLE_DOCUMENT_ID = "Bundle_Document_ID";
    public static String BUNDLE_DOCUMENT_WEB_CONTENT_LINK = "Bundle_Document_WEB_CONTENT_LINK";
    public static String BUNDLE_DOCUMENT_WEB_CONTENT_TITLE = "Bundle_Document_TITLE";
    //

    //View
    private ProgressDialog mProgressDialog;
    private View rootView;
    private RecyclerView recyclerView;
    private List<File> mFileList;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findView();
        // initial();

    }

    private void findView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        rootView = findViewById(R.id.root_google_document);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(DriveActivity.this, EditActivity.class);
                startActivity(intent);
                //createFile();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkGoogleLogin();
    }

    private void checkGoogleLogin() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(DriveScopes.DRIVE))) {
            setGoogleAccountCredential(account);
            mDriveServiceHelper = new DriveServiceHelper(getDriveService());
            updateUI(account);
        } else {
            updateUI(null);
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

    private void updateUI(GoogleSignInAccount account) {
        Log.v(TAG, "updateUI");
        if (account != null) {
            //query();
            //queryParentFile();
            //checkParentFile();
            loadData();
        } else {

        }
    }


    private void setListView() {
        mAdapter = new MyAdapter(this, mFileList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
    }

    /***/
    private void loadData() {
        if (mDriveServiceHelper == null) {
            return;
        }
        showProgressDialog();
        Log.d(TAG, "loadData");
        mDriveServiceHelper.loadFile().addOnSuccessListener(fileList -> {
            Log.v(TAG, "count:" + fileList.toString());
            Log.v(TAG, "count:" + fileList.getFiles().size());
            if (fileList.getFiles().size() != 0) {
                mFileList = fileList.getFiles();
                setListView();
            }
            hideProgressDialog();
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Unable to query files." + exception);
            Toast.makeText(this, R.string.errormessage_check_network, Toast.LENGTH_SHORT).show();
            hideProgressDialog();
        });
    }

    private void reloadData() {
        if (mDriveServiceHelper == null) {
            return;
        }
        showProgressDialog();
        Log.d(TAG, "loadData");
        mDriveServiceHelper.loadFile().addOnSuccessListener(fileList -> {
            Log.v(TAG, "count:" + fileList.toString());
            Log.v(TAG, "count:" + fileList.getFiles().size());
            if (fileList.getFiles().size() != 0) {
                mFileList.clear();
                mFileList.addAll(fileList.getFiles());
                mAdapter.notifyDataSetChanged();
            }
            hideProgressDialog();
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Unable to query files." + exception);
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


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Context mContext;
        private List<File> mData;

        public MyAdapter(Context context, List<File> data) {
            this.mContext = context;
            this.mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.cell_post, parent, false);
            ViewHolder holder = new ViewHolder(view);
            holder.ivPosterThumbnail = (ImageView) view.findViewById(R.id.ivFileThumbnail);
            holder.tvPosterName = (TextView) view.findViewById(R.id.tvFileName);
            holder.tvContent = (TextView) view.findViewById(R.id.tvContent);
            holder.btnDel = (ImageButton) view.findViewById(R.id.btnDel);
            holder.btnComment = (ImageButton) view.findViewById(R.id.btnComment);
            holder.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgressDialog();
                    mDriveServiceHelper.deleteDocumentFile(mData.get(holder.getPosition()).getId()).addOnSuccessListener(fileId -> {
                        Toast.makeText(mContext, "Delete Success!", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        reloadData();
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(mContext, "Delete Failure!", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    });
                }
            });
            holder.btnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(BUNDLE_DOCUMENT_ID, mData.get(holder.getPosition()).getId());
                    intent.putExtra(BUNDLE_DOCUMENT_WEB_CONTENT_LINK, mData.get(holder.getPosition()).getWebContentLink());
                    intent.putExtra(BUNDLE_DOCUMENT_WEB_CONTENT_TITLE, mData.get(holder.getPosition()).getName());
                    intent.setClass(DriveActivity.this, EditActivity.class);
                    startActivity(intent);
                }
            });
            return holder;

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            File file = mData.get(position);
            holder.tvPosterName.setText(file.getName());
            Glide.with(mContext)
                    .load(file.getIconLink())
                    .into(holder.ivPosterThumbnail);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView ivPosterThumbnail;
            public TextView tvPosterName;
            public TextView tvContent;
            public ImageButton btnDel;
            public ImageButton btnComment;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
