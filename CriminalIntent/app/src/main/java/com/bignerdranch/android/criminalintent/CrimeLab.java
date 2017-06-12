package com.bignerdranch.android.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Map<UUID, Crime> mCrimes;
    private List<Crime> mCrimesList;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mCrimes = new HashMap<>();
        mCrimesList = new ArrayList<>();
    }

    public void addCrime(Crime c) {
        mCrimesList.add(c);
        mCrimes.put(c.getId(), c);
    }

    public void deleteCrime(Crime c) {
        mCrimesList.remove(c);
        mCrimes.remove(c.getId());
    }

    public List<Crime> getCrimes() {
        return mCrimesList;
    }

    public Crime getCrime(UUID id) {
        return mCrimes.get(id);
    }
}
