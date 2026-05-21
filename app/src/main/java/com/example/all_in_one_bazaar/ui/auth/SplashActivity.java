package com.example.all_in_one_bazaar.ui.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2800;
    private static final String PREFS_NAME     = "AllInOneBazaarPrefs";
    private static final String KEY_ONBOARDING = "onboarding_done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen immersive — hides status bar so gradient fills edge-to-edge
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        LinearLayout centerGroup = findViewById(R.id.centerGroup);
        LinearLayout bottomGroup = findViewById(R.id.bottomGroup);
        CardView      logoCard   = findViewById(R.id.logoCard);

        // ── 1. Logo card: bounces in from above ────────────────────────
        logoCard.setTranslationY(-100f);
        logoCard.setAlpha(0f);

        ObjectAnimator logoSlide = ObjectAnimator.ofFloat(logoCard, "translationY", -100f, 0f);
        logoSlide.setDuration(550);
        logoSlide.setInterpolator(new OvershootInterpolator(1.4f));

        ObjectAnimator logoFade = ObjectAnimator.ofFloat(logoCard, "alpha", 0f, 1f);
        logoFade.setDuration(350);

        // ── 2. Center text group: fades and slides up ──────────────────
        centerGroup.setTranslationY(36f);
        centerGroup.setAlpha(0f);

        ObjectAnimator textFade = ObjectAnimator.ofFloat(centerGroup, "alpha", 0f, 1f);
        textFade.setDuration(550);
        textFade.setStartDelay(280);
        textFade.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator textSlide = ObjectAnimator.ofFloat(centerGroup, "translationY", 36f, 0f);
        textSlide.setDuration(550);
        textSlide.setStartDelay(280);
        textSlide.setInterpolator(new DecelerateInterpolator());

        // ── 3. Bottom loader: fades in last ────────────────────────────
        ObjectAnimator loaderFade = ObjectAnimator.ofFloat(bottomGroup, "alpha", 0f, 1f);
        loaderFade.setDuration(400);
        loaderFade.setStartDelay(750);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(logoSlide, logoFade, textFade, textSlide, loaderFade);
        animSet.start();

        // ── Navigate after splash duration ─────────────────────────────
        new Handler().postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;

            if (user != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean onboardingDone = prefs.getBoolean(KEY_ONBOARDING, false);

                if (!onboardingDone) {
                    // 🛠️ UX FIX: અહીંથી બુલિયન સેવ કરવાનું લોજિક હટાવ્યું છે, તે ઓનબોર્ડિંગ સ્ક્રીનના "Get Started" બટન પર રાખવું વધુ સારું રહેશે.
                    intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
            }

            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }, SPLASH_DURATION);
    }
}