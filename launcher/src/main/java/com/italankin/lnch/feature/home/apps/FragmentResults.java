package com.italankin.lnch.feature.home.apps;

public final class FragmentResults {

    public static final String RESULT = "result";

    public static final class Customize {
        public static final String KEY = "customize";

        public static final class Ignore {
            public static final String KEY = "customize_ignore";
            public static final String DESCRIPTOR = "descriptor";
        }

        public static final class Rename {
            public static final String KEY = "customize_rename";
            public static final String DESCRIPTOR = "descriptor";
        }

        public static final class SetColor {
            public static final String KEY = "customize_set_color";
            public static final String DESCRIPTOR = "descriptor";
        }

        public static final class Remove {
            public static final String KEY = "customize_remove";
            public static final String DESCRIPTOR = "descriptor";
        }

        public static final class CreateIntent {
            public static final String KEY = "customize_create_intent";
        }

        public static final class EditIntent {
            public static final String KEY = "customize_edit_intent";
            public static final String DESCRIPTOR_ID = "descriptor_id";
        }

        public static final class SelectFolder {
            public static final String KEY = "customize_select_folder";
            public static final String DESCRIPTOR = "descriptor";
        }

        public static final class AddFolder {
            public static final String KEY = "customize_add_folder";
        }

        public static final class RemoveFromFolder {
            public static final String KEY = "customize_remove_from_folder";
            public static final String DESCRIPTOR = "descriptor";
            public static final String FOLDER = "folder";
        }

        public static final class OpenFolder {
            public static final String KEY = "customize_open_folder";
            public static final String FOLDER = "folder";
        }
    }

    public static final class RemoveFromFolder {
        public static final String KEY = "remove_from_folder";
        public static final String DESCRIPTOR_ID = "descriptor_Id";
        public static final String FOLDER_ID = "folder_Id";
    }

    public static final class SelectFolder {
        public static final String KEY = "select_folder";
        public static final String DESCRIPTOR_ID = "descriptor_Id";
        public static final String FOLDER_ID = "folder_Id";
    }

    public static final class RemoveItem {
        public static final String KEY = "remove_item";
        public static final String DESCRIPTOR_ID = "descriptor_Id";
    }

    public static final class PinShortcut {
        public static final String KEY = "pin_shortcut";
        public static final String PACKAGE_NAME = "package_name";
        public static final String SHORTCUT_ID = "shortcut_id";
    }

    public static final class OnActionHandled {
        public static final String KEY = "on_action_handled";
    }

    public static final class Widgets {
        public static final class AppInfo {
            public static final String KEY = "widget_app_info";
            public static final String APP_WIDGET_ID = "app_widget_id";
        }

        public static final class RemoveWidget {
            public static final String KEY = "widget_remove";
            public static final String APP_WIDGET_ID = "app_widget_id";
        }
    }
}
