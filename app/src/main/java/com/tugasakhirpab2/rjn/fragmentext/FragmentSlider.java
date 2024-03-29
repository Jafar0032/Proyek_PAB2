package com.tugasakhirpab2.rjn.fragmentext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.tugasakhirpab2.rjn.R;

public class FragmentSlider extends Fragment {

    private static final String ARG_PARAM1 = "imgSlider";

    private int imageResources;

    public FragmentSlider() {
    }

    public static FragmentSlider newInstance(int params) {
        FragmentSlider fragment = new FragmentSlider();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, params);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        imageResources = getArguments().getInt(ARG_PARAM1);
        View view = inflater.inflate(R.layout.fragment_banner_slider, container, false);
        ImageView img = view.findViewById(R.id.img);
        Glide.with(getActivity())
                .load(imageResources)
                .placeholder(R.drawable.img_placeholder)
                .into(img);
        return view;
    }
}
