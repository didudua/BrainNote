package Lop48K14_1.group2.brainnote.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.NoteAdapter;

public class MoreOptionsBottomSheet extends BottomSheetDialogFragment {

    private ViewModeListener listener;
    private int currentViewMode;

    // Views for each option
    private LinearLayout optionTitle, optionSmall, optionLarge, optionTag;
    private TextView titleText, smallText, largeText, tagText;
    private ImageView smallIcon, tagIcon;
    private TextView titleIcon, largeIcon; // These are actually TextViews with "Aa" and "≡" symbols

    // Color constants
    private static final int COLOR_SELECTED = 0xFF4285F4; // Blue color
    private static final int COLOR_UNSELECTED = 0xFF757575; // Gray color

    public interface ViewModeListener {
        void onTitleViewSelected();
        void onSmallViewSelected();
        void onLargeViewSelected();
        void onGridViewSelected();
    }

    public static MoreOptionsBottomSheet newInstance(int currentViewMode) {
        MoreOptionsBottomSheet fragment = new MoreOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putInt("currentViewMode", currentViewMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentViewMode = getArguments().getInt("currentViewMode", NoteAdapter.VIEW_TYPE_LARGE);
        } else {
            // If no arguments, try to get from SharedPreferences
            SharedPreferences prefs = requireContext().getSharedPreferences("NoteViewPrefs", Context.MODE_PRIVATE);
            currentViewMode = prefs.getInt("ViewMode", NoteAdapter.VIEW_TYPE_LARGE);
        }
    }

    public void setViewModeListener(ViewModeListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_btn_more, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set the dialog to expand fully when shown
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        optionTitle = view.findViewById(R.id.option_title);
        optionSmall = view.findViewById(R.id.option_small);
        optionLarge = view.findViewById(R.id.option_large);
        optionTag = view.findViewById(R.id.option_tag);

        // Find all text and icon views
        // For title option
        titleIcon = optionTitle.findViewById(R.id.title_icon);
        titleText = optionTitle.findViewById(R.id.title_text);

        // For small option
        smallIcon = optionSmall.findViewById(R.id.small_icon);
        smallText = optionSmall.findViewById(R.id.small_text);

        // For large option
        largeIcon = optionLarge.findViewById(R.id.large_icon);
        largeText = optionLarge.findViewById(R.id.large_text);

        // For tag/grid option
        tagIcon = optionTag.findViewById(R.id.tag_icon);
        tagText = optionTag.findViewById(R.id.tag_text);

        // Update UI to show current selection
        updateSelectionUI();

        // Set click listeners
        optionTitle.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTitleViewSelected();
            }
            currentViewMode = NoteAdapter.VIEW_TYPE_TITLE;
            updateSelectionUI();
            dismiss();
        });

        optionSmall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSmallViewSelected();
            }
            currentViewMode = NoteAdapter.VIEW_TYPE_SMALL;
            updateSelectionUI();
            dismiss();
        });

        optionLarge.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLargeViewSelected();
            }
            currentViewMode = NoteAdapter.VIEW_TYPE_LARGE;
            updateSelectionUI();
            dismiss();
        });

        optionTag.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGridViewSelected();
            }
            currentViewMode = NoteAdapter.VIEW_TYPE_GRID;
            updateSelectionUI();
            dismiss();
        });
    }

    private void updateSelectionUI() {
        // Reset all to unselected state
        setOptionColor(titleIcon, titleText, COLOR_UNSELECTED);
        setOptionColor(smallIcon, smallText, COLOR_UNSELECTED);
        setOptionColor(largeIcon, largeText, COLOR_UNSELECTED);
        setOptionColor(tagIcon, tagText, COLOR_UNSELECTED);

        // Set the selected option to blue
        switch (currentViewMode) {
            case NoteAdapter.VIEW_TYPE_TITLE:
                setOptionColor(titleIcon, titleText, COLOR_SELECTED);
                break;
            case NoteAdapter.VIEW_TYPE_SMALL:
                setOptionColor(smallIcon, smallText, COLOR_SELECTED);
                break;
            case NoteAdapter.VIEW_TYPE_LARGE:
                setOptionColor(largeIcon, largeText, COLOR_SELECTED);
                break;
            case NoteAdapter.VIEW_TYPE_GRID:
                setOptionColor(tagIcon, tagText, COLOR_SELECTED);
                break;
        }
    }

    private void setOptionColor(View icon, TextView text, int color) {
        if (icon != null) {
            if (icon instanceof ImageView) {
                ((ImageView) icon).setColorFilter(color);
            } else if (icon instanceof TextView) {
                ((TextView) icon).setTextColor(color);
            }
        }

        if (text != null) {
            text.setTextColor(color);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Make sure it can be dismissed by clicking outside
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true);
        }
    }
}

