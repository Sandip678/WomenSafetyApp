package com.womensefty.womensafetyapp;



import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ImageView plusIcon;
    private ImageView addContactIcon;
    private EditText messageBox;
    private Button generateSOSButton;
    private Button logoutButton;
    private Button deleteButton;
    private FrameLayout sosButtonContainer;
    private RecyclerView contactRecyclerView;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private ContactDatabaseHelper dbHelper;

    // Views for the main contact box
    private ImageView mainContactImage;
    private TextView mainContactName;
    private TextView mainContactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        plusIcon = findViewById(R.id.plusIcon);
        addContactIcon = findViewById(R.id.addContactIcon);
        messageBox = findViewById(R.id.messageBox);
        generateSOSButton = findViewById(R.id.generateSOSButton);
        logoutButton = findViewById(R.id.logoutButton);
        deleteButton = findViewById(R.id.deleteButton);
        sosButtonContainer = findViewById(R.id.sosButtonContainer);
        contactRecyclerView = findViewById(R.id.contactRecyclerView);

        // Initialize views for the main contact box
        mainContactImage = findViewById(R.id.mainContactImage);
        mainContactName = findViewById(R.id.mainContactName);
        mainContactNumber = findViewById(R.id.mainContactNumber);

        // Set up RecyclerView
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList);
        contactRecyclerView.setAdapter(contactAdapter);

        // Initialize database
        dbHelper = new ContactDatabaseHelper(this);

        // Load contacts from database
        loadContacts();

        // Request runtime permissions
        requestPermissions();

        // Set click listeners
        plusIcon.setOnClickListener(v -> openContactPickerForList());
        addContactIcon.setOnClickListener(v -> openContactPickerForMainBox());
        generateSOSButton.setOnClickListener(v -> createSOSButton());
        logoutButton.setOnClickListener(v -> logoutUser());
        deleteButton.setOnClickListener(v -> deleteAllContacts());

    }



    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 102);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 103);
            }
        }
    }

    private void openContactPickerForList() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1); // Request code 1 for contact list
    }

    private void openContactPickerForMainBox() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 2); // Request code 2 for main contact box
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};

            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactNumber = getContactNumber(contactId);

                if (requestCode == 1) {
                    // Add contact to the list
                    contactList.add(new Contact(contactName, contactNumber, null));
                    contactAdapter.notifyDataSetChanged();
                } else if (requestCode == 2) {
                    // Update the main contact box
                    mainContactName.setText(contactName);
                    mainContactNumber.setText(contactNumber);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getContactNumber(String contactId) {
        String number = "";
        Cursor phoneCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );

        if (phoneCursor != null && phoneCursor.moveToFirst()) {
            number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneCursor.close();
        }
        return number;
    }

    private void saveContact(String name, String number, byte[] imageBytes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactDatabaseHelper.COLUMN_NAME, name);
        values.put(ContactDatabaseHelper.COLUMN_NUMBER, number);
        values.put(ContactDatabaseHelper.COLUMN_IMAGE, imageBytes);
        db.insert(ContactDatabaseHelper.TABLE_NAME, null, values);
    }

    private void loadContacts() {
        contactList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ContactDatabaseHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactDatabaseHelper.COLUMN_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactDatabaseHelper.COLUMN_NUMBER));
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(ContactDatabaseHelper.COLUMN_IMAGE));
                contactList.add(new Contact(name, number, imageBytes));
            } while (cursor.moveToNext());
        }
        cursor.close();
        contactAdapter.notifyDataSetChanged();
    }

    private void deleteAllContacts() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ContactDatabaseHelper.TABLE_NAME, null, null);
        loadContacts();
        Toast.makeText(this, "All contacts deleted", Toast.LENGTH_SHORT).show();
    }


    private void createSOSButton() {
        // Clear previous views
        sosButtonContainer.removeAllViews();

        // Create SOS Button
        Button sosButton = new Button(this);
        sosButton.setText("SOS");
        sosButton.setBackgroundResource(R.drawable.circular_red_bg);
        sosButton.setTextSize(20); // Text size in SP
        sosButton.setTextColor(Color.WHITE);

        // Set layout parameters for SOS Button
        FrameLayout.LayoutParams sosButtonParams = new FrameLayout.LayoutParams(
                dpToPx(150), // Width in pixels (converted from 100dp)
                dpToPx(150)  // Height in pixels (converted from 100dp)
        );
        sosButtonParams.gravity = Gravity.CENTER; // Center the button
        sosButton.setLayoutParams(sosButtonParams);


        // Add views to the container
        sosButtonContainer.addView(sosButton);
        //sosButtonContainer.addView(moveToScreenText);

        // Set click listeners
        sosButton.setOnClickListener(v -> sendSOSMessage());

    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    private void sendSOSMessage() {
        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
            Toast.makeText(this, "SMS permission is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 102);
            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the message from the message box
        String message = messageBox.getText().toString();

        // Get last known location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is still not granted, return
            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String locationMessage = "My location: " + (location != null ? location.getLatitude() + ", " + location.getLongitude() : "Location not available");

        // Send SOS message to all contacts
        SmsManager smsManager = SmsManager.getDefault();
        boolean allMessagesSent = true;

        for (Contact contact : contactList) {
            String phoneNumber = contact.getNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                try {
                    smsManager.sendTextMessage(phoneNumber, null, message + "\n" + locationMessage, null, null);
                } catch (Exception e) {
                    // If SMS sending fails, log the error
                    Log.e("SOSMessage", "Failed to send SMS to " + phoneNumber, e);
                    allMessagesSent = false;
                }
            } else {
                // If phone number is invalid, log the error
                Log.e("SOSMessage", "Invalid phone number for contact: " + contact.getName());
                allMessagesSent = false;
            }
        }

        // Show appropriate message to the user
        if (allMessagesSent) {
            Toast.makeText(this, "SOS message sent to all contacts", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to send SOS message to some contacts", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) { // SMS permission request code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // SMS permission granted, retry sending SOS message
                sendSOSMessage();
            } else {
                Toast.makeText(this, "SMS permission is required to send SOS messages!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 102) { // Location permission request code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, retry sending SOS message
                sendSOSMessage();
            } else {
                Toast.makeText(this, "Location permission is required to send SOS messages!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Contact model class
    public static class Contact {
        private String name;
        private String number;
        private byte[] image;

        public Contact(String name, String number, byte[] image) {
            this.name = name;
            this.number = number;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public byte[] getImage() {
            return image;
        }
    }
}