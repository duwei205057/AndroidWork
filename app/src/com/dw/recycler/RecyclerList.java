package com.dw.recycler;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.dw.R;

/**
 * Created by dw on 17-11-29.
 */

public class RecyclerList extends Activity {

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.recycler);
        mRecyclerView = (RecyclerView)findViewById(R.id.list);
    }

    class DataAdapter extends RecyclerView.Adapter{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

    }

    /*class ContentViewHold extends RecyclerView.ViewHolder{

    }*/
}
