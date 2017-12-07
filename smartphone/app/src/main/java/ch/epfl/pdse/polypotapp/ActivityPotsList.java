package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ActivityPotsList extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pots_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();
                        mAdapter.onItemMove(fromPos, toPos);

                        return true;
                    }
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        mAdapter.onItemDismiss(fromPos);
                    }
                }
            );

        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Reset setup configuration to default values
        SharedPreferences sharedPreferencesSetup = getSharedPreferences("setup_and_add", Context.MODE_PRIVATE);
        sharedPreferencesSetup.edit().clear().apply();
        PreferenceManager.setDefaultValues(this, "setup_and_add", MODE_PRIVATE, R.xml.setup_and_add_configuration, true);

        mAdapter.updateItems(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pots_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(this, ActivitySetupAndAdd.class);
                startActivity(intent);

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void cardClick(View v) {
        TextView uuid = v.findViewById(R.id.uuid);
        TextView server = v.findViewById(R.id.server);

        Intent intent = new Intent(this, ActivityMain.class);
        Bundle b = new Bundle();
        b.putString("uuid", uuid.getText().toString());
        b.putString("server", server.getText().toString());
        intent.putExtras(b);
        startActivity(intent);
    }

    public static class PotViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView server;
        final TextView uuid;

        PotViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            server = itemView.findViewById(R.id.server);
            uuid = itemView.findViewById(R.id.uuid);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<PotViewHolder>{
        private ArrayList<Pot> mPots;
        private Context mContext;

        @Override
        public PotViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_pot_card, viewGroup, false);
            return new PotViewHolder(v);
        }

        @Override
        public void onBindViewHolder(PotViewHolder potViewHolder, int i) {
            potViewHolder.name.setText(mPots.get(i).name);
            potViewHolder.server.setText(mPots.get(i).server);
            potViewHolder.uuid.setText(mPots.get(i).uuid);
        }

        public void updateItems(Context context) {
            mContext = context;
            mPots = Pot.getPots(context);
            notifyDataSetChanged();
        }

        public void onItemDismiss(int position) {
            // Save the pot and its position in the list
            final Pot deletedPot = mPots.remove(position);
            final int deletedPosition = position;

            // Save its configuration
            SharedPreferences sharedPreferencesPot = getSharedPreferences(Pot.getPreferenceName(deletedPot.server, deletedPot.uuid), Context.MODE_PRIVATE);
            final Map<String, ?> deletedPreferences = sharedPreferencesPot.getAll();
            sharedPreferencesPot.edit().clear().apply();

            // Update view
            notifyItemRemoved(position);

            // Save new list
            Pot.savePots(mContext, mPots);

            // Show undo action
            Snackbar.make(mRecyclerView, R.string.pot_deleted, Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Re-add pot
                    mPots.add(deletedPosition, deletedPot);

                    // Restore configuration
                    SharedPreferences sharedPreferencesPot = getSharedPreferences(Pot.getPreferenceName(deletedPot.server, deletedPot.uuid), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesPot.edit();
                    Set<String> keys = deletedPreferences.keySet();
                    for(String key : keys) {
                        Object value = deletedPreferences.get(key);

                        if(value instanceof String) {
                            editor.putString(key, (String) value);
                        } else if(value instanceof Integer) {
                            editor.putInt(key, (int) value);
                        }
                    }
                    editor.apply();

                    // Update view
                    notifyItemInserted(deletedPosition);

                    // Save new list
                    Pot.savePots(mContext, mPots);
                }
            }).show();
        }

        public void onItemMove(int fromPosition, int toPosition) {
            // Moving animation
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mPots, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mPots, i, i - 1);
                }
            }

            // Update view
            notifyItemMoved(fromPosition, toPosition);

            // Save new list
            Pot.savePots(mContext, mPots);
        }

        @Override
        public int getItemCount() {
            return mPots.size();
        }
    }
}
