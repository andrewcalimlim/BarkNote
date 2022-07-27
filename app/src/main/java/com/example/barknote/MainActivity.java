package com.example.barknote;

import static android.Manifest.permission.SEND_SMS;

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

        b1.setOnClickListener(new View.OnClickListener() {

            // this is an alert that tells the user to make sure pet has been fed and used the
            // bathroom
            @Override
            public void onClick(View view) {
                // confirmation dialog
                displayUpdateConfirmation(view.getContext());

            }
        });


    }

    protected void displayMain(Context c) {
        displayName(c);
        displayStatus(c);
        displayButton(c);
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

    protected void displaySetup(ActivityResultLauncher<String> rpl, Context c){

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
                displayMain(c);
                displayRequest(rpl, c);

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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

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
            public void afterTextChanged(Editable editable) {}

        });

        petNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

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
            public void afterTextChanged(Editable editable) {}

        });

        dialog.show();

    }

    /***
     * PET UPDATE FUNCTIONALITY
     */

    protected void displayUpdateCancelled(Context c){
        Pet thePet = loadPet(c);
        String cancelMessage = "";

        if(smsAllowed(c)){
            cancelMessage += "No text message was sent. ";
        }

        cancelMessage += " Please take care of " + thePet.getName() +
                " before trying to update again.";

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Pet Update Cancelled");
        builder.setMessage(cancelMessage);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    protected void displayUpdateSuccess(Context c){
        Pet thePet = loadPet(c);
        String successMessage = "";

        if(smsAllowed(c)){
            successMessage += "BarkNote text message sent to " + thePet.getContact() + ". ";
        }

        successMessage += thePet.getName() + " thanks you!";

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

    protected boolean smsAllowed(Context c){
        int verdict = ContextCompat.checkSelfPermission(c,
                SEND_SMS);
        if(verdict == -1){
            return false;
        }
        return true;
    }

    protected void setupCheck(ActivityResultLauncher<String> rpl, Context c){
        if(setupNeeded(c)){
            displaySetup(rpl, c);
        }
    }

    protected void displayRequest(ActivityResultLauncher<String> rpl, Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage("Please accept the following" +
                " SMS permission sending request so that BarkNote can automatically send texts" +
                " to your requested contact upon update.");
        builder.setTitle("SMS Text Permission Request Incoming");
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                rpl.launch(SEND_SMS);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    protected void displayRequestDeniedResponse(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("SMS Text Permission Request Denied");
        builder.setMessage("BarkNote will not SMS text your contact on update, however please"
                +" note that BarkNote now functions as a simple reminder app.\nIf you change" +
                " your mind, please enable SMS permissions for BarkNote in your phone's settings.");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                displaySetupCompletion(c);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    protected void displayRequestAcceptedResponse(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("SMS Text Permission Request Accepted");
        builder.setMessage("Thank you for enabling SMS permissions! BarkNote is now fully " +
                "functional.\nBarkNote will never ask for any other permissions.");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                displaySetupCompletion(c);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    protected void displaySetupCompletion(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Setup Complete!");
        builder.setMessage("BarkNote is now ready to be used. Thanks for downloading!");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void displayUpdateConfirmation(Context c){
        //getting the proper pet name to display in the alert
        Pet thePet = loadPet(c);

        String careAlertMessage = "Has " + thePet.getName() + " been taken outside to use" +
                " the bathroom and been fed?";

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(careAlertMessage);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                thePet.updateCare();
                savePet(thePet, MainActivity.this);

                attemptBarkNote(MainActivity.this);
                displayMain(MainActivity.this);
                displayUpdateSuccess(MainActivity.this);

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                displayMain(MainActivity.this);
                displayUpdateCancelled(MainActivity.this);

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    // checks sms text permission and if approval given, texts contact
    protected void attemptBarkNote(Context c){

        Pet thePet = loadPet(c);

        // sending SMS texts if allowed
        if(smsAllowed(c)){
            // getting the current time
            LocalTime rn = LocalTime.now();
            String pattern = "h:mm a";
            String theTime = rn.format(DateTimeFormatter.ofPattern(pattern));

            //creating a text
            String theTextMessage = "Hello! This is an automated BarkNote text " +
                    "confirming that " + thePet.getName() + " has been taken care" +
                    " of (walked, used the bathroom, and fed) at " + theTime;

            // sending the text
            textContact(thePet.getContact(), theTextMessage);

        }
    }

    /***
     *  MAIN ACTIVITY
     */

    // move all of the funcitonality below main into main lol

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Registering the permissions callback (aka how to react to a permissions decision)
        // this is ugly but the logic is copy and pasted from the Android Studio documentation
        // so yeah
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                displayRequestAcceptedResponse(MainActivity.this);
                            } else {
                                displayRequestDeniedResponse(MainActivity.this);
                            }
                        });

        setupCheck(requestPermissionLauncher, this);

        Pet thePet = loadPet(this);
        thePet.checkCare();
        savePet(thePet, this);

        //launching the main activity
        setContentView(R.layout.activity_main);

        // loading data from file and displaying on-screen
        displayMain(this);

    }


    @Override
    protected void onResume(){
        super.onResume();

        Pet thePet = loadPet(this);
        thePet.checkCare();
        savePet(thePet, this);
        displayMain(this);



    }


}