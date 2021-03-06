package com.pawandeep.teeterswipeup.swipelike.internal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.pawandeep.teeterswipeup.R;
import com.pawandeep.teeterswipeup.swipelike.StackFrom;
import com.pawandeep.teeterswipeup.swipelike.SwipeDirection;

import java.util.LinkedList;
import java.util.List;

public class CardStackViewLeft extends FrameLayout {

    public interface CardEventListener {
        void onCardDraggingLeft(float percentX, float percentY);
        void onCardSwipedLeft(SwipeDirection direction);
        void onCardReversedLeft();
        void onCardMovedToOriginLeft();
        void onCardClickedLeft(int index);
    }

    private CardStackOption option = new CardStackOption();
    private CardStackState state = new CardStackState();

    private BaseAdapter adapter = null;
    private LinkedList<CardContainerViewLeft> containers = new LinkedList<>();
    private CardEventListener cardEventListener = null;
    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            boolean shouldReset = false;
            if (state.isPaginationReserved) {
                state.isPaginationReserved = false;
            } else {
                boolean isSameCount = state.lastCount == adapter.getCount();
                shouldReset = !isSameCount;
            }
            initialize(shouldReset);
            state.lastCount = adapter.getCount();
        }
    };
    private CardContainerViewLeft.ContainerEventListener containerEventListener = new CardContainerViewLeft.ContainerEventListener() {
        @Override
        public void onContainerDragging(float percentX, float percentY) {
            update(percentX, percentY);
        }
        @Override
        public void onContainerSwiped(Point point, SwipeDirection direction) {
            swipe(point, direction);
        }
        @Override
        public void onContainerMovedToOrigin() {
            initializeCardStackPosition();
            if (cardEventListener != null) {
                cardEventListener.onCardMovedToOriginLeft();
            }
        }
        @Override
        public void onContainerClicked() {
            if (cardEventListener != null) {
                cardEventListener.onCardClickedLeft(state.topIndex);
            }
        }
    };

    public CardStackViewLeft(Context context) {
        this(context, null);
    }

    public CardStackViewLeft(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardStackViewLeft(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardStackView);
        setVisibleCount(array.getInt(R.styleable.CardStackView_visibleCount, option.visibleCount));
        setSwipeThreshold(array.getFloat(R.styleable.CardStackView_swipeThreshold, option.swipeThreshold));
        setTranslationDiff(array.getFloat(R.styleable.CardStackView_translationDiff, option.translationDiff));
        setScaleDiff(array.getFloat(R.styleable.CardStackView_scaleDiff, option.scaleDiff));
        setStackFrom(StackFrom.values()[array.getInt(R.styleable.CardStackView_stackFrom, option.stackFrom.ordinal())]);
        setElevationEnabled(array.getBoolean(R.styleable.CardStackView_elevationEnabled, option.isElevationEnabled));
        setSwipeEnabled(array.getBoolean(R.styleable.CardStackView_swipeEnabled, option.isSwipeEnabled));
        setSwipeDirection(SwipeDirection.from(array.getInt(R.styleable.CardStackView_swipeDirection, 0)));
        setLeftOverlay(array.getResourceId(R.styleable.CardStackView_leftOverlay, 0));
        setRightOverlay(array.getResourceId(R.styleable.CardStackView_rightOverlay, 0));
        setBottomOverlay(array.getResourceId(R.styleable.CardStackView_bottomOverlay, 0));
        setTopOverlay(array.getResourceId(R.styleable.CardStackView_topOverlay, 0));
        array.recycle();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (state.isInitialized && visibility == View.VISIBLE) {
            initializeCardStackPosition();
        }
    }

    private void initialize(boolean shouldReset) {
        resetIfNeeded(shouldReset);
        initializeViews();
        initializeCardStackPosition();
        initializeViewContents();
    }

    private void resetIfNeeded(boolean shouldReset) {
        if (shouldReset) {
            state.reset();
        }
    }

    private void initializeViews() {
        removeAllViews();
        containers.clear();

        for (int i = 0; i < option.visibleCount; i++) {
            CardContainerViewLeft view = (CardContainerViewLeft) LayoutInflater.from(getContext())
                    .inflate(R.layout.card_container, this, false);
            view.setDraggable(false);
            view.setCardStackOption(option);
            view.setOverlayLeft(option.leftOverlay, option.bottomOverlay, option.topOverlay);
            containers.add(0, view);
            addView(view);
        }

        containers.getFirst().setContainerEventListener(containerEventListener);

        state.isInitialized = true;
    }

    private void initializeCardStackPosition() {
        clear();
        update(0f, 0f);
    }

    private void initializeViewContents() {
        for (int i = 0; i < option.visibleCount; i++) {
            CardContainerViewLeft container = containers.get(i);
            int adapterIndex = state.topIndex + i;

            if (adapterIndex < adapter.getCount()) {
                ViewGroup parent = container.getContentContainer();
                View child = adapter.getView(adapterIndex, parent.getChildAt(0), parent);
                if (parent.getChildCount() == 0) {
                    parent.addView(child);
                }
                container.setVisibility(View.VISIBLE);
            } else {
                container.setVisibility(View.GONE);
            }
        }
        if (!adapter.isEmpty()) {
            getTopView().setDraggable(true);
        }
    }

    private void loadNextView() {
        int lastIndex = state.topIndex + option.visibleCount - 1;
        boolean hasNextCard = lastIndex < adapter.getCount();
        if (hasNextCard) {
            CardContainerViewLeft container = getBottomView();
            container.setDraggable(false);
            ViewGroup parent = container.getContentContainer();
            View child = adapter.getView(lastIndex, parent.getChildAt(0), parent);
            if (parent.getChildCount() == 0) {
                parent.addView(child);
            }
        } else {
            CardContainerViewLeft container = getBottomView();
            container.setDraggable(false);
            container.setVisibility(View.GONE);
        }

        boolean hasCard = state.topIndex < adapter.getCount();
        if (hasCard) {
            getTopView().setDraggable(true);
        }
    }

    private void clear() {
        for (int i = 0; i < option.visibleCount; i++) {
            CardContainerViewLeft view = containers.get(i);
            view.reset();
            ViewCompat.setTranslationX(view, 0f);
            ViewCompat.setTranslationY(view, 0f);
            ViewCompat.setScaleX(view, 1f);
            ViewCompat.setScaleY(view, 1f);
            ViewCompat.setRotation(view, 0f);
        }
    }

    private void update(float percentX, float percentY) {
        if (cardEventListener != null) {
            cardEventListener.onCardDraggingLeft(percentX, percentY);
        }

        if (!option.isElevationEnabled) {
            return;
        }

        for (int i = 1; i < option.visibleCount; i++) {
            CardContainerViewLeft view = containers.get(i);

            float currentScale = 1f - (i * option.scaleDiff);
            float nextScale = 1f - ((i - 1) * option.scaleDiff);
            float percent = currentScale + (nextScale - currentScale) * Math.abs(percentX);
            ViewCompat.setScaleX(view, percent);
            ViewCompat.setScaleY(view, percent);

            float currentTranslationY = i * Util.toPx(getContext(), option.translationDiff);
            if (option.stackFrom == StackFrom.Top) {
                currentTranslationY *= -1;
            }

            float nextTranslationY = (i - 1) * Util.toPx(getContext(), option.translationDiff);
            if (option.stackFrom == StackFrom.Top) {
                nextTranslationY *= -1;
            }

            float translationY = currentTranslationY - Math.abs(percentX) * (currentTranslationY - nextTranslationY);
            ViewCompat.setTranslationY(view, translationY);
        }
    }

    public void performReverse(Point point, View prevView, final Animator.AnimatorListener listener) {
        reorderForReverse(prevView);
        CardContainerViewLeft topView = getTopView();
        ViewCompat.setTranslationX(topView, point.x);
        ViewCompat.setTranslationY(topView, -point.y);
        topView.animate()
                .translationX(topView.getViewOriginX())
                .translationY(topView.getViewOriginY())
                .setListener(listener)
                .setDuration(400L)
                .start();
    }

    public void performSwipe(Point point, final Animator.AnimatorListener listener) {
        getTopView().animate()
                .translationX(point.x)
                .translationY(-point.y)
                .setDuration(400L)
                .setListener(listener)
                .start();
    }

    public void performSwipe(SwipeDirection direction, AnimatorSet set, final Animator.AnimatorListener listener) {
        if (direction == SwipeDirection.Left) {
            getTopView().showLeftOverlay();
            getTopView().setOverlayAlpha(1f);
        } else if (direction == SwipeDirection.Right) {
            getTopView().showRightOverlay();
            getTopView().setOverlayAlpha(1f);
        } else if (direction == SwipeDirection.Bottom){
            getTopView().showBottomOverlay();
            getTopView().setOverlayAlpha(1f);
        } else if (direction == SwipeDirection.Top){
            getTopView().showTopOverlay();
            getTopView().setOverlayAlpha(1f);
        }
        set.addListener(listener);
        set.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                CardContainerViewLeft view = getTopView();
                update(view.getPercentX(), view.getPercentY());
                return input;
            }
        });
        set.start();
    }

    private void moveToBottom(CardContainerViewLeft container) {
        CardStackViewLeft parent = (CardStackViewLeft) container.getParent();
        if (parent != null) {
            parent.removeView(container);
            parent.addView(container, 0);
        }
    }

    private void moveToTop(CardContainerViewLeft container, View child) {
        CardStackViewLeft parent = (CardStackViewLeft) container.getParent();
        if (parent != null) {
            parent.removeView(container);
            parent.addView(container);

            container.getContentContainer().removeAllViews();
            container.getContentContainer().addView(child);
            container.setVisibility(View.VISIBLE);
        }
    }

    private void reorderForSwipe() {
        moveToBottom(getTopView());
        containers.addLast(containers.removeFirst());
    }

    private void reorderForReverse(View prevView) {
        CardContainerViewLeft bottomView = getBottomView();
        moveToTop(bottomView, prevView);
        containers.addFirst(containers.removeLast());
    }

    private void executePreSwipeTask() {
        containers.getFirst().setContainerEventListener(null);
        containers.getFirst().setDraggable(false);
        if (containers.size() > 1) {
            containers.get(1).setContainerEventListener(containerEventListener);
            containers.get(1).setDraggable(true);
        }
    }

    private void executePostSwipeTask(Point point, SwipeDirection direction) {
        reorderForSwipe();

        state.lastPoint = point;

        initializeCardStackPosition();

        state.topIndex++;

        if (cardEventListener != null) {
            cardEventListener.onCardSwipedLeft(direction);
        }

        loadNextView();

        containers.getLast().setContainerEventListener(null);
        containers.getFirst().setContainerEventListener(containerEventListener);
    }

    private void executePostReverseTask() {
        state.lastPoint = null;

        initializeCardStackPosition();

        state.topIndex--;

        if (cardEventListener != null) {
            cardEventListener.onCardReversedLeft();
        }

        containers.getLast().setContainerEventListener(null);
        containers.getFirst().setContainerEventListener(containerEventListener);

        getTopView().setDraggable(true);
    }

    public void setCardEventListener(CardEventListener listener) {
        this.cardEventListener = listener;
    }

    public void setAdapter(BaseAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = adapter;
        this.adapter.registerDataSetObserver(dataSetObserver);
        this.state.lastCount = adapter.getCount();
        initialize(true);
    }

    public void setVisibleCount(int visibleCount) {
        option.visibleCount = visibleCount;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setSwipeThreshold(float swipeThreshold) {
        option.swipeThreshold = swipeThreshold;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setTranslationDiff(float translationDiff) {
        option.translationDiff = translationDiff;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setScaleDiff(float scaleDiff) {
        option.scaleDiff = scaleDiff;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setStackFrom(StackFrom stackFrom) {
        option.stackFrom = stackFrom;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setElevationEnabled(boolean isElevationEnabled) {
        option.isElevationEnabled = isElevationEnabled;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setSwipeEnabled(boolean isSwipeEnabled) {
        option.isSwipeEnabled = isSwipeEnabled;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setSwipeDirection(List<SwipeDirection> swipeDirection) {
        option.swipeDirection = swipeDirection;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setLeftOverlay(int leftOverlay) {
        option.leftOverlay = leftOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setRightOverlay(int rightOverlay) {
        option.rightOverlay = rightOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setBottomOverlay(int bottomOverlay) {
        option.bottomOverlay = bottomOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setTopOverlay(int topOverlay) {
        option.topOverlay = topOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setPaginationReserved() {
        state.isPaginationReserved = true;
    }

    public void swipe(final Point point, final SwipeDirection direction) {
        executePreSwipeTask();
        performSwipe(point, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                executePostSwipeTask(point, direction);
            }
        });
    }

    public void swipe(final SwipeDirection direction, AnimatorSet set) {
        executePreSwipeTask();
        performSwipe(direction, set, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                executePostSwipeTask(new Point(0, -2000), direction);
            }
        });
    }

    public void reverse() {
        if (state.lastPoint != null) {
            ViewGroup parent = containers.getLast();
            View prevView = adapter.getView(state.topIndex - 1, null, parent);
            performReverse(state.lastPoint, prevView, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    executePostReverseTask();
                }
            });
        }
    }

    public CardContainerViewLeft getTopView() {
        return containers.getFirst();
    }

    public CardContainerViewLeft getBottomView() {
        return containers.getLast();
    }

    public int getTopIndex() {
        return state.topIndex;
    }

}
