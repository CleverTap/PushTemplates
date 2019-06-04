package com.clevertap.pushtemplates;

enum TemplateType {

    BASIC("1"),
    AUTO_CAROUSEL("2"),
    RATING("3"),
    FIVE_ICONS("4");

    private final String templateType;

    TemplateType(String type) {
        this.templateType = type;
    }

    static TemplateType fromString(String type){
        switch (type){
            case "1" : return BASIC;
            case "2" : return AUTO_CAROUSEL;
            case "3" : return RATING;
            case "4" : return FIVE_ICONS;
            default: return null;
        }
    }


    @Override
    public String toString() {
        return templateType;
    }
}
