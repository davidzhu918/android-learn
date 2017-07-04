package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;
import java.util.UUID;

/**
 * Created by zixiangz on 6/4/17.
 */

public class CrimePagerActivity extends AppCompatActivity
    implements CrimeFragment.Callbacks {

    private static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private Button mJumpStart;
    private Button mJumpEnd;
    private int index;

    public static Intent newIntent(Context packageIntent, UUID crimeId) {
        Intent intent = new Intent(packageIntent, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);
        mCrimes = CrimeLab.get(this).getCrimes();
        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        mViewPager = (ViewPager) findViewById(R.id.crime_view_pager);
        mJumpStart = (Button) findViewById(R.id.jump_to_start);
        mJumpStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = 0;
                mViewPager.setCurrentItem(index);
            }
        });

        mJumpEnd = (Button) findViewById(R.id.jump_to_end);
        mJumpEnd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                index = mCrimes.size() - 1;
                mViewPager.setCurrentItem(index);
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                if (position == 0) {
                    mJumpStart.setEnabled(false);
                } else {
                    mJumpStart.setEnabled(true);
                }
                if (position == mCrimes.size() - 1) {
                    mJumpEnd.setEnabled(false);
                } else {
                    mJumpEnd.setEnabled(true);
                }
            }
        });

        //set view pager's current item to the one that's selected
        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                index = i;
                mViewPager.setCurrentItem(index);
                break;
            }
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {}
}
