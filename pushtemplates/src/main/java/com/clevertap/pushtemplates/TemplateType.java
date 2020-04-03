package com.clevertap.pushtemplates;

enum TemplateType {

    BASIC("1"),
    AUTO_CAROUSEL("pt_carousel"),
    RATING("pt_rating"),
    FIVE_ICONS("4"),
    PRODUCT_DISPLAY("pt_product_display");

    private final String templateType;

    TemplateType(String type) {
        this.templateType = type;
    }

    static TemplateType fromString(String type){
        switch (type){
            case "1" : return BASIC; //ignore
            case "pt_carousel" : return AUTO_CAROUSEL;
            case "pt_rating" : return RATING;
            case "4" : return FIVE_ICONS; //ignore
            case "pt_product_display" : return PRODUCT_DISPLAY;
            default: return null;
        }
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return templateType;
    }
}
