package org.karane.guitar.inventory;

import org.karane.guitar.domain.*;

public record SearchCriteria(
        GuitarType type,
        BodyStyle bodyStyle,
        WoodType bodyWood,
        WoodType neckWood,
        WoodType fretboardWood,
        Integer stringCount
) {
    boolean matches(GuitarSpec spec) {
        if (type          != null && spec.type()          != type)          return false;
        if (bodyStyle     != null && spec.bodyStyle()     != bodyStyle)     return false;
        if (bodyWood      != null && spec.bodyWood()      != bodyWood)      return false;
        if (neckWood      != null && spec.neckWood()      != neckWood)      return false;
        if (fretboardWood != null && spec.fretboardWood() != fretboardWood) return false;
        if (stringCount   != null && spec.stringCount()   != stringCount)   return false;
        return true;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private GuitarType type;
        private BodyStyle bodyStyle;
        private WoodType bodyWood;
        private WoodType neckWood;
        private WoodType fretboardWood;
        private Integer stringCount;

        public Builder type(GuitarType v)       { type          = v; return this; }
        public Builder bodyStyle(BodyStyle v)    { bodyStyle     = v; return this; }
        public Builder bodyWood(WoodType v)      { bodyWood      = v; return this; }
        public Builder neckWood(WoodType v)      { neckWood      = v; return this; }
        public Builder fretboardWood(WoodType v) { fretboardWood = v; return this; }
        public Builder stringCount(int v)        { stringCount   = v; return this; }

        public SearchCriteria build() {
            return new SearchCriteria(type, bodyStyle, bodyWood, neckWood, fretboardWood, stringCount);
        }
    }
}
