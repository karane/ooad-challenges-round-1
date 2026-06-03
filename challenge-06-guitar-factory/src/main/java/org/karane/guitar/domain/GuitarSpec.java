package org.karane.guitar.domain;

import java.util.List;

public record GuitarSpec(
        GuitarType type,
        BodyStyle bodyStyle,
        WoodType bodyWood,
        WoodType neckWood,
        WoodType fretboardWood,
        List<GuitarSpec.PickupType> pickups,
        int stringCount,
        Finish finish
) {
    public enum PickupType {
        SINGLE_COIL("Single-Coil"),
        HUMBUCKER("Humbucker"),
        P90("P90");

        private final String label;
        PickupType(String label) { this.label = label; }
        public String label() { return label; }
    }

    public GuitarSpec {
        if (stringCount < 4 || stringCount > 12)
            throw new IllegalArgumentException("stringCount must be between 4 and 12");
        pickups = List.copyOf(pickups);
    }

    public static Builder builder(GuitarType type) { return new Builder(type); }

    public static final class Builder {
        private final GuitarType type;
        private BodyStyle bodyStyle;
        private WoodType bodyWood;
        private WoodType neckWood;
        private WoodType fretboardWood;
        private List<PickupType> pickups = List.of();
        private int stringCount          = 6;
        private Finish finish;

        private Builder(GuitarType type) { this.type = type; }

        public Builder bodyStyle(BodyStyle v)   { bodyStyle     = v; return this; }
        public Builder bodyWood(WoodType v)      { bodyWood      = v; return this; }
        public Builder neckWood(WoodType v)      { neckWood      = v; return this; }
        public Builder fretboardWood(WoodType v) { fretboardWood = v; return this; }
        public Builder pickups(PickupType... v)  { pickups       = List.of(v); return this; }
        public Builder stringCount(int v)        { stringCount   = v; return this; }
        public Builder finish(Finish v)          { finish        = v; return this; }

        public GuitarSpec build() {
            if (bodyStyle     == null) throw new IllegalStateException("bodyStyle is required");
            if (bodyWood      == null) throw new IllegalStateException("bodyWood is required");
            if (neckWood      == null) throw new IllegalStateException("neckWood is required");
            if (fretboardWood == null) throw new IllegalStateException("fretboardWood is required");
            if (finish        == null) throw new IllegalStateException("finish is required");
            return new GuitarSpec(type, bodyStyle, bodyWood, neckWood,
                    fretboardWood, pickups, stringCount, finish);
        }
    }
}
