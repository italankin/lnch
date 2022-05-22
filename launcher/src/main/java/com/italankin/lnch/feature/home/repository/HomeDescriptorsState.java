package com.italankin.lnch.feature.home.repository;

import androidx.annotation.Nullable;

import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;

import java.util.List;

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
    HomeEntry<? extends DescriptorUi> find(String id);

    void removeById(String id);

    /**
     * Find descriptor by id with a given type
     *
     * @return item and it's position in a list, or {@code null}
     */
    @Nullable
    <T extends DescriptorUi> HomeEntry<T> find(Class<T> type, String id);

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

        default void onNewItems(List<DescriptorUi> items) {
        }

        default void onItemChanged(int position, DescriptorUi item) {
        }

        default void onItemRemoved(int position, DescriptorUi item) {
        }

        default void onItemInserted(int position, DescriptorUi item) {
        }

        default void onItemMoved(int fromPosition, int toPosition) {
        }
    }
}
