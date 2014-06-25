package com.msk.geotagger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.msk.geotagger.model.Location;
import com.msk.geotagger.model.Settings;
import com.msk.geotagger.utils.DBAdapter;
import com.msk.geotagger.utils.SendDataTask;
import com.msk.geotagger.utils.SendPicTask;


import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;



public class AddActivity extends Activity {

    private double latitude;
    private double longitude;

    private ImageView myImage;
    private Uri imageURI;

    private DBAdapter dba;
    private Settings settings;

    private String photoFileName;           // 글로벌리 유니크한 이름의 임시파일
    private String imagePath;               // 기계에 저장되는 이미지의 절대경로

    private Location loc;

    private Bitmap myImageBitmap;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add);

        myImage = (ImageView) findViewById(R.id.myImage);

        if(savedInstanceState != null)
        {
            if(savedInstanceState.containsKey("imagePath"))
            {
                imagePath = savedInstanceState.getString("imagePath");
                myImageBitmap = BitmapFactory.decodeFile(imagePath);
                myImage.setImageBitmap(myImageBitmap);

            }

        }


        dba = new DBAdapter(AddActivity.this);
        settings = dba.getSettings();


        /**
         * @since 2014-05-08
         * @update 2014-05-08 이준원
         */
        // 질문 1
        final CheckBox chkQ1_1 = (CheckBox)findViewById(R.id.chk_q1_1); // Evangelism
        final CheckBox chkQ1_2 = (CheckBox)findViewById(R.id.chk_q1_2); // Training
        final CheckBox chkQ1_3 = (CheckBox)findViewById(R.id.chk_q1_3); // Mercy

        // 질문 2
        final CheckBox chkQ2_1 = (CheckBox)findViewById(R.id.chk_q2_1); // Youth / Children
        final CheckBox chkQ2_2 = (CheckBox)findViewById(R.id.chk_q2_2); // Campus Ministry
        final CheckBox chkQ2_3 = (CheckBox)findViewById(R.id.chk_q2_3); // Indigenous People
        final CheckBox chkQ2_4 = (CheckBox)findViewById(R.id.chk_q2_4); // Prison Ministry
        final CheckBox chkQ2_5 = (CheckBox)findViewById(R.id.chk_q2_5); // Prostitutes
        final CheckBox chkQ2_6 = (CheckBox)findViewById(R.id.chk_q2_6); // Orphans
        final CheckBox chkQ2_7 = (CheckBox)findViewById(R.id.chk_q2_7); // Woman
        final CheckBox chkQ2_8 = (CheckBox)findViewById(R.id.chk_q2_8); // Urban Ministry
        final CheckBox chkQ2_9 = (CheckBox)findViewById(R.id.chk_q2_9); // Hospital Ministry
        final CheckBox chkQ2_10 = (CheckBox)findViewById(R.id.chk_q2_10); // Media / Communications
        final CheckBox chkQ2_11 = (CheckBox)findViewById(R.id.chk_q2_11); // Community Development
        final CheckBox chkQ2_12 = (CheckBox)findViewById(R.id.chk_q2_12); // Bible Studies
        final CheckBox chkQ2_13 = (CheckBox)findViewById(R.id.chk_q2_13); // Church Planting
        final CheckBox chkQ2_14 = (CheckBox)findViewById(R.id.chk_q2_14); // Arts / Entertainment / Sports
        final CheckBox chkQ2_15 = (CheckBox)findViewById(R.id.chk_q2_15); // Counseling
        final CheckBox chkQ2_16 = (CheckBox)findViewById(R.id.chk_q2_16); // Healthcare
        final CheckBox chkQ2_17 = (CheckBox)findViewById(R.id.chk_q2_17); // Maintenance / Construction
        final CheckBox chkQ2_18 = (CheckBox)findViewById(R.id.chk_q2_18); // Research

        // 질문 3
        final EditText editText1 = (EditText)findViewById(R.id.editText1); // description

        // 질문 4
        final EditText editText2 = (EditText)findViewById(R.id.EditText2); // keywords (comma)

        // 질문 5 - 정보 수집 동의
        final CheckBox chkQ5 = (CheckBox)findViewById(R.id.chk_q5);

        final EditText editText3 = (EditText)findViewById(R.id.editText3); // email
        final EditText editText4 = (EditText)findViewById(R.id.editText4); // phone
        final EditText editText5 = (EditText)findViewById(R.id.editText5); // website

        TextView btnCancel = (TextView)findViewById(R.id.btn_cancel);
        TextView btnSave = (TextView)findViewById(R.id.btn_save);





        Intent i = getIntent();
        latitude = i.getDoubleExtra("latitude", 0);
        longitude = i.getDoubleExtra("longitude", 0);


        TextView btnTakePhoto = (TextView)findViewById(R.id.btn_take_photo);
        TextView btnSelectPhoto = (TextView)findViewById(R.id.btn_select_photo);

        btnTakePhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 81);
            }
        });

        btnSelectPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 82);
            }
        });


        Log.d("위도", ""+latitude);
        Log.d("경도", ""+longitude);



        btnCancel.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {

                finish();
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Wait...");
        progressDialog.setMessage("Uploading photo");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);




        /**
         * @since 2014-05-08
         * @update 2014-05-08 이준원
         */
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                loc = new Location();

                // Question 1
                if(chkQ1_1.isChecked())
                {
                    loc.setEvanType(1);
                }

                if(chkQ1_2.isChecked())
                {
                    loc.setTrainType(1);
                }

                if(chkQ1_3.isChecked())
                {
                    loc.setMercyType(1);
                }

                // Question 2
                if(chkQ2_1.isChecked())
                {
                    loc.setYouthType(1);
                }

                if(chkQ2_2.isChecked())
                {
                    loc.setCampusType(1);
                }

                if(chkQ2_3.isChecked())
                {
                    loc.setIndigenousType(1);
                }

                if(chkQ2_4.isChecked())
                {
                    loc.setPrisonType(1);
                }

                if(chkQ2_5.isChecked())
                {
                    loc.setProstitutesType(1);
                }

                if(chkQ2_6.isChecked())
                {
                    loc.setOrphansType(1);
                }

                if(chkQ2_7.isChecked())
                {
                    loc.setWomenType(1);
                }

                if(chkQ2_8.isChecked())
                {
                    loc.setUrbanType(1);
                }

                if(chkQ2_9.isChecked())
                {
                    loc.setHospitalType(1);
                }

                if(chkQ2_10.isChecked())
                {
                    loc.setMediaType(1);
                }

                if(chkQ2_11.isChecked())
                {
                    loc.setCommunityDevType(1);
                }

                if(chkQ2_12.isChecked())
                {
                    loc.setBibleStudyType(1);
                }

                if(chkQ2_13.isChecked())
                {
                    loc.setChurchPlantingType(1);
                }

                if(chkQ2_14.isChecked())
                {
                    loc.setArtsType(1);
                }

                if(chkQ2_15.isChecked())
                {
                    loc.setCounselingType(1);
                }

                if(chkQ2_16.isChecked())
                {
                    loc.setHealthcareType(1);
                }

                if(chkQ2_17.isChecked())
                {
                    loc.setConstructionType(1);
                }

                if(chkQ2_18.isChecked())
                {
                    loc.setResearchType(1);
                }

                loc.setDesc(editText1.getText().toString());
                loc.setTags(editText2.getText().toString());

                // Question 5 - yes | no
                if(chkQ5.isChecked())
                {
                    loc.setContactConfirmed(1);
                }

                loc.setContactEmail(editText3.getText().toString());
                loc.setContactPhone(editText4.getText().toString());
                loc.setContactWebsite(editText5.getText().toString());

                loc.setLatitude(latitude);
                loc.setLongitude(longitude);

                loc.setCreated(new Timestamp(System.currentTimeMillis()));
                Log.d("created", loc.getCreatedTimestamp().toString());

				/* http://shstarkr.tistory.com/158 참고 */
                ConnectivityManager cManager;
                NetworkInfo mobile;
                NetworkInfo wifi;

                cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                mobile = cManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                wifi = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if(settings.getOffline() == 0 && (mobile.isConnected() || wifi.isConnected()))
                {
                    //3G 또는 WiFi 에 연결되어 있을 경우
                    // api key check
                    if( settings.getUsername() == null || "".equals(settings.getUsername().trim()) || settings.getApiKey() == null || "".equals(settings.getApiKey().trim()))
                    {
                        // show dialog

                        int identifier = getResources().getIdentifier("auth_error", "string", "com.msk.geotagger");

                        String auth_error = "Authentication Error";
                        if(identifier != 0)
                        {
                            auth_error = getResources().getString(identifier);
                        }

                        identifier = getResources().getIdentifier("apikey_missing", "string", "com.msk.geotagger");

                        String apikey_missing = "Username or Api Key is missing !!";

                        if(identifier != 0)
                        {
                            apikey_missing = getResources().getString(identifier);
                        }
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddActivity.this);
                        alertDialogBuilder.setTitle(auth_error);
                        alertDialogBuilder.setMessage(apikey_missing);
                        alertDialogBuilder.setPositiveButton("확인", dismissListener);
                        alertDialogBuilder.show();
                    }

                    else // 설정에 username 항목과 api key 항목이 저장되어 있을 경우
                    {
                        /// 파일 업로드 ////
                        if (myImageBitmap != null)
                        {
                            File photoFile = null;

                            if (imagePath != null) {
                                photoFile = new File(imagePath);
                                loc.setPhotoRealPath(imagePath);
                            }


                            photoFileName = dba.getSettings().getUsername() + loc.getCreatedTimestamp().toString().trim() + ".jpg";
                            final File photoFile2 = new File(photoFile.getParentFile().toString()+photoFileName);

                            int newHeight;
                            if(myImageBitmap.getHeight() > 400)
                            {
                                newHeight = 400;
                            }
                            else if(myImageBitmap.getHeight() < myImageBitmap.getWidth())
                            {
                                newHeight = 200;
                            }
                            else
                            {
                                newHeight = myImageBitmap.getHeight();
                            }



                            final float densityMultiplier = AddActivity.this.getResources().getDisplayMetrics().density;
                            int h= (int) (newHeight*densityMultiplier);
                            int w= (int) (h * myImageBitmap.getWidth()/((double) myImageBitmap.getHeight()));

                            Bitmap photo = Bitmap.createScaledBitmap(myImageBitmap, w, h, true);

                            try {
                                photoFile2.createNewFile();
                                OutputStream out = new FileOutputStream(photoFile2);
                                photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if(photoFile2.exists()) {

                                JsonObject json = new JsonObject();
                                json.addProperty("credential", dba.getSettings().getUsername()+":"+dba.getSettings().getApiKey());
                                json.addProperty("username", dba.getSettings().getUsername());
                                json.addProperty("filepath", photoFile2.getAbsolutePath());

                                progressDialog.show();
                                sendPhoto(json);


                            }

                            else // 사진파일 불러오기 실패
                            {
                                Log.d("사진 업로드", "사진 불러오기 실패!!");

                                sendLocation();

                                loc.setSync(1);
                                dba.insertLocation(loc);
                            }
                        }

                        else            // 사진파일이 없을 때
                        {
                            sendLocation();

                            loc.setSync(1);
                            dba.insertLocation(loc);
                            finish();
                        }
                    } // api 키 관련 if
                } // 인터넷 접속 관련 if

                else    // 인터넷이 안될 때
                {
                    if(myImageBitmap != null)
                    {
                        if(imagePath != null)
                            loc.setPhotoRealPath(imagePath);

                        loc.setSync(0);
                        dba.insertLocation(loc);
                    }

                    else
                    {
                        loc.setSync(0);
                        dba.insertLocation(loc);
                    }

                    finish();
                }
            }
        });
        // setOnClickListener
    } // onCreate


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(imagePath != null) {
            outState.putString("imagePath", imagePath);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            if( requestCode == 81 || requestCode == 82 )
            {
                imageURI = data.getData();

                Cursor cursor = null;
                try
                {
                    String[] proj = { MediaStore.Images.Media.DATA };
                    cursor = AddActivity.this.getContentResolver().query(imageURI,  proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    imagePath = cursor.getString(column_index);
                }

                finally
                {
                    if (cursor != null)
                    {
                        cursor.close();
                    }
                }


                try {
                    myImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                    if( myImageBitmap != null )
                        myImage.setImageBitmap(myImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } // else if 82

        } // if RESULT_OK
    } // onActivityResult

    private void sendLocation()
    {
        JsonObject json = loc.toJSON();
        json.addProperty("credential", dba.getSettings().getUsername()+":"+dba.getSettings().getApiKey());
        new SendLocationTask().execute(json);
    }

    private void sendPhoto(JsonObject json)
    {
        new SendPhotoTask().execute(json);
    }

    private DialogInterface.OnClickListener dismissListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
        }
    };

    private class SendLocationTask extends SendDataTask
    {
        @Override
        protected void onPostExecute(HttpResponse httpResponse) {
            super.onPostExecute(httpResponse);

            AddActivity.this.finish();
        }
    }

    private class SendPhotoTask extends SendPicTask
    {
        @Override
        protected void onPostExecute(HttpResponse httpResponse) {
            super.onPostExecute(httpResponse);

            progressDialog.dismiss();

            loc.setPhotoId(photoFileName);
            sendLocation();

            loc.setSync(1);
            dba.insertLocation(loc);

            finish();
        }
    }
}
