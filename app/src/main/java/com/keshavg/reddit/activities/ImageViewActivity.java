package com.keshavg.reddit.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.keshavg.reddit.R;
import com.klinker.android.sliding.SlidingActivity;

public class ImageViewActivity extends SlidingActivity {
    private String image;

    @Override
    public void init(Bundle savedInstanceState) {
        setContent(R.layout.activity_image_view);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            image = extras.getString("Image");
        }

        enableFullscreen();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar_image);

        final ImageView gifView = (ImageView) findViewById(R.id.gif_view);
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.image_view);

        if (image.contains(".gif")) {
            gifView.setVisibility(View.VISIBLE);
            GlideDrawableImageViewTarget glideDrawableImageViewTarget = new GlideDrawableImageViewTarget(gifView);
            Glide.with(ImageViewActivity.this)
                    .load(image)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(glideDrawableImageViewTarget);
        } else {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(ImageViewActivity.this)
                    .load(image)
                    .asBitmap()
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            imageView.setImage(ImageSource.bitmap(resource));
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}