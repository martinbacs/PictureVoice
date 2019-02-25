package application.picturevoice.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import java.util.List;
import java.util.Locale;

import application.picturevoice.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    //OnActivityResult codes
    private static final int SELECT_IMAGE = 100;
    private static final int SELECT_DOCUMENT = 200;

    //global variables
    private Context global;
    private String resultText;
    //init ui
    private TextView tw;
    private Button btnPlay, btnSelect, btnUpload, btnConvertAudio, btnConvertText, btnDisplayText;
    private CheckBox checkBoxTxt, checkBoxMp3, checkBoxPic;
    private ImageView imageViewTxt, imageViewMp3, imageViewPic;
    private ImageView imageView;
    private Uri imageUri;
    private int imageLength;


    //init firebase
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

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

        //init storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        tw = findViewById(R.id.textView);
        tw.setText(currentUser.getEmail());

        //init gui
        btnPlay = findViewById(R.id.btnplay);
        btnSelect = findViewById(R.id.btnselect);
        btnUpload = findViewById(R.id.btnupload);
        btnConvertAudio = findViewById(R.id.btnConvertAudio);
        btnConvertText = findViewById(R.id.btnConvertText);
        btnDisplayText = findViewById(R.id.btnDisplayText);

        imageViewPic = findViewById(R.id.imageViewPic);
        imageViewTxt = findViewById(R.id.imageViewTxt);
        imageViewMp3 = findViewById(R.id.imageViewMp);
        imageView = findViewById(R.id.imageView);

        checkBoxTxt = findViewById(R.id.checkBoxTxt);
        checkBoxMp3 = findViewById(R.id.checkBoxMp);
        checkBoxPic = findViewById(R.id.checkBoxPic);

        //disable gui that requires certain actions to be enabled
        btnConvertText.setEnabled(false);
        btnConvertAudio.setEnabled(false);
        btnPlay.setEnabled(false);
        btnUpload.setEnabled(false);
        btnDisplayText.setEnabled(false);
        imageViewTxt.setEnabled(false);
        imageViewMp3.setEnabled(false);
        checkBoxMp3.setEnabled(false);
        checkBoxTxt.setEnabled(false);
        checkBoxPic.setEnabled(false);

        //make images transparent to indicate that no file has been selected
        imageViewMp3.setAlpha(0.1f);
        imageViewTxt.setAlpha(0.1f);
        imageViewPic.setAlpha(0.1f);

        FirebaseVisionText firebaseVisionText;

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
                btnUpload.setEnabled(false);
                btnDisplayText.setEnabled(false);

                imageViewTxt.setEnabled(false);
                imageViewMp3.setEnabled(false);

                checkBoxMp3.setEnabled(false);
                checkBoxTxt.setEnabled(false);
                checkBoxPic.setEnabled(false);

                imageViewMp3.setAlpha(0.1f);
                imageViewTxt.setAlpha(0.1f);
                imageViewPic.setAlpha(0.1f);
                SelectImageFromGallery();

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(imageUri);
            }
        });


        btnConvertText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start text recognition
                FirebaseVisionImage vImage = null;
                try {
                    vImage = FirebaseVisionImage.fromFilePath(getApplicationContext(), imageUri);
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
                                        btnUpload.setEnabled(true);
                                        btnDisplayText.setEnabled(true);
                                        checkBoxTxt.setEnabled(true);
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
                        tw.setText("");
                        tw.setFocusableInTouchMode(true);
                        tw.requestFocus();
                        resultText = result.getResult().getText();
                        for (FirebaseVisionText.TextBlock block : result.getResult().getTextBlocks()) {
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            System.out.println("[blocktext]" + blockText);

                            //append textview with blockText result
                            tw.append(blockText);
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
                //@TODO GOOGLE API TEXT-TO-SPEECH SERVICE
                // MyAsyncTask myAsyncTask = new MyAsyncTask();
                // myAsyncTask.execute();
                btnPlay.setEnabled(true);
                checkBoxMp3.setEnabled(true);
                imageViewMp3.setAlpha(1f);
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

    public void uploadFile(Uri file) {
        //Uri file = Uri.fromFile(f);
        String fileName = getFileName(file);
        StorageReference riversRef = mStorageRef.child(mAuth.getUid() + "/" + fileName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                        Log.i(TAG, "userid: " + mAuth.getUid());
                        Log.i(TAG, "uploaded file successfully, uri result: " + downloadUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.i(TAG, "uploaded file failed");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    this.imageUri = imageReturnedIntent.getData();
                    this.imageView.setImageURI(this.imageUri);
                    this.btnUpload.setEnabled(true);
                    this.imageViewTxt.setEnabled(true);
                    this.btnConvertText.setEnabled(true);

                    this.imageViewPic.setAlpha(1f);
                    this.checkBoxPic.setEnabled(true);
                }

            case SELECT_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "doc: " + imageReturnedIntent.getData(), Toast.LENGTH_SHORT).show();
                }
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

//    public static class MyAsyncTask extends AsyncTask<Void,Void,Void> {
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            String text = "Hello World";
//            // Instantiates a client
//            System.out.println("init synt text");
//            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
//                // Set the text input to be synthesized
//                SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
//
//                System.out.println("input built");
//                // Build the voice request
//                VoiceSelectionParams voice =
//                        VoiceSelectionParams.newBuilder()
//                                .setLanguageCode("en-US") // languageCode = "en_us"
//                                .setSsmlGender(SsmlVoiceGender.FEMALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
//                                .build();
//
//                System.out.println("voice built");
//                // Select the type of audio file you want returned
//                AudioConfig audioConfig =
//                        AudioConfig.newBuilder()
//                                .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
//                                .build();
//
//                System.out.println("audio config built");
//                // Perform the text-to-speech request
//                SynthesizeSpeechResponse response =
//                        textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
//                System.out.println("response received");
//                // Get the audio contents from the response
//                ByteString audioContents = response.getAudioContent();
//                System.out.println("audio content extracted");
//                // Write the response to the output file.
//                try (OutputStream out = new FileOutputStream("output.mp3")) {
//                    out.write(audioContents.toByteArray());
//                    System.out.println("Audio content written to file \"output.mp3\"");
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
}
