package com.example.slidescan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.canhub.cropper.CropImageView;
import com.canhub.cropper.utils.GetUriForFileKt;
import com.example.slidescan.Models.Notes;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageActivity;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MakeActivity extends AppCompatActivity
{
    private static final int requestCameraCode = 500;
    EditText titleE, noteE;
    ImageView save;
    Notes notes;
    Button record, capture;
    Boolean isOldNote = false;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make);

        titleE = findViewById(R.id.id_editText_title);
        noteE = findViewById(R.id.id_editText_note);
        save = findViewById(R.id.imageview_save);
        record = findViewById(R.id.id_buttonrecord);
        capture = findViewById(R.id.id_buttoncapture);

        notes = new Notes();
        try
        {
            notes = (Notes) getIntent().getSerializableExtra("oldNote");
            titleE.setText(notes.getTitle());
            noteE.setText(notes.getNotes());
            isOldNote = true;
        }
        catch(Exception e)
        {
            Toast.makeText(MakeActivity.this, "New Note Created", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        if(ContextCompat.checkSelfPermission(MakeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MakeActivity.this, new String[] {
                    Manifest.permission.CAMERA
            }, requestCameraCode);
        }

        capture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            { CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MakeActivity.this); }
        });


        save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String title = titleE.getText().toString();
                String note = noteE.getText().toString();

                if(note.isEmpty())
                { Toast.makeText(MakeActivity.this, "Please Add a Note", Toast.LENGTH_SHORT).show(); }

                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm MMM d"); //EEE, MMM d, yyyy
                Date date = new Date();

                if(!isOldNote)
                    notes = new Notes();

                notes.setTitle(title);
                notes.setNotes(note);
                notes.setDate(sdf.format(date));

                Intent intent = new Intent();
                intent.putExtra("note", notes);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void getTextFromImage(Bitmap bitmap)
    {
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if(!recognizer.isOperational())
        { Toast.makeText(MakeActivity.this, "Error Scanning Text", Toast.LENGTH_SHORT).show(); }
        else
        {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < textBlockSparseArray.size(); i++)
            {
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }
            noteE.setText(noteE.getText().toString() + stringBuilder.toString());
            capture.setText("Retake");
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void getSpeech(View view)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, 45);
        else
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case 45:
                if(resultCode == RESULT_OK && data != null)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    noteE.setText(noteE.getText().toString() + result.get(0));
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK)
                {
                    assert result != null;
                    //Uri resultUri = result.getUri;
                    Uri resultUri = GetUriKt.getUri(result);
                    try
                    {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        getTextFromImage(bitmap);
                    }
                    catch (IOException e) { e.printStackTrace(); }
                }
        }
    }
}