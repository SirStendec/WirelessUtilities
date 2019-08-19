package com.lordmau5.wirelessutils.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomTrigger implements ICriterionTrigger<CustomTrigger.Instance> {

    private final ResourceLocation id;
    private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

    public CustomTrigger(String id) {
        this(new ResourceLocation(WirelessUtils.MODID, id));
    }

    public CustomTrigger(ResourceLocation resource) {
        id = resource;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(EntityPlayerMP player) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        Listeners listeners = this.listeners.get(playerAdvancements);
        if ( listeners != null )
            listeners.trigger();
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        Listeners listeners = this.listeners.get(playerAdvancementsIn);
        if ( listeners == null ) {
            listeners = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, listeners);
        }

        listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        CustomTrigger.Listeners listeners = this.listeners.get(playerAdvancementsIn);
        if ( listeners != null ) {
            listeners.remove(listener);
            if ( listeners.isEmpty() )
                this.listeners.remove(playerAdvancementsIn);
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId());
    }

    public static class Instance extends AbstractCriterionInstance {
        public Instance(ResourceLocation id) {
            super(id);
        }

        public boolean test() {
            return true;
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = new HashSet<>();

        public Listeners(PlayerAdvancements playerAdvancements) {
            this.playerAdvancements = playerAdvancements;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(Listener<Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger() {
            List<Listener<Instance>> list = null;
            for (Listener<Instance> listener : this.listeners) {
                if ( listener.getCriterionInstance().test() ) {
                    if ( list == null )
                        list = new ArrayList<>();
                    list.add(listener);
                }
            }

            if ( list != null ) {
                for (Listener<Instance> listener : list) {
                    listener.grantCriterion(playerAdvancements);
                }
            }
        }

    }
}
