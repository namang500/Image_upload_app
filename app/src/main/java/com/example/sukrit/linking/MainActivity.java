package com.example.sukrit.linking;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    RequestQueue requestQueue;
    Button btnSelect;
    ImageView imgView;
    private int PICK_IMAGE_REQUEST = 100;
    private Bitmap bitmap;
    TextView txtStatus,txtName;
    String imageStr="";
    String url="http://192.168.43.181:5002/image";
    public static final String MY_PREFS="FlaskData";
    int i=0;
    EditText etName;
    String personName="",imagePath="";
    ArrayList<String> namesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        namesArrayList=new ArrayList<>();
        namesArrayList.add("Naman");
        namesArrayList.add("Vaibhav");
        namesArrayList.add("Aditya");
        namesArrayList.add("Siddhant");
        namesArrayList.add("Noddy");
        namesArrayList.add("Utkarsh");

        etName= (EditText) findViewById(R.id.name);
        txtName= (TextView) findViewById(R.id.txtName);

        requestQueue= Volley.newRequestQueue(this);

        btnSelect= (Button) findViewById(R.id.selectBtn);
        imgView= (ImageView) findViewById(R.id.imgView);
        txtStatus= (TextView) findViewById(R.id.txtStatus);
        btnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    personName=etName.getText().toString();
                    //Toast.makeText(MainActivity.this, personName, Toast.LENGTH_SHORT).show();
                    showFileChooser();
                }
            });

        Log.d("TAG", "onCreate: ImageStr: "+imageStr);

    }

    public void volleyFunction(){
        StringRequest request=new StringRequest(Request.Method.POST, url, new Response.Listener<String>()    {
            @Override
            public void onResponse(String response) {
                Log.d("TAGGER", "onResponse: "+response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAGGER", "onFailure: "+error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> params=new HashMap<>();

                Log.d("TAG", "getParams: Person: "+personName);
                params.put("image",imageStr);
               // params.put("count",String.valueOf(i));
                params.put("dir",personName);
               // params.put("imagePath",imagePath);
             //   params.put("namesArrayList",namesArrayList.toString());
                return params;
            }
        };
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });////
        requestQueue.add(request);

    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

        /*    filePath = data.getData();
            selectedFilePath = getPath(filePath);
            Log.i(TAG, " File path : " + selectedFilePath);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            Uri selectedImageUri = data.getData();
            imagePath = getPath(this,selectedImageUri);
            Log.d("TAG", "onActivityResult: pa: "+imagePath);
            Log.d("HHH", "onActivityResult: jjk:   "+data.getData().getPath());
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap tempBitmap = MyBitmapCompressor.getCompressedImage(imagePath,512,512);
                tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); //compress to which format you want.

                Log.d("TAG", "onActivityResult: uri:  "+ selectedImageUri.toString());

                Log.d("TAG", "onActivityResult: path:  "+getPath(this,selectedImageUri));
                byte[] byte_arr = stream.toByteArray();
                imageStr = Base64.encodeToString(byte_arr,Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imgView.setImageBitmap(bitmap);
            txtStatus.setText(imagePath);
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(MY_PREFS,MODE_PRIVATE);
            SharedPreferences.Editor editor=preferences.edit();
            i++;
            editor.putInt("counter",preferences.getInt("counter",0));
            volleyFunction();
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
