package com.italankin.lnch.feature.home.repository;

import com.italankin.lnch.model.descriptor.DescriptorArg;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * Holds current UI state of home descriptor items
 */
public interface HomeDescriptorsState {

    /**
     * @return whenever current items state is initial
     */
    boolean isInitialState();

    /**
     * @param items new home descriptor items
     */
    void setItems(List<DescriptorUi> items);

    /**
     * @return current home descriptor items
     */
    List<DescriptorUi> items();

    /**
     * Find descriptor by id
     *
     * @return item and it's position in a list, or {@code null}
     */
    @Nullable
    DescriptorUiEntry<? extends DescriptorUi> find(String id);

    /**
     * Find descriptor by {@link DescriptorArg}
     *
     * @return item and it's position in a list, or {@code null}
     */
    @Nullable
    <T extends DescriptorUi> DescriptorUiEntry<T> find(DescriptorArg arg);

    /**
     * Remove descriptor by a given {@link DescriptorArg}
     */
    void removeByArg(DescriptorArg arg);

    void removeById(String id);

    /**
     * Find descriptor by id with a given type
     *
     * @return item and it's position in a list, or {@code null}
     */
    @Nullable
    <T extends DescriptorUi> DescriptorUiEntry<T> find(Class<T> type, String id);

    /**
     * @return all descriptors of a given type
     */
    <T extends DescriptorUi> List<T> allByType(Class<T> type);

    /**
     * @return all items in the folder
     */
    List<DescriptorUi> folderItems(FolderDescriptorUi folder);

    /**
     * Insert item to the list
     */
    void insertItem(DescriptorUi item);

    /**
     * Notify listeners that item is updated
     */
    void updateItem(DescriptorUi item);

    /**
     * Move item from {@code fromPosition} to {@code toPosition}
     */
    void moveItem(int fromPosition, int toPosition);

    void addCallback(Callback callback);

    void removeCallback(Callback callback);

    interface Callback {

        void onNewItems(List<DescriptorUi> items);

        void onItemChanged(int position, DescriptorUi item);

        void onItemRemoved(int position, DescriptorUi item);

        void onItemInserted(int position, DescriptorUi item);

        void onItemMoved(int fromPosition, int toPosition);
    }
}
