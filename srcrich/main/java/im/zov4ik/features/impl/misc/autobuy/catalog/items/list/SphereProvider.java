package im.zov4ik.features.impl.misc.autobuy.catalog.items.list;

import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.customitem.CustomItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.defaultsetpricec.Defaultpricec;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SphereProvider {
    public static List<AutoBuyableItem> getSpheres() {
        List<AutoBuyableItem> spheres = new ArrayList<>();

        
        
        long chaosMost = ((long)-1582985800 << 32) | (1375286202L & 0xFFFFFFFFL);
        long chaosLeast = ((long)-1125478243 << 32) | (595287099L & 0xFFFFFFFFL);
        UUID chaosUuid = new UUID(chaosMost, chaosLeast);
        NbtCompound chaosNbt = createSphereWithAttributes(
                chaosUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODY0MTkwMCwKICAicHJvZmlsZUlkIiA6ICIxNzRjZmRiNGEzY2I0M2I1YmZjZGU0MjRjM2JiMmM2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJhZWwxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lN2E3YWU3Y2RjZjYxNmU4YjdhNDIyMWE2MjFiMjQzNTc1M2M2MGVkNmEyNThlYTA2MGRhZTMwMDJmZmU5ZTI4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new AttributeData("minecraft:generic.max_health", -4.0, 0, "offhand"),
                new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.attack_damage", 3.0, 0, "offhand"),
                new AttributeData("minecraft:generic.movement_speed", 0.07, 1, "offhand"),
                new AttributeData("minecraft:generic.attack_speed", 0.13, 1, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Хаоса", chaosNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Хаоса"), null, null));

        
        
        long satyrMost = ((long)-2101547208 << 32) | (2058105556L & 0xFFFFFFFFL);
        long satyrLeast = ((long)-1495491604 << 32) | (1400184240L & 0xFFFFFFFFL);
        UUID satyrUuid = new UUID(satyrMost, satyrLeast);
        NbtCompound satyrNbt = createSphereWithAttributes(
                satyrUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODYwODUyOCwKICAicHJvZmlsZUlkIiA6ICJkMTQ4NjFiM2UwZmM0Njk5OTFlMTcyNTllMzdiZjZhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJyYXhpdG9jbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NzFhOWE0OThiNGZhNWVjNDkzNjJmOWJjODhlZGE0ZjUyYjA0ZGU0OWQ3NWFhM2NhMzMyYTFmZWExYWEwZTU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new AttributeData("minecraft:generic.attack_damage", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.attack_speed", 0.15, 1, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Сатира", satyrNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Сатира"), null, null));

        
        
        long bestiaMost = ((long)-1896649230 << 32) | (-398640094L & 0xFFFFFFFFL);
        long bestiaLeast = ((long)-1320063783 << 32) | (945063190L & 0xFFFFFFFFL);
        UUID bestiaUuid = new UUID(bestiaMost, bestiaLeast);
        NbtCompound bestiaNbt = createSphereWithAttributes(
                bestiaUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0MzgzNDkzMCwKICAicHJvZmlsZUlkIiA6ICI1MzUzNWIxN2M0ZDY0NWQ0YWUwY2U2ZjM4Zjk0NTFjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJVYml2aXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQxMWFjMTczODFiOWZjZTliYWIzYzcyYWZkYjdmMTk4NTcwZGFmNDczMmJkODExZDMxYzIyN2Q4MGZhMzliMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new AttributeData("minecraft:generic.armor", 1.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", 4.0, 0, "offhand"),
                new AttributeData("minecraft:generic.movement_speed", 0.1, 1, "offhand"),
                new AttributeData("minecraft:generic.attack_speed", 0.1, 1, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Бестии", bestiaNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Бестии"), null, null));

        
        
        long aresMost = ((long)2124864246 << 32) | (465122563L & 0xFFFFFFFFL);
        long aresLeast = ((long)-1511078600 << 32) | (-555424585L & 0xFFFFFFFFL);
        UUID aresUuid = new UUID(aresMost, aresLeast);
        NbtCompound aresNbt = createSphereWithAttributes(
                aresUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzc3NDI1NSwKICAicHJvZmlsZUlkIiA6ICJhYWMxYjA2OWNkMjE0NWE2ODNlNzQxNzE4MDcxMGU4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdXNhbXUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE2YWRjNmJhZmNiNTdmZDcwN2RlZTdkZDZhNzM2ZmUxMjY3MTFkNTNhMWZkNmNlNzg5ZGE0MWIzYmUxM2YyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new AttributeData("minecraft:generic.attack_damage", 6.0, 0, "offhand"),
                new AttributeData("minecraft:generic.armor", -2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", -2.0, 0, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Ареса", aresNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Ареса"), null, null));

        
        
        long hydraMost = ((long)-1308798306 << 32) | (1128020291L & 0xFFFFFFFFL);
        long hydraLeast = ((long)-1307054059 << 32) | (-1317369961L & 0xFFFFFFFFL);
        UUID hydraUuid = new UUID(hydraMost, hydraLeast);
        NbtCompound hydraNbt = createSphereWithAttributes(
                hydraUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODUzMjE4MywKICAicHJvZmlsZUlkIiA6ICI1OGZmZWI5NTMxNGQ0ODcwYTQwYjVjYjQyZDRlYTU5OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTa2luREJuZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzYzExOGQ2OTZkOTEwZTU0ZGUwMmNhNGQ4MDc1NDNmOWIxOGMwMDhjOTgzOGQyZmY2OTM3NzYyMmZiMWQzMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new AttributeData("minecraft:generic.max_health", 4.0, 0, "offhand"),
                new AttributeData("minecraft:generic.armor", 2.0, 0, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Гидры", hydraNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Гидры"), null, null));

        
        
        long ikarMost = ((long)1858435695 << 32) | (-517000716L & 0xFFFFFFFFL);
        long ikarLeast = ((long)-1346102858 << 32) | (1389869685L & 0xFFFFFFFFL);
        UUID ikarUuid = new UUID(ikarMost, ikarLeast);
        NbtCompound ikarNbt = createSphereWithAttributes(
                ikarUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODU4MjQ5MSwKICAicHJvZmlsZUlkIiA6ICJhZWNkODIxZTQyYzE0ZDJlOThmNTA1OTg1MWI5OWMzNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2RyaVgyMDc1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M2ODAzZTZkNTY2N2EyZDYxMDYyOGJjM2IzMmY4NjNjZGE0OTVjNDY1NjE2ZGU2NTVjYjMyOTkzM2I2MWFmNzciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new AttributeData("minecraft:generic.attack_damage", 2.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", 2.0, 0, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Икара", ikarNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Икара"), null, null));

        
        
        long eridaMost = ((long)-122635583 << 32) | (2062758360L & 0xFFFFFFFFL);
        long eridaLeast = ((long)-2035264015 << 32) | (874042263L & 0xFFFFFFFFL);
        UUID eridaUuid = new UUID(eridaMost, eridaLeast);
        NbtCompound eridaNbt = createSphereWithAttributes(
                eridaUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlNGUyZjEwNDdmM2VjNmU5ZTQ1OTE4NDczOWUzM2I3YzFmYzYzYWQ4MjAyYmRhYjlmMDI0NTA4YWRkMjNlNWIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new AttributeData("minecraft:generic.luck", 1.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", 2.0, 0, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Эрида", eridaNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Эрида"), null, null));

        
        
        long athenaMost = ((long)-1256824426 << 32) | (1078545174L & 0xFFFFFFFFL);
        long athenaLeast = ((long)-2056642511 << 32) | (594802538L & 0xFFFFFFFFL);
        UUID athenaUuid = new UUID(athenaMost, athenaLeast);
        NbtCompound athenaNbt = createSphereWithAttributes(
                athenaUuid.toString(),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJTcGhlcmVBdGhlbmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNmOWVlZGEzYmEyM2ZlMTQyM2M0MDM2ZTdkZDBhNzQ0NjFkZmY5NmJhZGM1YjJmMmI5ZmFhN2NjMTZmMzgyZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new AttributeData("minecraft:generic.attack_speed", 0.15, 1, "offhand"),
                new AttributeData("minecraft:generic.movement_speed", 0.15, 1, "offhand"),
                new AttributeData("minecraft:generic.attack_damage", 3.0, 0, "offhand"),
                new AttributeData("minecraft:generic.max_health", -2.0, 0, "offhand")
        );
        spheres.add(new CustomItem("[★] Сфера Афины", athenaNbt, Items.PLAYER_HEAD, Defaultpricec.getPrice("Сфера Афины"), null, null));

        return spheres;
    }

    
    private static NbtCompound createSphereWithAttributes(String headUuid, String texture, AttributeData... attributes) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("HideFlags", 1);

        
        NbtCompound skullOwner = new NbtCompound();
        skullOwner.putUuid("Id", UUID.fromString(headUuid));
        NbtCompound properties = new NbtCompound();
        NbtList textures = new NbtList();
        NbtCompound textureNbt = new NbtCompound();
        textureNbt.putString("Value", texture);
        textures.add(textureNbt);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        nbt.put("SkullOwner", skullOwner);

        
        NbtList attributeList = new NbtList();
        for (AttributeData attr : attributes) {
            NbtCompound attributeNbt = new NbtCompound();
            attributeNbt.putString("AttributeName", attr.attributeName);
            attributeNbt.putDouble("Amount", attr.amount);
            attributeNbt.putInt("Operation", attr.operation);
            attributeNbt.putString("Slot", attr.slot);
            attributeNbt.putString("Name", UUID.randomUUID().toString());
            attributeNbt.putIntArray("UUID", new int[]{
                    (int)(Math.random() * Integer.MAX_VALUE),
                    (int)(Math.random() * Integer.MAX_VALUE),
                    (int)(Math.random() * Integer.MAX_VALUE),
                    (int)(Math.random() * Integer.MAX_VALUE)
            });
            attributeList.add(attributeNbt);
        }
        nbt.put("AttributeModifiers", attributeList);

        return nbt;
    }

    
    private static class AttributeData {
        final String attributeName;
        final double amount;
        final int operation;
        final String slot;

        AttributeData(String attributeName, double amount, int operation, String slot) {
            this.attributeName = attributeName;
            this.amount = amount;
            this.operation = operation;
            this.slot = slot;
        }
    }
}
