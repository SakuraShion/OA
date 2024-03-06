package com.example.emos.api.oss;

/**
 * Created By zf
 * 描述:  对使用腾讯云存储服务的业务场景进行区分
 */
public enum TypeEnum {
    ARCHIVE("archive"),
    PHOTO("photo");
    private String key;

    TypeEnum(String key) {
        this.key = key;
    }

    private String getKey() {
        return key;
    }

    public static TypeEnum findByKey(String key) {
        if (key != null) {
            for (TypeEnum type : TypeEnum.values()) {
                if (key.equals(type.getKey())) {
                    return type;
                }
            }
        }

        return null;
    }
}
