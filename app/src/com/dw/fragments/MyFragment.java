package com.dw.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by duwei on 18-11-18.
 */

public class MyFragment extends Fragment {
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("xx","MyFragment   --------------    onAttach   -----");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("xx","MyFragment   --------------    onCreate   -----");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d("xx","MyFragment   --------------    onCreateView   -----");
        Button tx = new Button(getActivity());
        tx.setText("hello fragment");
        return tx;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("xx","MyFragment   --------------    onViewCreated   -----");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("xx","MyFragment   --------------    onActivityCreated   -----");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("xx","MyFragment   --------------    onStart   -----");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("xx","MyFragment   --------------    onResume   -----");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("xx","MyFragment   --------------    onPause   -----");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("xx","MyFragment   --------------    onStop   -----");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("xx","MyFragment   --------------    onDestroyView   -----");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("xx","MyFragment   --------------    onDestroy   -----");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("xx","MyFragment   --------------    onDetach   -----");
    }
}
