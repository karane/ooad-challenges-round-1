package org.karane.tax.domain;

public sealed interface ProductCategory
        permits ProductCategory.Food,
                ProductCategory.Electronics,
                ProductCategory.Clothing,
                ProductCategory.Luxury,
                ProductCategory.Medicine {

    record Food()        implements ProductCategory {}
    record Electronics() implements ProductCategory {}
    record Clothing()    implements ProductCategory {}
    record Luxury()      implements ProductCategory {}
    record Medicine()    implements ProductCategory {}
}
