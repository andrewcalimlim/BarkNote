package com.example.barknote;
import java.time.LocalDate;
import android.util.Log;


public class Pet {
    /**
     * Been a while since I programmed in Java. More like 2 years. Hoo, bear with me.
     *
     * I know this looks boilerplate, but that's because  I had to dig out my study guides
     * for my Java programming classes in college just as a review. It feels validating to use
     * them again after all these years.
     *
     * I'm like an old man chuckling to himself and saying "I've still got it."
     */

    /* Instance Variables */

    private String name;
    private String contact;
    private boolean caredForToday;
    private String lastCared;

    //private String photoFP;

    /* Constructor */

    public Pet(String name, String contact, boolean caredForToday, String lastCared){
        this.name = name;
        this.contact = contact;
        this.caredForToday = caredForToday;
        this.lastCared = lastCared;
    }

    /* Accessor Methods */

    public String getName(){
        return name;
    }

    public String getContact(){
        return contact;
    }

    public boolean getCaredForToday(){
        return caredForToday;
    }

    public String getLastCared(){
        return lastCared;
    }

    /* Override toString */

    public String toString(){
        String x = "\nName: " + name + "\nContact: " + contact + "\nCared for today?: " + caredForToday
                + "\nLast Cared: " + lastCared + "\n";
        return x;
    }

    /* Mutator Methods */

    public void setLastCared(String newLastCared){
        lastCared = newLastCared;

    }
    public void rename(String newName){ //rename your pet
        name = newName;

    }

    public void changeContact(String newContact){ //change pet's contact
        contact = newContact;
    }

    public void checkCare(){ // check if you took care of your pet today

        String today = LocalDate.now().toString();
        //Log.i("TODAY STRING", today);
        //Log.i("LAST CARED", lastCared);

        if(today.equals(lastCared)){
            caredForToday = true;
            //Log.i("CARED FOR TODAY", "true");
        }
        else{
            caredForToday = false;
            //Log.i("CARED FOR TODAY", "false");
        }


    }

    public void updateCare(){ // update pet object after pet was taken care of today
        caredForToday = true;
        lastCared = LocalDate.now().toString();
    }

}
