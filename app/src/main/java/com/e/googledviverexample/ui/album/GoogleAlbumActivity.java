package com.e.googledviverexample.ui.album;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.e.googledviverexample.R;
import com.e.googledviverexample.model.DriveServiceHelper;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class GoogleAlbumActivity extends AppCompatActivity {
    private final String TAG = GoogleAlbumActivity.class.getName();
    private Button btCreat, btOpenAlbum, btUploadPhoto, btGetGoogleAlbum;
    private GoogleAccountCredential mCredential;
    private final int PICK_FROM_GALLERY = 6;
    private final int PICK_FROM_GET = 7;
    private static final String[] SCOPES = {
            "https://www.googleapis.com/auth/photoslibrary"
            , "https://www.googleapis.com/auth/photoslibrary.appendonly"
            , "https://www.googleapis.com/auth/photoslibrary.sharing"};
    private DriveServiceHelper mDriveServiceHelper;

    private String mToken;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findView();
        onListener();
        initial();

    }

    private void findView() {
        btCreat = findViewById(R.id.button);
        btOpenAlbum = findViewById(R.id.button2);
        btUploadPhoto = findViewById(R.id.button3);
        btGetGoogleAlbum = findViewById(R.id.button4);
    }

    private void onListener() {
        btCreat.setOnClickListener(view -> {
//            CreatAlbumTask task = new CreatAlbumTask(this, idToken);
//            task.execute();
            //postyCreatAlbum(idToken);
            creatAlbum();
        });
        btOpenAlbum.setOnClickListener(view -> {
            openAlbum();
        });
        btUploadPhoto.setOnClickListener(view -> {
            uploadPhoto();
        });
        btGetGoogleAlbum.setOnClickListener(view -> {
            getAlbumList();
        });
    }

    private void initial() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkGoogleLogin();
    }

    //String idToken;
    //String accessToken;

    private void checkGoogleLogin() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            updateUI(null);
            return;
        }
        if (!GoogleSignIn.hasPermissions(account,
                new Scope("https://www.googleapis.com/auth/photoslibrary"),
                new Scope("https://www.googleapis.com/auth/photoslibrary.appendonly"),
                new Scope("https://www.googleapis.com/auth/photoslibrary.sharing"))) {
            setGoogleAccountCredential(account);
            updateUI(account);
        } else {

            getGoogleAccountCredential(account);
//            try {
//                accessToken = GoogleAuthUtil.getToken(this,
//                        new Account(account.getDisplayName(), "1"), "oauth2:https://www.googleapis.com/auth/photoslibrary");
//            } catch (IOException e) {
//
//            } catch (GoogleAuthException e) {
//
//            }

        }
    }

    private void getGoogleAccountCredential(GoogleSignInAccount account) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES));//設定想使用的權限
        mCredential.setSelectedAccount(account.getAccount());

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    token = mCredential.getToken();
                } catch (IOException transientEx) {
                    // Network or server error, try later
                    Log.e(TAG, transientEx.toString());
                } catch (GoogleAuthException authEx) {
                    // The call is not ever expected to succeed
                    // assuming you have already verified that
                    // Google Play services is installed.
                    Log.e(TAG, authEx.toString());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                Log.i(TAG, "Access token retrieved:" + token);
                hideProgressDialog();
                mToken = token;
            }

        };
        task.execute();
    }

    private void setGoogleAccountCredential(GoogleSignInAccount account) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES));//設定想使用的權限

        //.setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccount(account.getAccount());


        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    token = mCredential.getToken();
                } catch (IOException transientEx) {
                    // Network or server error, try later
                    Log.e(TAG, transientEx.toString());
                } catch (GoogleAuthException authEx) {
                    // The call is not ever expected to succeed
                    // assuming you have already verified that
                    // Google Play services is installed.
                    Log.e(TAG, authEx.toString());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                Log.i(TAG, "Access token retrieved:" + token);
                hideProgressDialog();
                mToken = token;
            }

        };
        task.execute();
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

    private void updateUI(GoogleSignInAccount account) {
        Log.v(TAG, "updateUI");
        if (account != null) {
            //query();
            //queryParentFile();
            //checkParentFile();
            //loadData();
        } else {

        }
    }


    OkHttpClient okHttpClient;

    //pageSize使用時會多一個nextPageToken，所以換頁時要塞進pageToken
    private void getAlbumList() {
        okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        Request request = new Request.Builder()
                .url("https://photoslibrary.googleapis.com/v1/albums?pageSize=10")
                .addHeader("Authorization", "Bearer " + mToken)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.v(TAG, "IOException:" + e.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                Log.v(TAG, "result:" + result);
            }
        });

    }

    private void creatAlbum() {
        //LogUnit.v(TAG, "idToken:" + idToken);
        // LogUnit.v(TAG, "idToken:" + accessToken);
        Log.v(TAG, "Token:" + mToken);

        okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);

//會造成Too many follow-up requests: 21 的問題
//        okHttpClient.setAuthenticator(new Authenticator() {
//            @Override
//            public Request authenticate(Proxy proxy, Response response) throws IOException {
//                return response.request().newBuilder().header("Authorization", idToken).build();
//            }
//
//            @Override
//            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
//                return null;
//            }
//        });
        try {
            // array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("title", "www");
            //array.put(object);
            JSONObject object1 = new JSONObject();
            object1.put("album", object);
            Log.e(TAG, "Json:" + object1.toString());
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody formBody = RequestBody.create(JSON, object1.toString());
            Request request = new Request.Builder()
                    .url("https://photoslibrary.googleapis.com/v1/albums")
                    .addHeader("Authorization", "Bearer " + mToken)
                    .post(formBody)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.v(TAG, "IOException:" + e.toString());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String result = response.body().string();
                    Log.v(TAG, "result:" + result);
                }
            });


        } catch (JSONException e) {

        }

    }

    String filePath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_GALLERY) {

            Uri uri = data.getData();//取得圖檔的路徑

            Log.e(TAG + "_onActivityResult", uri.toString());                   //寫log
            filePath = getRealFilePath(this, uri);
            Log.e(TAG + "_onActivityResult", "file Path:" + filePath);
            //ContentResolver cr = this.getContentResolver(); //抽象資料的接口
        }
    }

    public String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private void openAlbum() {
        Intent i = new Intent(Intent.ACTION_PICK, null);
        i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        startActivityForResult(i, PICK_FROM_GALLERY);
    }

    private void uploadPhoto() {
        String SAVEPATH = Environment.getExternalStorageDirectory().getPath();
        Log.v(TAG, "SAVEPATH:" + SAVEPATH);
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        if (filePath == null) {
            return;
        }
        File f = new File(filePath);
        if (!f.exists()) {
            return;
        }
        //  MediaMetadata ff=new MediaMetadata()
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), f);
        Request request = new Request.Builder()
                .url("https://photoslibrary.googleapis.com/v1/uploads")
                .addHeader("Authorization", "Bearer " + mToken)
                .addHeader("Content-type", "application/octet-stream")
                .addHeader("X-Goog-Upload-File-Name", "123")
                .addHeader("X-Goog-Upload-Protocol", "raw")
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = null;
        String uploadToken = "";
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.v(TAG, "IOException:" + e.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                Log.v(TAG, "result:" + result);
                uploadPhoto1(result);
            }
        });
    }

    //50筆限制
    //Photos	JPG, PNG, WEBP, HEIC, some RAW files.	75MB
    //Videos	MPG, MOD, MMV, TOD, WMV, ASF, AVI, DIVX, MOV, M4V, 3GP, 3G2, MP4, M2T, M2TS, MTS, MKV.	10GB

    private void uploadPhoto1(String UPLOAD_TOKEN) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        try {
            JSONObject object = new JSONObject();
            object.put("uploadToken", UPLOAD_TOKEN);
            JSONObject object1 = new JSONObject();
            object1.put("description", "ITEM_DESCRIPTION");
            object1.put("simpleMediaItem", object);
            JSONArray array = new JSONArray();
            array.put(object1);

            JSONObject object2 = new JSONObject();
            object2.put("newMediaItems", array);
            object2.put("albumId", "AMpYkSOMwRAgvRTuWnjUbdNQPksfiTLEfeQovdciz2ywyU_Bm1FxRTaD23jMIqTq3DXJH8OwXOhO");
            JSONObject object3 = new JSONObject();
            object3.put("position", "LAST_IN_ALBUM");
            object2.put("albumPosition", object3);

            Log.e(TAG, "Json:" + object2.toString());
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody formBody = RequestBody.create(JSON, object2.toString());
            Request request = new Request.Builder()
                    .url("https://photoslibrary.googleapis.com/v1/mediaItems:batchCreate")
                    .addHeader("Authorization", "Bearer " + mToken)
                    .addHeader("Content-type", "application/json")
                    .post(formBody)
                    .build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.v(TAG, "IOException:" + e.toString());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String result = response.body().string();
                    Log.v(TAG, "result:" + result);
                }
            });
        } catch (JSONException e) {

        }
    }

}
