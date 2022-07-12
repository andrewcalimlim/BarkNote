package com.example.barknote;

import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
//import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {

    protected void textContact(String contact){ //temp method for now
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contact, null, "sms message", null, null);
    }

    protected Pet loadPet(){
        // to be replaced with loading data from file
        Pet testPet = new Pet("Quilo", "2062440287", false);
        return testPet;
    }

    protected void displayName(){
        Pet thePet = loadPet();

        String theName = thePet.getName();
        TextView tv1 = (TextView) findViewById(R.id.name);
        tv1.setText(theName);
    }

    protected void displayStatus(){
        Pet thePet = loadPet();

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

    protected void displayButton(){
        Pet thePet = loadPet();

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

    protected void displayAll(){
        displayName();
        displayStatus();
        displayButton();
        //displayCareDate();
    }

    //displays LastCaredFor date, but for user this isn't really necessary
    /***
    protected void displayCareDate(){
        Pet thePet = loadPet();

        String theCareDate = thePet.whenLastCared().toString(); // just need the string
        TextView tv1 = (TextView) findViewById(R.id.careDate);
        tv1.setText(theCareDate);

    }
    ***/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayAll();

    }

}