# journeymap_sharenotice — ウェイポイントを共有したとき、送った本人にも1行出す

**サーバ専用**（`side=SERVER`）。**部員のクライアントには配らない。**
コードは2クラスだけで、JourneyMap が**公開している窓口**しか使わない。

## 何を解決するか

JourneyMap 6.0 の「共有」は、**相手にしか知らせない。**
送った側は画面が閉じるだけで、成功しても何も返らない。

部員の報告（2026-09-08）:

> ウェイポイントを共有したときに出るチャット通知が周りには出るが自分には出ない

⚠ **「全部選ぶ」で全員へ渡しても同じ。** 送り主は宛先から**2か所で外される**:

| どこ | 何をしているか |
| - | - |
| 宛先の一覧を作る `ServerWaypointHandler.onShareTargetsRequest()` | `if (!dto.getUuid().equals(senderUuid))` ＝ **自分を一覧に載せない** |
| 「全員へ」で送る `onShareSubmit()` | `filter(uuid -> !uuid.toString().equals(senderUuid))` ＝ **自分を宛先から外す** |

成功したときに送り主へ返るものは1つも無い（失敗したときだけ `sendCrudFailure` が返る）。

⚠ **6.0.4 でも直っていない。** Modrinth から 6.0.4 を落として 6.0.2 と突き合わせたところ、
`ServerWaypointHandler` / `ClientPacketHandler` / `ShareWaypointPopup` の class が
**sha1 まで同一**だった（2026-09-08）。上流への要望は別途出す。

## やること

`ServerEventRegistry.WAYPOINT_SHARE_SUBMIT_EVENT` を購読して、送り主へ1行返す。

```text
「拠点うら」を共有しました (x:123, y:64, z:-45)
```

⚠ **名前も人数も出さない**（2026-09-08 の判断）。
名前を出すと、**受け取らない設定にしている部員が居ることが送り主に分かってしまう。**

## ⚠ 決めごと（読む前に触ると壊す）

| ⚠ | なぜ |
| - | - |
| **座標を角括弧で書かない** | JourneyMap のクライアントは、受け取ったチャットの中の `[x:…, y:…, z:…]` を**押せるウェイポイントに描き替える**（`WaypointParser.getWaypointStrings` が `[` と `]` の間だけを見る）。押すと**同じ地点がもう1つできる**。だから丸括弧 |
| **「誰にも届かなかった」は出せない** | 届いた相手ごとに発火する `WAYPOINT_PENDING_RECEIVED_EVENT` は **API に宣言が在るだけで、6.0.4 の本体は1か所も発火していない**（jar を読んで確認）。この窓口が持つ宛先の数は**受け取らない設定・拒否設定で減る前の数**なので、人数として出すと嘘になる |
| **共有した地点1つにつき1行** | 窓口が1つずつ発火する。まとめて共有すると、その数だけ並ぶ |
| **API のクラスを jar に同梱しない** | 公式の手引が "No shading is needed" と指示している（実行時は JourneyMap 本体が同じクラスを持つ）。`compileOnly` で取る |
| **プラグインのクラスを他所から参照しない** | 同じく公式の指示。JourneyMap が入っていないときに読み込まれないようにするため。`JourneyMapShareNotice` は API を1つも参照しない |

## 免許の話（⚠ ここを踏み外さない）

⚠ **JourneyMap 本体は全権利留保。** jar の `license.txt` は、書面の許可なく変えること・逆アセンブルすること・配ることを禁じている:

> This mod may not be altered, file-hosted, re-packaged, reverse-engineered, or distributed in part or in whole without express written permission by Mark Woodman (techbrew).

公式の licensing の頁（`teamjm.github.io/journeymap-docs/latest/about/licensing/`）はもっと細かく、
仕組みを調べるための逆アセンブルは許し、本体を変えることは禁じている。許していること:

> Decompile the mod for the purpose of inspecting how it works and what it does, under the relevant laws in the USA.

禁じていること:

> Modify the mod in any way, even for personal use.

この MOD は本体に一切触らず、**別に公開されている API** だけを使う。
`journeymap-api` の `docs/license.md` が明記している:

> **You MAY:** Write your own code that uses the API source code herein as a dependency.

依存は `info.journeymap:journeymap-api-forge:1.20.1-2.0.0`（`maven.blamejared.com`）。

## ビルド

```bash
cd eruto-mc/journeymap_sharenotice
JAVA_HOME=<Java 17> ./gradlew build --no-daemon
```

⚠ **JDK は 17**（Forge 47 / ForgeGradle 6.0 系）。手元では
`AppData/Roaming/PrismLauncher/java/java-runtime-gamma`。

## 置き場

`worlds/world-3/dev/server/mods/` と `server-play/mods/` の2か所だけ。
⚠ **`instance/mods` には置かない**——置くと AutoModpack が部員へ配ってしまう。
