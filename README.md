# NoSparkInPurpur

[MCBBS](https://www.mcbbs.net/thread-1358769-1-1.html) | [SpigotMC](https://www.spigotmc.org/resources/103108/)

Some people don't need spark, or even timings, they just need a simple and intuitive /tps command using the bukkit.command.tps permission node.

They don't care which of timings or spark has an impact on server performance because the impact on their server is completely negligible.

~~PurpurMC is supposed to give users a choice, but they don't, that's why I made this plugin.~~

After Purpur#1695 build, add below to your jvm flag to disble spark: `-DPurpur.IReallyDontWantSpark=true`

---

有些人并不需要spark，甚至timings，他们只需要一个简洁直观的使用bukkit.command.tps权限节点的/tps指令。

他们不在乎timings和spark到底哪个会对服务器性能产生影响，因为这对他们服务器的影响基本不存在。

~~PurpurMC应该给用户一个选择，但他们没有，这就是为什么我制作了这个插件。~~

在Purpur的第1695个构建开始，你只需要添加以下参数即可禁用spark：`-DPurpur.IReallyDontWantSpark=true`

## How does it work?

**The plugin will detect if spark is already loaded, if so, the plugin will unload spark, and then the plugin will unload itself.**

**This plugin cannot prevent Purpur from downloading spark since PurpurMC lets spark take precedence over all plugin loading.**

---

插件会检测spark是否已被加载，如果是，插件将会卸载spark，然后，插件也会卸载自身。

由于PurpurMC让spark优先于所有插件加载，因此此插件无法阻止Purpur下载spark。

**Before:**
![before](https://user-images.githubusercontent.com/45266046/177168213-f97c3d5a-f05b-4abf-a312-f59db135f21a.png)

**After:**
![after](https://user-images.githubusercontent.com/45266046/177168243-3ee910ca-e9f9-4918-944e-84b90ff97ae1.png)

## How to enable Timings?

Since plugins cannot override timings commands, to enable Timings you need to add the following to the `commands.yml` file:

由于插件无法覆盖timings命令，因此要开启Timings，你需要将以下内容添加到`commands.yml`文件：

```yaml
aliases:
  timings: 
  - bukkit:timings $1-
```
