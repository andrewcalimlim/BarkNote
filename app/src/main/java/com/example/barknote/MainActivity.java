package com.example.barknote;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileReader;
import java.io.File;

import com.google.gson.Gson;

/***
 * NOTE TO SELF: /data/user/0/com.example.barknote/files/current_pet.json
 * make sure to SYNCHRONIZE folder too if not visible
 */


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

    // ok but THIS load function is all me, baby, all me
    // sourPls
    protected Pet loadPet(Context c){
        // to be replaced with loading data from file
        Gson gson = new Gson();
        Pet thePet = new Pet("default", "888", true);

        try{
            File petJsonFile = c.getFileStreamPath("current_pet.json");
            Log.i("JSON FILE LOADED", "from" + petJsonFile.getAbsolutePath());
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

    protected void displayMain(Context c) {
        displayName(c);
        displayStatus(c);
        displayButton(c);
    }

    /***
     * checks if json file exists, therefore setup is required
     * @param c = the Android context needed..i think
     */
    protected boolean setupNeeded(Context c){
        File petJsonFile = c.getFileStreamPath("current_pet.json");

        // if current_pet.json exists, no setup needed
        if(petJsonFile.isFile() && !petJsonFile.isDirectory()){
            return false;
        }
        // if current_pet.json does not exist, setup is needed
        return true;
    }

    // only used in main activity but this makes main activity not a god function

    protected void displaySetup(Context c){


        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Welcome to BarkNote!");
        builder.setMessage("Please fill out the following details.");


        // INPUTS

        // sigh, this is all mainly copied..
        // https://stackoverflow.com/questions/9345735/resizing-edittext-inside-of-an-alertdialog
        // BUT i was the one who figured out that you could write this outside of the main activity

        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(60,0,60,0);

        final EditText petNameInput = new EditText(c);
        petNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        petNameInput.setHint("Your Pet's Name");
        layout.addView(petNameInput, params);

        final EditText contactInput = new EditText(c);
        contactInput.setInputType(InputType.TYPE_CLASS_PHONE);
        contactInput.setHint("Contact Number to Text");
        layout.addView(contactInput, params);

        CheckBox caredQuery = new CheckBox(c);
        caredQuery.setText("Pet has already been taken care of today");
        layout.addView(caredQuery, params);

        builder.setView(layout);


        builder.setCancelable(false); //gotta submit! Not allowed to skip this info

        // BUTTON

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String petName = petNameInput.getText().toString();
                String contact = contactInput.getText().toString();
                boolean cared = caredQuery.isChecked();

                Pet thePet = new Pet(petName, contact, cared);
                savePet(thePet, c);
                displayMain(c);

            }
        });

        AlertDialog dialog = builder.create();


        /***
         * These listeners are here to ensure proper submission forms happen (basically
         * form submission is disabled when text inputs are empty, enabled when they both aren't.
         */

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            }
        });

        contactInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String petName = petNameInput.getText().toString();
                String contact = contactInput.getText().toString();

                if(!petName.isEmpty() && !contact.isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        petNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String petName = petNameInput.getText().toString();
                String contact = contactInput.getText().toString();

                if(!petName.isEmpty() && !contact.isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }

        });


        dialog.show();

    }

    protected void setupCheck(Context c){
        if(setupNeeded(c)){
            displaySetup(c);
        }
    }


    /* MAIN ACTIVITY */

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //launching the main activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupCheck(this);

        // loading data from file and displaying on-screen
        displayMain(this);

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
            }
        });

    }






}