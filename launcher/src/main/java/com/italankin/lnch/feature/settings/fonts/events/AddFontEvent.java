package com.italankin.lnch.feature.settings.fonts.events;

public interface AddFontEvent {

    class FontExistsError implements AddFontEvent {
        public final String fontName;

        public FontExistsError(String fontName) {
            this.fontName = fontName;
        }
    }

    class FontEmptyNameError implements AddFontEvent {
    }

    class FontAdded implements AddFontEvent {
    }

    class InvalidFormatError implements AddFontEvent {
    }

    class FontAddError implements AddFontEvent {
        public final Throwable error;

        public FontAddError(Throwable error) {
            this.error = error;
        }
    }
}
