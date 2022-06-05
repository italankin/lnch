package com.italankin.lnch.feature.settings.searchstore;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.ShortcutsFragment;
import com.italankin.lnch.feature.settings.apps.AppsSettingsFragment;
import com.italankin.lnch.feature.settings.backup.BackupFragment;
import com.italankin.lnch.feature.settings.experimental.ExperimentalSettingsFragment;
import com.italankin.lnch.feature.settings.hidden_items.HiddenItemsFragment;
import com.italankin.lnch.feature.settings.lookfeel.AppearanceFragment;
import com.italankin.lnch.feature.settings.lookfeel.LookAndFeelFragment;
import com.italankin.lnch.feature.settings.misc.MiscFragment;
import com.italankin.lnch.feature.settings.notifications.NotificationsFragment;
import com.italankin.lnch.feature.settings.search.SearchFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperOverlayFragment;
import com.italankin.lnch.feature.settings.widgets.WidgetsSettingsFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.Arrays;
import java.util.Collections;

final class SettingsEntries {

    static SettingsEntryImpl[] entries() {
        return new SettingsEntryImpl[]{
                /* --- Wallpaper --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_wallpaper)
                        .title(R.string.settings_home_wallpaper)
                        .category(R.string.settings_home_wallpaper)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(WallpaperFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_wallpaper_change)
                        .title(R.string.settings_home_wallpaper_change)
                        .category(R.string.settings_home_wallpaper)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(WallpaperFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.WALLPAPER_OVERLAY_SHOW)
                        .title(R.string.settings_home_wallpaper_overlay_show)
                        .category(R.string.settings_home_wallpaper)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(WallpaperFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.WALLPAPER_OVERLAY_COLOR)
                        .title(R.string.settings_home_wallpaper_overlay_color)
                        .category(R.string.settings_home_wallpaper)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(
                                    WallpaperFragment.newInstance(requestKey),
                                    WallpaperOverlayFragment.newInstance(requestKey)
                            );
                        })
                        .build(),

                /* --- Look and Feel --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_look_and_feel)
                        .title(R.string.settings_home_laf)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.COLOR_THEME)
                        .title(R.string.settings_home_laf_color_theme)
                        .category(R.string.settings_home_laf)
                        .addArraysSearchTokens(R.array.pref_desc_color_themes)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.HOME_ALIGNMENT)
                        .title(R.string.settings_home_laf_alignment)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.APPS_SORT_MODE)
                        .title(R.string.settings_apps_sorting)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.NAME_TRANSFORM)
                        .title(R.string.settings_apps_name_transform)
                        .addArraysSearchTokens(R.array.pref_desc_apps_name_transform)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.ITEM_WIDTH)
                        .title(R.string.settings_home_laf_other_width)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.HIDE_STATUS_BAR)
                        .title(R.string.settings_home_laf_other_hide_status_bar)
                        .summary(R.string.settings_home_laf_other_hide_status_bar_summary)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.APPS_LIST_ANIMATE)
                        .title(R.string.settings_home_laf_other_apps_list_animate)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.APPS_COLOR_OVERLAY_SHOW)
                        .title(R.string.settings_home_laf_other_color_overlay_show)
                        .summary(R.string.settings_home_laf_other_color_overlay_show_summary)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.APPS_COLOR_OVERLAY)
                        .title(R.string.settings_home_laf_other_color_overlay)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.NOTIFICATION_DOT_COLOR)
                        .title(R.string.settings_home_laf_notification_dot_color)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.NOTIFICATION_DOT_SIZE)
                        .title(R.string.settings_home_laf_notification_dot_size)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.LARGE_SEARCH_BAR)
                        .title(R.string.settings_home_laf_other_large_search_bar)
                        .summary(R.string.settings_home_laf_other_large_search_bar_summary)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.STATUS_BAR_COLOR)
                        .title(R.string.settings_home_laf_other_status_bar_color)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.FULLSCREEN_FOLDERS)
                        .title(R.string.settings_home_laf_other_fullscreen_folders)
                        .summary(R.string.settings_home_laf_other_fullscreen_folders_summary)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.HOME_PAGER_INDICATOR)
                        .title(R.string.settings_home_laf_other_home_pager_indicator)
                        .summary(R.string.settings_home_laf_other_home_pager_indicator_summary)
                        .category(R.string.settings_home_laf)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(LookAndFeelFragment.newInstance(requestKey));
                        })
                        .build(),

                /* --- Appearance --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_appearance)
                        .title(R.string.settings_home_laf_appearance)
                        .summary(R.string.settings_home_laf_appearance_summary)
                        .category(R.string.settings_home_laf_appearance)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(
                                    LookAndFeelFragment.newInstance(requestKey),
                                    AppearanceFragment.newInstance(requestKey)
                            );
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.ITEM_TEXT_SIZE)
                        .title(R.string.settings_home_laf_appearance__text_size)
                        .category(R.string.settings_home_laf_appearance)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(
                                    LookAndFeelFragment.newInstance(requestKey),
                                    AppearanceFragment.newInstance(requestKey)
                            );
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.ITEM_PADDING)
                        .title(R.string.settings_home_laf_appearance_padding)
                        .category(R.string.settings_home_laf_appearance)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(
                                    LookAndFeelFragment.newInstance(requestKey),
                                    AppearanceFragment.newInstance(requestKey)
                            );
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.ITEM_FONT)
                        .title(R.string.settings_home_laf_appearance_text_font)
                        .category(R.string.settings_home_laf_appearance)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(
                                    LookAndFeelFragment.newInstance(requestKey),
                                    AppearanceFragment.newInstance(requestKey)
                            );
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.ITEM_SHADOW_RADIUS)
                        .title(R.string.settings_home_laf_appearance_shadow_radius)
                        .category(R.string.settings_home_laf_appearance)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(
                                    LookAndFeelFragment.newInstance(requestKey),
                                    AppearanceFragment.newInstance(requestKey)
                            );
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.ITEM_SHADOW_COLOR)
                        .title(R.string.settings_home_laf_appearance_shadow_color)
                        .category(R.string.settings_home_laf_appearance)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(
                                    LookAndFeelFragment.newInstance(requestKey),
                                    AppearanceFragment.newInstance(requestKey)
                            );
                        })
                        .build(),

                /* --- Widgets --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_home_widgets)
                        .title(R.string.settings_home_widgets)
                        .category(R.string.settings_home_widgets)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new WidgetsSettingsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.ENABLE_WIDGETS)
                        .title(R.string.settings_home_widgets_enable)
                        .summary(R.string.settings_home_widgets_enable_summary)
                        .category(R.string.settings_home_widgets)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new WidgetsSettingsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.WIDGETS_POSITION)
                        .title(R.string.settings_home_widgets_position)
                        .category(R.string.settings_home_widgets)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new WidgetsSettingsFragment());
                        })
                        .build(),

                /* --- Miscellaneous --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_home_misc)
                        .title(R.string.settings_home_misc)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(MiscFragment.newInstance(requestKey), new HiddenItemsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_home_hidden_items)
                        .title(R.string.settings_home_hidden_items)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(MiscFragment.newInstance(requestKey), new HiddenItemsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SCREEN_ORIENTATION)
                        .title(R.string.settings_home_misc_orientation)
                        .category(R.string.settings_home_misc)
                        .addArraysSearchTokens(R.array.pref_desc_screen_orientation)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(MiscFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.APP_LONG_CLICK_ACTION)
                        .title(R.string.settings_home_misc_long_action)
                        .category(R.string.settings_home_misc)
                        .addArraysSearchTokens(R.array.pref_desc_app_long_click_actions)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(MiscFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SHOW_SCROLLBAR)
                        .title(R.string.settings_home_misc_scrollbar)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(MiscFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SCROLL_TO_TOP)
                        .title(R.string.settings_home_misc_scroll_top)
                        .summary(R.string.settings_home_misc_scroll_top_summary)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(MiscFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SMOOTH_SCROLL_TO_TOP)
                        .title(R.string.settings_home_misc_smooth_scroll_top)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(MiscFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.EXPAND_NOTIFICATIONS)
                        .title(R.string.settings_home_misc_expand_status_bar)
                        .summary(R.string.settings_home_misc_expand_status_bar_summary)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(MiscFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.DESTRUCTIVE_NON_EDIT)
                        .title(R.string.settings_home_misc_destructive_non_edit)
                        .summary(R.string.settings_home_misc_destructive_non_edit_summary)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(MiscFragment.newInstance(requestKey));
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_misc_experimental)
                        .title(R.string.settings_home_misc_experimental)
                        .summary(R.string.settings_home_misc_experimental_summary)
                        .category(R.string.settings_home_misc)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(new MiscFragment(), new ExperimentalSettingsFragment());
                        })
                        .build(),

                /* --- Experimental --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_misc_experimental)
                        .title(R.string.settings_home_misc_experimental)
                        .category(R.string.settings_home_misc_experimental)
                        .summary(R.string.settings_home_misc_experimental_summary)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(new MiscFragment(), new ExperimentalSettingsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.EXPERIMENTAL_INTENT_FACTORY)
                        .title(R.string.settings_home_misc_experimental_intent_factory)
                        .summary(R.string.settings_home_misc_experimental_intent_factory_summary)
                        .category(R.string.settings_home_misc_experimental)
                        .stackBuilder(requestKey -> {
                            return Arrays.asList(new MiscFragment(), new ExperimentalSettingsFragment());
                        })
                        .build(),

                /* --- Apps --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_apps_settings)
                        .title(R.string.settings_apps_list)
                        .category(R.string.settings_apps_list)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(AppsSettingsFragment.newInstance(requestKey));
                        })
                        .build(),

                /* --- Shortcuts --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_shortcuts)
                        .title(R.string.settings_home_misc_shortcuts)
                        .category(R.string.settings_home_misc_shortcuts)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new ShortcutsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SHOW_SHORTCUTS)
                        .title(R.string.settings_home_misc_shortcuts_show)
                        .category(R.string.settings_home_misc_shortcuts)
                        .addResourcesSearchTokens(
                                R.string.settings_home_misc_shortcuts_show_summary_off,
                                R.string.settings_home_misc_shortcuts_show_summary_on
                        )
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new ShortcutsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SHORTCUTS_SORT_MODE)
                        .title(R.string.settings_home_misc_shortcuts_sort_mode)
                        .category(R.string.settings_home_misc_shortcuts)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new ShortcutsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.MAX_DYNAMIC_SHORTCUTS)
                        .title(R.string.settings_home_misc_shortcuts_max_dynamic)
                        .category(R.string.settings_home_misc_shortcuts)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new ShortcutsFragment());
                        })
                        .build(),

                /* --- Notifications --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_notifications)
                        .title(R.string.settings_home_misc_notifications)
                        .category(R.string.settings_home_misc_notifications)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new NotificationsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.NOTIFICATION_DOT)
                        .title(R.string.settings_home_misc_notifications_dot)
                        .summary(R.string.settings_home_misc_notifications_dot_summary)
                        .category(R.string.settings_home_misc_notifications)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new NotificationsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.NOTIFICATION_DOT_ONGOING)
                        .title(R.string.settings_home_misc_notifications_dot_ongoing)
                        .category(R.string.settings_home_misc_notifications)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new NotificationsFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.NOTIFICATION_POPUP)
                        .title(R.string.settings_home_misc_notifications_popup_show)
                        .category(R.string.settings_home_misc_notifications)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new NotificationsFragment());
                        })
                        .build(),

                /* --- Search --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_search_settings)
                        .title(R.string.settings_search)
                        .category(R.string.settings_search)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SEARCH_SHOW_SOFT_KEYBOARD)
                        .title(R.string.settings_search_auto_show)
                        .category(R.string.settings_search)
                        .addResourcesSearchTokens(
                                R.string.settings_search_auto_show_summary_off,
                                R.string.settings_search_auto_show_summary_on
                        )
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SEARCH_SHOW_GLOBAL_SEARCH)
                        .title(R.string.settings_search_global)
                        .category(R.string.settings_search)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SEARCH_USE_CUSTOM_TABS)
                        .title(R.string.settings_search_custom_tabs)
                        .summary(R.string.settings_search_custom_tabs_summary)
                        .category(R.string.settings_search)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SEARCH_ENGINE)
                        .title(R.string.settings_search_engine)
                        .category(R.string.settings_search)
                        .addArraysSearchTokens(R.array.pref_desc_search_engines)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT)
                        .title(R.string.settings_search_engine_custom_format)
                        .category(R.string.settings_search)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SEARCH_SHOW_MOST_USED)
                        .title(R.string.settings_search_history_most_used)
                        .summary(R.string.settings_search_history_most_used_summary)
                        .category(R.string.settings_search_history)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_search_most_used_reset)
                        .title(R.string.settings_search_history_most_used_reset)
                        .category(R.string.settings_search_history)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(Preferences.SEARCH_TARGETS)
                        .title(R.string.settings_search_targets)
                        .summary(R.string.settings_search_targets_summary)
                        .category(R.string.settings_search)
                        .addArraysSearchTokens(R.array.pref_desc_search_targets)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new SearchFragment());
                        })
                        .build(),

                /* --- Backup --- */

                new SettingsEntryImpl.Builder(R.string.pref_key_backups)
                        .title(R.string.settings_other_bar)
                        .category(R.string.settings_other_bar)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new BackupFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_backup)
                        .title(R.string.settings_other_bar_backup)
                        .summary(R.string.settings_other_bar_backup_summary)
                        .category(R.string.settings_other_bar)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new BackupFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_restore)
                        .title(R.string.settings_other_bar_restore)
                        .summary(R.string.settings_other_bar_restore_summary)
                        .category(R.string.settings_other_bar)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new BackupFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_reset_apps)
                        .title(R.string.settings_other_bar_reset_apps)
                        .summary(R.string.settings_other_bar_reset_apps_summary)
                        .category(R.string.settings_other_bar)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new BackupFragment());
                        })
                        .build(),
                new SettingsEntryImpl.Builder(R.string.pref_key_reset_lnch)
                        .title(R.string.settings_other_bar_reset_lnch)
                        .summary(R.string.settings_other_bar_reset_lnch_summary)
                        .category(R.string.settings_other_bar)
                        .stackBuilder(requestKey -> {
                            return Collections.singletonList(new BackupFragment());
                        })
                        .build(),
        };
    }

    private SettingsEntries() {
    }
}
