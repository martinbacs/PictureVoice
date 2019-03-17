package application.picturevoice.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import application.picturevoice.R;
import application.picturevoice.classes.CloudFile;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    //OnActivityResult codes
    private static final int SELECT_IMAGE = 100;
    private static final int SELECT_DOCUMENT = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 300;

    //global variables
    private Context global;
    private String resultText;
    private boolean isImageURI;

    //init ui
    private TextView textViewAudioToTextResult, textViewProfile;
    private Button btnPlay, btnSelect, btnNewImage, btnUploadImage, btnUploadText, btnConvertAudio, btnConvertText, btnDisplayText;
    private ImageView imageViewTxt, imageViewPic;
    private ImageView imageViewResult;
    private Uri imageUri;
    private Bitmap imageBitmap;
    private int imageLength;

    //init firebase
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseDatabase database;

    //init text-to-speech
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //FirebaseApp.initializeApp(this);


        global = getApplicationContext();

        //check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        //init storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        textViewProfile = findViewById(R.id.twprofile);
        textViewAudioToTextResult = findViewById(R.id.twresult);

        //init gui
        btnPlay = findViewById(R.id.btnplay);
        btnSelect = findViewById(R.id.btnselect);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnUploadText = findViewById(R.id.btnUploadText);
        btnConvertAudio = findViewById(R.id.btnConvertAudio);
        btnConvertText = findViewById(R.id.btnConvertText);
        btnDisplayText = findViewById(R.id.btnDisplayText);
        btnNewImage = findViewById(R.id.btnnew);

        imageViewPic = findViewById(R.id.imageViewPic);
        imageViewTxt = findViewById(R.id.imageViewTxt);
        imageViewResult = findViewById(R.id.imageViewResult);


        //disable gui that requires certain actions to be enabled
        btnConvertText.setEnabled(false);
        btnConvertAudio.setEnabled(false);
        btnPlay.setEnabled(false);
        btnUploadImage.setEnabled(false);
        btnUploadText.setEnabled(false);
        btnDisplayText.setEnabled(false);
        imageViewTxt.setEnabled(false);


        //make images transparent to indicate that no file has been selected
        imageViewTxt.setAlpha(0.1f);
        imageViewPic.setAlpha(0.1f);

        FirebaseVisionText firebaseVisionText;

        //set profile to current user
        textViewProfile.setText(currentUser.getEmail());

        textViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        //load tts
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);

                }
            }
        });


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tts.isSpeaking()) {
                    tts.stop();
                } else {
                    tts.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, "1");
                }
            }
        });


        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnConvertText.setEnabled(false);
                btnConvertAudio.setEnabled(false);
                btnPlay.setEnabled(false);
                btnUploadImage.setEnabled(false);
                btnUploadText.setEnabled(false);
                btnDisplayText.setEnabled(false);

                imageViewTxt.setEnabled(false);

                imageViewTxt.setAlpha(0.1f);
                imageViewPic.setAlpha(0.1f);
                SelectImageFromGallery();

            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(imageUri);
            }
        });

        btnNewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCapturePictureIntent();
            }
        });

        btnUploadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //convert string to text file
                //upload file text file to cloud
                File file = writeToFile(getApplicationContext());

                if (file != null) {

                    Uri uri = Uri.fromFile(file);
                    uploadFile(uri);

                } else {
                    Log.d(TAG, "File does not exist");
                }
            }
        });

        btnConvertText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start text recognition
                FirebaseVisionImage vImage = null;

                try {
                    //maybe need to check when uploading file aswell?
                    if (isImageURI) {
                        vImage = FirebaseVisionImage.fromFilePath(getApplicationContext(), imageUri);

                    } else {
                        vImage = FirebaseVisionImage.fromBitmap(imageBitmap);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();
                final Task<FirebaseVisionText> result =
                        detector.processImage(vImage)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                        // Task completed successfully
                                        Toast.makeText(getApplicationContext(), "image to text conversion was successful", Toast.LENGTH_SHORT).show();
                                        btnConvertAudio.setEnabled(true);
                                        btnUploadText.setEnabled(true);
                                        btnDisplayText.setEnabled(true);
                                        imageViewTxt.setAlpha(1f);

                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                                Toast.makeText(getApplicationContext(), "image to text conversion failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });


                btnDisplayText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //get result from picture to text conversion
                        textViewAudioToTextResult.setText("");
                        textViewAudioToTextResult.setFocusableInTouchMode(true);
                        textViewAudioToTextResult.requestFocus();
                        resultText = result.getResult().getText();
                        for (FirebaseVisionText.TextBlock block : result.getResult().getTextBlocks()) {
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            System.out.println("[blocktext]" + blockText);

                            //append textview with blockText result
                            textViewAudioToTextResult.append(blockText);
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                System.out.println("[linetext]" + lineText);
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }
                    }
                });
            }

        });


        btnConvertAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlay.setEnabled(true);
                Toast.makeText(getApplicationContext(), "text-to-audio was successful", Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void SelectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    private void SelectFileFromDevice() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Document"), SELECT_DOCUMENT);
    }

    //upload file to google cloud bucket
    public void uploadFile(final Uri file) {
        final String fileName = getFileName(file);

        StorageReference riversRef = mStorageRef.child(mAuth.getUid() + "/" + fileName);

        //upload to database
        final DatabaseReference databaseReference = database.getReference("Users/" + mAuth.getUid() + "/files");


        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //get file size
                        double fileSize = 0;
                        try {
                            AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(file, "r");
                            fileSize = afd.getLength();
                            fileSize = fileSize / 1024;
                            afd.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //firebase database
                        CloudFile cloudFile = new CloudFile(fileName, String.valueOf(fileSize));
                        databaseReference.push().setValue(cloudFile);

                        //cloud storage
                        // Get a URL to the uploaded content
                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                        Log.i(TAG, "userid: " + mAuth.getUid());
                        Log.i(TAG, "uploaded file successfully, uri result: " + downloadUrl);

                        Toast.makeText(getApplicationContext(), "uploaded file successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.i(TAG, "uploaded file failed");
                        Toast.makeText(getApplicationContext(), "failed to upload file, please try again.", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    isImageURI = true;
                    this.imageUri = imageReturnedIntent.getData();

                    this.imageViewResult.setImageURI(this.imageUri);
                    this.btnUploadImage.setEnabled(true);
                    this.imageViewTxt.setEnabled(true);
                    this.btnConvertText.setEnabled(true);
                    this.imageViewPic.setAlpha(1f);
                    break;
                }

            case SELECT_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "doc: " + imageReturnedIntent.getData(), Toast.LENGTH_SHORT).show();
                    break;
                }

            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    isImageURI = false;
                    Bundle extras = imageReturnedIntent.getExtras();
                    //Bitmap imageBitmap = (Bitmap) extras.get("data");
                    this.imageBitmap = (Bitmap) extras.get("data");
                    this.imageViewResult.setImageBitmap(this.imageBitmap);

                    this.btnUploadImage.setEnabled(true);
                    this.imageViewTxt.setEnabled(true);
                    this.btnConvertText.setEnabled(true);
                    this.imageViewPic.setAlpha(1f);
                    break;

                }
        }
    }

    private void dispatchCapturePictureIntent() {
        Intent capturePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (capturePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(capturePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private String getFileName(Uri uri) {
        String result;
        //if uri is content
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = global.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    //local filesystem
                    int index = cursor.getColumnIndex("_data");
                    if (index == -1)
                        //google drive
                        index = cursor.getColumnIndex("_display_name");
                    result = cursor.getString(index);
                    if (result != null)
                        uri = Uri.parse(result);
                    else
                        return null;
                }
            } finally {
                cursor.close();
            }
        }

        result = uri.getPath();

        //get filename + ext of path
        int cut = result.lastIndexOf('/');
        if (cut != -1)
            result = result.substring(cut + 1);
        return result;
    }


    private File writeToFile(Context context) {
        try {
            File path = context.getFilesDir();
            File file = new File(path, "my-file-name.txt");

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(resultText.getBytes());
            } finally {
                stream.close();
            }

            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            String contents = new String(bytes);
            System.out.println("read from file: " + contents);

            return file;
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return null;
        }
    }

}
