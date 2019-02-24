/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.bronzeman;

import javax.inject.Inject;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.util.Text;

import java.util.ArrayList;
import java.util.List;

@PluginDescriptor(
        name = "Bronzeman",
        description = "Start of bronzeman project",
        tags = {"gay"},
        enabledByDefault = false
)
public class BronzemanPlugin extends Plugin
{
    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private BronzemanConfig config;

    @Provides
    BronzemanConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BronzemanConfig.class);
    }

    private boolean Check = false;

    private List<String> Data = new ArrayList<String>();

    @Override
    protected void startUp() throws Exception
    {
        String configData = config.getBronzemanData();
        List<String> l = Text.fromCSV(configData);
        for (int i = 0; i< l.size(); i++) {
            Data.add(l.get(i));
        }
    }

    private void save() {
        String configData = Text.toCSV(Data);
        config.setBronzemanData(configData);
    }

    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged itemContainerChanged)
    {
        ItemContainer itemContainer = itemContainerChanged.getItemContainer();
        ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);

        Item[] items = inv.getItems();
        for (int i = 0; i < items.length; i++) {
            Integer id = items[i].getId();
            if (!Data.contains(""+id)) {
                Data.add("" + id);
                sendUnlockMessage(items[i]);
                save();
            }
        }
    }

    private void sendUnlockMessage(Item itemUnlocked)
    {
        ItemComposition item = itemManager.getItemComposition(itemUnlocked.getId());
        final ChatMessageBuilder message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append("Item unlocked: ")
                .append(item.getName())
                .append(".")
                .append(ChatColorType.NORMAL);

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.EXAMINE_ITEM)
                .runeLiteFormattedMessage(message.build())
                .build());
    }

    private void setGEItems()
    {
        Widget widget = client.getWidget(WidgetInfo.TO_GROUP(10616885), WidgetInfo.TO_CHILD(10616885));
        Player player = client.getLocalPlayer();
        if (widget == null)
        {
            return;
        }

        Widget[] widgetChildren = widget.getChildren();

        if (widgetChildren == null)
        {
            return;
        }

        for (int i = 0; i < widgetChildren.length; i++) {
            int itemid = widgetChildren[i].getItemId();
            if (!Data.contains(""+itemid)) {

                Widget found = null;

                for (int x = 0; x < widgetChildren.length; x++) {

                    if (widgetChildren[x].getItemId() == itemid && widgetChildren[x].getName() == "overlay") {
                        found = widgetChildren[x];
                    }
                }

                if (found != null) {
                    continue;
                }

                widgetChildren[i-2].setOnOpListener(null);
                widgetChildren[i-2].revalidate();

                Widget Overlay = widget.createChild(widget.getChildren().length, WidgetType.RECTANGLE);
                Overlay.setOriginalY(widgetChildren[i - 2].getOriginalY());
                Overlay.setOriginalX(widgetChildren[i - 2].getOriginalX());
                Overlay.setOriginalHeight(widgetChildren[i - 2].getOriginalHeight());
                Overlay.setOriginalWidth(widgetChildren[i - 2].getOriginalWidth());
                Overlay.setScrollHeight(widgetChildren[i - 2].getScrollHeight());
                Overlay.setHeightMode(widgetChildren[i - 2].getHeightMode());
                Overlay.setWidthMode(widgetChildren[i - 2].getWidthMode());
                Overlay.setYPositionMode(widgetChildren[i - 2].getYPositionMode());
                Overlay.setXPositionMode(widgetChildren[i - 2].getXPositionMode());

                Overlay.setTextColor(0);
                Overlay.setOpacity(100);
                Overlay.setItemId(widgetChildren[i].getItemId());
                Overlay.setText(widgetChildren[i - 2].getText());
                Overlay.setName("overlay");
                Overlay.setBorderType(widgetChildren[i - 2].getBorderType());

                Overlay.setFilled(true);
                Overlay.setNoClickThrough(true);

                Overlay.revalidate();
            }
        }

        return;
    }

    @Subscribe
    public void onWidgetHiddenChanged(WidgetHiddenChanged event)
    {
        Widget widget = event.getWidget();
        int index = widget.getId();
        if (index == 10616872) {
            this.Check = !widget.isHidden();
        }
        return;
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (Check) {
            setGEItems();
        }
    }
}
