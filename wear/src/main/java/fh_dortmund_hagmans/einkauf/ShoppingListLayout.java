package fh_dortmund_hagmans.einkauf;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Beschreibt das Layout eines Listitems
 * @author Hendrik Hagmans
 */
public class ShoppingListLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    // Kreis, der neben dem Artikelnamen angezeigt wird
    private ImageView mCircle;
    // Name des Artikels
    private TextView mName;

    private final float mFadedTextAlpha;
    private final int mFadedCircleColor;
    private final int mChosenCircleColor;

    public ShoppingListLayout(Context context) {
        this(context, null);
    }

    public ShoppingListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShoppingListLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        mFadedTextAlpha = getResources()
                .getInteger(R.integer.action_text_faded_alpha) / 100f;
        mFadedCircleColor = getResources().getColor(R.color.wl_gray);
        mChosenCircleColor = getResources().getColor(R.color.wl_blue);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.name);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        mName.setAlpha(1f);
        ((GradientDrawable) mCircle.getDrawable()).setColor(mChosenCircleColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        ((GradientDrawable) mCircle.getDrawable()).setColor(mFadedCircleColor);
        mName.setAlpha(mFadedTextAlpha);
    }
}
