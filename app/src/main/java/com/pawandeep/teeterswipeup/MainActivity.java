package com.pawandeep.teeterswipeup;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.pawandeep.teeterswipeup.adapter.SecondAdapter;
import com.pawandeep.teeterswipeup.adapter.TouristSpotCardAdapter;
import com.pawandeep.teeterswipeup.swipelike.SwipeDirection;
import com.pawandeep.teeterswipeup.swipelike.internal.CardStackViewLeft;
import com.pawandeep.teeterswipeup.swipelike.internal.CardStackViewRight;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private CardStackViewLeft leftSwipe_mCardStackView;
    private CardStackViewRight rightSwipe_mCardStackView;
    private TouristSpotCardAdapter adapter;
    private SecondAdapter adapter2;
    boolean mDoubleBool = false, mSingleBool = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
        reloadLeft();
        reloadRight();
    }

    private List<TouristSpot> createTouristSpots() {
        List<TouristSpot> spots = new ArrayList<>();
        spots.add(new TouristSpot("Yasaka Shrine", "Kyoto", "https://source.unsplash.com/Xq1ntWruZQI/600x800"));
        spots.add(new TouristSpot("Fushimi Inari Shrine", "Kyoto", "https://source.unsplash.com/NYyCqdBOKwc/600x800"));
        spots.add(new TouristSpot("Bamboo Forest", "Kyoto", "https://source.unsplash.com/buF62ewDLcQ/600x800"));
        spots.add(new TouristSpot("Brooklyn Bridge", "New York", "https://source.unsplash.com/THozNzxEP3g/600x800"));
        spots.add(new TouristSpot("Empire State Building", "New York", "https://source.unsplash.com/USrZRcRS2Lw/600x800"));
        spots.add(new TouristSpot("The statue of Liberty", "New York", "https://source.unsplash.com/PeFk7fzxTdk/600x800"));
        spots.add(new TouristSpot("Louvre Museum", "Paris", "https://source.unsplash.com/LrMWHKqilUw/600x800"));
        spots.add(new TouristSpot("Eiffel Tower", "Paris", "https://source.unsplash.com/HN-5Z6AmxrM/600x800"));
        spots.add(new TouristSpot("Big Ben", "London", "https://source.unsplash.com/CdVAUADdqEc/600x800"));
        spots.add(new TouristSpot("Great Wall of China", "China", "https://source.unsplash.com/AWh9C-QjhE4/600x800"));
        return spots;
    }

    private TouristSpotCardAdapter createTouristSpotCardAdapter() {
        final TouristSpotCardAdapter adapter = new TouristSpotCardAdapter(getApplicationContext());
        adapter.addAll(createTouristSpots());
        return adapter;
    }


    private SecondAdapter createSecondAdapterCardAdapter() {
        final SecondAdapter adapter2 = new SecondAdapter(getApplicationContext());
        adapter2.addAll(createTouristSpots());
        return adapter2;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setup() {
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);

        rightSwipe_mCardStackView = (CardStackViewRight) findViewById(R.id.rightSwipe_mCardStackView);
        leftSwipe_mCardStackView = (CardStackViewLeft) findViewById(R.id.leftSwipe_mCardStackView);

        rightSwipe_mCardStackView.setCardEventListener(new CardStackViewRight.CardEventListener() {
            @Override
            public void onCardDraggingRight(float percentX, float percentY) {
                Log.e("~~~~~~~~~~~Right > " , "percentX:::   >> " + percentX + "  percentY:::   >> " + percentY);
            }

            @Override
            public void onCardSwipedRight(SwipeDirection direction) {
                Log.e("CardStackView", "onCardSwiped: " + direction.toString()+ "~~~~~~~~~~Right");
                Log.e("CardStackView", "topIndex: " + rightSwipe_mCardStackView.getTopIndex()+ "~~~~~~~~~~Right");
                if (rightSwipe_mCardStackView.getTopIndex() == adapter2.getCount() - 5) {
                    Log.e("CardStackView", "Paginate: " + rightSwipe_mCardStackView.getTopIndex());
                    paginate2();
                }
            }

            @Override
            public void onCardReversedRight() {

            }

            @Override
            public void onCardMovedToOriginRight() {

            }

            @Override
            public void onCardClickedRight(int index) {
                Log.e("~~~~~~~~~~~~~~~~~~> " , "INDEX Right:::   >> " + index);
            }
        });

        leftSwipe_mCardStackView.setCardEventListener(new CardStackViewLeft.CardEventListener() {
            @Override
            public void onCardDraggingLeft(float percentX, float percentY) {
                Log.e("~~~~~~~~~~~~~Left > " , "percentX:::   >> " + percentX + "  percentY:::   >> " + percentY);
            }

            @Override
            public void onCardSwipedLeft(SwipeDirection direction) {
                Log.e("CardStackView", "onCardSwiped: " + direction.toString()+ "~~~~~~~~~~Left");
                Log.e("CardStackView", "topIndex: " + leftSwipe_mCardStackView.getTopIndex()+ "~~~~~~~~~~Left");
                if (leftSwipe_mCardStackView.getTopIndex() == adapter.getCount() - 5) {
                    Log.e("CardStackView", "Paginate: " + leftSwipe_mCardStackView.getTopIndex());
                    paginate();
                }
            }

            @Override
            public void onCardReversedLeft() {

            }

            @Override
            public void onCardMovedToOriginLeft() {

            }

            @Override
            public void onCardClickedLeft(int index) {
                Log.e("~~~~~~~~~~~~~~~~~~> " , "INDEX Left:::   >> " + index);
            }
        });
    }

    private void reloadLeft() {
        leftSwipe_mCardStackView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter = createTouristSpotCardAdapter();
                leftSwipe_mCardStackView.setAdapter(adapter);
                leftSwipe_mCardStackView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, 1000);
    }


    private void reloadRight() {
        rightSwipe_mCardStackView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter2 = createSecondAdapterCardAdapter();
                rightSwipe_mCardStackView.setAdapter(adapter2);
                rightSwipe_mCardStackView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, 1000);
    }

    private void paginate() {
        leftSwipe_mCardStackView.setPaginationReserved();
        adapter.addAll(createTouristSpots());
        adapter.notifyDataSetChanged();
    }

    private void paginate2() {
        rightSwipe_mCardStackView.setPaginationReserved();
        adapter2.addAll(createTouristSpots());
        adapter2.notifyDataSetChanged();
    }
}
