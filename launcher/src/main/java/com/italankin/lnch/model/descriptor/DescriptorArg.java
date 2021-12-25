package com.italankin.lnch.model.descriptor;

import android.os.Parcel;
import android.os.Parcelable;

@SuppressWarnings("unchecked")
public final class DescriptorArg<T extends Descriptor> implements Parcelable {

    public static final Creator<DescriptorArg<?>> CREATOR = new Creator<DescriptorArg<?>>() {
        @Override
        public DescriptorArg<?> createFromParcel(Parcel in) {
            return new DescriptorArg<>(in);
        }

        @Override
        public DescriptorArg<?>[] newArray(int size) {
            return new DescriptorArg[size];
        }
    };

    public final String id;
    public final Class<T> type;

    public DescriptorArg(T descriptor) {
        this(descriptor.getId(), (Class<T>) descriptor.getClass());
    }

    public DescriptorArg(Descriptor descriptor, Class<T> type) {
        this(descriptor.getId(), type);
    }

    public DescriptorArg(String id, Class<T> type) {
        this.id = id;
        this.type = type;
    }

    public boolean is(Descriptor descriptor) {
        return descriptor.getId().equals(id) && type.isAssignableFrom(descriptor.getClass());
    }

    private DescriptorArg(Parcel in) {
        id = in.readString();
        type = (Class<T>) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeSerializable(type);
    }
}
