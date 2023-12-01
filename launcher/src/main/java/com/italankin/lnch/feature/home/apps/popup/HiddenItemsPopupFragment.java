package com.italankin.lnch.feature.home.apps.popup;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.DescriptorFragmentResultContract;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.util.DescriptorIconResolver;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.imageloader.ImageLoader;
import com.italankin.lnch.util.imageloader.cache.Cache;
import com.italankin.lnch.util.imageloader.cache.LruCache;
import com.italankin.lnch.util.widget.popup.PopupFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HiddenItemsPopupFragment extends PopupFragment {

    public static HiddenItemsPopupFragment newInstance(
            String requestKey,
            @Nullable Rect anchor) {
        HiddenItemsPopupFragment fragment = new HiddenItemsPopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String BACKSTACK_NAME = "hidden_items_popup";
    private static final String TAG = "hidden_items_popup";

    private final Cache imageLoaderCache = new LruCache(48);

    private HomeDescriptorsState homeDescriptorsState;

    private ImageLoader imageLoader;

    private RecyclerView list;

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeDescriptorsState = LauncherApp.daggerService.main().homeDescriptorState();
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        imageLoader = new ImageLoader.Builder(context)
                .cache(imageLoaderCache)
                .build();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        list = new RecyclerView(requireContext());
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        itemsContainer.addView(list);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<IgnorableDescriptorUi> ignored = new ArrayList<>();
        for (IgnorableDescriptorUi item : homeDescriptorsState.allByType(IgnorableDescriptorUi.class)) {
            if (item.isIgnored()) {
                ignored.add(item);
            }
        }
        Collections.sort(ignored, (lhs, rhs) -> getVisibleLabel(lhs).compareTo(getVisibleLabel(rhs)));

        list.setAdapter(new Adapter(getLayoutInflater(), imageLoader, ignored, descriptorUi -> {
            dismiss();
            sendResult(new ShowContract().result(descriptorUi.getDescriptor().getId()));
        }));

        showPopup();
    }

    private static String getVisibleLabel(DescriptorUi descriptorUi) {
        if (descriptorUi instanceof CustomLabelDescriptorUi) {
            return ((CustomLabelDescriptorUi) descriptorUi).getVisibleLabel();
        } else {
            return DescriptorUtils.getVisibleLabel(descriptorUi.getDescriptor());
        }
    }

    public static class ShowContract extends DescriptorFragmentResultContract {
        public ShowContract() {
            super("customize_show");
        }
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final LayoutInflater inflater;
        private final ImageLoader imageLoader;
        private final List<? extends IgnorableDescriptorUi> items;
        private final ClickListener clickListener;

        private Adapter(LayoutInflater inflater, ImageLoader imageLoader, List<? extends IgnorableDescriptorUi> items,
                ClickListener clickListener) {
            this.inflater = inflater;
            this.imageLoader = imageLoader;
            this.items = items;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.item_hidden, parent, false);
            ViewHolder holder = new ViewHolder(itemView);
            itemView.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    clickListener.onDescriptorClick(items.get(pos));
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull HiddenItemsPopupFragment.Adapter.ViewHolder holder, int position) {
            DescriptorUi item = items.get(position);
            holder.label.setText(getVisibleLabel(item));
            DescriptorIconResolver.resolve(item, holder.resolveResult);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView label;
            final DescriptorIconResolver.ResolveResult resolveResult = new DescriptorIconResolver.ResolveResult() {
                @Override
                public void uriIcon(Uri uri) {
                    imageLoader.load(uri).into(icon);
                }

                @Override
                public void resourceIcon(int drawableId) {
                    imageLoader.cancel(icon);
                    icon.setImageResource(drawableId);
                }
            };

            ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                label = itemView.findViewById(R.id.label);
            }
        }

        interface ClickListener {
            void onDescriptorClick(IgnorableDescriptorUi descriptorUi);
        }
    }
}
