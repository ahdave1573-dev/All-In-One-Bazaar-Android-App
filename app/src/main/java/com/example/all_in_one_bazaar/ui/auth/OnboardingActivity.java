package com.example.all_in_one_bazaar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.auth.adapter.OnboardingAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private ImageView btnNext;
    private TextView btnSkip;

    private OnboardingAdapter adapter;
    private List<OnboardingAdapter.OnboardingItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager  = findViewById(R.id.viewPagerOnboarding);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnNext    = findViewById(R.id.btnNext);
        btnSkip    = findViewById(R.id.btnSkip);

        // ── Onboarding pages ──────────────────────────────────────────
        items = new ArrayList<>();
        items.add(new OnboardingAdapter.OnboardingItem(
                getString(R.string.onboard_title_1),
                getString(R.string.onboard_desc_1),
                R.drawable.ic_onboard_quality   // shopping bag SVG
        ));
        items.add(new OnboardingAdapter.OnboardingItem(
                getString(R.string.onboard_title_2),
                getString(R.string.onboard_desc_2),
                R.drawable.ic_onboard_delivery  // delivery truck SVG
        ));
        items.add(new OnboardingAdapter.OnboardingItem(
                getString(R.string.onboard_title_3),
                getString(R.string.onboard_desc_3),
                R.drawable.ic_onboard_secure    // shield SVG
        ));

        adapter = new OnboardingAdapter(this, items);
        viewPager.setAdapter(adapter);

        // ── Dots ──────────────────────────────────────────────────────
        buildDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                buildDots(position);
                updateNextButton(position);
            }
        });

        // ── Next button ───────────────────────────────────────────────
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < items.size() - 1) {
                viewPager.setCurrentItem(current + 1, true);
            } else {
                goToLogin();
            }
        });

        // ── Skip button ───────────────────────────────────────────────
        btnSkip.setOnClickListener(v -> goToLogin());

        updateNextButton(0);
    }

    // ── Navigation ────────────────────────────────────────────────────

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // ── Button state ──────────────────────────────────────────────────

    private void updateNextButton(int position) {
        boolean isLast = position == items.size() - 1;

        // Amber CTA on last page, blue on all others
        btnNext.setBackgroundResource(
                isLast ? R.drawable.bg_btn_next_cta : R.drawable.bg_btn_next_ocean
        );

        // Tint arrow: dark on amber, light on blue
        btnNext.setColorFilter(getResources().getColor(
                isLast ? R.color.ocean_dark : R.color.ocean_background,
                getTheme()
        ));

        // Hide Skip on last page
        btnSkip.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
    }

    // ── Dot indicators ────────────────────────────────────────────────

    private void buildDots(int currentPage) {
        dotsLayout.removeAllViews();

        int dp7  = dp(7);
        int dp20 = dp(20);

        for (int i = 0; i < items.size(); i++) {
            ImageView dot = new ImageView(this);

            LinearLayout.LayoutParams params;

            if (i == currentPage) {
                // Active dot → pill shape (20 × 7 dp)
                dot.setImageResource(R.drawable.dot_active);
                params = new LinearLayout.LayoutParams(dp20, dp7);
            } else {
                // Inactive dot → circle (7 × 7 dp)
                dot.setImageResource(R.drawable.dot_inactive);
                params = new LinearLayout.LayoutParams(dp7, dp7);
            }

            params.setMargins(dp(4), 0, dp(4), 0);
            dot.setLayoutParams(params);
            dotsLayout.addView(dot);
        }
    }

    // ── Utility: dp → px ─────────────────────────────────────────────

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()
        ));
    }
}
