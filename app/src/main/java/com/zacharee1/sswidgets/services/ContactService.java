package com.zacharee1.sswidgets.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;

import com.zacharee1.sswidgets.activities.AddContactActivity;
import com.zacharee1.sswidgets.misc.Values;

import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_1;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_2;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_3;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_4;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_5;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_6;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_7;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_8;
import static com.zacharee1.sswidgets.widgets.Contacts.CONTACT_ID;

public class ContactService extends IntentService
{
    public ContactService()
    {
        super("ContactService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();
            if (action.equals(Values.CONTACT_INTENT_ACTION)) {
                String contactId = null;
                String whichContact = null;

                switch (intent.getIntExtra(Values.CONTACT_INTENT_ACTION, -2)) {
                    case -2:
                        return;
                    case Values.CONTACT_1:
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_1);
                        whichContact = CONTACT_1;
                        break;
                    case Values.CONTACT_2:
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_2);
                        whichContact = CONTACT_2;
                        break;
                    case Values.CONTACT_3:
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_3);
                        whichContact = CONTACT_3;
                        break;
                    case Values.CONTACT_4:
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_4);
                        whichContact = CONTACT_4;
                        break;
                    case Values.CONTACT_5:
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_5);
                        whichContact = CONTACT_5;
                        break;
                    case Values.CONTACT_6:
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_6);
                        whichContact = CONTACT_6;
                        break;
                    case Values.CONTACT_7:
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_7);
                        whichContact = CONTACT_7;
                        break;
                    case Values.CONTACT_8:
                        whichContact = CONTACT_8;
                        contactId = Settings.Global.getString(getContentResolver(), CONTACT_8);
                        break;
                }

                if (contactId != null && !contactId.isEmpty()) {
                    Intent contactIntent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
                    contactIntent.setData(uri);
                    startActivity(contactIntent);
                } else {
                    Intent contactIntent = new Intent(this, AddContactActivity.class);
                    contactIntent.putExtra(CONTACT_ID, whichContact);
                    startActivity(contactIntent);
                }
            }
        }
    }
}
