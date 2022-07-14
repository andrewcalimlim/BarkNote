package com.example.barknote;
import java.time.LocalDate;


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
    private LocalDate lastCared;

    //private String photoFP;

    /* Constructor */

    public Pet(String name, String contact, boolean caredForToday){
        this.name = name;
        this.contact = contact;
        this.caredForToday = caredForToday;
        this.lastCared = LocalDate.now();
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

    public LocalDate getLastCared(){
        return lastCared;
    }

    /* Override toString */

    public String toString(){
        String x = "Name: " + name + "Contact: " + contact + "\nCared for today? : " + caredForToday
                + "\nLast Cared: " + lastCared + "\n";
        return x;
    }

    /* Mutator Methods */

    public void rename(String newName){ //rename your pet
        name = newName;

    }

    public void changeContact(String newContact){ //change pet's contact
        contact = newContact;
    }

    public void checkCare(){ // check if you took care of your pet today
        LocalDate today = LocalDate.now();
        if(today.equals(lastCared)){
            caredForToday = true;
        }
        else{
            caredForToday = false;
        }

    }

    public void updateCare(){ // update pet object after pet was taken care of today
        caredForToday = true;
        lastCared = LocalDate.now();
    }

}
