package com.e.googledviverexample.model;

import android.util.Log;
import android.util.Pair;

import com.e.googledviverexample.ui.drive.DriveActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final String TAG = DriveServiceHelper.class.getSimpleName();
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * 讀取資料夾檔案
     * Load data of the file
     */
    public Task<FileList> loadFile() {
        return Tasks.call(mExecutor, () -> {
            FileList rootFileList = mDriveService.files().list()
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, parents)")
                    .setQ("name= '" + DriveActivity.PARENT_FILE_NAME + "' and trashed = false and mimeType = 'application/vnd.google-apps.folder'").execute();
            String rootFileId = null;
            if (rootFileList.getFiles().size() == 0) {
                rootFileId = createFile("root", "application/vnd.google-apps.folder", DriveActivity.PARENT_FILE_NAME).getResult();
            } else {
                rootFileId = rootFileList.getFiles().get(0).getId();
            }
            FileList fileList = mDriveService.files().list()
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, parents,iconLink,webContentLink)")
                    .setQ("'" + rootFileId + "' in parents and trashed = false and (mimeType = 'text/plain' or mimeType = 'application/vnd.google-apps.document')").execute();

            return fileList;
        });
    }

    public Task<String> createFile(String parents, String mimeType, String name) {
        Log.d(TAG, "createFile");
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList(parents))
                    .setMimeType(mimeType)
                    .setName(name);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            return googleFile.getId();
        });
    }

    public Task<Void> deleteDocumentFile(String fileId) {
        return Tasks.call(mExecutor, () -> mDriveService.files().delete(fileId).execute()
        );
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    public Task<String> createDocumentFile(String title, String message) {
        return Tasks.call(mExecutor, () -> {
            FileList rootFileList = mDriveService.files().list()
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, parents)")
                    .setQ("name= '" + DriveActivity.PARENT_FILE_NAME + "' and trashed = false and mimeType = 'application/vnd.google-apps.folder'").execute();
            String rootFileId = null;
            if (rootFileList.getFiles().size() == 0) {
                rootFileId = createFile("root", "application/vnd.google-apps.folder", DriveActivity.PARENT_FILE_NAME).getResult();
            } else {
                rootFileId = rootFileList.getFiles().get(0).getId();
            }
            File metadata = new File()
                    .setParents(Collections.singletonList(rootFileId))
                    .setMimeType("text/plain")
                    .setName(title);

            File googleFile;
            if (message != null) {
                ByteArrayContent messageStream = ByteArrayContent.fromString("text/plain", message);
                googleFile = mDriveService.files().create(metadata, messageStream).execute();
            } else {
                googleFile = mDriveService.files().create(metadata).execute();
            }

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }
    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }
}
