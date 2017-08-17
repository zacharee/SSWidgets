package com.zacharee1.sswidgets.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.misc.SuUtils;
import com.zacharee1.sswidgets.misc.Util;
import com.zacharee1.sswidgets.misc.Values;

import java.util.List;

import javax.xml.validation.Validator;

public class RequestPermissionsActivity extends AppIntro2
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int resId = -1;
        int rand = (int)(Math.random() * 2);

        switch (rand) {
            case 0:
                resId = R.drawable.v20_ss_info;
                break;
            case 1:
                resId = R.drawable.v20_ss_contacts;
                break;
        }

        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.welcome),
                getResources().getString(R.string.app_desc),
                resId,
                getResources().getColor(R.color.colorPrimaryDark, null)));
        addSlide(NotifSlide.newInstance());
        addSlide(PermsSlide.newInstance());

        showSkipButton(false);
        setGoBackLock(true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment)
    {
        String perm = Util.hasAllPerms(this);
        if (perm == null) {
            finish();
        } else {
            Toast.makeText(this, String.format(getResources().getString(R.string.perm_error), perm), Toast.LENGTH_SHORT).show();
            Log.e("MustardCorp Permission", perm);
            backButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment)
    {
        super.onSlideChanged(oldFragment, newFragment);

        List<Fragment> fragments = getSlides();
        int index = fragments.indexOf(newFragment);

        if (index > 0) {
            backButton.setVisibility(View.VISIBLE);
        } else {
            backButton.setVisibility(View.GONE);
        }
    }

    public static class NotifSlide extends Fragment implements View.OnClickListener {
        private View mView;

        public static NotifSlide newInstance() {
            return new NotifSlide();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            mView = inflater.inflate(R.layout.slide_notification_access, container, false);
            mView.findViewById(R.id.open_notification_access).setOnClickListener(this);
            mView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));

            TextView title = mView.findViewById(R.id.title);
            title.setText(getResources().getString(R.string.grant_notification_access_title));

            TextView desc = mView.findViewById(R.id.description);
            desc.setText(getResources().getString(R.string.grant_notification_access_desc));
            return mView;
        }

        @Override
        public void onClick(View view)
        {
            switch (view.getId()) {
                case R.id.open_notification_access:
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    break;
            }
        }
    }

    public static class PermsSlide extends Fragment implements View.OnClickListener {
        private View mView;

        public static PermsSlide newInstance() {
            return new PermsSlide();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            mView = inflater.inflate(R.layout.slide_permissions, container, false);
            mView.findViewById(R.id.request_perms_button).setOnClickListener(this);
            mView.findViewById(R.id.settings_write_button).setOnClickListener(this);
            mView.findViewById(R.id.battery_stats_button).setOnClickListener(this);
            mView.findViewById(R.id.adb_instructions).setOnClickListener(this);
            mView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));

            TextView title = mView.findViewById(R.id.title);
            title.setText(getResources().getString(R.string.grant_permissions_title));

            TextView desc = mView.findViewById(R.id.description);
            desc.setText(getResources().getString(R.string.grant_permissions_desc));
            return mView;
        }

        @Override
        public void onClick(View view)
        {
            switch (view.getId()) {
                case R.id.request_perms_button:
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS},
                            10001);
                    break;
                case R.id.settings_write_button:
                    if (SuUtils.testSudo()) {
                        SuUtils.sudo("pm grant " + getContext().getPackageName() + " " + Manifest.permission.WRITE_SECURE_SETTINGS);
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_root), Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.battery_stats_button:
                    if (SuUtils.testSudo()) {
                        SuUtils.sudo("pm grant " + getContext().getPackageName() + " " + Manifest.permission.BATTERY_STATS);
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_root), Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.adb_instructions:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Values.ADB_URL)));
                    break;
            }
        }
    }
}