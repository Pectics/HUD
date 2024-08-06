package com.peckot.hud.core.data;

import com.peckot.hud.HUD;
import com.peckot.hud.core.settings.PointsSettings;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.PlayerHudsHolderWrapper;
import me.clip.placeholderapi.PlaceholderAPI;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public class PointsHud extends PAPIHud<PointsSettings>
{
    private static boolean HAS_CHECKED_PLACEHOLDER = false;

    private final Player player;

    private int prevPoints;
    private String prevAmount;

    @Nullable
    BukkitTask arrowRemoveSchedule;
    @Nullable
    private FontImageWrapper currentArrow;

    public PointsHud(String placeholder,
                     PlayerHudsHolderWrapper holder,
                     PointsSettings settings) throws NullPointerException
    {
        super(placeholder, holder, settings);
        this.player = holder.getPlayer();

        this.prevPoints = PlayerPoints.getInstance().getAPI().look(player.getUniqueId());

        hud.setVisible(true);
    }

    @Override
    public RenderAction refreshRender()
    {
        return refreshRender(false);
    }

    @Override
    public RenderAction refreshRender(boolean forceRender)
    {
        if (hidden)
            return RenderAction.HIDDEN;

        if (!hudSettings.isEnabledInWorld(player.getWorld()))
        {
            hud.setVisible(false); //I think this will cause problems
            return RenderAction.HIDDEN;
        }
        
        int points = PlayerPoints.getInstance().getAPI().look(player.getUniqueId());
        if(points != prevPoints)
        {
            if (points > prevPoints)
                currentArrow = hudSettings.char_arrow_up;
            else
                currentArrow = hudSettings.char_arrow_down;
            
            prevPoints = points;
            
            if(arrowRemoveSchedule != null) arrowRemoveSchedule.cancel();
            arrowRemoveSchedule = Bukkit.getScheduler().runTaskLaterAsynchronously(HUD.inst(), () -> {
                currentArrow = null;
                arrowRemoveSchedule.cancel();
                arrowRemoveSchedule = null;
                refreshRender(true);
            }, 20 * 3);
        }

        String amount = PlaceholderAPI.setPlaceholders(holder.getPlayer(), placeholder);
        if (!forceRender && currentArrow == null && amount.equals(prevAmount))
            return RenderAction.SAME_AS_BEFORE;

        if(!HAS_CHECKED_PLACEHOLDER && amount.equals(placeholder))
        {
            HUD.inst().getLogger().severe(
                    ChatColor.RED +
                            "Failed to replace PAPI placeholder for player " + player.getName() + ". '" + placeholder + "' probably doesn't exists. " +
                            "Check HUD/config.yml file and check if you have the correct economy plugin installed."
            );
            prevAmount = amount;
            HAS_CHECKED_PLACEHOLDER = true;
            return RenderAction.HIDDEN;
        }

        imgsBuffer.clear();

        if(currentArrow != null)
            imgsBuffer.add(currentArrow);

        hudSettings.appendAmountToImages(amount, imgsBuffer);
        imgsBuffer.add(hudSettings.icon);

        hud.setFontImages(imgsBuffer);
        adjustOffset();

        prevAmount = amount;

        return RenderAction.SEND_REFRESH;
    }

    @Override
    public void deleteRender()
    {
        hud.clearFontImagesAndRefresh();

        if (arrowRemoveSchedule != null)
            arrowRemoveSchedule.cancel();
        arrowRemoveSchedule = null;
    }
}
