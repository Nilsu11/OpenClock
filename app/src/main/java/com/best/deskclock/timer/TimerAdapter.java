/*
 * Copyright (C) 2023 The LineageOS Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.best.deskclock.timer;

import static com.best.deskclock.DeskClockApplication.getDefaultSharedPreferences;
import static com.best.deskclock.settings.PreferencesDefaultValues.DEFAULT_SORT_TIMER_MANUALLY;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.best.deskclock.data.DataModel;
import com.best.deskclock.data.SettingsDAO;
import com.best.deskclock.data.Timer;
import com.best.deskclock.data.TimerListener;
import com.best.deskclock.R;
import com.best.deskclock.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This adapter produces a {@link TimerViewHolder} for each timer.
 */
public class TimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements TimerListener {

    private final int SINGLE_TIMER = R.layout.timer_single_item;
    private final int MULTIPLE_TIMERS = R.layout.timer_item;

    /** Maps each timer id to the corresponding {@link TimerViewHolder} that draws it. */
    private final Map<Integer, TimerViewHolder> mHolders = new ArrayMap<>();
    private final TimerClickHandler mTimerClickHandler;
    private final List<Timer> mTimers;
    private final Context mContext;
    private final SharedPreferences mPrefs;

    public TimerAdapter(Context context, SharedPreferences sharedPreferences, List<Timer> timers, TimerClickHandler timerClickHandler) {
        mContext = context;
        mPrefs = sharedPreferences;
        mTimers = timers;
        mTimerClickHandler = timerClickHandler;
    }

    @Override
    public int getItemCount() {
        return getTimers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mTimers.size() == 1) {
            return (ThemeUtils.isTablet() || ThemeUtils.isPortrait()) ? SINGLE_TIMER : MULTIPLE_TIMERS;
        } else {
            return MULTIPLE_TIMERS;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if (viewType == SINGLE_TIMER) {
            view = inflater.inflate(R.layout.timer_single_item, parent, false);
        } else {
            view = inflater.inflate(R.layout.timer_item, parent, false);
        }
        return new TimerViewHolder(view, mTimerClickHandler);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder itemViewHolder, int position) {
        TimerViewHolder holder = (TimerViewHolder) itemViewHolder;
        mHolders.put(getTimer(position).getId(), holder);
        holder.onBind(getTimer(position).getId());
    }

    @Override
    public void timerAdded(Context context, Timer timer) {
        saveTimerList(context);
        notifyDataSetChanged();
    }

    @Override
    public void timerRemoved(Timer timer) {
        mHolders.remove(timer.getId());
        notifyDataSetChanged();
    }

    @Override
    public void timerUpdated(Timer before, Timer after) {
        notifyDataSetChanged();
    }

    /**
     * @return {@code true} if at least one timer is in a state requiring continuous updates
     */
    boolean updateTime() {
        boolean continuousUpdates = false;
        for (TimerViewHolder holder : mHolders.values()) {
            continuousUpdates |= holder.updateTime();
        }
        return continuousUpdates;
    }

    Timer getTimer(int index) {
        return getTimers().get(index);
    }

    public List<Timer> getTimers() {
        List<Timer> timers = DataModel.getDataModel().getTimers();
        String timerSortingPreference = SettingsDAO.getTimerSortingPreference(mPrefs);
        if (!timerSortingPreference.equals(DEFAULT_SORT_TIMER_MANUALLY)) {
            Collections.sort(timers, Timer.createTimerStateComparator(mContext));
        }
        return mTimers;
    }

    public void saveTimerList(Context context) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Convert list of IDs to string
        StringBuilder sb = new StringBuilder();
        for (Timer timer : mTimers) {
            sb.append(timer.getId()).append(",");
        }

        // Delete the last comma
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        editor.putString("timerOrder", sb.toString());
        editor.apply();
    }

    public void loadTimerList(Context context) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        String savedOrder = sharedPreferences.getString("timerOrder", null);

        if (savedOrder != null) {
            List<Timer> sortedTimers = new ArrayList<>();
            String[] timerIds = savedOrder.split(",");

            // Fill the list according to the saved order
            for (String id : timerIds) {
                int timerId = Integer.parseInt(id);
                for (Timer timer : mTimers) {
                    if (timer.getId() == timerId) {
                        sortedTimers.add(timer);
                        break;
                    }
                }
            }

            // Update list and notify adapter
            mTimers.clear();
            mTimers.addAll(sortedTimers);
            notifyDataSetChanged();
        }
    }

    public static class TimerItemTouchHelper extends ItemTouchHelper.Callback {

        private final List<Timer> mTimers;
        private final TimerAdapter mAdapter;

        public TimerItemTouchHelper(List<Timer> timers, TimerAdapter adapter) {
            mTimers = timers;
            mAdapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();

            Collections.swap(mTimers, fromPosition, toPosition);
            Objects.requireNonNull(recyclerView.getAdapter()).notifyItemMoved(fromPosition, toPosition);

            mAdapter.saveTimerList(recyclerView.getContext());

            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // Unused
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int dragFlags;
            String timerSortingPreference = SettingsDAO.getTimerSortingPreference(mAdapter.mPrefs);

            // Allow dragging only if timers are sorted manually
            if (timerSortingPreference.equals(DEFAULT_SORT_TIMER_MANUALLY)) {
                if (ThemeUtils.isTablet()) {
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                } else {
                    if (ThemeUtils.isLandscape()) {
                        dragFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                    } else {
                        dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    }
                }
            } else {
                dragFlags = 0;
            }

            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            if (isCurrentlyActive) {
                viewHolder.itemView.setTranslationZ(20f);
            } else {
                viewHolder.itemView.setTranslationZ(0f);
            }
        }
    }

}
