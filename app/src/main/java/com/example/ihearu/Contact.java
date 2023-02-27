package com.example.ihearu;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity
public class Contact {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "contact_id")
    public String contactId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "number")
    public String number;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "do_email")
    public boolean doEmail;

    @ColumnInfo(name = "do_text")
    public boolean doText;

    @ColumnInfo(name = "do_call")
    public boolean doCall;

    public Contact(String name, String number, String email, boolean doEmail, boolean doText, boolean doCall){
        this.name = name;
        this.number = number;
        this.email = email;
        this.doCall = doCall;
        this.doEmail = doEmail;
        this.doText = doText;
        contactId = UUID.randomUUID().toString();
    }



}
