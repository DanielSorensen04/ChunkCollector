package me.dalot.enums;

public enum CollectionType {

    DROP, CROP, ORE;

    public String getAsString(){
        return switch (this) {
            case DROP -> "DROP";
            case CROP -> "CROP";
            case ORE -> "ORE";
        };
    }

}
