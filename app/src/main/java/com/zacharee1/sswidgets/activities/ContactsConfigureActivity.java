package com.zacharee1.sswidgets.activities;

import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.zacharee1.sswidgets.R;

import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_1;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_2;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_3;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_4;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_5;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_6;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_7;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_8;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_ID;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_KEY;
import static com.zacharee1.sswidgets.widgets.Contacts.DEF_KEY;

import static com.zacharee1.sswidgets.misc.Util.openDisplayPhoto;

public class ContactsConfigureActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener
{

    private ContentObserver stateObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_configure);

        addIcons();
        listenForContactChange();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        try {
            getContentResolver().unregisterContentObserver(stateObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForContactChange() {
        stateObserver = new ContentObserver(null)
        {
            @Override
            public void onChange(boolean selfChange, final Uri uri)
            {
                Uri contact1 = Settings.Global.getUriFor(CONTACT_1);
                Uri contact2 = Settings.Global.getUriFor(CONTACT_2);
                Uri contact3 = Settings.Global.getUriFor(CONTACT_3);
                Uri contact4 = Settings.Global.getUriFor(CONTACT_4);
                Uri contact5 = Settings.Global.getUriFor(CONTACT_5);
                Uri contact6 = Settings.Global.getUriFor(CONTACT_6);
                Uri contact7 = Settings.Global.getUriFor(CONTACT_7);
                Uri contact8 = Settings.Global.getUriFor(CONTACT_8);

                if (uri.equals(contact1) ||
                        uri.equals(contact2) ||
                        uri.equals(contact3) ||
                        uri.equals(contact4) ||
                        uri.equals(contact5) ||
                        uri.equals(contact6) ||
                        uri.equals(contact7) ||
                        uri.equals(contact8)) {
                    addIcons();
                }
            }
        };

        getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, stateObserver);
    }

    private void addIcons() {
        String a1 = Settings.Global.getString(getContentResolver(), CONTACT_1);
        String a2 = Settings.Global.getString(getContentResolver(), CONTACT_2);
        String a3 = Settings.Global.getString(getContentResolver(), CONTACT_3);
        String a4 = Settings.Global.getString(getContentResolver(), CONTACT_4);
        String a5 = Settings.Global.getString(getContentResolver(), CONTACT_5);
        String a6 = Settings.Global.getString(getContentResolver(), CONTACT_6);
        String a7 = Settings.Global.getString(getContentResolver(), CONTACT_7);
        String a8 = Settings.Global.getString(getContentResolver(), CONTACT_8);

        ImageView contact1 = findViewById(R.id.contact_1);
        contact1.setTag(DEF_KEY, CONTACT_1);
        ImageView contact2 = findViewById(R.id.contact_2);
        contact2.setTag(DEF_KEY, CONTACT_2);
        ImageView contact3 = findViewById(R.id.contact_3);
        contact3.setTag(DEF_KEY, CONTACT_3);
        ImageView contact4 = findViewById(R.id.contact_4);
        contact4.setTag(DEF_KEY, CONTACT_4);
        ImageView contact5 = findViewById(R.id.contact_5);
        contact5.setTag(DEF_KEY, CONTACT_5);
        ImageView contact6 = findViewById(R.id.contact_6);
        contact6.setTag(DEF_KEY, CONTACT_6);
        ImageView contact7 = findViewById(R.id.contact_7);
        contact7.setTag(DEF_KEY, CONTACT_7);
        ImageView contact8 = findViewById(R.id.contact_8);
        contact8.setTag(DEF_KEY, CONTACT_8);

        if (a1 != null && !a1.isEmpty()) {
            try {
                contact1.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a1))));
                contact1.setTag(CONTACT_KEY, a1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact1.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact1.setTag(CONTACT_KEY, null);
        }

        if (a2 != null && !a2.isEmpty()) {
            try {
                contact2.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a2))));
                contact2.setTag(CONTACT_KEY, a2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact2.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact2.setTag(CONTACT_KEY, null);
        }

        if (a3 != null && !a3.isEmpty()) {
            try {
                contact3.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a3))));
                contact3.setTag(CONTACT_KEY, a3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact3.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact3.setTag(CONTACT_KEY, null);
        }

        if (a4 != null && !a4.isEmpty()) {
            try {
                contact4.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a4))));
                contact4.setTag(CONTACT_KEY, a4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact4.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact4.setTag(CONTACT_KEY, null);
        }

        if (a5 != null && !a5.isEmpty()) {
            try {
                contact5.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a5))));
                contact5.setTag(CONTACT_KEY, a5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact5.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact5.setTag(CONTACT_KEY, null);
        }

        if (a6 != null && !a6.isEmpty()) {
            try {
                contact6.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a6))));
                contact6.setTag(CONTACT_KEY, a6);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact6.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact6.setTag(CONTACT_KEY, null);
        }

        if (a7 != null && !a7.isEmpty()) {
            try {
                contact7.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a7))));
                contact7.setTag(CONTACT_KEY, a7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact7.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact7.setTag(CONTACT_KEY, null);
        }

        if (a8 != null && !a8.isEmpty()) {
            try {
                contact8.setImageDrawable(new BitmapDrawable(getResources(), openDisplayPhoto(this, Long.decode(a8))));
                contact8.setTag(CONTACT_KEY, a8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contact8.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_24dp, null));
            contact8.setTag(CONTACT_KEY, null);
        }

        contact1.setOnClickListener(this);
        contact2.setOnClickListener(this);
        contact3.setOnClickListener(this);
        contact4.setOnClickListener(this);
        contact5.setOnClickListener(this);
        contact6.setOnClickListener(this);
        contact7.setOnClickListener(this);
        contact8.setOnClickListener(this);

        contact1.setOnLongClickListener(this);
        contact2.setOnLongClickListener(this);
        contact3.setOnLongClickListener(this);
        contact4.setOnLongClickListener(this);
        contact5.setOnLongClickListener(this);
        contact6.setOnLongClickListener(this);
        contact7.setOnLongClickListener(this);
        contact8.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getTag(DEF_KEY) != null) {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra(CONTACT_ID, view.getTag(DEF_KEY).toString());
            startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View view)
    {
        if (view.getTag(DEF_KEY) != null) {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra(CONTACT_ID, view.getTag(DEF_KEY).toString());
            startActivity(intent);
            return true;
        } else return false;
    }
}
