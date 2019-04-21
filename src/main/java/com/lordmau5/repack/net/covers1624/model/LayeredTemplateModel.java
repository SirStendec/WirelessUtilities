package com.lordmau5.repack.net.covers1624.model;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.lordmau5.repack.codechicken.lib.bakedmodel.BakedPropertiesModel;
import com.lordmau5.repack.codechicken.lib.bakedmodel.CachedFormat;
import com.lordmau5.repack.codechicken.lib.bakedmodel.LayeredWrappedModel;
import com.lordmau5.repack.codechicken.lib.bakedmodel.Quad;
import com.lordmau5.repack.codechicken.lib.bakedmodel.SimpleBakedModel;
import com.lordmau5.repack.codechicken.lib.bakedmodel.SimpleMultiModel;
import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.IModelProperties;
import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.ModelProperties;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Locale.ROOT;

/**
 * Created by covers1624 on 19/01/19.
 */
public class LayeredTemplateModel implements IModel {

    private static final Logger logger = LogManager.getLogger("LayeredTemplateModel");
    private static final Joiner colonJoiner = Joiner.on(":");

    private static final Class<? extends IModel> c_FancyMissingModel;
    private static final Constructor<? extends IModel> ctr_FancyMissingModel;

    private static final Class<?> c_VanillaLoader;
    private static final Method m_getLoader;
    private static final Field f_instance;

    private static final Class<?> c_VanillaModelWrapper;
    private static final Field f_VMW_model;

    static {
        try {
            //noinspection unchecked
            c_FancyMissingModel = (Class<? extends IModel>) Class.forName("net.minecraftforge.client.model.FancyMissingModel");
            ctr_FancyMissingModel = c_FancyMissingModel.getDeclaredConstructor(IModel.class, String.class);

            c_VanillaLoader = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaLoader");
            m_getLoader = c_VanillaLoader.getDeclaredMethod("getLoader");
            f_instance = c_VanillaLoader.getDeclaredField("INSTANCE");

            c_VanillaModelWrapper = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
            f_VMW_model = c_VanillaModelWrapper.getDeclaredField("model");

            ctr_FancyMissingModel.setAccessible(true);
            m_getLoader.setAccessible(true);
            f_instance.setAccessible(true);
            f_VMW_model.setAccessible(true);

        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException("Unable to reflect FancyMissingModel", e);
        }
    }

    private final IModelProperties modelProperties;
    private final boolean isUVLock;
    private final ResourceLocation template;
    private final List<OffsetEntry> offsetList;
    private final List<TintEntry> tintList;
    private final List<TextureEntry> textures;

    private IModel resolvedTemplate = null;

    //Default
    public LayeredTemplateModel() {

        this(ModelProperties.DEFAULT_BLOCK, false, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    //Clone.
    private LayeredTemplateModel(IModelProperties modelProperties, boolean isUVLock, ResourceLocation template, List<OffsetEntry> offsetList, List<TintEntry> tintList, List<TextureEntry> textures) {

        this.modelProperties = modelProperties;
        this.isUVLock = isUVLock;
        this.template = template;
        this.offsetList = offsetList;
        this.tintList = tintList;
        this.textures = textures;
    }

    //Specific clone.
    private LayeredTemplateModel(LayeredTemplateModel other, IModelProperties properties) {

        this(properties, other.isUVLock, other.template, other.offsetList, other.tintList, other.textures);
    }

    private LayeredTemplateModel(LayeredTemplateModel other, boolean isUVLock) {

        this(other.modelProperties, isUVLock, other.template, other.offsetList, other.tintList, other.textures);
    }

    public LayeredTemplateModel(LayeredTemplateModel other, ResourceLocation template, List<OffsetEntry> offsetList, List<TintEntry> tintList) {

        this(other.modelProperties, other.isUVLock, template, offsetList, tintList, other.textures);
    }

    public LayeredTemplateModel(LayeredTemplateModel other, List<TextureEntry> textures) {

        this(other.modelProperties, other.isUVLock, other.template, other.offsetList, other.tintList, textures);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {

        return Collections.emptyList();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {

        List<ResourceLocation> textures = new ArrayList<>();
        this.textures.stream().filter(TextureEntry.IS_TEXTURE).map(e -> e.texture).forEach(textures::add);
        if ( template != null ) {
            textures.addAll(getTemplate().getTextures());
        }
        return textures;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> texFunc) {

        Optional<ResourceLocation> particle = Optional.empty();
        IModel template = getTemplate();
        if ( template instanceof ModelBlockWrapper ) {
            ImmutableMap.Builder<String, String> replaceTextures = ImmutableMap.builder();
            textures.stream()//
                    .filter(TextureEntry.IS_TEXTURE)//
                    .filter(e -> e.equals("layer0", BlockRenderLayer.SOLID))//
                    .forEach(e -> {
                        replaceTextures.put(colonJoiner.join("layer0", e.name), e.texture.toString());
                        replaceTextures.put(colonJoiner.join("layer0", "solid", e.name), e.texture.toString());
                    });
            ModelBlockWrapper wrapper = (ModelBlockWrapper) template.retexture(replaceTextures.build());
            String tryResolve = wrapper.model.textures.getOrDefault("particle", "particle");
            particle = ModelBlockWrapper.tryResolve(wrapper.model, tryResolve).map(ResourceLocation::new);
        }

        IModelProperties modelProps = ModelProperties.builder(modelProperties)//
                .withState(state)//
                .withParticleTexture(particle.orElse(TextureMap.LOCATION_MISSING_TEXTURE))//
                .build();
        //If the model has any properties for this variant, add a simple model for dynamic baking.
        if ( textures.stream().anyMatch(TextureEntry.IS_PROPERTY) ) {
            List<String> validProps = textures.stream().filter(TextureEntry.IS_PROPERTY).map(e -> e.property).collect(Collectors.toList());//Add all texture props.
            validProps.addAll(tintList.stream().map(e -> e.tintSourceProp).collect(Collectors.toList()));//Add all tint props.
            validProps.add("model.cache.ext");
            //Anon model class for dynamically baking.
            return new BakedPropertiesModel(modelProps) {

                //This is thrown away when models are reloaded.
                private Map<String, IBakedModel> bakedPropertyCache = new HashMap<>();
                //Errors are a hard lock on the model.
                private IBakedModel error = null;
                private final ItemOverrideList overrideList = new ItemOverrideList(Collections.emptyList()) {

                    private IBakedModel simpleModel;

                    @Override
                    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {

                        NBTTagCompound propertiesTag = stack.getSubCompound("model_properties");
                        if ( propertiesTag == null || propertiesTag.isEmpty() ) {
                            if ( simpleModel == null ) {
                                simpleModel = bakeImpl(modelProps, state, format, texFunc, Collections.emptyMap());
                            }
                            return simpleModel;
                        }
                        StringBuilder builder = new StringBuilder("item");
                        Map<String, String> properties = new HashMap<>();
                        for (String str : validProps) {
                            builder.append(str);
                            NBTBase tag = propertiesTag.getTag(str);
                            if ( tag instanceof NBTTagString ) {
                                String v = ((NBTTagString) tag).getString();
                                builder.append(v);
                                properties.put(str, v);
                            }
                        }

                        return bakedPropertyCache.computeIfAbsent(builder.toString(), e -> bakeImpl(modelProps, state, format, texFunc, properties));
                    }
                };

                @Override
                public List<BakedQuad> getQuads(@Nullable IBlockState stateIn, @Nullable EnumFacing side, long rand) {

                    IBakedModel retModel = error;//Short circuit everything if we have an error.
                    if ( retModel == null && !(stateIn instanceof IExtendedBlockState) ) {
                        //We arent an IExendedBlockstate, dev forgot stuff.
                        retModel = error = makeFancyMissingModel("Blockstate is not an IExtendedBlockstate").bake(state, format, texFunc);
                    }

                    IExtendedBlockState extendedState = (IExtendedBlockState) stateIn;
                    if ( retModel == null && !extendedState.getUnlistedProperties().containsKey(Properties.MODEL_PROPERTIES) ) {
                        //Dev also forgot stuff, we dont have MODEL_PROPERTIES property.
                        retModel = error = makeFancyMissingModel("Blockstate does not have MODEL_PROPERTIES UnlistedProperty.").bake(state, format, texFunc);
                    }

                    Map<String, String> modelProperties = extendedState.getValue(Properties.MODEL_PROPERTIES);

                    if ( retModel == null ) {
                        //Everything seems to be in order, Gen cache key and compute if absent.
                        String key = "block:" + validProps.stream()//
                                .filter(modelProperties::containsKey)//
                                .map(e -> e + "=" + modelProperties.get(e))//
                                .collect(Collectors.joining(","));
                        retModel = bakedPropertyCache.computeIfAbsent(key, e -> bakeImpl(modelProps, state, format, texFunc, modelProperties));
                    }
                    return retModel.getQuads(stateIn, side, rand);
                }

                @Override
                public ItemOverrideList getOverrides() {

                    return overrideList;
                }
            };
        } else {//Doesn't have any properties, just bake it normally.
            return bakeImpl(modelProps, state, format, texFunc, Collections.emptyMap());
        }
    }

    /**
     * Attempts to resolve any TextureEntry properties then bake the model.
     * This will always return a model, but may return a FancyMissingModel if an error occurs.
     *
     * @param modelProps The IModelProperties.
     * @param state      The IModelState.
     * @param format     The VertexFormat.
     * @param texFunc    The texture lookup function.
     * @param properties The Properties.
     * @return The baked model.
     */
    public IBakedModel bakeImpl(IModelProperties modelProps, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> texFunc, Map<String, String> properties) {
        //Resolve our textures.
        List<TextureEntry> resolvedTextures = textures.stream()//
                .map(e -> e.resolve(properties))//
                .collect(Collectors.toList());
        //Collect and sort our layerIndexes.
        List<String> sortedLayerIndexes = resolvedTextures.stream()//
                .filter(TextureEntry.IS_TEXTURE)//
                .map(e -> e.layerIndex)//
                .distinct()//
                .sorted(Comparator.comparingInt(e -> Integer.parseUnsignedInt(e.replace("layer", ""))))//
                .collect(Collectors.toList());
        //Collect all used BlockRenderLayers
        List<BlockRenderLayer> validRenderLayers = resolvedTextures.stream()//
                .filter(TextureEntry.IS_TEXTURE)//
                .map(e -> e.renderLayer)//
                .distinct()//
                .collect(Collectors.toList());
        //Master final model map.
        Map<BlockRenderLayer, IBakedModel> renderLayerModels = new EnumMap<>(BlockRenderLayer.class);
        for (BlockRenderLayer renderLayer : validRenderLayers) {
            List<IBakedModel> layerModels = new ArrayList<>();
            for (String layerIndex : sortedLayerIndexes) {
                //Build the textures map for the template.
                ImmutableMap.Builder<String, String> textures = ImmutableMap.builder();
                resolvedTextures.stream()//
                        .filter(TextureEntry.IS_TEXTURE)//
                        .filter(e -> e.equals(layerIndex, renderLayer))//
                        .forEach(e -> {
                            textures.put(colonJoiner.join(layerIndex, e.name), e.texture.toString());
                            textures.put(colonJoiner.join(layerIndex, renderLayer.name().toLowerCase(ROOT), e.name), e.texture.toString());
                        });
                //Build the custom data for the template.
                ImmutableMap.Builder<String, String> customData = ImmutableMap.builder();
                //Give it the layer index.
                customData.put("layerIndex", layerIndex);
                //Give it the first offset that matches for this layerIndex + renderLayer.
                offsetList.stream()//
                        .filter(e -> e.equals(layerIndex, renderLayer))//
                        .findFirst()//
                        .ifPresent(e -> customData.put("offset", Float.toString(e.offset)));
                //Map unresolved texture properties to their tint counterpart and pass the values to the template.
                this.textures.stream()//
                        .filter(TextureEntry.IS_PROPERTY)//
                        .filter(e -> e.equals(layerIndex, renderLayer))//
                        .forEach(e -> {//
                            Optional<String> tintOpt = tintList.stream()//
                                    .filter(e2 -> e2.tintTargetProp.equals(e.property))//
                                    .map(e2 -> e2.tintSourceProp)//
                                    .findFirst();
                            tintOpt.ifPresent(prop -> {
                                customData.put("tint:#" + colonJoiner.join(layerIndex, e.name), properties.get(prop));
                                customData.put("tint:#" + colonJoiner.join(layerIndex, renderLayer.name().toLowerCase(ROOT), e.name), properties.get(prop));
                            });
                        });

                //Setup and bake the model.
                IModel model = getTemplate()//
                        .smoothLighting(modelProps.isAO())//
                        .gui3d(modelProps.isGui3D())//
                        .uvlock(isUVLock)//
                        .process(customData.build())//
                        .retexture(textures.build());
                layerModels.add(model.bake(state, format, texFunc));
            }
            renderLayerModels.put(renderLayer, new SimpleMultiModel(modelProps, layerModels));
        }
        return new LayeredWrappedModel(modelProps, renderLayerModels);
    }

    @Override
    public IModel smoothLighting(boolean value) {

        if ( modelProperties.isAO() == value ) {
            return this;
        }
        ModelProperties.Builder props = ModelProperties.builder(modelProperties)//
                .withAO(value);
        return new LayeredTemplateModel(this, props.build());
    }

    @Override
    public IModel gui3d(boolean value) {

        if ( modelProperties.isGui3D() == value ) {
            return this;
        }
        ModelProperties.Builder props = ModelProperties.builder(modelProperties)//
                .withGui3D(value);
        return new LayeredTemplateModel(this, props.build());
    }

    @Override
    public IModel uvlock(boolean value) {

        if ( isUVLock == value ) {
            return this;
        }
        return new LayeredTemplateModel(this, value);
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {

        List<TextureEntry> newTex = new ArrayList<>(this.textures);
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            if ( entry.getValue().isEmpty() ) {
                newTex.removeIf(e -> e.key.equals(entry.getKey()));
            } else {
                newTex.add(TextureEntry.parse(entry.getKey(), entry.getValue()));
            }
        }
        return new LayeredTemplateModel(this, newTex);
    }

    @Override
    public IModel process(ImmutableMap<String, String> _data) {

        Map<String, String> customData = new HashMap<>();
        //Thanks forge, Passes a JsonPrimitive to us using JsonElement.toString().
        //Meaning it gets converted back to valid json, hence has quotes around it.
        //We have to copy and replace all quotes with nothing, This also assumes that
        ///We will only ever be passed Strings, in the future we may need to change this up..
        _data.forEach((k, v) -> customData.put(k, v.replace("\"", "")));
        String template = customData.get("template");
        if ( template == null ) {
            throw new RuntimeException("Null template.");
        }
        List<OffsetEntry> offsetList = new ArrayList<>(this.offsetList);
        List<TintEntry> tintList = new ArrayList<>(this.tintList);
        customData.forEach((key, value) -> {
            if ( key.endsWith("offset") ) {
                String offsetKey = key.replace("offset", "");
                if ( offsetKey.endsWith(":") ) {
                    offsetKey = offsetKey.substring(0, offsetKey.length() - 1);
                }
                String[] segs = offsetKey.split(":");
                if ( segs.length > 2 ) {
                    throw new RuntimeException("Invalid offset key. " + offsetKey);
                }
                String layerIndex = segs[0];
                BlockRenderLayer renderLayer = segs.length > 1 ? parseLayer(segs[1]) : null;
                //Always remove.
                offsetList.removeIf(e -> e.equals(layerIndex, renderLayer));
                if ( !value.isEmpty() ) {
                    //Add new offset if the value is not empty.
                    offsetList.add(new OffsetEntry(layerIndex, renderLayer, Float.parseFloat(value)));
                }
            }
            if ( key.startsWith("tint:") ) {
                String targetProp = key.replace("tint:", "");
                //Same comments as offset parsing.
                tintList.removeIf(e -> e.tintTargetProp.equals(targetProp));
                if ( !value.isEmpty() ) {
                    tintList.add(new TintEntry(targetProp, value));
                }
            }
        });
        return new LayeredTemplateModel(this, new ResourceLocation(template), offsetList, tintList);
    }

    private IModel getTemplate() {

        if ( resolvedTemplate == null ) {
            resolvedTemplate = ModelBlockWrapper.load(template);
        }
        return resolvedTemplate;
    }

    /**
     * Parses a BlockRenderLayer from a string.
     * Input must be the enum name but lowercase.
     *
     * @param name The name.
     * @return The layer.
     * TODO, Utils class?
     */
    private static BlockRenderLayer parseLayer(String name) {

        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            if ( layer.name().toLowerCase(ROOT).equals(name) ) {
                return layer;
            }
        }
        throw new IllegalArgumentException("Unknown render layer '" + name + "'.");
    }

    /**
     * Makes a FancyMissingModel, uses reflection to access the constructor of FancyMissingModel.
     *
     * @param message The message to display.
     * @return The model.
     */
    private static IModel makeFancyMissingModel(String message) {

        try {
            return ctr_FancyMissingModel.newInstance(ModelLoaderRegistry.getMissingModel(), message);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create FancyMissingModel.", e);
        }
    }

    /**
     * Simple helper to invoke methods without exceptions.
     *
     * @param method   The method.
     * @param instance The instance to call it on.
     * @param args     The arguments.
     * @return If the method returns anything.
     * TODO, Helpers? or CCL reflection?
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Method method, Object instance, Object... args) {

        try {
            return (T) method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simple helper to get a field without exceptions.
     *
     * @param field    The field.
     * @param instance The instance to get the field from.
     * @return The value in the field.
     * TODO, Helpers? or CCL reflection?
     */
    @SuppressWarnings("unchecked")
    private static <T> T getField(Field field, Object instance) {

        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The ICustomModelLoader for LayeredTemplateModel.
     */
    public static class Loader implements ICustomModelLoader {

        public static final Loader INSTANCE = new Loader();

        private Loader() {

        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {

            ModelBlockWrapper.clearCache();
        }

        @Override
        public boolean accepts(ResourceLocation modelLocation) {

            String path = modelLocation.getPath();
            return modelLocation.getNamespace().equals(WirelessUtils.MODID) && (path.equals("layered_template") || path.equals("models/block/layered_template"));
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {

            return new LayeredTemplateModel();
        }
    }

    /**
     * A Wrapper around a ModelBlock.
     * Heavily based off {@link ModelLoader.VanillaModelWrapper},
     * but supports our tinting, offsetting, and removal of quads that
     * did not have a texture resolved, forge would instead resolve to missing sprite.
     */
    public static class ModelBlockWrapper implements IModel {

        public static final Map<ResourceLocation, IModel> cache = new HashMap<>();
        private static final ThreadLocal<Quad> transformerQuads = ThreadLocal.withInitial(Quad::new);

        private static ModelLoader loader;

        private final ModelBlock model;
        private final boolean uvLock;
        private final OptionalDouble offset;
        private final Object2IntMap<String> tints;
        private final int layerIndex;

        public ModelBlockWrapper(ModelBlock model, boolean uvLock, OptionalDouble offset, Object2IntMap<String> tints, int layerIndex) {

            this.model = model;
            this.uvLock = uvLock;
            this.offset = offset;
            this.tints = tints;
            this.layerIndex = layerIndex;
        }

        /**
         * Bakes the _basically_ the same way Forge does for VanillaModelWrapper.
         */
        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> texFunc) {
            //Build the composite Item Transform.
            Map<ItemCameraTransforms.TransformType, TRSRTransformation> tMap = new EnumMap<>(ItemCameraTransforms.TransformType.class);
            tMap.putAll(PerspectiveMapWrapper.getTransforms(model.getAllTransforms()));
            tMap.putAll(PerspectiveMapWrapper.getTransforms(state));
            IModelState newState = new SimpleModelState(ImmutableMap.copyOf(tMap));

            List<BakedQuad> generalQuads = new ArrayList<>();
            Map<EnumFacing, List<BakedQuad>> faceQuads = new HashMap<>();

            //Get the default transformation for the provided state.
            //This will usually be a ModelRotation.
            TRSRTransformation baseState = state.apply(Optional.empty()).orElse(TRSRTransformation.identity());
            FaceBakery bakery = getLoader().faceBakery;
            for (BlockPart part : model.getElements()) {
                //Try and resolve the texture for the partFace
                part.mapFaces.forEach((face, partFace) -> tryResolve(model, partFace.texture)//
                        .map(ResourceLocation::new)//
                        .map(texFunc)//
                        .ifPresent(tex -> {
                            //Yayyy, tell the bakery to make a quad!
                            BakedQuad quad = bakery.makeBakedQuad(part.positionFrom, part.positionTo, partFace, tex, face, baseState, part.partRotation, uvLock, part.shade);

                            //Use parts of CCL's BakedPipeline to transform the quads.
                            CachedFormat fmt = CachedFormat.lookup(quad.getFormat());
                            Quad transformerQuad = transformerQuads.get();//This is a ThreadLocal since Quad is a shared Mutable state. Someone may thread baking(\o/).
                            transformerQuad.reset(fmt);//Reset quad to the given format.
                            quad.pipe(transformerQuad);//Pipe the BakedQuad into the transformerQuad.
                            if ( offset.isPresent() ) {//If we have an offset, apply it.
                                for (Quad.Vertex vertex : transformerQuad.vertices) {
                                    for (int i = 0; i < 3; i++) {
                                        vertex.vec[i] += vertex.normal[i] * offset.getAsDouble();
                                    }
                                }
                            }//If we have a tint, apply it.
                            if ( tints.containsKey(partFace.texture) ) {
                                int tint = tints.getInt(partFace.texture);
                                //TODO, Complain if there is no color?
                                if ( fmt.hasColor ) {
                                    float r = (tint >> 0x10 & 0xFF) / 255F;
                                    float g = (tint >> 0x08 & 0xFF) / 255F;
                                    float b = (tint & 0xFF) / 255F;
                                    for (Quad.Vertex vertex : transformerQuad.vertices) {
                                        vertex.color[0] *= r;
                                        vertex.color[1] *= g;
                                        vertex.color[2] *= b;
                                    }
                                }
                            }
                            //Set the tintIndex.
                            transformerQuad.tintIndex = layerIndex;
                            //Bake the quad back to an UnpackedBakedQuad, to _mildly_ speed up the lighter.
                            quad = transformerQuad.bakeUnpacked();

                            //Decide where to put the quad.
                            if ( partFace.cullFace == null || !TRSRTransformation.isInteger(baseState.getMatrix()) ) {
                                generalQuads.add(quad);
                            } else {
                                faceQuads.computeIfAbsent(baseState.rotate(partFace.cullFace), e -> new ArrayList<>()).add(quad);
                            }
                        }));
            }
            //Build the item model state.
            IModelProperties modelProps = ModelProperties.builder()//
                    .withAO(model.isAmbientOcclusion())//
                    .withGui3D(model.isGui3d())//
                    .withState(newState)//
                    .build();

            return new SimpleBakedModel(modelProps, faceQuads, generalQuads);
        }

        @Override
        public IModel process(ImmutableMap<String, String> customData) {

            String offsetVal = customData.get("offset");
            String layerVal = customData.get("layerIndex");
            OptionalInt layerIndex = layerVal == null ? OptionalInt.empty() : OptionalInt.of(Integer.parseUnsignedInt(layerVal.replace("layer", "")));
            OptionalDouble offset = offsetVal == null ? OptionalDouble.empty() : OptionalDouble.of(Float.parseFloat(offsetVal));
            Object2IntMap<String> tints = new Object2IntArrayMap<>(this.tints);
            customData.forEach((k, v) -> {
                if ( k.startsWith("tint:") ) {
                    String tintKey = k.replace("tint:", "");
                    tints.put(tintKey, Integer.parseInt(v));
                }
            });
            return new ModelBlockWrapper(model, uvLock, offset, tints, layerIndex.orElse(-1));
        }

        @Override//Clone of forge's impl.
        public IModel retexture(ImmutableMap<String, String> textures) {

            if ( textures.isEmpty() ) {
                return this;
            }
            List<BlockPart> elements = new ArrayList<>();
            model.getElements().forEach(e -> elements.add(new BlockPart(e.positionFrom, e.positionTo, new HashMap<>(e.mapFaces), e.partRotation, e.shade)));
            ModelBlock newModel = new ModelBlock(model.getParentLocation(), elements, new HashMap<>(model.textures), model.isAmbientOcclusion(), model.isGui3d(), model.getAllTransforms(), new ArrayList<>(model.getOverrides()));
            newModel.name = model.name;
            newModel.parent = model.parent;

            Set<String> removed = new HashSet<>();
            textures.forEach((k, v) -> {
                if ( v.equals("") ) {
                    removed.add(k);
                    newModel.textures.remove(k);
                } else {
                    newModel.textures.put(k, v);
                }
            });
            Map<String, String> remapped = new HashMap<>();
            textures.forEach((k, v) -> {
                if ( v.startsWith("#") ) {
                    String k2 = v.substring(1);
                    if ( newModel.textures.containsKey(k2) ) {
                        remapped.put(k, newModel.textures.get(k2));
                    }
                }
            });
            newModel.textures.putAll(remapped);
            newModel.getElements().forEach(e -> e.mapFaces.values().removeIf(v -> removed.contains(v.texture)));
            return new ModelBlockWrapper(newModel, uvLock, offset, tints, layerIndex);
        }

        @Override
        public IModel smoothLighting(boolean value) {

            if ( model.isAmbientOcclusion() == value ) {
                return this;
            }
            ModelBlock newModel = new ModelBlock(model.getParentLocation(), model.getElements(), new HashMap<>(model.textures), value, model.isGui3d(), model.getAllTransforms(), new ArrayList<>(model.getOverrides()));
            newModel.name = model.name;
            newModel.parent = model.parent;
            return new ModelBlockWrapper(newModel, uvLock, offset, tints, layerIndex);
        }

        @Override
        public IModel gui3d(boolean value) {

            if ( model.isGui3d() == value ) {
                return this;
            }
            ModelBlock newModel = new ModelBlock(model.getParentLocation(), model.getElements(), new HashMap<>(model.textures), model.isAmbientOcclusion(), value, model.getAllTransforms(), new ArrayList<>(model.getOverrides()));
            newModel.name = model.name;
            newModel.parent = model.parent;
            return new ModelBlockWrapper(newModel, uvLock, offset, tints, layerIndex);
        }

        @Override
        public IModel uvlock(boolean value) {

            if ( uvLock == value ) {
                return this;
            }
            return new ModelBlockWrapper(model, value, offset, tints, layerIndex);
        }

        /**
         * Loads a ModelBlockWrapper, may return MissingModel.
         * Unlike forge, we resolve the parent hierarchy immediately.
         *
         * @param model The ResourceLocation for the model, will have 'model/' pre pended to the path.
         * @return The IModel.
         */
        public static IModel load(ResourceLocation model) {

            return cache.computeIfAbsent(model, e -> {
                try {
                    ModelBlock loadedModel = loadModel(ModelLoaderRegistry.getActualLocation(model));
                    ModelBlockWrapper wrapper = new ModelBlockWrapper(loadedModel, false, OptionalDouble.empty(), new Object2IntArrayMap<>(), -1);
                    if ( loadedModel.getParentLocation() != null ) {
                        if ( loadedModel.getParentLocation().getPath().equals("builtin/generated") ) {
                            loadedModel.parent = ModelLoader.MODEL_GENERATED;
                        } else {
                            IModel parent = load(loadedModel.getParentLocation());
                            if ( parent instanceof ModelBlockWrapper ) {
                                loadedModel.parent = ((ModelBlockWrapper) parent).model;
                            } else if ( c_VanillaModelWrapper.isAssignableFrom(parent.getClass()) ) {
                                loadedModel.parent = getField(f_VMW_model, parent);
                            } else {
                                logger.error("Non vanilla parent model for '{}'. Parent: '{}'.", model, loadedModel.getParentLocation());
                            }
                        }
                    }
                    return wrapper;
                } catch (IOException ex) {
                    //TODO, Hook into Forge's Model error map?
                    logger.error("Failed to load model '{}'.", model, ex);
                    return ModelLoaderRegistry.getMissingModel();
                }
            });
        }

        private static Optional<String> tryResolve(ModelBlock model, String name) {

            if ( name.charAt(0) != '#' ) {
                name = '#' + name;
            }
            return tryResolve(model, name, new ModelBlock.Bookkeep(model));
        }

        //Re implementation of ModelBlock.resolveTextureName, but allows us to distinguish between explicit missing texture
        //and unresolved.
        private static Optional<String> tryResolve(ModelBlock model, String name, ModelBlock.Bookkeep bookkeep) {

            if ( name.charAt(0) == '#' ) {
                if ( model == bookkeep.modelExt ) {
                    logger.warn("Unable to resolve texture due to upward reference: {} in {}", name, model.name);
                    return Optional.empty();
                }
                Optional<String> s = Optional.ofNullable(model.textures.get(name.substring(1)));
                if ( !s.isPresent() && model.hasParent() ) {
                    s = tryResolve(model.parent, name, bookkeep);
                }
                bookkeep.modelExt = model;
                if ( s.isPresent() && s.get().charAt(0) == '#' ) {
                    s = tryResolve(bookkeep.model, s.get(), bookkeep);
                }
                if ( s.isPresent() && s.get().charAt(0) == '#' ) {
                    return Optional.empty();
                }
                return s;
            } else {
                return Optional.of(name);
            }
        }

        /**
         * Used ModeLoader to resolve the ModelBLock.
         *
         * @param model The model to resolve.
         * @return The ModelBlock
         * @throws IOException idk, shit fucked.
         */
        private static ModelBlock loadModel(ResourceLocation model) throws IOException {

            return getLoader().loadModel(model);
        }

        /**
         * Whilst we can retrieve instances of ModelLoader from the ModelBakeEvent
         * that is fired _after_ model loading, so we pull it from
         * ModelLoader$VanillaLoader.INSTANCE via reflection and cache it.
         * Whilst this can _technically_ return null, it never will unless someone
         * severely fucks with model loading and calls us before Forge has initialized.
         *
         * @return The ModelLoader.
         */
        public static ModelLoader getLoader() {

            if ( loader == null ) {
                loader = invokeMethod(m_getLoader, getField(f_instance, null));
            }
            return loader;
        }

        /**
         * Clear our ModelBlock loading cache and ModelLoader instance.
         */
        public static void clearCache() {

            cache.clear();
            loader = null;
        }
    }

    /**
     * Simple tuple for holding tint target -> source mapping.
     */
    public static class TintEntry {

        @Nonnull
        public final String tintTargetProp;
        @Nonnull
        public final String tintSourceProp;

        public TintEntry(@Nonnull String tintTargetProp, @Nonnull String tintSourceProp) {

            this.tintTargetProp = tintTargetProp;
            this.tintSourceProp = tintSourceProp;
        }

        @Override
        public boolean equals(@Nullable Object obj) {

            if ( super.equals(obj) ) {
                return true;
            }
            if ( !(obj instanceof TintEntry) ) {
                return false;
            }
            TintEntry other = (TintEntry) obj;
            return other.tintTargetProp.equals(tintTargetProp) && other.tintSourceProp.equals(tintSourceProp);
        }

        @Override
        public int hashCode() {

            int result = 1;
            result = 31 * result + tintTargetProp.hashCode();
            result = 31 * result + tintSourceProp.hashCode();
            return result;
        }
    }

    /**
     * Simple triple for holding a offset entry.
     */
    public static class OffsetEntry {

        @Nonnull
        public final String layerIndex;
        @Nullable
        public final BlockRenderLayer renderLayer;
        public final float offset;

        public OffsetEntry(@Nonnull String layerIndex, @Nullable BlockRenderLayer renderLayer, float offset) {

            this.layerIndex = layerIndex;
            this.renderLayer = renderLayer;
            this.offset = offset;
        }

        /**
         * Checks if this OffsetEntry can be applied to the provided layerIndex and renderLayer.
         *
         * @param layerIndex  The layerIndex.
         * @param renderLayer The renderLayer
         * @return If this OffsetEntry is applicable.
         */
        public boolean equals(@Nonnull String layerIndex, @Nullable BlockRenderLayer renderLayer) {

            return this.layerIndex.equals(layerIndex) && (Objects.equals(this.renderLayer, renderLayer) || this.renderLayer == null);
        }

        @Override
        public boolean equals(@Nullable Object obj) {

            if ( super.equals(obj) ) {
                return true;
            }
            if ( !(obj instanceof OffsetEntry) ) {
                return false;
            }
            OffsetEntry other = (OffsetEntry) obj;
            return other.layerIndex.equals(layerIndex)//
                    && Objects.equals(other.renderLayer, renderLayer)//
                    && other.offset == offset;
        }

        @Override
        public int hashCode() {

            int result = 1;
            result = 31 * result + layerIndex.hashCode();
            result = 31 * result + (renderLayer != null ? renderLayer.hashCode() : 0);
            result = 31 * result + Float.floatToIntBits(offset);
            return result;
        }
    }

    /**
     * A resolved or property Texture.
     * In the case of a property, will be dynamically evaluated and baked.
     */
    private static class TextureEntry {

        public static final Predicate<TextureEntry> IS_PROPERTY = e -> e.type == EntryType.PROPERTY;
        public static final Predicate<TextureEntry> IS_TEXTURE = e -> e.type == EntryType.TEXTURE;

        @Nonnull
        public final String key;
        @Nonnull
        public final String name;
        @Nonnull
        public final String layerIndex;
        @Nonnull
        public final BlockRenderLayer renderLayer;
        @Nullable
        public final ResourceLocation texture;
        @Nullable
        public final String property;

        @Nonnull
        public final EntryType type;

        public TextureEntry(@Nonnull String key, @Nonnull String name, @Nonnull String layerIndex, @Nonnull BlockRenderLayer renderLayer, @Nullable ResourceLocation texture, @Nullable String property, @Nonnull EntryType type) {

            this.key = key;
            this.name = name;
            this.layerIndex = layerIndex;
            this.renderLayer = renderLayer;
            this.texture = texture;
            this.property = property;
            this.type = type;
        }

        /**
         * Resolves he TextureEntry, or returns the same instance if already resolved..
         *
         * @param properties The properties.
         * @return The resolved TextureEntry.
         */
        public TextureEntry resolve(@Nonnull Map<String, String> properties) {

            if ( type == EntryType.PROPERTY ) {
                String prop = properties.get(property);
                if ( prop != null ) {
                    ResourceLocation tex = new ResourceLocation(prop);
                    return new TextureEntry(key, name, layerIndex, renderLayer, tex, null, EntryType.TEXTURE);
                }
            }
            return this;
        }

        /**
         * Parses a TextureEntry from key and value, See the design doc for more info on the format.
         * //TODO, make design doc available somewhere.
         *
         * @param key     The Key.
         * @param texture The Value.
         * @return
         */
        public static TextureEntry parse(@Nonnull String key, @Nonnull String texture) {

            boolean isProperty = texture.startsWith("model_property:");

            String name;
            String layerIndex = "layer0";
            BlockRenderLayer renderLayer = BlockRenderLayer.SOLID;

            String[] segs = key.split(":");
            if ( segs.length > 3 || segs.length == 0 ) {
                throw new RuntimeException("Malformed TextureEntry key. Greater than 3 segments. " + key);
            }
            name = segs[segs.length - 1];
            if ( segs.length == 2 ) {
                renderLayer = parseLayer(segs[0]);
            } else if ( segs.length == 3 ) {
                layerIndex = segs[0];
                renderLayer = parseLayer(segs[1]);
            }
            ResourceLocation tex = !isProperty ? new ResourceLocation(texture) : null;
            String property = isProperty ? texture.substring(15) : null;
            return new TextureEntry(key, name, layerIndex, renderLayer, tex, property, isProperty ? EntryType.PROPERTY : EntryType.TEXTURE);
        }

        /**
         * Checks if this TextureEntry is applicable to the provided layerIndex and renderLayer.
         *
         * @param layerIndex  The layerIndex.
         * @param renderLayer The renderLayer.
         * @return if it matches.
         */
        public boolean equals(@Nonnull String layerIndex, @Nonnull BlockRenderLayer renderLayer) {

            return this.layerIndex.equals(layerIndex) && this.renderLayer.equals(renderLayer);
        }

        @Override
        public boolean equals(Object obj) {

            if ( super.equals(obj) ) {
                return true;
            }
            if ( !(obj instanceof TextureEntry) ) {
                return false;
            }
            TextureEntry other = (TextureEntry) obj;
            return other.key.equals(key)//
                    && other.name.equals(name)//
                    && other.layerIndex.equals(layerIndex)//
                    && other.renderLayer.equals(renderLayer)//
                    && Objects.equals(other.texture, texture)//
                    && Objects.equals(other.property, property)//
                    && other.type.equals(type);
        }

        @Override
        public int hashCode() {

            int result = 1;
            result = 31 * result + key.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + layerIndex.hashCode();
            result = 31 * result + renderLayer.hashCode();
            result = 31 * result + (texture != null ? texture.hashCode() : 0);
            result = 31 * result + (property != null ? property.hashCode() : 0);
            result = 31 * result + type.hashCode();
            return result;
        }

    }

    public enum EntryType {
        TEXTURE, PROPERTY
    }

}