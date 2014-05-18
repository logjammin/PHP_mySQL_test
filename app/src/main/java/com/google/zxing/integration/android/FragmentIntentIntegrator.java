package com.google.zxing.integration.android;

import android.app.Fragment;
import android.content.Intent;

/**
 * Created by Logjammin on 5/18/14.
 */
public final class FragmentIntentIntegrator extends IntentIntegrator {

    private final Fragment fragment;

    public FragmentIntentIntegrator(Fragment fragment) {
        super(fragment.getActivity());
        this.fragment = fragment;
    }

    @Override
    protected void startActivityForResult(Intent intent, int code) {
        fragment.startActivityForResult(intent, code);
    }
}
