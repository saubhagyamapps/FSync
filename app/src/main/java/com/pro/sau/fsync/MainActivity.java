package com.pro.sau.fsync;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pro.sau.fsync.model.upvidsModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final int MULTIPLE_PERMISSIONS = 10;
    private static final String TAG = "MainActivity";
    Button btn_select_file;
    Button upload_file;
    Spinner spinner;
    String type;
    int serverResponseCode = 0;
    TextView file_name;
    String uriString;
    String mSelectedType;
    String mediaPath, mediaPath1;
    ProgressDialog progressDialog;
    String mFileName;
    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    int CAMERA_REQUEST = 1001;
    int GALLERY_REQUEST = 1002;
    int CROP_REQUEST = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        call_permissions();
        file_name = findViewById(R.id.file_name);
        upload_file = findViewById(R.id.upload_file);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        spinner = (Spinner) findViewById(R.id.type);


    /*    upload_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
                if (type.equals("") ){
                }
                else if (type.equals(""))
                {
                }
            }
        });*/
        btn_select_file = findViewById(R.id.select_file);

        selectType();
        upload_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mFileName != null && !mFileName.isEmpty() && !mFileName.equals("null")) {
                    uploadFile();
                } else {
                    if (mSelectedType.equals("Flag")) {
                        Toast.makeText(MainActivity.this, "Select Type.", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, "Please Select File", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    private void call_permissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
        }
        return;
    }

    public void selectType() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemSelected: " + i);
                if (i == 0) {
                    mSelectedType = "Flag";
                    btn_select_file.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mSelectedType = "Flag";
                            Toast.makeText(MainActivity.this, "Select Type.", Toast.LENGTH_SHORT).show();

                        }
                    });

                } else if (i == 1) {
                    mSelectedType = "images";
                    btn_select_file.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final CharSequence[] items = {"Take Photo", "Choose from Library",
                                    "Cancel"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Add Photo!");
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items[item].equals("Take Photo")) {
                                        //  userChoosenTask = "Take Photo";
                                        cameraIntent();
                                    } else if (items[item].equals("Choose from Library")) {
                                        //  userChoosenTask = "Choose from Library";
                                        galleryIntent();
                                    } else if (items[item].equals("Cancel")) {
                                        dialog.dismiss();
                                    }
                                }
                            });
                            builder.show();
                         /*   mSelectedType = "images";
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, 0);*/
                        }
                    });

                } else if (i == 2) {
                    mSelectedType = "video";
                    btn_select_file.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mSelectedType = "video";

                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, 1);

                        }
                    });

                } else if (i == 3) {

                } else if (i == 4) {

                } else {

                }
                String text = spinner.getSelectedItem().toString();
                Log.e(TAG, "selectType: " + text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void galleryIntent() {
        mSelectedType = "images";
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 0);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == 0 && resultCode == RESULT_OK && null != data) {

                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mediaPath = cursor.getString(columnIndex);

                // Set the Image in ImageView for Previewing the Media

                String filename = mediaPath.substring(mediaPath.lastIndexOf("/") + 1);
                Log.e(TAG, "onActivityResult: " + filename);
                file_name.setText(filename);
                mFileName = file_name.getText().toString();
                cursor.close();

            } // When an Video is picked
            else if (requestCode == 1 && resultCode == RESULT_OK && null != data) {

                // Get the Video from data
                Uri selectedVideo = data.getData();
                String[] filePathColumn = {MediaStore.Video.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedVideo, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                mediaPath = cursor.getString(columnIndex);

                String filename = mediaPath.substring(mediaPath.lastIndexOf("/") + 1);
                file_name.setText(filename);
                mFileName = file_name.getText().toString();
                // Set the Video Thumb in ImageView Previewing the Media
                //     imgView.setImageBitmap(getThumbnailPathForLocalFile(MainActivity.this, selectedVideo));
                cursor.close();

            } else if (requestCode == 2 && resultCode == RESULT_OK && null != data) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");


                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri(getApplicationContext(), photo);

                // CALL THIS METHOD TO GET THE ACTUAL PATH
                File finalFile = new File(getRealPathFromURI(tempUri));


            } else {
                Toast.makeText(this, "You haven't picked Image/Video", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    private String getRealPathFromURI(Uri tempUri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(tempUri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mediaPath = cursor.getString(idx);
                String filename = mediaPath.substring(mediaPath.lastIndexOf("/") + 1);
                file_name.setText(filename);
                mFileName = file_name.getText().toString();
                Log.e(TAG, "getRealPathFromURI: " + mediaPath);
                cursor.close();
            }
        }
        return path;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    String displayName = null;
                    Log.e(TAG, "onActivityResult: " + uriString);
                    file_name.setText(uriString);
                    Log.e(TAG, "onActivityResult: " + uri);
                    getMimeType(uriString);
                    Log.e(TAG, "onActivityResult: " + path);
                    if (uriString.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                Log.e(TAG, "onActivityResult: " + displayName);

                            }
                        } finally {
                            cursor.close();
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.getName();
                        Log.e(TAG, "onActivityResult: " + displayName);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    public void getMimeType(String url) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            Log.e(TAG, "getMimeType: " + type);
        }
    }

    // upload photo and video
    private void uploadFile() {
        progressDialog.show();
        Call<upvidsModel> call;
        // Map is used to multipart the file using okhttp3.RequestBody
        File file = new File(mediaPath);
        file_name.setText("");
        mFileName = "";
        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("video", file.getName(), requestBody);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());

        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);
        if (mSelectedType.equals("video")) {
            call = getResponse.uploadVideo(fileToUpload);
        } else {
            RequestBody requestBody1 = RequestBody.create(MediaType.parse("*/*"), file);
            MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("image", file.getName(), requestBody1);
            call = getResponse.uploadImage(fileToUpload1);
        }

        call.enqueue(new Callback<upvidsModel>() {
            @Override
            public void onResponse(Call<upvidsModel> call, Response<upvidsModel> response) {
                upvidsModel serverResponse = response.body();

                if (serverResponse.getStatus().equals("0")) {
                    Toast toast = Toast.makeText(getApplicationContext(), /*serverResponse.getResult()*/"File is uploaded", Toast.LENGTH_SHORT);
                    TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
                    toastMessage.setTextColor(getResources().getColor(R.color.green));
                    toast.show();
                } else {
                    Toast.makeText(getApplicationContext(), serverResponse.getResult(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<upvidsModel> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

/*
// upload pdf file

//     (new Upload(MainActivity.this, path)).execute();

    class Upload extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;
        private Context c;
        private Uri path;

        public Upload(Context c, Uri path) {
            this.c = c;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(c, "Uploading", "Please Wait");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pd.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url_path = "http://192.168.43.50/projectpri/upload.php";
            HttpURLConnection conn = null;

            int maxBufferSize = 1024;
            try {
                URL url = new URL(url_path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setChunkedStreamingMode(1024);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data");

                OutputStream outputStream = conn.getOutputStream();
                InputStream inputStream = c.getContentResolver().openInputStream(path);

                int bytesAvailable = inputStream.available();
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                int bytesRead;
                while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                inputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.i("result", line);
                }
                reader.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    // upload doc or txt file

    public  void  uploaddoc(){

        new Thread(new Runnable()
        {
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                    }
                });
                uploadFile(uriString + "" + uriString);
            }
        }).start();
    }
    public int uploadFile(String sourceFileUri)
    {
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile())
        {

            Log.e("uploadFile", "Source File not exist :"
                    +uriString + "" + uriString);

            runOnUiThread(new Runnable()
            {
                public void run()
                {

                }
            });

            return 0;

        }
        else
        {
            try
            {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(uriString);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200)
                {

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"+" serverpath"
                                    +uriString;

                            Toast.makeText(MainActivity.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            }
            catch (MalformedURLException ex)
            {

                ex.printStackTrace();

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {

                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            }
            catch (Exception e)
            {

                e.printStackTrace();

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {

                    }
                });

            }
            return serverResponseCode;

        }
    }*/
}

