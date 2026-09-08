package net.erutobusiness.journeymapsharenotice;

import net.minecraftforge.fml.common.Mod;

/**
 * ウェイポイントを共有したとき、<b>送った本人のチャットにも1行出す</b>。
 *
 * <p>なぜ要るか: JourneyMap 6.0 の「共有」は、<b>相手にしか知らせない</b>。
 * 送った側は画面が閉じるだけで、成功しても何も返らない
 * （失敗したときだけ {@code sendCrudFailure} が返る）。
 * 相手を「全部選ぶ」で全員にしても同じで、<b>送り主は宛先から2か所で外される</b>——
 * 宛先の一覧を作る {@code onShareTargetsRequest} が自分を載せず、
 * 「全員へ」の {@code onShareSubmit} も自分を {@code filter} で外す。
 *
 * <p>⚠ <b>JourneyMap 6.0.4 でも直っていない。</b> 6.0.2 と 6.0.4 で
 * {@code ServerWaypointHandler} / {@code ClientPacketHandler} / {@code ShareWaypointPopup} の
 * class が sha1 まで同一だった（2026-09-08 に確かめた）。上流への要望は別途出す。
 *
 * <p>⚠ <b>このクラスは JourneyMap の API を1つも参照しない。</b>
 * 参照は {@code plugin.ShareNoticePlugin} 側だけに閉じる——
 * 公式の手引が「JourneyMap が入っていないときに読み込まれないよう、
 * プラグインのクラスを他所から参照するな」と指示しているため。
 *
 * <p>サーバ専用（{@code side=SERVER}）。部員のクライアントには配らない。
 */
@Mod(JourneyMapShareNotice.MODID)
public class JourneyMapShareNotice {
    public static final String MODID = "journeymap_sharenotice";

    public JourneyMapShareNotice() {
        // ⚠ ここでは何も登録しない。JourneyMap が在るときだけ
        //    @JourneyMapPlugin の付いたクラスが向こうから呼ばれる。
    }
}
