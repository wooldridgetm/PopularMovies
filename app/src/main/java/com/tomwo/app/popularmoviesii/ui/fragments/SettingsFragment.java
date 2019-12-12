package com.tomwo.app.popularmoviesii.ui.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.utils.PreferenceUtils;

/**
 * Created by wooldridgetm on 6/3/17.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
    private static final String TAG = "SettingsFragment";
    public interface ISettingsFragmentInteraction
    {
        void notifyParentOnPreferenceChange(String orderBy);
    }

    private ISettingsFragmentInteraction mParent;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.addPreferencesFromResource(R.xml.preferences);

        ListPreference listPreference = (ListPreference) findPreference(this.getString(R.string.pref_key_filter));

        if (listPreference == null)
            return;

        this.bindPreferenceSummaryToValue(listPreference, this.getString(R.string.pref_sync_default));
    }

    private void bindPreferenceSummaryToValue(Preference preference, String defaultValue)
    {
        // set the list to watch for values changes
        preference.setOnPreferenceChangeListener(this);

        // trigger listener immediately w/ Preference's current value
        this.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), defaultValue));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        String orderBy = this.getString(R.string.pref_sync_default);
        String filterBy = "";

        if (preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(newValue.toString());

            if (prefIndex >= 0)
            {
                orderBy = listPreference.getEntries()[prefIndex].toString();
                preference.setSummary(orderBy);

                filterBy = listPreference.getEntryValues()[prefIndex].toString();
            }

            PreferenceUtils.updateFilterIndex(this.getActivity(), prefIndex);

            // save this value to an IV in SettingsActivity
            if (this.mParent == null)
            {
                // 1st time this runs, the preference hasn't changed. assign a reference & invoke callback all other times!
                this.mParent = (ISettingsFragmentInteraction) this.getActivity();
                return true;
            }

            this.mParent.notifyParentOnPreferenceChange(filterBy);
        }

        return true;
    }

}
