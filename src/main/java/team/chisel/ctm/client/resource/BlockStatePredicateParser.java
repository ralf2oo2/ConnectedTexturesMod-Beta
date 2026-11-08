package team.chisel.ctm.client.resource;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.block.context.BlockTagContext;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import net.modificationstation.stationapi.api.registry.RegistryEntryList;
import net.modificationstation.stationapi.api.state.property.Property;
import net.modificationstation.stationapi.api.tag.TagKey;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.JsonHelper;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BlockStatePredicateParser {
    private static final Type MAP_TYPE = new TypeToken<EnumMap<Direction, Predicate<BlockState>>>() { } .getType();
    private static final Type PREDICATE_TYPE = new TypeToken<Predicate<BlockState>>() { } .getType();

    public static final BlockStatePredicateParser INSTANCE = new BlockStatePredicateParser();

    private final PredicateDeserializer predicateDeserializer = new PredicateDeserializer();
    private final Gson GSON = new GsonBuilder().registerTypeAdapter(PREDICATE_TYPE, predicateDeserializer).registerTypeAdapter(ComparisonType.class, new ComparisonType.Deserializer()).registerTypeAdapter(MAP_TYPE, (InstanceCreator<?>) type -> new EnumMap<>(Direction.class)).registerTypeAdapter(PredicateMap.class, new MapDeserializer()).create();

    @Nullable
    public BiPredicate<Direction, BlockState> parse(JsonElement json) {
        return GSON.fromJson(json, PredicateMap.class);
    }

    private enum ComparisonType {
        EQUAL("=", i -> i == 0),
        NOT_EQUAL("!=", i -> i != 0),
        GREATER_THAN(">", i -> i > 0),
        LESS_THAN("<", i -> i < 0),
        GREATER_THAN_EQ(">=", i -> i >= 0),
        LESS_THAN_EQ("<=", i -> i <= 0);

        private final String key;
        private final IntPredicate compareFunc;

        ComparisonType(final String key, final IntPredicate compareFunc) {
            this.key = key;
            this.compareFunc = compareFunc;
        }

        private static class Deserializer implements JsonDeserializer<ComparisonType> {
            @Override
            public ComparisonType deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                    Optional<ComparisonType> type = Arrays.stream(ComparisonType.values()).filter(t -> t.key.equals(jsonElement.getAsString())).findFirst();
                    if (type.isPresent()) {
                        return type.get();
                    }
                    throw new JsonParseException(jsonElement + " is not a valid comparison type!");
                }
                throw new JsonSyntaxException("ComparisonType must be a String");
            }
        }
    }

    private enum Composition {
        AND(Predicate::and),
        OR(Predicate::or);

        private final BiFunction<Predicate<BlockState>, Predicate<BlockState>, Predicate<BlockState>> composer;

        Composition(final BiFunction<Predicate<BlockState>, Predicate<BlockState>, Predicate<BlockState>> composer) {
            this.composer = composer;
        }
    }

    private static final class PropertyPredicate<T extends Comparable<T>> implements Predicate<BlockState> {
        private final Predicate<BlockState> basePredicate;
        private final Object target;
        private final Property<T> property;
        private final T value;
        private final ComparisonType type;

        PropertyPredicate(final Predicate<BlockState> basePredicate, final Object target, final Property<T> property, final T value, final ComparisonType type) {
            this.basePredicate = basePredicate;
            this.target = target;
            this.property = property;
            this.value = value;
            this.type = type;
        }

        @Override
        public boolean test(BlockState state) {
            return basePredicate.test(state)
                           && state.getProperties().contains(property)
                           && type.compareFunc.test(state.get(property).compareTo(value));
        }

        public Object getTarget() {
            return target;
        }

        public Property<T> getProperty() {
            return property;
        }

        public T getValue() {
            return value;
        }

        public ComparisonType getType() {
            return type;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof BlockStatePredicateParser.PropertyPredicate)) return false;
            final BlockStatePredicateParser.PropertyPredicate<?> other = (BlockStatePredicateParser.PropertyPredicate<?>) o;
            final Object this$target = getTarget();
            final Object other$target = other.getTarget();
            if (this$target == null ? other$target != null : !this$target.equals(other$target)) return false;
            final Object this$prop = getProperty();
            final Object other$prop = other.getProperty();
            if (this$prop == null ? other$prop != null : !this$prop.equals(other$prop)) return false;
            final Object this$value = getValue();
            final Object other$value = other.getValue();
            if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
            final Object this$type = getType();
            final Object other$type = other.getType();
            if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $target = getTarget();
            result = result * PRIME + ($target == null ? 43 : $target.hashCode());
            final Object $prop = getProperty();
            result = result * PRIME + ($prop == null ? 43 : $prop.hashCode());
            final Object $value = getValue();
            result = result * PRIME + ($value == null ? 43 : $value.hashCode());
            final Object $type = getType();
            result = result * PRIME + ($type == null ? 43 : $type.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "BlockStatePredicateParser.PropertyPredicate(target=" + getTarget() + ", property=" + getProperty() + ", value=" + getValue() + ", type=" + getType() + ")";
        }
    }

    private static final class MultiPropertyPredicate<T extends Comparable<T>> implements Predicate<BlockState> {
        private final Predicate<BlockState> basePredicate;
        private final Object target;
        private final Property<T> property;
        private final Set<T> validValues;

        MultiPropertyPredicate(final Predicate<BlockState> basePredicate, final Object target, final Property<T> property, final Set<T> validValues) {
            this.basePredicate = basePredicate;
            this.target = target;
            this.property = property;
            this.validValues = validValues;
        }

        @Override
        public boolean test(BlockState state) {
            return basePredicate.test(state)
                           && state.getProperties().contains(property)
                           && validValues.contains(state.get(property));
        }

        public Object getTarget() {
            return target;
        }

        public Property<T> getProperty() {
            return property;
        }

        public Set<T> getValidValues() {
            return validValues;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof BlockStatePredicateParser.MultiPropertyPredicate)) return false;
            final BlockStatePredicateParser.MultiPropertyPredicate<?> other = (BlockStatePredicateParser.MultiPropertyPredicate<?>) o;
            final Object this$target = getTarget();
            final Object other$target = other.getTarget();
            if (this$target == null ? other$target != null : !this$target.equals(other$target)) return false;
            final Object this$prop = getProperty();
            final Object other$prop = other.getProperty();
            if (this$prop == null ? other$prop != null : !this$prop.equals(other$prop)) return false;
            final Object this$validValues = getValidValues();
            final Object other$validValues = other.getValidValues();
            if (this$validValues == null ? other$validValues != null : !this$validValues.equals(other$validValues)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $target = getTarget();
            result = result * PRIME + ($target == null ? 43 : $target.hashCode());
            final Object $prop = getProperty();
            result = result * PRIME + ($prop == null ? 43 : $prop.hashCode());
            final Object $validValues = getValidValues();
            result = result * PRIME + ($validValues == null ? 43 : $validValues.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "BlockStatePredicateParser.MultiPropertyPredicate(target=" + getTarget() + ", property=" + getProperty() + ", validValues=" + getValidValues() + ")";
        }
    }

    private static final class BlockPredicate implements Predicate<BlockState> {
        private final Block block;

        BlockPredicate(final Block block) {
            this.block = block;
        }

        @Override
        public boolean test(BlockState state) {
            return state.getBlock() == block;
        }

        public Block getBlock() {
            return block;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof BlockStatePredicateParser.BlockPredicate)) return false;
            final BlockStatePredicateParser.BlockPredicate other = (BlockStatePredicateParser.BlockPredicate) o;
            final Object this$block = getBlock();
            final Object other$block = other.getBlock();
            if (this$block == null ? other$block != null : !this$block.equals(other$block)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $block = getBlock();
            result = result * PRIME + ($block == null ? 43 : $block.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "BlockStatePredicateParser.BlockPredicate(block=" + getBlock() + ")";
        }
    }

    private static class PredicateComposition implements Predicate<BlockState> {
        private final Composition type;
        private final List<Predicate<BlockState>> composed;

        PredicateComposition(final Composition type, final List<Predicate<BlockState>> composed) {
            this.type = type;
            this.composed = composed;
        }

        @Override
        public boolean test(BlockState state) {
            if (type == Composition.AND) {
                for (Predicate<BlockState> predicate : composed) {
                    if (!predicate.test(state)) {
                        return false;
                    }
                }
                return true;
            } else {
                for (Predicate<BlockState> predicate : composed) {
                    if (predicate.test(state)) {
                        return true;
                    }
                }
                return false;
            }
        }

        @Override
        public String toString() {
            return "BlockStatePredicateParser.PredicateComposition(type=" + type + ", composed=" + composed + ")";
        }
    }

    private static class PredicateDeserializer implements JsonDeserializer<Predicate<BlockState>> {
        static final Predicate<BlockState> EMPTY = p -> false;
        // Unlikely that this will be threaded, but I think foamfix tries, so let's be safe
        // A global cache for the default predicate for use in creating deferring predicates
        ThreadLocal<Predicate<BlockState>> defaultPredicate = new ThreadLocal<>();

        @Override
        public Predicate<BlockState> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                boolean hasBlock = jsonObject.has("block");
                boolean hasTag = jsonObject.has("tag");

                if (hasBlock && hasTag) {
                    throw new JsonSyntaxException("A predicate cannot specify both 'block' and 'tag'!");
                }
                if (!hasBlock && !hasTag) {
                    throw new JsonSyntaxException("A predicate must specify either 'block' or 'tag'!");
                }

                Predicate<BlockState> basePredicate;
                Object target;

                if(hasBlock) {
                    Block block = BlockRegistry.INSTANCE.get(Identifier.of(JsonHelper.getString(jsonObject, "block")));
                    if (block == null) {
                        return EMPTY;
                    }
                    target = block;
                    basePredicate = state -> state.getBlock() == block;
                } else {
                    Identifier tagId = Identifier.of(JsonHelper.getString(jsonObject, "tag"));
                    TagKey<Block> tag = TagKey.of(BlockRegistry.KEY, tagId);
                    if (tag == null) {
                        return EMPTY;
                    }
                    target = tag;
                    basePredicate = state -> state.isIn(tag, BlockTagContext.DEFAULT);
                }
                Composition composition = null;
                if (jsonObject.has("defer")) {
                    if (defaultPredicate.get() == null) {
                        throw new JsonParseException("Cannot defer when no default is set!");
                    }
                    try {
                        composition = Composition.valueOf(JsonHelper.getString(jsonObject, "defer").toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        throw new JsonSyntaxException(JsonHelper.getString(jsonObject, "defer") + " is not a valid defer type.");
                    }
                }
                if (!jsonObject.has("predicate")) {
                    return compose(composition, basePredicate);
                }
                JsonElement propsElement = jsonObject.get("predicate");
                if (propsElement.isJsonObject()) {
                    return compose(composition, parsePredicate(basePredicate, target, propsElement.getAsJsonObject(), context));
                } else if (propsElement.isJsonArray()) {
                    List<Predicate<BlockState>> predicates = new ArrayList<>();
                    for (JsonElement ele : propsElement.getAsJsonArray()) {
                        if (ele.isJsonObject()) {
                            predicates.add(parsePredicate(basePredicate, target, ele.getAsJsonObject(), context));
                        } else {
                            throw new JsonSyntaxException("Predicate entry must be a JSON Object. Found: " + ele);
                        }
                    }
                    return compose(composition, new PredicateComposition(Composition.AND, predicates));
                }
            } else if (jsonElement.isJsonArray()) {
                List<Predicate<BlockState>> predicates = new ArrayList<>();
                for (JsonElement ele : jsonElement.getAsJsonArray()) {
                    Predicate<BlockState> p = context.deserialize(ele, PREDICATE_TYPE);
                    if (p != EMPTY) {
                        predicates.add(p);
                    }
                }
                return predicates.size() == 0 ? EMPTY : predicates.size() == 1 ? predicates.get(0) : new PredicateComposition(Composition.OR, predicates);
            }
            throw new JsonSyntaxException("Predicate deserialization expects an object or an array. Found: " + jsonElement);
        }

        private Predicate<BlockState> compose(@Nullable Composition composition, @NotNull Predicate<BlockState> child) {
            if (composition == null) {
                return child;
            }
            return composition.composer.apply(defaultPredicate.get(), child);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private Predicate<BlockState> parsePredicate(@NotNull Predicate<BlockState> basePredicate, Object target, JsonObject jsonObject, JsonDeserializationContext context) {
            ComparisonType compareFunc = JsonHelper.deserialize(jsonObject, "compare_func", ComparisonType.EQUAL, context, ComparisonType.class);
            jsonObject.remove("compare_func");
            final Set<Map.Entry<String, JsonElement>> entryset = jsonObject.entrySet();
            if (entryset.size() != 1) {
                throw new JsonSyntaxException("Predicate entry must define exactly one property->value pair. Found: " + entryset.size());
            }
            String key = entryset.iterator().next().getKey();


            Optional<Property<?>> property = Optional.empty();
            if (target instanceof Block) {
                property = ((Block) target).getStateManager().getProperties().stream().filter(p -> p.getName().equals(key)).findFirst();
            } else if (target instanceof TagKey) {
                RegistryEntryList.Named<Block> entryList = BlockRegistry.INSTANCE.getOrCreateEntryList((TagKey<Block>) target);
                for (var entry : entryList) {
                    Block sampleBlock = entry.value();
                    Optional<Property<?>> found = sampleBlock.getStateManager().getProperties().stream().filter(p -> p.getName().equals(key)).findFirst();
                    if (found.isPresent()) {
                        property = found;
                        break;
                    }
                }
            }

            if (property.isEmpty()) {
                throw new JsonParseException(key + " is not a valid property for blockstate " + target);
            }
            JsonElement valueElement = jsonObject.get(key);
            final Property<?> prop = property.get();
            if (valueElement.isJsonArray()) {
                return new MultiPropertyPredicate(basePredicate, target, prop, StreamSupport.stream(valueElement.getAsJsonArray().spliterator(), false).map(e -> parseValue(prop, e)).collect(Collectors.toSet()));
            } else {
                return new PropertyPredicate(basePredicate, target, prop, parseValue(prop, valueElement), compareFunc);
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private Comparable parseValue(Property property, JsonElement jsonElement) {
            String valueString = JsonHelper.asString(jsonElement, property.getName());
            Optional<Comparable> value = (Optional<Comparable>) property.getValues().stream().filter(v -> property.name((Comparable) v).equalsIgnoreCase(valueString)).findFirst();
            if (value.isEmpty()) {
                throw new JsonParseException(valueString + " is not a valid value for property " + property);
            }
            return value.get();
        }
    }

    private static class PredicateMap implements BiPredicate<Direction, BlockState> {
        private final EnumMap<Direction, Predicate<BlockState>> predicates = new EnumMap<>(Direction.class);

        @Override
        public boolean test(Direction dir, BlockState state) {
            return predicates.get(dir).test(state);
        }
    }

    private class MapDeserializer implements JsonDeserializer<PredicateMap> {
        @Override
        public PredicateMap deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("default")) {
                    predicateDeserializer.defaultPredicate.set(context.deserialize(jsonObject.get("default"), PREDICATE_TYPE));
                    jsonObject.remove("default");
                }
                PredicateMap map = new PredicateMap();
                map.predicates.putAll(context.deserialize(jsonObject, MAP_TYPE));
                for (Direction direction : Direction.values()) {
                    map.predicates.putIfAbsent(direction, Optional.ofNullable(predicateDeserializer.defaultPredicate.get()).orElse(PredicateDeserializer.EMPTY));
                }
                predicateDeserializer.defaultPredicate.set(null);
                return map;
            } else if (jsonElement.isJsonArray()) {
                Predicate<BlockState> predicate = context.deserialize(jsonElement, PREDICATE_TYPE);
                PredicateMap map = new PredicateMap();
                for (Direction direction : Direction.values()) {
                    map.predicates.put(direction, predicate);
                }
                return map;
            }
            throw new JsonSyntaxException("connectTo must be an object or an array. Found: " + jsonElement);
        }
    }
}
