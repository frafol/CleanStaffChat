package it.frafol.cleanstaffchat.velocity.utils;

import litebans.api.Database;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import space.arim.libertybans.api.LibertyBans;
import space.arim.libertybans.api.PlayerVictim;
import space.arim.libertybans.api.PunishmentType;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;

import java.util.List;
import java.util.UUID;

@UtilityClass
public class BansUtil {

    @SneakyThrows
    public boolean isLibertyBansMuted(UUID uuid) {
        Omnibus omnibus = OmnibusProvider.getOmnibus();
        LibertyBans libertyBans = omnibus.getRegistry().getProvider(LibertyBans.class).orElseThrow();;
        List<Punishment> punishments = libertyBans.getSelector()
                .selectionBuilder()
                .selectActiveOnly(true)
                .victim(PlayerVictim.of(uuid)).build().getAllSpecificPunishments().toCompletableFuture().get();
        for (Punishment punishment : punishments) {
            if (punishment.getType().equals(PunishmentType.MUTE)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLiteBansMuted(UUID uuid, String address) {
        return Database.get().isPlayerMuted(uuid, address);
    }
}
