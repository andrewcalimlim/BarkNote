package com.example.barknote;

import static android.Manifest.permission.SEND_SMS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.LocalTime;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileReader;
import java.io.File;
import java.time.format.DateTimeFormatter;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * NOTE TO SELF: /data/user/0/com.example.barknote/files/current_pet.json
 * make sure to SYNCHRONIZE folder too if not visible
 */


public class MainActivity extends AppCompatActivity {

    /***
     * PET-JSON FILE INTERACTIONS
     */

    protected void savePet(Pet thePet, Context c){
        //Log.i("BOUT TO SAVE THIS PET TO FILE YO: ", thePet.toString());
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();

        String petString = gson.toJson(thePet);

        // I won't lie, this saving from file is like all stack overflow but i don't care lol
        // https://stackoverflow.com/questions/14376807/read-write-string-from-to-a-file-in-android

        try{
            OutputStreamWriter osw = new OutputStreamWriter(c.openFileOutput(
                    "current_pet.json", Context.MODE_PRIVATE));
            osw.write(petString);
            osw.close();
            //System.out.println("File written at " + c.getFileStreamPath("current_pet.json"));
        }
        catch(IOException e){
            Log.e("Exception", "JSON File write failed: " + e.toString());
        }

    }

    // ok but THIS load function is all me, baby, all me
    // sourPls
    protected Pet loadPet(Context c){
        Gson gson = new Gson();
        Pet thePet = new Pet(null, null, false, null);
        //Log.i("HEADS-UP: ", thePet.toString());

        try{
            File petJsonFile = c.getFileStreamPath("current_pet.json");
            Log.i("JSON FILE LOADED", "from" + petJsonFile.getAbsolutePath());
            FileReader fr = new FileReader(petJsonFile);
            thePet = gson.fromJson(fr, Pet.class);
            //thePet.checkCare();
            //savePet(thePet, c);
        }
        catch(IOException e){
            Log.e("Exception", "JSON File load failed: " + e.toString());
        }

        return thePet;
    }

    /***
     * DISPLAY FEATURES ON MAIN ACTIVITY
     */

    protected void displayName(Pet thePet, Context c) {
        String theName = thePet.getName();
        TextView tv1 = (TextView) findViewById(R.id.name);
        tv1.setText(theName);
    }

    protected void displayStatus(Pet thePet, Context c) {
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

    protected void displayButton(Pet thePet, Context c) {
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

    protected void displayMain(Pet thePet, Context c) {
        displayName(thePet, c);
        displayStatus(thePet, c);
        displayButton(thePet, c);
    }

    /***
     * FIRST-TIME SETUP FUNCTIONALITY
     */

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
                String caredDate = null;

                if(cared){
                    caredDate = LocalDate.now().toString();
                }


                Pet thePet = new Pet(petName, contact, cared, caredDate);
                savePet(thePet, c);
                Log.i("SAVE SUCCESS", "cared: " + cared);
                Log.i("PET OBJ: ", thePet.toString());
                //displayRequestWarning(thePet, c);
                displayMain(thePet, c);

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

    /***
     * PET UPDATE FUNCTIONALITY
     */

    protected void displayUpdateCancelled(Pet thePet, Context c){

        String cancelMessage = "No text message was sent. Please take care of " +
                thePet.getName() + " before trying to update again.";
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Pet Update Cancelled");
        builder.setMessage(cancelMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    protected void displayUpdateSuccess(Pet thePet, Context c){
        String successMessage = "BarkNote text message sent to " + thePet.getContact() +
                ". " + thePet.getName() + " thanks you!";
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Pet Update Succeeded!");
        builder.setMessage(successMessage);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /***
     * SMS PERMISSION TO TEXT & SMS TEXT FUNCTIONALITY
     */

    protected void textContact(String contact, String textMessage){ //temp method for now
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contact, null, textMessage, null, null);
    }

    protected boolean requestNeeded(Context c){
        int verdict = ContextCompat.checkSelfPermission(c,
                SEND_SMS);
        if(verdict == -1){
            return true;
        }
        return false;
    }

    protected void setupCheck(Context c){
        if(setupNeeded(c)){
            displaySetup(c);
        }
    }

    protected void displayRequestWarning(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage("Please accept the following" +
                " SMS permission sending request so that BarkNote can automatically send texts" +
                " to your requested contact.\nBarkNote will only text the contact number you" +
                " set earlier whenever you update the pet's care in the app (once a day at most).");
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                displayRequestDeniedResponse(c);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    protected void displayRequestDeniedResponse(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage("BarkNote will not SMS text your contact on update then.");
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        builder.setPositiveButton("OK", null);
        dialog.show();

    }


    /* MAIN ACTIVITY */

    @Override
    protected void onCreate(Bundle savedInstanceState){

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher, as an instance variable.
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.

                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.

                        displayRequestDeniedResponse(MainActivity.this);

                    }
                });

        //launching the main activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupCheck(this);

        Pet thePet = loadPet(this);
        thePet.checkCare();
        savePet(thePet, this);


        // loading data from file and displaying on-screen
        displayMain(thePet, this);

        //adding button functionality
        Button careButton = (Button) findViewById(R.id.careAction);
        careButton.setOnClickListener(new View.OnClickListener() {

            // this is an alert that tells the user to make sure pet has been fed and used the
            // bathroom
            @Override
            public void onClick(View view) {

                //getting the proper pet name to display in the alert

                // what happens if i reload the pet here...
                Pet thePet = loadPet(view.getContext());

                String careAlertMessage = "Has " + thePet.getName() + " been taken outside to use" +
                        " the bathroom and been fed?";

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(careAlertMessage);

                // functionality for confirming
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        thePet.updateCare();
                        savePet(thePet, MainActivity.this);

                        // getting the current time
                        LocalTime rn = LocalTime.now();
                        String pattern = "h:mm a";
                        String theTime = rn.format(DateTimeFormatter.ofPattern(pattern));

                        //creating a text
                        String theTextMessage = "Hello! This is an automated BarkNote text " +
                        "confirming that " + thePet.getName() + " has been taken care of " +
                        "(walked, used the bathroom, and fed) at " + theTime;


                        if (ContextCompat.checkSelfPermission(MainActivity.this, SEND_SMS) ==
                                PackageManager.PERMISSION_GRANTED) {
                            // You can use the API that requires the permission.

                            // All is well! (insert dance number from 3 Idiots here)
                            // sending the text uhhh shit
                            textContact(thePet.getContact(), theTextMessage);

                        } else if (shouldShowRequestPermissionRationale(SEND_SMS)) {
                            // In an educational UI, explain to the user why your app requires this
                            // permission for a specific feature to behave as expected. In this UI,
                            // include a "cancel" or "no thanks" button that allows the user to
                            // continue using your app without granting the permission.
                            displayRequestWarning(MainActivity.this);
                        } else {
                            // You can directly ask for the permission.
                            // The registered ActivityResultCallback gets the result of this request.
                            requestPermissionLauncher.launch(SEND_SMS);
                        }


                        displayUpdateSuccess(thePet, MainActivity.this);
                        displayMain(thePet, MainActivity.this);

                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        displayUpdateCancelled(thePet, MainActivity.this);

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }






}