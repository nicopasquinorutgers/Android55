package com.example.android55;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DataManager {
    private static final String TAG      = "DataManager";
    private static final String FILENAME = "user.dat";

    public static void saveUser(Context context, User user) throws IOException {
        try (var fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
             var out = new ObjectOutputStream(fos)) {
            out.writeObject(user);
            Log.d(TAG, "Save successful: " + user.getAlbums().size() + " albums");
        }
    }

    public static User loadUser(Context context) {
        try (var fis = context.openFileInput(FILENAME);
             var in  = new ObjectInputStream(fis)) {

            User user = (User) in.readObject();
            Log.d(TAG, "Load successful: " + user.getAlbums().size() + " albums");
            return user;

        } catch (FileNotFoundException e) {
            Log.d(TAG, "No user.dat found; starting fresh");
            return new User();

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Corrupt data: class not found during load", e);
            throw new RuntimeException("Failed to load user data", e);

        } catch (IOException e) {
            Log.e(TAG, "I/O error during load", e);
            throw new RuntimeException("Failed to load user data", e);
        }
    }
}