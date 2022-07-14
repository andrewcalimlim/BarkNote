package com.example.barknote;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.telephony.SmsManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileReader;
import java.io.File;




import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity {

    protected void textContact(String contact){ //temp method for now
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contact, null, "sms message", null, null);
    }

    protected void savePet(Pet thePet, Context c){
        Gson gson = new Gson();
        String petString = gson.toJson(thePet);

        // I won't lie, this saving from file is like all stack overflow but i don't care lol
        // https://stackoverflow.com/questions/14376807/read-write-string-from-to-a-file-in-android

        try{
            OutputStreamWriter osw = new OutputStreamWriter(c.openFileOutput(
                    "current_pet.json", Context.MODE_PRIVATE));
            osw.write(petString);
            osw.close();
            System.out.println("File written at " + c.getFileStreamPath("current_pet.json"));
        }
        catch(IOException e){
            Log.e("Exception", "JSON File write failed: " + e.toString());
        }

    }

    protected Pet loadPet(Context c){
        // to be replaced with loading data from file
        Gson gson = new Gson();
        Pet thePet = new Pet("default", "888", true);

        try{
            File petJsonFile = c.getFileStreamPath("current_pet.json");
            FileReader fr = new FileReader(petJsonFile);
            thePet = gson.fromJson(fr, Pet.class);
        }
        catch(IOException e){
            Log.e("Exception", "JSON File load failed: " + e.toString());
        }

        return thePet;
    }

    protected void displayName(Context c) {
        Pet thePet = loadPet(c);

        String theName = thePet.getName();
        TextView tv1 = (TextView) findViewById(R.id.name);
        tv1.setText(theName);
    }

    protected void displayStatus(Context c) {
        Pet thePet = loadPet(c);

        String theStatus;

        if(thePet.getCaredForToday()){
            theStatus = "Taken care of already!";
        }
        else{
            theStatus = "Not taken care of yet";
        }

        TextView tv1 = (TextView) findViewById(R.id.status);
        tv1.setText(theStatus);
    }

    protected void displayButton(Context c) {
        Pet thePet = loadPet(c);

        String buttonText;
        boolean shouldBeOff = false;

        if(thePet.getCaredForToday()){
            buttonText = "See you next time!";
            shouldBeOff = true;
        }
        else{
            buttonText = "Taken care of";
        }

        Button b1 = (Button) findViewById(R.id.careAction);
        b1.setText(buttonText);

        if(shouldBeOff){
            b1.setEnabled(false);
        }
        else{
            b1.setEnabled(true);
        }

    }

    protected void displayAll(Context c) {
        displayName(c);
        displayStatus(c);
        displayButton(c);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //launching the main activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // loading data from file and displaying on-screen
        displayAll(this);

        //adding button functionality
        Button careButton = (Button) findViewById(R.id.careAction);
        careButton.setOnClickListener(new View.OnClickListener() {

            // this is an alert that tells the user to make sure pet has been fed and used the
            // bathroom
            @Override
            public void onClick(View view) {

                //getting the proper pet name to display in the alert

                Pet thePet = loadPet(view.getContext());

                String careAlertMessage = "Has " + thePet.getName() + " been taken outside to use" +
                        " the bathroom and been fed?";

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(careAlertMessage);

                // functionality for confirming
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                //System.out.println(":sourPls:");
            }
        });

    }






}