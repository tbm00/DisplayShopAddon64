# DisplayShopAddon64
A spigot plugin that expands DisplayShops and CommandPanels for MC64.

Created by tbm00 for play.mc64.wtf.


## Dependencies
- **Java 17+**: REQUIRED
- **Spigot 1.18.1+**: UNTESTED ON OLDER VERSIONS


## Commands
#### Player Commands
- `/testshop help` Display help menu
- `/testshop` Open shop category gui
- `/testshop [pog/ores/tools/blocks/drops/food/farming]` Open shop category gui
- `/testshop [random item name]` Search all player DisplayShops
- `/testshop buy <#>` Buy DisplayShops
- `/testshop advertise` Broadcast shop to all players
- `/testshop deposit-all max/<#>` Deposit money into all your shops
- `/testshop withdraw-all max/<#>` Withdraw money from all your shops

#### Admin Commands
- none


## Permissions
#### Player Permissions
- `displayshopaddon64.player` Ability to use player features *(default: everyone)*

#### Admin Permissions
- `displayshopaddon64.admin` Ability to use admin features *(default: op)*


## Config
```
# DisplayShopAddon64 v0.0.1-beta by @tbm00
# https://github.com/tbm00/DisplayShopAddon64

enabled: true

lang:
  prefix: "&8[&fShop&8] &7"

feature:
  enabled: true
  dsMaxStoredBalance: 20000000
  guiDefaultCategory: "shoppog"
```