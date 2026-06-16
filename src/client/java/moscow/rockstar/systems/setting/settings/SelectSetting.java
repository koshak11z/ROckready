/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.render.penis.PenisPlayer;
import org.jetbrains.annotations.NotNull;

public class SelectSetting
extends AbstractSetting {
    private final List<Value> values = new ArrayList<Value>();
    private List<Value> selectedValues = new ArrayList<Value>();
    private boolean draggable;
    private int min;

    public SelectSetting(@NotNull SettingsContainer parent, String name, String description, @NotNull BooleanSupplier hideCondition) {
        super(parent, name, hideCondition);
    }

    public SelectSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
        super(parent, name, hideCondition);
    }

    public SelectSetting(@NotNull SettingsContainer parent, String name, String description) {
        super(parent, name);
    }

    public SelectSetting(@NotNull SettingsContainer parent, String name) {
        super(parent, name);
    }

    public SelectSetting draggable() {
        this.draggable = true;
        return this;
    }

    public SelectSetting min(int min) {
        this.min = Math.max(0, min);
        return this;
    }

    public void add(Value value) {
        this.values.add(value);
    }

    public void select(Value value) {
        if (!this.selectedValues.contains(value)) {
            this.selectedValues.add(value);
        }
    }

    @Override
    public JsonElement save() {
        JsonObject object = new JsonObject();
        JsonArray selectedArray = new JsonArray();
        for (Value selected : this.selectedValues) {
            selectedArray.add((JsonElement)new JsonPrimitive(selected.getName()));
        }
        object.add("selected", (JsonElement)selectedArray);
        JsonArray orderArray = new JsonArray();
        for (Value value : this.values) {
            orderArray.add((JsonElement)new JsonPrimitive(value.getName()));
        }
        object.add("order", (JsonElement)orderArray);
        return object;
    }

    @Override
    public void load(JsonElement element) {
        this.selectedValues.clear();
        if (element.isJsonObject()) {
            List<Value> orderedValues;
            JsonObject object = element.getAsJsonObject();
            if (object.has("order")) {
                JsonArray orderArray = object.getAsJsonArray("order");
                orderedValues = new ArrayList<>();
                for (JsonElement orderElement : orderArray) {
                    String name = orderElement.getAsString();
                    this.values.stream().filter(value -> value.getName().equalsIgnoreCase(name)).findFirst().ifPresent(orderedValues::add);
                }
                for (Value value2 : this.values) {
                    if (orderedValues.contains(value2)) continue;
                    orderedValues.add(value2);
                }
                this.values.clear();
                this.values.addAll(orderedValues);
            }
            if (object.has("selected")) {
                JsonArray selectedArray = object.getAsJsonArray("selected");
                for (JsonElement selectedElement : selectedArray) {
                    String name = selectedElement.getAsString();
                    this.values.stream().filter(value -> value.getName().equalsIgnoreCase(name)).findFirst().ifPresent(this.selectedValues::add);
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement el : array) {
                String name = el.getAsString();
                this.values.stream().filter(value -> value.getName().equalsIgnoreCase(name)).findFirst().ifPresent(this.selectedValues::add);
            }
        }
        for (Value value3 : this.values) {
            if (!value3.isAlwaysEnabled() || this.selectedValues.contains(value3)) continue;
            this.selectedValues.add(value3);
        }
        if (this.selectedValues.size() < this.min) {
            this.values.stream().filter(v -> !this.selectedValues.contains(v)).limit(this.min - this.selectedValues.size()).forEach(this.selectedValues::add);
        }
    }

    @Generated
    public List<Value> getValues() {
        return this.values;
    }

    @Generated
    public List<Value> getSelectedValues() {
        return this.selectedValues;
    }

    @Generated
    public boolean isDraggable() {
        return this.draggable;
    }

    @Generated
    public int getMin() {
        return this.min;
    }

    @Generated
    public void setSelectedValues(List<Value> selectedValues) {
        this.selectedValues = selectedValues;
    }

    @Generated
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    @Generated
    public void setMin(int min) {
        this.min = min;
    }

    public static class Value {
        private final SelectSetting parent;
        private final String name;
        private final String description;
        private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
        private final Animation activeAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
        private final Animation yAnim = new Animation(300L, Easing.BAKEK);
        private float yFactor;
        private boolean alwaysEnabled;
        private final BooleanSupplier hideCondition;
        private PenisPlayer enablePenis;
        private PenisPlayer disablePenis;
        private PenisPlayer currentPenis;
        private boolean lastState;

        public Value(SelectSetting parent, String name) {
            this(parent, name, "", () -> false);
        }

        public Value(SelectSetting parent, String name, String description) {
            this(parent, name, description, () -> false);
        }

        public Value(SelectSetting parent, String name, String description, BooleanSupplier hideCondition) {
            this.parent = parent;
            this.name = name;
            this.description = description;
            this.hideCondition = hideCondition;
            parent.add(this);
        }

        public boolean isHidden() {
            return this.hideCondition != null && this.hideCondition.getAsBoolean();
        }

        public Value select() {
            if (!this.parent.getSelectedValues().contains(this)) {
                this.parent.getSelectedValues().add(this);
            }
            return this;
        }

        public Value alwaysEnabled() {
            this.alwaysEnabled = true;
            this.parent.select(this);
            return this;
        }

        public Value toggle() {
            if (this.alwaysEnabled) {
                return this;
            }
            if (this.parent.getSelectedValues().contains(this)) {
                if (this.parent.getSelectedValues().size() > this.parent.getMin()) {
                    this.parent.getSelectedValues().remove(this);
                }
            } else {
                this.parent.getSelectedValues().add(this);
            }
            return this;
        }

        public boolean isSelected() {
            return this.parent.getSelectedValues().contains(this);
        }

        public String toString() {
            return this.name;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            Value that = (Value)obj;
            return Objects.equals(this.parent, that.parent) && Objects.equals(this.name, that.name) && Objects.equals(this.description, that.description);
        }

        public int hashCode() {
            return Objects.hash(this.parent, this.name, this.description);
        }

        @Generated
        public SelectSetting getParent() {
            return this.parent;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public String getDescription() {
            return this.description;
        }

        @Generated
        public Animation getHoverAnimation() {
            return this.hoverAnimation;
        }

        @Generated
        public Animation getActiveAnimation() {
            return this.activeAnimation;
        }

        @Generated
        public Animation getYAnim() {
            return this.yAnim;
        }

        @Generated
        public float getYFactor() {
            return this.yFactor;
        }

        @Generated
        public boolean isAlwaysEnabled() {
            return this.alwaysEnabled;
        }

        @Generated
        public BooleanSupplier getHideCondition() {
            return this.hideCondition;
        }

        @Generated
        public PenisPlayer getEnablePenis() {
            return this.enablePenis;
        }

        @Generated
        public PenisPlayer getDisablePenis() {
            return this.disablePenis;
        }

        @Generated
        public PenisPlayer getCurrentPenis() {
            return this.currentPenis;
        }

        @Generated
        public boolean isLastState() {
            return this.lastState;
        }

        @Generated
        public void setYFactor(float yFactor) {
            this.yFactor = yFactor;
        }

        @Generated
        public void setAlwaysEnabled(boolean alwaysEnabled) {
            this.alwaysEnabled = alwaysEnabled;
        }

        @Generated
        public void setEnablePenis(PenisPlayer enablePenis) {
            this.enablePenis = enablePenis;
        }

        @Generated
        public void setDisablePenis(PenisPlayer disablePenis) {
            this.disablePenis = disablePenis;
        }

        @Generated
        public void setCurrentPenis(PenisPlayer currentPenis) {
            this.currentPenis = currentPenis;
        }

        @Generated
        public void setLastState(boolean lastState) {
            this.lastState = lastState;
        }
    }
}
