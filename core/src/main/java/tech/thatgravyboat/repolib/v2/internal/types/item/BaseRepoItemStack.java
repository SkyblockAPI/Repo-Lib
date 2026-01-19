package tech.thatgravyboat.repolib.v2.internal.types.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record BaseRepoItemStack(

        String internalId,
        String recipeId,

        String itemType,
        String model,

        JsonElement name,
        JsonElement lore,

        boolean sackable,
        boolean museumable,


        int color,
        boolean enchantmentOverride,

        JsonObject extraData

        /*
        internalId,
        recipeId,

        itemType,
        model,

        name,
        lore,

        sackable,
        museumable,

        color,
        enchantmentOverride,

        extraData

        Category,
        Tier,
        Craftin Requirements,
        Power,
        Stats,
        Rift Stats,

        Ability Stats,
        Requirements,
        Essence,
        Essence Cost,

        Dungeon Requirements,
        Gemstone Slots,

        Tradable,
        Auctionable,
        Reforgeable,
        Enchantable,
        Museumable,
        Bazzaarable,
        Salvageable,
        Rift Item,

        Reforge,
        Reforge Type,
        Reforge Requirements,

        Collection,

        Upgrading,
        Scaled Stats,
        Essence Upgrading,
        Skins

        Rift Name
        Soulbound,
        Rift Transfer Status (none, exportable, transferable),
        Motes Value,
        Coin Value,
         */
) {


}
