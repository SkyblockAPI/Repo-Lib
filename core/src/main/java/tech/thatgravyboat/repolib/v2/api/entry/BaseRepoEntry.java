package tech.thatgravyboat.repolib.v2.api.entry;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.api.components.BaseRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.components.PatchedRepoComponentMap;
import tech.thatgravyboat.repolib.v2.api.components.RepoComponentMap;
import tech.thatgravyboat.repolib.v2.api.components.RepoDataComponentGetter;
import tech.thatgravyboat.repolib.v2.api.id.BaseSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.id.BaseSkyblockIdRepresentable;
import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.StructRepoCodec;
import tech.thatgravyboat.repolib.v2.internal.variants.VariantSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record BaseRepoEntry(
        BaseSkyBlockId baseId,
        Set<IdProperty> properties,
        RepoComponentMap base,
        VariantSelector<RepoEntry> variantSelector
) implements RepoDataComponentGetter, BaseSkyblockIdRepresentable {
    @ApiStatus.Internal
    public static RepoCodec<BaseRepoEntry> codec(SkyBlockIdType type) {
        return createCodecInternal(type);
    }

    private static RepoCodec<BaseRepoEntry> createCodecInternal(SkyBlockIdType type) {
        var components = type.internal().components();
        var baseComponentMap = RepoComponentMap.codec(components).fieldOf("base");
        var propertiesCodec = IdProperty.CODEC.setOf().fieldOf("properties");
        var idCodec = RepoCodec.STRING.fieldOf("globalId");

        return StructRepoCodec.create(
                object -> {
                    var propertiesMapper = RepoCodec.<String, IdProperty>idMapper();

                    var prototype = baseComponentMap.decode(object).orElseThrow();
                    var properties = propertiesCodec.decode(object).orElseThrow();
                    var id = idCodec.decode(object).orElseThrow();

                    for (var property : properties) {
                        propertiesMapper.put(property.name(), property);
                    }

                    var propertyCodec = propertiesMapper.dispatch(RepoCodec.STRING);

                    var propertyDefaults = RepoCodec.Map(propertyCodec, RepoCodec.STRING).fieldOf("defaults").decode(object).orElseThrow();

                    var variantSelector = new VariantSelector<RepoEntry>(propertyDefaults);
                    var baseEntry = new BaseRepoEntry(
                            new BaseSkyBlockId(type, id), properties, prototype, variantSelector
                    );

                    var variantCodec = RepoCodec.Map(IdProperty.stringCodec(properties), RepoComponentMap.codec(components)).fieldOf("variantSelector");
                    var variants = variantCodec.decode(object).orElseThrow();

                    for (var variant : variants.entrySet()) {
                        variantSelector.register(
                                variant.getKey(),
                                new RepoEntry(
                                        baseEntry,
                                        new GlobalSkyBlockId(type, id, variant.getKey()),
                                        new PatchedRepoComponentMap(prototype, variant.getValue()
                                        )
                                )
                        );
                    }

                    return CodecResult.success(baseEntry);
                },
                entry -> {
                    var object = new JsonObject();

                    var propertiesMapper = RepoCodec.<String, IdProperty>idMapper();

                    baseComponentMap.encode(object, entry.base).orElseThrow();
                    propertiesCodec.encode(object, entry.properties).orElseThrow();
                    idCodec.encode(object, entry.baseId.id()).orElseThrow();

                    var propertyCodec = propertiesMapper.dispatch(RepoCodec.STRING);

                   RepoCodec.Map(propertyCodec, RepoCodec.STRING).fieldOf("defaults").encode(object, entry.variantSelector.defaults).orElseThrow();

                    var variantCodec = RepoCodec.Map(IdProperty.stringCodec(entry.properties), RepoComponentMap.codec(components)).fieldOf("variantSelector");
                    var map = new HashMap<Map<IdProperty, String>, RepoComponentMap>();

                    for (var repoEntry : entry.variantSelector.collect()) {
                        map.put(repoEntry.globalId().properties(), repoEntry.map().patch());
                    }

                    variantCodec.encode(object, map).orElseThrow();

                    return CodecResult.success(object);
                }
        );
    }

    @Override
    public <Type> @Nullable Type getBase(BaseRepoDataComponent<Type> component) {
        return this.base().get(component);
    }
}
