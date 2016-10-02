package com.keshavg.reddit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.keshavg.reddit.R;
import com.keshavg.reddit.adapters.SubredditAutocompleteAdapter;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.models.CaptchaRepsonse;
import com.keshavg.reddit.models.SubmitPostResponse;
import com.keshavg.reddit.models.UploadImageResponse;
import com.keshavg.reddit.network.ImgurApiClient;
import com.keshavg.reddit.network.ImgurApiInterface;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import es.guiguegon.gallerymodule.GalleryActivity;
import es.guiguegon.gallerymodule.GalleryHelper;
import es.guiguegon.gallerymodule.model.GalleryMedia;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class SubmitPostActivity extends AppCompatActivity {
    private final int REQUEST_CODE_GALLERY = 1;

    private Switch aSwitch;
    private String kind;
    private TextInputEditText title;
    private AutoCompleteTextView subreddit;
    private TextInputEditText text;
    private TextInputEditText url;
    private Button uploadImgButton;
    private CheckBox sendReplies;
    private ProgressBar progressBar;
    private String captchIden;
    private ImageView captcha;
    private TextInputEditText captchaText;
    private ImageButton refreshCaptcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_post);

        kind = "self"; // according to initial switch position

        title = (TextInputEditText) findViewById(R.id.title);
        subreddit = (AutoCompleteTextView) findViewById(R.id.subreddit);
        text = (TextInputEditText) findViewById(R.id.text);
        url = (TextInputEditText) findViewById(R.id.url);
        uploadImgButton = (Button) findViewById(R.id.upload_img);
        sendReplies = (CheckBox) findViewById(R.id.send_replies);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        captcha = (ImageView) findViewById(R.id.captcha);
        captchaText = (TextInputEditText) findViewById(R.id.captcha_text);
        refreshCaptcha = (ImageButton) findViewById(R.id.refresh_captcha);

        SubredditAutocompleteAdapter adapter = new SubredditAutocompleteAdapter(
                getApplicationContext(),
                R.layout.textview);
        subreddit.setAdapter(adapter);

        Button submit = (Button) findViewById(R.id.submit);
        Button discard = (Button) findViewById(R.id.discard);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubmit();
            }
        });
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        aSwitch = (Switch) findViewById(R.id.toggle_post);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    text.setVisibility(GONE);
                    url.setVisibility(View.VISIBLE);
                    uploadImgButton.setVisibility(View.VISIBLE);
                    kind = "link";
                } else {
                    uploadImgButton.setVisibility(GONE);
                    url.setVisibility(GONE);
                    text.setVisibility(View.VISIBLE);
                    kind = "self";
                }
            }
        });

        uploadImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickUploadImage();
            }
        });

        refreshCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCaptcha();
            }
        });

        getCaptcha();
    }

    private void onClickUploadImage() {
//        TODO
//        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(SubmitPostActivity.this)
//                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
//                    @Override
//                    public void onImageSelected(final Uri uri) {
//                        Log.d("Image", uri.getEncodedPath());
//                        File image = new File(uri.getEncodedPath());
//                        uploadImageToImgur(convertFileToBase64(image));
//                    }
//                })
//                .create();
//
//        tedBottomPicker.show(getSupportFragmentManager());

        startActivityForResult(new GalleryHelper()
                .setMultiselection(false)
                .setShowVideos(false)
                .getCallingIntent(this), REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            List<GalleryMedia> galleryMedias =
                    data.getParcelableArrayListExtra(GalleryActivity.RESULT_GALLERY_MEDIA_LIST);
            File image = new File(galleryMedias.get(0).mediaUri());
            uploadImageToImgur(convertFileToBase64(image));
        }
    }

    private void showToast(String message) {
        Toast.makeText(SubmitPostActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read the file " + file.getName());
        }

        is.close();
        return bytes;
    }

    private String convertFileToBase64(File file) {
        byte[] byteArray = null;

        try {
            byteArray = loadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void uploadImageToImgur(String encoded) {
        ImgurApiInterface apiService = ImgurApiClient.getClient().create(ImgurApiInterface.class);
        Call<UploadImageResponse> call = apiService.uploadImage(encoded);
        call.enqueue(new Callback<UploadImageResponse>() {
            @Override
            public void onResponse(Call<UploadImageResponse> call, Response<UploadImageResponse> response) {
                if (response.isSuccessful()) {
                    url.setText(response.body().getLink());
                } else {
                    showToast("Upload Image - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UploadImageResponse> call, Throwable t) {
                showToast(getString(R.string.error_server_connect));
            }
        });
    }

    private void getCaptcha() {
        progressBar.setVisibility(View.VISIBLE);

        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<CaptchaRepsonse> call = apiService.getNewCaptcha(
                "bearer " + AuthSharedPrefHelper.getAccessToken(),
                "json"
        );

        call.enqueue(new Callback<CaptchaRepsonse>() {
            @Override
            public void onResponse(Call<CaptchaRepsonse> call, Response<CaptchaRepsonse> response) {
                if (response.isSuccessful()) {
                    captchIden = response.body().getIden();

                    Glide.with(SubmitPostActivity.this)
                            .load(RedditApiClient.BASE_URL + "captcha/" + captchIden)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e,
                                                           String model,
                                                           Target<GlideDrawable> target,
                                                           boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource,
                                                               String model,
                                                               Target<GlideDrawable> target,
                                                               boolean isFromMemoryCache,
                                                               boolean isFirstResource) {
                                    progressBar.setVisibility(GONE);
                                    return false;
                                }
                            })
                            .into(captcha);
                } else {
                    showToast("Captcha - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CaptchaRepsonse> call, Throwable t) {
                showToast(getString(R.string.error_server_connect));
            }
        });
    }

    private void refreshCaptcha() {
        captcha.setImageDrawable(null);
        captchaText.setText(null);
        getCaptcha();
    }

    private void onClickSubmit() {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<SubmitPostResponse> call = apiService.createPost(
                "bearer " + AuthSharedPrefHelper.getAccessToken(),
                "json",
                kind,
                title.getText().toString(),
                subreddit.getText().toString(),
                captchIden,
                captchaText.getText().toString(),
                text.getText().toString(),
                url.getText().toString(),
                sendReplies.isChecked()
        );

        call.enqueue(new Callback<SubmitPostResponse>() {
            @Override
            public void onResponse(Call<SubmitPostResponse> call, Response<SubmitPostResponse> response) {
                if (response.isSuccessful()) {
                    showToast("Submitted");
                    finish();
                } else {
                    Boolean badCaptchaFlag = false;
                    for (List<String> error : response.body().getErrors()) {
                        String errorString = "";
                        for (String err : error) {
                            if (err.equals(getString(R.string.bad_captcha))) {
                                badCaptchaFlag = true;
                            }

                            errorString += err + "\n";
                        }
                        showToast(errorString);
                    }

                    if (badCaptchaFlag) {
                        refreshCaptcha();
                    }
                }
            }

            @Override
            public void onFailure(Call<SubmitPostResponse> call, Throwable t) {
                showToast(getString(R.string.error_server_connect));
            }
        });
    }
}
