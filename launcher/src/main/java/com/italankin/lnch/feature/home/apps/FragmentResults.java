package com.italankin.lnch.feature.home.apps;

public final class FragmentResults {

    public static final String RESULT = "result";

    public static final class Customize {
        public static final String KEY = "customize";
    }

    public static final class RemoveFromFolder {
        public static final String KEY = "remove_from_folder";
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
}
