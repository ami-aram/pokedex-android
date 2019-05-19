package com.example.pokedex.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.InputDeviceCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pokedex.R;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 3;
    static final int REQUEST_IMAGE_GALERY = 2;
    static final int REQUEST_PERMISSION_CAMERA = 1;
    static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;

    private static final String IMAGE_DIRECTORY_NAME = "Pictures";

    private Button bCamera;
    private Button bAnalisar;
    private ImageView iFotoPokemon;
    private ImageView iBackBottom;
    private ImageView iBackTop;
    private GifImageView gITop;
    private ProgressBar pBAnalizando;
    private TextView tResultado;

    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iFotoPokemon = findViewById(R.id.imagem);

        pBAnalizando = findViewById(R.id.pb_analizando);

        tResultado = findViewById(R.id.texto_resultado);

        iBackTop = findViewById(R.id.background_topo);

        gITop = findViewById(R.id.gif_background_topo);

        iBackBottom = findViewById(R.id.background_fundo);
        iBackBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getScreenSize();
                toRigth();
            }
        });

        bCamera = findViewById(R.id.botao_camera);
        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Imagem da galeria
//                if (checkSelfPermission()){
//
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//
//                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GALERY);
//                }

                //Camera

                if (checkSelfPermission()) {

                    if (getApplicationContext().getPackageManager().hasSystemFeature(
                            PackageManager.FEATURE_CAMERA)) { // this device has a camera

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        File mediaStorageDir = new File(
                                Environment
                                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                IMAGE_DIRECTORY_NAME);

                        // Create the storage directory if it does not exist
                        if (!mediaStorageDir.exists()) {
                            if (!mediaStorageDir.mkdirs()) {
                                Log.d(this.getClass().getName(), "Oops! Failed create "
                                        + IMAGE_DIRECTORY_NAME + " directory");
                            }else{
                                //Log.d(this.getClass().getName(), "Criou" + IMAGE_DIRECTORY_NAME + " directory");
                            }
                        }else{
                            //Log.d(this.getClass().getName(), "exixte o diretório");
                        }

                        // Create a media file name
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                                Locale.getDefault()).format(new Date());

                        File file = new File(mediaStorageDir.getPath() + File.separator
                                + "IMG_" + timeStamp + ".jpg");


                        uriImage = FileProvider.getUriForFile(
                                getApplicationContext(),
                                getApplicationContext().getPackageName()+".fileprovider",
                                file);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);

                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                    }
                }
            }
        });

        bAnalisar = findViewById(R.id.botao_analisar);
        bAnalisar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //testTouchEvent();

                pBAnalizando.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        File file = createFileFromURI(uriImage);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                iBackTop.setVisibility(View.GONE);
                            }
                        });

                        Log.d(MainActivity.class.getName(),"Enviando arquivo pro servidor");
                        Ion.with(getApplicationContext())
                            .load("https://pokemon.onrender.com/analyze")
                            .setMultipartFile("file", "image/*", file)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    if(result!= null){

                                        toRigth(result.get("result").getAsString());

                                        Log.d(MainActivity.class.getName(),result.get("result").getAsString());
                                        final String resultado = result.get("result").getAsString();

                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                pBAnalizando.setVisibility(View.GONE);
                                                tResultado.setText(resultado);
                                            }
                                        });

                                    }else{
                                        Log.d("oi","nulo");
                                    }
                                }
                            });

                    }

                }).start();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        int[] location = new int[2];
        bCamera.getLocationInWindow(location);
        Log.d(MainActivity.class.getName(),
                "view point x,y (" + location[0]+ ", " + location[1] + ")");

        int x = location[0] + bCamera.getWidth() / 2;
        int y = location[1] + bCamera.getHeight() / 2;

        Log.d(MainActivity.class.getName(),
                "centro point x,y (" + x+ ", " + y + ")");
    }

    private boolean checkSelfPermission (){

        boolean permissionGranted = true;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            permissionGranted = false;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSION_CAMERA);
            permissionGranted = false;
        }

        return permissionGranted;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

//            case REQUEST_IMAGE_GALERY:
//
//                if(resultCode == RESULT_OK){
//
//                    Ion.with(getApplicationContext())
//                            .load("https://pokemon.onrender.com/analyze")
//                            .setMultipartFile("file", "image/*", createFileFromURI(data.getData()))
//                            .asJsonObject()
//                            .setCallback(new FutureCallback<JsonObject>() {
//                                @Override
//                                public void onCompleted(Exception e, JsonObject result) {
//                                    if(result!= null){
//
//                                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
//                                        startActivity(intent);
//                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//
//                                        Log.d("oi",result.get("result").getAsString());
//                                    }else{
//                                        Log.d("oi","nulo");
//                                    }
//                                }
//                            });
//                }
//                break;
            case REQUEST_IMAGE_CAPTURE:

                if(resultCode == RESULT_OK){

                    iFotoPokemon.setVisibility(View.VISIBLE);
                    iFotoPokemon.setImageURI(uriImage);

                }else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(getApplicationContext(),
                            "Cancelado", Toast.LENGTH_LONG)
                            .show();
                }

                break;
        }
    }

   private File createFileFromURI(Uri uri){

        //solução 2
       //File f = new File(getPath(uri));

       //solução 1
//        File f = new File(getApplicationContext().getCacheDir(), "file");
//
//        InputStream imageStream;
//            try {
//                imageStream = this.getContentResolver().openInputStream(uri);
//
//                Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
//
//                try {
//                    f.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                //Convert bitmap to byte array
//                Bitmap bitmap = yourSelectedImage;
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
//                byte[] bitmapdata = bos.toByteArray();
//
//                //Write the bytes in file
//                FileOutputStream fos = new FileOutputStream(f);
//                try {
//                    fos.write(bitmapdata);
//                    fos.flush();
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }

            //solucao 3

       File f = new File(getApplicationContext().getCacheDir(), "file");

       InputStream imageStream;

       try {
           imageStream = this.getContentResolver().openInputStream(uri);

           try {
               OutputStream output = null;
               try {
                   output = new FileOutputStream(f);
               } catch (FileNotFoundException e) {
                   e.printStackTrace();
               }
               try {
                   byte[] buffer = new byte[4 * 1024]; // or other buffer size
                   int read;

                   while ((read = imageStream.read(buffer)) != -1) {
                       output.write(buffer, 0, read);
                   }

                   output.flush();
               } finally {
                   output.close();
               }
           }catch (Exception e){

           }

           imageStream.close();

       } catch (Exception e) {
           e.printStackTrace();
       }

       return f;
   }

   private void toRigth(String... parameters){

       Intent intent = new Intent(MainActivity.this, ResultActivity.class);

        if(parameters != null){
            intent.putExtra("result", parameters);
        }

       startActivity(intent);
       overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
   }

//    public String getPath(Uri uri) {
//        String[] projection = {MediaStore.MediaColumns.DATA};
//        Cursor cursor = managedQuery(uri, projection, null, null, null);
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//        cursor.moveToFirst();
//        Log.d(MainActivity.class.getName(),cursor.getString(column_index));
//        return cursor.getString(column_index);
//    }

    private void getScreenSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.d("Width", "" + width);// 1080
        Log.d("height", "" + height);// 1920
    }

    private void testTouchEvent(){

        try{

            String methodName = "getInstance";
            Object[] objArr = new Object[0];
            InputManager im = (InputManager) InputManager.class.getDeclaredMethod(methodName, new Class[0])
                    .invoke(null, objArr);

            //Make MotionEvent.obtain() method accessible
            methodName = "obtain";
            MotionEvent.class.getDeclaredMethod(methodName, new Class[0]).setAccessible(true);

            //Get the reference to injectInputEvent method
            methodName = "injectInputEvent";
            Method injectInputEventMethod = InputManager.class.getMethod(methodName, new Class[]{InputEvent.class, Integer.TYPE});

            long when = SystemClock.uptimeMillis();
            int source = InputDeviceCompat.SOURCE_TOUCHSCREEN;
            float pressure = 1.0f;
            MotionEvent event = MotionEvent.obtain(when, when, MotionEvent.ACTION_UP, 292, 1656, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
            Log.d(MainActivity.class.getName(), event.toString());
            event.setSource(source);
            injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});

        }catch (Exception e){

        }



        Log.d(MainActivity.class.getName(), "Tocou a tela");
    }

}
