package com.zacharee1.sswidgets.misc;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

/**
 * Created by Zacha on 8/13/2017.
 */

public class Util
{
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Open app by specified package name/ID
     * @param context caller's context
     * @param packageName desired app's package name
     * @return success
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get contact image by ID
     * @param context caller's context
     * @param contactId ID of contact
     * @return Bitmap containing contact's avatar
     */
    public static Bitmap openDisplayPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);

        Cursor name = context.getContentResolver().query(contactUri,
                new String[] {ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {
            String string = "?";

            if (name.moveToFirst()) {
                byte[] data = name.getBlob(0);
                if (data != null) {
                    ByteArrayInputStream in = new ByteArrayInputStream(data);

                    int n = in.available();
                    byte[] bytes = new byte[n];
                    in.read(bytes, 0, n);
                    string = new String(bytes, StandardCharsets.UTF_8); // Or any encoding.
                }
            }

            return returnDefaultContact(context, string, contactId);
        }

        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }

        String string = "?";

        if (name.moveToFirst()) {
            byte[] data = name.getBlob(0);
            if (data != null) {
                ByteArrayInputStream in = new ByteArrayInputStream(data);

                int n = in.available();
                byte[] bytes = new byte[n];
                in.read(bytes, 0, n);
                string = new String(bytes, StandardCharsets.UTF_8); // Or any encoding.
            }
        }

        return returnDefaultContact(context, string, contactId);
    }

    /**
     * Create or load a generic colored image based on the contact's name
     * @param context caller's context
     * @param name contact's name
     * @param id contact's ID
     * @return Bitmap containing generic contact avatar
     */
    private static Bitmap returnDefaultContact(Context context, String name, long id) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(80);

        int color = PreferenceManager.getDefaultSharedPreferences(context).getInt("contact_by_id_" + id, 0);

        if (color == 0) {
            int colorValue1 = (int)((56 + Math.random() * 200));
            int colorValue2 = (int)((56 + Math.random() * 200));
            int colorValue3 = (int)((56 + Math.random() * 200));

            color = Color.rgb(colorValue1, colorValue2, colorValue3);

            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("contact_by_id_" + id, color).apply();
        }

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(color);

        Bitmap bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

        canvas.drawText(name.substring(0, 1), xPos, yPos, textPaint);

        return bitmap;
    }

    /**
     * Retrieve and compile a map of contacts, sorted by name
     * @param context caller's context
     * @return sorted map of contacts
     */
    public static TreeMap<String, Contact> compileContactsList(Context context) {
        TreeMap<String, Contact> contacts = new TreeMap<>();

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, "display_name");

        while (phones.moveToNext())
        {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

            Contact contact = new Contact();
            contact.name = name;
            contact.number = phoneNumber;
            contact.id = Long.decode(id);

            contact.avatar = openDisplayPhoto(context, contact.id);

            contacts.put(contact.name, contact);
        }
        phones.close();

        return contacts;
    }

}
