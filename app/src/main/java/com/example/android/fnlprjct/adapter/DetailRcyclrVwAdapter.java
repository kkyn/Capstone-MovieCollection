package com.example.android.fnlprjct.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.fnlprjct.MyQuery;
import com.example.android.fnlprjct.R;


public class DetailRcyclrVwAdapter extends RecyclerView.Adapter<DetailRcyclrVwAdapter.DetailsViewHolder> {

    private static final String LOG_TAG = DetailRcyclrVwAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_A = 0;
    private Cursor mCursor;
    private Context mContext;

    public DetailRcyclrVwAdapter(Context context) {
        this.mContext = context;
    }

    // basic requirement
    @Override
    public DetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (parent instanceof RecyclerView) {

            int layoutId = -1;
            switch (viewType) {

                case VIEW_TYPE_A: {

                    //=================================
                    layoutId = R.layout.cardview_review;
                    //=================================
                    break;
                }
            }
            View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);

            // Control whether a view can take focus
            view.setFocusable(true);

            DetailsViewHolder detailsViewHolder = new DetailsViewHolder(view);

            view.setTag(detailsViewHolder);

            return detailsViewHolder;
        }
        else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override // basic requirement
    public void onBindViewHolder(DetailsViewHolder holder, int position) {

        if (mCursor.moveToPosition(position)) {

            holder.reviewerTextView.setText(mCursor.getString(MyQuery.Reviews.COL_REVIEWER));
            holder.reviewTextView.setText(mCursor.getString(MyQuery.Reviews.COL_REVIEWCONTENT));
        }
    }


    @Override // basic requirement
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {

        mCursor = newCursor;

        this.notifyDataSetChanged();

    }
    //--------------------------------------
    //---- Begin, ViewHolder definition ----
    //--------------------------------------
    public class DetailsViewHolder extends RecyclerView.ViewHolder {

        public final TextView reviewerTextView;
        public final TextView reviewTextView;

        public DetailsViewHolder(View itemView) {
            super(itemView);
            reviewerTextView = (TextView) itemView.findViewById(R.id.reviewer_tv);
            reviewTextView = (TextView) itemView.findViewById(R.id.review_tv);
        }
    }
    //--------------------------------------
    //---- End,   ViewHolder definition ----
    //--------------------------------------

}
