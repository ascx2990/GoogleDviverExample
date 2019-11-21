# GoogleDviverExample
Google Drive and Album Example
# 申請權限[(網址)](https://console.cloud.google.com/apis) 
![image](https://github.com/ascx2990/GoogleDviverExample/blob/master/pic/Screenshot_112119_044744_PM.jpg)  
記得要請OAUTH 的Android與網路應用程式各申請一個(弄了超久原來兩個都要申請才能登入)  
注意:網路應用程式的用戶端ID記得要複製到程式碼裡面。  




# Login  
 ```
 GoogleSignInOptions mSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE), new Scope(DriveScopes.DRIVE_APPDATA),
                                new Scope("https://www.googleapis.com/auth/photoslibrary"),
                                new Scope("https://www.googleapis.com/auth/photoslibrary.appendonly"),
                                new Scope("https://www.googleapis.com/auth/photoslibrary.sharing"))
                        .requestIdToken("")
                        .build();
```

1.首先把用端ID複製到 ```requestIdToken```  
2.DriveScopes.DRIVE,DriveScopes.DRIVE_APPDATA分別為Drive的權限  
3.分別為相簿的權限   
"https://www.googleapis.com/auth/photoslibrary",  
                                "https://www.googleapis.com/auth/photoslibrary.appendonly",  
                                "https://www.googleapis.com/auth/photoslibrary.sharing"  
                                
![image](https://github.com/ascx2990/GoogleDviverExample/blob/master/pic/device-2019-11-21-161843.png)

# 功能選則   
分別有:登入畫面，Drive畫面，相簿畫面。 
  
![image](https://github.com/ascx2990/GoogleDviverExample/blob/master/pic/device-2019-11-21-161833.png)
# Drive畫面  
提供讀取Drive內資料夾內容。  
可以到DriveActivty找PARENT_FILE_NAME修改資料夾檔名，可以切換檔案。
也提供方法可以新增文字檔以及修改文字檔內容。  
  
![image](https://github.com/ascx2990/GoogleDviverExample/blob/master/pic/device-2019-11-21-161905.png)
![image](https://github.com/ascx2990/GoogleDviverExample/blob/master/pic/device-2019-11-21-161917.png)


# 相簿
範例寫的比較簡陋一點，用的是OkHttp的方式來上傳。
1.Creat Folder:可以修改方法creatAlbum()裡的 title來變更檔名。  
2.Open Album:來選擇圖片，這邊沒有把圖片顯示出來，只單純紀錄圖片路徑而已，所以如果要上傳照片記得要選圖片。
3.Upload Photo: 記得要先用 get Album得到相簿的albumId才能指定上傳位置。
![image](https://github.com/ascx2990/GoogleDviverExample/blob/master/pic/device-2019-11-21-161932.png)
