package net.erutobusiness.journeymapsharenotice.plugin;

import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.event.ServerEventRegistry;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.server.IServerAPI;
import journeymap.api.v2.server.IServerPlugin;
import journeymap.api.v2.server.event.WaypointShareSubmitEvent;
import net.erutobusiness.journeymapsharenotice.JourneyMapShareNotice;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 共有した本人へ「何を共有したか」を1行返す。
 *
 * <p>⚠ <b>名前も人数も出さない</b>（2026-09-08 のあなたの判断
 * 「相手の名前も人数もいらない。どの座標を共有したかだけ分かればいい」）。
 * 名前を出すと、受け取らない設定にしている部員が居ることが送り主に分かってしまう。
 *
 * <p>⚠⚠ <b>座標を角括弧で書かない。</b> JourneyMap のクライアントは、受け取ったチャットの中の
 * {@code [x:…, y:…, z:…]} を<b>押せるウェイポイントに描き替える</b>
 * （{@code WaypointParser.getWaypointStrings} が {@code [} と {@code ]} の間だけを見る）。
 * 押すと<b>同じ地点がもう1つできる</b>ので、丸括弧で書く。
 *
 * <p>⚠ <b>「誰にも届かなかった」は出せない。</b> 届いた相手ごとに発火する
 * {@code WAYPOINT_PENDING_RECEIVED_EVENT} は API に宣言があるだけで、
 * <b>JourneyMap 6.0.4 の本体は1か所も発火していない</b>（jar を読んで確認）。
 * この窓口が持つ宛先の数は<b>受け取らない設定・拒否設定で減る前の数</b>なので、
 * 人数として出すと嘘になる。だから数に触れない。
 *
 * <p>⚠ 共有した<b>ウェイポイント1つにつき1行</b>出る（窓口が1つずつ発火するため）。
 * まとめて共有すると、その数だけ並ぶ。
 */
@JourneyMapPlugin(apiVersion = "2.0.0")
public class ShareNoticePlugin implements IServerPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(JourneyMapShareNotice.MODID);

    @Override
    public String getModId() {
        return JourneyMapShareNotice.MODID;
    }

    @Override
    public void initialize(IServerAPI api) {
        ServerEventRegistry.WAYPOINT_SHARE_SUBMIT_EVENT
                .subscribe(JourneyMapShareNotice.MODID, this::onShareSubmit);
        LOG.info("[journeymap_sharenotice] 共有の窓口に登録した（送り主へ1行返す）");
    }

    private void onShareSubmit(WaypointShareSubmitEvent event) {
        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null || event.senderUUID == null) {
                return;
            }
            Waypoint wp = event.waypoint;
            if (wp == null) {
                return;
            }
            // ⚠ 名前は空のことがある（座標だけで作った地点）。そのときは座標だけ出す。
            String name = wp.getName();
            String body = (name == null || name.isEmpty())
                    ? String.format("ウェイポイントを共有しました (x:%d, y:%d, z:%d)",
                            wp.getX(), wp.getY(), wp.getZ())
                    : String.format("「%s」を共有しました (x:%d, y:%d, z:%d)",
                            name, wp.getX(), wp.getY(), wp.getZ());
            // ⚠ 窓口はネットワークの処理から呼ばれる。本体のスレッドへ渡してから送る。
            server.execute(() -> {
                ServerPlayer sender = server.getPlayerList().getPlayer(event.senderUUID);
                if (sender != null) {
                    sender.sendSystemMessage(
                            Component.literal(body).withStyle(ChatFormatting.GRAY));
                }
            });
        } catch (Throwable t) {
            // ⚠ ここで落ちても共有そのものは止めない（この窓口は取り消しもできる位置に在る）。
            LOG.warn("[journeymap_sharenotice] 送り主への1行を出せなかった: {}", t.toString());
        }
    }
}
