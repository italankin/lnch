package com.italankin.lnch.feature.intentfactory;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public final class IntentFactoryResult implements Parcelable {

    @Nullable
    public final String descriptorId;
    public final Intent intent;

    public static final Creator<IntentFactoryResult> CREATOR = new Creator<IntentFactoryResult>() {
        @Override
        public IntentFactoryResult createFromParcel(Parcel in) {
            return new IntentFactoryResult(in);
        }

        @Override
        public IntentFactoryResult[] newArray(int size) {
            return new IntentFactoryResult[size];
        }
    };

    IntentFactoryResult(@Nullable String descriptorId, Intent intent) {
        this.descriptorId = descriptorId;
        this.intent = intent;
    }

    private IntentFactoryResult(Parcel in) {
        this.descriptorId = in.readString();
        this.intent = in.readParcelable(Intent.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(descriptorId);
        dest.writeParcelable(intent, flags);
    }
}
