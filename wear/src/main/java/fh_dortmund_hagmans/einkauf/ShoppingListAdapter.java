package fh_dortmund_hagmans.einkauf;

import android.content.Context;
import android.graphics.Paint;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fh_dortmund_hagmans.einkauf.models.Article;

/** Adapter für die Listview, die die Artikel beinhaltet
 * @author Hendrik Hagmans
 */
public class ShoppingListAdapter extends WearableListView.Adapter {
    private Article[] mDataset;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public ShoppingListAdapter(Context context, Article[] dataset) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    /**
     * Klasse, die die Items der Listview beschreibt
     */
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.name);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    /**
     * Ersetzt die Items in der Listview und setzt den checked Status der ViewItems
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView view = itemHolder.textView;
        // ArtikelName in die TextView setzen
        view.setText(mDataset[position].getName());
        // Setze die Artikel Id als Tag, um später wieder darauf zugreifen zu können
        holder.itemView.setTag(mDataset[position].getId());
        if (mDataset[position].isChecked()) { // Wenn der Artikel gechecked ist, in der View als durchgestrichen darstellen
            view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else { // Nicht gechecked, daher nicht als durchgestrichen darstellen
            view.setPaintFlags(view.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    public void setmDataset(Article[] dataset) {
        this.mDataset = dataset;
    }

    public Article[] getmDataset() {
        return this.mDataset;
    }
}
